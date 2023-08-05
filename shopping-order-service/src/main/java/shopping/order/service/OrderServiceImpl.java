package shopping.order.service;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.Order;
import shopping.order.entity.OrderStatus;
import shopping.order.repository.OrderRepository;

@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    public OrderServiceImpl (OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderResponse createOrder (OrderRequest orderRequest) {
        final Order order = repository.save(toOrderEntity(orderRequest, UUID.randomUUID().toString(), OrderStatus.CREATED));
        return toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderById (String orderId) {
        return
            repository
                .findById(orderId)
                .map(OrderServiceImpl::toOrderResponse)
                .orElse(null);
    }

    @Override
    public OrderResponse updateOrder (String orderId, OrderRequest orderRequest) {
        return repository
            .findById(orderId)
            .map(e -> toOrderResponse(repository.save(toOrderEntity(orderRequest, e.getId(), OrderStatus.UPDATED))))
            .orElse(null);
    }

    private static Order toOrderEntity (OrderRequest orderRequest, String id, OrderStatus status) {
        return Order.builder().id(id).productId(orderRequest.getProductId()).quantity(orderRequest.getQuantity())
            .amount(orderRequest.getTotalAmount()).orderDate(LocalDateTime.now()).orderStatus(status)
            .build();

    }

    private static OrderResponse toOrderResponse (Order order) {
        return OrderResponse.builder().orderId(order.getId()).orderStatus(order.getOrderStatus()).build();
    }

}
