package shopping.order;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.springframework.context.ApplicationContext;
import shopping.order.repository.OrderRepository;
import shopping.order.repository.SpringIntegration;
import shopping.order.service.OrderService;
import shopping.order.service.OrderServiceImpl;

public class Main {

    public static void main(String[] args) {
        // #server-bootstrapping

        Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {
            ApplicationContext springContext = SpringIntegration.applicationContext(context.getSystem());
            OrderRepository orderRepository = springContext.getBean(OrderRepository.class);
            OrderService orderService = new OrderServiceImpl(orderRepository);
            ActorRef<OrderActors.Command> actorRef = context.spawn(OrderActors.create(orderService), "OrderService");
            OrderServiceRoutes orderRoutes = new OrderServiceRoutes(context.getSystem(), actorRef);

            OrderServiceHttpServer.startHTTPServer(orderRoutes.routes(), context.getSystem());
            return Behaviors.empty();
        });
        // boot up server using the route as defined below
        ActorSystem.create(rootBehavior, "OrderServiceHttpServer");

        // #server-bootstrapping
    }

}
