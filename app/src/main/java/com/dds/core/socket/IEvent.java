package com.dds.core.socket;

/**
 * Created by dds on 2019/7/26.
 * ddssingsong@163.com
 */
public interface IEvent {


    void onOpen();

    void loginSuccess(String userId, String avatar);

    void onCancel(String inviteId);

    void onRing(String userId, String roomId);

    void onNewPeer(String myId);

    void onReject(String userId, int type);

    // onOffer
    void onOffer(String userId, String sdp);

    // onAnswer
    void onAnswer(String userId, String sdp);

    // ice-candidate
    void onIceCandidate(String userId, String id, int label, String candidate);

    void logout(String str);

    void reConnect();

}
