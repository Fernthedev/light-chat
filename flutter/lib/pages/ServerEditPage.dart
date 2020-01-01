import 'dart:io';

import 'package:lightchat_client/data/serverdata.dart';
import 'package:lightchat_client/main.dart';
import 'package:flutter/material.dart';

class ServerEditPage extends StatelessWidget {
  static ServerData _serverData;
  static ServerData _originalServerData;
  int index;
  static bool difModified = false;

  static const routeName = "/servereditpage";

  @override
  Widget build(BuildContext context) {
    // Extract the arguments from the current ModalRoute settings and cast
    // them as ScreenArguments.
    final List<Object> objects = ModalRoute.of(context).settings.arguments;

    index = objects[0];
    _serverData = objects[1];
    _originalServerData = _serverData;

    index = getList().indexOf(_serverData);
    print(index);

    TextFormField ipField = TextFormField(
      decoration: const InputDecoration(
        hintText: '192.168.2.*',
        labelText: 'IP Address',
      ),
      initialValue: _serverData.ip,
      onSaved: (String value) {
        if(value != _originalServerData.ip) {
          difModified = true;
        }else difModified = false;
        _serverData.ip = value;
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

    TextFormField portField = TextFormField(
      decoration: const InputDecoration(
        hintText: '2000',
        labelText: 'Port',
      ),
      onSaved: (String value) {
        if(int.parse(value) != _originalServerData.port) {
          difModified = true;
        }else difModified = false;

        _serverData.port = int.parse(value);
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      validator: (String value) {
        return isNumeric(value) == false ? 'Not a valid port' : null;
      },
      keyboardType: TextInputType.number,
      initialValue: _serverData.port.toString(),
    );

    TextFormField passwordField = TextFormField(
      decoration: const InputDecoration(
        hintText: 'Password (Optional)',
        labelText: 'Password',
      ),
      onSaved: (String value) {
        if(value != _originalServerData.hashedPassword) {
          difModified = true;
        }else difModified = false;
        _serverData.hashedPassword = value;
        // This optional block of code can be used to run
        // code when the user saves the form.
      },
      obscureText: true,
      initialValue: _serverData.hashedPassword,
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
      // Provide a function to handle named routes. Use this function to
      // identify the named route being pushed and create the correct
      // Screen.
      onGenerateRoute: (settings) {
        // If you push the PassArguments route
        if (settings.name == ServerEditPage.routeName) {
          // Cast the arguments to the correct type: ScreenArguments.
          final List<Object> args = settings.arguments;

          int index = args[0];
          ServerData serverData = args[1];

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
          appBar: AppBar(
            title: Text('Edit server'),
            actions: <Widget>[
              IconButton(
                onPressed: () {

                  if(difModified) {
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
                  }else {
                    Navigator.pop(context);
                  }

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
          )),
    );
  }

  void saveFile() {
    index = getDataIndex(_serverData);

    getList().insert(index, _serverData);
    Main.fileHandler.writeServerData(getList());
  }

  List<ServerData> getList() {
    return Main.getServerList();
  }

  void cancel(BuildContext context) {
    Navigator.pop(context);
  }

  int getDataIndex(ServerData serverData) {
    List<ServerData> serverDataList = getList();
    for(ServerData f in serverDataList) {
      if(f.uuid == serverData.uuid) {
        return serverDataList.indexOf(f);
      }
    }

    return -2;
  }
}

isNumeric(string) => num.tryParse(string) != null;
