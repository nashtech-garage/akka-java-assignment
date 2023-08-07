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
	public OrderResponse getOrderById(String productId) {
		return toOrderResponse(repository.findById(productId).orElseThrow(() -> new RuntimeException("Not fount order "+productId)));
	}

	@Override
	public OrderResponse putOrder(String id, OrderRequest orderRequest) {
		Order order = repository.findById(id).orElseThrow(() -> new RuntimeException("Not fount order "+id));
		order.setProductId(orderRequest.getProductId());
		order.setAmount(orderRequest.getTotalAmount());
		order.setQuantity(orderRequest.getQuantity());
		return toOrderResponse(order);
	}

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder().orderId(order.getId()).orderStatus(order.getOrderStatus()).productId(order.getProductId()).quantity(order.getQuantity()).totalAmount(order.getAmount()).build();
	}

}
