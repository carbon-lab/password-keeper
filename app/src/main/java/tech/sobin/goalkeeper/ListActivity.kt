package tech.sobin.goalkeeper

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import tech.sobin.json.JSON
import java.lang.Exception
import java.util.*


internal class RecordAdapter: SimpleAdapter {
	constructor(context: Context, items: List<Map<String, Any>>) :
			super(context, items, R.layout.layout_list_record,
				arrayOf("name"), intArrayOf(R.id.textRecordName)) {}
}

class ListActivity : AppCompatActivity() {

	private lateinit var originKey: String

	private lateinit var db: DBOpenHelper
	private lateinit var listRecord: ListView
	private lateinit var btnAdd: Button
	private lateinit var keyHash: ByteArray
	private lateinit var keyText: String

	private var records = arrayListOf<Map<String, String>>()

	@RequiresApi(Build.VERSION_CODES.Q)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_list)
		window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE)
		ActivityManager.push(this)

		originKey = intent.getStringExtra("origin_key")
		keyText = intent.getStringExtra("key_sha512")!!
		keyHash = blobOf(keyText)

		db = DBOpenHelper.liveDB!!

		listRecord = findViewById(R.id.listRecord)
		btnAdd = findViewById(R.id.btnAdd)

		btnAdd.setOnClickListener(View.OnClickListener {
			val intent = Intent(this, EditRecordActivity::class.java)
			intent.putExtra("new", true)
			intent.putExtra("origin_key", originKey)
			startActivityForResult(intent, 1)
		})

		listRecord.onItemClickListener = OnItemClickListener { _, _, position, _ ->
			val record = records[position]
			val intent = Intent(this, EditRecordActivity::class.java)
			intent.putExtra("new", false)
			intent.putExtra("origin_key", originKey)
			intent.putExtra("id", record["id"]?.toInt())
			intent.putExtra("name", record["name"])
			intent.putExtra("username", record["username"])
			intent.putExtra("url", record["url"])
			intent.putExtra("password", record["password"])
			intent.putExtra("comment", record["comment"])
			startActivityForResult(intent, 2)
		}

		listRecord.onItemLongClickListener =
			AdapterView.OnItemLongClickListener { _, _, position, _ ->
				val record = records[position]
				AlertDialog.Builder(this)
					.setTitle(R.string.confirm)
					.setMessage(R.string.alert_delete_confirm)
					.setPositiveButton(R.string.yes) { _, _->
						// Remove
						val id = record["id"]?.toInt()
						db.writableDatabase.delete("record", "id=$id", null)
						refreshList()
					}
					.setNegativeButton(R.string.no, null)
					.show()
				true
			}

		refreshList()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		when (requestCode) {
			1 -> {
				// New record
				if (resultCode == 0) {
					// Saved
					refreshList()
				}
			}
			2 -> {
				// Exists record
				if (resultCode == 0) {
					// Saved
					refreshList()
				}
			}
			3 -> {
				// Setting end
				if (resultCode == 1) {
					// Password changed
					finish()
				}
			}
			4 -> {
				refreshList()
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	private fun refreshList() {
		val rdb = db.readableDatabase
		val res = rdb.rawQuery("SELECT * FROM record", null)
		val list = LinkedList<Map<String, Any>>()
		records = arrayListOf()
		while (res.moveToNext()) {
			try {
				val id = res.getInt(0)
				val body = res.getBlob(1)
				val json = String(DefaultDecrypt(body, originKey.toByteArray())!!)
				val obj = JSON.parse(json)
				list.add(mapOf(
					"name" to obj["name"].toString()
				))
				records.add(mapOf(
					"id" to id.toString(),
					"name" to obj["name"].toString(),
					"username" to obj["username"].toString(),
					"url" to obj["url"].toString(),
					"password" to obj["password"].toString(),
					"comment" to obj["comment"].toString()
				))
			} catch (e: Exception) {
				continue
			}
		}
		list.sortBy { it["name"].toString() }
		records.sortBy { it["name"] }
		val adapter = RecordAdapter(this, list)
		listRecord.adapter = adapter
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

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_list, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.item_setting -> {
				val intent = Intent(this, SettingActivity::class.java)
				startActivityForResult(intent, 3)
			}

			R.id.item_import -> {
				val it = Intent(this, ImportExportActivity::class.java)
				it.putExtra("mode", "IMPORT")
				it.putExtra("origin_key", originKey)
				startActivityForResult(it, 4)
			}

			R.id.item_export -> {
				val it = Intent(this, ImportExportActivity::class.java)
				it.putExtra("mode", "EXPORT")
				startActivityForResult(it, 5)
			}
		}
		return super.onOptionsItemSelected(item)
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
