package shopping.order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AskPattern;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.OrderStatus;
import shopping.order.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderActorsTest {

    static final Config       config  = ConfigFactory.load("application-test");
    static final ActorTestKit testKit = ActorTestKit.create(config);

    @Mock
    OrderService orderService;

    @Test
    void testCreateOrder () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        OrderResponse orderResponse = OrderResponse
            .builder()
            .orderId(UUID_name)
            .orderStatus(OrderStatus.CREATED)
            .build();
        doAnswer(invocationOnMock -> orderResponse).when(orderService).createOrder(any(OrderRequest.class));
        ActorRef<OrderActors.Command> actorRef = testKit.spawn(OrderActors.create(orderService), "testCreateOrder");
        TestProbe<OrderActors.ActionPerformed> probe = testKit.createTestProbe();
        OrderRequest orderRequest = OrderRequest.builder().build();
        AskPattern.ask(actorRef, ref ->
            new OrderActors.CreateOrder(orderRequest, probe.ref()), Duration.ofSeconds(1), testKit.scheduler());

        var actual = probe.receiveMessage().orderResponse;
        assertThat(actual, is(orderResponse));
    }

    @Test
    void testGetOrder_Found () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        OrderResponse orderResponse = OrderResponse
            .builder()
            .orderId(UUID_name)
            .orderStatus(OrderStatus.CANCELLED)
            .build();
        doAnswer(invocationOnMock -> orderResponse).when(orderService).getOrderById(UUID_name);
        ActorRef<OrderActors.Command> actorRef = testKit.spawn(OrderActors.create(orderService), "testGetOrder_Found");
        TestProbe<OrderActors.ActionPerformed> probe = testKit.createTestProbe();
        AskPattern.ask(actorRef, ref ->
            new OrderActors.GetOrder(UUID_name, probe.ref()), Duration.ofSeconds(1), testKit.scheduler());

        var actual = probe.receiveMessage().orderResponse;
        assertThat(actual, is(orderResponse));
    }

    @Test
    void testGetOrder_NotFound () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        doAnswer(invocationOnMock -> null).when(orderService).getOrderById(UUID_name);
        ActorRef<OrderActors.Command> actorRef = testKit.spawn(OrderActors.create(orderService), "testGetOrder_NotFound");
        TestProbe<OrderActors.ActionPerformed> probe = testKit.createTestProbe();
        AskPattern.ask(actorRef, ref ->
            new OrderActors.GetOrder(UUID_name, probe.ref()), Duration.ofSeconds(1), testKit.scheduler());

        var actual = probe.receiveMessage().orderResponse;
        assertThat(actual, is(nullValue()));
    }

    @Test
    void testUpdateOrder_Found () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        OrderRequest orderRequest = OrderRequest
            .builder()
            .productId(1)
            .quantity(2)
            .totalAmount(1500)
            .build();
        OrderResponse orderResponse = OrderResponse
            .builder()
            .orderId(UUID_name)
            .orderStatus(OrderStatus.UPDATED)
            .build();
        doAnswer(invocationOnMock -> orderResponse).when(orderService).updateOrder(any(), any(OrderRequest.class));
        ActorRef<OrderActors.Command> actorRef = testKit.spawn(OrderActors.create(orderService), "testUpdateOrder_Found");
        TestProbe<OrderActors.ActionPerformed> probe = testKit.createTestProbe();
        AskPattern.ask(actorRef, ref ->
            new OrderActors.UpdateOrder(UUID_name, orderRequest, probe.ref()), Duration.ofSeconds(1), testKit.scheduler());

        var actual = probe.receiveMessage().orderResponse;
        assertThat(actual, is(orderResponse));
    }

    @Test
    void testUpdateOrder_NotFound () {
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        OrderRequest orderRequest = OrderRequest
            .builder()
            .productId(1)
            .quantity(2)
            .totalAmount(1500)
            .build();

        doAnswer(invocationOnMock -> null).when(orderService).updateOrder(any(), any(OrderRequest.class));
        ActorRef<OrderActors.Command> actorRef = testKit.spawn(OrderActors.create(orderService), "testUpdateOrder_NotFound");
        TestProbe<OrderActors.ActionPerformed> probe = testKit.createTestProbe();
        AskPattern.ask(actorRef, ref ->
            new OrderActors.UpdateOrder(UUID_name, orderRequest, probe.ref()), Duration.ofSeconds(1), testKit.scheduler());

        OrderResponse orderResponse = probe.receiveMessage().orderResponse;
        assertThat(orderResponse, is(nullValue()));
    }

    @AfterAll
    static void tearDown () {
        testKit.shutdownTestKit();
    }

}

