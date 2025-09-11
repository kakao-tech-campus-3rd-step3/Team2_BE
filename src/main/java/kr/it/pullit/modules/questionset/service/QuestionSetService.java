package kr.it.pullit.modules.questionset.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
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
public class QuestionSetService implements QuestionSetPublicApi {

  private final DifficultyPolicyFactory difficultyPolicyFactory;
  private final QuestionTypePolicyFactory questionTypePolicyFactory;
  private final QuestionSetRepository questionSetRepository;
  private final LlmClient llmClient;

  @Transactional(readOnly = true)
  public QuestionSetDto questionSetGetById(Long id) {
    QuestionSet questionSet =
        questionSetRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문제집을 찾을 수 없습니다"));
    return new QuestionSetDto(questionSet);
  }

  @Transactional(readOnly = true)
  @Async("llmGeneratorAsyncExecutor")
  public void generateQuestion(
      int questionCount,
      QuestionType questionType,
      DifficultyType difficultyType,
      String filePath,
      QuestionGenerationSuccessCallback callback) {
    // TODO: 파일 S3에서 읽어오기
    byte[] fileData;
    try {
      fileData = Files.readAllBytes(Paths.get(filePath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    DifficultyPolicy difficultyPolicy = difficultyPolicyFactory.getInstance(difficultyType);
    QuestionTypePolicy questionTypePolicy = questionTypePolicyFactory.getInstance(questionType);

    String difficultyPrompt = difficultyPolicy.getDifficultyPrompt();
    String questionTypePrompt = questionTypePolicy.getQuestionTypePrompt();
    String examplePrompt = questionTypePolicy.getExamplePrompt();

    String prompt = LlmClient.getPrompt(difficultyPrompt, questionTypePrompt, examplePrompt);
    // TODO: 정책에 따라 모델 변경
    List<LlmGeneratedQuestionDto> llmGeneratedQuestionDtoList =
        llmClient.getLlmGeneratedQuestionContent(
            prompt, fileData, questionCount, "gemini-2.5-flash");

    callback.onSuccess(llmGeneratedQuestionDtoList);
  }
}
