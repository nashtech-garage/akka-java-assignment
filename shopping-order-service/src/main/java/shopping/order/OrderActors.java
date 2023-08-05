package shopping.order;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;
import shopping.order.dto.OrderRequest;
import shopping.order.dto.OrderResponse;
import shopping.order.service.OrderService;


@Slf4j
public class OrderActors extends AbstractBehavior<OrderActors.Command> {

    public static final class CreateOrder implements Command {
        public final OrderRequest              orderRequest;
        public final ActorRef<ActionPerformed> replyTo;

        public CreateOrder (OrderRequest orderRequest, ActorRef<ActionPerformed> replyTo) {
            this.orderRequest = orderRequest;
            this.replyTo = replyTo;
        }
    }

    public static final class GetOrder implements Command {
        public final String                    orderId;
        public final ActorRef<ActionPerformed> replyTo;

        public GetOrder (String orderId, ActorRef<ActionPerformed> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }

    public static final class UpdateOrder implements Command {
        final        String                    orderId;
        final        OrderRequest              orderRequest;
        public final ActorRef<ActionPerformed> replyTo;

        public UpdateOrder (String orderId, OrderRequest orderRequest, ActorRef<ActionPerformed> replyTo) {
            this.orderId = orderId;
            this.orderRequest = orderRequest;
            this.replyTo = replyTo;
        }
    }

    public static final class ActionPerformed implements Command {
        public final OrderResponse orderResponse;

        public ActionPerformed (OrderResponse orderResponse) {
            this.orderResponse = orderResponse;
        }

    }

    private final OrderService orderService;

    public interface Command {
    }

    private OrderActors (ActorContext<Command> context, OrderService orderService) {
        super(context);
        this.orderService = orderService;
    }

    Behavior<Command> onCreateOrder (CreateOrder createOrder) {
        createOrder.replyTo.tell(new ActionPerformed(orderService.createOrder(createOrder.orderRequest)));
        return this;
    }

    public static Behavior<Command> create (OrderService orderService) {
        return Behaviors.setup(ctx -> new OrderActors(ctx, orderService));
    }

    Behavior<Command> onGetOrder (GetOrder getOrder) {
        getOrder.replyTo.tell(new ActionPerformed(orderService.getOrderById(getOrder.orderId)));
        return this;
    }

    Behavior<Command> onUpdateOrder (UpdateOrder updateOrder) {
        updateOrder.replyTo.tell(new ActionPerformed(orderService.updateOrder(updateOrder.orderId, updateOrder.orderRequest)));
        return this;
    }

    @Override
    public Receive<Command> createReceive () {
        return newReceiveBuilder()
            .onMessage(CreateOrder.class, this::onCreateOrder)
            .onMessage(GetOrder.class, this::onGetOrder)
            .onMessage(UpdateOrder.class, this::onUpdateOrder)
            .build();
    }

}
