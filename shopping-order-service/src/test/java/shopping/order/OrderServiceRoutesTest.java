package shopping.order;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.entity;
import static akka.http.javadsl.server.Directives.onSuccess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.mockito.Mockito;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.ContentType;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.entity.OrderStatus;
import shopping.order.service.OrderService;


public class OrderServiceRoutesTest extends JUnitRouteTest {

    static final Config config = ConfigFactory.load("application-test");

    static final ActorTestKit testKit = ActorTestKit.create(config);

    @Test
    public void testHttpGetOrder () {
        OrderService orderService = mock(OrderService.class);
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        OrderResponse orderResponse = OrderResponse
            .builder()
            .orderId(UUID_name)
            .orderStatus(OrderStatus.CANCELLED)
            .build();
        doAnswer(invocationOnMock -> orderResponse).when(orderService).getOrderById(UUID_name);
        ActorRef<OrderActors.Command> actorRef = testKit.spawn(OrderActors.create(orderService), "testHttpGetOrder");
        TestRoute testRoute = testRoute(new OrderServiceRoutes(testKit.system(), actorRef).routes());
        testRoute.run(HttpRequest.GET("/orders/5fc03087-d265-11e7-b8c6-83e29cd24f4c"))
            .assertStatusCode(200)
            .assertEntity("{\"orderResponse\":{\"orderId\":\"5fc03087-d265-11e7-b8c6-83e29cd24f4c\",\"orderStatus\":\"CANCELLED\"}}");
    }

    @Test
    public void testHttpGetOrder_NotFound () {
        OrderService orderService = mock(OrderService.class);
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4c";
        doAnswer(invocationOnMock -> null).when(orderService).getOrderById(UUID_name);
        ActorRef<OrderActors.Command> actorRef = testKit.spawn(OrderActors.create(orderService), "testHttpGetOrder_NotFound");
        TestRoute testRoute = testRoute(new OrderServiceRoutes(testKit.system(), actorRef).routes());
        testRoute.run(HttpRequest.GET("/orders/5fc03087-d265-11e7-b8c6-83e29cd24f4c"))
            .assertStatusCode(404)
            .assertEntity("{\"orderResponse\":null}");
    }

    @Test
    public void testHttpCreateOrder () {
        OrderService orderService = mock(OrderService.class);
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4d";
        UUID UUID_1 = UUID.fromString(UUID_name);
        OrderResponse orderResponse = OrderResponse
            .builder()
            .orderId(UUID_name)
            .orderStatus(OrderStatus.CREATED)
            .build();
        doAnswer(invocationOnMock -> orderResponse).when(orderService).createOrder(any(OrderRequest.class));

        try (var uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(UUID_1);
            assertThat(UUID.randomUUID(), is(UUID_1));
            ActorRef<OrderActors.Command> actorRef =
                testKit.spawn(OrderActors.create(orderService), "testHttpCreateOrder");
            TestRoute testRoute = testRoute(new OrderServiceRoutes(testKit.system(), actorRef).routes());
            testRoute.run(HttpRequest.POST("/orders")
                              .withEntity(ContentTypes.APPLICATION_JSON, "{\"productId\": 2,\"quantity\": 2,\"totalAmount\": 1000}")
                )
                .assertStatusCode(201)
                .assertEntity("{\"orderResponse\":{\"orderId\":\"5fc03087-d265-11e7-b8c6-83e29cd24f4d\",\"orderStatus\":\"CREATED\"}}");
        }

    }

    @Test
    public void testHttpUpdateOrder_Found () {
        OrderService orderService = mock(OrderService.class);
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4e";
        OrderResponse orderResponse = OrderResponse
            .builder()
            .orderId(UUID_name)
            .orderStatus(OrderStatus.UPDATED)
            .build();
        doAnswer(invocationOnMock -> orderResponse).when(orderService).updateOrder(anyString(), any(OrderRequest.class));
        var path = "/orders/" + UUID_name;
        ActorRef<OrderActors.Command> actorRef =
            testKit.spawn(OrderActors.create(orderService), "testHttpUpdateOrder_Found");
        TestRoute testRoute = testRoute(new OrderServiceRoutes(testKit.system(), actorRef).routes());
        testRoute.run(
                HttpRequest
                    .PUT(path)
                    .withEntity(ContentTypes.APPLICATION_JSON, "{\"productId\": 2,\"quantity\": 2,\"totalAmount\": 1000}")
            )
            .assertStatusCode(202)
            .assertEntity("{\"orderResponse\":{\"orderId\":\"5fc03087-d265-11e7-b8c6-83e29cd24f4e\",\"orderStatus\":\"UPDATED\"}}");

    }

    @Test
    public void testHttpUpdateOrder_NotFound () {
        OrderService orderService = mock(OrderService.class);
        String UUID_name = "5fc03087-d265-11e7-b8c6-83e29cd24f4f";
        doAnswer(invocationOnMock -> null).when(orderService).updateOrder(anyString(), any(OrderRequest.class));
        var path = "/orders/" + UUID_name;
        ActorRef<OrderActors.Command> actorRef =
            testKit.spawn(OrderActors.create(orderService), "testHttpUpdateOrder_NotFound");
        TestRoute testRoute = testRoute(new OrderServiceRoutes(testKit.system(), actorRef).routes());
        testRoute.run(
                HttpRequest
                    .PUT(path)
                    .withEntity(ContentTypes.APPLICATION_JSON, "{\"productId\": 2,\"quantity\": 2,\"totalAmount\": 1000}")
            )
            .assertStatusCode(404);

    }

    @AfterAll
    static void tearDown () {
        testKit.shutdownTestKit();
    }


}