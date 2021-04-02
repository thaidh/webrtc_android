package com.dds.core.model;

import org.json.JSONObject;

public class CallingInfo {
    public String fromUserId;
    public String toUserId;
    public String roomId;

    public CallingInfo(final String fromUserId, final String toUserId, final String roomId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.roomId = roomId;
    }

    public CallingInfo(JSONObject jsonObject) {
        this.fromUserId = jsonObject.optString("fromUserId");
        this.toUserId = jsonObject.optString("toUserId");
        this.roomId = jsonObject.optString("roomId");
    }
}
