import io.vertx.core.AbstractVerticle;

import java.util.concurrent.ThreadLocalRandom;

public class Admin extends AbstractVerticle {
    private String _name;
    private String _clan;
    private int _maxCountModerator;
    private int _maxCountUsers;

    public Admin(String clan, int n, int maxCountModerator, int maxCountUsers)
    {
        _clan = clan;
        _name = "Admin" + n;
        _maxCountModerator = maxCountModerator;
        _maxCountUsers = maxCountUsers;
    }

    public void start()
    {
        vertx.setPeriodic(10000, timer ->
                vertx.sharedData().getAsyncMap("clans", map ->
                        map.result().put(_clan, _clan, completion ->{
                            checkCountMembers();
                            System.out.println(_clan + " is alive");
                                }
                        ))
        );
        vertx.eventBus().consumer(_clan + "Moderators", event -> {
            if (ThreadLocalRandom.current().nextBoolean())
            {
                vertx.sharedData().getCounter(_clan + "Moderators", counter -> {
                    if(counter.succeeded())
                    {
                        counter.result().get(number ->{
                            if(number.result() < _maxCountModerator)
                            {
                                event.reply("You are our moderator");
                                counter.result().getAndIncrement(number2 ->{});
                            }
                        });
                    }
                });
            }
        });

    }

    public void sendMessage()
    {
        vertx.eventBus().publish(_clan + "Information", null);
    }

    private void checkCountMembers()
    {
//        boolean[] check = new boolean[1];
//        vertx.sharedData().getCounter(_clan + "Moderators", counter -> {
//            if(counter.succeeded())
//            {
//                counter.result().get(number ->{
//                    if(number.result() > _maxCountModerator)
//                    {
//                        check[0] = true;
//                        sendMessage();
//                    }
//                });
//            }
//        });
//        if(check[0])
//            return;
        vertx.sharedData().getCounter(_clan + "CountUser", counter -> {
            if(counter.succeeded())
            {
                counter.result().get(number ->{
                    if(number.result() > _maxCountUsers)
                    {
                        sendMessage();
                    }
                });
            }
        });
    }
}
