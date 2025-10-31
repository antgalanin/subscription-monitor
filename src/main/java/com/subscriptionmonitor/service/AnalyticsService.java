package com.subscriptionmonitor.service;

import com.subscriptionmonitor.dto.CategoryStatisticsDto;
import com.subscriptionmonitor.dto.UpcomingPaymentDto;
import com.subscriptionmonitor.dto.UserStatisticsDto;
import com.subscriptionmonitor.exception.notfound.UserNotFoundException;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.model.enums.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public UserStatisticsDto getUserStatistics(UUID userId) throws UserNotFoundException {
        log.debug("Getting statistics for user: {}", userId);

        String sql = "SELECT * FROM analytics.user_subscriptions_summary WHERE user_id = ?";

        List<UserStatisticsDto> results = jdbcTemplate.query(sql, new UserStatisticsRowMapper(), userId);

        if (results.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        return results.get(0);
    }

    public List<UpcomingPaymentDto> getUpcomingPayments(UUID userId) {
        log.debug("Getting upcoming payments for user: {}", userId);

        String sql = "SELECT * FROM analytics.upcoming_payments WHERE user_id = ? ORDER BY next_billing_date ASC";

        return jdbcTemplate.query(sql, new UpcomingPaymentRowMapper(), userId);
    }

    public List<CategoryStatisticsDto> getCategoryStatistics(UUID userId) {
        log.debug("Getting category statistics for user: {}", userId);

        String sql = """
            SELECT
                category_id,
                category_name,
                category_type,
                total_subscriptions,
                active_subscriptions,
                unique_users,
                total_cost_rub,
                total_cost_usd,
                total_cost_eur,
                avg_cost,
                avg_billing_days
            FROM analytics.user_category_statistics
            WHERE user_id = ?
            ORDER BY active_subscriptions DESC
            """;

        return jdbcTemplate.query(sql, new CategoryStatisticsRowMapper(), userId);
    }

    private static class UserStatisticsRowMapper implements RowMapper<UserStatisticsDto> {
        @Override
        public UserStatisticsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserStatisticsDto dto = new UserStatisticsDto();
            dto.setUserId((UUID) rs.getObject("user_id"));
            dto.setUsername(rs.getString("username"));
            dto.setEmail(rs.getString("email"));
            dto.setTotalSubscriptions(rs.getLong("total_subscriptions"));
            dto.setActiveSubscriptions(rs.getLong("active_subscriptions"));
            dto.setInactiveSubscriptions(rs.getLong("inactive_subscriptions"));
            dto.setTotalCostRub(rs.getBigDecimal("total_cost_rub"));
            dto.setTotalCostUsd(rs.getBigDecimal("total_cost_usd"));
            dto.setTotalCostEur(rs.getBigDecimal("total_cost_eur"));
            dto.setAvgBillingPeriodDays(rs.getLong("avg_billing_period_days"));
            return dto;
        }
    }

    private static class UpcomingPaymentRowMapper implements RowMapper<UpcomingPaymentDto> {
        @Override
        public UpcomingPaymentDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            UpcomingPaymentDto dto = new UpcomingPaymentDto();
            dto.setUserId((UUID) rs.getObject("user_id"));
            dto.setUsername(rs.getString("username"));
            dto.setEmail(rs.getString("email"));
            dto.setSubscriptionId((UUID) rs.getObject("subscription_id"));
            dto.setSubscriptionName(rs.getString("subscription_name"));
            dto.setCategoryName(rs.getString("category_name"));
            dto.setCost(rs.getBigDecimal("cost"));
            dto.setCurrency(Currency.valueOf(rs.getString("currency")));
            dto.setNextBillingDate(rs.getDate("next_billing_date").toLocalDate());
            dto.setBillingPeriodDays(rs.getInt("billing_period_days"));
            dto.setPaymentUrgency(rs.getString("payment_urgency"));
            dto.setDaysOverdue(rs.getInt("days_overdue"));
            dto.setDaysUntilPayment(rs.getInt("days_until_payment"));
            return dto;
        }
    }

    private static class CategoryStatisticsRowMapper implements RowMapper<CategoryStatisticsDto> {
        @Override
        public CategoryStatisticsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            CategoryStatisticsDto dto = new CategoryStatisticsDto();
            dto.setCategoryId((UUID) rs.getObject("category_id"));
            dto.setCategoryName(rs.getString("category_name"));
            dto.setCategoryType(CategoryType.valueOf(rs.getString("category_type")));
            dto.setTotalSubscriptions(rs.getLong("total_subscriptions"));
            dto.setActiveSubscriptions(rs.getLong("active_subscriptions"));
            dto.setUniqueUsers(rs.getLong("unique_users"));
            dto.setTotalCostRub(rs.getBigDecimal("total_cost_rub"));
            dto.setTotalCostUsd(rs.getBigDecimal("total_cost_usd"));
            dto.setTotalCostEur(rs.getBigDecimal("total_cost_eur"));
            dto.setAvgCost(rs.getBigDecimal("avg_cost"));
            dto.setAvgBillingDays(rs.getLong("avg_billing_days"));
            return dto;
        }
    }
}
