import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.JavaVerticleFactory;

import java.util.concurrent.ThreadLocalRandom;

public class User extends AbstractVerticle {
    private String _name;
    private String _clan;
    private long _timerId;
    private long _timeIdAlive;
    private MessageConsumer _ms;
    private String _tempString;

    public User(int n)
    {
        _name = "User" + n;

    }

    @Override
    public void start() {
        vertx.eventBus().consumer(_name, event -> {
                        event.reply("Thanks!");
                        //System.out.println(event.body());
                }
        );

        setPeriodicSendMessageToClan();
    }

    private void setPeriodicSendMessageToClan()
    {
        _timerId = vertx.setPeriodic(5000, timer ->
                {

                    if(_clan == null)
                        vertx.<String>executeBlocking(promise -> promise.complete(
                                listenerClans("clans")), res ->sendToClan(res.result()
                        ));
                }
        );
    }

    private void createMS()
    {
        _ms = vertx.eventBus().consumer(_clan + "Exit", event -> {
            setPeriodicSendMessageToClan();
            _ms.pause();
        });
    }

    private void setPeriodicSendMessageToUsers()
    {
        _timerId = vertx.setPeriodic(5000, timer ->
                {
                    sayAlive();
                    vertx.<String>executeBlocking(promise -> promise.complete(listenerClans(_clan + "Friends")), res ->sendMessage(res.result()
                    ));
                }
        );
    }

    public String listenerClans(String mapName){
        vertx.sharedData().getAsyncMap(mapName, map ->
                map.result().entries(item -> {
                    item.result().forEach((name, nameClan) ->{
                                if (ThreadLocalRandom.current().nextBoolean()){

                                    _tempString = (String) name;
                                    if(_tempString != null)
                                        return;
                                }
                            }
                    );
                })
        );
        return _tempString;
    }

    private boolean sendToClan(String clan)
    {
        if(clan == null )
            return false;
        final DeliveryOptions options = new DeliveryOptions().setSendTimeout(1000);
        System.out.println(_name + " want to "+ clan);
        vertx.eventBus().request(clan, _name, options, reply -> {
            if (reply.succeeded()) {
                _clan = clan;
                vertx.cancelTimer(_timerId);
                setPeriodicSendMessageToUsers();
                createMS();
            }
        });
        return _clan == null ? false : true;
    }

    private boolean sendMessage(String to){
        if(to == null || to.equals(_name))
            return false;
        final String[] res = new String[1];
        final DeliveryOptions options = new DeliveryOptions().setSendTimeout(1000);
        vertx.eventBus().request(to,  _name , options, reply -> {
            if (reply.succeeded()) {
                res[0] = to;
            }
        });
        return res[0] == null ? false : true;
    }

    private void sayAlive(){
        vertx.sharedData().getAsyncMap(_clan + "Friends", map ->
                map.result().put(_name, _name, completion ->
                        {}
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
