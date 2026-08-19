package io.github.rudsoncarvalho.reof.config;

import java.time.Duration;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientsConfig {

    // REOF CE/SE timeout values are deliberately resolved in code, not left to framework defaults.
    static final Duration CONNECT_TIMEOUT = Duration.ofMillis(200);
    static final Duration RESPONSE_TIMEOUT = Duration.ofMillis(700);
    static final Duration CONNECTION_REQUEST_TIMEOUT = Duration.ofMillis(150);
    static final int MAX_CONNECTIONS_TOTAL = 32;
    static final int MAX_CONNECTIONS_PER_ROUTE = 16;

    @Bean(destroyMethod = "close")
    CloseableHttpClient catalogHttpClient() {
        return resilientHttpClient();
    }

    @Bean(destroyMethod = "close")
    CloseableHttpClient webhookHttpClient() {
        return resilientHttpClient();
    }

    @Bean("catalogRestClient")
    RestClient catalogRestClient(
            @org.springframework.beans.factory.annotation.Qualifier("catalogHttpClient") CloseableHttpClient catalogHttpClient,
            @Value("${demo.clients.catalog.base-url}") String baseUrl) {
        return restClient(catalogHttpClient, baseUrl);
    }

    @Bean("webhookRestClient")
    RestClient webhookRestClient(
            @org.springframework.beans.factory.annotation.Qualifier("webhookHttpClient") CloseableHttpClient webhookHttpClient,
            @Value("${demo.clients.webhook.base-url}") String baseUrl) {
        return restClient(webhookHttpClient, baseUrl);
    }

    private CloseableHttpClient resilientHttpClient() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(CONNECT_TIMEOUT))
                .setSocketTimeout(Timeout.of(RESPONSE_TIMEOUT))
                .build();

        PoolingHttpClientConnectionManager pool = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(MAX_CONNECTIONS_TOTAL)
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(CONNECTION_REQUEST_TIMEOUT))
                .setResponseTimeout(Timeout.of(RESPONSE_TIMEOUT))
                .build();

        return HttpClients.custom()
                .setConnectionManager(pool)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .build();
    }

    private RestClient restClient(CloseableHttpClient httpClient, String baseUrl) {
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .build();
    }
}
