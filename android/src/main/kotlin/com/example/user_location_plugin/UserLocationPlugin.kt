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

class UserLocationPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity

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
                permissionEventSource = events
            }

            override fun onCancel(arguments: Any?) {
                permissionEventSource = null
            }
        }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, userLocationPlugin)
        channel.setMethodCallHandler(this)
        permissionEventChannel =
            EventChannel(flutterPluginBinding.binaryMessenger, permissionEventChannel)
        permissionEventChannel?.setStreamHandler(permissionStreamHandler)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            getPlatformVersion -> getPlatformVersion(result)
            requestPermission -> requestPermission(result)
            checkPermission -> checkPermission(result)
            else -> result.notImplemented()
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
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
        context?.run {
            activity?.apply {
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

    private fun checkPermission(result: Result): Boolean {
        var permitted = false
        context?.run {
            permitted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            permissionEventSource?.success(
                if (permitted) permissionGranted else permissionEventSource?.success(
                    permissionDenied
                )
            )
        }
        result.success(permitted)
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
        const val checkPermission: String = "checkPermission"
        const val requestPermissionCode: Int = 500
        const val userLocationPlugin: String = "user_location_plugin"
        const val permissionEventChannerl: String = "permission_event_channel"
    }
}
