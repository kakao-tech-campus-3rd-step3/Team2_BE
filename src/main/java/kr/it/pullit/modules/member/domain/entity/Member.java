package kr.it.pullit.modules.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="members")
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private Long kakaoId;

  @Column(nullable = false, unique = true)
  private String email;

  @Column private String name;

  @Enumerated(EnumType.STRING)
  @Column
  private MemberStatus status;

  @OneToMany(mappedBy = "owner")
  private final List<QuestionSet> questionSets = new ArrayList<>();

  @OneToMany(mappedBy = "member")
  private final List<SourceFolder> sourceFolders = new ArrayList<>();

  @OneToMany(mappedBy = "member")
  private final List<Source> sources = new ArrayList<>();

  @Builder
  public Member(Long kakaoId, String email, String name, MemberStatus status) {
    this.kakaoId = kakaoId;
    this.email = email;
    this.name = name;
    this.status = status;
  }

  public static Member create(Long kakaoId, String email, String name) {
    return Member.builder()
        .kakaoId(kakaoId)
        .email(email)
        .name(name)
        .status(MemberStatus.ACTIVE)
        .build();
  }
}
