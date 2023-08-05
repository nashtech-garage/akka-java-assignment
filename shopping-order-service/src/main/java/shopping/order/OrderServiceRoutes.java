/**
 *
 */
package shopping.order;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.entity;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.onSuccess;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.post;
import static akka.http.javadsl.server.Directives.put;
import static akka.http.javadsl.server.PathMatchers.segment;
import static java.util.regex.Pattern.compile;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

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
    private static final Logger                        log = LoggerFactory.getLogger(OrderServiceRoutes.class);
    private final        ActorRef<OrderActors.Command> actorRef;
    private final        Duration                      askTimeout;
    private final        Scheduler                     scheduler;

    private static final Pattern UUID_REGX =
        compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");

    public OrderServiceRoutes (ActorSystem<?> system, ActorRef<Command> actorRef) {
        this.actorRef = actorRef;
        scheduler = system.scheduler();
        askTimeout = system.settings().config().getDuration("order-service.routes.ask-timeout");
    }

    private CompletionStage<OrderActors.ActionPerformed> createOrder (OrderRequest orderRequest) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.CreateOrder(orderRequest, ref), askTimeout, scheduler);
    }

    private CompletionStage<OrderActors.ActionPerformed> getOrder (String orderId) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.GetOrder(orderId, ref), askTimeout, scheduler);
    }

    private CompletionStage<OrderActors.ActionPerformed> updateOrder (String orderId, OrderRequest orderRequest) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.UpdateOrder(orderId, orderRequest, ref), askTimeout, scheduler);
    }

    /**
     * This method creates one route (of possibly many more that will be part of
     * your Web App)
     */
    // #all-routes
    public Route routes () {
        return pathPrefix(
            "orders",
            () -> concat(
                // # Create an Order
                post(() -> entity(
                    Jackson.unmarshaller(OrderRequest.class),
                    order -> onSuccess(createOrder(order), performed -> {
                        log.info("Create result: {}", performed.orderResponse);
                        return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                    })
                )),

                get(() -> path(
                        segment(UUID_REGX),
                        id -> onSuccess(getOrder(id), performed -> {
                            log.info("Get result: {}", performed.orderResponse);
                            if ( performed.orderResponse == null ) {
                                return complete(StatusCodes.NOT_FOUND, performed, Jackson.marshaller());
                            }
                            return complete(StatusCodes.OK, performed, Jackson.marshaller());
                        })
                    )
                ),
                put(() -> path(
                        segment(UUID_REGX),
                        id -> entity(
                            Jackson.unmarshaller(OrderRequest.class),
                            order -> onSuccess(updateOrder(id, order), performed -> {
                                log.info("Update result: {}", performed.orderResponse);
                                if ( performed.orderResponse == null ) {
                                    return complete(StatusCodes.NOT_FOUND, performed, Jackson.marshaller());
                                }
                                return complete(StatusCodes.ACCEPTED, performed, Jackson.marshaller());
                            })
                        )
                    )
                )
            )
        );

    }
    // #all-routes

}
