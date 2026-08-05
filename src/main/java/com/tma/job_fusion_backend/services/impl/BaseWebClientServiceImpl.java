package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.AiServiceException;
import com.tma.job_fusion_backend.services.BaseWebClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Log4j2
public class BaseWebClientServiceImpl implements BaseWebClientService {

    @Override
    public <T, R> R post(WebClient webClient, String serviceName, String uri, T request, Class<R> responseClass) {
        try {
            return webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(responseClass)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from {} POST {}: {} - {}", serviceName, uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with {} service at POST {}", serviceName, uri, e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    public <R> R get(WebClient webClient, String serviceName, String uri, Class<R> responseClass) {
        try {
            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(responseClass)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from {} GET {}: {} - {}", serviceName, uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with {} service at GET {}", serviceName, uri, e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    public <T, R> R put(WebClient webClient, String serviceName, String uri, T request, Class<R> responseClass) {
        try {
            return webClient.put()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(responseClass)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from {} PUT {}: {} - {}", serviceName, uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with {} service at PUT {}", serviceName, uri, e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    public <R> R delete(WebClient webClient, String serviceName, String uri, Class<R> responseClass) {
        try {
            return webClient.delete()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(responseClass)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from {} DELETE {}: {} - {}", serviceName, uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with {} service at DELETE {}", serviceName, uri, e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }
}
