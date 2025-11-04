package kr.it.pullit.modules.questionset.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.notification.api.NotificationEventPublicApi;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.service.SourceValidator;
import kr.it.pullit.modules.questionset.service.creationstrategy.QuestionCreationStrategy;
import kr.it.pullit.modules.questionset.service.creationstrategy.QuestionCreationStrategyFactory;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.fixture.QuestionSetFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("QuestionGenerationEventHandler 단위 테스트")
class QuestionGenerationEventHandlerTest {

  @InjectMocks private QuestionGenerationEventHandler questionGenerationEventHandler;

  @Mock private QuestionPublicApi questionPublicApi;
  @Mock private QuestionSetPublicApi questionSetPublicApi;
  @Mock private NotificationEventPublicApi notificationEventPublicApi;
  @Mock private SourceValidator sourceValidator;
  @Mock private QuestionCreationStrategyFactory questionCreationStrategyFactory;
  @Mock private LearnStatsRecalibrationPublicApi learnStatsRecalibrationPublicApi;

  @Test
  @DisplayName("문제집 생성 성공 시, 관련 API들이 순차적으로 호출되고 완료 상태로 변경된다")
  void handleQuestionSetCreatedEvent_Success() throws Exception {
    // given
    var questionSetWithoutId = QuestionSetFixtures.basic();
    setIdUsingReflection(questionSetWithoutId, 1L);
    var event = QuestionSetCreatedEvent.from(questionSetWithoutId);

    var questionSetResponse = mock(QuestionSetResponse.class);
    var llmResponse = mock(LlmGeneratedQuestionSetResponse.class);
    var questionSet = mock(QuestionSet.class);
    var creationStrategy = mock(QuestionCreationStrategy.class);
    var question = mock(Question.class);

    given(
            questionSetPublicApi.getQuestionSetWhenHaveNoQuestionsYet(
                event.questionSetId(), event.ownerId()))
        .willReturn(questionSetResponse);
    given(questionSetResponse.getSourceIds()).willReturn(List.of(1L));
    given(questionSetResponse.getDifficulty()).willReturn(DifficultyType.EASY);
    given(questionSetResponse.getType()).willReturn(QuestionType.MULTIPLE_CHOICE);
    given(questionSetResponse.getQuestionLength()).willReturn(10);

    given(questionPublicApi.generateQuestions(any())).willReturn(llmResponse);
    given(llmResponse.title()).willReturn("생성된 문제집");
    given(llmResponse.questions())
        .willReturn(Collections.singletonList(mock(LlmGeneratedQuestionResponse.class)));
    given(questionSetPublicApi.findEntityByIdAndMemberId(event.questionSetId(), event.ownerId()))
        .willReturn(Optional.of(questionSet));
    given(questionSet.getType()).willReturn(QuestionType.MULTIPLE_CHOICE);
    given(questionCreationStrategyFactory.getStrategy(any(QuestionType.class)))
        .willReturn(creationStrategy);
    given(creationStrategy.create(any(), any())).willReturn(question);
    given(
            questionSetPublicApi.getQuestionSetForSolving(
                event.questionSetId(), event.ownerId(), false))
        .willReturn(questionSetResponse);
    given(questionSetResponse.getId()).willReturn(event.questionSetId());
    given(questionSetResponse.getTitle()).willReturn("생성된 문제집");

    // when
    questionGenerationEventHandler.handleQuestionSetCreatedEvent(event);

    // then
    verify(sourceValidator).validateSourcesAreReady(any(), eq(event.questionSetId()));
    verify(questionPublicApi).generateQuestions(any());
    verify(questionPublicApi).saveQuestion(any(Question.class));
    verify(questionSetPublicApi)
        .updateAndMarkAsComplete(
            eq(event.questionSetId()), any(QuestionSetUpdateRequestDto.class), eq(event.ownerId()));
    verify(notificationEventPublicApi)
        .publishQuestionSetCreationComplete(
            eq(event.ownerId()), any(QuestionSetCreationCompleteResponse.class));
    verify(learnStatsRecalibrationPublicApi)
        .recalibrateTotalQuestionCountForMember(event.ownerId());
    verify(questionSetPublicApi, never()).markAsFailed(anyLong());
  }

  @Test
  @DisplayName("문제집 생성 중 예외 발생 시, 실패 상태로 변경되고 관련 API가 호출되지 않는다")
  void handleQuestionSetCreatedEvent_Failure() throws Exception {
    // given
    var questionSetWithoutId = QuestionSetFixtures.basic();
    setIdUsingReflection(questionSetWithoutId, 1L);
    var event = QuestionSetCreatedEvent.from(questionSetWithoutId);

    var questionSetResponse = mock(QuestionSetResponse.class);

    given(
            questionSetPublicApi.getQuestionSetWhenHaveNoQuestionsYet(
                event.questionSetId(), event.ownerId()))
        .willReturn(questionSetResponse);
    given(questionSetResponse.getSourceIds()).willReturn(List.of(1L));
    given(questionSetResponse.getDifficulty()).willReturn(DifficultyType.EASY);
    given(questionSetResponse.getType()).willReturn(QuestionType.MULTIPLE_CHOICE);
    given(questionSetResponse.getQuestionLength()).willReturn(10);

    willThrow(new RuntimeException("LLM API Error"))
        .given(questionPublicApi)
        .generateQuestions(any());

    // when
    questionGenerationEventHandler.handleQuestionSetCreatedEvent(event);

    // then
    verify(questionSetPublicApi).markAsFailed(event.questionSetId());
    verify(questionSetPublicApi, never()).updateAndMarkAsComplete(anyLong(), any(), anyLong());
    verify(questionPublicApi, never()).saveQuestion(any());
    verify(notificationEventPublicApi, never())
        .publishQuestionSetCreationComplete(anyLong(), any());
    verify(learnStatsRecalibrationPublicApi, never())
        .recalibrateTotalQuestionCountForMember(anyLong());
  }

  private void setIdUsingReflection(QuestionSet questionSet, Long id) throws Exception {
    Field idField = QuestionSet.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(questionSet, id);
  }
}
