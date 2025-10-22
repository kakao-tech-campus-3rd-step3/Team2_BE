package kr.it.pullit.platform.security.jwt.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class HeaderHidingRequestWrapper extends HttpServletRequestWrapper {

  private final String headerNameToHide;

  public HeaderHidingRequestWrapper(HttpServletRequest request, String headerNameToHide) {
    super(request);
    this.headerNameToHide = headerNameToHide;
  }

  @Override
  public String getHeader(String name) {
    if (headerNameToHide.equalsIgnoreCase(name)) {
      return null;
    }
    return super.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    if (headerNameToHide.equalsIgnoreCase(name)) {
      return Collections.emptyEnumeration();
    }
    return super.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    List<String> names = Collections.list(super.getHeaderNames());
    names.removeIf(name -> name.equalsIgnoreCase(headerNameToHide));
    return Collections.enumeration(names);
  }
}
