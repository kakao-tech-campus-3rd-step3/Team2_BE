package kr.it.pullit.shared.paging.dto;

import java.util.List;
import java.util.function.Function;
import lombok.Builder;

@Builder
public record CursorPageResponse<T>(List<T> content, Long nextCursor, boolean hasNext, int size) {

  public static <T> CursorPageResponse<T> of(
      List<T> contentWithExtra, int size, Function<T, Long> idExtractor) {
    boolean hasNext = contentWithExtra.size() > size;
    List<T> content = hasNext ? contentWithExtra.subList(0, size) : contentWithExtra;
    Long nextCursor = calculateNextCursor(contentWithExtra, size, hasNext, idExtractor);

    return CursorPageResponse.<T>builder()
        .content(content)
        .nextCursor(nextCursor)
        .hasNext(hasNext)
        .size(content.size())
        .build();
  }

  private static <T> Long calculateNextCursor(
      List<T> results, int size, boolean hasNext, Function<T, Long> idExtractor) {
    if (!hasNext) {
      return null;
    }
    T lastItem = results.get(size - 1);
    return idExtractor.apply(lastItem);
  }
}
