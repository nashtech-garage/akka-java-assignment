/**
 *
 */
package shopping.order;

import static akka.http.javadsl.server.Directives.*;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shopping.order.OrderActors.Command;
import shopping.order.OrderActors.DeleteOrder;
import shopping.order.OrderActors.UpdateOrder;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderUpdateDto;

/**
 * @author loinguyenx
 *
 */
public class OrderServiceRoutes {
	private static final String ORDER_NOT_FOUND = "Order not found";
	private static final Logger log = LoggerFactory.getLogger(OrderServiceRoutes.class);
	private final ActorRef<OrderActors.Command> actorRef;
	private final Duration askTimeout;
	private final Scheduler scheduler;
	private final Marshaller<Object, RequestEntity> marshaller;


	public OrderServiceRoutes(ActorSystem<?> system, ActorRef<Command> actorRef, Marshaller<Object, RequestEntity> marshaller) {
		this.actorRef = actorRef;
		scheduler = system.scheduler();
		askTimeout = system.settings().config().getDuration("order-service.routes.ask-timeout");
		this.marshaller = marshaller;
	}

	private CompletionStage<OrderActors.ActionPerformed> createOrder(OrderRequest orderRequest) {
		return AskPattern.ask(actorRef, ref -> new OrderActors.CreateOrder(orderRequest, ref), askTimeout, scheduler);
	}

	private CompletionStage<OrderActors.GetOrderResponse> getOrderById(String orderId) {
		return AskPattern.ask(actorRef, ref -> new OrderActors.GetOrder(orderId, ref), askTimeout, scheduler);
	}

	private CompletionStage<OrderActors.DeletePerformed> deleteOrder(String orderId) {
		return AskPattern.ask(actorRef, ref -> new DeleteOrder(orderId, ref), askTimeout, scheduler);
	}

	private CompletionStage<OrderActors.UpdatePerformed> updateOrder(String orderId, OrderUpdateDto orderUpdateDto) {
		return AskPattern.ask(actorRef, ref -> new UpdateOrder(orderId, orderUpdateDto, ref), askTimeout, scheduler);
	}

	/**
	 * This method creates one route (of possibly many more that will be part of
	 * your Web App)
	 */
	// #all-routes
	public Route routes() {
		return pathPrefix("orders", () ->
				concat(
						pathEnd(() ->
								// # Create an Order
								post(() -> entity(Jackson.unmarshaller(OrderRequest.class), order ->
										onSuccess(createOrder(order), performed -> {
											log.info("Create result: {}", performed.orderResponse);
											return complete(StatusCodes.CREATED, performed, marshaller);
										})))
						),
						path(PathMatchers.segment(), (String orderId) ->
								concat(
										//#orders-get-logic
										get(() ->
												onSuccess(getOrderById(orderId), performed -> {
															log.info("Order response: {}", performed.maybeOrder);
															if (performed.maybeOrder.isPresent()) {
																return complete(StatusCodes.OK, performed.maybeOrder.get(), marshaller);
															} else {
																return complete(StatusCodes.NOT_FOUND, ORDER_NOT_FOUND);
															}
														}
												)),
										put(() -> entity(Jackson.unmarshaller(OrderUpdateDto.class), orderUpdateDto ->
												//#orders-update-logic
												onSuccess(updateOrder(orderId, orderUpdateDto), updatedOrder -> {
													if (updatedOrder.isOrderUpdated) {
														return complete(StatusCodes.OK);
													}
													return complete(StatusCodes.NOT_FOUND, ORDER_NOT_FOUND);

												})
										)),
										delete(() ->
												//#orders-delete-logic
												onSuccess(deleteOrder(orderId), performed -> {
													log.info("Delete result: {}", performed);
													if (performed.deleteResult) {
														return complete(StatusCodes.ACCEPTED);
													} else {
														return complete(StatusCodes.NOT_FOUND, ORDER_NOT_FOUND);
													}
												})
										)
								)
						)));
	}
}
