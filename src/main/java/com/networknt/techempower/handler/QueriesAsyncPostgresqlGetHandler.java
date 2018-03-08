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
import rx.Observable;


import java.nio.ByteBuffer;

import static com.networknt.techempower.Helper.randomWorld;

public class QueriesAsyncPostgresqlGetHandler implements HttpHandler {
    private final Db db = PostgresStartupHookProvider.db;
    private DslJson<Object> dsl = new DslJson<>();
    private JsonWriter writer = dsl.newWriter(1024);


    @Override
    public void handleRequest(HttpServerExchange exchange) {

        exchange.dispatch();
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        Observable.range(0, Helper.getQueries(exchange))
                .flatMap(i -> db.querySet("SELECT * FROM World WHERE id = $1", randomWorld()))
                .map(resultSet -> {
                    Row row = resultSet.row(0);
                    int id = row.getInt("id");
                    int randomNumber = row.getInt("randomNumber");
                    return new World(id, randomNumber);
                })
                .toList()
                .subscribe(worlds -> {
                    System.out.println("Before control reaches here.");
                    writer.reset();
                    writer.serialize(worlds);
                    exchange.getResponseSender().send(ByteBuffer.wrap(writer.toByteArray()));
                }, System.err::println);
        System.out.println("We return from here");
    }
}
