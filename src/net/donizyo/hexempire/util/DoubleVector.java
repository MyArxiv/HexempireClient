package net.donizyo.hexempire.util;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DoubleVector {
	private final double[] entries;
	private AtomicBoolean isLocked;

	public DoubleVector(double ... entries) {
		this.entries = entries;
		isLocked = new AtomicBoolean(false);
	}
	
	public DoubleVector(IntVector vec) {
		entries = new double[vec.length()];
		for (int i = 0; i < vec.length(); i++) {
			entries[i] = (int) vec.getEntries()[i];
		}
	}
	
	public DoubleVector clone() {
		return new DoubleVector(entries.clone());
	}
	
	public double[] getEntries() {
		if (isLocked.get())
			return entries.clone();
		return entries;
	}
	
	public int length() {
		return entries.length;
	}
	
	public DoubleVector lock() {
		isLocked.set(true);
		return this;
	}

	public DoubleVector unlock() {
		isLocked.set(false);
		return this;
	}
	
	public DoubleVector setNegative() {
		if (isLocked.get())
			return clone().setNegative();
		for (int i = 0; i < entries.length; i++) {
			entries[i] = -entries[i];
		}
		return this;
	}

	public DoubleVector plusWith(DoubleVector vec)
			throws InvalidOperationException {
		if (isLocked.get())
			return clone().plusWith(vec);
		if (entries.length != vec.entries.length)
			throw new InvalidOperationException();
		for (int i = 0; i < entries.length; i++) {
			entries[i] += vec.entries[i];
		}
		return this;
	}

	public DoubleVector minusWith(DoubleVector vec)
			throws InvalidOperationException {
		if (isLocked.get())
			return clone().minusWith(vec);
		if (entries.length != vec.entries.length)
			throw new InvalidOperationException();
		for (int i = 0; i < entries.length; i++) {
			entries[i] -= vec.entries[i];
		}
		return this;
	}

	public DoubleVector multiplyWith(double dValue) {
		if (isLocked.get())
			return clone().multiplyWith(dValue);
		for (int i = 0; i < entries.length; i++) {
			entries[i] *= dValue;
		}
		return this;
	}
	
	public DoubleVector divideWith(double dValue) {
		if (isLocked.get())
			return clone().divideWith(dValue);
		for (int i = 0; i < entries.length; i++) {
			entries[i] /= dValue;
		}
		return this;
	}
	
	public double dot(DoubleVector vec)
			throws InvalidOperationException {
		double result = 0;
		if (entries.length != vec.entries.length)
			throw new InvalidOperationException();
		for (int i = 0; i < entries.length; i++) {
			result += entries[i] * vec.entries[i];
		}
		return result;
	}
	
	public double size() {
		double result = 0;
		for (int i = 0; i < entries.length; i++) {
			result += entries[i] * entries[i];
		}
		return Math.sqrt(result);
	}
	
	public double cosine(DoubleVector vec)
			throws InvalidOperationException {
		return dot(vec) / (size() * vec.size());
	}
	
	public double projectSize(DoubleVector vec)
			throws InvalidOperationException {
		return dot(vec) / vec.size();
	}
	
	public DoubleVector project(DoubleVector vec)
			throws InvalidOperationException {
		double cos = cosine(vec);
		DoubleVector result = vec.clone().multiplyWith(cos * size() / vec.size());
		return cos > 0 ? result : result.setNegative();
	}
	
	public String toString() {
		String result = "DoubleVector (" + entries[0];
		for (int i = 1; i < entries.length; i++) {
			result += ',';
			result += entries[i];
		}
		result += ")";
		return result;
	}
}
