Есть FollowController
Есть FollowerGateway
При вызове в TweetController метода getTimeline происходит ошибка
2025-12-30 17:17:03.702 tweet-api [http-nio-8082-exec-2] INFO  [15a504c0388cc79250896869eb5d7dd8,b5105799f9c27486] c.t.c.aspect.LoggableRequestAspect - ### REQUEST GET /api/v1/tweets/timeline/cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2 ,Headers: user-agent: PostmanRuntime/7.51.0; accept: */*; cache-control: no-cache; postman-token: d953fb50-074d-4272-b35d-9a55c06ecd88; host: localhost:8082; accept-encoding: gzip, deflate, br; connection: keep-alive , Body: cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2
2025-12-30 17:17:06.316 tweet-api [http-nio-8082-exec-2] DEBUG [15a504c0388cc79250896869eb5d7dd8,b5105799f9c27486] com.twitter.gateway.UserGateway - User cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2 exists: true
2025-12-30 17:17:06.317 tweet-api [http-nio-8082-exec-2] DEBUG [15a504c0388cc79250896869eb5d7dd8,b5105799f9c27486] com.twitter.gateway.FollowerGateway - Retrieving following list for user: userId=cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2
2025-12-30 17:17:06.652 tweet-api [http-nio-8082-exec-2] WARN  [15a504c0388cc79250896869eb5d7dd8,b5105799f9c27486] com.twitter.gateway.FollowerGateway - Failed to retrieve following list for user: userId=cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2, error: Type definition error: [simple type, class org.springframework.data.web.PagedModel]
feign.codec.DecodeException: Type definition error: [simple type, class org.springframework.data.web.PagedModel]
at feign.InvocationContext.decode(InvocationContext.java:125)
at feign.InvocationContext.proceed(InvocationContext.java:94)
at feign.ResponseHandler.handleResponse(ResponseHandler.java:69)
at feign.SynchronousMethodHandler.executeAndDecode(SynchronousMethodHandler.java:109)
at feign.SynchronousMethodHandler.invoke(SynchronousMethodHandler.java:53)
at feign.ReflectiveFeign$FeignInvocationHandler.invoke(ReflectiveFeign.java:104)
at jdk.proxy2/jdk.proxy2.$Proxy183.getFollowing(Unknown Source)
at com.twitter.gateway.FollowerGateway.getFollowingUserIds(FollowerGateway.java:60)
at com.twitter.service.TweetServiceImpl.getTimeline(TweetServiceImpl.java:115)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
at java.base/java.lang.reflect.Method.invoke(Method.java:565)
at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360)
at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:380)
at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:728)
at com.twitter.service.TweetServiceImpl$$SpringCGLIB$$0.getTimeline(<generated>)
at com.twitter.controller.TweetController.getTimeline(TweetController.java:110)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
at java.base/java.lang.reflect.Method.invoke(Method.java:565)
at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360)
at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:89)
at com.twitter.common.aspect.LoggableRequestAspect.log(LoggableRequestAspect.java:102)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
at java.base/java.lang.reflect.Method.invoke(Method.java:565)
at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:649)
at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:631)
at org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:71)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:173)
at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:728)
at com.twitter.controller.TweetController$$SpringCGLIB$$0.getTimeline(<generated>)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
at java.base/java.lang.reflect.Method.invoke(Method.java:565)
at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258)
at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191)
at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)
at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:991)
at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:896)
at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089)
at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979)
at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)
at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903)
at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564)
at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)
at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195)
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)
at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
at org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:110)
at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)
at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)
at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)
at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483)
at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:116)
at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)
at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)
at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:398)
at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)
at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903)
at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1769)
at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)
at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1189)
at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:658)
at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)
at java.base/java.lang.Thread.run(Thread.java:1447)
Caused by: org.springframework.http.converter.HttpMessageConversionException: Type definition error: [simple type, class org.springframework.data.web.PagedModel]
at org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.readJavaType(AbstractJackson2HttpMessageConverter.java:405)
at org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.read(AbstractJackson2HttpMessageConverter.java:356)
at org.springframework.web.client.HttpMessageConverterExtractor.extractData(HttpMessageConverterExtractor.java:105)
at org.springframework.cloud.openfeign.support.SpringDecoder.decode(SpringDecoder.java:71)
at org.springframework.cloud.openfeign.support.ResponseEntityDecoder.decode(ResponseEntityDecoder.java:62)
at feign.optionals.OptionalDecoder.decode(OptionalDecoder.java:38)
at feign.InvocationContext.decode(InvocationContext.java:121)
... 88 common frames omitted
Caused by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `org.springframework.data.web.PagedModel` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2]
at com.fasterxml.jackson.databind.exc.InvalidDefinitionException.from(InvalidDefinitionException.java:67)
at com.fasterxml.jackson.databind.DeserializationContext.reportBadDefinition(DeserializationContext.java:1915)
at com.fasterxml.jackson.databind.DatabindContext.reportBadDefinition(DatabindContext.java:415)
at com.fasterxml.jackson.databind.DeserializationContext.handleMissingInstantiator(DeserializationContext.java:1402)
at com.fasterxml.jackson.databind.deser.BeanDeserializerBase.deserializeFromObjectUsingNonDefault(BeanDeserializerBase.java:1514)
at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserializeFromObject(BeanDeserializer.java:340)
at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserialize(BeanDeserializer.java:177)
at com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.readRootValue(DefaultDeserializationContext.java:342)
at com.fasterxml.jackson.databind.ObjectReader._bindAndClose(ObjectReader.java:2130)
at com.fasterxml.jackson.databind.ObjectReader.readValue(ObjectReader.java:1500)
at org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.readJavaType(AbstractJackson2HttpMessageConverter.java:397)
... 94 common frames omitted
2025-12-30 17:17:06.656 tweet-api [http-nio-8082-exec-2] DEBUG [15a504c0388cc79250896869eb5d7dd8,b5105799f9c27486] c.twitter.service.TweetServiceImpl - User cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2 has no following relationships, returning empty page
2025-12-30 17:17:06.657 tweet-api [http-nio-8082-exec-2] INFO  [15a504c0388cc79250896869eb5d7dd8,b5105799f9c27486] c.t.c.aspect.LoggableRequestAspect - ### RESPONSE GET, /api/v1/tweets/timeline/cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2 

