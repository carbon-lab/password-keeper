package tech.sobin.goalkeeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.*

class FirstUseActivity : AppCompatActivity() {

	private lateinit var editKey: EditText
	private lateinit var editConfirm: EditText
	private lateinit var btnConfirm: Button

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_first_use)
		window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE)
		ActivityManager.push(this)

		editKey = findViewById(R.id.editKey1)
		editConfirm = findViewById(R.id.editConfirm)
		btnConfirm = findViewById(R.id.btnConfirm)

		btnConfirm.setOnClickListener(View.OnClickListener {
			val key = editKey.text.toString()
			val confirm = editConfirm.text.toString()
			if (key != confirm) {
				Toast.makeText(this,
					R.string.key_not_match,
					Toast.LENGTH_LONG)
					.show();
			} else {
				val db = DBOpenHelper.liveDB
				val rs = hexOf(SHA512(key.toByteArray()))
				db?.writableDatabase?.execSQL(
					"INSERT INTO context(password_hash) VALUES ('$rs')"
				)
				setResult(0)
				finish()
			}
		})
	}

	private var lastBackPressed = 0L
	override fun onBackPressed() {
		val now = Date().time
		if (now - lastBackPressed < 1800)
			ActivityManager.finishAllActivity()
		else {
			lastBackPressed = now
			Toast.makeText(
				this,
				R.string.press_more_to_exit,
				Toast.LENGTH_SHORT
			).show()
		}
	}
}
