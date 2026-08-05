package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.AiServiceException;
import com.tma.job_fusion_backend.services.BaseWebClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Log4j2
public class BaseWebClientServiceImpl implements BaseWebClientService {

    @Override
    public <T, R> R post(WebClient webClient, String serviceName, String uri, T request, Class<R> responseClass) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
        return execute(requestSpec, serviceName, "POST", uri, responseClass);
    }

    @Override
    public <T, R> R post(WebClient webClient, String serviceName, String uri, T request, ParameterizedTypeReference<R> responseType) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
        return execute(requestSpec, serviceName, "POST", uri, responseType);
    }

    @Override
    public <R> R get(WebClient webClient, String serviceName, String uri, Class<R> responseClass) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                .uri(uri);
        return execute(requestSpec, serviceName, "GET", uri, responseClass);
    }

    @Override
    public <R> R get(WebClient webClient, String serviceName, String uri, ParameterizedTypeReference<R> responseType) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                .uri(uri);
        return execute(requestSpec, serviceName, "GET", uri, responseType);
    }

    @Override
    public <T, R> R put(WebClient webClient, String serviceName, String uri, T request, Class<R> responseClass) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.put()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
        return execute(requestSpec, serviceName, "PUT", uri, responseClass);
    }

    @Override
    public <T, R> R put(WebClient webClient, String serviceName, String uri, T request, ParameterizedTypeReference<R> responseType) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.put()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
        return execute(requestSpec, serviceName, "PUT", uri, responseType);
    }

    @Override
    public <R> R delete(WebClient webClient, String serviceName, String uri, Class<R> responseClass) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.delete()
                .uri(uri);
        return execute(requestSpec, serviceName, "DELETE", uri, responseClass);
    }

    @Override
    public <R> R delete(WebClient webClient, String serviceName, String uri, ParameterizedTypeReference<R> responseType) {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.delete()
                .uri(uri);
        return execute(requestSpec, serviceName, "DELETE", uri, responseType);
    }

    private <R> R execute(WebClient.RequestHeadersSpec<?> requestSpec, String serviceName, String method, String uri, Class<R> responseClass) {
        try {
            return requestSpec
                    .retrieve()
                    .bodyToMono(responseClass)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from {} {} {}: {} - {}", serviceName, method, uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with {} service at {} {}", serviceName, method, uri, e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }

    private <R> R execute(WebClient.RequestHeadersSpec<?> requestSpec, String serviceName, String method, String uri, ParameterizedTypeReference<R> responseType) {
        try {
            return requestSpec
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP error from {} {} {}: {} - {}", serviceName, method, uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(ErrorCode.AI_SERVICE_ERROR, e);
        } catch (Exception e) {
            log.error("Error communicating with {} service at {} {}", serviceName, method, uri, e);
            throw new AiServiceException(ErrorCode.AI_SERVICE_UNAVAILABLE, e);
        }
    }
}