При этом в follower-api все хорошо
3a9802f0,17854a405904c9fb] c.t.c.aspect.LoggableRequestAspect - ### REQUEST GET /api/v1/follows/cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2/following ,Headers: accept: */*; user-agent: Java/24.0.1; host: localhost:8084; connection: keep-alive , Body: cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2
2025-12-30 17:17:06.401 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] c.twitter.service.FollowServiceImpl - Retrieving following for user: userId=cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2, filter=FollowingFilter[login=null], page=0, size=100
2025-12-30 17:17:06.568 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user ee1ade5b-4598-4b06-a26e-f15093ea3cd6: julienne_funk_1767083749814_2f082d56
2025-12-30 17:17:06.575 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user b60c54fb-0967-4dc1-b78c-805cca624b74: nieves_gleason_1767083749784_20dd19ea
2025-12-30 17:17:06.581 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user c0f83c83-d4d0-4354-9d29-82d3a86a6e80: garland_johns_1767083749805_5cbe23fe
2025-12-30 17:17:06.586 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user 05992dff-be41-4c38-9f8e-3f02988b657a: perry_oreilly_1767083749722_bcba61a7
2025-12-30 17:17:06.591 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user 1ed88eab-0708-443f-a926-a02569575e9e: mia_weissnat_1767083749773_3a82c95b
2025-12-30 17:17:06.597 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user 3a10d22b-7f45-4d04-b884-ea155a5d5907: major_pfannerstill_1767083749734_f06f9ebb
2025-12-30 17:17:06.602 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user cfdcccd6-799d-42c3-9068-84972aa93845: isabella_kilback_1767083749856_74fe5736
2025-12-30 17:17:06.607 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user 0e16140e-c3e7-4447-b717-0525a5d6c29b: thomasena_rodriguez_1767083749743_c5e2dca2
2025-12-30 17:17:06.612 follower-api [http-nio-8084-exec-1] DEBUG [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] com.twitter.gateway.UserGateway - Retrieved login for user 3ce88f73-5e0c-4950-a419-18245b29f58a: christoper_kunde_1767083749867_c8b7f602
2025-12-30 17:17:06.613 follower-api [http-nio-8084-exec-1] INFO  [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] c.twitter.service.FollowServiceImpl - Retrieved 9 following for user: userId=cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2, totalElements=9
2025-12-30 17:17:06.615 follower-api [http-nio-8084-exec-1] INFO  [4d07afa2cdac7d4df1330d6f3a9802f0,17854a405904c9fb] c.t.c.aspect.LoggableRequestAspect - ### RESPONSE GET, /api/v1/follows/cf86f61b-c8fb-4d53-ad68-47f2ec0c1da2/following 