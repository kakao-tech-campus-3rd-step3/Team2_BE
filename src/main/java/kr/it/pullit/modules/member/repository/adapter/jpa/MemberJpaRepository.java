package kr.it.pullit.modules.member.repository.adapter.jpa;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  Optional<Member> findByKakaoId(Long kakaoId);

  Optional<Member> findByRefreshToken(String refreshToken);
}
