package kr.it.pullit.modules.questionset.service.policy.difficulty;

import org.springframework.stereotype.Component;

@Component
public class HardPolicy implements DifficultyPolicy {

  @Override
  public String getDifficultyPrompt() {
    return "어려움. (많은 추론이 요구됩니다.)";
  }
}
