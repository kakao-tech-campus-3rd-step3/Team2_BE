package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.QuestionRepository;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.service.callback.QuestionGenerationSuccessCallback;
import kr.it.pullit.modules.questionset.service.factory.DifficultyPolicyFactory;
import kr.it.pullit.modules.questionset.service.factory.QuestionTypePolicyFactory;
import kr.it.pullit.modules.questionset.service.policy.difficulty.DifficultyPolicy;
import kr.it.pullit.modules.questionset.service.policy.type.QuestionTypePolicy;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService implements QuestionPublicApi {

  private final QuestionRepository questionRepository;
  private final QuestionSetRepository questionSetRepository;
  private final DifficultyPolicyFactory difficultyPolicyFactory;
  private final QuestionTypePolicyFactory questionTypePolicyFactory;
  private final SourcePublicApi sourcePublicApi;
  private final LlmClient llmClient;

  @Override
  @Async("llmGeneratorAsyncExecutor")
  public void generateQuestions(
      QuestionSetResponse questionSetResponse, QuestionGenerationSuccessCallback callback) {

    QuestionSet questionSet =
        questionSetRepository
            .findById(questionSetResponse.getId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "QuestionSet not found with id: " + questionSetResponse.getId()));

    DifficultyPolicy difficultyPolicy =
        difficultyPolicyFactory.getInstance(questionSetResponse.getDifficulty());
    QuestionTypePolicy questionTypePolicy =
        questionTypePolicyFactory.getInstance(questionSetResponse.getType());

    String difficultyPrompt = difficultyPolicy.getDifficultyPrompt();
    String questionTypePrompt = questionTypePolicy.getQuestionTypePrompt();
    String examplePrompt = questionTypePolicy.getExamplePrompt();

    String prompt =
        LlmClient.getPrompt(
            difficultyPrompt,
            questionTypePrompt,
            examplePrompt,
            questionSetResponse.getQuestionLength());

    List<byte[]> sourceFileDataBytes = new ArrayList<>();
    for (Long sourceId : questionSetResponse.getSourceIds()) {
      byte[] contentBytes =
          sourcePublicApi.getContentBytes(sourceId, questionSetResponse.getOwnerID());
      sourceFileDataBytes.add(contentBytes);
    }

    log.info(
        "AI 문제 생성을 시작합니다. QuestionSet ID: {}, Model: {}",
        questionSetResponse.getId(),
        "gemini-2.5-flash-lite");

    // TODO: 정책에 따라 모델 변경
    // TODO: soureceId 여러개 등록 가능하도록
    List<LlmGeneratedQuestionDto> llmGeneratedQuestionDtoList =
        llmClient.getLlmGeneratedQuestionContent(
            prompt,
            sourceFileDataBytes,
            questionSetResponse.getQuestionLength(),
            "gemini-1.5-flash-latest");

    saveQuestions(questionSet.getId(), llmGeneratedQuestionDtoList);

    callback.onSuccess(llmGeneratedQuestionDtoList);
  }

  @Override
  @Transactional
  public void saveQuestions(Long questionSetId, List<LlmGeneratedQuestionDto> questions) {
    QuestionSet questionSet =
        questionSetRepository
            .findById(questionSetId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "QuestionSet not found with id: " + questionSetId));

    for (LlmGeneratedQuestionDto llmGeneratedQuestionDto : questions) {
      Question question =
          new Question(
              questionSet,
              llmGeneratedQuestionDto.questionText(),
              llmGeneratedQuestionDto.options(),
              llmGeneratedQuestionDto.answer(),
              llmGeneratedQuestionDto.explanation());
      questionRepository.save(question);
    }
  }
}
