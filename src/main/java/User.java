import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.impl.JavaVerticleFactory;

import java.util.concurrent.ThreadLocalRandom;

public class User extends AbstractVerticle {
    private String _name;
    private String _clan;

    private User(int n)
    {
        _name = "user" + n;
    }

    @Override
    public void start() {

        vertx.eventBus().consumer(_name, event -> {
                        event.reply("Thanks!");
                        System.out.println("Thanks");
                }
        );

        long t = vertx.setPeriodic(5000, timer ->
                {
                    vertx.executeBlocking(event -> {if(_clan == null) {
                        listenerClans();
                    }
                    else {
                        sayAlive();
                        listenerUsers();
                    }}, event -> System.out.println(_name + "Use BLOKED CODe"));

                }
        );
    }

    private boolean sendToClan(String clan)
    {
        final DeliveryOptions options = new DeliveryOptions().setSendTimeout(1000);
        vertx.eventBus().request(clan, _name, options, reply -> {
            if (reply.succeeded()) {
                _clan = clan;
                System.out.println(_name + " in " + _clan);
            }
        });
        return _clan == null ? false : true;
    }

    private void listenerClans(){
        vertx.sharedData().getAsyncMap("clans", map ->
                map.result().entries(cookies -> {
                    cookies.result().forEach((name, nameClan) ->{
                                if (ThreadLocalRandom.current().nextBoolean() && _clan == null){
                                    String clan = (String) name;
                                    sendToClan(clan);
                                }
                            }
                    );
                })
        );
    }

    private boolean sendMessage(String to){
        final String[] res = new String[1];
        final DeliveryOptions options = new DeliveryOptions().setSendTimeout(1000);
        vertx.eventBus().request(to, "Smth message", options, reply -> {
            if (reply.succeeded()) {
                res[0] = to;
            }
        });
        return res[0] == null ? false : true;
    }

    private void listenerUsers()
    {
        vertx.sharedData().getAsyncMap(_clan + "Friends", map ->
                map.result().entries(cookies -> {
                    cookies.result().forEach((name, nameClan) ->{
                                if (ThreadLocalRandom.current().nextBoolean() && !_name.equals(name)){
                                    sendMessage((String) name);
                                    return;
                                }
                            }
                    );
                })
        );
    }

    private void sayAlive(){
        vertx.sharedData().getAsyncMap(_clan + "Friends", map ->
                map.result().put(_name, _name, completion ->
                        System.out.println("")
                ));
    }

    public static final class Factory extends JavaVerticleFactory {
        private int number;

        @Override
        public String prefix() {
            return "sphere";
        }

        @SuppressWarnings("ProhibitedExceptionDeclared")
        @Override
        public Verticle createVerticle(String verticleName, ClassLoader classLoader) {
            return new User(number++);
        }
    }
}
