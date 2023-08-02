package shopping.order.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.Order;
import shopping.order.entity.OrderStatus;
import shopping.order.repository.OrderRepository;

public class OrderServiceImpl implements OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

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
	public OrderResponse getOrderById(String orderId) {
		final Order order = repository.findById(orderId).orElse(null);
		return toOrderResponse(order);
	}

	@Override
	public OrderResponse updateOrder(OrderRequest orderRequest) {
		if(orderRequest.getId() == null) {
			throw new RuntimeException("The given id cannot be null");
		}
		Order order = repository.findById(orderRequest.getId()).orElseThrow(() -> new RuntimeException("Not found order with id "+orderRequest.getId()));
		order.setOrderStatus(OrderStatus.UPDATED);
		order.setAmount(orderRequest.getTotalAmount());
		order.setQuantity(orderRequest.getQuantity());
		order.setProductId(orderRequest.getProductId());
		return toOrderResponse(repository.save(order));
	}

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder().orderId(order.getId()).orderStatus(order.getOrderStatus()).productId(order.getProductId()).build();
	}

}
