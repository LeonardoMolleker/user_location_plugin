import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
import 'package:user_location_plugin/user_location_plugin.dart';
import 'utils/main_page_strings.dart';
import 'widgets/button_widget.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await UserLocationPlugin.platformVersion ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text(
            MainPageStrings.appTitle,
          ),
        ),
        body: Center(
          child: Column(
            children: [
              ButtonWidget(
                onPressed: () {
                  UserLocationPlugin.requestPermission;
                },
                buttonText: MainPageStrings.requestPermissionButtonText,
              ),
              ButtonWidget(
                onPressed: () {
                  UserLocationPlugin.checkPermission;
                },
                buttonText: MainPageStrings.checkPermissionButtonText,
              ),
              StreamBuilder(
                stream: UserLocationPlugin.permissionEventChannelStream,
                builder: (
                  BuildContext context,
                  AsyncSnapshot<dynamic> snapshot,
                ) {
                  return snapshot.hasData
                      ? Text(
                          snapshot.data.toString(),
                        )
                      : Text(
                          MainPageStrings.defaultCheckedPermission,
                        );
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}
