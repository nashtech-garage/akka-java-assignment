package shopping.order.service;

import java.util.Optional;

import shopping.order.dto.OrderDTO;
import shopping.order.dto.OrderRequest;

public interface OrderService {
	
    OrderDTO createOrder(OrderRequest orderRequest);
	
    Optional<OrderDTO> updateOrder(String id, OrderDTO orderDTO);
	
	Optional<OrderDTO> getOrder(String id);
	
}
