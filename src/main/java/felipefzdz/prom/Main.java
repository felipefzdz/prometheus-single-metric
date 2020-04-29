package felipefzdz.prom;

import com.codahale.metrics.MetricRegistry;
import ratpack.dropwizard.metrics.DropwizardMetricsModule;
import ratpack.dropwizard.metrics.MetricsPrometheusHandler;
import ratpack.form.Form;
import ratpack.guice.Guice;
import ratpack.http.Status;
import ratpack.server.RatpackServer;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;

public class Main {
    public static AtomicInteger queueCount = new AtomicInteger(3);

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
            .registry(r -> Guice.registry(b -> {
                b.module(new DropwizardMetricsModule(), module -> module.prometheusCollection(true));
                final MetricRegistry metrics = new MetricRegistry();
                b.bindInstance(MetricRegistry.class, metrics);
                metrics.register("queueCount", new com.codahale.metrics.Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return queueCount.get();
                    }
                });

            }).apply(r))
            .handlers(chain -> chain
                .get("admin/metrics-report", new MetricsPrometheusHandler())
                .post("admin/queue-count", ctx -> ctx.parse(Form.class).then(f -> {
                    queueCount.set(parseInt(f.get("queueCount")));
                    ctx.getResponse().status(Status.OK).send();
                }))
            )
        );
    }
}
