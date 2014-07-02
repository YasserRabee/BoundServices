package com.learn.boundservice;

import com.learn.boundservice.BoundService;
import com.learn.boundservice.test.SensorsService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class BoundActivity extends Activity {

	final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceMessenger = null;
			isBound = false;

			Toast.makeText(BoundActivity.this, "Bind Break", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			serviceMessenger = new Messenger(service);
		}
	};

	protected class RecieveHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == BoundService.COMMAND_RUN_RESPOND) {
				if (validate(msg)) {
					onRunBoundServiceWorkRespond();
				}
			} else if (msg.what == BoundService.COMMAND_PAUSE_RESPOND) {
				if (validate(msg)) {
					onPauseBoundServiceWorkRespond();
				}
			} else if (msg.what == BoundService.COMMAND_RESET_RESPOND) {
				if (validate(msg)) {
					onResetBoundServiceWorkRespond();
				}
			} else if (msg.what == BoundService.COMMAND_STOP_SERVICE_RESPOND) {
				if (validate(msg)) {
					onStopBoundServiceRespond();
				}
			}

			super.handleMessage(msg);
		}

		protected boolean validate(Message msg) {
			if (msg.arg1 == 1 && msg.arg2 == 1) {
				mToast(msg.what + " Successful");
				return true;
			} else {
				mToast(msg.what + " Unsuccessful");
				return false;
			}
		}
	}

	RecieveHandler recieveHandler;

	private Messenger recieveMessenger;

	Intent serviceIntent;
	Messenger serviceMessenger;

	protected boolean isBound = false;
	protected boolean isRun = false;
	protected boolean isPaused = false;
	protected boolean isStarted = false;

	protected void onCreate(BoundActivity bActivity, RecieveHandler rHandler,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		recieveHandler = rHandler;
		// try {
		// Constructor<?> c = (Constructor<?>) rHandler.getConstructors()[0];
		// recieveHandler = (RecieveHandler) c.newInstance(bActivity);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		recieveMessenger = new Messenger(recieveHandler);

		if (savedInstanceState == null) {
			serviceIntent = new Intent(BoundActivity.this, SensorsService.class);
		} else {
			if (isBound = savedInstanceState.getBoolean("isBound")) {
				serviceIntent = savedInstanceState
						.getParcelable("serviceIntent");
				serviceMessenger = savedInstanceState
						.getParcelable("serviceMessenger");
				bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
			}

			isStarted = savedInstanceState.getBoolean("isStarted");
			isRun = savedInstanceState.getBoolean("isRun");
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isBound)
			unbindService(serviceConnection);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isBound) {
			bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isStarted", isStarted);
		outState.putBoolean("isBound", isBound);
		outState.putBoolean("isRun", isRun);

		outState.putParcelable("serviceIntent", serviceIntent);
		outState.putParcelable("serviceMessenger", serviceMessenger);
	}

	protected final Messenger getRecieveMessenger() {
		return recieveMessenger;
	}

	protected final boolean startBoundService() {
		if (startService(serviceIntent) != null) {
			isStarted = true;
			onStartBoundService();
		} else {
			isStarted = false;
			onStartBoundServiceFailed();
		}
		return isStarted;
	}

	protected void onStartBoundServiceFailed() {
		Toast.makeText(BoundActivity.this, "Start BoundService Unsuccessful",
				Toast.LENGTH_SHORT).show();
	}

	protected void onStartBoundService() {
		Toast.makeText(BoundActivity.this, "Start BoundService Successful",
				Toast.LENGTH_SHORT).show();
	}

	protected final boolean stopBoundService() {
		if (stopService(serviceIntent)) {
			isStarted = false;
			// TODO: Add more..
			isBound = false;
			isRun = false;
			isPaused = false;
			onStopBoundService();
		} else
			onStopBoundServiceFailed();
		return !isStarted;
	}

	protected void onStopBoundServiceFailed() {
		Toast.makeText(BoundActivity.this, "Stop BoundService Unsuccessful",
				Toast.LENGTH_SHORT).show();
	}

	protected void onStopBoundService() {
		Toast.makeText(BoundActivity.this, "Stop BoundService Successful",
				Toast.LENGTH_SHORT).show();
	}

	protected final boolean bindBoundService() {
		if (bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)) {
			isBound = true;
			onBindBoundService();
		} else {
			onBindBoundServiceFailed();
			isBound = false;
		}
		return isBound;
	}

	protected void onBindBoundServiceFailed() {
		Toast.makeText(BoundActivity.this, "Bind BoundService Unsuccessful",
				Toast.LENGTH_SHORT).show();
	}

	protected void onBindBoundService() {
		Toast.makeText(BoundActivity.this, "Bind BoundService Successful",
				Toast.LENGTH_SHORT).show();
	}

	protected final void unbindBoundService() {
		unbindService(serviceConnection);
		isBound = false;
		onUnbindBoundService();
	}

	protected void onUnbindBoundService() {
		Toast.makeText(BoundActivity.this, "Unbind BoundService Successful",
				Toast.LENGTH_SHORT).show();
	}

	protected boolean runBoundServiceWork(Message msg) {
		return sendMsg(BoundService.COMMAND_RUN_REQUEST, msg);
	}

	protected void onRunBoundServiceWorkRespond() {
		isRun = true;
		isPaused = false;
	}

	protected boolean pauseBoundServiceWork(Message msg) {
		return sendMsg(BoundService.COMMAND_PAUSE_REQUEST, msg);
	}

	protected void onPauseBoundServiceWorkRespond() {
		isRun = false;
		isPaused = true;
	}

	protected boolean resetBoundServiceWork(Message msg) {
		return sendMsg(BoundService.COMMAND_RESET_REQUEST, msg);
	}

	protected void onResetBoundServiceWorkRespond() {
		isRun = false;
		isPaused = false;
	}

	protected boolean stopBoundService(Message msg) {
		return sendMsg(BoundService.COMMAND_STOP_SERVICE_REQUEST, msg);
	}

	protected void onStopBoundServiceRespond() {
		isRun = false;
		isPaused = false;
		// TODO: Add isStarted
		isStarted = false;
		isBound = false;
	}

	protected final boolean sendMsg(int what, Message msg) {
		if (!validateBound()) {
			onSendMsgFailed(msg);
			return false;
		}

		Message sMsg = Message.obtain(msg);
		sMsg.what = what;
		sMsg.replyTo = recieveMessenger;
		try {
			serviceMessenger.send(sMsg);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}

		onSendMsg(msg);
		return true;
	}

	protected void onSendMsgFailed(Message msg) {
		mToast(msg.what + " Unsuccessful");
	}

	protected void onSendMsg(Message msg) {
		mToast(msg.what + " Successful");
	}

	protected final boolean validateBound() {
		if (!isBound)
			Toast.makeText(this, "Bind to the service first",
					Toast.LENGTH_SHORT).show();
		return isBound;
	}

	protected void mToast(String s) {
		Toast.makeText(BoundActivity.this, s, Toast.LENGTH_SHORT).show();
	}
}
