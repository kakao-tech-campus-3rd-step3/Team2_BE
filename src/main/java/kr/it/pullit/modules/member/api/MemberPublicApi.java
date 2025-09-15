package kr.it.pullit.modules.member.api;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;

public interface MemberPublicApi {

  Optional<Member> findById(Long id);

  Optional<Member> findByKakaoId(Long kakaoId);

  Member create(Member member);

  Member findOrCreateMember(SocialLoginCommand command);
}
