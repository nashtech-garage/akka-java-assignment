package shopping.order.service;

import java.time.LocalDateTime;
import java.util.UUID;


import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.Order;
import shopping.order.entity.OrderStatus;
import shopping.order.repository.OrderRepository;

public class OrderServiceImpl implements OrderService {


	private final OrderRepository repository;

	public OrderServiceImpl(OrderRepository repository) {
		this.repository = repository;
	}

	@Override
	public OrderResponse createOrder(OrderRequest orderRequest) {
		final Order order = repository.save(toOrderEntity(orderRequest, UUID.randomUUID().toString()));
		return toOrderResponse(order);
	}

	@Override
	public OrderResponse getOrderById(String id) {
		Order order = repository.findById(id).orElse(null);
		if(order == null)
			return null;
		return toOrderResponse(order);
	}

	@Override
	public OrderResponse upDateOrder(String id, OrderRequest orderRequest) {
		Order order = repository.findById(id).orElse(null);
		if(order == null)
			return null;
		order.setProductId(orderRequest.getProductId());
		order.setAmount(orderRequest.getTotalAmount());
		order.setQuantity(orderRequest.getQuantity());
		Order newOrder = repository.save(order);
		return toOrderResponse(newOrder);
	}

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder()
				.orderId(order.getId())
				.amount(order.getAmount())
				.productId(order.getProductId())
				.quantity(order.getQuantity())
				.orderStatus(order.getOrderStatus())
				.build();
	}

}
