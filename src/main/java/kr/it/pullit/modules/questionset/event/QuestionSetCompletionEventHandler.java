package kr.it.pullit.modules.questionset.event;

import kr.it.pullit.configuration.RabbitMqConfig;
import kr.it.pullit.modules.notification.api.NotificationEventPublicApi;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!worker")
@RequiredArgsConstructor
public class QuestionSetCompletionEventHandler {

  private final NotificationEventPublicApi notificationEventPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final LearnStatsRecalibrationPublicApi learnStatsRecalibrationPublicApi;

  @RabbitListener(queues = RabbitMqConfig.COMPLETION_QUEUE_NAME)
  public void handleQuestionSetCompletion(QuestionSetCompletionEvent event) {
    log.info(
        "문제집 생성 완료 이벤트를 수신했습니다. QuestionSet ID: {}, Owner ID: {}",
        event.questionSetId(),
        event.ownerId());

    QuestionSetCreationCompleteResponse responseDto = createSuccessResponse(event);
    notificationEventPublicApi.publishQuestionSetCreationComplete(event.ownerId(), responseDto);
    learnStatsRecalibrationPublicApi.recalibrateTotalQuestionCountForMember(event.ownerId());

    log.info("사용자에게 SSE 알림을 전송하고 통계 정보를 갱신했습니다. QuestionSet ID: {}", event.questionSetId());
  }

  private QuestionSetCreationCompleteResponse createSuccessResponse(
      QuestionSetCompletionEvent event) {
    QuestionSetResponse questionSetResponse =
        questionSetPublicApi.getQuestionSetForSolving(
            event.questionSetId(), event.ownerId(), false);
    return new QuestionSetCreationCompleteResponse(
        true, questionSetResponse.getId(), "문제집 생성 완료 (" + questionSetResponse.getTitle() + ")");
  }
}
