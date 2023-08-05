package shopping.order.service;

import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.Order;

import java.util.Optional;

public interface OrderService {
	
	OrderResponse createOrder(OrderRequest orderRequest);

	OrderRequest getOrder(String orderId);

	OrderResponse updateOrder(OrderRequest orderRequest, String orderId);
	
}
