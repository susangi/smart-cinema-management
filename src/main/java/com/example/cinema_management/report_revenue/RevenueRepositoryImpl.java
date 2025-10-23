package com.example.cinema_management.report_revenue;

import com.example.cinema_management.report_revenue.dto.RevenueReportParams;
import com.example.cinema_management.report_revenue.dto.RevenueRow;
import com.example.cinema_management.report_revenue.repository.RevenueRepository;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
class RevenueRepositoryImpl implements RevenueRepository {

    private final EntityManager em;

    RevenueRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<RevenueRow> findRevenue(RevenueReportParams p) {
        String basisColumn = p.basis().equals("PERFORMANCE_DATE")
                ? "st.date"
                : "DATE(pay.payment_date)";  
        String sql = """
      SELECT
                         %s AS basis_date,
                         m.id   AS movie_id,
                         m.title AS movie_title,
                         st.id  AS show_time_id,
                         pay.payment_method,
                         SUM(CASE WHEN pay.status='SUCCESS' AND pay.amount>0 THEN pay.amount ELSE 0 END) AS sales,
                         SUM(CASE WHEN (pay.status='REFUNDED' OR pay.amount<0) THEN ABS(pay.amount) ELSE 0 END) AS refunds
                       FROM payment pay
                       JOIN booking b   ON b.booking_id = pay.booking_id
                       JOIN schedules st ON st.id = b.showtime_id
                       JOIN movies m ON m.id = st.movie_id
                       WHERE DATE(pay.payment_date) BETWEEN :start AND :end
                         AND (:movieId IS NULL OR m.id = :movieId)
                         AND (:showTimeId IS NULL OR st.id = :showTimeId)
                         AND (:method IS NULL OR pay.payment_method = :method)
                         AND pay.status IN ('SUCCESS','REFUNDED')
                       GROUP BY basis_date, m.id, m.title, st.id, pay.payment_method
                       ORDER BY basis_date, m.title
    """.formatted(basisColumn);

        var q = em.createNativeQuery(sql);
        q.setParameter("start", p.startDate());
        q.setParameter("end", p.endDate());
        q.setParameter("movieId", p.movieId());
        q.setParameter("showTimeId", p.showTimeId());
        String method = (p.paymentMethod() == null || p.paymentMethod().isBlank()) ? null : p.paymentMethod();
        q.setParameter("method", method);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        System.out.println(rows);
        return rows.stream().map(r -> new RevenueRow(
                ((Date) r[0]).toLocalDate(),
                ((Number) r[1]).longValue(),
                (String) r[2],
                ((Number) r[3]).longValue(),
                (String) r[4],
                toBig(r[5]),
                toBig(r[6]),
                toBig(r[5]).subtract(toBig(r[6]))
        )).toList();
    }

    private static BigDecimal toBig(Object v) {
        return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
    }
}

