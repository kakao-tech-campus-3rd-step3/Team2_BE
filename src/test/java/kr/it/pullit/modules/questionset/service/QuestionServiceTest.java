package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.exception.QuestionNotFoundException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotFoundException;
import kr.it.pullit.modules.questionset.repository.QuestionRepository;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionCreateRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoUnitTest
@DisplayName("QuestionService 단위 테스트")
class QuestionServiceTest {

  @Mock private QuestionRepository questionRepository;
  @Mock private QuestionSetRepository questionSetRepository;
  @Mock private SourcePublicApi sourcePublicApi;
  @Mock private LlmClient llmClient;
  @Mock private QuestionSetFileTempManager questionSetFileTempManager;

  private QuestionService questionService;

  @BeforeEach
  void setUp() {
    questionService =
        new QuestionService(
            questionRepository,
            questionSetRepository,
            sourcePublicApi,
            llmClient,
            questionSetFileTempManager);
  }

  @Nested
  @DisplayName("generateQuestions")
  class DescribeGenerateQuestions {

    @Test
    @DisplayName("LLM 클라이언트를 호출하여 문제를 생성하고 임시 파일을 정리한다")
    void generatesQuestionsThroughLlmAndCleansUpFiles() throws IOException {
      Long memberId = 10L;
      Long questionSetId = 20L;

      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(memberId).build();
      ReflectionTestUtils.setField(questionSet, "id", questionSetId);

      when(questionSetRepository.findByIdAndMemberId(questionSetId, memberId))
          .thenReturn(Optional.of(questionSet));

      Path path1 = Path.of("temp1.pdf");
      Path path2 = Path.of("temp2.pdf");

      when(sourcePublicApi.downloadFileToTemp(1L, memberId)).thenReturn(path1);
      when(sourcePublicApi.downloadFileToTemp(2L, memberId)).thenReturn(path2);

      LlmGeneratedQuestionSetResponse expected =
          new LlmGeneratedQuestionSetResponse("제목", List.of());
      when(llmClient.getLlmGeneratedQuestionContent(any(LlmGeneratedQuestionRequest.class)))
          .thenReturn(expected);

      QuestionGenerationSpecification specification =
          new QuestionGenerationSpecification(DifficultyType.EASY, QuestionType.MULTIPLE_CHOICE, 3);
      QuestionGenerationRequest request =
          new QuestionGenerationRequest(memberId, questionSetId, List.of(1L, 2L), specification);
      LlmGeneratedQuestionSetResponse response = questionService.generateQuestions(request);

      assertThat(response).isEqualTo(expected);

      ArgumentCaptor<LlmGeneratedQuestionRequest> captor =
          ArgumentCaptor.forClass(LlmGeneratedQuestionRequest.class);
      verify(llmClient).getLlmGeneratedQuestionContent(captor.capture());
      assertThat(captor.getValue().model()).isEqualTo("gemini-2.5-flash-lite");
      assertThat(captor.getValue().specification()).isEqualTo(specification);
      assertThat(captor.getValue().fileDataList()).containsExactly(path1, path2);

      verify(sourcePublicApi).downloadFileToTemp(1L, memberId);
      verify(sourcePublicApi).downloadFileToTemp(2L, memberId);
      verify(questionSetFileTempManager).cleanUp(List.of(path1, path2));
    }

