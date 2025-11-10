package kr.it.pullit.modules.member.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.it.pullit.modules.auth.web.apidocs.AuthApiDocs;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.web.apidocs.GrantAdminRoleApiDocs;
import kr.it.pullit.modules.member.web.apidocs.RevokeAdminRoleApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member Admin API", description = "관리자용 회원 관리 API")
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@AuthApiDocs
public class MemberAdminController {

  private final MemberPublicApi memberPublicApi;

  @PostMapping("/{id}/grant-admin")
  @GrantAdminRoleApiDocs
  public ResponseEntity<Void> grantAdminRole(@PathVariable Long id) {
    memberPublicApi.grantAdminRole(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/revoke-admin")
  @RevokeAdminRoleApiDocs
  public ResponseEntity<Void> revokeAdminRole(@PathVariable Long id) {
    memberPublicApi.revokeAdminRole(id);
    return ResponseEntity.ok().build();
  }
}
