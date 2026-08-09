package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.AiServiceException;
import com.tma.job_fusion_backend.exceptions.BadRequestException;
import com.tma.job_fusion_backend.pojo.requests.CvEvaluateRequest;
import com.tma.job_fusion_backend.pojo.responses.CvEvaluateResponse;
import com.tma.job_fusion_backend.services.CvAiService;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class CvAiServiceImpl implements CvAiService {

    private final String EVALUATE_URI = "/api/v1/cv/evaluate";

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CvAiServiceImpl(
            @Value("${ai-service.url}") String apiBaseUrl,
            @Value("${ai-service.timeout-seconds}") int timeoutSeconds) {
        
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl(apiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public CvEvaluateResponse evaluateCv(
            MultipartFile file,
            List<CvEvaluateRequest.JobCriterionInput> criteria,
            String jobTitle,
            String jobDescription,
            String jobRequirements
    ) {
        log.info("Sending direct file upload request to AI CV Evaluator service for file: {}", file.getOriginalFilename());

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource(), MediaType.MULTIPART_FORM_DATA)
                .filename(Objects.requireNonNull(file.getOriginalFilename()));

        String criteriaJson;
        try {
            criteriaJson = objectMapper.writeValueAsString(criteria);
        } catch (Exception e) {
            log.error("Failed to serialize job criteria to JSON string: {}", e.getMessage(), e);
            throw new BadRequestException(ErrorCode.INVALID_JOB_CRITERIA);
        }
        builder.part("criteria", criteriaJson);

        if (jobTitle != null) {
            builder.part("job_title", jobTitle);
        }
        if (jobDescription != null) {
            builder.part("job_description", jobDescription);
        }
        if (jobRequirements != null) {
            builder.part("job_requirements", jobRequirements);
        }

        String responseBody;
        try {
            responseBody = webClient.post()
                    .uri(EVALUATE_URI)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from AI CV Evaluator service: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with AI CV Evaluator service", e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }

        try {
            return objectMapper.readValue(responseBody, CvEvaluateResponse.class);
        } catch (Exception e) {
            log.error("Failed to deserialize response from AI CV Evaluator service: {}", e.getMessage(), e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        }
    }
}
