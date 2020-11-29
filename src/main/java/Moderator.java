import io.vertx.core.AbstractVerticle;

import java.util.concurrent.ThreadLocalRandom;

public class Moderator extends AbstractVerticle {

    private String _name;
    private String _clan;

    public Moderator(int n, String clan)
    {
        _name = "moderator" + n;
        _clan = clan;
    }

    public void start()
    {
        vertx.eventBus().consumer(_clan, event -> {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        event.reply("We accepted you");
                        System.out.println(event.body());
                    }
                }
        );
    }
}
