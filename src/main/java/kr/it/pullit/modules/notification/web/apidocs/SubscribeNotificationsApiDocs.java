package kr.it.pullit.modules.notification.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "알림 구독 (SSE)",
    description =
        """
        서버로부터 실시간 알림을 받기 위해 SSE(Server-Sent Events) 연결을 시작합니다.
        재연결 시 마지막으로 수신한 이벤트 ID를 `Last-Event-ID` 헤더에 담아 보내면 유실된 이벤트를 받을 수 있습니다.
        """)
@ApiResponses(
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 구독 채널에 연결되었습니다. Content-Type은 text/event-stream 입니다."))
public @interface SubscribeNotificationsApiDocs {}
