package shopping.order.service;

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
	public OrderResponse getOrder(String id) {
		final Order order = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found order with id "+id));
		return toOrderResponse(order);
	}
	@Override
	public OrderResponse editOrder(OrderRequest orderRequest, String id){
		final Order order =  repository.findById(id).orElseThrow(() -> new RuntimeException("Not found order with id "+id));
		order.setOrderDate(LocalDateTime.now());
		order.setOrderStatus(OrderStatus.UPDATE);
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
		return OrderResponse.builder()
				.orderId(order.getId())
				.orderStatus(order.getOrderStatus())
				.amount(order.getAmount())
				.productId(order.getProductId())
				.orderDate(order.getOrderDate())
				.build();
	}

}
