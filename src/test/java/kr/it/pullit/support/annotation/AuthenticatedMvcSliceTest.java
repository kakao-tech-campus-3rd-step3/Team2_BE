package kr.it.pullit.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.platform.security.jwt.filter.JwtAuthenticationFilter;
import kr.it.pullit.support.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MvcSliceTest
@Import(TestSecurityConfig.class)
public @interface AuthenticatedMvcSliceTest {

  @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
  Class<?>[] controllers() default {};

  @AliasFor(annotation = WebMvcTest.class, attribute = "excludeFilters")
  ComponentScan.Filter[] excludeFilters() default {
    @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class)
  };
}
