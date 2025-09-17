package kr.it.pullit.modules.learningsource.sourcefolder.repository;

import java.util.NoSuchElementException;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.learningsource.sourcefolder.repository.adapter.jpa.SourceFolderJpaRepository;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SourceFolderRepositoryImpl implements SourceFolderRepository {

  private static final String DEFAULT_FOLDER_NAME = "전체 폴더";
  private final SourceFolderJpaRepository sourceFolderJpaRepository;
  private final MemberPublicApi memberPublicApi;

  @Override
  public SourceFolder save(SourceFolder sourceFolder) {
    return sourceFolderJpaRepository.save(sourceFolder);
  }

  @Override
  public Optional<SourceFolder> findById(Long id) {
    return sourceFolderJpaRepository.findById(id);
  }

  @Override
  public Optional<SourceFolder> findDefaultFolderByMemberId(Long memberId) {
    return Optional.ofNullable(
        sourceFolderJpaRepository
            .findByMemberIdAndName(memberId, DEFAULT_FOLDER_NAME)
            .orElseGet(() -> createDefaultFolder(memberId)));
  }

  private SourceFolder createDefaultFolder(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Member not found with id: " + memberId));

    SourceFolder defaultFolder =
        SourceFolder.builder()
            .member(member)
            .name(DEFAULT_FOLDER_NAME)
            .description("기본으로 생성되는 폴더입니다.")
            .build();

    return save(defaultFolder);
  }
}
