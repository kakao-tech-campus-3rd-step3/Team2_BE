package kr.it.pullit.modules.member.api;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;

public interface MemberPublicApi {

  Optional<Member> findById(Long id);
}
