package org.websoso.support.logging.sql;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.Set;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P6SpySqlFormatter implements MessageFormattingStrategy {

    private static final Set<String> DDL_KEYWORDS = Set.of("create", "alter", "comment", "drop");

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
                                String prepared, String sql, String url) {
        String formattedSql = formatSql(category, sql);
        return String.format("[%s] | %d ms | %s", category, elapsed, formattedSql);
    }

    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty() || !Category.STATEMENT.getName().equals(category)) {
            return sql;
        }

        String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);

        if (DDL_KEYWORDS.stream().anyMatch(trimmedSQL::startsWith)) {
            return FormatStyle.DDL.getFormatter().format(sql);
        }

        return FormatStyle.BASIC.getFormatter().format(sql);
    }
}
