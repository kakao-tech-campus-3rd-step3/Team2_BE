package kr.it.pullit.shared.paging.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record CursorPageResponse<T>(List<T> content, Long nextCursor, boolean hasNext, int size) {

  public static <T> CursorPageResponse<T> of(List<T> content, Long nextCursor, boolean hasNext) {
    return CursorPageResponse.<T>builder()
        .content(content)
        .nextCursor(nextCursor)
        .hasNext(hasNext)
        .size(content.size())
        .build();
  }
}
