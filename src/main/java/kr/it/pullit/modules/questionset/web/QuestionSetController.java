package kr.it.pullit.modules.questionset.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-set")
public class QuestionSetController {

  private final QuestionSetPublicApi questionSetPublicApi;

  @GetMapping("/{id}")
  public ResponseEntity<QuestionSetResponse> getQuestionSetById(
      @AuthenticationPrincipal Long memberId,
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") Boolean isReviewing) {
    QuestionSetResponse questionSetResponse =
        questionSetPublicApi.getQuestionSetForSolving(id, memberId, isReviewing);
    return ResponseEntity.ok(questionSetResponse);
  }

  @GetMapping
  public ResponseEntity<List<MyQuestionSetsResponse>> getMyQuestionSets(
      @AuthenticationPrincipal Long memberId) {
    return ResponseEntity.ok(questionSetPublicApi.getMemberQuestionSets(memberId));
  }

  @PostMapping
  public ResponseEntity<Void> createQuestionSet(
      @AuthenticationPrincipal Long memberId,
      @Valid @RequestBody QuestionSetCreateRequestDto questionSetCreateRequestDto) {
    QuestionSetResponse questionSetResponse =
        questionSetPublicApi.create(questionSetCreateRequestDto, memberId);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(questionSetResponse.getId())
            .toUri();

    return ResponseEntity.created(location).build();
  }
}
