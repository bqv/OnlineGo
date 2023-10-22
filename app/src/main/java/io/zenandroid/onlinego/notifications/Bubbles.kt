package io.zenandroid.onlinego.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log

private const val TAG = "Bubbles"

object Bubbles {
  // The minimum version to use Notification Bubbles.
  // Acceptable values are 29 (requires Dev Options), 30, and 999 (disabled).
  const val MIN_SDK_BUBBLES: Int = 30

  // NotificationManager#areBubblesAllowed does not check if bubbles have been globally disabled,
  // (verified on R), so we use this check as well. Luckily, ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS
  // works well for both cases.
  fun bubblesEnabledGlobally(context: Context): Boolean = if (Build.VERSION.SDK_INT < MIN_SDK_BUBBLES) {
    false
  } else if (Build.VERSION.SDK_INT >= 30) {
    // In R+, the system setting is stored in Global.
    Settings.Global.getInt(context.getContentResolver(), "notification_bubbles", 1) == 1
  } else {
    // In Q, the system setting is stored in Secure.
    Settings.Secure.getInt(context.getContentResolver(), "notification_bubbles", 1) == 1
  }

  fun bubblesEnabledLocally(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < MIN_SDK_BUBBLES) {
      return false
    }

    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    //return notificationManager.areBubblesAllowed()
    return notificationManager.getBubblePreference() != NotificationManager.BUBBLE_PREFERENCE_NONE
  }

  fun canDisplayBubbles(context: Context, notificationChannelId: String): Boolean {
    if (Build.VERSION.SDK_INT < MIN_SDK_BUBBLES) {
      return false
    }

    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    Log.v(TAG, "Bubbles are ${if (bubblesEnabledGlobally(context)) "" else "not "}enabled globally")
    Log.v(TAG, "Bubbles are ${if (bubblesEnabledLocally(context)) "" else "not "}enabled locally")

    // This boolean is supposed to be set to map to the current state of the Bubble notification.
    // True when the notification is displayed as a bubble and false when it's displayed as a notification.
    // This is set inside of BubbleController#onUserChangedBubble. However, every time I query this, it returns false.
    val channelCanBubble = notificationManager.getNotificationChannel(notificationChannelId).canBubble()
    Log.v(TAG, "Bubbles are ${if (channelCanBubble) "" else "not "}enabled for channel")

    return bubblesEnabledGlobally(context) && bubblesEnabledLocally(context)
  }
}
