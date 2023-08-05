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
  public OrderResponse getOrder(String orderId) {
    return this.repository.findById(orderId)
        .map(OrderServiceImpl::toOrderResponse)
        .orElseThrow();
  }
	
	@Override
	public OrderResponse updateOrder(String orderId, OrderRequest orderRequest) {
    final Order order = this.repository.findById(orderId).orElseThrow();
    
    order.setQuantity(orderRequest.getQuantity());
    order.setProductId(orderRequest.getProductId());
    order.setAmount(orderRequest.getTotalAmount());
    
    final Order savedOrder = this.repository.save(order);
    
    return toOrderResponse(savedOrder);
	}

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder()
		    .id(id)
		    .productId(orderRequest.getProductId())
		    .quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount())
				.orderDate(LocalDateTime.now())
				.orderStatus(OrderStatus.CREATED)
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
