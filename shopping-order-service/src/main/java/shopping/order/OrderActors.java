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
import shopping.order.service.OrderService;
import shopping.order.service.OrderServiceImpl;

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

    public static final class GetOrderById implements Command {
        public final String orderId;
        public final ActorRef<ActionPerformed> replyTo;

        public GetOrderById(String orderId, ActorRef<ActionPerformed> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }

    public static final class UpdateOrder implements Command {
        public final String orderId;
        public final OrderRequest orderRequest;
        public final ActorRef<ActionPerformed> replyTo;

        public UpdateOrder(String id, OrderRequest orderRequest, ActorRef<ActionPerformed> replyTo) {
            this.orderId = id;
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

    public static Behavior<Command> create(OrderService orderService) {
        return Behaviors.setup(ctx -> {
            return new OrderActors(ctx, orderService);
        });
    }

    private Behavior<Command> onGetOrderById(GetOrderById getOrderById) {
        getOrderById.replyTo.tell(new ActionPerformed(orderService.getOrderById(getOrderById.orderId)));
        return this;
    }

    public static Behavior<Command> getOrderById(OrderService orderService) {
        return Behaviors.setup(ctx -> {
            return new OrderActors(ctx, orderService);
        });
    }

    private Behavior<Command> onUpdateOrder(UpdateOrder updateOrder) {
        updateOrder.replyTo.tell(new ActionPerformed(orderService.updateOrder(updateOrder.orderId, updateOrder.orderRequest)));
        return this;
    }

    public static Behavior<Command> updateOrder(OrderService orderService) {
        return Behaviors.setup(ctx -> {
            return new OrderActors(ctx, orderService);
        });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CreateOrder.class, this::onCreateOrder)
                .onMessage(GetOrderById.class, this::onGetOrderById)
                .onMessage(UpdateOrder.class, this::onUpdateOrder)
                .build();
    }

}
