package kr.it.pullit.modules.questionset.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import kr.it.pullit.modules.questionset.domain.entity.Question;
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
  private final LlmClient llmClient;

  @Transactional
  @Async("llmGeneratorAsyncExecutor")
  public void generateQuestions(
      QuestionSetDto questionSetDto, QuestionGenerationSuccessCallback callback) {
    System.out.println("3333");

    DifficultyPolicy difficultyPolicy =
        difficultyPolicyFactory.getInstance(questionSetDto.getDifficulty());
    QuestionTypePolicy questionTypePolicy =
        questionTypePolicyFactory.getInstance(questionSetDto.getType());

    String difficultyPrompt = difficultyPolicy.getDifficultyPrompt();
    String questionTypePrompt = questionTypePolicy.getQuestionTypePrompt();
    String examplePrompt = questionTypePolicy.getExamplePrompt();

    String prompt = LlmClient.getPrompt(difficultyPrompt, questionTypePrompt, examplePrompt);
    // TODO: 정책에 따라 모델 변경
    List<LlmGeneratedQuestionDto> llmGeneratedQuestionDtoList =
        llmClient.getLlmGeneratedQuestionContent(
            prompt,
            getSourceFileDataBytes(questionSetDto.getSourceIds()),
            questionSetDto.getQuestionLength(),
            "gemini-2.5-flash-lite");
    System.out.println("-sd-sd-fa-sdf-asd-f");
    System.out.println(llmGeneratedQuestionDtoList);

    for (LlmGeneratedQuestionDto llmGeneratedQuestionDto : llmGeneratedQuestionDtoList) {
      // TODO: soureceId 동적으로 변경
      Question question =
          new Question(
              questionSetDto.getSourceIds().getFirst(),
              questionSetDto.getId(),
              llmGeneratedQuestionDto.questionText(),
              llmGeneratedQuestionDto.options(),
              llmGeneratedQuestionDto.answer(),
              llmGeneratedQuestionDto.explanation());
      questionRepository.save(question);

      System.out.println(question);
    }
    callback.onSuccess(llmGeneratedQuestionDtoList);
  }

  private List<byte[]> getSourceFileDataBytes(List<Long> sourceIds) {
    List<byte[]> fileDataList = new ArrayList<>();

    // TODO: 파일 S3에서 읽어오기
    // TODO: sourceIds로 파일 불러오기
    /* ------------------------- */
    final String pdfPath = "src/test/resources/test.pdf";
    byte[] pdfData;
    try {
      pdfData = Files.readAllBytes(Paths.get(pdfPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    fileDataList.add(pdfData);
    /* ------------------------- */

    return fileDataList;
  }
}
