package com.codertea.nkcommunity.entity;

import java.util.HashMap;

public class Event {
    private String topic;
    // 谁userId触发了事件，如userId给帖子进行点赞了
    private int userId;
    // 对entityType类型的entityId进行触发，如某一个帖子
    private int entityType;
    private int entityId;
    // 这个entityType类型的entityId的作者是谁,如被点赞的帖子的作者是谁
    private int entityUserId;
    // 其他数据
    private HashMap<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    // 返回this，方便链式编程
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
