package kr.it.pullit;

import java.util.List;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.client.GeminiClient;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PullitApplicationTests {

  @Test
  void generateLlmGeneratedQuestionStream() {
    LlmClient llmClient = new GeminiClient();

    llmClient.getLlmGeneratedQuestionStream(
        "컴퓨터 공학과에서 배우는 과목이야. 해당 pdf에서 나오는 내용에서 중요한 개념을 한국어를 사용하여 3문제를 만들어줘.",
        "src/test/resources/test.pdf",
        "gemini-2.5-flash-lite",
            jsonStr -> {
              System.out.println("--- onData ---");
              System.out.println(jsonStr);
            });
  }

  @Test
  void generateQuestions() {
    LlmClient llmClient = new GeminiClient();

    // ⬇️ 배열 반환에 맞게 타입 변경
    List<LlmGeneratedQuestionDto> questions =
        llmClient.getLlmGeneratedQuestionContent(
            "컴퓨터 공학과에서 배우는 과목이야. 해당 pdf에서 나오는 내용에서 중요한 개념을 한국어를 사용하여 20문제를 만들어줘",
            "src/test/resources/test.pdf",
            "gemini-2.5-flash-lite");

    System.out.println(questions);

    // 배열 자체 검증
    Assertions.assertNotNull(questions);

    // 각 아이템 검증
    for (LlmGeneratedQuestionDto q : questions) {
      Assertions.assertNotNull(q);
      Assertions.assertNotNull(q.questionText());
      Assertions.assertNotNull(q.options());
      Assertions.assertNotNull(q.answer());
      Assertions.assertNotNull(q.explanation());
      Assertions.assertEquals(4, q.options().size());
    }
  }
}
