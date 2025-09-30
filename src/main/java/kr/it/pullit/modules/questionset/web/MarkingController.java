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

  @PostMapping
  public ResponseEntity<Void> markQuestionAsIncorrect(
      @AuthenticationPrincipal Long memberId, @RequestBody List<MarkingRequest> markingRequest) {

    List<MarkingServiceRequest> markingServiceRequest =
        markingRequest.stream()
            .map(req -> new MarkingServiceRequest(memberId, req.questionId(), req.isCorrect()))
            .toList();
    markingService.markQuestionAsIncorrect(markingServiceRequest);

    return ResponseEntity.ok().build();
  }
}
