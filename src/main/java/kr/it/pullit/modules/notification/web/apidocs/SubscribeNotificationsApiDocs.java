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
            클라이언트는 이 엔드포인트에 연결하여 문제 생성 완료, 새 공지 등 다양한 이벤트에 대한 알림을 실시간으로 수신할 수 있습니다.

            **재연결 메커니즘:**
            네트워크 문제 등으로 연결이 끊겼을 경우, 클라이언트는 마지막으로 수신한 이벤트의 ID를
            `Last-Event-ID` 헤더 또는 `lastEventId` 쿼리 파라미터에 담아 재연결을 시도해야 합니다.
            서버는 이 ID를 바탕으로 유실된 이벤트가 있다면 모두 전송해줍니다.

            [Request]
            - `Last-Event-ID` (Header, 옵션): 마지막으로 수신한 이벤트 ID
            - `lastEventId` (Query Param, 옵션): 마지막으로 수신한 이벤트 ID
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, `Content-Type`이 `text/event-stream`으로 설정된 지속적인 연결이 수립됩니다.
            - 서버는 이벤트 발생 시마다 해당 연결을 통해 데이터를 전송합니다.""")
@ApiResponses(
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 구독 채널에 연결되었습니다. Content-Type은 text/event-stream 입니다."))
public @interface SubscribeNotificationsApiDocs {}
