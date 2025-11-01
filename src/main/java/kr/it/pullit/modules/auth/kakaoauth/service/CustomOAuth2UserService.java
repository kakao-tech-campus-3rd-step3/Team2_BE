package kr.it.pullit.modules.auth.kakaoauth.service;

import java.util.Collections;
import kr.it.pullit.modules.auth.kakaoauth.domain.KakaoPrincipal;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final MemberPublicApi memberPublicApi;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = super.loadUser(userRequest);

    Member member = findOrCreateMember(oauth2User);

    return buildPrincipal(oauth2User, member);
  }

  private Member findOrCreateMember(OAuth2User oauth2User) {
    KakaoPrincipal kakaoPrincipal = KakaoPrincipal.from(oauth2User);

    Member member =
        memberPublicApi
            .findOrCreateMember(kakaoPrincipal.toSocialLoginCommand())
            .orElseThrow(() -> MemberNotFoundException.byKakaoId(kakaoPrincipal.kakaoId()));

    log.info("멤버 생성 또는 조회 완료: {}", member.getId());
    return member;
  }

  private DefaultOAuth2User buildPrincipal(OAuth2User oauth2User, Member member) {
    Role role = member.getRole();
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.getKey());

    return new DefaultOAuth2User(
        Collections.singleton(authority), oauth2User.getAttributes(), "id");
  }
}
