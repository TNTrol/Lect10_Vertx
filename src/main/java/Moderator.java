import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.concurrent.ThreadLocalRandom;

public class Moderator extends AbstractVerticle {

    private String _name;
    private String _clan;
    private int _max = 20;
    private MessageConsumer _ms;

    public Moderator(int n, String clan)
    {
        _name = "Moderator" + n;
        _clan = clan;
    }

    public void start()
    {
       createMS();
    }

    private void createMS()
    {
        _ms = vertx.eventBus().consumer(_clan, event -> {
                    if (ThreadLocalRandom.current().nextBoolean() ) {
                        vertx.sharedData().getCounter(_clan + "CountUser", counter -> {
                            if (counter.succeeded()) {
                                counter.result().get(event1 -> {
                                    if (event1.result() < _max) {
                                        event.reply("We accepted you");
                                        counter.result().incrementAndGet(number -> System.out.println(_name + " accepted " + event.body() + " in " + _clan + " (" + number.result()));
                                    }
                                }) ;
                            }
                        });
                    }
                }
        );
    }
}
