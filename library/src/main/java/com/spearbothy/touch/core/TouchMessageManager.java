package com.spearbothy.touch.core;

import android.os.Handler;
import android.view.MotionEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mahao
 * @date 2018/11/13 下午2:32
 */

class TouchMessageManager {

    private static final String ACTION_DOWN = "ACTION_DOWN";
    private static final String ACTION_UP = "ACTION_UP";
    private static final long CLEAR_DELAY = 1000L;

    private List<Message> messagesList = new ArrayList<>();

    private Clear clear = new Clear();
    private Handler handler = new Handler();

    private Print mConsolePrint = new ConsolePrint();
    private Print mFilePrint = new FilePrint();

    private static TouchMessageManager sInstance;

    private TouchMessageManager() {}

    public static TouchMessageManager getInstance() {
        if (sInstance == null) {
            sInstance = new TouchMessageManager();
        }
        return sInstance;
    }

    void printBefore(Object proxy, Method method, Object[] args) {
        Message message = buildMessage(proxy, method, args);
        message.setBefore(true);
        addMessage(message);
    }

    void printAfter(Object proxy, Method method, Object[] args, Object result) {
        Message message = buildMessage(proxy, method, args);
        message.setBefore(false);
        message.setResult((Boolean) result);
        addMessage(message);
    }

    private void addMessage(Message message) {
        messagesList.add(message);
        mConsolePrint.printMessage(message);
        handler.removeCallbacks(clear);
        handler.postDelayed(clear, CLEAR_DELAY);
    }

    private static Message buildMessage(Object proxy, Method method, Object[] args) {
        String className = proxy.getClass().getSuperclass().getSimpleName();
        String methodName = method.getName();
        String eventStr = "";
        Object arg = args[0];
        if (arg instanceof MotionEvent) {
            MotionEvent event = (MotionEvent) arg;
            eventStr = MotionEvent.actionToString(event.getAction());
        }
        return new Message(className, methodName, eventStr);
    }

    private void clearMessage() {
        if (messagesList.isEmpty()) {
            return;
        }
        List<Message> writeMessageList = new ArrayList<>(messagesList);

        mFilePrint.printMultipleMessage(writeMessageList);

        messagesList.clear();
    }

    private class Clear implements Runnable {

        @Override
        public void run() {
            clearMessage();
        }
    }
}