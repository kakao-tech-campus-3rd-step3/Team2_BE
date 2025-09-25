package kr.it.pullit.modules.questionset.service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.entity.LlmPrompt;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
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

  private final QuestionRepository questionRepository;
  private final QuestionSetRepository questionSetRepository;
  private final SourcePublicApi sourcePublicApi;
  private final LlmClient llmClient;

  public List<LlmGeneratedQuestionResponse> generateQuestions(QuestionGenerationRequest request) {
    questionSetRepository
        .findById(request.questionSetId())
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "QuestionSet not found with id: " + request.questionSetId()));

    LlmPrompt llmPrompt =
        LlmPrompt.compose(
            request.specification().difficultyType(), request.specification().questionType());

    List<byte[]> sourceFileDataBytes = new ArrayList<>();
    for (Long sourceId : request.sourceIds()) {
      byte[] contentBytes = sourcePublicApi.getContentBytes(sourceId, request.ownerId());
      sourceFileDataBytes.add(contentBytes);
    }

    // TODO: 정책에 따라 모델 변경
    final String modelName = "gemini-2.5-flash-lite";
    log.info("AI 문제 생성을 시작합니다. QuestionSet ID: {}, Model: {}", request.questionSetId(), modelName);

    return llmClient.getLlmGeneratedQuestionContent(
        new LlmGeneratedQuestionRequest(
            llmPrompt.value(),
            sourceFileDataBytes,
            request.specification().questionCount(),
            modelName));
  }

  @Override
  @Transactional
  public void saveQuestion(Question question) {
    Long questionSetId = question.getQuestionSet().getId();
    questionSetRepository
        .findById(questionSetId)
        .orElseThrow(
            () -> new IllegalArgumentException("QuestionSet not found with id: " + questionSetId));

    questionRepository.save(question);
  }

  @Override
  @Transactional
  public QuestionResponse createQuestion(QuestionCreateRequest requestDto) {
    var questionSet =
        questionSetRepository
            .findById(requestDto.questionSetId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "QuestionSet not found with id: " + requestDto.questionSetId()));

    Question question =
        new Question(
            questionSet,
            requestDto.questionText(),
            requestDto.options(),
            requestDto.answer(),
            requestDto.explanation());

    Question savedQuestion = questionRepository.save(question);
    return QuestionResponse.from(savedQuestion);
  }

  @Override
  @Transactional
  public QuestionResponse updateQuestion(Long questionId, QuestionUpdateRequestDto requestDto) {
    Question question =
        questionRepository
            .findById(questionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Question not found with id: " + questionId));

    question.update(
        requestDto.questionText(),
        requestDto.options(),
        requestDto.answer(),
        requestDto.explanation());

    Question updatedQuestion = questionRepository.save(question);
    return QuestionResponse.from(updatedQuestion);
  }

  @Override
  @Transactional
  public void deleteQuestion(Long questionId) {
    if (questionRepository.findById(questionId).isEmpty()) {
      throw new IllegalArgumentException("Question not found with id: " + questionId);
    }
    questionRepository.deleteById(questionId);
  }

  @Override
  public QuestionResponse getQuestionById(Long questionId) {
    Question question =
        questionRepository
            .findById(questionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Question not found with id: " + questionId));
    return QuestionResponse.from(question);
  }
}
