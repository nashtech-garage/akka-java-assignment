package shopping.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.Order;
import shopping.order.entity.OrderStatus;
import shopping.order.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Optional;
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
        logger.info("Order created successfully with id {}", order.getId());
        return toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrder(String id) {
        final Optional<Order> order = repository.findById(id);
        if (order.isEmpty()) {
            logger.info("Order not found with id {}", id);
            return null;
        }
        logger.info("Order retrieved successfully with id {}", order.get().getId());
        return toOrderResponse(order.get());
    }

    @Override
    public OrderResponse updateOrder(String id, OrderRequest orderRequest) {
        final Optional<Order> orderOptional = repository.findById(id);
        if (orderOptional.isEmpty()) {
            logger.info("Order not found with id {}", id);
            return null;
        }
        final Order order = orderOptional.get();
        order.setProductId(orderRequest.getProductId());
        order.setQuantity(orderRequest.getQuantity());
        order.setAmount(orderRequest.getTotalAmount());
        final Order updatedOrder = repository.save(order);
        logger.info("Order updated successfully with id {}", updatedOrder.getId());
        return toOrderResponse(updatedOrder);
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
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getAmount())
                .orderDate(order.getOrderDate().toString())
                .orderStatus(order.getOrderStatus())
                .build();
    }

}
