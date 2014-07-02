package com.learn.boundservice.test;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import com.learn.boundservice.BoundService;

public class SensorsService extends BoundService {

	public static final int COMMAND_GET_DATA_REQUEST = 0x00000004;
	public static final int COMMAND_GET_DATA_RESPOND = 0xf0000004;

	public static final int COMMAND_READ_STATE_REQUEST = 0x00000005;
	public static final int COMMAND_READ_STATE_RESPOND = 0xf0000005;

	public static final int COMMAND_WORK_STATE_REQUEST = 0x00000006;
	public static final int COMMAND_WORK_STATE_RESPOND = 0xf0000006;

	public static final String STATE_isWorkPaused = "isWorkPaused";
	public static final String STATE_isWorkRunning = "isWorkRunning";
	public static final String STATE_registeredSensors = "registeredSensors";
	public static final String STATE_registeredSensorsDelays = "registeredSensorsDelays";

	public class SensorsMonitor implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO: optimize this code
			int sType = event.sensor.getType();

			Float[] v = new Float[event.values.length];

			for (int i = 0; i < v.length; i++) {
				v[i] = event.values[i];
			}

			synchronized (dataBlocking) {
				ArrayList<Float[]> sValues = data.get(sType);
				if (sValues == null) {
					sValues = new ArrayList<Float[]>();
				}

				sValues.add(v);

				data.append(sType, sValues);
			}
		}

	}

	protected class SensorsHandler extends RecieveHandler {

		public SensorsHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case COMMAND_GET_DATA_REQUEST:
				Object rData = readData();
				replyMsg(COMMAND_GET_DATA_RESPOND, rData != null, rData, null,
						msg.replyTo);
				break;

			case COMMAND_READ_STATE_REQUEST:
				replyMsg(COMMAND_READ_STATE_RESPOND, true, null, readState(),
						msg.replyTo);
				break;

			case COMMAND_WORK_STATE_REQUEST:
				replyMsg(COMMAND_WORK_STATE_RESPOND, isWorkRunning ? 1 : 0,
						isWorkPaused ? 1 : 0, null, null, msg.replyTo);
				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}
	}

	SensorManager sensorManager;
	SensorsMonitor sMonitor;

	Object dataBlocking = new Object();
	SensorsData data;
	SparseBooleanArray registeredSensors;
	SparseIntArray registeredSensorsDelays;

	protected boolean isWorkPaused;
	protected boolean isWorkRunning;

	@Override
	public void onCreate() {
		super.onCreate(SensorsService.this, SensorsHandler.class);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sMonitor = new SensorsMonitor();
		data = new SensorsData();
		registeredSensors = new SparseBooleanArray();
		registeredSensorsDelays = new SparseIntArray();
	}

	@Override
	protected boolean runWork(Message msg) {
		if (!super.runWork(msg))
			return false;

		if (isWorkPaused)
			resumeWork(msg);
		else
			startWork(msg);

		return isWorkRunning;
	}

	@Override
	protected void startWork(Message msg) {
		for (SensorsRegisteration sReg : (SensorsRegisteration[]) msg.obj) {
			if (!registeredSensors.get(sReg.sensorType)
					&& sensorManager.registerListener(sMonitor,
							sensorManager.getDefaultSensor(sReg.sensorType),
							sReg.delay, getWorkHandler())) {
				registeredSensors.append(sReg.sensorType, true);
				registeredSensorsDelays.append(sReg.sensorType, sReg.delay);
			}
		}

		isWorkRunning = true;
	}

	@Override
	protected void resumeWork(Message msg) {
		for (int i = 0; i < registeredSensors.size(); i++) {
			int key = registeredSensors.keyAt(i);
			if (!sensorManager.registerListener(sMonitor,
					sensorManager.getDefaultSensor(key),
					registeredSensorsDelays.get(key), getWorkHandler())) {
				sensorManager.unregisterListener(sMonitor);
				return;
			}
		}

		isWorkRunning = true;
		isWorkPaused = false;
	}

	@Override
	protected boolean pauseWork(Message msg) {
		sensorManager.unregisterListener(sMonitor);
		isWorkRunning = false;
		isWorkPaused = true;

		return super.pauseWork(msg);
	}

	@Override
	protected boolean resetWork(Message msg) {
		sensorManager.unregisterListener(sMonitor);

		synchronized (dataBlocking) {
			data.clear();
		}
		registeredSensors.clear();
		registeredSensorsDelays.clear();

		isWorkPaused = false;
		isWorkRunning = false;

		return super.resetWork(msg);
	}

	protected final SparseArray<ArrayList<Float[]>> readData() {
		synchronized (dataBlocking) {
			if (data != null)
				return data.clone();
		}

		return null;
	}

	protected final Bundle readState() {

		Bundle data = new Bundle();
		data.putBoolean(STATE_isWorkPaused, isWorkPaused);
		data.putBoolean(STATE_isWorkRunning, isWorkRunning);

		int senNum = registeredSensors.size();
		int[] regSensors = new int[senNum];
		int[] regSensorsDelays = new int[senNum];
		for (int i = 0; i < registeredSensors.size(); i++) {
			int sensor = registeredSensors.keyAt(i);
			regSensors[i] = sensor;
			regSensorsDelays[i] = registeredSensorsDelays.get(sensor);
		}

		data.putIntArray(STATE_registeredSensors, regSensors);
		data.putIntArray(STATE_registeredSensorsDelays, regSensorsDelays);

		return data;
	}

}
