package kr.it.pullit.modules.member.web;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MemberAdminController {

  private final MemberPublicApi memberPublicApi;

  @PostMapping("/{id}/grant-admin")
  public ResponseEntity<Void> grantAdminRole(@PathVariable Long id) {
    memberPublicApi.grantAdminRole(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/revoke-admin")
  public ResponseEntity<Void> revokeAdminRole(@PathVariable Long id) {
    memberPublicApi.revokeAdminRole(id);
    return ResponseEntity.ok().build();
  }
}
