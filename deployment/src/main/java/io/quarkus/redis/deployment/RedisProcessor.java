package io.quarkus.redis.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.substrate.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.substrate.SubstrateProxyDefinitionBuildItem;
import io.quarkus.redis.runtime.RedisClientProducer;
import io.quarkus.redis.runtime.RedisClientTemplate;
import io.quarkus.redis.runtime.RedisConfiguration;
import io.quarkus.runtime.RuntimeValue;
import org.apache.commons.pool2.*;
import org.apache.commons.pool2.impl.BaseGenericObjectPool;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.EvictionPolicy;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedisPool;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

public final class RedisProcessor {
    private static final String[] INTERFACES_TO_REGISTER = {
            EvictionPolicy.class.getName(),
    };

    /**
     * Register a extension capability and feature
     *
     * @return test-extension feature build item
     */
    @BuildStep(providesCapabilities = "quarkus-redis")
    FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("redis");
    }

    @BuildStep
    AdditionalBeanBuildItem registerBean() {
        return AdditionalBeanBuildItem.unremovableOf(RedisClientProducer.class);
    }

    @BuildStep
    SubstrateProxyDefinitionBuildItem httpProxies() {
        return new SubstrateProxyDefinitionBuildItem(MBeanServer.class.getName(),
                MBeanServerConnection.class.getName(),
                KeyedObjectPool.class.getName(),
                KeyedPooledObjectFactory.class.getName(),
                ObjectPool.class.getName(),
                PooledObject.class.getName(),
                PooledObjectFactory.class.getName(),
                SwallowedExceptionListener.class.getName(),
                TrackedUse.class.getName(),
                UsageTracking.class.getName());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    RedisBuildItem build(RedisClientTemplate template, BeanContainerBuildItem beanContainer,
            ShutdownContextBuildItem shutdown, RedisConfiguration config, BuildProducer<ServiceStartBuildItem> serviceStart,
            CombinedIndexBuildItem indexBuildItem,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClassBuildItemBuildProducer) {

        //        for (String className : INTERFACES_TO_REGISTER) {
        //            System.out.println("============= " + className);
        //            for (ClassInfo i : indexBuildItem.getIndex().getAllKnownImplementors(DotName.createSimple(className))) {
        //                String name = i.name().toString();
        //                System.out.println("===================== " + name);
        //                reflectiveClassBuildItemBuildProducer.produce(new ReflectiveClassBuildItem(false, false, name));
        //            }
        //        }
        reflectiveClassBuildItemBuildProducer
                .produce(new ReflectiveClassBuildItem(false, false, BaseGenericObjectPool.class.getName()));
        reflectiveClassBuildItemBuildProducer
                .produce(new ReflectiveClassBuildItem(false, false, DefaultEvictionPolicy.class.getName()));
        RuntimeValue<ShardedJedisPool> shardedPool = null;
        RuntimeValue<JedisPool> pool = null;
        if (config.sharded.equals("true")) {
            shardedPool = template.configureShardedJedisPool(beanContainer.getValue(), config,
                    shutdown);
        } else {
            pool = template.configureJedisPool(beanContainer.getValue(), config, shutdown);
        }

        serviceStart.produce(new ServiceStartBuildItem("jedisPool"));
        return new RedisBuildItem(pool, shardedPool);
    }
}
