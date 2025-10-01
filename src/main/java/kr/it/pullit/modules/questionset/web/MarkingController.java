package kr.it.pullit.modules.questionset.web;

import java.util.List;
import kr.it.pullit.modules.questionset.service.MarkingService;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/marking")
public class MarkingController {

  private final MarkingService markingService;

  /**
   * 문제풀이 과정을 다 마치고 호출 할 엔드포인트
   * @param markingRequest questionId
   * @param memberId memberId
   * @return ResponseEntity<Void>
   */
  @PostMapping
  public ResponseEntity<Void> markQuestionAsWrong(
      @RequestBody MarkingRequest markingRequest, @AuthenticationPrincipal Long memberId) {

    MarkingServiceRequest markingServiceRequest = MarkingServiceRequest.of(memberId, markingRequest.questionIds());
    markingService.markQuestionsAsWrong(markingServiceRequest);
    return ResponseEntity.ok().build();
  }

  /**
   * 오답노트 문제풀이 과정을 다 마치고 호출할 엔드포인트
   * @return
   */
  @PostMapping
  public ResponseEntity<Void> markQuestionAsCorrect(
      @RequestBody MarkingRequest markingRequest, @AuthenticationPrincipal Long memberId
      ) {
    MarkingServiceRequest markingServiceRequest = MarkingServiceRequest.of(memberId, markingRequest.questionIds());
    markingService.markQuestionsAsCorrect(markingServiceRequest);
    return ResponseEntity.ok().build();
  }
}
