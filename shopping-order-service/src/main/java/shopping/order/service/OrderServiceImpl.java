package shopping.order.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javassist.NotFoundException;
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
    public OrderResponse updateOrder(String id, OrderRequest orderRequest) {
        Order orderWillUpdate = repository.findById(id).orElseThrow(()
                -> new NullPointerException("Order does not exist with: " + id));

        orderWillUpdate.setAmount(orderRequest.getTotalAmount());
        orderWillUpdate.setQuantity(orderRequest.getQuantity());
        orderWillUpdate.setProductId(orderRequest.getProductId());
        orderWillUpdate.setOrderStatus(OrderStatus.UPDATED);

        final Order updatedOrder = repository.save(orderWillUpdate);
        return toOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse getOrderById(String id) {
        Order order = repository.findById(id).orElseThrow(()
                -> new NullPointerException("Order does not exist with id: " + id));
        return toOrderResponse(order);
    }

    private static Order toOrderEntity(OrderRequest orderRequest, String id) {
        return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
                .amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(OrderStatus.CREATED)
                .build();

    }

    private static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate().toString())
                .totalAmount(order.getAmount())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .orderStatus(order.getOrderStatus())
                .build();
    }

}
