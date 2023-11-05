package com.codertea.nkcommunity.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 某个实体的赞，拼接它的rediskey为 like:entity:entityType:entityId 每个key对应存储一个set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}
