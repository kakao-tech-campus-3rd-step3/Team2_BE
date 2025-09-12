package kr.it.pullit.modules.questionset.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionSetService implements QuestionSetPublicApi {

  private final QuestionSetRepository questionSetRepository;
  private final SourceRepository sourceRepository;
  private final MemberRepository memberRepository;

  @Transactional(readOnly = true)
  public QuestionSetDto questionSetGetById(Long id) {
    QuestionSet questionSet =
        questionSetRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문제집을 찾을 수 없습니다"));
    return new QuestionSetDto(questionSet);
  }

  @Transactional
  public QuestionSetDto create(QuestionSetDto questionSetDto) {
    List<Source> sources = sourceRepository.findByIdIn(questionSetDto.getSourceIds());

    if (sources.size() != questionSetDto.getSourceIds().size()) {
      throw new IllegalArgumentException("일부 소스를 찾을 수 없습니다");
    }

    Member owner =
        memberRepository
            .findById(questionSetDto.getOwnerID())
            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다"));

    Set<Source> sourceSet = new HashSet<>(sources);

    QuestionSet questionSet =
        new QuestionSet(
            owner,
            sourceSet,
            questionSetDto.getTitle(),
            questionSetDto.getDifficulty(),
            questionSetDto.getType(),
            questionSetDto.getQuestionLength());

    QuestionSet savedQuestionSet = questionSetRepository.save(questionSet);
    return new QuestionSetDto(savedQuestionSet);
  }
}
