package com.tma.job_fusion_backend.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

public interface BaseWebClientService {

    <T, R> R post(WebClient webClient, String serviceName, String uri, T request, Class<R> responseClass);

    <T, R> R post(WebClient webClient, String serviceName, String uri, T request, ParameterizedTypeReference<R> responseType);

    <R> R get(WebClient webClient, String serviceName, String uri, Class<R> responseClass);

    <R> R get(WebClient webClient, String serviceName, String uri, ParameterizedTypeReference<R> responseType);

    <T, R> R put(WebClient webClient, String serviceName, String uri, T request, Class<R> responseClass);

    <T, R> R put(WebClient webClient, String serviceName, String uri, T request, ParameterizedTypeReference<R> responseType);

    <R> R delete(WebClient webClient, String serviceName, String uri, Class<R> responseClass);

    <R> R delete(WebClient webClient, String serviceName, String uri, ParameterizedTypeReference<R> responseType);
}
