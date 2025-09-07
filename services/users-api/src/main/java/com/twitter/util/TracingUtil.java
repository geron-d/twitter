//package com.twitter.util;
//
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.MDC;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Утилитный класс для работы с трейсингом
// * Предоставляет удобные методы для работы с MDC и логированием
// */
//@Slf4j
//@Component
//public class TracingUtil {
//
//    /**
//     * Получает текущий trace ID из MDC
//     */
//    public String getCurrentTraceId() {
//        return MDC.get("traceId");
//    }
//
//    /**
//     * Получает текущий span ID из MDC
//     */
//    public String getCurrentSpanId() {
//        return MDC.get("spanId");
//    }
//
//    /**
//     * Получает текущий request ID из MDC
//     */
//    public String getCurrentRequestId() {
//        return MDC.get("requestId");
//    }
//
//    /**
//     * Получает текущий user ID из MDC
//     */
//    public String getCurrentUserId() {
//        return MDC.get("userId");
//    }
//
//    /**
//     * Добавляет пользовательский контекст в MDC
//     */
//    public void addUserContext(String userId, String username) {
//        MDC.put("userId", userId);
//        MDC.put("username", username);
//    }
//
//    /**
//     * Очищает пользовательский контекст из MDC
//     */
//    public void clearUserContext() {
//        MDC.remove("userId");
//        MDC.remove("username");
//    }
//
//
//    /**
//     * Добавляет тег в MDC для логирования
//     */
//    public void addTag(String key, String value) {
//        MDC.put(key, value);
//    }
//
//    /**
//     * Добавляет событие в логи
//     */
//    public void addEvent(String eventName) {
//        log.info("Event: {} [traceId={}, spanId={}]", eventName, getCurrentTraceId(), getCurrentSpanId());
//    }
//
//    /**
//     * Выполняет операцию с логированием начала и конца
//     */
//    public <T> T executeInSpan(String spanName, SpanOperation<T> operation) {
//        log.debug("Starting span: {} [traceId={}, spanId={}]", spanName, getCurrentTraceId(), getCurrentSpanId());
//        try {
//            T result = operation.execute();
//            log.debug("Completed span: {} [traceId={}, spanId={}]", spanName, getCurrentTraceId(), getCurrentSpanId());
//            return result;
//        } catch (RuntimeException e) {
//            log.error("Failed span: {} [traceId={}, spanId={}]", spanName, getCurrentTraceId(), getCurrentSpanId(), e);
//            throw e;
//        } catch (Exception e) {
//            log.error("Failed span: {} [traceId={}, spanId={}]", spanName, getCurrentTraceId(), getCurrentSpanId(), e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * Выполняет операцию с тегами и логированием
//     */
//    public <T> T executeInSpan(String spanName, Map<String, String> tags, SpanOperation<T> operation) {
//        // Добавляем теги в MDC
//        tags.forEach(this::addTag);
//
//        log.debug("Starting span: {} with tags: {} [traceId={}, spanId={}]",
//                spanName, tags, getCurrentTraceId(), getCurrentSpanId());
//
//        try {
//            T result = operation.execute();
//            log.debug("Completed span: {} [traceId={}, spanId={}]", spanName, getCurrentTraceId(), getCurrentSpanId());
//            return result;
//        } catch (RuntimeException e) {
//            log.error("Failed span: {} [traceId={}, spanId={}]", spanName, getCurrentTraceId(), getCurrentSpanId(), e);
//            throw e;
//        } catch (Exception e) {
//            log.error("Failed span: {} [traceId={}, spanId={}]", spanName, getCurrentTraceId(), getCurrentSpanId(), e);
//            throw new RuntimeException(e);
//        } finally {
//            // Очищаем теги
//            tags.keySet().forEach(MDC::remove);
//        }
//    }
//
//    /**
//     * Функциональный интерфейс для операций в span'е
//     */
//    @FunctionalInterface
//    public interface SpanOperation<T> {
//        T execute() throws Exception;
//    }
//
//    /**
//     * Генерирует уникальный ID для span'а
//     */
//    public String generateSpanId() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }
//
//    /**
//     * Генерирует уникальный ID для trace'а
//     */
//    public String generateTraceId() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }
//}
