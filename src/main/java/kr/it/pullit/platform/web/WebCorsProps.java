package kr.it.pullit.platform.web;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * 디폴트 값을 http://localhost:5173으로 설정. yml에서 값 지정하면 덮어씌워짐.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "cors")
public class WebCorsProps {

  private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));

  // CORS 캐시 시간 (초 단위) - 기본값 1시간
  private int maxAgeSeconds = 3600;
}
