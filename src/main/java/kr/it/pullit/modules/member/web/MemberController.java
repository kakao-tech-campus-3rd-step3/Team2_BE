package kr.it.pullit.modules.member.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.member.web.apidocs.GetMyInfoApiDocs;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

  private final MemberPublicApi memberPublicApi;

  @GetMyInfoApiDocs
  @GetMapping("/me")
  public ResponseEntity<MemberInfoResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
    return ResponseEntity.ok(
        memberPublicApi
            .getMemberInfo(memberId)
            .orElseThrow(() -> MemberNotFoundException.byId(memberId)));
  }
}
