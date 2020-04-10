package felipefzdz.prom;

import com.codahale.metrics.MetricRegistry;
import ratpack.dropwizard.metrics.DropwizardMetricsModule;
import ratpack.dropwizard.metrics.MetricsPrometheusHandler;
import ratpack.guice.Guice;
import ratpack.server.RatpackServer;

import java.util.Optional;

public class Main {
    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
            .registry(r -> Guice.registry(b -> {
                b.module(new DropwizardMetricsModule(), module -> module.prometheusCollection(true));
                final MetricRegistry metrics = new MetricRegistry();
                b.bindInstance(MetricRegistry.class, metrics);
                metrics.register("queueCount", new com.codahale.metrics.Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return Integer.valueOf(Optional.ofNullable(System.getenv("QUEUE_COUNT")).orElse("3"));
                    }
                });

            }).apply(r))
            .handlers(chain -> chain
                .get("admin/metrics-report", new MetricsPrometheusHandler())
            )
        );

    }
}
