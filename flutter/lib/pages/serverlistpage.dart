import 'package:flutter/material.dart';
import 'package:light_chat_client/data/serverdata.dart';
import 'package:light_chat_client_flutter/assets/colors.dart';
import 'package:light_chat_client_flutter/main.dart';

import 'ServerEditPage.dart';

class ServerPage extends StatefulWidget {
  static const routeName = "/serverpage";

  @override
  State<StatefulWidget> createState() {
    return ServerPageState();
  }
}

class ServerPageState extends State<ServerPage> {
  Map<String, ServerData> serverDataList;

  @override
  Widget build(BuildContext context) {
    Main.fileHandler.reloadData();
    serverDataList = new Map.from(Main.fileHandler.serverDataMap);

    final Iterable<ListTile> tiles = serverDataList.values.map<ListTile>(
      (ServerData serverData) {
        return ListTile(
          title: Text(
            serverData.ip,
          ),
          subtitle: Text(serverData.port.toString()),
          trailing: InkWell(
              onTap: () {
                editServer(serverData);
              },
              child: Column(children: [
                Icon(
                  // Add the lines from here...
                  Icons.edit,
                  color: Colors.grey,
                ),
              ])),
          onTap: () {
            // When the user taps the button, navigate to a named route
            // and provide the arguments as an optional parameter.
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => LoginForm(serverData),
                // Pass the arguments as part of the RouteSettings. The
                // ExtractArgumentScreen reads the arguments from these
                // settings.
                settings: RouteSettings(arguments: serverData),
              ),
            );
          },
        );
      },
    );

    final List<Widget> divided = ListTile.divideTiles(
      context: context,
      tiles: tiles,
    ).toList();

    return MaterialApp(
      theme: buildLightTheme(),
      darkTheme: buildDarkTheme(),
      title: 'Saved Servers',
      home: Scaffold(
          appBar: AppBar(
            title: Text('Saved Servers'),
            actions: <Widget>[
              IconButton(
                onPressed: () {
                  Navigator.pop(context);
                },
                icon: Icon(Icons.arrow_back_ios),
              )
            ],
          ),
          body: Container(
            child: ListView.builder(
                itemCount: serverDataList.length,
                itemBuilder: (context, index) {
                  String key = serverDataList.keys.elementAt(index);
                  ServerData serverData = serverDataList[key];

                  return Dismissible(
                    background: stackBehindDismiss(),
                    key: Key(serverData.uuid),
                    child: divided[index],
                    onDismissed: (direction) {
                      setState(() {

                        //To delete
                        deleteItem(serverData);

                        serverDataList = Map.from(Main.fileHandler.serverDataMap);
                        serverDataList.remove(key);
                      });

                      //To show a snackbar with the UNDO button
                      Scaffold.of(context).showSnackBar(SnackBar(
                          content: Text("Item deleted"),
                          action: SnackBarAction(
                              label: "UNDO",
                              onPressed: () {
                                //To undo deletion
                                undoDeletion(serverData);
                              })));

                    },
                  );
                }),
          ),
        floatingActionButton: FloatingActionButton(
        tooltip: 'Send', // used by assistive technologies
        child: Icon(Icons.add),
        onPressed: () {
          addItem(ServerData("ip", 2000, null));
        },
      ),
      ),
    );
  }

  Widget stackBehindDismiss() {
    return Container(
      alignment: Alignment.centerRight,
      padding: EdgeInsets.only(right: 20.0),
      color: Colors.red,
      child: Icon(
        Icons.delete,
        color: Colors.white,
      ),
    );
  }

  String addItem(ServerData data) {
    /*
    By implementing this method, it ensures that upon being dismissed from our widget tree,
    the item is removed from our list of items and our list is updated, hence
    preventing the "Dismissed widget still in widget tree error" when we reload.
    */
    setState(() {
      Main.fileHandler.addServerData(data);
      Main.fileHandler.saveData();
      editServer(data);

      return data.uuid;
    });
  }

  void deleteItem(ServerData data) {
    /*
    By implementing this method, it ensures that upon being dismissed from our widget tree,
    the item is removed from our list of items and our list is updated, hence
    preventing the "Dismissed widget still in widget tree error" when we reload.
    */
    setState(() {
      Main.fileHandler.removeServerData(data);
      Main.fileHandler.saveData();
    });
  }

  void undoDeletion(ServerData item) {
    /*
    This method accepts the parameters index and item and re-inserts the {item} at
    index {index}
    */
    setState(() {
      Main.fileHandler.addServerData(item);
      Main.fileHandler.saveData();
    });
  }

  void editServer(ServerData serverData) {
    // When the user taps the button, navigate to a named route
    // and provide the arguments as an optional parameter.
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ServerEditPage(),
        // Pass the arguments as part of the RouteSettings. The
        // ExtractArgumentScreen reads the arguments from these
        // settings.
        settings: RouteSettings(arguments: [serverData.uuid]),
      ),
    );
  }
}

class ServerPageRunnable extends Runnable {
  @override
  void run() {
    BuildContext context = Main.mainKey.currentState.context;

    if (Main.fileHandler == null) {
      showDialog(
          context: context,
          builder: (context) {
            return AlertDialog(
              title: Text('Error'),
              content: Text('Filehandler is null'),
              actions: <Widget>[
                FlatButton(
                    onPressed: () => Navigator.of(context).pop(),
                    child: Text('OK')),
              ],
            );
          });
    }

    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => ServerPage()),
    );
  }
}
