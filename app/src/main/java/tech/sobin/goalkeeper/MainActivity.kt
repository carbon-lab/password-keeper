package tech.sobin.goalkeeper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

	private lateinit var db: DBOpenHelper

	private lateinit var btnOpen: Button
	private lateinit var editKey: EditText

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE)
		ActivityManager.push(this)

		db = DBOpenHelper(this)
		DBOpenHelper.liveDB = db

		btnOpen = findViewById(R.id.btnOpen)
		editKey = findViewById(R.id.editKey)

		btnOpen.setOnClickListener(View.OnClickListener {
			val key = editKey.text.toString()
			val sha = hexOf(SHA512(key.toByteArray()))
			if (sha == AppContext.passwordHash) {
				editKey.setText("")
				val intent = Intent(this, ListActivity::class.java)
				intent.putExtra("key_sha512", sha)
				intent.putExtra("origin_key", key)
				startActivity(intent)
			} else {
				Toast.makeText(this,
					R.string.wrong_password,
					Toast.LENGTH_SHORT)
					.show()
			}
		})

		if (!usedBefore()) {
			val intent = Intent(this, FirstUseActivity::class.java)
			startActivityForResult(intent, 1)
		} else {
			AppContext.loadContext(db)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			1 -> {
				if (resultCode != 0) ActivityManager.finishAllActivity()
				else AppContext.loadContext(db)
			}
		}
	}

	private fun usedBefore(): Boolean {
		val wdb = db.readableDatabase
		val res = wdb.rawQuery("SELECT COUNT(*) FROM context", null)
		res.moveToFirst()
		val count = res.getInt(0)
		return count == 1
	}

	private var countBackPressed = 0
	override fun onBackPressed() {
		countBackPressed += 1
		if (countBackPressed >= 2)
			ActivityManager.finishAllActivity()
		else
			Toast.makeText(
				this,
				R.string.press_more_to_exit,
				Toast.LENGTH_LONG).show()
	}
}
