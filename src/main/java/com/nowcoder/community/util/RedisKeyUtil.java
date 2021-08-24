package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";

    // 帖子和评论的赞(作为实体)
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";
    // 被关注者
    private static final String PREFIX_FOLLOWEE = "followee";
    // 关注者
    private static final String PREFIX_FOLLOWER = "follower";
    // 验证码
    private static final String PREFIX_CAPTCHA = "captcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLike(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体(根据关注时间的有序集合)
    // followee:userId:entityType -> zset(entityId,time)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId,time)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    public static String getCaptchaKey(String owner) {
        return PREFIX_CAPTCHA + SPLIT + owner;
    }

    // 登陆凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

}
