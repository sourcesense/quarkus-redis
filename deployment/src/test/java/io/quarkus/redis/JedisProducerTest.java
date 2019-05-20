/*
 * Copyright 2019 Sourcesense spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.quarkus.redis;

import io.quarkus.test.QuarkusUnitTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import redis.clients.jedis.Jedis;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class JedisProducerTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("application.properties")
                    .addClasses(BeanUsingJedisFromPool.class));

    @Inject
    BeanUsingJedisFromPool beanUsingJedisFromPool;

    @Test
    public void testFromPool() throws Exception {
        beanUsingJedisFromPool.verify();
    }

    @ApplicationScoped
    static class BeanUsingJedisFromPool {
        @Inject
        Instance<Jedis> provider;

        public void verify() throws Exception {
            Jedis jedis = provider.get();
            String val = "2";
            jedis.set("test", val);
            String res = jedis.get("test");
            Assertions.assertEquals(res, val);
            jedis.del("test");
            Assertions.assertNull(jedis.get("test"));
            jedis.close();
        }

    }

}
