package com.app.xz.autohelper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class AutoService extends AccessibilityService {

    public static boolean strongMode = false;

    private static final String TAG = "testkkk";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //界面点击
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                //界面文字改动
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotification(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                String className = event.getClassName().toString();
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {//聊天和聊天列表等4个tab都是这个界面
                    //模拟点击红包
                    getPacket();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    //抢红包
                    openPacket();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                    //关闭红包
                    close();
                }

                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                if (strongMode) {
                    getPacket();
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        Log.e(TAG, "serviceConnected");
        super.onServiceConnected();
        initServiceInfo();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return super.onKeyEvent(event);
    }

    //监听指定package,检索窗口内容,获取事件类型的时间等等.
    private void initServiceInfo() {
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;//监听事件类型 这里是所有事件
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;//反馈方式 语音还是振动
        serviceInfo.packageNames = new String[]{"com.tencent.mm", "com.app.xz.onefish"};//监听的包
        serviceInfo.notificationTimeout = 100;//事件的时间间隔
//        canRetrieveWindowContent:表示该服务能否访问活动窗口中的内容.也就是如果你希望在服务中获取窗体内容的化,则需要设置其值为true.
        setServiceInfo(serviceInfo);
    }

    /**
     * 处理通知栏信息
     * <p>
     * 如果是微信红包的提示信息,则模拟点击
     *
     * @param event
     */
    private void handleNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                if (content.contains("[微信红包]")) {
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 关闭红包详情界面,实现自动返回聊天窗口
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void close() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            //为了演示,直接查看了关闭按钮的id
            List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ho");
            nodeInfo.recycle();
            for (AccessibilityNodeInfo item : infos) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 模拟点击,拆开红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            logId(nodeInfo);
            //为了演示,直接查看了红包控件的id
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c2i");
            nodeInfo.recycle();
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 模拟点击,打开抢红包界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getPacket() {
        //循环查找关键字
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        AccessibilityNodeInfo node = recycle(rootNode);

        //节点找到 所以node!=null
        if (node != null) {
            //找到关键字后 一级一级往上找 直到可执行点击事件
            if (node.isCheckable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            AccessibilityNodeInfo parent = node.getParent();
            while (parent != null) {
                if (parent.isClickable()) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
                parent = parent.getParent();
            }
        }

    }

    /**
     * 递归查找当前聊天窗口中的红包信息
     * <p>
     * 聊天窗口中的红包都存在"领取红包"一词,因此可根据该词查找红包
     *
     * @param node
     */
    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) {

        if (node == null){
            return null;
        }

        AccessibilityNodeInfo temp = null;

        if (node.getChildCount() == 0) {
            if (node.getText() != null) {
                if ("领取红包".equals(node.getText().toString())) {
                    return node;
                }
                //这里还获取不到列表里的字 所以不成功 以后改
//                else if (strongMode) {
//                    if (node.getText().toString() != null && node.getText().toString().contains("[微信红包]")) {
//                        return node;
//                    }
//                }
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    AccessibilityNodeInfo node_temp = recycle(node.getChild(i));
                    if (node_temp != null) {//返回null 说明不满足条件 不接收
                        temp = node_temp;
                    }
                }
            }
        }
        return temp;
    }

    /**
     * 测试代码
     *
     * @param info
     */
    private void logId(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            Log.e(TAG, info.getClassName() + "");
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                logId(info.getChild(i));
            }
        }
    }

    /**
     * 模拟点击
     * 这个方法可以指定点击点的坐标
     * 要求：线程中执行
     * 问题：该方法需要跨进程，然而权限限制了别的app不能这样跨进程点击
     * 解决：1.使用NDK的方式绕过权限检测 （复杂）
     * 2.签名时使用系统签名，以获取权限
     * 3.root
     * 都是和权限有关 暂时不好解决
     * 为什么要使用这个方法：
     * 小米手机在微信弹出LuckyMoneyReceiveUI界面时，应该是擅自添加了一个图层，导致界面识别有误；又或者是屏蔽了这个界面的UI获取；
     */
    private void simulateClick() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int width = MyApplication.screenWidth;
                int height = MyApplication.screenHeight;

                Instrumentation inst = new Instrumentation();
                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, width * 0.5f, height * 0.5f, 0));
                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, width * 0.5f, height * 0.5f, 0));

                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, width * 0.5f, height * 0.55f, 0));
                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, width * 0.5f, height * 0.55f, 0));

                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, width * 0.5f, height * 0.6f, 0));
                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, width * 0.5f, height * 0.6f, 0));

                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, width * 0.5f, height * 0.65f, 0));
                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, width * 0.5f, height * 0.65f, 0));

                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, width * 0.5f, height * 0.7f, 0));
                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, width * 0.5f, height * 0.7f, 0));

                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, width * 0.5f, height * 0.75f, 0));
                inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, width * 0.5f, height * 0.75f, 0));
            }
        }).start();

    }
}
