package com.twitter.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Feign Clients.
 *
 * @author geron
 * @version 1.0
 */
@Configuration
@EnableFeignClients(basePackages = "com.twitter.client")
public class FeignConfig {
}


