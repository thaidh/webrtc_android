package com.dds.core.socket;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.autofill.AutofillId;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dds.core.consts.SocketConstants;
import com.dds.core.model.SocketEvent;
import com.dds.core.util.StringUtil;
import com.dds.skywebrtc.EnumType;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.X509TrustManager;

/**
 * Created by dds on 2019/7/26.
 * android_shuai@163.com
 */
public class MyWebSocket extends WebSocketClient {
    private final static String TAG = "dds_WebSocket";
    private final IEvent iEvent;
    private boolean connectFlag = false;


    public MyWebSocket(URI serverUri, IEvent event) {
        super(serverUri);
        this.iEvent = event;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e("dds_error", "onClose:" + reason + "remote:" + remote);
//        if (connectFlag) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.iEvent.reConnect();
//        } else {
//            this.iEvent.logout("onClose");
//        }

    }

    @Override
    public void onError(Exception ex) {
        Log.e("dds_error", "onError:" + ex.toString());
        this.iEvent.logout("onError");
        connectFlag = false;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.e("dds_info", "onOpen");
        this.iEvent.onOpen();
        connectFlag = true;
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, message);
        handleMessage(message);
    }


    public void setConnectFlag(boolean flag) {
        connectFlag = flag;
    }


    private void handleMessage(String message) {
        Map map = JSON.parseObject(message, Map.class);
        String type = (String) map.get("type");
        if (type == null) return;
        if (type.equals(SocketConstants.SOCKET_EVENT_RINGING)) {
            handleRing(map);
        } else if (type.equalsIgnoreCase(SocketConstants.SOCKET_EVENT_BUSY)) {
            handleCancel(map);
        } else if (type.equalsIgnoreCase(SocketConstants.SOCKET_EVENT_ACCEPT)) {
            handleAcceptCall(map);
        } else if (type.equalsIgnoreCase(SocketConstants.SOCKET_EVENT_REJECT)) {
                handleReject(map);
        } else if (type.equalsIgnoreCase(SocketConstants.SOCKET_EVENT_ENDCALL)) {
            handleCancel(map);
        } else if (type.equalsIgnoreCase(SocketConstants.SOCKET_EVENT_SIGNAL)) {
            JSONObject payload = (JSONObject) map.get("payload");
            JSONObject signal = payload.getJSONObject("signal");
            if (signal.containsKey("sdp")) {
                handleOffer(payload);
            } else if (signal.containsKey("candidate")) {
                handleIceCandidate(payload);
            }
        }
    }

    private void handleIceCandidate(Map map) {
        Map signal = (Map) map.get("signal");
        if (signal != null) {
            String userID = (String) map.get("userId");
            Map candidateMap = (Map) signal.get("candidate");
            String id = (String) candidateMap.get("sdpMid");
            int label = (int) candidateMap.get("sdpMLineIndex");
            String candidate = (String) candidateMap.get("candidate");
            this.iEvent.onIceCandidate(userID, id, label, candidate);
        }
    }

    private void handleOffer(Map map) {
        Map signal = (Map) map.get("signal");
        String sdp = (String) signal.get("sdp");
        String userID = (String) map.get("userId");
        this.iEvent.onOffer(userID, sdp);
    }

    private void handleReject(Map map) {
        Map data = (Map) map.get("payload");
        if (data != null) {
            String fromID = (String) data.get("fromUserId");
            int rejectType = 1 ; // remote hangup
            this.iEvent.onReject(fromID, rejectType);
        }
    }

    private void handleAcceptCall(Map map) {
        Map data = (Map) map.get("payload");
        if (data != null) {
            this.iEvent.onNewPeer("__SERVER__");
        }
    }

    public void peerReady(String myId, String toId, String roomId) {
        SocketEvent socketEvent = new SocketEvent();
        socketEvent.setReadyData(new SocketEvent.ReadyData(roomId, myId, "thaidh", toId));
        emitMessage(SocketConstants.SOCKET_EVENT_READY, socketEvent.toJSONOject());
    }

    private void handleRing(Map map) {
        Map data = (Map) map.get("payload");
        if (data != null) {
            String fromId = (String) data.get("fromUserId");
            String roomId = (String) data.get("roomId");
            this.iEvent.onRing(fromId, roomId);
        }
    }

    private void handleCancel(Map map) {
        Map data = (Map) map.get("payload");
        if (data != null) {
            String fromID = (String) data.get("fromUserId");
            this.iEvent.onCancel(fromID);
        }
    }

    /**
     * ------------------------------trueid----------------------------------------
     */
    public void makeCall(String groupId, String myId) {
        SocketEvent socketEvent = new SocketEvent();
        socketEvent.setMakeCallData(new SocketEvent.MakeCallData(myId, groupId));
        emitMessage(SocketConstants.SOCKET_EVENT_MAKECALL, socketEvent.toJSONOject());
    }

    public void emitMessage(String type, org.json.JSONObject message) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("type", type);
            obj.put("payload", message);
            String msgSend = obj.toString();
            Log.i(TAG, "emit message:" + msgSend);
            send(msgSend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createRoom(String room, int roomSize, String myId) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__create");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("roomSize", roomSize);
        childMap.put("userID", myId);

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 发送邀请
    public void sendInvite(String room, String myId, List<String> users, boolean audioOnly) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__invite");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("audioOnly", audioOnly);
        childMap.put("inviteID", myId);

        String join = StringUtil.listToString(users);
        childMap.put("userList", join);

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    public void sendEndCall(String mRoomId, String useId, String toUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "end");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("fromUserId", useId);
        childMap.put("toUserId", toUserId);
        childMap.put("room", mRoomId);

        map.put("payload", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 取消邀请
    public void sendCancel(String mRoomId, String useId, List<String> users) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "end");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("", useId);
        childMap.put("room", mRoomId);

        String join = StringUtil.listToString(users);
        childMap.put("userList", join);


        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 发送响铃通知
    public void sendRing(String myId, String toId, String room) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__ring");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("fromID", myId);
        childMap.put("toID", toId);
        childMap.put("room", room);


        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    //加入房间
    public void sendJoin(String room, String myId) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__join");

        Map<String, String> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("userID", myId);


        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 拒接接听
    public void sendRefuse(String room, String inviteID, String myId, int refuseType) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__reject");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("toID", inviteID);
        childMap.put("fromID", myId);
        childMap.put("refuseType", String.valueOf(refuseType));

        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 离开房间
    public void sendLeave(String myId, String room, String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "end");

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("fromUserId", myId);
        childMap.put("toUserId", userId);

        map.put("payload", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        if (isOpen()) {
            send(jsonString);
        }
    }

    // send offer
    public void sendOffer(String myId, String userId, String sdp) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("sdp", sdp);
        childMap.put("userID", userId);
        childMap.put("fromID", myId);
        map.put("data", childMap);
        map.put("eventName", "__offer");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // send answer
    public void sendAnswer(String myId, String userId, String sdp) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();

        Map<String, Object>  signalMap = new HashMap<>();
        signalMap.put("sdp", sdp);
        signalMap.put("type", "answer");

        childMap.put("fromUserId", myId);
        childMap.put("userId", userId);
        childMap.put("signal", signalMap);


        map.put("payload", childMap);
        map.put("type", SocketConstants.SOCKET_EVENT_SIGNAL);

        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // send ice-candidate
    public void sendIceCandidate(String myId, String userId, String id, int label, String candidate) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", SocketConstants.SOCKET_EVENT_SIGNAL);

        Map<String, Object> candidateMap = new HashMap<>();
        candidateMap.put("sdpMid", id);
        candidateMap.put("sdpMLineIndex", label);
        candidateMap.put("candidate", candidate);
        Map<String, Object>  signalMap = new HashMap<>();
        signalMap.put("candidate", candidateMap);



        Map<String, Object> childMap = new HashMap<>();
        childMap.put("userId", userId);
        childMap.put("fromUserId", myId);
        childMap.put("signal", signalMap);

        map.put("payload", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        if (isOpen()) {
            send(jsonString);
        }
    }

    // 切换到语音
    public void sendTransAudio(String myId, String userId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("fromID", myId);
        childMap.put("userID", userId);
        map.put("data", childMap);
        map.put("eventName", "__audio");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 断开重连
    public void sendDisconnect(String room, String myId, String userId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("fromID", myId);
        childMap.put("userID", userId);
        childMap.put("room", room);
        map.put("data", childMap);
        map.put("eventName", "__disconnect");
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    public void sendRenegotiate(String myId, String userId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> childMap = new HashMap<>();

        Map<String, Object>  signalMap = new HashMap<>();
        signalMap.put("renegotiate", true);

        childMap.put("fromUserId", myId);
        childMap.put("userId", userId);
        childMap.put("signal", signalMap);


        map.put("payload", childMap);
        map.put("type", SocketConstants.SOCKET_EVENT_SIGNAL);

        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        Log.d(TAG, "send-->" + jsonString);
        send(jsonString);
    }

    // 忽略证书
    public static class TrustManagerTest implements X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
