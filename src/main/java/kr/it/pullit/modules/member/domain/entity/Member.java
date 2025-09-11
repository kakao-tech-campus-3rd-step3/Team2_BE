package kr.it.pullit.modules.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long kakaoId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String name;

    @Enumerated(EnumType.STRING)
    @Column
    private MemberStatus status;

    @Builder
    public Member(Long kakaoId, String email, String name, MemberStatus status) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.name = name;
        this.status = status;
    }
}
