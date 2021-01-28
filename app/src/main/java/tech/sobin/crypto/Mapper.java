package tech.sobin.crypto;

public class Mapper extends ByteMatrix {

	public Mapper() {
		super(16, 16);
		byte k = 0;
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 16; j++)
				set(j, i, k++);
	}

	public void swap(byte times) {
		int T = times & 3;
		if (T == 0) return;
		else if (T == 2) {
			byte t;
			for (int i = 0, y = 15; i <= y; i++, y--) {
				for (int j = 0, x = 15; j < 16; j++, x--) {
					t = get(j, i);
					set(j, i, get(x, y));
					set(x, y, t);
				}
			}
		}
		else {
			ByteMatrix example = this.clone();
			if (T == 1) {
				for (int i = 0, x = 15; i < 16; i++, x--) {
					for (int j = 0, y = 0; j < 16; j++, y++) {
						set(x, y, example.get(j, i));
					}
				}
			} else {
				for (int i = 0, x = 0; i < 16; i++, x++) {
					for (int j = 0, y = 15; j < 16; j++, y--) {
						set(x, y, example.get(j, i));
					}
				}
			}
		}
	}

	public void translate(byte distance) {
		int T = distance & 0xff;
		if (T == 0) return;
		byte[] example = new byte[256];
		int i, j;
		for (i = 0; i < 256; i++)
			example[i] = getAsLine(i);
		for (i = T, j = 0; i < 256; i++, j++)
			setAsLine(i, example[j]);
		for (i = 0; j < 256; i++, j++)
			setAsLine(i, example[j]);
	}
}
