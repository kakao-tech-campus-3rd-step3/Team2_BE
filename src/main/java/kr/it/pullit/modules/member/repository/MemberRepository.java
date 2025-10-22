package kr.it.pullit.modules.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import kr.it.pullit.modules.member.domain.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  Optional<Member> findById(Long id);

  Member save(Member member);

  Optional<Member> findByKakaoId(Long kakaoId);

  Optional<Member> findByRefreshToken(String refreshToken);
}
