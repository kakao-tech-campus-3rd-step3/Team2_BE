package kr.it.pullit.modules.auth.kakaoauth.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
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

  // TODO: 외부 API연동할 때 응답부에서 Value Object를 도출해서 타입체크하거나, 적어도 Optional로 받아서 예외 상황에 대한 에러 핸들링
  // TODO: CustomOAuth2UserService의 역할이 너무 많음. OAuth2 인증, 사용자 데이터 추출, 멤버 생성/조회..

  @Override
  @Transactional
  @SuppressWarnings("unchecked")
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = super.loadUser(userRequest);

    Map<String, Object> attributes = oauth2User.getAttributes();
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

    Long kakaoId = (Long) attributes.get("id");
    String email = (String) kakaoAccount.get("email");
    String name = (String) profile.get("nickname");

    if (email == null) {
      String randomId = UUID.randomUUID().toString().substring(0, 8);
      log.warn("카카오 계정에 이메일 정보가 없어 가상 이메일을 생성합니다. randomId={}", randomId);
      email = randomId + "@kakao.pullit";
    }

    Member member =
        memberPublicApi
            .findOrCreateMember(SocialLoginCommand.kakao(kakaoId, email, name))
            .orElseThrow(() -> MemberNotFoundException.byKakaoId(kakaoId));
    log.info("멤버 생성 또는 조회 완료: {}", member.getId());

    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
  }
}
