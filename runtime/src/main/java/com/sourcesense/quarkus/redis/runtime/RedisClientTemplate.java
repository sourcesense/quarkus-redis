package com.sourcesense.quarkus.redis.runtime;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Template;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

@Template
public class RedisClientTemplate {

    public RuntimeValue<JedisPool> configureJedisPool(BeanContainer container, RedisConfiguration config,
            ShutdownContext shutdown) {
        RedisClientProducer producer = container.instance(RedisClientProducer.class);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxWaitMillis(config.maxWaitMillis);
        poolConfig.setMaxTotal(config.maxTotal);
        poolConfig.setJmxEnabled(false);

        URI uri = URI.create(config.uri);
        JedisPool pool = new JedisPool(poolConfig, uri);
        producer.setJedisPool(pool);

        shutdown.addShutdownTask(() -> pool.close());

        return new RuntimeValue<>(pool);
    }

    public RuntimeValue<ShardedJedisPool> configureShardedJedisPool(BeanContainer container, RedisConfiguration config,
            ShutdownContext shutdown) {
        RedisClientProducer producer = container.instance(RedisClientProducer.class);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxWaitMillis(config.maxWaitMillis);
        poolConfig.setMaxTotal(config.maxTotal);
        poolConfig.setJmxEnabled(false);

        List<JedisShardInfo> shards = Arrays.stream(config.uri.split(",")).map(s -> {
            URI uri = URI.create(s);
            return new JedisShardInfo(uri);
        }).collect(Collectors.toList());

        ShardedJedisPool pool = new ShardedJedisPool(poolConfig, shards);
        producer.setShardedJedisPool(pool);

        shutdown.addShutdownTask(() -> pool.close());

        return new RuntimeValue<>(pool);

    }
}
