package kr.it.pullit.modules.questionset.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

  /**
   * 문제집의 모든 문제를 조회하는 API
   *
   * @param memberId 회원 ID
   * @param id 문제집 ID
   * @param isReviewing 오답노트 복습 모드 여부 (true: 오답노트 복습 모드, false: 오답노트 복습 모드 아님)
   * @return 문제집 응답
   */
  @GetMapping("/{id}")
  public ResponseEntity<QuestionSetResponse> getQuestionSetById(
      @AuthenticationPrincipal Long memberId,
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") Boolean isReviewing) {
    QuestionSetResponse questionSetResponse =
        questionSetPublicApi.getQuestionSetForSolving(id, memberId, isReviewing);
    return ResponseEntity.ok(questionSetResponse);
  }

  /**
   * 회원의 모든 문제집을 조회하는 API
   *
   * @param memberId 회원 ID
   * @return
   */
  @GetMapping
  public ResponseEntity<List<MyQuestionSetsResponse>> getMyQuestionSets(
      @AuthenticationPrincipal Long memberId) {
    return ResponseEntity.ok(questionSetPublicApi.getMemberQuestionSets(memberId));
  }

  /**
   * 문제집을 생성하는 API
   *
   * @param memberId 회원 ID
   * @param questionSetCreateRequestDto 문제집 생성 요청
   * @return 문제집 생성 응답
   */
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

  // TODO: 수정기능이 타이틀만 수정하는 기능임. 명칭오해가 없도록 해야함.
  /**
   * 문제집을 수정하는 API
   *
   * @param memberId 회원 ID
   * @param id 문제집 ID
   * @param questionSetUpdateRequestDto 문제집 수정 요청
   * @return 문제집 수정 응답
   */
  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateQuestionSet(
      @AuthenticationPrincipal Long memberId,
      @PathVariable Long id,
      @RequestBody QuestionSetUpdateRequestDto questionSetUpdateRequestDto) {
    questionSetPublicApi.updateTitle(id, questionSetUpdateRequestDto.title(), memberId);
    return ResponseEntity.ok().build();
  }

  /**
   * 문제집을 삭제하는 API
   *
   * @param memberId 회원 ID
   * @param id 문제집 ID
   * @return 문제집 삭제 응답
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteQuestionSet(
      @AuthenticationPrincipal Long memberId, @PathVariable Long id) {
    questionSetPublicApi.delete(id, memberId);
    return ResponseEntity.ok().build();
  }
}
