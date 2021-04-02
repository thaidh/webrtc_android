package com.dds.core.model;

import org.json.JSONObject;

public class SocketEvent {
    ReadyData readyData;
    MakeCallData makeCallData;
    CallingInfo callingInfo;

    public void setReadyData(final ReadyData readyData) {
        this.readyData = readyData;
    }

    public void setMakeCallData(final MakeCallData makeCallData) {
        this.makeCallData = makeCallData;
    }

    public void setCallingInfo(final CallingInfo callingInfo) {
        this.callingInfo = callingInfo;
    }

    public JSONObject toJSONOject() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (readyData != null) {
                jsonObject.put("room", readyData.room);
                jsonObject.put("userId", readyData.userId);
                jsonObject.put("nickname", readyData.nickName);
                jsonObject.put("toUserId", readyData.toUserId);
            }

            if (makeCallData != null) {
                jsonObject.put("fromUserId", makeCallData.fromId);
                jsonObject.put("groupId", makeCallData.toGroupId);
            }

            if (callingInfo != null) {
                jsonObject.put("fromUserId", callingInfo.fromUserId);
                jsonObject.put("toUserId", callingInfo.toUserId);
                jsonObject.put("roomId", callingInfo.roomId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static class ReadyData {
        String room;
        String userId;
        String nickName;
        String toUserId;

        public ReadyData(final String room, final String userId, final String nickName, String toUserId) {
            this.room = room;
            this.userId = userId;
            this.nickName = nickName;
            this.toUserId = toUserId;
        }
    }

    public static class MakeCallData {
        String fromId;
        String toGroupId;

        public MakeCallData(final String fromId, final String groupId) {
            this.fromId = fromId;
            this.toGroupId = groupId;
        }
    }

}
