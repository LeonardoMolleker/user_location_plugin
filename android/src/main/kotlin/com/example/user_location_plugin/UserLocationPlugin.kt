package com.example.user_location_plugin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

class UserLocationPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private var permissionEventChannel: EventChannel? = null
    private var permissionEventSource: EventChannel.EventSink? = null
    private var permissionStreamHandler: EventChannel.StreamHandler =
        object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                permissionEventSource = events
            }

            override fun onCancel(arguments: Any?) {
                permissionEventSource = null
            }
        }

    private var locationEventChannel: EventChannel? = null
    private var locationEventSource: EventChannel.EventSink? = null
    private var locationStreamHandler: EventChannel.StreamHandler =
        object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                locationEventSource = events
            }

            override fun onCancel(arguments: Any?) {
                locationEventSource = null
            }
        }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, userLocationPlugin)
        channel.setMethodCallHandler(this)
        permissionEventChannel =
            EventChannel(flutterPluginBinding.binaryMessenger, permissionEventChanel)
        permissionEventChannel?.setStreamHandler(permissionStreamHandler)
        locationEventChannel =
            EventChannel(flutterPluginBinding.binaryMessenger, locationEventChanel)
        locationEventChannel?.setStreamHandler(locationStreamHandler)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            getPlatformVersion -> getPlatformVersion(result)
            requestPermission -> requestPermission(result)
            checkPermission -> checkPermission(result)
            initializePlugin -> initializePlugin(result)
            startListeningLocation -> startListeningLocation(result)
            stopListeningLocation -> stopListeningLocation(result)
            else -> result.notImplemented()
        }
    }

    private fun initializePlugin(result: Result) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        val permission = permissionGranted()
        if(permission){
            locationRequest = LocationRequest.create()
            locationRequest?.interval = 3000
            locationRequest?.priority = PRIORITY_HIGH_ACCURACY
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationEventSource?.success(longitudDisplay + locationResult?.lastLocation?.longitude.toString() + ", " + latitudDisplay + locationResult?.lastLocation?.latitude.toString())
                }
            }
        }else{
            locationEventSource?.success(locationFail)
        }
        result.success(permission)
    }

    private fun startListeningLocation(result: Result){
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        result.success(true)
    }

    private fun stopListeningLocation(result: Result) {
        context.run {
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }
        locationEventSource?.success(stopListeningResponse)
        result.success(true)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.i("onDetachedFromActivity", "Not implemented")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.i("nReattachedToActivity", "Not implemented")
    }

    override fun onDetachedFromActivity() {
        Log.i("onDetachedFromActivity", "Not implemented")
    }

    private fun getPlatformVersion(result: Result) {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }

    private fun requestPermission(result: Result) {
        context.run {
            activity.apply {
                requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    requestPermissionCode
                )
            }
        }
        result.success(null)
    }

    private fun checkPermission(result: Result) {
        val permitted = permissionGranted()
        permissionEventSource?.success(if (permitted) permissionGranted else permissionDenied)
        result.success(permitted)
    }

    private fun permissionGranted(): Boolean{
        var permitted: Boolean
        context.run {
            permitted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        return permitted
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.i("onActivityResult", "Not implemented")
        return true
    }

    companion object PluginConstants {
        const val permissionGranted: String = "Permission granted"
        const val permissionDenied: String = "Permission denied"
        const val getPlatformVersion: String = "getPlatformVersion"
        const val requestPermission: String = "requestPermission"
        const val startListeningLocation: String = "startListeningLocation"
        const val stopListeningLocation: String = "stopListeningLocation"
        const val stopListeningResponse: String = "You stop the plugin"
        const val initializePlugin: String = "initializePlugin"
        const val checkPermission: String = "checkPermission"
        const val requestPermissionCode: Int = 500
        const val userLocationPlugin: String = "user_location_plugin"
        const val permissionEventChanel: String = "permission_event_channel"
        const val locationEventChanel: String = "location_event_channel"
        const val latitudDisplay: String = "Latitud: "
        const val longitudDisplay: String = "Longitud: "
        const val locationFail: String = "Not permission granted"
    }
}
