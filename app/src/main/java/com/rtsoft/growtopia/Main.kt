package com.rtsoft.growtopia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gentz.launcher.databinding.ActivityGrowtopiaMainBinding

class Main : AppCompatActivity() {
    private lateinit var binding: ActivityGrowtopiaMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrowtopiaMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loaded = runCatching { System.loadLibrary("growtopia") }.isSuccess
        val version = intent.getStringExtra("growtopia_version").orEmpty()
        val role = intent.getStringExtra("user_role").orEmpty()
        binding.gameStatus.text = if (loaded) {
            "libgrowtopia.so loaded\n$version\n$role"
        } else {
            "Waiting for packaged native libraries in jniLibs/arm64-v8a"
        }
    }
}
