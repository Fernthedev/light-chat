import 'dart:collection';
import 'dart:io';

import 'package:device_info/device_info.dart';
import 'package:flutter/material.dart';
import 'package:light_chat_client/client.dart';
import 'package:light_chat_client/data/serverdata.dart';
import 'package:light_chat_client/multicast.dart';
import 'package:light_chat_client/packets/handshake_packets.dart';
import 'package:light_chat_client/variables.dart';
import 'package:light_chat_client_flutter/pages/chatpage.dart';
import 'package:light_chat_client_flutter/pages/serverlistpage.dart';
import 'package:light_chat_client_flutter/util/filehandler.dart';

import 'assets/colors.dart';

void main() => runApp(Main());

class Main extends StatelessWidget {
  Main() {
    Variables.debug = true; // TODO: Remove when finish debugging

    print("Main debug mode: ${Variables.debug}");
  }

  static FileHandler _fileHandler;
  static GlobalKey _mainKey;

  static Client client;

  static GlobalKey currentKey;
  static DeviceInfoPlugin deviceInfo;

  static Future<String> getDeviceName() async {
    if (Platform.isAndroid)
      return await deviceInfo.androidInfo.then((info) => info.androidId);

    if (Platform.isIOS)
      return await deviceInfo.iosInfo.then((info) => info.model);

    throw "Not running on a device";
  }

  @override
  Widget build(BuildContext context) {
    _fileHandler = new FileHandler();
    _fileHandler.getFiles().then((_) {
      _fileHandler.setToDefaultIfNecessary();
    });

    deviceInfo = DeviceInfoPlugin();

    return LoginForm(null);
  }

  static GlobalKey get mainKey => _mainKey;
//
//  static set mainKey(GlobalKey value) {
//    _mainKey = value;
//  }

  static FileHandler get fileHandler => _fileHandler;
}

class LoginForm extends StatefulWidget {
  final ServerData _serverData;

  LoginForm(this._serverData);

  @override
  State<StatefulWidget> createState() {
    return LoginFormState(_serverData);
  }
}

class LoginFormState extends State<LoginForm> {
  final formKey = GlobalKey<FormState>();
  final currentKey = GlobalKey<LoginFormState>();
  final loginFormHomeKey = GlobalKey<ScaffoldState>();

  static const routeName = "/loginform";

  bool showAnimation = false;

  LoginFormState(ServerData serverData) {
    if (serverData != null) {
      _ip = serverData.ip;
      _port = serverData.port;
      _password = serverData.hashedPassword;

      print("Password is $_password");
    }
  }

  String _ip = "192.168.0.17";
  int _port = 2000;
  String _password = "";

  set ip(String value) {
    _ip = value;
  }

  Future<void> _performLogin() async {
    // Validate will return true if the form is valid, or false if
    // the form is invalid.
    // If the form is valid, display a snackbar. In the real world, you'd
    // often want to call a server or save the information in a database
    final context = loginFormHomeKey.currentState.context;

    ServerData serverData = ServerData(_ip, _port, _password);

    Main.client = Client(ConnectedPacket.create(
        await Main.getDeviceName(),
        Platform.operatingSystem,
        Variables.versionData,
        "Flutter ${Variables.defaultLangFramework}"));
    try {
      Main.client.onError((e) => {
            setState(() {
              showAnimation = false;
            })
          });
      Main.client.initializeConnection(serverData).catchError((e) => {throw e});
      setState(() {
        showAnimation = true;
      });
    } on Exception {
      setState(() {
        showAnimation = false;
      });
    }

    Main.client.onConnect(onConnect);
  }

