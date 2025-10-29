package kr.it.pullit.shared.apidocs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 이 어노테이션이 붙은 다른 어노테이션들은 API 문서 그룹으로 취급되어, ApiDocsTestUtils에 의해 자동으로 스캔됩니다. */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDocsGroup {}
