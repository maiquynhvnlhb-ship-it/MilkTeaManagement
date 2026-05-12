package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(CustomerOrder order);

    @Query("""
            select oi.product.name, sum(oi.quantity)
            from OrderItem oi
            where oi.order in :orders
            group by oi.product.name
            order by sum(oi.quantity) desc
            """)
    List<Object[]> findTopSellingProductsForOrders(@Param("orders") List<org.example.milkteamanagement.entity.CustomerOrder> orders);
}


