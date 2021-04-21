package com.dds.core.model;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketEvent {
    ReadyData readyData;
    MakeCallData makeCallData;
    CallingInfo callingInfo;
    SubTrackData subTrackData;

    public void setReadyData(final ReadyData readyData) {
        this.readyData = readyData;
    }

    public void setMakeCallData(final MakeCallData makeCallData) {
        this.makeCallData = makeCallData;
    }

    public void setSubTrackData(final SubTrackData subTrackData) {
        this.subTrackData = subTrackData;
    }

    public void setCallingInfo(final CallingInfo callingInfo) {
        this.callingInfo = callingInfo;
    }

    public JSONObject toJSONOject() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (readyData != null) {
                jsonObject.put("roomId", readyData.room);
                jsonObject.put("fromUserId", readyData.userId);
                jsonObject.put("nickname", readyData.nickName);
                jsonObject.put("toUserId", readyData.toUserId);
            }

            if (makeCallData != null) {
                jsonObject.put("fromUserId", makeCallData.fromId);
                jsonObject.put("clientId", makeCallData.toGroupId);
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

    public static int TRACK_EVENT_TYPE_ADD = 1;
    public static int TRACK_EVENT_TYPE_REMOVE = 2;
    public static int TRACK_EVENT_TYPE_SUB = 3;
    public static int TRACK_EVENT_TYPE_UNSUB = 4;



    public static class TrackId {
        String id;
        String streamId;

        public TrackId(JSONObject json) {
            try {
                id = json.getString("id");
                streamId = json.getString("streamId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", id);
                jsonObject.put("streamId", streamId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonObject;
        }
    }

    public static class SubTrackData {
        int type;
        TrackId trackId;
        String pubCientId;

        public SubTrackData(final int type, final TrackId trackId, final String pubCientId) {
            this.type = type;
            this.trackId = trackId;
            this.pubCientId = pubCientId;
        }

        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("trackId", trackId.toJson());
                jsonObject.put("type", type);
                jsonObject.put("pubClientId", pubCientId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonObject;
        }    }

}
