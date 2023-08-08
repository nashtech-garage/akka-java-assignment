package shopping.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.Order;
import shopping.order.entity.OrderStatus;
import shopping.order.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.UUID;

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
	public OrderResponse findOrderById(String orderId) {
		return this.repository.findById(orderId)
				.map(OrderServiceImpl::toOrderResponse)
				.orElseThrow();
	}

	@Override
	public OrderResponse updateOrder(String orderId, OrderRequest orderRequest) {
		Order order = this.repository.findById(orderId).orElseThrow();
		mapRequestToEntity(orderRequest, order);
		Order savedOrder = this.repository.save(order);
		return toOrderResponse(savedOrder);
	}

	private static void mapRequestToEntity(OrderRequest orderRequest, Order order) {
		order.setQuantity(orderRequest.getQuantity());
		order.setProductId(orderRequest.getProductId());
		order.setAmount(orderRequest.getTotalAmount());
	}

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder()
				.orderId(order.getId())
				.orderStatus(order.getOrderStatus())
				.productId(order.getProductId())
				.amount(order.getAmount())
				.quantity(order.getQuantity())
				.orderDate(order.getOrderDate().toString())
				.build();
	}

}
