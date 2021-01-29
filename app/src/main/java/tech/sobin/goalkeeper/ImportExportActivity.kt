package tech.sobin.goalkeeper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tech.sobin.crypto.ByteLock
import tech.sobin.json.JSArray
import tech.sobin.json.JSON
import tech.sobin.json.JSObject
import tech.sobin.json.JSString
import java.io.IOException


class ImportExportActivity : AppCompatActivity() {

	private lateinit var mode: String

	private lateinit var textTitle: TextView
	private lateinit var editFilePath: EditText
	private lateinit var btnExec: Button

	private var subActivityOpened = false
	private var originKey = ""

	@SuppressLint("ClickableViewAccessibility")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_import_export)
		window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE)
		ActivityManager.push(this)

		textTitle = findViewById(R.id.textTitle)
		editFilePath = findViewById(R.id.editFilePath)
		btnExec = findViewById(R.id.btnExec)

		mode = intent.getStringExtra("mode")
		when (mode) {
			"IMPORT" -> {
				textTitle.setText(R.string.label_import)
				btnExec.setText(R.string.label_import)
				originKey = intent.getStringExtra("origin_key")
			}
			"EXPORT" -> {
				textTitle.setText(R.string.label_export)
				btnExec.setText(R.string.label_export)
			}
			else -> {
				mode = "IMPORT"
				textTitle.setText(R.string.label_import)
				btnExec.setText(R.string.label_import)
				originKey = intent.getStringExtra("origin_key")
			}
		}

		btnExec.setOnClickListener {
			when (mode) {
				"IMPORT" -> {
					import()
				}
				"EXPORT" -> {
					export()
					setResult(0)
					finish()
				}
			}
		}

		editFilePath.keyListener = null
		editFilePath.setOnTouchListener(object: View.OnTouchListener {
			override fun onTouch(v: View?, event: MotionEvent?): Boolean {
				if (subActivityOpened) return false
				subActivityOpened = true
				if (mode == "IMPORT") {
					val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
					intent.type = "*/*"
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					ActivityManager.activityCount += 1
					startActivityForResult(intent, 1)
				} else if (mode == "EXPORT") {
					val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
					intent.type = "*/*"
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					ActivityManager.activityCount += 1
					startActivityForResult(intent, 1)
				}
				return true
			}
		})
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		when (requestCode) {
			1 -> {
				if (resultCode == Activity.RESULT_OK) {
					try {
						val uri = data?.data!!.toString()
						editFilePath.setText(uri)
						ActivityManager.activityCount -= 1
					} catch (e: Exception) {
						alert(R.string.failed)
					}
				}
			}
		}
		subActivityOpened = false
		super.onActivityResult(requestCode, resultCode, data)
	}

	private fun export() {
		grantExit = false

		try {
			// Get File Path
			val uri = editFilePath.text.toString()
			if (uri.isEmpty()) {
				alert(R.string.alert_choose_path)
				return
			}
			val fos = contentResolver.openOutputStream(Uri.parse(uri), "w")!!
			// Prepare data
			val obj = JSObject()
			obj.put("key_hash", AppContext.passwordHash)
			val jlist = JSArray()
			val rdb = DBOpenHelper.liveDB?.readableDatabase!!
			val all = rdb.rawQuery("SELECT * FROM record", null)
			while (all.moveToNext()) {
				val cdata = all.getBlob(1)
				jlist.push(JSString(String(Base64.encode(cdata, Base64.NO_WRAP))))
			}
			obj.put("records", jlist)
			val output = JSON.stringify(obj).toByteArray()
			// Export
			fos.write(output)
			fos.close()
		} catch (ioe: IOException) {
			alert(R.string.alert_output_error)
		} catch (e: Exception) {
			alert(R.string.failed)
		} finally {
			grantExit = true
		}
	}

	private fun import() {
		grantExit = false

		try {
			// Get File Path
			val uri = editFilePath.text.toString()
			if (uri.isEmpty()) {
				alert(R.string.alert_choose_path)
				return
			}
			val fis = contentResolver.openInputStream(Uri.parse(uri))!!
			val fsize = fis.available()
			if (fsize > 1073741824) throw Exception()
			val buffer = ByteArray(fsize.toInt())
			fis.read(buffer)
			val json = String(buffer)
			val obj = JSON.parse(json)
			val keyHash = obj["key_hash"].toString()
			val records = obj["records"] as JSArray

			val input = EditText(this)
			input.inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
			AlertDialog.Builder(this)
				.setTitle(R.string.label_input_password)
				.setView(input)
				.setPositiveButton(R.string.yes) { _, _ ->
					val inputKey = input.text.toString()
					val inputHash = hexOf(SHA512(inputKey.toByteArray()))
					if (keyHash != inputHash) {
						alert(R.string.wrong_password)
					} else {
						val itslock = ByteLock(inputKey.toByteArray())
						val mylock = ByteLock(originKey.toByteArray())
						for (e in records.array) {
							e.value ?: continue
							val base64 = e.value.toString()
							val blob = Base64.decode(base64, Base64.NO_WRAP)
							val de = itslock.decrypt(blob)
							val en = mylock.encrypt(de)
							DBOpenHelper.addRecord(en)
						}
					}
				}
				.setNegativeButton(R.string.no, null)
				.show()

		} catch (ioe: IOException) {
			alert(R.string.alert_input_error)
		} catch (e: Exception) {
			alert(R.string.failed)
		} finally {
			grantExit = true
		}
	}

	private var grantExit = true
	override fun onBackPressed() {
		if (grantExit) super.onBackPressed()
	}

	override fun finish() {
		if (grantExit) super.finish()
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
