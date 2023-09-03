package th.in_.myo.xposed_lockscreen.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.MembersType
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.java.IntClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import th.in_.myo.xposed_lockscreen.BuildConfig
import th.in_.myo.xposed_lockscreen.application.DataConst

@InjectYukiHookWithXposed(isUsingResourcesHook = false)
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        isEnableModuleAppResourcesCache = false
        isEnableDataChannel = false
        isDebug = BuildConfig.DEBUG
    }

    override fun onHook() = encase {
        loadApp(name = "com.android.systemui") {
            val lockscreenColor = prefs.get(DataConst.LOCKSCREEN_COLOR)

            val target =
                "com.android.systemui.shared.clocks.DefaultClockController\$DefaultClockFaceController"
            if (target.hasClass()) {
                // Lineage 13 Hook
                findClass(target).hook {
                    injectMember {
                        allMembers(MembersType.CONSTRUCTOR)
                        afterHook {
                            val seedColor = field {
                                name = "seedColor"
                                type = IntClass
                            }.get(instance)
                            @Suppress("RedundantNullableReturnType") val to: Int? =
                                lockscreenColor
                            seedColor.set(to)
                        }
                    }
                }
            } else {
                // Emulator (Tiramisu) Hook
                findClass("com.android.systemui.shared.clocks.AnimatableClockView").hook {
                    injectMember {
                        method {
                            name = "animateAppearOnLockscreen"
                        }
                        beforeHook {
                            val lockScreenColor = field {
                                name = "lockScreenColor"
                                type = IntType
                            }.get(instance)
                            lockScreenColor.set(lockscreenColor)
                        }
                    }
                }
            }
        }
    }
}