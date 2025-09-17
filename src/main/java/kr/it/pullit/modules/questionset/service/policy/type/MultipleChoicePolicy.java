package kr.it.pullit.modules.questionset.service.policy.type;

import org.springframework.stereotype.Component;

@Component
public class MultipleChoicePolicy implements QuestionTypePolicy {

  @Override
  public String getQuestionTypePrompt() {
    return "객관식 (정답은 하나만, 오답은 여러개인 객관식 옵션을 제공합니다.)";
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Override
  public String getExamplePrompt() {
    return """
                문제번호: 문제번호 (예시: 1)
                문제: 반드시 문제 제목만 작성 (예시: "데이터가 송신 측의 응용 계층에서 물리 계층으로 내려가면서 각 계층의 프로토콜이 필요한 제어 정보를 추가하는 과정을 무엇이라고 합니까?")
                정답: 반드시 정답 키워드만 작성 (예시: "캡슐화 (Encapsulation)")
                틀린선지: 반드시 정답이 아닌 선지 키워드만 작성 (예시: [ "다중화 (Multiplexing)", "단편화 (Fragmentation)", "오류 제어 (Error Control)" ])
                해설: 반드시 해설내용만 작성 (예시: "캡슐화는 상위 계층의 데이터(페이로드)에 현재 계층의 제어 정보(헤더)를 추가하여 하위 계층으로 전달하는 과정입니다.")
                """;
  }
}
