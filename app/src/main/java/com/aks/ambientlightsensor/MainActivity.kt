package com.aks.ambientlightsensor

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var ambientLight: Sensor? = null
    private var switch: Switch? = null
    private var btnIncrease: Button? = null
    private var btnDecrease: Button? = null

    // Variable to store brightness value
    private var brightness = 0

    // Content resolver used as a handle to the system's settings
    private var cResolver: ContentResolver? = null

    private var name : String?="Anil"

    // Window object, that will store a reference to the current window
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        switch = findViewById(R.id.switch1)
        btnIncrease = findViewById(R.id.increase)
        btnDecrease = findViewById(R.id.decrease)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        ambientLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        btnDecrease?.isEnabled = false
        btnIncrease?.setOnClickListener {
            flashLightOperation()
            btnDecrease?.isEnabled = true
        }

        btnDecrease?.setOnClickListener {

            Log.e(
                "TAG", "Current Brightness: ${
                    Settings.System.getInt(
                        cResolver, Settings.System.SCREEN_BRIGHTNESS
                    )
                }"
            )
            Settings.System.putInt(
                cResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness
            )
        }
        println(name?.length)
        name = null
        println(name!!.length)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val millibarsOfPressure = event?.values?.get(0)
        Log.e("TAG", "onSensorChanged: $millibarsOfPressure")
        if (millibarsOfPressure != null) {
            switch?.isChecked = millibarsOfPressure < 60
        }
    }

    override fun onAccuracyChanged(event: Sensor?, accuracy: Int) {
        Log.e("TAG", "onAccuracyChanged: $accuracy")
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, ambientLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun flashLightOperation() {
        if (permissionEnabled()) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cResolver = contentResolver
                try {
                    // To handle the auto
                    Settings.System.putInt(
                        cResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    )
                    // Get the current system brightness
                    brightness = Settings.System.getInt(
                        cResolver, Settings.System.SCREEN_BRIGHTNESS
                    )

                    if (brightness < 255) {
                        Settings.System.putInt(
                            cResolver,
                            Settings.System.SCREEN_BRIGHTNESS,
                            255
                        )
                    }
                    Log.e("TAG", "Current Brightness: $brightness")
                } catch (e: SettingNotFoundException) {
                    // Throw an error case it couldn't be retrieved
                    Log.e("Error", "Cannot access system brightness")
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Flash light not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            //requestPermission
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            permissionLauncher.launch(intent)
        }
    }

    private fun permissionEnabled(): Boolean {
        return Settings.System.canWrite(this)
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            flashLightOperation()
        }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}