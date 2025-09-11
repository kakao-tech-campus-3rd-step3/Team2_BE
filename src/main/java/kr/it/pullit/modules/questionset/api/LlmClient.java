package kr.it.pullit.modules.questionset.api;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import org.springframework.stereotype.Component;

@Component
public interface LlmClient {
  static String getPrompt(
      String difficultyPrompt, String questionTypePrompt, String examplePrompt) {
    return String.format(
        """
            당신은 해당 pdf를 기반으로하는 시험 문제 출제 위원입니다.
            따라서 당신은 수험자들의 학습을 잘 했는지 확인 할 수 있게 중요한 개념과 학생들이 어려워 하는 부분들을 문제로 출제하세요.

            [지시사항]:
            - Think step by step
            - 문제유형: %s
            - 난이도: %s
            - 오답은 모호하면 안됩니다. 확실하게 오답이어야 합니다.
            - 학생들이 어려워 하거나, 중요하다고 생각되는 부분을 문제로 출제해야 합니다.
            - 자기검증: 최종 정보에 대해서, 스스로 검증한 후 대답해야 합니다.
            - 모르는 정보는 절대 문제로 출제해서는 안됩니다.
            - 각각 다른 문제를 생성하세요.
            - 아래 예시를 참조하여 만드세요.

            %s
            """,
        questionTypePrompt, difficultyPrompt, examplePrompt);
  }

  List<LlmGeneratedQuestionDto> getLlmGeneratedQuestionContent(
      String prompt, byte[] fileData, int questionCount, String model);

  void getLlmGeneratedQuestionStream(
      String prompt, byte[] fileData, int questionCount, String model, SseDataCallback callback);
}
