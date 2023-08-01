package shopping.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shopping.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
}
