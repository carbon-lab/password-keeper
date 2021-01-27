package tech.sobin.crypto;

public class ByteMatrix implements Cloneable {

	private byte[][] mat;

	private ByteMatrix() {}

	public ByteMatrix(int width, int height) {
		this.mat = new byte[height][width];
	}

	public void set(int x, int y, byte value) {
		mat[y][x] = value;
	}

	public byte get(int x, int y) {
		return mat[y][x];
	}

	public int getWidth() {
		return mat[0].length;
	}

	public int getHeight() {
		return mat.length;
	}

	public byte getAsLine(int index) {
		int width = getWidth();
		int y = index / width;
		int x = index % width;
		return get(x, y);
	}

	public void setAsLine(int index, byte value) {
		int width = getWidth();
		int y = index / width;
		int x = index % width;
		set(x, y, value);
	}

	public String toString() {
		StringBuilder R = new StringBuilder();
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++)
				R.append(String.format("%4d", get(j, i) & 0xff));
			R.append('\n');
		}
		return R.toString();
	}

	@Override
	public ByteMatrix clone() {
		ByteMatrix R = new ByteMatrix(getWidth(), getHeight());
		for (int i = 0; i < getHeight(); i++)
			for (int j = 0; j < getWidth(); j++)
				R.set(j, i, this.get(j, i));
		return R;
	}
}
