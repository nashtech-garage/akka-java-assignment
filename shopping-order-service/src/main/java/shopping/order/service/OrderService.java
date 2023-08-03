package shopping.order.service;

import java.util.Optional;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.dto.OrderUpdateDto;
import shopping.order.entity.Order;

public interface OrderService {
	
	OrderResponse createOrder(OrderRequest orderRequest);

	Optional<Order> getOrderById(String orderId);

	boolean deleteOrder(String orderId);

	boolean updateOrder(String orderId, OrderUpdateDto orderUpdateDto);
	
}
