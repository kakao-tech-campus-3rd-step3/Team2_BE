package kr.it.pullit.modules.questionset.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.domain.entity.LlmPrompt;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
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
    List<byte[]> sourceFileDataBytes = getSourceFileBytes(request.sourceIds(), request.ownerId());

    return callLlmClient(
        request.questionSetId(),
        llmPrompt,
        sourceFileDataBytes,
        request.specification().questionCount());
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
    validateQuestionExists(questionId);
    questionRepository.deleteById(questionId);
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

  private List<byte[]> getSourceFileBytes(List<Long> sourceIds, Long ownerId) {
    return sourceIds.stream()
        .map(sourceId -> sourcePublicApi.getContentBytes(sourceId, ownerId))
        .toList();
  }

  private LlmGeneratedQuestionSetResponse callLlmClient(
      Long questionSetId,
      LlmPrompt llmPrompt,
      List<byte[]> sourceFileDataBytes,
      Integer questionCount) {
    logLlmCall(questionSetId, DEFAULT_MODEL_NAME);
    LlmGeneratedQuestionRequest request =
        createLlmRequest(llmPrompt, sourceFileDataBytes, questionCount, DEFAULT_MODEL_NAME);
    return llmClient.getLlmGeneratedQuestionContent(request);
  }

  private void logLlmCall(Long questionSetId, String modelName) {
    log.info("AI 문제 생성을 시작합니다. QuestionSet ID: {}, Model: {}", questionSetId, modelName);
  }

  private LlmGeneratedQuestionRequest createLlmRequest(
      LlmPrompt llmPrompt,
      List<byte[]> sourceFileDataBytes,
      Integer questionCount,
      String modelName) {
    return new LlmGeneratedQuestionRequest(
        llmPrompt.value(), sourceFileDataBytes, questionCount, modelName);
  }

  private QuestionSet findQuestionSetById(Long questionSetId) {
    return questionSetRepository
        .findById(questionSetId)
        .orElseThrow(() -> QuestionSetNotFoundException.byId(questionSetId));
  }

  private Question buildQuestionFromRequest(
      QuestionSet questionSet, QuestionCreateRequest requestDto) {
    return new Question(
        questionSet,
        requestDto.questionText(),
        requestDto.options(),
        requestDto.answer(),
        requestDto.explanation());
  }

  private Question findQuestionById(Long questionId) {
    return questionRepository
        .findById(questionId)
        .orElseThrow(() -> QuestionNotFoundException.byId(questionId));
  }

  private void updateQuestionDetails(Question question, QuestionUpdateRequestDto requestDto) {
    question.update(
        requestDto.questionText(),
        requestDto.options(),
        requestDto.answer(),
        requestDto.explanation());
  }

  private void validateQuestionExists(Long questionId) {
    if (questionRepository.findById(questionId).isEmpty()) {
      throw QuestionNotFoundException.byId(questionId);
    }
  }
}
