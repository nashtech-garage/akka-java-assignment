package shopping.order;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.dto.OrderUpdateDto;
import shopping.order.entity.Order;
import shopping.order.service.OrderService;

public class OrderActors extends AbstractBehavior<OrderActors.Command> {

	private static final Logger logger = LoggerFactory.getLogger(OrderActors.class);

	public static final class CreateOrder implements Command {
		public final OrderRequest orderRequest;
		public final ActorRef<ActionPerformed> replyTo;

		public CreateOrder(OrderRequest orderRequest, ActorRef<ActionPerformed> replyTo) {
			this.orderRequest = orderRequest;
			this.replyTo = replyTo;
		}
	}

	public static final class GetOrder implements Command {

		public final String id;
		public final ActorRef<GetOrderResponse> replyTo;
		public GetOrder(String id, ActorRef<GetOrderResponse> replyTo) {
			this.id = id;
			this.replyTo = replyTo;
		}
	}

	public static final class GetOrderResponse {
		public final Optional<Order> maybeOrder;
		public GetOrderResponse(Optional<Order> maybeOrder) {
			this.maybeOrder = maybeOrder;
		}
	}

	public static final class DeleteOrder implements Command {

		public final String id;
		public final ActorRef<DeletePerformed> replyTo;
		public DeleteOrder(String id, ActorRef<DeletePerformed> replyTo) {
			this.id = id;
			this.replyTo = replyTo;
		}
	}

	public static final class DeletePerformed implements Command {

		public final boolean deleteResult;
		public DeletePerformed(boolean deleteResult) {
			this.deleteResult = deleteResult;
		}
	}

	public static final class ActionPerformed implements Command {
		public final OrderResponse orderResponse;
		public ActionPerformed(OrderResponse orderResponse) {
			this.orderResponse = orderResponse;
		}
	}

	public static final class UpdateOrder implements Command {
		public final String orderId;
		public final OrderUpdateDto orderUpdateDto;
		public final ActorRef<UpdatePerformed> replyTo;

		public UpdateOrder(String orderId, OrderUpdateDto orderUpdateDto, ActorRef<UpdatePerformed> replyTo) {
			this.orderId = orderId;
			this.orderUpdateDto = orderUpdateDto;
			this.replyTo = replyTo;
		}
	}

	public static final class UpdatePerformed implements Command {
		public final boolean isOrderUpdated;
		public UpdatePerformed(boolean isOrderUpdated) {
			this.isOrderUpdated = isOrderUpdated;
		}
	}

	private final OrderService orderService;

	interface Command {
	}

	private OrderActors(ActorContext<Command> context, OrderService orderService) {
		super(context);
		this.orderService = orderService;
	}

	public static Behavior<Command> create(OrderService orderService) {
		return Behaviors.setup(ctx -> new OrderActors(ctx, orderService));
	}

	private Behavior<Command> onCreateOrder(CreateOrder createOrder) {
		createOrder.replyTo.tell(new ActionPerformed(orderService.createOrder(createOrder.orderRequest)));
		return this;
	}

	private Behavior<Command> onGetOrder(GetOrder getOrder) {
		getOrder.replyTo.tell(new GetOrderResponse(orderService.getOrderById(getOrder.id)));
		return this;
	}

	private Behavior<Command> onDeleteOrder(DeleteOrder deleteOrder) {
		deleteOrder.replyTo.tell(new DeletePerformed(orderService.deleteOrder(deleteOrder.id)));
		return this;
	}

	private Behavior<Command> onUpdateOrder(UpdateOrder updateOrder) {
		final boolean b = orderService.updateOrder(updateOrder.orderId, updateOrder.orderUpdateDto);
		updateOrder.replyTo.tell(new UpdatePerformed(b));
		return this;
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(CreateOrder.class, this::onCreateOrder)
				.onMessage(GetOrder.class, this::onGetOrder)
				.onMessage(DeleteOrder.class, this::onDeleteOrder)
				.onMessage(UpdateOrder.class, this::onUpdateOrder)
				.build();
	}

}
