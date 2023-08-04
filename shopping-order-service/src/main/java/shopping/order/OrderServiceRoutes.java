/**
 * 
 */
package shopping.order;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.PathMatchers.segment;

/**
 * @author loinguyenx
 *
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
	private CompletionStage<OrderActors.ActionPerformed> getOrderId(String id) {
		return AskPattern.ask(actorRef, ref -> new OrderActors.GetOrder(id, ref), askTimeout, scheduler);
	}
	private CompletionStage<OrderActors.ActionPerformed> editOrderId(OrderRequest orderRequest, String id) {
		return AskPattern.ask(actorRef, ref -> new OrderActors.EditOrder(id,orderRequest, ref), askTimeout, scheduler);
	}
	private static final ObjectMapper objectMapper =
			JsonMapper.builder()
					.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
					.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
					.addModule(new JavaTimeModule())
					.build();

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
				get(() -> path(segment(),  id -> {
					CompletionStage<OrderActors.ActionPerformed> order = getOrderId(id);
					return onSuccess(() -> order, performed -> complete( StatusCodes.OK, performed, Jackson.marshaller(objectMapper)).orElse(
							complete(StatusCodes.NOT_FOUND, "Not Found")
					));
				})),
				put(()-> path(segment(), id -> entity(Jackson.unmarshaller(OrderRequest.class),
						order -> onSuccess(editOrderId(order,id), performed -> {
							return complete(StatusCodes.OK, performed, Jackson.marshaller(objectMapper));
						}))))
				)

		);
	}
	// #all-routes

}
