package kr.it.pullit.modules.learningsource.sourcefolder.repository;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.learningsource.sourcefolder.repository.adapter.jpa.SourceFolderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SourceFolderRepositoryImpl implements SourceFolderRepository {

  private final SourceFolderJpaRepository sourceFolderJpaRepository;

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
    return sourceFolderJpaRepository.findByMemberIdAndName(
        memberId, SourceFolder.DEFAULT_FOLDER_NAME);
  }
}
