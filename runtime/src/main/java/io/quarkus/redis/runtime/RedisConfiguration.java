package io.quarkus.redis.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public class RedisConfiguration {
    /**
     * COnnection uri
     */
    @ConfigItem(defaultValue = "redis://localhost:6379")
    public String uri;

    /**
     * If Sharded is enabled
     */
    @ConfigItem(defaultValue = "false")
    public String sharded;

    /**
     * Max number of connections in the pool
     */
    @ConfigItem(defaultValue = "15")
    public int maxTotal;

    /**
     * Max wait of connection pool
     */
    @ConfigItem(defaultValue = "5000")
    public long maxWaitMillis;
}
