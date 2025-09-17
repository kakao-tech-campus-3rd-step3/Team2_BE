package kr.it.pullit.modules.learningsource.sourcefolder.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.api.SourceFolderPublicApi;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.learningsource.sourcefolder.repository.SourceFolderRepository;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceFolderService implements SourceFolderPublicApi {

  private final SourceFolderRepository sourceFolderRepository;
  private final MemberPublicApi memberPublicApi;

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

  @Override
  public SourceFolder findOrCreateDefaultFolder(Long memberId) {
    return findDefaultFolderByMemberId(memberId)
        .orElseGet(
            () -> {
              Member member =
                  memberPublicApi
                      .findById(memberId)
                      .orElseThrow(
                          () ->
                              new NoSuchElementException("Member not found with id: " + memberId));
              return sourceFolderRepository.createDefaultFolder(member);
            });
  }
}
