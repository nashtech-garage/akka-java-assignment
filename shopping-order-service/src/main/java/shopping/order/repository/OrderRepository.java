package shopping.order.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import shopping.order.entity.Order;

public interface OrderRepository extends Repository<Order, String> {

	Order save(Order order);

	Optional<Order> findById(String id);

	void deleteById(String id);
}
