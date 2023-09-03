@file:Suppress("SetTextI18n")

package th.in_.myo.xposed_lockscreen.ui.activity

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.prefs
import com.highcapable.yukihookapi.hook.xposed.prefs.YukiHookPrefsBridge
import com.highcapable.yukihookapi.hook.xposed.prefs.data.PrefsData
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import th.in_.myo.xposed_lockscreen.BuildConfig
import th.in_.myo.xposed_lockscreen.R
import th.in_.myo.xposed_lockscreen.application.DataConst
import th.in_.myo.xposed_lockscreen.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    /**
     * Get the binding layout object
     */
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsBridge: YukiHookPrefsBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Hide Activity title bar
        supportActionBar?.hide()
        // Init immersive status bar
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ResourcesCompat.getColor(resources, R.color.colorThemeBackground, null).also {
            window?.statusBarColor = it
            window?.navigationBarColor = it
            window?.navigationBarDividerColor = it
        }

        refreshModuleStatus()
        binding.mainTextVersion.text = getString(R.string.module_version, BuildConfig.VERSION_NAME)
        binding.hideIconInLauncherSwitch.isChecked = isLauncherIconShowing.not()
        binding.hideIconInLauncherSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) hideOrShowLauncherIcon(isChecked)
        }

        binding.buttonRestart.setOnClickListener {
            Runtime.getRuntime().exec("su -c pkill systemui -9")
        }

        prefsBridge = applicationContext.prefs()
        makePicker(binding.buttonPickLockscreen, DataConst.LOCKSCREEN_COLOR)
        makePicker(binding.buttonPickNotification, DataConst.NOTIFICATION_COLOR)

        binding.switchOverrideLockscreenColor.isChecked =
            prefsBridge.get(DataConst.LOCKSCREEN_COLOR_OVERRIDE)
        binding.switchOverrideLockscreenColor.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed)
                prefsBridge.edit {
                    put(DataConst.LOCKSCREEN_COLOR_OVERRIDE, isChecked)
                }
        }
        binding.switchOverrideNotificationColor.isChecked =
            prefsBridge.get(DataConst.NOTIFICATION_COLOR_OVERRIDE)
        binding.switchOverrideNotificationColor.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed)
                prefsBridge.edit {
                    put(DataConst.NOTIFICATION_COLOR_OVERRIDE, isChecked)
                }
        }
    }

    private fun makePicker(btn: Button, pref: PrefsData<Int>) {
        btn.setBackgroundColor(prefsBridge.get(pref))
        btn.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setPositiveButton("Choose",
                    object : ColorEnvelopeListener {
                        override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                            val color = envelope!!.color
                            prefsBridge.edit {
                                put(pref, color)
                            }
                            btn.setBackgroundColor(color)
                        }
                    })
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .apply {
                    colorPickerView.setInitialColor(prefsBridge.get(pref))
                }
                .setBottomSpace(12)
                .show()
        }
    }

    /**
     * Hide or show launcher icons
     *
     * - You may need the latest version of LSPosed to enable the function of hiding launcher
     *   icons in higher version systems
     *
     * @param isShow Whether to display
     */
    private fun hideOrShowLauncherIcon(isShow: Boolean) {
        packageManager?.setComponentEnabledSetting(
            ComponentName(packageName, "${BuildConfig.APPLICATION_ID}.Home"),
            if (isShow) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Get launcher icon state
     *
     * @return [Boolean] Whether to display
     */
    private val isLauncherIconShowing
        get() = packageManager?.getComponentEnabledSetting(
            ComponentName(packageName, "${BuildConfig.APPLICATION_ID}.Home")
        ) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED

    /**
     * Refresh module status
     */
    private fun refreshModuleStatus() {
        binding.mainLinStatus.setBackgroundResource(
            when {
                YukiHookAPI.Status.isModuleActive -> R.drawable.bg_green_round
                else -> R.drawable.bg_dark_round
            }
        )
        binding.mainImgStatus.setImageResource(
            when {
                YukiHookAPI.Status.isModuleActive -> R.mipmap.ic_success
                else -> R.mipmap.ic_warn
            }
        )
        binding.mainTextStatus.text = getString(
            when {
                YukiHookAPI.Status.isModuleActive -> R.string.module_is_activated
                else -> R.string.module_not_activated
            }
        )
        binding.mainTextApiWay.isVisible = YukiHookAPI.Status.isModuleActive
        binding.mainTextApiWay.text = if (YukiHookAPI.Status.Executor.apiLevel > 0)
            "Activated by ${YukiHookAPI.Status.Executor.name} API ${YukiHookAPI.Status.Executor.apiLevel}"
        else "Activated by ${YukiHookAPI.Status.Executor.name}"
    }
}