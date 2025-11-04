package kr.it.pullit.shared.error;

import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import kr.it.pullit.shared.error.dto.TestDto;
import kr.it.pullit.shared.error.exception.TestBusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @PostMapping("/api/test/enum")
  public void testEnum(@RequestBody TestDto testDto) {}

  @GetMapping("/api/test/invalid-refresh-token")
  public void throwInvalidRefreshTokenException() {
    throw InvalidRefreshTokenException.by();
  }

  @GetMapping("/api/test/business-exception")
  public void throwBusinessException() {
    throw new TestBusinessException();
  }

  @GetMapping("/api/test/question-set-not-ready")
  public void throwQuestionSetNotReadyException() {
    throw QuestionSetNotReadyException.byId(1L);
  }

  @GetMapping("/api/test/illegal-argument")
  public void throwIllegalArgumentException() {
    throw new IllegalArgumentException("Test Illegal Argument");
  }
}
