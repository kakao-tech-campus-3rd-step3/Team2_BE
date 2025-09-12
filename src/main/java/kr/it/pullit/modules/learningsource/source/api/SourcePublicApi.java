package kr.it.pullit.modules.learningsource.source.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;

public interface SourcePublicApi {

  SourceUploadResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId);

  void processUploadComplete(SourceUploadCompleteRequest request, Long memberId);

  List<SourceResponse> getMySources(Long memberId);

  byte[] getContentBytes(Long sourceId, Long memberId);

  Optional<Source> findById(Long id);

  @SuppressWarnings("unused")
  Optional<Source> findByIdAndMemberId(Long id);
}
