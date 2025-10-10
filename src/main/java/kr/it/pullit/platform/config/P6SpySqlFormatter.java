package kr.it.pullit.platform.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P6SpySqlFormatter implements MessageFormattingStrategy {

  @PostConstruct
  public void setLogMessageFormat() {
    P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
  }

  @Override
  public String formatMessage(
      int connectionId,
      String now,
      long elapsed,
      String category,
      String prepared,
      String sql,
      String url) {
    String formattedSql = formatSql(category, sql);
    return String.format(
        "[p6spy] | took %dms | %s | connection %d%s",
        elapsed, category, connectionId, formattedSql == null ? "" : formattedSql);
  }

  private String formatSql(String category, String sql) {
    if (sql == null || sql.trim().isEmpty()) {
      return sql;
    }

    boolean shouldFormat =
        Category.STATEMENT.getName().equals(category) || Category.BATCH.getName().equals(category);

    if (!shouldFormat) {
      return sql;
    }

    String trimmed = sql.trim().toLowerCase(Locale.ROOT);
    if (trimmed.startsWith("create")
        || trimmed.startsWith("alter")
        || trimmed.startsWith("comment")) {
      return FormatStyle.DDL.getFormatter().format(sql);
    } else {
      return FormatStyle.BASIC.getFormatter().format(sql);
    }
  }
}
