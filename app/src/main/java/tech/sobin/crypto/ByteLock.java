package tech.sobin.crypto;

public class ByteLock {

	private final Mapper mapper;
	private final byte[] reverse;

	public ByteLock(byte[] key) {
		mapper = new Mapper();
		for (byte b : key) {
			mapper.translate(b);
			mapper.swap(b);
		}

		reverse = new byte[256];
		for (int i = 0; i < 256; i++)
			reverse[mapper.getAsLine(i) & 0xff] = (byte)i;
	}

	public byte[] encrypt(byte[] data) {
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = mapper.getAsLine(data[i] & 0xff);
		return result;
	}

	public byte[] decrypt(byte[] data) {
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = reverse[data[i] & 0xff];
		return result;
	}

	public static byte[] encrypt(byte[] data, byte[] key) {
		ByteLock lock = new ByteLock(key);
		return lock.encrypt(data);
	}

	public static byte[] decrypt(byte[] data, byte[] key) {
		ByteLock lock = new ByteLock(key);
		return lock.decrypt(data);
	}
}
