package com.networknt.techempower.handler;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.github.pgasync.Db;
import com.github.pgasync.Row;
import com.networknt.techempower.Helper;
import com.networknt.techempower.db.postgres.PostgresStartupHookProvider;
import com.networknt.techempower.model.World;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.SameThreadExecutor;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import static com.networknt.techempower.Helper.randomWorld;

public class QueriesAsyncPostgresqlGetHandler implements HttpHandler {
    private final Db db = PostgresStartupHookProvider.db;
    private static DslJson<Object> dsl = new DslJson<>();
    private static JsonWriter writer = dsl.newWriter(1024);

    private static class ResultHandler implements HttpHandler {
        private final CountDownLatch cdl;
        private final World[] worlds;
        public ResultHandler(CountDownLatch cdl, World[] worlds){
            this.cdl = cdl;
            this.worlds = worlds;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            if(cdl.getCount() != 0){
                exchange.dispatch(this);
                return;
            }


            writer.reset();
            writer.serialize(worlds);

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(ByteBuffer.wrap(writer.toByteArray()));

        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        int queries = Helper.getQueries(exchange);

        World[] worlds = new World[queries];
        CountDownLatch cdl = new CountDownLatch(queries);
        for (int i = 0; i < queries; i++) {
            int worldIndex = i;
            int randomWorld = randomWorld();
            db.querySet("SELECT * FROM World WHERE id = " + randomWorld)
                    .subscribe((resultSetContainer) -> {
                        Row resultSet = resultSetContainer.row(0);
                        int id = resultSet.getInt("id");
                        int randomNumber = resultSet.getInt("randomNumber");
                        worlds[worldIndex] = new World(id, randomNumber);
                        cdl.countDown();
                    }, System.err::println);
        }

        exchange.dispatch(new ResultHandler(cdl, worlds));
    }
}
