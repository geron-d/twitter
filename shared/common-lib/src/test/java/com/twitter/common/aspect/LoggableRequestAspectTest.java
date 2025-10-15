package com.twitter.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggableRequestAspectTest {

    private LoggableRequestAspect aspect;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Method method;

    @Mock
    private Logger logger;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @BeforeEach
    void setUp() {
        aspect = new LoggableRequestAspect();
    }

    private void setupBasicMocks() throws Exception {
        when(requestAttributes.getRequest()).thenReturn(request);
        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(method.getAnnotation(LoggableRequest.class)).thenReturn(createLoggableRequestAnnotation(true, new String[]{}));
    }

    private LoggableRequest createLoggableRequestAnnotation(boolean printRequestBody, String[] hideFields) {
        return new LoggableRequest() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return LoggableRequest.class;
            }

            @Override
            public boolean printRequestBody() {
                return printRequestBody;
            }

            @Override
            public String[] hideFields() {
                return hideFields;
            }
        };
    }

    @Nested
    class SuccessfulScenarios {

        @Test
        void shouldLogSuccessfullyWithResponseEntity() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldLogSuccessfullyWithNullResponse() throws Throwable {
            setupBasicMocks();
            when(proceedingJoinPoint.proceed()).thenReturn(null);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertNull(result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldLogSuccessfullyWithPlainObject() throws Throwable {
            setupBasicMocks();
            String expectedResponse = "Plain Object Response";
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("PUT");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldLogSuccessfullyWithCollection() throws Throwable {
            setupBasicMocks();
            List<String> expectedResponse = Arrays.asList("item1", "item2", "item3");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldLogSuccessfullyWithMap() throws Throwable {
            setupBasicMocks();
            Map<String, Object> expectedResponse = new HashMap<>();
            expectedResponse.put("key1", "value1");
            expectedResponse.put("key2", "value2");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldLogSuccessfullyWithDebugEnabled() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Debug Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldLogSuccessfullyWithDebugDisabled() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("No Debug Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

    }

    @Nested
    class BoundaryScenarios {

        @Test
        void shouldHandleEmptyArgsArray() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Empty Args Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldHandleSingleArg() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Single Arg Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"single arg"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldHandleMultipleArgs() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Multiple Args Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("PUT");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"first arg", "second arg"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldHandleEmptyHeaders() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Empty Headers Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldHandleSingleHeader() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Single Header Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/test");

            Enumeration<String> headerNames = Collections.enumeration(Arrays.asList("Content-Type"));
            when(request.getHeaderNames()).thenReturn(headerNames);
            when(request.getHeader("Content-Type")).thenReturn("application/json");

            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldHandleMultipleHeaders() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Multiple Headers Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("PUT");
            when(request.getRequestURI()).thenReturn("/test");

            Enumeration<String> headerNames = Collections.enumeration(Arrays.asList("Content-Type", "Authorization", "Accept"));
            when(request.getHeaderNames()).thenReturn(headerNames);
            when(request.getHeader("Content-Type")).thenReturn("application/json");
            when(request.getHeader("Authorization")).thenReturn("Bearer token123");
            when(request.getHeader("Accept")).thenReturn("application/json");

            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

    }

    @Nested
    class ExceptionScenarios {

        @Test
        void shouldHandleExceptionInLogResponseDetails() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Exception Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldHandleExceptionInHideSensitiveFields() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Hide Fields Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("PUT");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            when(method.getAnnotation(LoggableRequest.class)).thenReturn(createLoggableRequestAnnotation(true, new String[]{"password", "secret"}));

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

    }

    @Nested
    class AdditionalScenarios {

        @Test
        void shouldReturnCorrectValue() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Correct Value Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                assertSame(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
            }
        }

        @Test
        void shouldExecuteOperationsInCorrectOrder() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Order Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/test");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(requestAttributes);

                Object result = aspect.log(proceedingJoinPoint);

                assertEquals(expectedResponse, result);
                verify(proceedingJoinPoint).proceed();
                verify(request, atLeastOnce()).getMethod();
                verify(request, atLeastOnce()).getRequestURI();
            }
        }

        @Test
        void shouldHandleDifferentHttpMethods() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("HTTP Methods Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
            String[] uris = {"/api/users", "/api/posts", "/api/comments", "/api/likes", "/api/follows"};

            for (int i = 0; i < httpMethods.length; i++) {
                when(request.getMethod()).thenReturn(httpMethods[i]);
                when(request.getRequestURI()).thenReturn(uris[i]);

                try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                    mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                        .thenReturn(requestAttributes);

                    Object result = aspect.log(proceedingJoinPoint);

                    assertEquals(expectedResponse, result);
                    verify(request, atLeastOnce()).getMethod();
                    verify(request, atLeastOnce()).getRequestURI();
                }
            }
        }

        @Test
        void shouldHandleDifferentURIs() throws Throwable {
            setupBasicMocks();
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("URI Test Response");
            when(proceedingJoinPoint.proceed()).thenReturn(expectedResponse);
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"test body"});

            String[] testUris = {
                "/",
                "/api",
                "/api/users",
                "/api/users/123",
                "/api/users/123/posts/456/comments/789",
                "/api/users?page=1&size=10",
                "/api/users/123/posts?sort=createdAt&order=desc"
            };

            for (String uri : testUris) {
                when(request.getRequestURI()).thenReturn(uri);

                try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                    mockedRequestContextHolder.when(RequestContextHolder::currentRequestAttributes)
                        .thenReturn(requestAttributes);

                    Object result = aspect.log(proceedingJoinPoint);

                    assertEquals(expectedResponse, result);
                    verify(request, atLeastOnce()).getRequestURI();
                }
            }
        }

    }
}