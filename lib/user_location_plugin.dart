import 'package:flutter/services.dart';
import 'dart:async';

class UserLocationPlugin {
  static const _permissionEventChannel = EventChannel("permission_event_channel");
  static Stream<dynamic> permissionEventChannelStream = _permissionEventChannel.receiveBroadcastStream();
  
  static const MethodChannel _channel =
  const MethodChannel('user_location_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get initializePlugin async{
    final bool initialize = await _channel.invokeMethod('initializePlugin');
    return initialize;
  }

  static Future<bool> get checkPermission async{
    final bool granted = await _channel.invokeMethod('checkPermission');
    return granted;
  }

  static Future<void> get requestPermission async{
    await _channel.invokeMethod('requestPermission');
    return;
  }

  static Future<Map<dynamic,dynamic>> get lastCoordinates async{
    final Map<dynamic,dynamic> coordinates = await _channel.invokeMethod('lastCoordinates');
    return coordinates;
  }

  static Future<bool> get stopLocation async{
    final bool stopLocation = await _channel.invokeMethod('stopLocation');
    return stopLocation;
  }
}
