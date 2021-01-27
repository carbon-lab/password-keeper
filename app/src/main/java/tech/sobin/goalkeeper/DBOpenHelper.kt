package tech.sobin.goalkeeper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper: SQLiteOpenHelper {

	companion object {
		var liveDB: DBOpenHelper? = null
	}

	constructor(context: Context?) :
			super(context, "storage.db", null, 1) {
	}

	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(
			"""
				CREATE TABLE IF NOT EXISTS context (
					password_hash TEXT NOT NULL,
					genkey_length INT NOT NULL DEFAULT 8,
					genkey_number INT NOT NULL DEFAULT 1,
					genkey_upper INT NOT NULL DEFAULT 1,
					genkey_lower INT NOT NULL DEFAULT 1,
					genkey_symbol INT NOT NULL DEFAULT 0
				)
			""".trimIndent()
		)
		db.execSQL(
			"""
                CREATE TABLE IF NOT EXISTS record (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    body BLOB NOT NULL
                )
            """.trimIndent()
		)
	}

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
		// Nothing to do
	}
}