package fsstat;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.nio.file.Path;

public class FSStatLib {

    private final Vertx vertx;

    public FSStatLib(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Asynchronous computation of filesystem statistics.
     *
     * @param dir   root directory
     * @param maxFS max file size for banding
     * @param nb    number of bands
     * @return Future completed with FSReport
     */
    public Future<FSReport> getFSReport(Path dir, long maxFS, int nb) {
        Promise<FSReport> promise = Promise.promise();

        vertx.executeBlocking(promiseBlocking -> {
            try {
                FSReport report = FileScanner.scan(dir, maxFS, nb);
                promiseBlocking.complete(report);
            } catch (Exception e) {
                promiseBlocking.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                promise.complete((FSReport) res.result());
            } else {
                promise.fail(res.cause());
            }
        });

        return promise.future();
    }
}
