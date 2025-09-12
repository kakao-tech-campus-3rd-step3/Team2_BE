package kr.it.pullit.modules.member.web;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.service.MemberService;
import kr.it.pullit.modules.member.web.dto.MemberResponse;
import kr.it.pullit.modules.member.web.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberPublicApi memberPublicApi;
  private final MemberService memberService;

  @GetMapping("/{id}")
  public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
    return ResponseEntity.of(memberPublicApi.findById(id));
  }

  @PostMapping("/test/signup")
  public ResponseEntity<MemberResponse> signup(@RequestBody SignUpRequest request) {
    Member newMember = memberService.signup(request);
    return ResponseEntity.ok(MemberResponse.from(newMember));
  }
}
