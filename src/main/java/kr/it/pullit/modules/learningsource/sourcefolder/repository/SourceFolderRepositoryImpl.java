package kr.it.pullit.modules.learningsource.sourcefolder.repository;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.learningsource.sourcefolder.repository.adapter.jpa.SourceFolderJpaRepository;
import kr.it.pullit.modules.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SourceFolderRepositoryImpl implements SourceFolderRepository {

  private final SourceFolderJpaRepository sourceFolderJpaRepository;
  private static final String DEFAULT_FOLDER_NAME = "전체 폴더";

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
    return sourceFolderJpaRepository.findByMemberIdAndName(memberId, DEFAULT_FOLDER_NAME);
  }

  @Override
  public SourceFolder createDefaultFolder(Member member) {
    SourceFolder defaultFolder =
        SourceFolder.builder()
            .member(member)
            .name(DEFAULT_FOLDER_NAME)
            .description("기본으로 생성되는 폴더입니다.")
            .build();

    return save(defaultFolder);
  }
}
