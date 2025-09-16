package kr.it.pullit.modules.learningsource.sourcefolder.repository;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.domain.entity.Member;

public interface SourceFolderRepository {

  SourceFolder save(SourceFolder sourceFolder);

  Optional<SourceFolder> findById(Long id);

  Optional<SourceFolder> findDefaultFolderByMemberId(Long memberId);

  SourceFolder createDefaultFolder(Member member);
}
