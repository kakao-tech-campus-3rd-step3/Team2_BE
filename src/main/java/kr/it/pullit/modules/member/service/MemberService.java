package kr.it.pullit.modules.member.service;

import java.util.Optional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements MemberPublicApi {

  private final MemberRepository memberRepository;
  private final CommonFolderPublicApi commonFolderPublicApi;

  @Override
  @Transactional(readOnly = true)
  public Optional<Member> findById(Long id) {
    return memberRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Member> findByKakaoId(Long kakaoId) {
    return memberRepository.findByKakaoId(kakaoId);
  }

  /** 카카오 앱이 변경되더라도 기존 멤버 정보를 그대로 사용할 수 있기 위해 조회 후 생성을 진행한다. */
  @Override
  @Transactional
  public Optional<Member> findOrCreateMember(SocialLoginCommand command) {
    Optional<Member> byKakaoId = memberRepository.findByKakaoId(command.kakaoId());
    if (byKakaoId.isPresent()) {
      return updateExistingKakaoMember(byKakaoId.get(), command);
    }

    Optional<Member> byEmail = memberRepository.findByEmail(command.email());
    if (byEmail.isPresent()) {
      return linkKakaoToExistingEmailMember(byEmail.get(), command);
    }

    return createNewMember(command);
  }

  @Override
  public Optional<Member> findByRefreshToken(String refreshToken) {
    return memberRepository.findByRefreshToken(refreshToken);
  }

  @Override
  public Page<Member> findAll(Pageable pageable) {
    return memberRepository.findAll(pageable);
  }

  @Override
  public Member save(Member member) {
    return memberRepository.save(member);
  }

  @Override
  public Optional<MemberInfoResponse> getMemberInfo(Long memberId) {
    return memberRepository.findById(memberId).map(MemberInfoResponse::from);
  }

  @Override
  public void grantAdminRole(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    member.grantAdmin();
  }

  @Override
  public void revokeAdminRole(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    member.revokeAdmin();
  }

  private Optional<Member> updateExistingKakaoMember(Member member, SocialLoginCommand command) {
    member.updateMemberInfo(command.email(), command.name());
    return Optional.of(memberRepository.save(member));
  }

  private Optional<Member> linkKakaoToExistingEmailMember(
      Member member, SocialLoginCommand command) {
    member.linkKakaoId(command.kakaoId());
    member.updateMemberInfo(command.email(), command.name());
    return Optional.of(memberRepository.save(member));
  }

  private Optional<Member> createNewMember(SocialLoginCommand command) {
    Member newMember = Member.createMember(command.kakaoId(), command.email(), command.name());
    Member savedMember = memberRepository.save(newMember);
    commonFolderPublicApi.createInitialFolders(savedMember.getId());
    return Optional.of(savedMember);
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> MemberNotFoundException.byId(memberId));
  }
}
