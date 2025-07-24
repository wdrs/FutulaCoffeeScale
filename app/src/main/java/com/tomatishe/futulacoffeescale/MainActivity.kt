package com.tomatishe.futulacoffeescale

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.tomatishe.futulacoffeescale.databinding.ActivityMainBinding
import com.tomatishe.futulacoffeescale.coordinators.dataCoordinator.DataCoordinator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel

private const val PERMISSION_REQUEST_CODE = 1

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private fun MainActivity.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this, permissionType
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Determine whether the current [Activity] has been granted the relevant permissions to perform
     * Bluetooth operations depending on the mobile device's Android version.
     */
    private fun MainActivity.hasRequiredBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun MainActivity.requestRelevantRuntimePermissions() {
        if (hasRequiredBluetoothPermissions()) {
            return
        }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }

            true -> {
                requestBluetoothPermissions()
            }
        }
    }

    private fun requestLocationPermission() = runOnUiThread {
        AlertDialog.Builder(this).setTitle(getString(R.string.location_permission_required))
            .setMessage(
                getString(R.string.location_permission_required_text)
            ).setCancelable(false).setPositiveButton(android.R.string.ok) { _, _ ->
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE
            )
        }.show()
    }

    private fun requestBluetoothPermissions() = runOnUiThread {
        AlertDialog.Builder(this).setTitle(getString(R.string.bluetooth_permission_required))
            .setMessage(
                getString(R.string.bluetooth_permission_required_text)
            ).setCancelable(false).setPositiveButton(android.R.string.ok) { _, _ ->
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
                ), PERMISSION_REQUEST_CODE
            )
        }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST_CODE) return
        val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
            it.second == PackageManager.PERMISSION_DENIED && !ActivityCompat.shouldShowRequestPermissionRationale(
                this, it.first
            )
        }
        val containsDenial = grantResults.any { it == PackageManager.PERMISSION_DENIED }
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when {
            containsPermanentDenial -> {
                // TODO: Handle permanent denial (e.g., show AlertDialog with justification)
                // Note: The user will need to navigate to App Settings and manually grant
                // permissions that were permanently denied
            }

            containsDenial -> {
                requestRelevantRuntimePermissions()
            }

            allGranted && hasRequiredBluetoothPermissions() -> {
                // startBleScan()
            }

            else -> {
                // Unexpected scenario encountered when handling permissions
                recreate()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }

        // Init database
        Dependencies.init(applicationContext)

        // Init datastore
        DataCoordinator.shared.initialize(
            context = applicationContext,
            onLoad = {
                Log.d("INFO", "LOADED DATASTORE")
            }
        )

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_history, R.id.nav_settings, R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        if (!hasRequiredBluetoothPermissions()) {
            requestRelevantRuntimePermissions()
        }
    }

    // override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    //     menuInflater.inflate(R.menu.main, menu)
    //     return true
    // }

    override fun onDestroy() {
        try {
            GlobalScope.cancel()
        } catch (exception: Exception) {
            Log.d("INFO", "NO ACTIVE ACTIVITY, IGNORE")
        }
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}