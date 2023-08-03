package shopping.order;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.ApplicationContext;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import shopping.order.repository.OrderRepository;
import shopping.order.repository.SpringIntegration;
import shopping.order.service.OrderService;
import shopping.order.service.OrderServiceImpl;

public class Main {

	private static final ObjectMapper objectmapper = JsonMapper.builder()
			.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
			.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
			.addModule(new JavaTimeModule())
			.build();

	public static void main(String[] args) throws Exception {
		// #server-bootstrapping

		Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {
			ApplicationContext springContext = SpringIntegration.applicationContext(context.getSystem());
			OrderRepository orderRepository = springContext.getBean(OrderRepository.class);
			OrderService orderService = new OrderServiceImpl(orderRepository);
			ActorRef<OrderActors.Command> actorRef = context.spawn(OrderActors.create(orderService), "OrderService");
			final Marshaller<Object, RequestEntity> marshaller = Jackson.marshaller(objectmapper);
			OrderServiceRoutes orderRoutes = new OrderServiceRoutes(context.getSystem(), actorRef, marshaller);

			OrderServiceHttpServer.startHTTPServer(orderRoutes.routes(), context.getSystem());
			return Behaviors.empty();
		});
		// boot up server using the route as defined below
		ActorSystem.create(rootBehavior, "OrderServiceHttpServer");
		// #server-bootstrapping
	}

}
