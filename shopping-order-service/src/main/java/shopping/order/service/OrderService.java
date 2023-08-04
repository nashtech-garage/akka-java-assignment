package shopping.order.service;

import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;

public interface OrderService {
	
	OrderResponse createOrder(OrderRequest orderRequest);

	/**
	 * retrieve order by id
	 * @param id of order
	 * @return order response
	 */
	OrderResponse getOrderById(String id);

	OrderResponse updateOrder(String id, OrderRequest req);
	
}
