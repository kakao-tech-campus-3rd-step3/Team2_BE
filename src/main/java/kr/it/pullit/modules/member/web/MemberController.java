package kr.it.pullit.modules.member.web;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

  private final MemberPublicApi memberPublicApi;

  @GetMapping("/me")
  public ResponseEntity<MemberInfoResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.info(
        "[MemberController] Authentication object from SecurityContextHolder: {}", authentication);
    log.info("[MemberController] Injected @AuthenticationPrincipal memberId: {}", memberId);

    return memberPublicApi
        .findById(memberId)
        .map(MemberInfoResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<MemberInfoResponse> getMemberById(@PathVariable Long id) {
    return ResponseEntity.of(memberPublicApi.findById(id).map(MemberInfoResponse::from));
  }
}
