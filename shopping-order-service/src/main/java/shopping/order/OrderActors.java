package shopping.order;

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
import shopping.order.entity.Order;
import shopping.order.service.OrderService;
import shopping.order.service.OrderServiceImpl;

import java.time.Duration;
import java.util.Optional;

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
		public final String orderId;
		public final ActorRef<GetOrderDetail> replyTo;
		public GetOrder(String orderId, ActorRef<GetOrderDetail> replyTo) {
			this.orderId = orderId;
			this.replyTo = replyTo;
		}
	}

	public static final class UpdateOrder implements Command {
		public final String orderId;
		public final OrderRequest orderRequest;
		public final ActorRef<ActionPerformed> replyTo;
		public UpdateOrder(OrderRequest orderRequest, String orderId, ActorRef<ActionPerformed> replyTo) {
			this.orderId = orderId;
			this.orderRequest = orderRequest;
			this.replyTo = replyTo;
		}
	}

	public static final class ActionPerformed implements Command {
		public final OrderResponse orderResponse;

		public ActionPerformed(OrderResponse orderResponse) {
			this.orderResponse = orderResponse;
		}
	}

	public static final class GetOrderDetail implements Command {
		public final OrderRequest orderDetail;
		public GetOrderDetail(OrderRequest orderDetail) {
			this.orderDetail = orderDetail;
		}
	}

	private final OrderService orderService;

	interface Command {
	}

	private OrderActors(ActorContext<Command> context, OrderService orderService) {
		super(context);
		this.orderService = orderService;
	}

	private Behavior<Command> onCreateOrder(CreateOrder createOrder) {
		createOrder.replyTo.tell(new ActionPerformed(orderService.createOrder(createOrder.orderRequest)));
		return this;
	}

	private Behavior<Command> onGetOrder(GetOrder getOrder) {
		getOrder.replyTo.tell(new GetOrderDetail(orderService.getOrder(getOrder.orderId)));
		return this;
	}

	private Behavior<Command> onUpdateOrder(UpdateOrder updateOrder) {
		updateOrder.replyTo.tell(new ActionPerformed(orderService.updateOrder(updateOrder.orderRequest, updateOrder.orderId)));
		return this;
	}

	public static Behavior<Command> create(OrderService orderService) {
		return Behaviors.setup(ctx -> {
			return new OrderActors(ctx, orderService);
		});
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(CreateOrder.class, this::onCreateOrder)
				.onMessage(GetOrder.class, this::onGetOrder)
				.onMessage(UpdateOrder.class, this::onUpdateOrder)
				.build();
	}

}
