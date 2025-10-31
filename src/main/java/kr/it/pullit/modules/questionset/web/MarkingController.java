package kr.it.pullit.modules.questionset.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import kr.it.pullit.modules.auth.web.apidocs.AuthApiDocs;
import kr.it.pullit.modules.questionset.service.MarkingService;
import kr.it.pullit.modules.questionset.web.apidocs.MarkQuestionsApiDocs;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/marking")
@AuthApiDocs
public class MarkingController {

  private final MarkingService markingService;

  /**
   * 문제풀이 완료 후 채점 결과를 저장하는 엔드포인트
   *
   * @param markingRequest 문제 채점 요청 정보
   * @param memberId 회원 ID
   * @param isReviewing 오답노트 복습 모드 여부 (true: 맞힌 문제 제거, false: 틀린 문제 추가)
   * @return ResponseEntity 응답
   */
  @PostMapping
  @MarkQuestionsApiDocs
  public ResponseEntity<MarkQuestionsResponse> markQuestions(
      @RequestBody @Valid List<MarkingRequest> request,
      @AuthenticationPrincipal Long memberId,
      @RequestParam(defaultValue = "false") Boolean isReviewing) {

    MarkingServiceRequest markingServiceRequest =
        MarkingServiceRequest.of(memberId, request, isReviewing);
    MarkQuestionsResponse res = markingService.markQuestions(markingServiceRequest);
    return ResponseEntity.ok(res);
  }
}
