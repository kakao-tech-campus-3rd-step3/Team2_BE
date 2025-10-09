package kr.it.pullit.modules.questionset.domain.enums;

import lombok.Getter;

@Getter
public enum QuestionType {
  SUBJECTIVE("주관식", "TBD"),
  @SuppressWarnings("checkstyle:LineLength")
  MULTIPLE_CHOICE("객관식 (정답은 하나만, 오답은 여러개인 객관식 옵션을 제공합니다.)", createMultipleChoiceTemplate()),
  TRUE_FALSE("OX 문제", createTrueFalseTemplate()),
  SHORT_ANSWER("단답형 문제", createShortAnswerTemplate());

  private final String type;
  private final String example; // 2. 새로운 필드 추가

  QuestionType(String type, String example) {
    this.type = type;
    this.example = example;
  }

  @SuppressWarnings("checkstyle:LineLength")
  private static String createMultipleChoiceTemplate() {
    return """
        문제번호: 문제번호 (예시: 1)
        문제: 반드시 문제 제목만 작성 (예시: "데이터가 송신 측의 응용 계층에서 물리 계층으로 내려가면서 각 계층의 프로토콜이 필요한 제어 정보를 추가하는 과정을 무엇이라고 합니까?")
        정답: 반드시 정답 키워드만 작성 (예시: "캡슐화 (Encapsulation)")
        틀린선지: 반드시 정답이 아닌 선지 키워드만 작성 (예시: [ "다중화 (Multiplexing)", "단편화 (Fragmentation)","오류 제어 (Error Control)" ])
        해설: 반드시 해설내용만 작성 (예시: "캡슐화는 상위 계층의 데이터(페이로드)에 현재 계층의 제어 정보(헤더)를 추가하여 하위 계층으로 전달하는 과정입니다.")
        """;
  }

  @SuppressWarnings("checkstyle:LineLength")
  private static String createTrueFalseTemplate() {
    return """
        문제번호: 문제번호 (예시: 1)
        문제: 참 또는 거짓으로 판별할 수 있는 서술문 (예시: "OSI 7계층에서 전송 계층은 데이터의 암호화와 압축을 담당한다.")
        정답: '참' 또는 '거짓' 중 하나만 작성 (예시: "거짓")
        해설: 정답에 대한 상세 설명 (예시: "데이터의 암호화와 압축은 표현 계층(Presentation Layer)의 역할입니다. 전송 계층은 종단 간의 신뢰성 있는 데이터 전송을 담당합니다.")
        """;
  }

  @SuppressWarnings("checkstyle:LineLength")
  private static String createShortAnswerTemplate() {
    return """
        문제번호: 문제번호 (예시: 1)
        문제: 단답형으로 답할 수 있는 질문 (예시: "컴퓨터 네트워크에서 다른 네트워크로 데이터를 보내기 위한 관문 역할을 하는 장치는 무엇인가?")
        정답: 정답이 되는 하나의 단어 또는 용어 (예시: "게이트웨이")
        해설: 정답에 대한 상세 설명 (예시: "게이트웨이는 서로 다른 프로토콜을 사용하는 네트워크 간의 통신을 가능하게 하는 장치로, 데이터 형식 변환 등의 기능을 수행합니다.")
        """;
  }
}
