package com.codertea.nkcommunity.config;

import com.codertea.nkcommunity.NkcommunityApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NkcommunityApplication.class)
public class RedisConfigTest {
    @Autowired
    private RedisTemplate redisTemplate;

    // 利用RedisTemplate访问Redis，存取各种类型的数据。
    @Test
    public void redisTemplate() {
        System.out.println("=========================字符串===========================");
        String key = "test:count";
        redisTemplate.opsForValue().set(key, 1);
        System.out.println(redisTemplate.opsForValue().get(key));
        System.out.println(redisTemplate.opsForValue().increment(key));
        System.out.println(redisTemplate.opsForValue().decrement(key));
        System.out.println("=========================哈希===========================");
        key = "test:user";
        redisTemplate.opsForHash().put(key, "id", 207);
        redisTemplate.opsForHash().put(key, "username", "张三");
        System.out.println(redisTemplate.opsForHash().get(key, "id"));
        System.out.println(redisTemplate.opsForHash().get(key, "name"));
        System.out.println("=========================列表===========================");
        key = "test:ids";
        redisTemplate.opsForList().leftPush(key, 101);
        redisTemplate.opsForList().leftPush(key, 102);
        redisTemplate.opsForList().leftPush(key, 103);
        System.out.println(redisTemplate.opsForList().size(key));
        System.out.println(redisTemplate.opsForList().index(key, 0));
        System.out.println(redisTemplate.opsForList().range(key, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println("=========================集合===========================");
        key = "test:teachers";
        redisTemplate.opsForSet().add(key, "刘备", "关羽", "张飞");
        System.out.println(redisTemplate.opsForSet().size(key));
        System.out.println(redisTemplate.opsForSet().pop(key));
        System.out.println(redisTemplate.opsForSet().members(key));
        System.out.println("=========================有序集合===========================");
        key = "test:students";
        redisTemplate.opsForZSet().add(key, "唐僧", 80);
        redisTemplate.opsForZSet().add(key, "悟空", 90);
        redisTemplate.opsForZSet().add(key, "八戒", 50);
        redisTemplate.opsForZSet().add(key, "沙僧", 70);
        redisTemplate.opsForZSet().add(key, "白龙马", 60);
        System.out.println(redisTemplate.opsForZSet().zCard(key));
        System.out.println(redisTemplate.opsForZSet().score(key, "八戒"));
        System.out.println(redisTemplate.opsForZSet().range(key, 0, 2));
        System.out.println(redisTemplate.opsForZSet().reverseRange(key, 0, 2));

        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    // 多次访问同一个key，使用bound更方便
    @Test
    public void testBoundOperations() {
        String key = "test:count";
        BoundValueOperations boundValueOperations = redisTemplate.boundValueOps(key);
        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.increment();
        System.out.println(boundValueOperations.get());
    }

    // 编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                operations.multi();

                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");

                System.out.println(operations.opsForSet().members(redisKey));  // []

                return operations.exec();
            }
        });
        System.out.println(obj); // [1, 1, 1, [wangwu, lisi, zhangsan]] 三个1表示前三条命令都执行成功，[wangwu, lisi, zhangsan]是第四条查询命令的结果
    }
}