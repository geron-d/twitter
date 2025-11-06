package com.twitter.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Feign Clients.
 * <p>
 * This configuration class enables scanning and registration of Feign clients
 * in the specified base package for integration with external services.
 *
 * @author geron
 * @version 1.0
 */
@Configuration
@EnableFeignClients(basePackages = "com.twitter.client")
public class FeignConfig {
}
