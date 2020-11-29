import io.vertx.core.AbstractVerticle;

public class Admin extends AbstractVerticle {
    private String _name;
    private String _clan;

    public Admin(String clan, int n)
    {
        _clan = clan;
        _name = "admin" + n;
    }

    public void start()
    {
        vertx.setPeriodic(10000, timer ->
                vertx.sharedData().getAsyncMap("clans", map ->
                        map.result().put(_clan, _clan, completion ->
                                System.out.println(_clan + " is alive")
                        ))
        );
    }
}
