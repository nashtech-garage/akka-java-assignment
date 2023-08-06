package shopping.order.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shopping.order.dto.OrderDTO;
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
	public OrderDTO createOrder(OrderRequest orderRequest) {
		final Order order = repository.save(toOrderEntity(orderRequest, UUID.randomUUID().toString()));
		return toOrderDTO(order);
	}
	
	@Override
    public Optional<OrderDTO> updateOrder(String id, OrderDTO order) {
	    if (repository.findById(id).isEmpty()) {
	        return Optional.empty();
	    }
	    
	    order.setId(id);
        return Optional.of(repository.save(toOrderEntity(order)))
                .map(OrderServiceImpl::toOrderDTO);
    }
    
    @Override
    public Optional<OrderDTO> getOrder(String id) {
        return repository.findById(id).map(OrderServiceImpl::toOrderDTO);
    }

	private static Order toOrderEntity(OrderRequest orderRequest, String id) {
		return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
				.amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
				.build();

	}

	private static OrderResponse toOrderResponse(Order order) {
		return OrderResponse.builder().orderId(order.getId()).orderStatus(order.getOrderStatus()).build();
	}
    
    private static Order toOrderEntity(OrderDTO orderDTO) {
        return Order.builder()
                .id(orderDTO.getId())
                .productId(orderDTO.getProductId())
                .quantity(orderDTO.getQuantity())
                .amount(orderDTO.getAmount())
                .orderDate(orderDTO.getOrderDate())
                .orderStatus(orderDTO.getOrderStatus())
                .build();

    }
    
    private static OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .build();

    }

}
