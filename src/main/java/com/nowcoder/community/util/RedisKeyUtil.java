package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";

    // 帖子和评论的赞(作为实体)
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLike(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

}