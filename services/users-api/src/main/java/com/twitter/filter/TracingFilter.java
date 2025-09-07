//package com.twitter.filter;
//
//import jakarta.servlet.Filter;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.MDC;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.UUID;
//
///**
// * Фильтр для автоматического добавления trace ID и других метаданных в MDC
// * Обеспечивает сквозное отслеживание запросов через микросервисы
// */
//@Slf4j
//@Component
//@Order(1)
//public class TracingFilter implements Filter {
//
//    private static final String TRACE_ID_HEADER = "X-Trace-Id";
//    private static final String SPAN_ID_HEADER = "X-Span-Id";
//    private static final String REQUEST_ID_HEADER = "X-Request-Id";
//    private static final String USER_ID_HEADER = "X-User-Id";
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        try {
//            // Получаем или создаем trace ID
//            String traceId = getOrCreateTraceId(httpRequest);
//            String spanId = generateSpanId();
//            String requestId = getOrCreateRequestId(httpRequest);
//            String userId = getUserId(httpRequest);
//
//            // Добавляем в MDC для логирования
//            MDC.put("traceId", traceId);
//            MDC.put("spanId", spanId);
//            MDC.put("requestId", requestId);
//            if (userId != null) {
//                MDC.put("userId", userId);
//            }
//
//            // Добавляем заголовки в ответ для передачи в другие сервисы
//            httpResponse.setHeader(TRACE_ID_HEADER, traceId);
//            httpResponse.setHeader(SPAN_ID_HEADER, spanId);
//            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
//
//            // Логируем начало запроса
//            log.info("Incoming request: {} {} from {} with traceId={}, spanId={}, requestId={}",
//                    httpRequest.getMethod(),
//                    httpRequest.getRequestURI(),
//                    httpRequest.getRemoteAddr(),
//                    traceId,
//                    spanId,
//                    requestId);
//
//            // Продолжаем выполнение цепочки фильтров
//            chain.doFilter(request, response);
//
//            // Логируем завершение запроса
//            log.info("Outgoing response: {} {} -> {} with traceId={}, spanId={}, requestId={}",
//                    httpRequest.getMethod(),
//                    httpRequest.getRequestURI(),
//                    httpResponse.getStatus(),
//                    traceId,
//                    spanId,
//                    requestId);
//
//        } finally {
//            // Очищаем MDC после обработки запроса
//            MDC.clear();
//        }
//    }
//
//    /**
//     * Получает trace ID из заголовка или создает новый
//     */
//    private String getOrCreateTraceId(HttpServletRequest request) {
//        String traceId = request.getHeader(TRACE_ID_HEADER);
//        if (traceId == null || traceId.isEmpty()) {
//            traceId = generateTraceId();
//        }
//        return traceId;
//    }
//
//    /**
//     * Получает request ID из заголовка или создает новый
//     */
//    private String getOrCreateRequestId(HttpServletRequest request) {
//        String requestId = request.getHeader(REQUEST_ID_HEADER);
//        if (requestId == null || requestId.isEmpty()) {
//            requestId = generateRequestId();
//        }
//        return requestId;
//    }
//
//    /**
//     * Получает user ID из заголовка (если есть)
//     */
//    private String getUserId(HttpServletRequest request) {
//        return request.getHeader(USER_ID_HEADER);
//    }
//
//    /**
//     * Генерирует новый trace ID
//     */
//    private String generateTraceId() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }
//
//    /**
//     * Генерирует новый span ID
//     */
//    private String generateSpanId() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }
//
//    /**
//     * Генерирует новый request ID
//     */
//    private String generateRequestId() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }
//}
//
