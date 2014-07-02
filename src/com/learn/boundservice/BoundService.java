package com.learn.boundservice;

import java.lang.reflect.Constructor;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class BoundService extends Service {

	public static final int COMMAND_STOP_SERVICE_REQUEST = 0xf0000000;
	public static final int COMMAND_STOP_SERVICE_RESPOND = 0xffffffff;

	public static final int COMMAND_RUN_REQUEST = 0x00000001;
	public static final int COMMAND_RUN_RESPOND = 0xf0000001;

	public static final int COMMAND_PAUSE_REQUEST = 0x00000002;
	public static final int COMMAND_PAUSE_RESPOND = 0xf0000002;

	public static final int COMMAND_RESET_REQUEST = 0x00000003;
	public static final int COMMAND_RESET_RESPOND = 0xf0000003;

	protected class RecieveHandler extends Handler {

		public RecieveHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// handle all request here -- but do NOT DO WORK
			if (msg.what == COMMAND_STOP_SERVICE_REQUEST) {
				replyMsg(COMMAND_RUN_RESPOND, stopBoundService(), null, null,
						msg.replyTo);
			} else if (msg.what == COMMAND_RUN_REQUEST) {
				replyMsg(COMMAND_RUN_RESPOND, runWork(msg), null, null,
						msg.replyTo);
			} else if (msg.what == COMMAND_PAUSE_REQUEST) {
				replyMsg(COMMAND_PAUSE_RESPOND, pauseWork(msg), null, null,
						msg.replyTo);
			} else if (msg.what == COMMAND_RESET_REQUEST) {
				replyMsg(COMMAND_RESET_RESPOND, resetWork(msg), null, null,
						msg.replyTo);
			}

			super.handleMessage(msg);
		}

		protected boolean replyMsg(int what, boolean isValid, Object result,
				Bundle extra, Messenger replyTo) {
			if (replyTo == null)
				return false;

			Message rMsg = Message.obtain(null, what, isValid ? 1 : 0,
					isValid ? 1 : 0, result);
			if (extra != null)
				rMsg.setData(extra);

			try {
				replyTo.send(rMsg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return true;
		}

		protected boolean replyMsg(int what, int arg1, int arg2, Object result,
				Bundle extra, Messenger replyTo) {
			if (replyTo == null)
				return false;

			Message rMsg = Message.obtain(null, what, arg1, arg2, result);
			if (extra != null)
				rMsg.setData(extra);
			if (extra != null)
				rMsg.setData(extra);

			try {
				replyTo.send(rMsg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			return true;
		}
	}

	HandlerThread recieveThread;
	Looper recieveLooper;
	RecieveHandler recieveHandler;
	Messenger recieveMessenger;

	HandlerThread workThread;
	Looper workLooper;
	Handler workHandler;
	Messenger workMessenger;

	/**
	 * Indicates if there is running work.
	 */
	protected boolean isWorkThreadRunning = false;

	int lastStartId;

	public void onCreate(BoundService bService, Class<?> rHandler) {
		super.onCreate();
		recieveThread = new HandlerThread("ServiceRecieveFThread");
		recieveThread.start();
		recieveLooper = recieveThread.getLooper();
		try {
			Constructor<?> c = (Constructor<?>) rHandler.getConstructors()[0];
			recieveHandler = (RecieveHandler) c.newInstance(bService,
					recieveLooper);
		} catch (Exception e) {
			e.printStackTrace();
		}
		recieveMessenger = new Messenger(recieveHandler);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return recieveMessenger.getBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		lastStartId = startId;

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		recieveLooper.quit();
		recieveThread = null;
		recieveLooper = null;
		recieveHandler = null;
		recieveMessenger = null;

		if (isWorkThreadRunning)
			workLooper.quit();

		workThread = null;
		workLooper = null;
		workHandler = null;
	}

	protected boolean stopBoundService() {
		recieveLooper.quit();
		recieveThread = null;
		recieveLooper = null;
		recieveHandler = null;
		recieveMessenger = null;

		if (isWorkThreadRunning)
			workLooper.quit();

		workThread = null;
		workLooper = null;
		workHandler = null;
		return stopSelfResult(lastStartId);
	}

	protected final Handler getWorkHandler() {
		return workHandler;
	}

	protected boolean setupWorkThread() {
		if (isWorkThreadRunning)
			return true;

		workThread = new HandlerThread("WorkThread");
		workThread.start();
		workLooper = workThread.getLooper();
		workHandler = new Handler(workLooper);
		isWorkThreadRunning = true;

		return isWorkThreadRunning;
	}

	protected boolean finalizeWorkThread() {
		if (!isWorkThreadRunning)
			return true;

		workLooper.quit();
		workHandler = null;
		workLooper = null;
		workThread = null;
		isWorkThreadRunning = false;

		return !isWorkThreadRunning;
	}

	protected boolean runWork(Message msg) {
		return setupWorkThread();
	}

	protected void startWork(Message msg) {

	}

	protected void resumeWork(Message msg) {

	}

	protected boolean pauseWork(Message msg) {
		return finalizeWorkThread();
	}

	protected boolean resetWork(Message msg) {
		return finalizeWorkThread();
	}

}
