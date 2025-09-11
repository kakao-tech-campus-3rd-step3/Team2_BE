package kr.it.pullit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.client.GeminiClient;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PullitApplicationTests {

  final String pdfPath = "src/test/resources/test.pdf";
  final String prompt =
"""
당신은 해당 pdf를 기반으로 객관식 시험 문제 출제 위원입니다.
따라서 당신은 수험자들의 학습을 잘 했는지 확인 할 수 있게 중요한 개념과 학생들이 어려워 하는 부분들을 문제로 출제하세요.

[지시사항]:
- Think step by step
- 정답은 하나만, 오답은 여러개인 객관식 옵션을 제공합니다.
- 오답은 모호하면 안됩니다. 확실하게 오답이어야 합니다.
- 학생들이 어려워 하거나, 중요하다고 생각되는 부분을 문제로 출제해야 합니다.
- 자기검증: 최종 정보에 대해서, 스스로 검증한 후 대답해야 합니다.
- 모르는 정보는 절대 문제로 출제해서는 안됩니다.
- 각각 다른 문제를 생성하세요.
- 아래 예시를 참조하여 만드세요.

문제번호: 문제번호 (예시: 1)
문제: 반드시 문제 제목만 작성 (예시: "데이터가 송신 측의 응용 계층에서 물리 계층으로 내려가면서 각 계층의 프로토콜이 필요한 제어 정보를 추가하는 과정을 무엇이라고 합니까?")
정답: 반드시 정답 키워드만 작성 (예시: "캡슐화 (Encapsulation)")
틀린선지: 반드시 정답이 아닌 선지 키워드만 작성 (예시: [ "다중화 (Multiplexing)", "단편화 (Fragmentation)", "오류 제어 (Error Control)" ])
해설: 반드시 해설내용만 작성 (예시: "캡슐화는 상위 계층의 데이터(페이로드)에 현재 계층의 제어 정보(헤더)를 추가하여 하위 계층으로 전달하는 과정입니다.")
""";

  @Test
  void generateLlmGeneratedQuestionStream() {
    byte[] pdfData;
    try {
      pdfData = Files.readAllBytes(Paths.get(pdfPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    LlmClient llmClient = new GeminiClient();

    llmClient.getLlmGeneratedQuestionStream(
        prompt,
        pdfData,
        10,
        "gemini-2.5-flash",
        jsonStr -> {
          System.out.println("--- onData ---");
          System.out.println(jsonStr);
        });
  }

  @Test
  void generateQuestions() {
    LlmClient llmClient = new GeminiClient();

    final String pdfPath = "src/test/resources/test.pdf";

    byte[] pdfData;
    try {
      pdfData = Files.readAllBytes(Paths.get(pdfPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    List<LlmGeneratedQuestionDto> questions =
        llmClient.getLlmGeneratedQuestionContent(prompt, pdfData, 10, "gemini-2.5-pro");

    for (LlmGeneratedQuestionDto question : questions) {
      System.out.println(question);
    }

    Assertions.assertNotNull(questions);

    for (LlmGeneratedQuestionDto q : questions) {
      Assertions.assertNotNull(q);
      Assertions.assertNotNull(q.questionText());
      Assertions.assertNotNull(q.wrongs());
      Assertions.assertNotNull(q.answer());
      Assertions.assertNotNull(q.explanation());
      Assertions.assertEquals(3, q.wrongs().size());
    }
  }
}
