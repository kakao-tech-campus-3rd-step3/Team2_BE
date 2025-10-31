package kr.it.pullit.modules.wronganswer.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.repository.WrongAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("WrongAnswerService 단위 테스트")
class WrongAnswerServiceTest {

  private static final Long MEMBER_ID = 11L;

  @Mock private WrongAnswerRepository wrongAnswerRepository;

  @Mock private MemberPublicApi memberPublicApi;

  @Mock private QuestionPublicApi questionPublicApi;

  private WrongAnswerService wrongAnswerService;

  @BeforeEach
  void setUp() {
    wrongAnswerService =
        new WrongAnswerService(wrongAnswerRepository, memberPublicApi, questionPublicApi);
  }

  @Test
  @DisplayName("동시성 충돌이 발생해도 예외를 던지지 않고 저장을 마친다")
  void shouldSwallowDataIntegrityViolationWhenSavingWrongAnswers() {
    Question question = mock(Question.class);
    when(question.getId()).thenReturn(99L);

    when(memberPublicApi.findById(MEMBER_ID)).thenReturn(Optional.of(mock(Member.class)));
    when(questionPublicApi.findEntitiesByIds(anyList())).thenReturn(List.of(question));
    when(wrongAnswerRepository.findByMemberIdAndQuestionIdIn(MEMBER_ID, List.of(99L)))
        .thenReturn(List.of());
    when(wrongAnswerRepository.saveAll(any()))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    assertThatNoException()
        .isThrownBy(() -> wrongAnswerService.markAsWrongAnswers(MEMBER_ID, List.of(99L)));
  }

  @Test
  @DisplayName("null 또는 빈 입력은 즉시 반환한다")
  void shouldReturnEarlyForEmptyInputs() {
    wrongAnswerService.markAsWrongAnswers(MEMBER_ID, null);
    wrongAnswerService.markAsWrongAnswers(MEMBER_ID, Collections.emptyList());
    wrongAnswerService.markAsCorrectAnswers(MEMBER_ID, null);
    wrongAnswerService.markAsCorrectAnswers(MEMBER_ID, Collections.emptyList());

    verifyNoInteractions(memberPublicApi, questionPublicApi, wrongAnswerRepository);
  }

  @Test
  @DisplayName("이미 존재하는 오답만 전달되면 저장을 시도하지 않는다")
  void shouldSkipSavingWhenAllWrongAnswersAlreadyExist() {
    Question question = mock(Question.class);
    when(question.getId()).thenReturn(88L);

    when(memberPublicApi.findById(MEMBER_ID)).thenReturn(Optional.of(mock(Member.class)));
    when(questionPublicApi.findEntitiesByIds(List.of(88L))).thenReturn(List.of(question));

    when(wrongAnswerRepository.findByMemberIdAndQuestionIdIn(MEMBER_ID, List.of(88L)))
        .thenReturn(List.of(WrongAnswer.create(MEMBER_ID, question)));

    wrongAnswerService.markAsWrongAnswers(MEMBER_ID, List.of(88L));

    verify(wrongAnswerRepository, never()).saveAll(any());
  }
}
