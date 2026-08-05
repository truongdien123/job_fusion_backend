package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.AiServiceException;
import com.tma.job_fusion_backend.models.Skill;
import com.tma.job_fusion_backend.pojo.requests.JdGenerateRequest;
import com.tma.job_fusion_backend.pojo.responses.CriteriaAiGenerateResponse;
import com.tma.job_fusion_backend.pojo.responses.JdGenerateResponse;
import com.tma.job_fusion_backend.pojo.requests.CriteriaAiGenerateRequest;
import com.tma.job_fusion_backend.repositories.SkillRepository;
import com.tma.job_fusion_backend.repositories.query.SkillQueryRepository;
import com.tma.job_fusion_backend.services.JdAiService;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

@Slf4j
@Service
public class JdAiServiceImpl implements JdAiService {

    private final String URI = "/api/v1/jd/generate";
    private final String CRITERIA_URI = "/api/v1/jd/generate-criteria";

    private final WebClient webClient;
    private final SkillRepository skillRepository;
    private final SkillQueryRepository skillQueryRepository;

    public JdAiServiceImpl(
            @Value("${ai-service.url}") String apiBaseUrl,
            @Value("${ai-service.timeout-seconds}") int timeoutSeconds,
            SkillRepository skillRepository,
            SkillQueryRepository skillQueryRepository) {
        this.skillRepository = skillRepository;
        this.skillQueryRepository = skillQueryRepository;
        
        // Configure netty HttpClient with timeouts tailored for local LLM usage
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 10s connection timeout
                .responseTimeout(Duration.ofSeconds(timeoutSeconds)) // response timeout (90s default)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl(apiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    @Transactional
    public JdGenerateResponse generateJd(JdGenerateRequest request) {
        log.info("Sending request to AI JD Generator service for title: {}", request.getJobTitle());

        if (ObjectUtils.isNotEmpty(request.getKeySkills())) {
            List<String> cleanedSkills = request.getKeySkills().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(StringUtils::isNotEmpty)
                    .distinct()
                    .toList();

            if (!cleanedSkills.isEmpty()) {
                Set<String> lowerCaseSkillNames = cleanedSkills.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

                List<Skill> existingSkills = skillQueryRepository.findByNamesIgnoreCase(lowerCaseSkillNames);
                Set<String> existingNames = existingSkills.stream()
                        .map(Skill::getName)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

                List<Skill> newSkills = cleanedSkills.stream()
                        .filter(name -> !existingNames.contains(name.toLowerCase()))
                        .map(name -> {
                            Skill skill = new Skill();
                            skill.setName(name);
                            return skill;
                        })
                        .collect(Collectors.toList());

                if (!newSkills.isEmpty()) {
                    skillRepository.saveAll(newSkills);
                    log.info("Saved {} new skills in bulk", newSkills.size());
                }
            }
        }

        try {
            return this.webClient.post()
                    .uri(URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JdGenerateResponse.class)
                    .block(); // Synchronously wait for the response block
        } catch (WebClientResponseException e) {
            log.error("HTTP error from AI JD Generator: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with AI JD Generator service", e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    public CriteriaAiGenerateResponse generateJobCriteria(CriteriaAiGenerateRequest request) {
        log.info("Sending request to AI Criteria Generator service for title: {}", request.getJobTitle());
        try {
            return this.webClient.post()
                    .uri(CRITERIA_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CriteriaAiGenerateResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from AI Criteria Generator: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with AI Criteria Generator service", e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }
}
