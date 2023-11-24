package kz.just_code.geoserviceapp

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kz.just_code.geoserviceapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.start.setOnClickListener {
            binding.serviceState.text = "Service is running"
            startService(Intent(this, GeoService::class.java))
            //setTheme(Theme.LIGHT)
        }

        binding.stop.setOnClickListener {
           // setTheme(Theme.DARK)
           binding.serviceState.text = "Service is not running"
            stopService(Intent(this, GeoService::class.java))
        }

        binding.serviceState.text =
            if (this.isServiceRunning(GeoService::class.java)) "Service is running"
            else "Service is not running"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionGranted()) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (!this.hasLocationPermission()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private var permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(
                this,
                if (it) "Permission granted" else "Permission NOT granted",
                Toast.LENGTH_SHORT
            ).show()
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    fun setTheme(theme: Theme) {
        AppCompatDelegate.setDefaultNightMode(theme.system)
    }
}

enum class Theme(val system: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = manager.getRunningServices(10)
    services.forEach {
        if (it.service.className == serviceClass.name) return true
    }
    return false
}