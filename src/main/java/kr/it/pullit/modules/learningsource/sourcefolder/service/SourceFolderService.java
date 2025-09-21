package kr.it.pullit.modules.learningsource.sourcefolder.service;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.api.SourceFolderPublicApi;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.learningsource.sourcefolder.repository.SourceFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceFolderService implements SourceFolderPublicApi {

  private final SourceFolderRepository sourceFolderRepository;

  @Override
  public Optional<SourceFolder> findById(Long id) {
    return sourceFolderRepository.findById(id);
  }

  @Override
  public SourceFolder create(SourceFolder sourceFolder) {
    return sourceFolderRepository.save(sourceFolder);
  }

  @Override
  public Optional<SourceFolder> findDefaultFolderByMemberId(Long memberId) {
    return sourceFolderRepository.findDefaultFolderByMemberId(memberId);
  }
}