    @Test
    @DisplayName("문제집이 존재하지 않으면 예외를 던진다")
    void throwsWhenQuestionSetMissing() {
      Long memberId = 10L;
      Long questionSetId = 20L;

      when(questionSetRepository.findByIdAndMemberId(questionSetId, memberId))
          .thenReturn(Optional.empty());

      // [수정] 'specification' 변수 선언을 사용 직전으로 이동
      QuestionGenerationSpecification specification =
          new QuestionGenerationSpecification(DifficultyType.EASY, QuestionType.MULTIPLE_CHOICE, 3);
      QuestionGenerationRequest request =
          new QuestionGenerationRequest(memberId, questionSetId, List.of(1L), specification);

      assertThatThrownBy(() -> questionService.generateQuestions(request))
          .isInstanceOf(QuestionSetNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("saveQuestion")
  class DescribeSaveQuestion {

    @Test
    @DisplayName("질문 저장 시 문제집 존재 여부를 검증한다")
    void validatesQuestionSetBeforeSaving() {
      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(5L).build();
      ReflectionTestUtils.setField(questionSet, "id", 100L);

      MultipleChoiceQuestion question =
          MultipleChoiceQuestion.builder()
              .questionSet(questionSet)
              .questionText("문제")
              .options(List.of("1", "2", "3", "4"))
              .answer("1")
              .explanation("해설")
              .build();

      when(questionSetRepository.findByIdAndMemberId(100L, 5L))
          .thenReturn(Optional.of(questionSet));
      when(questionRepository.save(question)).thenReturn(question);

      questionService.saveQuestion(question);

      verify(questionSetRepository).findByIdAndMemberId(100L, 5L);
      verify(questionRepository).save(question);
    }

    @Test
    @DisplayName("문제집을 찾지 못하면 예외를 던진다")
    void saveThrowsWhenQuestionSetMissing() {
      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(5L).build();
      ReflectionTestUtils.setField(questionSet, "id", 100L);

      MultipleChoiceQuestion question =
          MultipleChoiceQuestion.builder()
              .questionSet(questionSet)
              .questionText("문제")
              .options(List.of("1", "2", "3", "4"))
              .answer("1")
              .explanation("해설")
              .build();

      when(questionSetRepository.findByIdAndMemberId(100L, 5L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> questionService.saveQuestion(question))
          .isInstanceOf(QuestionSetNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("createQuestion")
  class DescribeCreateQuestion {

    @Test
    @DisplayName("객관식 문제를 생성한다")
    void createsMultipleChoiceQuestion() {
      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(1L).build();
      ReflectionTestUtils.setField(questionSet, "id", 200L);

      when(questionSetRepository.findById(200L)).thenReturn(Optional.of(questionSet));
      when(questionRepository.save(any(MultipleChoiceQuestion.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      QuestionCreateRequest request =
          QuestionCreateRequest.builder()
              .questionSetId(200L)
              .questionType(QuestionType.MULTIPLE_CHOICE)
              .questionText("질문")
              .options(List.of("1", "2", "3", "4"))
              .answer("1")
              .explanation("해설")
              .build();

      QuestionResponse response = questionService.createQuestion(request);

      assertThat(response.questionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
      assertThat(response.options()).containsExactly("1", "2", "3", "4");
    }

    @Test
    @DisplayName("OX 문제를 생성한다")
    void createsTrueFalseQuestion() {
      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(1L).build();
      ReflectionTestUtils.setField(questionSet, "id", 201L);

      when(questionSetRepository.findById(201L)).thenReturn(Optional.of(questionSet));
      when(questionRepository.save(any(TrueFalseQuestion.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      QuestionCreateRequest request =
          QuestionCreateRequest.builder()
              .questionSetId(201L)
              .questionType(QuestionType.TRUE_FALSE)
              .questionText("질문")
              .answer("true")
              .explanation("해설")
              .build();

      QuestionResponse response = questionService.createQuestion(request);

      assertThat(response.questionType()).isEqualTo(QuestionType.TRUE_FALSE);
      assertThat(response.answer()).isEqualTo(true);
    }

    @Test
    @DisplayName("단답형 문제를 생성한다")
    void createsShortAnswerQuestion() {
      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(1L).build();
      ReflectionTestUtils.setField(questionSet, "id", 202L);

      when(questionSetRepository.findById(202L)).thenReturn(Optional.of(questionSet));
      when(questionRepository.save(any(ShortAnswerQuestion.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      QuestionCreateRequest request =
          QuestionCreateRequest.builder()
              .questionSetId(202L)
              .questionType(QuestionType.SHORT_ANSWER)
              .questionText("질문")
              .answer("답")
              .explanation("해설")
              .build();

      QuestionResponse response = questionService.createQuestion(request);

      assertThat(response.questionType()).isEqualTo(QuestionType.SHORT_ANSWER);
      assertThat(response.answer()).isEqualTo("답");
    }

    @Test
    @DisplayName("문제집을 찾지 못하면 예외를 던진다")
    void createThrowsWhenQuestionSetMissing() {
      when(questionSetRepository.findById(999L)).thenReturn(Optional.empty());

      QuestionCreateRequest request =
          QuestionCreateRequest.builder()
              .questionSetId(999L)
              .questionType(QuestionType.MULTIPLE_CHOICE)
              .questionText("질문")
              .options(List.of("1", "2"))
              .answer("1")
              .explanation("해설")
              .build();

      assertThatThrownBy(() -> questionService.createQuestion(request))
          .isInstanceOf(QuestionSetNotFoundException.class);
    }

    @Test
    @DisplayName("지원하지 않는 유형이면 예외를 던진다")
    void createThrowsWhenUnsupportedType() {
      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(1L).build();
      ReflectionTestUtils.setField(questionSet, "id", 203L);

      when(questionSetRepository.findById(203L)).thenReturn(Optional.of(questionSet));

      QuestionCreateRequest request =
          QuestionCreateRequest.builder()
              .questionSetId(203L)
              .questionType(QuestionType.SUBJECTIVE)
              .questionText("질문")
              .answer("답")
              .explanation("해설")
              .build();

      assertThatThrownBy(() -> questionService.createQuestion(request))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  @DisplayName("updateQuestion")
  class DescribeUpdateQuestion {

    @Test
    @DisplayName("문제를 수정한다")
    void updatesQuestion() {
      MultipleChoiceQuestion question =
          MultipleChoiceQuestion.builder()
              .questionText("이전 질문")
              .options(List.of("1", "2"))
              .answer("1")
              .explanation("이전 해설")
              .build();

      when(questionRepository.findById(301L)).thenReturn(Optional.of(question));
      when(questionRepository.save(question)).thenReturn(question);

      QuestionUpdateRequestDto request =
          QuestionUpdateRequestDto.builder()
              .questionText("새 질문")
              .options(List.of("A", "B"))
              .answer("A")
              .explanation("새 해설")
              .build();

      QuestionResponse response = questionService.updateQuestion(301L, request);

      assertThat(response.questionText()).isEqualTo("새 질문");
      assertThat(response.options()).containsExactly("A", "B");
      assertThat(response.answer()).isEqualTo("A");
    }

    @Test
    @DisplayName("문제를 찾지 못하면 예외를 던진다")
    void updateThrowsWhenQuestionMissing() {
      when(questionRepository.findById(302L)).thenReturn(Optional.empty());

      QuestionUpdateRequestDto request =
          QuestionUpdateRequestDto.builder()
              .questionText("새 질문")
              .options(List.of("A", "B"))
              .answer("A")
              .explanation("새 해설")
              .build();

      assertThatThrownBy(() -> questionService.updateQuestion(302L, request))
          .isInstanceOf(QuestionNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("deleteQuestion")
  class DescribeDeleteQuestion {

    @Test
    @DisplayName("문제를 문제집에서 제거한다")
    void deletesQuestion() {
      QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(1L).build();
      MultipleChoiceQuestion question =
          MultipleChoiceQuestion.builder()
              .questionSet(questionSet)
              .questionText("질문")
              .options(List.of("1", "2"))
              .answer("1")
              .explanation("해설")
              .build();
      questionSet.addQuestion(question);

      when(questionRepository.findById(401L)).thenReturn(Optional.of(question));

      questionService.deleteQuestion(401L);

      assertThat(questionSet.getQuestions()).isEmpty();
      assertThat(question.getQuestionSet()).isNull();
    }
  }

  @Nested
  @DisplayName("조회")
  class DescribeFinders {

    @Test
    @DisplayName("ID로 문제를 조회한다")
    void getQuestionById() {
      MultipleChoiceQuestion question =
          MultipleChoiceQuestion.builder()
              .questionText("질문")
              .options(List.of("1", "2"))
              .answer("1")
              .explanation("해설")
              .build();

      when(questionRepository.findById(501L)).thenReturn(Optional.of(question));

      QuestionResponse response = questionService.getQuestionById(501L);

      assertThat(response.questionText()).isEqualTo("질문");
    }

    @Test
    @DisplayName("Optional로 문제를 조회한다")
    void findEntityById() {
      when(questionRepository.findById(601L)).thenReturn(Optional.of(mockQuestion()));

      Optional<Question> result = questionService.findEntityById(601L);

      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("여러 문제를 조회한다")
    void findEntitiesByIds() {
      when(questionRepository.findAllById(List.of(1L, 2L)))
          .thenReturn(List.of(mockQuestion(), mockQuestion()));

      List<Question> results = questionService.findEntitiesByIds(List.of(1L, 2L));

      assertThat(results).hasSize(2);
    }
  }

  private Question mockQuestion() {
    return MultipleChoiceQuestion.builder()
        .questionText("질문")
        .options(List.of("1", "2"))
        .answer("1")
        .explanation("해설")
        .build();
  }
}
