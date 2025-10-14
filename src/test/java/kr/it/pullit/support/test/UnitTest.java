package kr.it.pullit.support.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.support.test.config.FixedClockConfig;
import kr.it.pullit.support.test.id.SequentialIdGeneratorConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Import({FixedClockConfig.class, SequentialIdGeneratorConfig.class})
public @interface UnitTest {}