  void onConnect(ServerData serverData) {
    BuildContext context = Main.mainKey.currentState.context;
    setState(() {
      showAnimation = false;
    });
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => ChatPage(serverData)),
    );
  }

  void _submit() {
    final form = formKey.currentState;

    if (form.validate()) {
      form.save();

      // Email & password matched our validation rules
      // and are saved to _email and _password fields.
      _performLogin();
    }
  }

  @override
  Widget build(BuildContext context) {
    if (ModalRoute.of(context) != null) {
      // Extract the arguments from the current ModalRoute settings and cast
      // them as ScreenArguments.
      final ServerData args = ModalRoute.of(context).settings.arguments;

      setState(() {
        _ip = args.ip;
        _password = args.hashedPassword;
        _port = args.port;
      });
    }

    Widget ipField = TextFormField(
      decoration: const InputDecoration(
        hintText: '192.168.0.*',
        labelText: 'IP Address',
        border: OutlineInputBorder(),
      ),
      initialValue: _ip,
      onSaved: (String value) {
        _ip = value;
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      validator: (String value) {
        if (value.isEmpty) {
          return 'Please enter an ip address';
        }

        if (value == "localhost") return null;

        try {
          InternetAddress address = InternetAddress(value);
          if (address.host == null) {
            return 'Not a valid ip address';
          }
        } on ArgumentError {
          return 'Not a valid ip address';
        }

        if (!isNumeric(value.replaceAll('.', ""))) {
          return 'Numeric only';
        }
        return null;
      },
      keyboardType:
          TextInputType.numberWithOptions(signed: false, decimal: true),
      autocorrect: false,
    );

    Widget portField = TextFormField(
      decoration: const InputDecoration(
        hintText: '2000',
        labelText: 'Port',
        border: OutlineInputBorder(),
      ),
      onSaved: (String value) {
        _port = int.parse(value);
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      validator: (String value) {
        return isNumeric(value) == false ? 'Not a valid port' : null;
      },
      keyboardType: TextInputType.number,
      initialValue: _port.toString(),
    );

    Widget passwordField = TextFormField(
      decoration: const InputDecoration(
        hintText: 'Password (Optional)',
        labelText: 'Password',
        border: OutlineInputBorder(),
      ),
      onSaved: (String value) {
        _password = value;
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      obscureText: true,
      initialValue: _password,
    );

    Color color = buildLightTheme().buttonColor;
    Color textColor = buildLightTheme().primaryTextTheme.display1.color;

    RaisedButton connectButton = new RaisedButton(
      padding: const EdgeInsets.all(8.0),
      color: color,
      textColor: textColor,
      onPressed: _submit,
      child: new Text("Connect"),
    );

    CircularProgressIndicator animation = CircularProgressIndicator();
    Visibility _animationVisibility = Visibility(
      child: animation,
      visible: showAnimation,
    );

    HashMap buttonMap = HashMap<String, Function()>();

    buttonMap["Search for servers on network"] = Multicast().startChecking;
    buttonMap["Saved servers"] = ServerPageRunnable().run;

    /// This is for debugging
//    buttonMap["Chat page"] = () {
//      BuildContext context = Main.mainKey.currentState.context;
//      Navigator.push(
//        context,
//        MaterialPageRoute(builder: (context) =>
//            ChatPage(
//                ServerData("n",
//                  2000,
//                  null,
//                )
//            )
//        ),
//      );
//    };

    List<String> buttonKeys = buttonMap.keys.toList();

// This menu button widget updates a _selection field (of type String,
// not shown here).
    PopupMenuButton popupMenuButton = PopupMenuButton<String>(
      onSelected: (String result) {
        Function runnable = buttonMap[result];
        runnable();
//        runnable.run();
      },
      itemBuilder: (BuildContext context) =>
          buttonKeys.map<PopupMenuItem<String>>((String value) {
        return PopupMenuItem<String>(
          value: value,
          child: Text(value),
        );
      }).toList(),
      //icon: Icon(Icons.)
    );

    Main._mainKey = formKey;
    Main.currentKey = currentKey;

    return MaterialApp(

        // Provide a function to handle named routes. Use this function to
        // identify the named route being pushed and create the correct
        // Screen.
        onGenerateRoute: (settings) {
          // If you push the PassArguments route
          if (settings.name == ServerPage.routeName) {
            // Cast the arguments to the correct type: ScreenArguments.
            final ServerData args = settings.arguments;

            // Then, extract the required data from the arguments and
            // pass the data to the correct screen.
            return MaterialPageRoute(
              builder: (context) {
                return LoginForm(args);
              },
            );
          }
        },
        theme: buildLightTheme(),
        darkTheme: buildDarkTheme(),
        title: 'ServerLogin',
        key: currentKey,
        home: Scaffold(
            key: loginFormHomeKey,
            appBar: AppBar(
              title: Text('Server Login'),
              actions: <Widget>[
                // Add 3 lines from here...
                popupMenuButton,
              ],
            ),
            body: Stack(children: <Widget>[
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Form(
                  key: formKey,
                  child: ListView(
                    children: [
                      SizedBox(height: 10.0),
                      ipField,
                      SizedBox(height: 12.0),
                      portField,
                      SizedBox(height: 12.0),
                      passwordField,
                      SizedBox(height: 12.0),
                      connectButton,
                    ],
                  ),
                ),
              ),
              Align(
                child: _animationVisibility,
                alignment: FractionalOffset.center,
              )
            ])));
  }

  void setServerState(ServerData serverData) {
    setState(() {
      ip = serverData.ip;
      port = serverData.port;
      password = serverData.hashedPassword;
    });
  }

  set port(int value) {
    _port = value;
  }

  set password(String value) {
    _password = value;
  }
}

abstract class Runnable {
  void run();
}

class NullRunnable extends Runnable {
  @override
  void run() {}
}

isNumeric(string) => num.tryParse(string) != null;
