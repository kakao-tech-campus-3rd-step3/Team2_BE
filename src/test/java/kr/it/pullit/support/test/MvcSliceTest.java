package kr.it.pullit.support.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.shared.error.GlobalExceptionAdvice;
import kr.it.pullit.support.test.config.FixedClockConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({GlobalExceptionAdvice.class, FixedClockConfig.class})
public @interface MvcSliceTest {}
