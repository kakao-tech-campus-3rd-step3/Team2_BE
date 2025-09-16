package kr.it.pullit.modules.questionset.service;

import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
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
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final QuestionSetRepository questionSetRepository;
  private final DifficultyPolicyFactory difficultyPolicyFactory;
  private final QuestionTypePolicyFactory questionTypePolicyFactory;
  private final SourcePublicApi sourcePublicApi;
  private final LlmClient llmClient;

  @Transactional
  @Async("llmGeneratorAsyncExecutor")
  public void generateQuestions(
      QuestionSetDto questionSetDto, QuestionGenerationSuccessCallback callback) {

    QuestionSet questionSet =
        questionSetRepository
            .findById(questionSetDto.getId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "QuestionSet not found with id: " + questionSetDto.getId()));

    DifficultyPolicy difficultyPolicy =
        difficultyPolicyFactory.getInstance(questionSetDto.getDifficulty());
    QuestionTypePolicy questionTypePolicy =
        questionTypePolicyFactory.getInstance(questionSetDto.getType());

    String difficultyPrompt = difficultyPolicy.getDifficultyPrompt();
    String questionTypePrompt = questionTypePolicy.getQuestionTypePrompt();
    String examplePrompt = questionTypePolicy.getExamplePrompt();

    String prompt = LlmClient.getPrompt(difficultyPrompt, questionTypePrompt, examplePrompt);
    // TODO: 정책에 따라 모델 변경
    // TODO: soureceId 여러개 등록 가능하도록
    List<LlmGeneratedQuestionDto> llmGeneratedQuestionDtoList =
        llmClient.getLlmGeneratedQuestionContent(
            prompt,
            List.of(
                sourcePublicApi.getContentBytes(
                    questionSetDto.getId(), questionSetDto.getOwnerID())),
            questionSetDto.getQuestionLength(),
            "gemini-2.5-flash-lite");

    for (LlmGeneratedQuestionDto llmGeneratedQuestionDto : llmGeneratedQuestionDtoList) {
      Question question =
          new Question(
              questionSetDto.getSourceIds().getFirst(),
              questionSet,
              llmGeneratedQuestionDto.questionText(),
              llmGeneratedQuestionDto.options(),
              llmGeneratedQuestionDto.answer(),
              llmGeneratedQuestionDto.explanation());
      questionRepository.save(question);
    }
    callback.onSuccess(llmGeneratedQuestionDtoList);
  }
}
