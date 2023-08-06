/**
 * 
 */
package shopping.order;

import static akka.http.javadsl.server.PathMatchers.*;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.entity;
import static akka.http.javadsl.server.Directives.onSuccess;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.post;
import static akka.http.javadsl.server.Directives.put;
import static akka.http.javadsl.server.Directives.get;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import shopping.order.OrderActors.Command;
import shopping.order.dto.OrderDTO;
import shopping.order.dto.OrderRequest;

/**
 * @author loinguyenx
 *
 */
public class OrderServiceRoutes {
	private static final Logger log = LoggerFactory.getLogger(OrderServiceRoutes.class);
	private final ActorRef<OrderActors.Command> actorRef;
	private final Duration askTimeout;
	private final Scheduler scheduler;
	
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .build();

	public OrderServiceRoutes(ActorSystem<?> system, ActorRef<Command> actorRef) {
		this.actorRef = actorRef;
		scheduler = system.scheduler();
		askTimeout = system.settings().config().getDuration("order-service.routes.ask-timeout");
	}

	private CompletionStage<OrderDTO> createOrder(OrderRequest orderRequest) {
		return AskPattern.ask(actorRef, ref -> new OrderActors.CreateOrder(orderRequest, ref), askTimeout, scheduler);
	}
	
	private CompletionStage<Optional<OrderDTO>> updateOrder(String orderId, OrderDTO order) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.UpdateOrder(orderId, order, ref), askTimeout, scheduler);
    }
	
	private CompletionStage<Optional<OrderDTO>> getOrder(String orderId) {
        return AskPattern.ask(actorRef, ref -> new OrderActors.GetOrder(orderId, ref), askTimeout, scheduler);
    }

	/**
	 * This method creates one route (of possibly many more that will be part of
	 * your Web App)
	 */
	// #all-routes
	public Route routes() {
		return pathPrefix("orders", () -> concat(
				// # Create an Order
				post(() -> entity(Jackson.unmarshaller(objectMapper, OrderRequest.class),
						orderInput -> onSuccess(createOrder(orderInput), order -> {
							log.info("Create result: {}", order);
							return complete(StatusCodes.CREATED, order, getMarshaller());
						}))),
                put(() -> path(segment(), orderId -> entity(Jackson.unmarshaller(objectMapper, OrderDTO.class),
                        orderInput -> onSuccess(updateOrder(orderId, orderInput), orderOpt -> {
                            if (orderOpt.isEmpty()) {
                                return complete(StatusCodes.NOT_FOUND);
                            }
                            return complete(StatusCodes.OK, orderOpt.get(), getMarshaller());
                        })))),
				get(() -> path(
				        segment(),
				        orderId -> onSuccess(getOrder(orderId), orderOpt -> {
                            if (orderOpt.isEmpty()) {
                                return complete(StatusCodes.NOT_FOUND);
                            }
                            return complete(StatusCodes.OK, orderOpt.get(), getMarshaller());
                        })
                    )
                )));
	}
	// #all-routes
	
	private Marshaller<OrderDTO, RequestEntity> getMarshaller() {
	    return Jackson.marshaller(objectMapper);
	}

}
