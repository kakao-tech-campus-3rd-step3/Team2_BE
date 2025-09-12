package kr.it.pullit.modules.questionset.service.factory;

import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.service.policy.difficulty.DifficultyPolicy;
import kr.it.pullit.modules.questionset.service.policy.difficulty.EasyPolicy;
import kr.it.pullit.modules.questionset.service.policy.difficulty.HardPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DifficultyPolicyFactory {
  private final EasyPolicy easyPolicy;
  private final HardPolicy hardPolicy;

  public DifficultyPolicy getInstance(DifficultyType difficultyType) {
    return switch (difficultyType) {
      case EASY -> easyPolicy;
      case HARD -> hardPolicy;
      default ->
          throw new IllegalArgumentException(
              "Unsupported question difficulty type: " + difficultyType);
    };
  }
}
