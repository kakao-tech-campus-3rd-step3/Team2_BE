package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.exception.QuestionNotFoundException;
import kr.it.pullit.modules.questionset.repository.MarkingResultRepository;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import kr.it.pullit.shared.event.EventPublisher;
import kr.it.pullit.support.annotation.SpringUnitTest;
import kr.it.pullit.support.fixture.QuestionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringUnitTest
@DisplayName("MarkingService 단위 테스트")
@ContextConfiguration(classes = {MarkingService.class})
class MarkingServiceTest {

  @Autowired private MarkingService markingService;

  @MockitoBean private QuestionPublicApi questionPublicApi;
  @MockitoBean private EventPublisher eventPublisher;
  @MockitoBean private MarkingResultRepository markingResultRepository;
  @MockitoBean private QuestionSetRepository questionSetRepository;

  @Nested
  @DisplayName("markQuestions 메서드는")
  class DescribeMarkQuestions {

    @Test
    @DisplayName("채점 결과를 저장하고 응답을 반환한다")
    void shouldSaveMarkingResultsAndReturnResponse() {
      // given
      Long memberId = 1L;
      Question question = spy(QuestionFixtures.aCorrectTrueFalseQuestion());
      QuestionSet questionSet = question.getQuestionSet();
      Long questionId = 100L; // ID를 가정

      given(question.getId()).willReturn(questionId);
      given(questionPublicApi.findEntityById(questionId)).willReturn(Optional.of(question));
      given(markingResultRepository.countByQuestionSetIdAndMemberId(questionSet.getId(), memberId))
          .willReturn(1L);

      MarkingServiceRequest request =
          MarkingServiceRequest.of(memberId, List.of(MarkingRequest.of(questionId, true)), false);

      // when
      MarkQuestionsResponse response = markingService.markQuestions(request);

      // then
      assertThat(response.results()).hasSize(1);
      assertThat(response.results().getFirst().isCorrect()).isTrue();
      assertThat(response.results().getFirst().questionId()).isEqualTo(questionId);

      // 저장 로직 검증
      verify(markingResultRepository).save(any());
      verify(questionSetRepository).save(questionSet);

      // 이벤트 발행 검증
      verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("맞은 문제와 틀린 문제를 구분하여 저장한다")
    void shouldSaveCorrectAndIncorrectResults() {
      // given
      Long memberId = 1L;
      Long correctQuestionId = 100L;
      Long incorrectQuestionId = 200L;

      Question correctQuestion = spy(QuestionFixtures.aCorrectTrueFalseQuestion());
      Question incorrectQuestion = spy(QuestionFixtures.anIncorrectTrueFalseQuestion());

      given(correctQuestion.getId()).willReturn(correctQuestionId);
      given(incorrectQuestion.getId()).willReturn(incorrectQuestionId);

      given(questionPublicApi.findEntityById(correctQuestionId))
          .willReturn(Optional.of(correctQuestion));
      given(questionPublicApi.findEntityById(incorrectQuestionId))
          .willReturn(Optional.of(incorrectQuestion));
      given(markingResultRepository.countByQuestionSetIdAndMemberId(any(), any()))
          .willReturn(1L, 2L);

      MarkingServiceRequest request =
          MarkingServiceRequest.of(
              memberId,
              List.of(
                  MarkingRequest.of(correctQuestionId, true),
                  MarkingRequest.of(incorrectQuestionId, true)),
              false);

      // when
      MarkQuestionsResponse response = markingService.markQuestions(request);

      // then
      assertThat(response.results()).hasSize(2);
      assertThat(response.results().get(0).isCorrect()).isTrue();
      assertThat(response.results().get(1).isCorrect()).isFalse();
      assertThat(response.correctCount()).isEqualTo(1);

      // 저장이 2번 호출되었는지 검증
      verify(markingResultRepository, times(2)).save(any());
      verify(questionSetRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("객관식 문제를 정답으로 채점한다")
    void shouldMarkMultipleChoiceQuestionAsCorrect() {
      // given
      Long memberId = 1L;
      Long questionId = 300L;
      Question question = spy(QuestionFixtures.aCorrectMultipleChoiceQuestion());

      given(question.getId()).willReturn(questionId);
      given(questionPublicApi.findEntityById(questionId)).willReturn(Optional.of(question));
      given(
              markingResultRepository.countByQuestionSetIdAndMemberId(
                  question.getQuestionSet().getId(), memberId))
          .willReturn(1L);

      MarkingServiceRequest request =
          MarkingServiceRequest.of(memberId, List.of(MarkingRequest.of(questionId, "보기1")), false);

      // when
      MarkQuestionsResponse response = markingService.markQuestions(request);

      // then
      assertThat(response.results().get(0).isCorrect()).isTrue();
      verify(markingResultRepository).save(any());
      verify(questionSetRepository).save(any());
    }

    @Test
    @DisplayName("주관식 문제를 정답으로 채점한다 (공백/대소문자 무시)")
    void shouldMarkShortAnswerQuestionAsCorrect() {
      // given
      Long memberId = 1L;
      Long questionId = 400L;
      Question question = spy(QuestionFixtures.aCorrectShortAnswerQuestion());

      given(question.getId()).willReturn(questionId);
      given(questionPublicApi.findEntityById(questionId)).willReturn(Optional.of(question));
      given(
              markingResultRepository.countByQuestionSetIdAndMemberId(
                  question.getQuestionSet().getId(), memberId))
          .willReturn(1L);

      MarkingServiceRequest request =
          MarkingServiceRequest.of(memberId, List.of(MarkingRequest.of(questionId, " 정답 ")), false);

      // when
      MarkQuestionsResponse response = markingService.markQuestions(request);

      // then
      assertThat(response.results().get(0).isCorrect()).isTrue();
      verify(markingResultRepository).save(any());
      verify(questionSetRepository).save(any());
    }

    @Test
    @DisplayName("요청이 null이면 예외를 던진다")
    void throwsWhenRequestNull() {
      assertThatThrownBy(() -> markingService.markQuestions(null))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("채점 대상이 비어 있으면 예외를 던진다")
    void throwsWhenRequestsEmpty() {
      MarkingServiceRequest request = MarkingServiceRequest.of(1L, List.of(), false);

      assertThatThrownBy(() -> markingService.markQuestions(request))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("문제를 찾지 못하면 예외를 던진다")
    void throwsWhenQuestionMissing() {
      Long missingQuestionId = 999L;

      given(questionPublicApi.findEntityById(missingQuestionId)).willReturn(Optional.empty());

      MarkingServiceRequest request =
          MarkingServiceRequest.of(1L, List.of(MarkingRequest.of(missingQuestionId, true)), false);

      assertThatThrownBy(() -> markingService.markQuestions(request))
          .isInstanceOf(QuestionNotFoundException.class);
    }
  }
}
