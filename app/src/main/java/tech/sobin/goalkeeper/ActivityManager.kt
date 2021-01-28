package tech.sobin.goalkeeper

import android.app.Activity
import java.util.*

class ActivityManager {
	companion object {
		private val activityStack = LinkedList<Activity>()

		fun push(activity: Activity) {
			activityStack.push(activity)
		}

		fun pop(): Activity? {
			return activityStack.pop()
		}

		fun finishAllActivity() {
			while (activityStack.size > 0) {
				val activity = activityStack.pop()
				if (!activity.isFinishing)
					activity.finish()
			}
		}

		fun lockApplication() {
			for (a in activityStack) {
				if (!a.isFinishing && a !is MainActivity)
					a.finish()
			}
		}

		var activityCount = 0
	}
}