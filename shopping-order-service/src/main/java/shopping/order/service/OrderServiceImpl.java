package shopping.order.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	public OrderResponse getOrderById(String id) {
		final var order = repository.findById(id);
		return order.map(OrderServiceImpl::toOrderResponse).orElseThrow();
	}

	@Override
	public OrderResponse updateOrder(String id, OrderRequest req) {
		final var order = repository.findById(id);
		return order.map(e -> {
			e.setProductId(req.getProductId());
			e.setAmount(req.getTotalAmount());
			e.setQuantity(req.getQuantity());
			e.setOrderStatus(OrderStatus.UPDATED);
			e.setOrderDate(LocalDateTime.now());
			repository.save(e);
			return toOrderResponse(e);
		}).orElseThrow();
	}

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder()
				.orderId(order.getId())
				.totalAmount(order.getAmount())
				.quantity(order.getQuantity())
				.productId(order.getProductId())
				.orderDate(order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
				.orderStatus(order.getOrderStatus()).build();
	}

}
