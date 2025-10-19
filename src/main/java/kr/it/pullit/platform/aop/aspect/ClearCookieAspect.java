package kr.it.pullit.platform.aop.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.platform.aop.annotation.ClearCookie;
import kr.it.pullit.platform.web.cookie.CookieManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ClearCookieAspect {

  private final CookieManager cookieManager;

  @After("@annotation(kr.it.pullit.platform.aop.annotation.ClearCookie)")
  public void clearCookie(JoinPoint joinPoint) {
    log.info("쿠키 삭제 AOP 실행");
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      log.warn("AOP에서 HttpServletRequest를 찾을 수 없습니다.");
      return;
    }

    HttpServletRequest request = attributes.getRequest();
    HttpServletResponse response = attributes.getResponse();

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    ClearCookie clearCookie = signature.getMethod().getAnnotation(ClearCookie.class);

    if (response != null) {
      cookieManager.expireCookie(request, response, clearCookie.name());
      log.info("'{}' 쿠키를 삭제했습니다.", clearCookie.name());
    } else {
      log.warn("AOP에서 HttpServletResponse를 찾을 수 없어 쿠키를 삭제하지 못했습니다.");
    }
  }
}
