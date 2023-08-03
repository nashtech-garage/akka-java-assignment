package shopping.order.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import shopping.order.entity.Order;

public interface OrderRepository extends Repository<Order, Long> {

	Order save(Order order);

	Optional<Order> findById(String id);
}
