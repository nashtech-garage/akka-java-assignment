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
import shopping.order.dto.OrderDTO;
import shopping.order.dto.OrderRequest;
import shopping.order.service.OrderService;

public class OrderActors extends AbstractBehavior<OrderActors.Command> {

	private static final Logger logger = LoggerFactory.getLogger(OrderActors.class);

	public static final class CreateOrder implements Command {
		public final OrderRequest orderRequest;
		public final ActorRef<OrderDTO> replyTo;

		public CreateOrder(OrderRequest orderRequest, ActorRef<OrderDTO> replyTo) {
			this.orderRequest = orderRequest;
			this.replyTo = replyTo;
		}
	}
	
	public static final class UpdateOrder implements Command {
	    public final String orderId;
        public final OrderDTO order;
        public final ActorRef<Optional<OrderDTO>> replyTo;

        public UpdateOrder(String orderId, OrderDTO order, ActorRef<Optional<OrderDTO>> replyTo) {
            this.orderId = orderId;
            this.order = order;
            this.replyTo = replyTo;
        }
    }
	
	public static final class GetOrder implements Command {
        public final String orderId;
        public final ActorRef<Optional<OrderDTO>> replyTo;

        public GetOrder(String orderId, ActorRef<Optional<OrderDTO>> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
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
		createOrder.replyTo.tell(orderService.createOrder(createOrder.orderRequest));
		return this;
	}
	
	private Behavior<Command> onUpdateOrder(UpdateOrder updateOrder) {
	    updateOrder.replyTo.tell(orderService.updateOrder(updateOrder.orderId, updateOrder.order));
        return this;
    }
	
	private Behavior<Command> onGetOrder(GetOrder getOrder) {
        getOrder.replyTo.tell(orderService.getOrder(getOrder.orderId));
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
		        .onMessage(UpdateOrder.class, this::onUpdateOrder)
		        .onMessage(GetOrder.class, this::onGetOrder)
		        .build();
	}

}
