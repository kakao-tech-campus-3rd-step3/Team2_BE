package kr.it.pullit;

import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.client.GeminiClient;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PullitApplicationTests {

  @Test
  void contextLoads() {}

  @Test
  void generateQuestions() {
    LlmClient llmClient = new GeminiClient();
    LlmGeneratedQuestionDto llmGeneratedQuestionDto =
        llmClient.getLlmGeneratedQuestion(
            "컴퓨터 공학과에서 배우는 과목이야. 해당 pdf에서 나오는 내용에서 중요한 개념을 한국어를 사용하여 문제로 만들어줘",
            "src/test/resources/test.pdf",
            "gemini-2.5-pro");

    System.out.println(llmGeneratedQuestionDto.toString());

    Assertions.assertNotNull(llmGeneratedQuestionDto);
    Assertions.assertNotNull(llmGeneratedQuestionDto.questionText());
    Assertions.assertNotNull(llmGeneratedQuestionDto.options());
    Assertions.assertNotNull(llmGeneratedQuestionDto.answer());
    Assertions.assertNotNull(llmGeneratedQuestionDto.explanation());
    Assertions.assertEquals(4, llmGeneratedQuestionDto.options().size());
  }
}
