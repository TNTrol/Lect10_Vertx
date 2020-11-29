import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args)
    {
        final var vertx = Vertx.vertx();

        final var factory = new User.Factory();
        vertx.registerVerticleFactory(factory);
        final DeploymentOptions optionsMember = new DeploymentOptions().setWorker(true).setInstances(20);
        vertx.deployVerticle(
                factory.prefix() + ':' + User.class.getName(),
                optionsMember,
                res -> System.out.println("Members deploy result: " + res.succeeded())
        );

        for(int i = 0; i < 3; i++) {
            vertx.deployVerticle(new Admin("clan" + i, i), new DeploymentOptions().setWorker(true));
            vertx.deployVerticle(new Moderator( i,"clan" + i), new DeploymentOptions().setWorker(true));
        }
        vertx.deployVerticle(new Moderator( 5,"clan" + 1), new DeploymentOptions().setWorker(true));
    }
}
