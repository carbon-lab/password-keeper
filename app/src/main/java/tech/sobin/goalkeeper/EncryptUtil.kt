package tech.sobin.goalkeeper

import tech.sobin.crypto.ByteLock
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec


fun SHA512(data: ByteArray): ByteArray {
	val dig = MessageDigest.getInstance("SHA-512")
	dig.update(data)
	return dig.digest()
}

fun hexOf(data: ByteArray): String {
	val rs = StringBuilder()
	for (byte in data) {
		var s = Integer.toHexString(byte.toInt().and(0xff))
		if (s.length == 1) s = "0$s"
		rs.append(s)
	}
	return rs.toString()
}

fun blobOf(hex: String): ByteArray {
	val result = ByteArray(hex.length / 2)
	var i = 0
	var j = 0
	while (i + 1 < hex.length) {
		var b: Byte = 0
		val subs = hex.substring(i, i + 2)
		b = Integer.parseInt(subs, 16).toByte()
		result[j] = b
		j += 1
		i += 2
	}
	return result
}

fun AESEncrypt(data: ByteArray, password: ByteArray): ByteArray {
	try {
		val kgen = KeyGenerator.getInstance("AES")
		val random = SecureRandom.getInstance("SHA1PRNG")
		random.setSeed(password)
		kgen.init(128, random)
		val secretKey = kgen.generateKey()
		val enCodeFormat = secretKey.encoded
		val key = SecretKeySpec(enCodeFormat, "AES")
		val cipher = Cipher.getInstance("AES")
		cipher.init(Cipher.ENCRYPT_MODE, key)
		return cipher.doFinal(data)
	} catch (e: Exception) {
		e.printStackTrace()
		return byteArrayOf()
	}
}

fun AESDecrypt(data: ByteArray, password: ByteArray): ByteArray {
	try {
		val kgen = KeyGenerator.getInstance("AES")
		val random = SecureRandom.getInstance("SHA1PRNG")
		random.setSeed(password)
		kgen.init(128, random)
		val secretKey = kgen.generateKey()
		val enCodeFormat = secretKey.encoded
		val key = SecretKeySpec(enCodeFormat, "AES")
		val cipher = Cipher.getInstance("AES")
		cipher.init(Cipher.DECRYPT_MODE, key)
		return cipher.doFinal(data)
	} catch (e: Exception) {
		e.printStackTrace()
		return byteArrayOf()
	}
}

fun DefaultEncrypt(data: ByteArray, key: ByteArray): ByteArray {
	return ByteLock.encrypt(data, key)
}

fun DefaultDecrypt(data: ByteArray, key: ByteArray): ByteArray {
	return ByteLock.decrypt(data, key)
}

private const val numberSet = "0123456789"
private const val upperSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val lowerSet = "abcdefghijklmnopqrstuvwxyz"
private const val symbolSet = "!@#$%^&*|+-_=~?/<>."

fun genKey(length: Int,
		   needNumber: Boolean,
		   needUpper: Boolean,
		   needLower: Boolean,
		   needSymbol: Boolean): String {
	val setbdr = java.lang.StringBuilder()
	if (needNumber) setbdr.append(numberSet)
	if (needUpper) setbdr.append(upperSet)
	if (needLower) setbdr.append(lowerSet)
	if (needSymbol) setbdr.append(symbolSet)
	val set = setbdr.toString()
	if (set.isEmpty()) return ""
	val result = java.lang.StringBuilder()
	for (i in 1..length) {
		result.append(
			set[(Math.random() * set.length).toInt()]
		)
	}
	return result.toString()
}