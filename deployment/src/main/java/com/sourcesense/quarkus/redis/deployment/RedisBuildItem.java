package com.sourcesense.quarkus.redis.deployment;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.runtime.RuntimeValue;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedisPool;

public final class RedisBuildItem extends SimpleBuildItem {
    private final RuntimeValue<JedisPool> pool;
    private final RuntimeValue<ShardedJedisPool> shardedPool;

    public RedisBuildItem(RuntimeValue<JedisPool> pool, RuntimeValue<ShardedJedisPool> shardedPool) {
        this.pool = pool;
        this.shardedPool = shardedPool;
    }

    public RuntimeValue<JedisPool> getJedisPool() {
        return pool;
    }

    public RuntimeValue<ShardedJedisPool> getShardedPool() {
        return shardedPool;
    }
}
