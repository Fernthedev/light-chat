import 'dart:io';

import 'package:flutter/material.dart';
import 'package:light_chat_client/data/serverdata.dart';
import 'package:light_chat_client/util/encryption/encryption.dart';
import 'package:light_chat_client_flutter/assets/colors.dart';
import 'package:light_chat_client_flutter/main.dart';

class ServerEditPage extends StatelessWidget {
  ServerData _serverData;
  ServerData _originalServerData;
  static bool difModified = false;

  TextEditingController _ipDataController = new TextEditingController();

  TextEditingController _portDataController = new TextEditingController();

  TextEditingController _passDataController = new TextEditingController();

  static const routeName = "/servereditpage";

  var _scaffoldKey = new GlobalKey<ScaffoldState>();

  @override
  Widget build(BuildContext context) {
    // Extract the arguments from the current ModalRoute settings and cast
    // them as ScreenArguments.
    final List<Object> objects = ModalRoute.of(context).settings.arguments;
    _serverData = (Main.fileHandler.serverDataMap)[objects[0]];

    if (_serverData == null)
      throw ArgumentError(
          "Uuid ${objects[0]} does not seem to be assigned to server data.");

    _originalServerData = ServerData.fromServer(_serverData);

    _ipDataController.text = _originalServerData.ip;
    TextFormField ipField = TextFormField(
      decoration: const InputDecoration(
        hintText: '192.168.2.*',
        labelText: 'IP Address',
      ),
      onSaved: (String value) {
        _serverData.ip = value;
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      controller: _ipDataController,
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

        return "";
      },
      keyboardType:
          TextInputType.numberWithOptions(signed: false, decimal: true),
      autocorrect: false,
    );

    _portDataController.text = _serverData.port.toString();
    TextFormField portField = TextFormField(
      decoration: const InputDecoration(
        hintText: '2000',
        labelText: 'Port',
      ),
      controller: _portDataController,
      onSaved: (String value) {
        _serverData.port = int.parse(value);
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      validator: (String value) {
        return isNumeric(value) == false ? 'Not a valid port' : null;
      },
      keyboardType: TextInputType.number,
    );

    _passDataController.text = _serverData.hashedPassword;

    TextFormField passwordField = TextFormField(
      decoration: const InputDecoration(
        hintText: 'Password (Optional)',
        labelText: 'Password',
      ),
      controller: _passDataController,
      onSaved: (String value) {
        _serverData.hashedPassword = value;
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      obscureText: true,
      autocorrect: false,
      enableInteractiveSelection: true,
      validator: (String val) {
        return null;
      },
    );

    Color color = Theme.of(context).primaryColor;

    RaisedButton saveButton = new RaisedButton(
      padding: const EdgeInsets.all(8.0),
      textColor: Colors.white,
      color: color,
      onPressed: saveFile,
      child: new Text("Save"),
    );

    return MaterialApp(
      theme: buildLightTheme(),
      darkTheme: buildDarkTheme(),
      // Provide a function to handle named routes. Use this function to
      // identify the named route being pushed and create the correct
      // Screen.
      onGenerateRoute: (settings) {
        // If you push the PassArguments route
        if (settings.name == routeName) {
          // Cast the arguments to the correct type: ScreenArguments.
          final List<Object> args = settings.arguments;

          ServerData serverData = args[0];

          // Then, extract the required data from the arguments and
          // pass the data to the correct screen.
          return MaterialPageRoute(
            builder: (context) {
              ServerEditPage page = ServerEditPage();
              // page.index = index;
              // page._serverData = serverData;
              return page;
            },
          );
        }
      },

      title: 'Edit server',
      home: Scaffold(
          key: _scaffoldKey,
          appBar: AppBar(
            title: Text('Edit server'),
            actions: <Widget>[
              IconButton(
                onPressed: () {
                  if (isModified()) {
                    return showDialog<void>(
                      context: context,
                      barrierDismissible: true, //must tap = false
                      builder: (BuildContext contexte) {
                        return AlertDialog(
                          title: Text('Are you sure you want to exit?'),
                          content: SingleChildScrollView(
                            child: ListBody(
                              children: <Widget>[
                                Text('You haven\'t saved changes yet'),
                                Text('Do you want to exit?'),
                              ],
                            ),
                          ),
                          actions: <Widget>[
                            FlatButton(
                              child: Text('Cancel'),
                              onPressed: () {
                                Navigator.of(contexte).pop();
                              },
                            ),
                            FlatButton(
                              child: Text('Ok'),
                              onPressed: () {
                                Navigator.of(context).pop();
                                Navigator.of(context).pop();
                              },
                            ),
                            FlatButton(
                              child: Text('Save'),
                              onPressed: () {
                                saveFile();
                                Navigator.of(context).pop(true);
                                Navigator.of(context).pop();
                              },
                            ),
                          ],
                        );
                      },
                    );
                  } else {
                    Navigator.pop(context);
                  }
                  return Future.value();
                },
                icon: Icon(Icons.arrow_back_ios),
              )
            ],
          ),
          body: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Form(
                child: Column(
              children: [
                ipField,
                portField,
                passwordField,
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    saveButton,
                    RaisedButton(
                      padding: const EdgeInsets.all(8.0),
                      textColor: Colors.white,
                      color: color,
                      onPressed: () {
                        cancel(context);
                      },
                      child: new Text("Cancel"),
                    )
                  ],
                )
              ],
            )),
          )
      ),
    );
  }

  void saveFile() {
//    index = Main.fileHandler.getDataIndex(_serverData);
//
//    if (index == -2) {
//      Utilities.showInfoDialogue(
//          _scaffoldKey.currentContext,
//          "Serverdata index is -2 which means it cannot be found on the list",
//          [Text("The server data is not in the list")]);
//      return;
//    }
//
//    getList().insert(index, _serverData)

    _serverData.ip = _ipDataController.value.text;
    _serverData.port = int.parse(_portDataController.value.text);

    var pass = _passDataController.value.text;

    if (_serverData.hashedPassword != _passDataController.text) {
      if (pass == "" || pass == null)
        _serverData.hashedPassword = "";
      else
        _serverData.hashedPasswordDoHash = pass;
    }

    print(
        "Saving from edit $_serverData with hash ${_serverData.hashedPassword}");

    Main.fileHandler.updateServerData(_serverData);
    _originalServerData = ServerData.fromServer(_serverData);

    Main.fileHandler.saveData();
  }

  bool isModified() {
    return _ipDataController.text == _originalServerData.ip ||
        int.parse(_portDataController.text) == _originalServerData.port ||
        EncryptionUtil.toSha256(_passDataController.text) ==
            _originalServerData.hashedPassword;
  }

  void cancel(BuildContext context) {
    Navigator.pop(context);
  }
}

isNumeric(string) => num.tryParse(string) != null;
