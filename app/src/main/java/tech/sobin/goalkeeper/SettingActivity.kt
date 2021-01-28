package tech.sobin.goalkeeper

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import tech.sobin.crypto.ByteLock

class SettingActivity : AppCompatActivity() {

	private lateinit var editOldPassword: EditText
	private lateinit var editNewPassword: EditText
	private lateinit var editConfirmNew: EditText
	private lateinit var btnChangePassword: Button
	private lateinit var editRandomLength: EditText
	private lateinit var checkNumber: CheckBox
	private lateinit var checkUpper: CheckBox
	private lateinit var checkLower: CheckBox
	private lateinit var checkSymbol: CheckBox

	private val db = DBOpenHelper.liveDB

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_setting)
		window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE)
		ActivityManager.push(this)

		editOldPassword = findViewById(R.id.editOldPassword)
		editNewPassword = findViewById(R.id.editNewPassword)
		editConfirmNew = findViewById(R.id.editConfirmNew)
		btnChangePassword = findViewById(R.id.btnChangePassword)
		editRandomLength = findViewById(R.id.editRandomLength)
		checkNumber = findViewById(R.id.checkNumber)
		checkUpper = findViewById(R.id.checkUpper)
		checkLower = findViewById(R.id.checkLower)
		checkSymbol = findViewById(R.id.checkSymbol)

		editRandomLength.setText(AppContext.genkeyLength.toString())
		checkNumber.isChecked = AppContext.genkeyNumber
		checkUpper.isChecked = AppContext.genkeyUpper
		checkLower.isChecked = AppContext.genkeyLower
		checkSymbol.isChecked = AppContext.genkeySymbol

		btnChangePassword.setOnClickListener {
			val old = editOldPassword.text.toString()
			val new = editNewPassword.text.toString()
			val confirm = editConfirmNew.text.toString()
			if (new != confirm) {
				alert(R.string.key_not_match)
			}
			else if (hexOf(SHA512(old.toByteArray())) != AppContext.passwordHash) {
				alert(R.string.wrong_password)
			}
			else {
				val wdb = db?.writableDatabase
				val cv = ContentValues()
				cv.put("password_hash", hexOf(SHA512(new.toByteArray())))
				wdb?.update("context", cv, null, null)
				AppContext.reloadContext()
				reEncrypt(old.toByteArray(), new.toByteArray())
				setResult(1)
				finish()
			}
		}
	}

	private fun reEncrypt(oldKey: ByteArray, newKey: ByteArray) {
		val oldLock = ByteLock(oldKey)
		val newLock = ByteLock(newKey)
		val rdb = db?.readableDatabase
		val wdb = db?.writableDatabase
		val all = rdb?.rawQuery("SELECT * FROM record", null)!!
		while (all.moveToNext()) {
			val id = all.getInt(0)
			val cdata = all.getBlob(1)
			val data = oldLock.decrypt(cdata)
			val ncd = newLock.encrypt(data)
			val cv = ContentValues()
			cv.put("body", ncd)
			wdb?.update("record", cv, "id=$id", null)
		}
	}

	private fun save() {
		try {
			val genkey_length = editRandomLength.text.toString().toInt()
			val genkey_number = if (checkNumber.isChecked) 1 else 0
			val genkey_upper = if (checkUpper.isChecked) 1 else 0
			val genkey_lower = if (checkLower.isChecked) 1 else 0
			val genkey_symbol = if (checkSymbol.isChecked) 1 else 0
			val cv = ContentValues()
			cv.put("genkey_length", genkey_length)
			cv.put("genkey_number", genkey_number)
			cv.put("genkey_upper", genkey_upper)
			cv.put("genkey_lower", genkey_lower)
			cv.put("genkey_symbol", genkey_symbol)
			val wdb = db?.writableDatabase
			wdb?.update("context", cv, null, null)
			AppContext.reloadContext()
		} catch (e: Exception) {
			Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
		}
	}

	override fun onBackPressed() {
		save()
		setResult(0)
		finish()
	}

	private fun alert(msgId: Int) {
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()
	}

	override fun onStart() {
		super.onStart()
		ActivityManager.activityCount += 1
	}

	override fun onStop() {
		super.onStop()
		ActivityManager.activityCount -= 1
		if (ActivityManager.activityCount == 0)
			ActivityManager.lockApplication()
	}
}
