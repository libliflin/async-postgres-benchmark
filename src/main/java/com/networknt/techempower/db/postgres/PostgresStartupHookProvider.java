package com.networknt.techempower.db.postgres;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import com.networknt.config.Config;
import com.networknt.server.StartupHookProvider;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Created by steve on 10/02/17.
 */
public class PostgresStartupHookProvider implements StartupHookProvider {

    static String CONFIG_NAME = "postgres";
    public static HikariDataSource ds;
    public static Db db;

    @Override
    public void onStartup() {
        initDataSource();
    }

    static void initDataSource() {
        PostgresConfig config = (PostgresConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, PostgresConfig.class);


        db = new ConnectionPoolBuilder()
                .hostname("localhost")
                .port(5432)
                .database("postgres")
                .username(config.getUsername())
                .password(config.getPassword())
                .poolSize(config.getMaximumPoolSize())
                .build();



        ds = new HikariDataSource();
        ds.setJdbcUrl(config.getJdbcUrl());
        ds.setUsername(config.getUsername());
        ds.setPassword(config.getPassword());
        ds.setMaximumPoolSize(config.getMaximumPoolSize());


    }
}
