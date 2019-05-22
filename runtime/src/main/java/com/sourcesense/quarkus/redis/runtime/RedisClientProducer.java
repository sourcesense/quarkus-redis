package com.sourcesense.quarkus.redis.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

@ApplicationScoped
public class RedisClientProducer {

    private volatile JedisPool jedisPool;
    private volatile ShardedJedisPool shardedJedisPool;
    private volatile RedisConfiguration config;

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }

    @Dependent
    @Produces
    public Jedis get() {
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

    public void destroy(@Disposes Jedis jedis) {
        jedis.close();
    }

    @Dependent
    @Produces
    public ShardedJedis getSharded() {
        ShardedJedis jedis = shardedJedisPool.getResource();
        return jedis;
    }

    public void destroy(@Disposes ShardedJedis jedis) {
        jedis.close();
    }

}
