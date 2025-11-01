package kr.it.pullit.modules.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private Long kakaoId;

  @Column(nullable = false, unique = true)
  private String email;

  @Column private String name;

  @Column(length = 512)
  private String refreshToken;

  @Enumerated(EnumType.STRING)
  @Column
  private MemberStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder(access = AccessLevel.PRIVATE)
  public Member(
      final Long kakaoId,
      final String email,
      final String name,
      final MemberStatus status,
      final Role role) {
    Assert.hasText(email, "email은 비어있을 수 없습니다.");
    Assert.hasText(name, "name은 비어있을 수 없습니다.");
    Assert.notNull(status, "status는 null일 수 없습니다.");
    Assert.notNull(role, "role은 null일 수 없습니다.");

    this.kakaoId = kakaoId;
    this.email = email;
    this.name = name;
    this.status = status;
    this.role = role;
  }

  public static Member createMember(Long kakaoId, String email, String name) {
    return Member.builder()
        .kakaoId(kakaoId)
        .email(email)
        .name(name)
        .status(MemberStatus.ACTIVE)
        .role(Role.MEMBER)
        .build();
  }

  public static Member createAdmin(Long kakaoId, String email, String name) {
    return Member.builder()
        .kakaoId(kakaoId)
        .email(email)
        .name(name)
        .status(MemberStatus.ACTIVE)
        .role(Role.ADMIN)
        .build();
  }

  public void updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void linkKakaoId(Long kakaoId) {
    this.kakaoId = kakaoId;
  }

  public void updateMemberInfo(String email, String name) {
    this.email = StringUtils.hasText(email) ? email : this.email;
    this.name = StringUtils.hasText(name) ? name : this.name;
  }

  public void grantAdmin() {
    this.role = Role.ADMIN;
  }

  public void revokeAdmin() {
    this.role = Role.MEMBER;
  }
}
