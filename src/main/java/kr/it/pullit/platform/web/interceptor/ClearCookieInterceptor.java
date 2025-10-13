package kr.it.pullit.platform.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.platform.web.cookie.CookieManager;
import kr.it.pullit.platform.web.interceptor.annotation.ClearCookie;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ClearCookieInterceptor implements HandlerInterceptor {

  private final CookieManager cookieManager;

  @Override
  public void afterCompletion(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull Object handler,
      Exception ex) {

    if (!(handler instanceof HandlerMethod handlerMethod) || ex != null) {
      return;
    }

    ClearCookie clearCookie = handlerMethod.getMethodAnnotation(ClearCookie.class);

    if (clearCookie == null) {
      return;
    }
    cookieManager.expireCookie(request, response, clearCookie.name());
  }
}
