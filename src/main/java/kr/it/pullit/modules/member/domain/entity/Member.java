package kr.it.pullit.modules.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
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

  @Column(nullable = false, unique = true)
  private String email;

  @Column private String name;

  @Column private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column
  private MemberStatus status;

  @OneToMany(mappedBy = "owner")
  private List<QuestionSet> questionSets = new ArrayList<>();

  @Builder
  public Member(String email, String name, String profileImageUrl, MemberStatus status) {
    this.email = email;
    this.name = name;
    this.profileImageUrl = profileImageUrl;
    this.status = status;
  }
}
