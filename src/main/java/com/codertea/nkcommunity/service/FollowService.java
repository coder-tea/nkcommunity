package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;
import java.util.List;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    // 关注
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    // 取消关注
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    // 查询某个用户关注的某一类实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询某个实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询某个用户是否已经关注某一个实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某个用户的关注列表list，是一系列map的列表，每个map包含followeeUser和followeeTime
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> followeeIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if(followeeIds==null) return null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer followeeId : followeeIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(followeeId);
            map.put("followeeUser", user);
            Double haomiao = redisTemplate.opsForZSet().score(followeeKey, followeeId);
            map.put("followeeTime", new Date(haomiao.longValue()));
            list.add(map);
        }
        return list;
    }

    // 查询某个用户的粉丝列表list，是一系列map的列表，每个map包含followerUser和followerTime
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if(followerIds==null) return null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer followerId : followerIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(followerId);
            map.put("followerUser", user);
            Double haomiao = redisTemplate.opsForZSet().score(followerKey, followerId);
            map.put("followerTime", new Date(haomiao.longValue()));
            list.add(map);
        }
        return list;
    }
}
