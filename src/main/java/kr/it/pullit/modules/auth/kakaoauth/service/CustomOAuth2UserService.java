package kr.it.pullit.modules.auth.kakaoauth.service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.MemberStatus;
import kr.it.pullit.modules.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        Long kakaoId = (Long) attributes.get("id");
        String email = (String) kakaoAccount.get("email");
        String name = (String) profile.get("nickname");

        Optional<Member> memberOptional = memberRepository.findByKakaoId(kakaoId);

        if (memberOptional.isEmpty()) {
            Member newMember = Member.builder()
                    .kakaoId(kakaoId)
                    .email(email)
                    .name(name)
                    .status(MemberStatus.ACTIVE)
                    .build();
            memberRepository.save(newMember);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "id"
        );
    }
}