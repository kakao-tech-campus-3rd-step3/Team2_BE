package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.questionset.domain.entity.IncorrectAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.repository.IncorrectAnswerQuestionRepository;
import kr.it.pullit.modules.questionset.repository.QuestionRepository;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkingService {
  private final IncorrectAnswerQuestionRepository incorrectAnswerQuestionRepository;
  private final QuestionRepository questionRepository;
  private final MemberRepository memberRepository;

  public void markQuestionAsIncorrect(List<MarkingServiceRequest> request) {
    Objects.requireNonNull(request, "questionId must not be null");

    List<IncorrectAnswerQuestion> inCorrectAnswers = new ArrayList<>();

    for (MarkingServiceRequest req : request) {
      if (req.isCorrect()) {
        continue;
      }

      Member member =
          memberRepository
              .findById(req.userId())
              .orElseThrow(() -> new IllegalArgumentException("Member not found"));
      Question question =
          questionRepository
              .findById(req.questionId())
              .orElseThrow(() -> new IllegalArgumentException("Question not found"));

      inCorrectAnswers.add(new IncorrectAnswerQuestion(member, question));
    }

    incorrectAnswerQuestionRepository.saveAll(inCorrectAnswers);
  }
}
