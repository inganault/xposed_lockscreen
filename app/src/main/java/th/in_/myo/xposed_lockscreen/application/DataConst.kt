package th.in_.myo.xposed_lockscreen.application

import com.highcapable.yukihookapi.hook.xposed.prefs.data.PrefsData

object DataConst {
    val LOCKSCREEN_COLOR_OVERRIDE = PrefsData("lockscreen_color_override", false)
    val LOCKSCREEN_COLOR = PrefsData("lockscreen_color", 0xFFFFFFFF.toInt())
    val NOTIFICATION_COLOR_OVERRIDE = PrefsData("notification_color_override", false)
    val NOTIFICATION_COLOR = PrefsData("notification_color", 0xFFFFFFFF.toInt())
}
