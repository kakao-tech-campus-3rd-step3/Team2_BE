package kr.it.pullit.modules.questionset.web;

import java.util.List;
import kr.it.pullit.modules.questionset.service.MarkingService;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
      @RequestBody List<MarkingRequest> markingRequest) {

    final Long userId = 1L; // TODO: 인증 기능이 추가되면 수정 필요
    List<MarkingServiceRequest> markingServiceRequest =
        markingRequest.stream()
            .map(req -> new MarkingServiceRequest(userId, req.questionId(), req.isCorrect()))
            .toList();
    markingService.markQuestionAsIncorrect(markingServiceRequest);

    return ResponseEntity.ok().build();
  }
}
