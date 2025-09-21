package kr.it.pullit.modules.member.repository;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.adapter.jpa.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;

  @Override
  public Optional<Member> findByEmail(String email) {
    return memberJpaRepository.findByEmail(email);
  }

  @Override
  public Optional<Member> findById(Long id) {
    return memberJpaRepository.findById(id);
  }

  @Override
  public Member save(Member member) {
    return memberJpaRepository.save(member);
  }

  @Override
  public Optional<Member> findByKakaoId(Long kakaoId) {
    return memberJpaRepository.findByKakaoId(kakaoId);
  }
}
