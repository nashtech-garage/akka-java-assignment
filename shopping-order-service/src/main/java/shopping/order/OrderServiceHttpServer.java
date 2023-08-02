/**
 *
 */
package shopping.order;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.typesafe.config.Config;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

/**
 * @author loinguyenx
 *
 */
public class OrderServiceHttpServer {

    public static final String HOST_KEY = "akka.http.host";
    public static final String PORT_KEY = "akka.http.port";

    static void startHTTPServer(Route route, ActorSystem<?> system) {
        Config config = system.settings().config();
        String host = config.getString(HOST_KEY);
        int port = config.getInt(PORT_KEY);
        CompletionStage<ServerBinding> futureBinding = Http.get(system).newServerAt(host, port).bind(route);

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                system.log().info("Server online at http://{}:{}/", address.getHostString(), address.getPort());
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }


}
