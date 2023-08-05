package shopping.order.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalInt;
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
		final Order order = repository.save(toOrderEntity(orderRequest, UUID.randomUUID().toString(),true));
		return toOrderResponse(order);
	}

	@Override
	public OrderRequest getOrder(String orderId) {
		Optional<Order> order = Optional.ofNullable(repository.findById(orderId));
		if (order.isPresent()) {
			return toOrderDetail(order.get());
		} else {
			return null;
		}
	}

	@Override
	public OrderResponse updateOrder(OrderRequest orderRequest, String orderId) {
		Optional<Order> orderOld = Optional.ofNullable(repository.findById(orderId));
		if (orderOld.isPresent()) {
			final Order orderResult = repository.save(toOrderEntity(orderRequest, orderId, false));
			return toOrderResponse(orderResult);
		} else {
			return null;
		}
	}


	private static Order toOrderEntity(OrderRequest orderRequest, String id, boolean newOrder) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now())
				.orderStatus(true == newOrder? OrderStatus.CREATED : OrderStatus.UPDATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder().orderId(order.getId()).orderStatus(order.getOrderStatus()).build();
	}

	private static OrderRequest toOrderDetail(Order order) {
		return OrderRequest.builder()
				.productId(order.getProductId())
				.quantity(order.getQuantity())
				.totalAmount(order.getAmount())
				.build();
	}

}
