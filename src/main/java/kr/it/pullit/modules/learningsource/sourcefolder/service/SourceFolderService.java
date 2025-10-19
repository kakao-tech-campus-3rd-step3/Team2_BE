package kr.it.pullit.modules.learningsource.sourcefolder.service;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.api.SourceFolderPublicApi;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.learningsource.sourcefolder.repository.SourceFolderRepository;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SourceFolderService implements SourceFolderPublicApi {

  private final SourceFolderRepository sourceFolderRepository;
  private final MemberPublicApi memberPublicApi;

  @Override
  @Transactional(readOnly = true)
  public Optional<SourceFolder> findById(Long id) {
    return sourceFolderRepository.findById(id);
  }

  @Override
  @Transactional
  public SourceFolder create(SourceFolder sourceFolder) {
    return sourceFolderRepository.save(sourceFolder);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SourceFolder> findDefaultFolderByMemberId(Long memberId) {
    return sourceFolderRepository.findDefaultFolderByMemberId(memberId);
  }

  @Override
  @Transactional
  public SourceFolder findOrCreateDefaultFolder(Long memberId) {
    Optional<SourceFolder> maybeDefaultFolder =
        sourceFolderRepository.findDefaultFolderByMemberId(memberId);

    return maybeDefaultFolder.orElseGet(() -> createDefaultFolderForMember(memberId));
  }

  private SourceFolder createDefaultFolderForMember(Long memberId) {
    memberPublicApi.findById(memberId).orElseThrow(() -> MemberNotFoundException.byId(memberId));

    SourceFolder folder = SourceFolder.createDefaultFolder(memberId);

    return sourceFolderRepository.save(folder);
  }
}
