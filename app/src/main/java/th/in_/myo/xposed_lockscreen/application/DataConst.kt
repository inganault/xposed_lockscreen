package th.in_.myo.xposed_lockscreen.application

import com.highcapable.yukihookapi.hook.xposed.prefs.data.PrefsData

object DataConst {
    val LOCKSCREEN_COLOR = PrefsData("lockscreen_color", 0xFFFFFFFF.toInt())
}
