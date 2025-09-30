package kr.it.pullit.modules.wronganswer.web;

import java.util.List;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import kr.it.pullit.modules.wronganswer.web.dto.WrongAnswerSetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wrong-answers")
public class WrongAnswerController {

  private final WrongAnswerPublicApi wrongAnswerPublicApi;

  @GetMapping
  public ResponseEntity<CursorPageResponse<WrongAnswerSetResponse>> getMyWrongAnswers(
      @AuthenticationPrincipal Long memberId,
      @RequestParam(required = false) Long cursor,
      @RequestParam(defaultValue = "20") int size) {
    CursorPageResponse<WrongAnswerSetResponse> response =
        wrongAnswerPublicApi.getMyWrongAnswers(memberId, cursor, size);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/all")
  public ResponseEntity<List<WrongAnswerSetResponse>> getAllMyWrongAnswers(
      @AuthenticationPrincipal Long memberId) {

    List<WrongAnswerSetResponse> response = wrongAnswerPublicApi.getAllMyWrongAnswers(memberId);
    return ResponseEntity.ok(response);
  }
}
