package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.enums.BlacklistType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlacklistBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * PostgreSQL parameter limit is much higher than this,
     * but 1000 records x 3 parameters = 3000 parameters,
     * which is perfectly safe.
     */
    private static final int MAX_BATCH_SIZE = 1000;

    /**
     * Inserts records and returns the ACTUAL number of rows
     * inserted by PostgreSQL.
     * <p>
     * ON CONFLICT DO NOTHING means existing records are skipped.
     * <p>
     * RETURNING allows us to know exactly which rows were inserted.
     */
    public int insertBatch(
            List<BlacklistBatchItem> items
    ) {

        if (items == null || items.isEmpty()) {
            return 0;
        }

        int inserted = 0;

        for (int start = 0; start < items.size(); start += MAX_BATCH_SIZE) {

            int end = Math.min(
                    start + MAX_BATCH_SIZE,
                    items.size()
            );

            List<BlacklistBatchItem> batch =
                    items.subList(start, end);

            inserted += insertSingleBatch(batch);
        }

        return inserted;
    }

    private int insertSingleBatch(
            List<BlacklistBatchItem> items
    ) {

        if (items.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("""
                INSERT INTO blacklist_entry
                    (id, value, type, reason)
                VALUES
                """);

        List<Object> parameters = new ArrayList<>(
                items.size() * 3
        );

        for (int i = 0; i < items.size(); i++) {

            if (i > 0) {
                sql.append(", ");
            }

            sql.append("(gen_random_uuid(), ?, ?, ?)");

            BlacklistBatchItem item = items.get(i);

            parameters.add(item.value());
            parameters.add(item.type().name());
            parameters.add(item.reason());
        }

        sql.append("""
                
                ON CONFLICT (type, value)
                DO NOTHING
                RETURNING id
                """);

        List<Object> insertedIds = jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    for (int i = 0; i < parameters.size(); i++) {
                        ps.setObject(i + 1, parameters.get(i));
                    }
                },
                (rs, rowNum) -> rs.getObject("id")
        );

        return insertedIds.size();
    }

    public record BlacklistBatchItem(
            String value,
            BlacklistType type,
            String reason
    ) {
    }
}