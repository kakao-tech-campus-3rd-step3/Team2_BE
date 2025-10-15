package kr.it.pullit.modules.questionset.domain.entity;

import static kr.it.pullit.modules.questionset.domain.QuestionSetConstants.TITLE_MAX_LENGTH;
import java.util.Objects;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;

public record LlmPrompt(String value) {
  public LlmPrompt {
    Objects.requireNonNull(value, "LLM prompt cannot be null");
    if (value.length() > 10_000) {
      throw new IllegalArgumentException("Prompt too long: max 10,000 characters");
    }
  }

  public static LlmPrompt compose(DifficultyType difficulty, QuestionType questionType) {
    String shortAnswerInstruction =
        questionType == QuestionType.SHORT_ANSWER ? "- 단답형 문제의 정답은 반드시 20자 이내로 생성해주세요.\n" : "";

    String composed = String.format("""
        당신은 해당 pdf를 기반으로하는 시험 문제 출제 위원입니다.
        따라서 당신은 수험자들의 학습을 잘 했는지 확인 할 수 있게 중요한 개념과 학생들이 어려워 하는 부분들을 문제로 출제하세요.
        당신은 pdf의 내용을 기반으로 문제집 제목과 문제들을 생성해야 합니다.

        [지시사항]:
        - 문제집 내용이 전부 영어라고 할 지라도 당신이 사용해야하는 주요 언어는 한국어입니다.
        - 생성되는 문제집의 제목(title)은 반드시 %d자 이내여야 합니다.
        - 난이도: %s
        - 문제유형: %s
        %s- Think step by step
        - 오답은 모호하면 안됩니다. 확실하게 오답이어야 합니다.
        - 학생들이 어려워 하거나, 중요하다고 생각되는 부분을 문제로 출제해야 합니다.
        - 자기검증: 최종 정보에 대해서, 스스로 검증한 후 대답해야 합니다.
        - 모르는 정보는 절대 문제로 출제해서는 안됩니다.
        - 각각 다른 문제를 생성하세요.
        - 아래 예시를 참조하여 만드세요.

        [예시]:
        %s
        """, TITLE_MAX_LENGTH, difficulty.getPrompt(), questionType.getType(),
        shortAnswerInstruction, questionType.getExample());

    return new LlmPrompt(composed);
  }
}
