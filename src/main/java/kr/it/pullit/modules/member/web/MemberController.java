package kr.it.pullit.modules.member.web;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberPublicApi memberPublicApi;

  @GetMapping("/{id}")
  public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
    return ResponseEntity.of(memberPublicApi.findById(id));
  }
}
