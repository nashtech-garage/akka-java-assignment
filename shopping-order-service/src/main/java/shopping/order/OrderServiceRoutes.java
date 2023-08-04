/**
 *
 */
package shopping.order;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.*;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import shopping.order.OrderActors.Command;
import shopping.order.dto.OrderRequest;

/**
 * @author loinguyenx
 */
public class OrderServiceRoutes {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceRoutes.class);
    private final ActorRef<OrderActors.Command> actorRef;
    private final Duration askTimeout;
    private final Scheduler scheduler;

    public OrderServiceRoutes(ActorSystem<?> system, ActorRef<Command> actorRef) {
        this.actorRef = actorRef;
        scheduler = system.scheduler();
        askTimeout = system.settings().config().getDuration("order-service.routes.ask-timeout");
    }

    private CompletionStage<OrderActors.ActionPerformed> createOrder(OrderRequest orderRequest) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.CreateOrder(orderRequest, ref), askTimeout, scheduler);
    }

    private CompletionStage<OrderActors.ActionPerformed> updateOrder(String orderId, OrderRequest orderRequest) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.UpdateOrder(orderId, orderRequest, ref), askTimeout, scheduler);
    }

    private CompletionStage<OrderActors.ActionPerformed> getOrderById(String orderId) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.GetOrderById(orderId, ref), askTimeout, scheduler);
    }


    /**
     * This method creates one route (of possibly many more that will be part of
     * your Web App)
     */
    // #all-routes
    public Route routes() {
        return pathPrefix("orders", () -> concat(
                // # Create an Order
                post(() -> entity(Jackson.unmarshaller(OrderRequest.class),
                        order -> onSuccess(createOrder(order), performed -> {
                            log.info("Create result: {}", performed.orderResponse);
                            return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                        }))),
                // # Get Order by id
                get(() -> path(segment(), id -> onSuccess(getOrderById(id),
                        performed -> {
                            log.info("Get Order result: {}", performed.orderResponse);
                            return complete(StatusCodes.OK, performed, Jackson.marshaller());
                        }))
                ),
                put(() -> path(segment(), id -> concat(
                        entity(Jackson.unmarshaller(OrderRequest.class),
                                order -> onSuccess(updateOrder(id, order),
                                        performed -> {
                                            log.info("Update Order result: {}", performed.orderResponse);
                                            return complete(StatusCodes.OK, performed, Jackson.marshaller());
                                        }
                                )
                        )
                )))
        ));
    }
    // #all-routes
}
