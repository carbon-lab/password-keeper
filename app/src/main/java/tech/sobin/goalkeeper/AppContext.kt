package tech.sobin.goalkeeper

class AppContext {
	companion object {
		lateinit var passwordHash: String
		var genkeyLength = 8
		var genkeyNumber: Boolean = true
		var genkeyUpper: Boolean = true
		var genkeyLower: Boolean = true
		var genkeySymbol: Boolean = false

		private lateinit var source: DBOpenHelper

		fun loadContext(db: DBOpenHelper) {
			source = db
			reloadContext()
		}

		fun reloadContext() {
			val db = source
			val rdb = db.readableDatabase
			val context = rdb.rawQuery("SELECT * FROM context", null)
			if (context.moveToFirst()) {
				passwordHash = context.getString(0)
				genkeyLength = context.getInt(1)
				genkeyNumber = context.getInt(2) != 0
				genkeyUpper = context.getInt(3) != 0
				genkeyLower = context.getInt(4) != 0
				genkeySymbol = context.getInt(5) != 0
			}
		}
	}
}