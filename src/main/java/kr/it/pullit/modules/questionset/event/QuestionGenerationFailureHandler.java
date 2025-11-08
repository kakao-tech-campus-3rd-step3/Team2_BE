package kr.it.pullit.modules.questionset.event;

import java.util.Optional;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.client.exception.LlmErrorType;
import kr.it.pullit.modules.questionset.client.exception.LlmException;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionGenerationFailureHandler {

  private final QuestionSetPublicApi questionSetPublicApi;

  public void handle(QuestionSetCreatedEvent event, Exception e) {
    log.error("문제 생성 중 오류 발생. QuestionSet ID: {}", event.questionSetId(), e);

    if (isPermanentLlmError(e)) {
      handlePermanentFailure(event.questionSetId(), "페이지 한도 초과와 같은 영구적인 LLM 오류");
      return;
    }
    handleTemporaryFailure(event);
  }

  private boolean isPermanentLlmError(Exception e) {
    return e instanceof LlmException llmEx && llmEx.getErrorType() == LlmErrorType.PERMANENT;
  }

  private void handlePermanentFailure(Long questionSetId, String reason) {
    questionSetPublicApi.markAsUnprocessable(questionSetId);
    log.warn("문제집 ID {}는 처리 불가능한(UNPROCESSABLE) 상태로 변경되었습니다. (사유: {})", questionSetId, reason);
  }

  private void handleTemporaryFailure(QuestionSetCreatedEvent event) {
    Optional<QuestionSet> questionSetOpt =
        questionSetPublicApi.findEntityByIdAndMemberId(event.questionSetId(), event.ownerId());

    if (questionSetOpt.isEmpty()) {
      log.error("실패 처리 중 문제집을 찾을 수 없습니다. QuestionSet ID: {}", event.questionSetId());
      return;
    }

    QuestionSet questionSet = questionSetOpt.get();
    if (questionSet.hasExhaustedRetries()) {
      handlePermanentFailure(
          event.questionSetId(), "최대 재시도 횟수(" + QuestionSet.MAX_RETRY_COUNT + ") 도달");
    } else {
      questionSetPublicApi.markAsFailed(event.questionSetId());
    }
  }
}
