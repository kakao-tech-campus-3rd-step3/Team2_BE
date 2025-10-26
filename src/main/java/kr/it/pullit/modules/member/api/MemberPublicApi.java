package kr.it.pullit.modules.member.api;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberPublicApi {

  Optional<Member> findById(Long id);

  Optional<Member> findByKakaoId(Long kakaoId);

  Optional<Member> findOrCreateMember(SocialLoginCommand command);

  Optional<Member> findByRefreshToken(String refreshToken);

  Member save(Member member);

  Optional<MemberInfoResponse> getMemberInfo(Long memberId);

  void grantAdminRole(Long memberId);

  void revokeAdminRole(Long memberId);

  Page<Member> findAll(Pageable pageable);
}
