package com.gentz.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gentz.launcher.databinding.ActivityLauncherBinding
import com.rtsoft.growtopia.NativeLibraries

class LauncherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.greeting.text = getString(R.string.greeting, LauncherConfig.DEFAULT_USER)
        binding.growtopiaVersion.text =
            getString(R.string.version_value, LauncherConfig.GROWTOPIA_VERSION)
        binding.launcherVersion.text =
            getString(R.string.version_value, LauncherConfig.LAUNCHER_VERSION)
        binding.discordValue.text = LauncherConfig.DISCORD
        binding.roleValue.text = LauncherConfig.DEFAULT_ROLE
        binding.versionChanger.text =
            getString(R.string.version_changer_value, LauncherConfig.GROWTOPIA_VERSION)

        binding.launchButton.setOnClickListener { launchGrowtopia() }
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, PermissionActivity::class.java))
        }
        binding.scriptHubButton.setOnClickListener { comingSoon(R.string.menu_script_hub) }
        binding.myScriptButton.setOnClickListener { comingSoon(R.string.menu_my_script) }
        binding.themePickerButton.setOnClickListener { comingSoon(R.string.menu_theme_picker) }
    }

    private fun comingSoon(labelRes: Int) {
        Toast.makeText(this, getString(R.string.coming_soon, getString(labelRes)), Toast.LENGTH_SHORT)
            .show()
    }

    private fun launchGrowtopia() {
        if (!NativeLibraries.isGameLibraryPresent(this)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.engine_missing_title)
                .setMessage(
                    getString(
                        R.string.engine_missing_message,
                        NativeLibraries.GAME_LIBRARY,
                        LauncherConfig.GROWTOPIA_VERSION
                    )
                )
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }
        val gameIntent = Intent().setClassName(this, "com.rtsoft.growtopia.Main")
            .putExtra("growtopia_version", LauncherConfig.GROWTOPIA_VERSION)
            .putExtra("launcher_version", LauncherConfig.LAUNCHER_VERSION)
            .putExtra("user_name", LauncherConfig.DEFAULT_USER)
            .putExtra("user_role", LauncherConfig.DEFAULT_ROLE)
        startActivity(gameIntent)
    }
}
