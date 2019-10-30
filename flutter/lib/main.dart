import 'dart:collection';
import 'dart:io';

import 'package:chatclientflutter/backend/client.dart';
import 'package:chatclientflutter/backend/multicast.dart';
import 'package:chatclientflutter/pages/serverlistpage.dart';
import 'package:chatclientflutter/util/filehandler.dart';
import 'package:chatclientflutter/util/serverdata.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

import 'assets/colors.dart';

void main() => runApp(Main());

class Main extends StatelessWidget {
  static FileHandler _fileHandler;
  static GlobalKey _mainKey;

  static GlobalKey currentKey;

  @override
  Widget build(BuildContext context) {
    _fileHandler = new FileHandler();

    return LoginForm(null);
  }

  static List<ServerData> getServerList() {
    if (fileHandler.serverData == null) {
      fileHandler.serverData = new List<ServerData>();
      fileHandler.serverData
          .add(ServerData("192.168.2.11", 2000, "no wawwasu"));
      fileHandler.serverData.add(ServerData("192.168.3.11", 2005, "no u"));
      fileHandler.serverData
          .add(ServerData("192.168.5.11", 1942, "no easdawu"));
    }

    if (fileHandler.serverData.isEmpty) {
      fileHandler.serverData = new List<ServerData>();
      fileHandler.serverData
          .add(ServerData("192.168.2.11", 2000, "no wawwasu"));
      fileHandler.serverData.add(ServerData("192.168.3.11", 2005, "no u"));
    }

    return fileHandler.serverData;
  }

  static GlobalKey get mainKey => _mainKey;

  static set mainKey(GlobalKey value) {
    _mainKey = value;
  }

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
    }
  }

  Visibility _animationVisiblity;

  String _ip = "";
  int _port = 2000;
  String _password = "";

  set ip(String value) {
    _ip = value;
  }

  void _performLogin() {
    // Validate will return true if the form is valid, or false if
    // the form is invalid.
    // If the form is valid, display a snackbar. In the real world, you'd
    // often want to call a server or save the information in a database
    final context = loginFormHomeKey.currentState.context;
    Client client = Client();
    try {
      client.initializeConnection(new ServerData(_ip, _port, _password));
      setState(() {
        showAnimation = true;
      });
    } on Exception {
      setState(() {
        showAnimation = false;
      });
    }
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

    Widget ipField =
         TextFormField(
          decoration: const InputDecoration(
            hintText: '192.168.2.*',
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

            if (!isNumeric(value.replaceAll('.', ""))) {
              return 'Numeric only';
            }

            try {
              InternetAddress address = InternetAddress(value);
              if (address.host == null) {
                return 'Not a valid ip address';
              }
            } on ArgumentError {
              return 'Not a valid ip address';
            }
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

    Color color = _buildLightTheme().buttonColor;
    Color textColor = _buildLightTheme().primaryTextTheme.display1.color;

    RaisedButton connectButton = new RaisedButton(
      padding: const EdgeInsets.all(8.0),
      color: color,
      textColor: textColor,
      onPressed: _submit,
      child: new Text("Connect"),
    );

    SpinKitCubeGrid animation = SpinKitCubeGrid(
      color: color,
      size: 50.0,
    );

    _animationVisiblity = Visibility(
      child: animation,
      visible: showAnimation,
    );

    HashMap buttonMap = new HashMap<String, Runnable>();

    buttonMap["Search for servers on network"] = Multicast();
    buttonMap["Saved servers"] = new ServerPageRunnable();

    List<String> buttonKeys = buttonMap.keys.toList();

// This menu button widget updates a _selection field (of type String,
// not shown here).
    PopupMenuButton popupMenuButton = PopupMenuButton<String>(
      onSelected: (String result) {
        Runnable runnable = buttonMap[result];
        runnable.run();
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
        theme: _buildLightTheme(),
        darkTheme: _buildDarkTheme(),
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
          body: Padding(
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
                    _animationVisiblity
                  ],
                ),
              )),
        ));
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

ThemeData _buildLightTheme() {
  final ThemeData base = ThemeData.light();
  return base.copyWith(
    primaryColor: kBlue,
    primaryTextTheme: _buildTextTheme(base.primaryTextTheme, kBrown),
    primaryIconTheme: base.iconTheme.copyWith(color: kBrown),
    buttonColor: kBlue,
    accentColor: kBrown,
    scaffoldBackgroundColor: kWhite,
    cardColor: Colors.white,
    textSelectionColor: kBlue,
    errorColor: kError,
    buttonTheme: ButtonThemeData(
      textTheme: ButtonTextTheme.accent,
    ),
    textSelectionHandleColor: kBlue,
    accentTextTheme: _buildTextTheme(base.accentTextTheme, kBrown),
    textTheme: _buildTextTheme(base.textTheme, kBrown),
  );
}

TextTheme _buildTextTheme(TextTheme base, Color color) {
  return base
      .copyWith(
        headline: base.headline.copyWith(
          fontWeight: FontWeight.w500,
        ),
        title: base.title.copyWith(fontSize: 18.0),
        caption: base.caption.copyWith(
          fontWeight: FontWeight.w400,
          fontSize: 14.0,
        ),
      )
      .apply(
        fontFamily: 'Rubik',
        displayColor: color,
        bodyColor: color,
      );
}

ThemeData _buildDarkTheme() {
  final ThemeData base = ThemeData.dark();
  return base.copyWith(
    primaryColor: kShrinePurple,
    primaryTextTheme: _buildTextTheme(base.primaryTextTheme, kShrinePurple),
    primaryIconTheme: base.iconTheme.copyWith(color: kShrineGreen),
    buttonColor: kShrineGreen,
    accentColor: kShrinePurple,
    cardColor: Colors.white,
    textSelectionColor: kWhite,
    errorColor: kError,
    buttonTheme: ButtonThemeData(
      textTheme: ButtonTextTheme.accent,
    ),
    textSelectionHandleColor: kShrinePurple,
    accentTextTheme: _buildTextTheme(base.accentTextTheme, kShrinePurple),
    textTheme: _buildTextTheme(base.textTheme, kShrinePurple),
  );
}

abstract class Runnable {
  void run();
}

class NullRunnable extends Runnable {
  @override
  void run() {}
}

isNumeric(string) => num.tryParse(string) != null;