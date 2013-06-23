package net.donizyo.hexempire.util;

public class IntVector {
	private final int[] entries;

	public IntVector(int ... entries) {
		this.entries = entries;
	}
	
	public IntVector(DoubleVector vec) {
		entries = new int[vec.length()];
		for (int i = 0; i < vec.length(); i++) {
			entries[i] = (int) vec.getEntries()[i];
		}
	}
	
	public int[] getEntries() {
		return entries;
	}
	
	public int length() {
		return entries.length;
	}
	
	public IntVector copy() {
		return new IntVector(entries);
	}
	
	public IntVector setNegative() {
		for (int i = 0; i < entries.length; i++) {
			entries[i] = -entries[i];
		}
		return this;
	}

	public IntVector plusWith(IntVector vec)
			throws InvalidOperationException {
		if (entries.length != vec.entries.length)
			throw new InvalidOperationException();
		for (int i = 0; i < entries.length; i++) {
			entries[i] += vec.entries[i];
		}
		return this;
	}

	public IntVector minusWith(IntVector vec)
			throws InvalidOperationException {
		if (entries.length != vec.entries.length)
			throw new InvalidOperationException();
		for (int i = 0; i < entries.length; i++) {
			entries[i] -= vec.entries[i];
		}
		return this;
	}

	public IntVector multiplyWith(int dValue) {
		for (int i = 0; i < entries.length; i++) {
			entries[i] *= dValue;
		}
		return this;
	}
	
	public IntVector divideWith(int dValue) {
		for (int i = 0; i < entries.length; i++) {
			entries[i] /= dValue;
		}
		return this;
	}
	
	public int dot(IntVector vec)
			throws InvalidOperationException {
		int result = 0;
		if (entries.length != vec.entries.length)
			throw new InvalidOperationException();
		for (int i = 0; i < entries.length; i++) {
			result += entries[i] * vec.entries[i];
		}
		return result;
	}
	
	public double size() {
		int result = 0;
		for (int i = 0; i < entries.length; i++) {
			result += entries[i] * entries[i];
		}
		return Math.sqrt(result);
	}
	
	public double cosine(IntVector vec)
			throws InvalidOperationException {
		return dot(vec) / (size() * vec.size());
	}
	
	public double projectSize(IntVector vec)
			throws InvalidOperationException {
		return dot(vec) / vec.size();
	}
	
	public IntVector project(IntVector vec)
			throws InvalidOperationException {
		IntVector result = vec.copy();
		result.divideWith((int) vec.size());
		result.multiplyWith((int) (cosine(vec) * size()));
//		result.multiplyWith(projectSize(vec) / vec.size());
		return cosine(vec) > 0 ? result : result.setNegative();
	}
	
	public String toString() {
		String result = "IntVector (" + entries[0];
		for (int i = 1; i < entries.length; i++) {
			result += ',';
			result += entries[i];
		}
		result += ")";
		return result;
	}
}
