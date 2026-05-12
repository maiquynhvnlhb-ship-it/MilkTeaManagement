package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.entity.UserAccount;
import org.example.milkteamanagement.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByStaff(UserAccount staff);

    List<CustomerOrder> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
            select coalesce(sum(o.totalAmount), 0)
            from CustomerOrder o
            where o.status = :status and o.createdAt between :from and :to
            """)
    java.math.BigDecimal sumTotalByStatusAndRange(@Param("status") OrderStatus status,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);
}

