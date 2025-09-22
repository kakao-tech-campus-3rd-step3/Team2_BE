package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.*;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class IncorrectAnswerQuestion extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Column private Boolean corrected;
}
