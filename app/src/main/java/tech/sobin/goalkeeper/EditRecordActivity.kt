package tech.sobin.goalkeeper

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import tech.sobin.json.JSON
import tech.sobin.json.JSObject

class EditRecordActivity : AppCompatActivity() {

	private var isNew: Boolean = true

	private var recordId: Int = -1
	private lateinit var editName: EditText
	private lateinit var editUsername: EditText
	private lateinit var editURL: EditText
	private lateinit var editPassword: EditText
	private lateinit var btnGenKey: Button
	private lateinit var editComment: EditText
	private lateinit var switchLight: Switch

	private var originName: String = ""
	private var originUsername: String = ""
	private var originURL: String = ""
	private var originPassword: String = ""
	private var originComment: String = ""

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_edit_record)
		window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE)
		ActivityManager.push(this)

		isNew = intent?.getBooleanExtra("new", true)!!

		editName = findViewById(R.id.editName)
		editUsername = findViewById(R.id.editUsername)
		editURL = findViewById(R.id.editURL)
		editPassword = findViewById(R.id.editPassword)
		btnGenKey = findViewById(R.id.btnGenKey)
		editComment = findViewById(R.id.editComment)
		switchLight = findViewById(R.id.switchLight)

		btnGenKey.setOnClickListener {
			val key = genKey(
				AppContext.genkeyLength,
				AppContext.genkeyNumber,
				AppContext.genkeyUpper,
				AppContext.genkeyLower,
				AppContext.genkeySymbol)
			editPassword.setText(key)
		}

		switchLight.setOnCheckedChangeListener { _, isChecked ->
			editPassword.inputType =
				if (isChecked)
					InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
				else
					InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
		}

		if (isNew) {
			switchOn()
		} else {
			switchOff()
			recordId = intent.getIntExtra("id", -1)
			originName = intent.getStringExtra("name")
			originUsername = intent.getStringExtra("username")
			originURL = intent.getStringExtra("url")
			originPassword = intent.getStringExtra("password")
			val commentBase64 = intent.getStringExtra("comment")
			val comment = Base64.decode(commentBase64, Base64.DEFAULT)
			originComment = String(comment)
			editName.setText(originName)
			editUsername.setText(originUsername)
			editURL.setText(originURL)
			editPassword.setText(originPassword)
			editComment.setText(originComment)
		}
	}

	private fun switchOn() {
		switchLight.isChecked = true
		editPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
	}

	private fun switchOff() {
		switchLight.isChecked = false
		editPassword.inputType =
			InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
	}

	private fun checkTextChange(): Int {
		var result = 0

		if (editName.text.toString() != originName) result = result.or(1)
		if (editUsername.text.toString() != originUsername) result = result.or(2)
		if (editURL.text.toString() != originURL) result = result.or(4)
		if (editPassword.text.toString() != originPassword) result = result.or(8)
		if (editComment.text.toString() != originComment) result = result.or(16)

		return result
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_edit_record, menu)
		return true
	}

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val change = checkTextChange()
		if (change > 0) {
			val name = editName.text.toString()
			val username = editUsername.text.toString()
			val url = editURL.text.toString()
			val password = editPassword.text.toString()
			val comment = editComment.text.toString()
			if (name.trim().isEmpty()) {
				alert(R.string.alert_name_notnull)
				return false
			}

			if (password.trim().isEmpty()) {
				alert(R.string.alert_passwd_notnull)
				return false
			}

			val obj = JSObject()
			obj.put("name", name)
			obj.put("username", username)
			obj.put("url", url)
			obj.put("password", password)
			obj.put("comment", String(Base64.encode(comment.toByteArray(), Base64.DEFAULT)))
			val json = JSON.stringify(obj)
			val cdata = DefaultEncrypt(json.toByteArray(), blobOf(AppContext.passwordHash))
			val db = DBOpenHelper.liveDB?.writableDatabase
			val cv = ContentValues()
			cv.put("body", cdata)
			if (isNew) {
				// Add new
				db?.insert("record", null, cv)
				setResult(0)
				finish()
			} else {
				// Save old
				db?.update("record", cv, "id=$recordId", null)
				setResult(0)
				finish()
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onBackPressed() {
		val change = checkTextChange()
		if (change > 0) {
			AlertDialog.Builder(this)
				.setTitle(R.string.confirm)
				.setMessage(R.string.alert_text_changed)
				.setPositiveButton(R.string.yes) { _, _->
					setResult(1)
					finish()
				}
				.setNegativeButton(R.string.no, null)
				.show()
		} else {
			setResult(1)
			finish()
		}
	}

	private fun alert(msg: String) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
	}

	private fun alert(msgId: Int) {
		Toast.makeText(this, msgId, Toast.LENGTH_LONG).show()
	}
}
