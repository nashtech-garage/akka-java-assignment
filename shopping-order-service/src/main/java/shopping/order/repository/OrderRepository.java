package shopping.order.repository;

import org.springframework.data.repository.Repository;
import shopping.order.entity.Order;

import java.util.Optional;

public interface OrderRepository extends Repository<Order, String> {

	Order save(Order order);

	Optional<Order> findById(String id);

}
