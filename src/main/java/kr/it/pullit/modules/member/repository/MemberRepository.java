package kr.it.pullit.modules.member.repository;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepository {

  Optional<Member> findByEmail(String email);

  Optional<Member> findById(Long id);

  Member save(Member member);

  Optional<Member> findByKakaoId(Long kakaoId);

  Optional<Member> findByRefreshToken(String refreshToken);

  Page<Member> findAll(Pageable pageable);
}
