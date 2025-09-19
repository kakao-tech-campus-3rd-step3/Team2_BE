package kr.it.pullit.modules.questionset.service.policy.difficulty;

import org.springframework.stereotype.Component;

@Component
public class EasyPolicy implements DifficultyPolicy {

  @Override
  public String getDifficultyPrompt() {
    return "쉬움. (추론이 요구되지 않습니다.)";
  }
}
