package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.enums.BlacklistType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlacklistBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
            INSERT INTO blacklist_entry
                (id, value, type, reason)
            VALUES
                (gen_random_uuid(), ?, ?, ?)
            ON CONFLICT (type, value)
            DO NOTHING
            """;

    public int insertBatch(List<BlacklistBatchItem> items) {

        if (items == null || items.isEmpty()) {
            return 0;
        }

        int[] results = jdbcTemplate.batchUpdate(
                INSERT_SQL,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(
                            PreparedStatement ps,
                            int i
                    ) throws SQLException {

                        BlacklistBatchItem item = items.get(i);

                        ps.setString(1, item.value());
                        ps.setString(2, item.type().name());
                        ps.setString(3, item.reason());
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                }
        );

        int inserted = 0;

        for (int result : results) {
            if (result > 0) {
                inserted++;
            }
        }

        return inserted;
    }

    public record BlacklistBatchItem(
            String value,
            BlacklistType type,
            String reason
    ) {
    }
}