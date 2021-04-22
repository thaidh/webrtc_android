package com.dds.skywebrtc;

import android.content.Context;
import android.util.Log;

import com.dds.skywebrtc.except.NotInitializedException;
import com.dds.skywebrtc.inter.ISkyEvent;

/**
 * 主控类
 * Created by dds on 2019/8/19.
 */
public class SkyEngineKit {
    private final static String TAG = "dds_AVEngineKit";
    private static SkyEngineKit avEngineKit;
    private CallSession mCurrentCallSession;
    private ISkyEvent mEvent;


    public static SkyEngineKit Instance() {
        SkyEngineKit var;
        if ((var = avEngineKit) != null) {
            return var;
        } else {
            throw new NotInitializedException();
        }
    }

    // 初始化
    public static void init(ISkyEvent iSocketEvent) {
        if (avEngineKit == null) {
            avEngineKit = new SkyEngineKit();
            avEngineKit.mEvent = iSocketEvent;
        }
    }


    public void sendRefuseOnPermissionDenied(String room, String inviteId) {
        // 未初始化
        if (avEngineKit == null) {
            Log.e(TAG, "startOutCall error,please init first");
            return;
        }
        if (mCurrentCallSession != null) {
            endCall();
        } else {
            avEngineKit.mEvent.sendRefuse(room, inviteId, EnumType.RefuseType.Hangup.ordinal());
        }
    }

    public void sendDisconnected(String room, String toId, boolean isCrashed) {
        // 未初始化
        if (avEngineKit == null) {
            Log.e(TAG, "startOutCall error,please init first");
            return;
        }
        avEngineKit.mEvent.sendDisConnect(room, toId, isCrashed);
    }

    // 拨打电话
    public boolean startOutCall(Context context, final String room, final String targetId,
                                final boolean audioOnly) {
        // 未初始化
        if (avEngineKit == null) {
            Log.e(TAG, "startOutCall error,please init first");
            return false;
        }
        // 忙线中
        if (mCurrentCallSession != null && mCurrentCallSession.getState() != EnumType.CallState.Idle) {
            Log.i(TAG, "startCall error,currentCallSession is exist");
            return false;
        }
        // 初始化会话
        mCurrentCallSession = new CallSession(context, room, audioOnly, mEvent);
        mCurrentCallSession.setTargetId(targetId);
        mCurrentCallSession.setIsComing(false);
        mCurrentCallSession.setCallState(EnumType.CallState.Outgoing);
        // 创建房间
        mCurrentCallSession.makeCall("ACB");
//        mCurrentCallSession.makeCall("client_id_3");


        return true;
    }

    // 接听电话
    public boolean startInCall(Context context, final String room, final String targetId,
                               final boolean audioOnly) {
        if (avEngineKit == null) {
            Log.e(TAG, "startInCall error,init is not set");
            return false;
        }
        // 忙线中
        if (mCurrentCallSession != null && mCurrentCallSession.getState() != EnumType.CallState.Idle) {
            // 发送->忙线中...
            Log.i(TAG, "startInCall busy,currentCallSession is exist,start sendBusyRefuse!");
            mCurrentCallSession.sendBusyRefuse(room, targetId);
            return false;
        }
        // 初始化会话
        mCurrentCallSession = new CallSession(context, room, audioOnly, mEvent);
        mCurrentCallSession.setTargetId(targetId);
        mCurrentCallSession.setIsComing(true);
        mCurrentCallSession.setCallState(EnumType.CallState.Incoming);

        // 开始响铃并回复
        mCurrentCallSession.shouldStartRing();
        mCurrentCallSession.sendRingBack(targetId, room);


        return true;
    }

    // 挂断会话
    public void endCall() {
        Log.d(TAG, "endCall mCurrentCallSession != null is " + (mCurrentCallSession != null));
        if (mCurrentCallSession != null) {
            // 停止响铃
            mCurrentCallSession.shouldStopRing();

//            if (mCurrentCallSession.isComing()) {
//                if (mCurrentCallSession.getState() == EnumType.CallState.Incoming) {
//                    // 接收到邀请，还没同意，发送拒绝
//                    mCurrentCallSession.sendRefuse();
//                } else {
//                    // 已经接通，挂断电话
//                    mCurrentCallSession.leave();
//                }
//            } else {
//                if (mCurrentCallSession.getState() == EnumType.CallState.Outgoing) {
//                    mCurrentCallSession.sendCancel();
//                } else {
//                    // 已经接通，挂断电话
//                    mCurrentCallSession.leave();
//                }
//            }
            mCurrentCallSession.leave();
            mCurrentCallSession.setCallState(EnumType.CallState.Idle);
        }

    }

    // 获取对话实例
    public CallSession getCurrentSession() {
        return this.mCurrentCallSession;
    }


}
