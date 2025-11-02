package kr.it.pullit.modules.questionset.enums;

import lombok.Getter;

@Getter
public enum DifficultyType {
  EASY("쉬움 (추론이 요구되지 않습니다.)"),
  HARD("어려움 (많은 추론이 요구됩니다.)");

  private final String prompt;

  DifficultyType(String description) {
    this.prompt = description;
  }
}
