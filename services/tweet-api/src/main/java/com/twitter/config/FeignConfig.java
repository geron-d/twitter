package com.twitter.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для Feign Clients.
 * Включает сканирование и регистрацию Feign клиентов.
 */
@Configuration
@EnableFeignClients(basePackages = "com.twitter.client")
public class FeignConfig {
}
