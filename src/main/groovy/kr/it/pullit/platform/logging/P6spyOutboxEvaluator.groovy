package kr.it.pullit.platform.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import java.util.regex.Pattern;

/**
 * p6spy가 남기는 로그 중 'from outbox_event'를 포함하면 true.
 * EvaluatorFilter에서 OnMatch=DENY로 설정하여 컷.
 */
public class P6spyOutboxEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    // (?is) 플래그: i는 대소문자 무시, s는 개행문자(.) 포함
    // \bfrom\s+outbox_event\b : 단어 경계(b)와 하나 이상의 공백(s+)을 포함하여 정확한 구문만 매칭
    private static final Pattern OUTBOX_PATTERN = Pattern.compile("(?is).*\\bfrom\\s+outbox_event\\b.*");

    @Override
    public boolean evaluate(ILoggingEvent event) {
        String logger = event.getLoggerName();
        String msg    = event.getFormattedMessage();

        if (logger != null && logger.equalsIgnoreCase("p6spy") && msg != null) {
            return OUTBOX_PATTERN.matcher(msg).matches();
        }

        return false;
    }
}
