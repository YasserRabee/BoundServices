package com.learn.boundservice.test;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.learn.boundservice.BoundActivity;
import com.learn.boundservice.BoundService;
import com.learn.boundservice.R;

public class SensorsActivity extends BoundActivity implements OnClickListener {

	protected class SensorsHandler extends RecieveHandler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SensorsService.COMMAND_GET_DATA_RESPOND:
				if (validate(msg))
					outView.setText("Sensors Readings:\n"
							+ ((SensorsData) msg.obj).toString());
				break;
			case SensorsService.COMMAND_READ_STATE_RESPOND:
				if (validate(msg)) {
					Bundle data = msg.getData();
					outView.setText("Service State:"
							+ "\nisWorkRunning = "
							+ data.getBoolean(SensorsService.STATE_isWorkRunning)
							+ "\nisWorkPaused = "
							+ data.getBoolean(SensorsService.STATE_isWorkPaused)
							+ "\nregisteredSensors = "
							+ data.getIntArray(SensorsService.STATE_registeredSensors)
							+ "\nregisteredSensorsDelays = "
							+ data.getIntArray(SensorsService.STATE_registeredSensorsDelays));
				}
				break;

			case SensorsService.COMMAND_WORK_STATE_RESPOND:
				mToast(msg.what + " Successful");
				outView.setText("Work State:" + "\nisWorkRunning = "
						+ (msg.arg1 == 1) + "\nisWorkPaused = "
						+ (msg.arg2 == 1));

				break;

			default:
				break;
			}

			super.handleMessage(msg);
		}

	}

	Button startButton;
	Button killButton;

	Button bindButton;
	Button runButton;
	Button resetButton;
	Button stopButton;

	Button dataButton;
	Button stateButton;
	Button wStateButton;

	Button clearButton;
	TextView outView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(SensorsActivity.this, new SensorsHandler(),
				savedInstanceState);
		setContentView(R.layout.activity_sensors);

		startButton = (Button) findViewById(R.id.start);
		startButton.setOnClickListener(this);
		killButton = (Button) findViewById(R.id.kill);
		killButton.setOnClickListener(this);

		bindButton = (Button) findViewById(R.id.bind);
		bindButton.setOnClickListener(this);
		runButton = (Button) findViewById(R.id.run);
		runButton.setOnClickListener(this);
		resetButton = (Button) findViewById(R.id.reset);
		resetButton.setOnClickListener(this);
		stopButton = (Button) findViewById(R.id.stop);
		stopButton.setOnClickListener(this);

		dataButton = (Button) findViewById(R.id.data);
		dataButton.setOnClickListener(this);

		stateButton = (Button) findViewById(R.id.state);
		stateButton.setOnClickListener(this);

		wStateButton = (Button) findViewById(R.id.workState);
		wStateButton.setOnClickListener(this);

		clearButton = (Button) findViewById(R.id.clear);
		clearButton.setOnClickListener(this);
		outView = (TextView) findViewById(R.id.out);

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("outView_Text", outView.getText().toString());
		outState.putInt("OutView_Scroll", outView.getScrollY());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (isStarted)
			startButton.setText(R.string.stop);

		if (isRun)
			runButton.setText(R.string.pause);

		if (isBound) {
			runButton.setText(R.string.unbind);
		}

		outView.setText(savedInstanceState.getString("outView_Text"));
		outView.scrollTo(0, savedInstanceState.getInt("OutView_Scroll"));
	}

	@Override
	public void onClick(View v) {
		if (startButton == v) {
			onStartClick();
		} else if (killButton == v) {
			onKillClick();
		} else if (bindButton == v) {
			onBindClick();
		} else if (runButton == v) {
			onRunClick();
		} else if (stopButton == v) {
			onStopClick();
		} else if (resetButton == v) {
			onResetClick();
		} else if (dataButton == v) {
			onGetClick();
		} else if (wStateButton == v) {
			onWorkStateClick();
		} else if (stateButton == v) {
			onStateClick();
		} else if (clearButton == v) {
			outView.setText(R.string.results);
		}
	}

	protected void onStartClick() {
		if (isStarted)
			stopBoundService();
		else
			startBoundService();
	}

	@Override
	protected void onStartBoundService() {
		super.onStartBoundService();
		startButton.setText(R.string.stop);
	}

	@Override
	protected void onStopBoundService() {
		super.onStopBoundService();
		startButton.setText(R.string.start);
		// TODO: add more..
		bindButton.setText(R.string.bind);
		runButton.setText(R.string.run);
	}

	protected void onKillClick() {
		// TODO: kill not stop
		stopBoundService();
	}

	protected void onBindClick() {
		if (isBound)
			unbindBoundService();
		else
			bindBoundService();
	}

	@Override
	protected void onBindBoundService() {
		super.onBindBoundService();
		bindButton.setText(R.string.unbind);
	}

	@Override
	protected void onUnbindBoundService() {
		super.onUnbindBoundService();
		bindButton.setText(R.string.bind);
		// TODO: Add more...
	}

	protected void onRunClick() {
		if (isRun)
			pauseBoundServiceWork(new Message());
		else {
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
			runBoundServiceWork(msg);
		}
	}

	@Override
	protected void onRunBoundServiceWorkRespond() {
		super.onRunBoundServiceWorkRespond();
		runButton.setText(R.string.pause);
	}

	@Override
	protected void onPauseBoundServiceWorkRespond() {
		super.onPauseBoundServiceWorkRespond();
		runButton.setText(R.string.run);
	}

	protected void onResetClick() {
		resetBoundServiceWork(new Message());
	}

	protected void onStopClick() {
		stopBoundService(new Message());
	}

	@Override
	protected void onStopBoundServiceRespond() {
		super.onStopBoundServiceRespond();
		bindButton.setText(R.string.bind);
		// TODO: add more..
		startButton.setText(R.string.start);
		runButton.setText(R.string.run);
	}

	protected void onGetClick() {
		sendMsg(SensorsService.COMMAND_GET_DATA_REQUEST, new Message());
	}

	protected void onStateClick() {
		sendMsg(SensorsService.COMMAND_READ_STATE_REQUEST, new Message());
	}

	protected void onWorkStateClick() {
		sendMsg(SensorsService.COMMAND_WORK_STATE_REQUEST, new Message());
	}

}
