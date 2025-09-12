package kr.it.pullit.modules.learningsource.sourcefolder.api;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;

public interface SourceFolderPublicApi {

  Optional<SourceFolder> findById(Long id);

  SourceFolder create(SourceFolder sourceFolder);

  Optional<SourceFolder> findDefaultFolderByMemberId(Long memberId);
}
