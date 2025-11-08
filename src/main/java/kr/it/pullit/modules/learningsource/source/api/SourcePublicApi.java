package kr.it.pullit.modules.learningsource.source.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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

  InputStream getContentStream(Long sourceId, Long memberId);

  Path downloadFileToTemp(long sourceId, long memberId) throws IOException;

  Optional<Source> findById(Long id);

  Optional<Source> findByIdAndMemberId(Long id, Long memberId);

  List<Source> findByIdIn(List<Long> ids);

  void deleteSource(Long sourceId, Long memberId);

  void synchronizeS3Files();

  void migrateUploadedSourcesToReady();
}
