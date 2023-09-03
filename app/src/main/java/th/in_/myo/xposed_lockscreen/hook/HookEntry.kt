package th.in_.myo.xposed_lockscreen.hook

import android.graphics.drawable.Drawable
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.MembersType
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.param.PackageParam
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

    private fun PackageParam.hookLockscreen() {
        if (!prefs.get(DataConst.LOCKSCREEN_COLOR_OVERRIDE)) return

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

    private fun PackageParam.hookNotification() {
        if (!prefs.get(DataConst.NOTIFICATION_COLOR_OVERRIDE)) return

        val notificationColor = prefs.get(DataConst.NOTIFICATION_COLOR)
        findClass("com.android.systemui.statusbar.notification.row.NotificationBackgroundView").hook {
            injectMember {
                method {
                    name = "setCustomBackground\$1"
                }
                afterHook {
                    val mBackground = field {
                        name = "mBackground"
                        type = Drawable::class.java
                    }.get(instance).cast<Drawable>()
                    mBackground?.setTint(notificationColor)
                }
            }
        }
    }

    override fun onHook() = encase {
        loadApp(name = "com.android.systemui") {
            hookLockscreen()
            hookNotification()
        }
    }
}