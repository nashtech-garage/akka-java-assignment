package shopping.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.dto.OrderUpdateDto;
import shopping.order.entity.Order;
import shopping.order.entity.OrderStatus;
import shopping.order.repository.OrderRepository;
import shopping.order.service.OrderServiceImpl;

class OrderServiceImplTest {

  @Mock
  private OrderRepository repository;

  @InjectMocks
  private OrderServiceImpl orderService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void test_createOrder_ShouldReturnOrderResponse_WhenValidOrderRequest() {
    // Mock input
    OrderRequest orderRequest = new OrderRequest();
    orderRequest.setProductId(1);
    orderRequest.setQuantity(5);
    orderRequest.setTotalAmount(100);

    // Mock the repository behavior
    Order savedOrder = new Order();
    savedOrder.setId(UUID.randomUUID().toString());
    savedOrder.setProductId(orderRequest.getProductId());
    savedOrder.setQuantity(orderRequest.getQuantity());
    savedOrder.setAmount(orderRequest.getTotalAmount());
    savedOrder.setOrderDate(LocalDateTime.now());
    savedOrder.setOrderStatus(OrderStatus.CREATED);

    when(repository.save(any())).thenReturn(savedOrder);

    // Perform the test
    OrderResponse orderResponse = orderService.createOrder(orderRequest);

    // Assertions
    assertNotNull(orderResponse);
    assertNotNull(orderResponse.getOrderId());
    assertEquals(OrderStatus.CREATED, orderResponse.getOrderStatus());
  }

  @Test
  void test_getOrderById_ShouldReturnOptionalOrder_WhenOrderExists() {
    // Mock input
    String orderId = UUID.randomUUID().toString();

    // Mock the repository behavior
    Order order = new Order();
    order.setId(orderId);
    order.setProductId(1);
    order.setQuantity(1);
    order.setAmount(100);
    order.setOrderDate(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.CREATED);

    when(repository.findById(orderId)).thenReturn(Optional.of(order));

    // Perform the test
    Optional<Order> resultOrder = orderService.getOrderById(orderId);

    // Assertions
    assertTrue(resultOrder.isPresent());
    assertEquals(orderId, resultOrder.get().getId());
  }

  @Test
  void test_deleteOrder_ShouldReturnTrue_WhenOrderExists() {
    // Mock input
    String orderId = UUID.randomUUID().toString();

    // Mock the repository behavior
    Order order = new Order();
    order.setId(orderId);
    order.setProductId(1);
    order.setQuantity(5);
    order.setAmount(100);
    order.setOrderDate(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.CREATED);

    when(repository.findById(orderId)).thenReturn(Optional.of(order));

    // Perform the test
    boolean result = orderService.deleteOrder(orderId);

    // Assertions
    assertTrue(result);
    verify(repository, times(1)).deleteById(orderId);
  }

  @Test
  void test_deleteOrder_ShouldReturnFalse_WhenOrderDoesNotExist() {
    // Mock input
    String orderId = UUID.randomUUID().toString();

    // Mock the repository behavior (return empty optional)
    when(repository.findById(orderId)).thenReturn(Optional.empty());

    // Perform the test
    boolean result = orderService.deleteOrder(orderId);

    // Assertions
    assertFalse(result);
    verify(repository, never()).deleteById(any());
  }

  @Test
  void test_updateOrder_ShouldReturnTrue_WhenOrderExistsAndValidOrderUpdateDto() {
    // Mock input
    String orderId = UUID.randomUUID().toString();
    OrderUpdateDto orderUpdateDto = new OrderUpdateDto();
    orderUpdateDto.setProductId(1L);
    orderUpdateDto.setQuantity(10L);
    orderUpdateDto.setOrderStatus(OrderStatus.CREATED);

    // Mock the repository behavior
    Order existingOrder = new Order();
    existingOrder.setId(orderId);
    existingOrder.setProductId(1);
    existingOrder.setQuantity(5);
    existingOrder.setAmount(100);
    existingOrder.setOrderDate(LocalDateTime.now());
    existingOrder.setOrderStatus(OrderStatus.CREATED);

    when(repository.findById(orderId)).thenReturn(Optional.of(existingOrder));

    // Perform the test
    boolean result = orderService.updateOrder(orderId, orderUpdateDto);

    // Assertions
    assertTrue(result);
    assertEquals(orderUpdateDto.getProductId(), existingOrder.getProductId());
    assertEquals(orderUpdateDto.getQuantity(), existingOrder.getQuantity());
    assertEquals(orderUpdateDto.getOrderStatus(), existingOrder.getOrderStatus());
  }

  @Test
  void test_updateOrder_ShouldReturnFalse_WhenOrderDoesNotExist() {
    // Mock input
    String orderId = UUID.randomUUID().toString();
    OrderUpdateDto orderUpdateDto = new OrderUpdateDto();
    orderUpdateDto.setProductId(1L);
    orderUpdateDto.setQuantity(10L);
    orderUpdateDto.setOrderStatus(OrderStatus.CREATED);

    // Mock the repository behavior (return empty optional)
    when(repository.findById(orderId)).thenReturn(Optional.empty());

    // Perform the test
    boolean result = orderService.updateOrder(orderId, orderUpdateDto);

    // Assertions
    assertFalse(result);
    verify(repository, never()).save(any());
  }
}

