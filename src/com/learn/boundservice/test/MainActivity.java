package com.learn.boundservice.test;


import com.learn.boundservice.BoundService;
import com.learn.boundservice.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceMessenger = null;
			isBound = false;
			bindButton.setText(R.string.bind);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized (bindBlock) {
				serviceMessenger = new Messenger(service);
				isBound = true;
				bindButton.setText(R.string.unbind);
			}
		}
	};

	final Handler recieveHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SensorsService.COMMAND_GET_DATA_RESPOND) {
				if (msg.obj != null && msg.obj != "") {
					outView.setGravity(Gravity.CENTER);
					if (outView.getText() == getText(R.string.results))
						outView.setText("");
					outView.append(msg.obj.toString());
				} else
					outView.setText("No Data");
			} else if (msg.what == BoundService.COMMAND_RUN_RESPOND) {
				if (msg.arg1 == 1 && msg.arg2 == 1)
					Toast.makeText(MainActivity.this, "Run Successful",
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(MainActivity.this, "Run Unsuccessful",
							Toast.LENGTH_SHORT).show();
			} else if (msg.what == BoundService.COMMAND_PAUSE_RESPOND) {
				if (msg.arg1 == 1 && msg.arg2 == 1)
					Toast.makeText(MainActivity.this, "Pause Successful",
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(MainActivity.this, "Pause Unsuccessful",
							Toast.LENGTH_SHORT).show();
			} else if (msg.what == BoundService.COMMAND_RESET_RESPOND) {
				if (msg.arg1 == 1 && msg.arg2 == 1)
					Toast.makeText(MainActivity.this, "Reset Successful",
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(MainActivity.this, "Reset Unsuccessful",
							Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(MainActivity.this, R.string.unknown_message,
						Toast.LENGTH_SHORT).show();
			}

			super.handleMessage(msg);
		}

	};

	final Messenger recieveMessenger = new Messenger(recieveHandler);

	Button startButton;
	Button bindButton;
	Button killButton;
	Button runButton;
	Button resetButton;
	Button getButton;
	Button clearButton;

	TextView outView;

	Intent serviceIntent;
	Messenger serviceMessenger = null;
	boolean isBound = false;
	boolean isRun = false;
	boolean isStarted = false;

	Object bindBlock = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensors);

		startButton = (Button) findViewById(R.id.start);
		startButton.setOnClickListener(this);
		bindButton = (Button) findViewById(R.id.bind);
		bindButton.setOnClickListener(this);
		killButton = (Button) findViewById(R.id.kill);
		killButton.setOnClickListener(this);
		runButton = (Button) findViewById(R.id.run);
		runButton.setOnClickListener(this);
		getButton = (Button) findViewById(R.id.data);
		getButton.setOnClickListener(this);
		resetButton = (Button) findViewById(R.id.reset);
		resetButton.setOnClickListener(this);
		clearButton = (Button) findViewById(R.id.clear);
		clearButton.setOnClickListener(this);

		outView = (TextView) findViewById(R.id.out);

		serviceIntent = new Intent(MainActivity.this, SensorsService.class);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isBound")) {
			synchronized (bindBlock) {
				serviceIntent = savedInstanceState
						.getParcelable("serviceIntent");
				serviceMessenger = savedInstanceState
						.getParcelable("serviceMessenger");
				bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
				bindButton.setText(R.string.unbind);
			}

		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isStarted && isBound)
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

		outState.putString("outView_Text", outView.getText().toString());
		outState.putInt("OutView_Scroll", outView.getScrollY());
		outState.putParcelable("serviceIntent", serviceIntent);
		outState.putParcelable("serviceMessenger", serviceMessenger);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (isStarted = savedInstanceState.getBoolean("isStarted")) {
			startService(serviceIntent);
			startButton.setText(R.string.stop);
		}

		if (savedInstanceState.getBoolean("isRun")) {
			Message msg = Message
					.obtain(null, BoundService.COMMAND_RUN_REQUEST);

			SensorsRegisteration[] regs = new SensorsRegisteration[] {
					new SensorsRegisteration(Sensor.TYPE_ACCELEROMETER,
							SensorManager.SENSOR_DELAY_NORMAL),
					new SensorsRegisteration(Sensor.TYPE_GYROSCOPE,
							SensorManager.SENSOR_DELAY_NORMAL),
					new SensorsRegisteration(Sensor.TYPE_PRESSURE,
							SensorManager.SENSOR_DELAY_NORMAL) };
			msg.obj = regs;
			msg.replyTo = recieveMessenger;
			try {
				synchronized (bindBlock) {

					serviceMessenger.send(msg);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			isRun = true;
			runButton.setText(R.string.pause);
		}

		outView.setText(savedInstanceState.getString("outView_Text"));
		outView.scrollTo(0, savedInstanceState.getInt("OutView_Scroll"));
	}

	@Override
	public void onClick(View v) {
		if (startButton == v) {
			onStartClick();
		} else if (bindButton == v) {
			onBindClick();
		} else if (killButton == v) {
			onKillClick();
		} else if (runButton == v) {
			onRunClick();
		} else if (resetButton == v) {
			onResetClick();
		} else if (getButton == v) {
			onGetClick();
		} else if (clearButton == v) {
			outView.setText(R.string.results);
		}
	}

	protected void onGetClick() {
		if (!isBound) {
			onNotBound();
			return;
		}

		Message msg = Message.obtain(null,
				SensorsService.COMMAND_GET_DATA_REQUEST);
		msg.replyTo = recieveMessenger;
		try {
			serviceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected void onResetClick() {
		if (!isBound) {
			onNotBound();
			return;
		}

		Message msg = Message.obtain(null, BoundService.COMMAND_RESET_REQUEST);
		msg.replyTo = recieveMessenger;
		try {
			serviceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected void onRunClick() {
		if (!isBound) {
			onNotBound();
			return;
		}
		Message msg;
		if (isRun) {
			msg = Message.obtain(null, BoundService.COMMAND_PAUSE_REQUEST);
			isRun = false;
			runButton.setText(R.string.run);
		} else {
			msg = Message.obtain(null, BoundService.COMMAND_RUN_REQUEST);

			SensorsRegisteration[] regs = new SensorsRegisteration[] {
					new SensorsRegisteration(Sensor.TYPE_ACCELEROMETER,
							SensorManager.SENSOR_DELAY_NORMAL),
					new SensorsRegisteration(Sensor.TYPE_GYROSCOPE,
							SensorManager.SENSOR_DELAY_NORMAL),
					new SensorsRegisteration(Sensor.TYPE_PRESSURE,
							SensorManager.SENSOR_DELAY_NORMAL) };
			msg.obj = regs;

			isRun = true;
			runButton.setText(R.string.pause);
		}
		msg.replyTo = recieveMessenger;
		try {
			serviceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected void onKillClick() {
		if (isStarted) {
			stopService(serviceIntent);
			startButton.setText(R.string.start);
			isStarted = false;
			if (isBound)
				bindButton.setText(R.string.bind);
			if (isRun)
				runButton.setText(R.string.run);

			Toast.makeText(this, "Service killed succsessfully",
					Toast.LENGTH_SHORT).show();
		} else
			Toast.makeText(this, "Service allready killed", Toast.LENGTH_SHORT)
					.show();
	}

	protected void onBindClick() {
		if (isBound) {
			unbindService(serviceConnection);
			bindButton.setText(R.string.bind);
			isBound = false;
		} else {
			bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
			bindButton.setText(R.string.unbind);
		}
	}

	protected void onStartClick() {
		if (isStarted) {
			if (!isBound) {
				onNotBound();
				return;
			}
			Message msg = Message.obtain(null,
					BoundService.COMMAND_STOP_SERVICE_REQUEST);
			try {
				serviceMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			startButton.setText(R.string.start);
			isStarted = false;

		} else {
			startService(serviceIntent);

			startButton.setText(R.string.stop);
			isStarted = true;
		}
	}

	protected void onNotBound() {
		Toast.makeText(this, "Bind to the service first", Toast.LENGTH_SHORT)
				.show();
	}
}
