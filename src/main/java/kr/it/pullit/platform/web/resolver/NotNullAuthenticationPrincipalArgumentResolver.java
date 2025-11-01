package kr.it.pullit.platform.web.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class NotNullAuthenticationPrincipalArgumentResolver
    implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new AuthenticationCredentialsNotFoundException(
          "No authentication object found in security context");
    }
    Object principal = authentication.getPrincipal();

    if (principal == null) {
      throw new AuthenticationCredentialsNotFoundException("Principal is null");
    }
    return principal;
  }
}
