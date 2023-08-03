package shopping.order.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.dto.OrderUpdateDto;
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
	public Optional<Order> getOrderById(String orderId) {
		return repository.findById(orderId);
	}

	@Override
	public boolean deleteOrder(String orderId) {
		Optional<Order> orderOptional = repository.findById(orderId);
		if (orderOptional.isPresent()) {
			repository.deleteById(orderId);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean updateOrder(String orderId, OrderUpdateDto orderUpdateDto) {
		Optional<Order> orderOptional = repository.findById(orderId);
		if (orderOptional.isPresent()) {
			repository.save(toEntity(orderUpdateDto, orderOptional.get()));
			return true;
		}
		return false;
	}

	private static Order toEntity(OrderUpdateDto orderUpdateDto, Order order) {
		if (null != orderUpdateDto.getProductId()) {
			order.setProductId(orderUpdateDto.getProductId());
		}
		if (null != orderUpdateDto.getQuantity()) {
			order.setQuantity(orderUpdateDto.getQuantity());
		}

		if (null != orderUpdateDto.getOrderDate()) {
			order.setOrderDate(orderUpdateDto.getOrderDate());
		}

		if (null != orderUpdateDto.getOrderStatus()) {
			order.setOrderStatus(orderUpdateDto.getOrderStatus());
		}

		return order;

	}

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder().orderId(order.getId()).orderStatus(order.getOrderStatus()).build();
	}

}
