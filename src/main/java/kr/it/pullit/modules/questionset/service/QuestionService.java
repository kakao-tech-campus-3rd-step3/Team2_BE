package kr.it.pullit.modules.questionset.service;

import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.domain.dto.QuestionUpdateParam;
import kr.it.pullit.modules.questionset.domain.entity.LlmPrompt;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.exception.QuestionNotFoundException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotFoundException;
import kr.it.pullit.modules.questionset.repository.QuestionRepository;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionCreateRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService implements QuestionPublicApi {

  private static final String DEFAULT_MODEL_NAME = "gemini-2.5-flash-lite";

  private final QuestionRepository questionRepository;
  private final QuestionSetRepository questionSetRepository;
  private final SourcePublicApi sourcePublicApi;
  private final LlmClient llmClient;

  @Override
  public LlmGeneratedQuestionSetResponse generateQuestions(QuestionGenerationRequest request) {
    validateQuestionSetExists(request.questionSetId(), request.ownerId());

    LlmPrompt llmPrompt = createLlmPrompt(request.specification());
    List<InputStream> sourceFileDataStreams =
        getSourceFileStreams(request.sourceIds(), request.ownerId());

    return callLlmClient(
        request.questionSetId(), llmPrompt, sourceFileDataStreams, request.specification());
  }

  @Override
  @Transactional
  public void saveQuestion(Question question) {
    validateQuestionSetExists(
        question.getQuestionSet().getId(), question.getQuestionSet().getOwner().getId());
    questionRepository.save(question);
  }

  @Override
  @Transactional
  public QuestionResponse createQuestion(QuestionCreateRequest requestDto) {
    QuestionSet questionSet = findQuestionSetById(requestDto.questionSetId());
    Question question = buildQuestionFromRequest(questionSet, requestDto);
    Question savedQuestion = questionRepository.save(question);
    return QuestionResponse.from(savedQuestion);
  }

  @Override
  @Transactional
  public QuestionResponse updateQuestion(Long questionId, QuestionUpdateRequestDto requestDto) {
    Question question = findQuestionById(questionId);
    updateQuestionDetails(question, requestDto);
    Question updatedQuestion = questionRepository.save(question);
    return QuestionResponse.from(updatedQuestion);
  }

  @Override
  @Transactional
  public void deleteQuestion(Long questionId) {
    Question question = findQuestionById(questionId);
    QuestionSet questionSet = question.getQuestionSet();
    questionSet.removeQuestion(question);
  }

  @Override
  public QuestionResponse getQuestionById(Long questionId) {
    Question question = findQuestionById(questionId);
    return QuestionResponse.from(question);
  }

  @Override
  public Optional<Question> findEntityById(Long questionId) {
    return questionRepository.findById(questionId);
  }

  private void validateQuestionSetExists(Long questionSetId, Long ownerId) {
    questionSetRepository
        .findByIdAndMemberId(questionSetId, ownerId)
        .orElseThrow(() -> QuestionSetNotFoundException.byId(questionSetId));
  }

  private LlmPrompt createLlmPrompt(QuestionGenerationSpecification spec) {
    return LlmPrompt.compose(spec.difficultyType(), spec.questionType());
  }

  private List<InputStream> getSourceFileStreams(List<Long> sourceIds, Long ownerId) {
    return sourceIds.stream()
        .map(sourceId -> sourcePublicApi.getContentStream(sourceId, ownerId))
        .toList();
  }

  private LlmGeneratedQuestionSetResponse callLlmClient(
      Long questionSetId,
      LlmPrompt llmPrompt,
      List<InputStream> sourceFileDataStreams,
      QuestionGenerationSpecification spec) {
    logLlmCall(questionSetId, DEFAULT_MODEL_NAME);
    LlmGeneratedQuestionRequest request =
        createLlmRequest(llmPrompt, sourceFileDataStreams, spec, DEFAULT_MODEL_NAME);
    return llmClient.getLlmGeneratedQuestionContent(request);
  }

  private void logLlmCall(Long questionSetId, String modelName) {
    log.info("AI 문제 생성을 시작합니다. QuestionSet ID: {}, Model: {}", questionSetId, modelName);
  }

  private LlmGeneratedQuestionRequest createLlmRequest(
      LlmPrompt llmPrompt,
      List<InputStream> sourceFileDataStreams,
      QuestionGenerationSpecification spec,
      String modelName) {
    return new LlmGeneratedQuestionRequest(
        llmPrompt.value(), sourceFileDataStreams, modelName, spec);
  }

  private QuestionSet findQuestionSetById(Long questionSetId) {
    return questionSetRepository
        .findById(questionSetId)
        .orElseThrow(() -> QuestionSetNotFoundException.byId(questionSetId));
  }

  private Question buildQuestionFromRequest(
      QuestionSet questionSet, QuestionCreateRequest requestDto) {
    return switch (requestDto.questionType()) {
      case MULTIPLE_CHOICE ->
          MultipleChoiceQuestion.builder()
              .questionSet(questionSet)
              .questionText(requestDto.questionText())
              .options(requestDto.options())
              .answer(requestDto.answer())
              .explanation(requestDto.explanation())
              .build();
      case TRUE_FALSE ->
          TrueFalseQuestion.builder()
              .questionSet(questionSet)
              .questionText(requestDto.questionText())
              .answer(Boolean.parseBoolean(requestDto.answer()))
              .explanation(requestDto.explanation())
              .build();
      case SHORT_ANSWER ->
          ShortAnswerQuestion.builder()
              .questionSet(questionSet)
              .questionText(requestDto.questionText())
              .answer(requestDto.answer())
              .explanation(requestDto.explanation())
              .build();
      default ->
          throw new IllegalStateException(
              "Unsupported question type: " + requestDto.questionType());
    };
  }

  private Question findQuestionById(Long questionId) {
    return questionRepository
        .findById(questionId)
        .orElseThrow(() -> QuestionNotFoundException.byId(questionId));
  }

  private void updateQuestionDetails(Question question, QuestionUpdateRequestDto requestDto) {
    QuestionUpdateParam param =
        new QuestionUpdateParam(
            requestDto.questionText(),
            requestDto.explanation(),
            requestDto.options(),
            requestDto.answer());
    question.update(param);
  }

  private void validateQuestionExists(Long questionId) {
    if (questionRepository.findById(questionId).isEmpty()) {
      throw QuestionNotFoundException.byId(questionId);
    }
  }
}
