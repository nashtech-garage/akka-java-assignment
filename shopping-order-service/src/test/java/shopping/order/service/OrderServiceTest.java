package shopping.order.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.Order;
import shopping.order.entity.OrderStatus;
import shopping.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    OrderService orderService;
    @Mock
    OrderRepository orderRepository;

    @BeforeEach
    void init () {
        orderService = new OrderServiceImpl(orderRepository);
    }

    @Test
    void testCreateOrder () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
            .when(orderRepository).save(any(Order.class));
        UUID UUID_1 = UUID.fromString(UUID_name);
        try (var uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(UUID_1);
            assertThat(UUID.randomUUID(), is(UUID_1));
            OrderRequest orderRequest = OrderRequest
                .builder()
                .productId(1)
                .quantity(2)
                .totalAmount(1500)
                .build();
            OrderResponse order = orderService.createOrder(orderRequest);
            assertThat(order.getOrderId(), is(UUID_name));
            assertThat(order.getOrderStatus(), is(OrderStatus.CREATED));

        }
    }

    @Test
    void testGetOrderById_WithFound () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4d";
        Order order = Order.builder()
            .id(UUID_name)
            .orderStatus(OrderStatus.CANCELLED)
            .build();
        when(orderRepository.findById(anyString()))
            .thenReturn(Optional.of(order));
        OrderResponse orderResponse = orderService.getOrderById(UUID_name);
        assertThat(orderResponse.getOrderId(), is(UUID_name));
        assertThat(order.getOrderStatus(), is(OrderStatus.CANCELLED));
    }

    @Test
    void testGetOrderById_WithNotFound () {
        when(orderRepository.findById(anyString()))
            .thenReturn(Optional.empty());
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4d";
        assertThat(orderService.getOrderById(UUID_name), is(nullValue()));
    }

    @Test
    void testUpdateOrder_WithFound () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4f";
        Order order = Order.builder()
            .id(UUID_name)
            .orderStatus(OrderStatus.CREATED)
            .build();
        when(orderRepository.findById(anyString()))
            .thenReturn(Optional.of(order));
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
            .when(orderRepository).save(any(Order.class));

        OrderRequest orderRequest = OrderRequest
            .builder()
            .productId(1)
            .quantity(2)
            .totalAmount(2000)
            .build();
        OrderResponse orderResponse = orderService.updateOrder(UUID_name, orderRequest);
        assertThat(orderResponse.getOrderId(), is(UUID_name));
        assertThat(orderResponse.getOrderStatus(), is(OrderStatus.UPDATED));
    }

    @Test
    void testUpdateOrder_WithNotFound () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f5a";
        when(orderRepository.findById(anyString()))
            .thenReturn(Optional.empty());

        OrderRequest orderRequest = OrderRequest
            .builder()
            .productId(2)
            .quantity(2)
            .totalAmount(10000)
            .build();
        assertThat(orderService.updateOrder(UUID_name, orderRequest), is(nullValue()));
    }

}
