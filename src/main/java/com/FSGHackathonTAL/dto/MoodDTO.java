package com.FSGHackathonTAL.dto;

import java.util.Date;

/**
 * DTO cho đối tượng Mood, tránh vấn đề serialization vòng lặp.
 */
public class MoodDTO {
    private int entryId;
    private String mood;
    private String reason;
    private Date createdAt;
    private Integer userId;
    private String userName;

    public MoodDTO() {
    }

    public MoodDTO(int entryId, String mood, String reason, Date createdAt, Integer userId, String userName) {
        this.entryId = entryId;
        this.mood = mood;
        this.reason = reason;
        this.createdAt = createdAt;
        this.userId = userId;
        this.userName = userName;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
} 