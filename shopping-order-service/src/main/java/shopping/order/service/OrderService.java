package shopping.order.service;

import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;

public interface OrderService {
	
	OrderResponse createOrder(OrderRequest orderRequest);

	OrderResponse getOrderById(String id);

	OrderResponse upDateOrder(String id, OrderRequest orderRequest);
}
