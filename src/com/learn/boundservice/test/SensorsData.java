package com.learn.boundservice.test;

import java.util.ArrayList;

import android.util.SparseArray;

public class SensorsData extends SparseArray<ArrayList<Float[]>> {

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < this.size(); i++) {
			int key = this.keyAt(i);
			res.append("Sensor: " + key + "\n	Values: \n");
			for (Float[] values : this.get(key)) {
				for (Float v : values) {
					res.append(v);
					res.append(',');
				}
				res.append(";\n");
			}
			res.append("##################\n");
		}
		return res.toString();
	}

	@Override
	public SensorsData clone() {
		SensorsData res = new SensorsData();
		for (int i = 0; i < this.size(); i++) {
			int key = keyAt(i);
			ArrayList<Float[]> sReadingsClone = new ArrayList<Float[]>();
			ArrayList<Float[]> sReadings = get(key);
			for (int j = 0; j < sReadings.size(); j++) {
				Float[] sRead = sReadings.get(j);
				Float[] sReadClone = new Float[sRead.length];
				for (int k = 0; k < sRead.length; k++) {
					sReadClone[k] = sRead[k];
				}
				sReadingsClone.add(sReadClone);
			}
			res.append(key, sReadingsClone);
		}

		return res;
	}

}
