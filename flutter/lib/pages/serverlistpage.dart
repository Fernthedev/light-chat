import 'package:chatclientflutter/main.dart';
import 'package:chatclientflutter/pages/ServerEditPage.dart';
import 'package:chatclientflutter/util/filehandler.dart';
import 'package:chatclientflutter/util/serverdata.dart';
import 'package:flutter/material.dart';

class ServerPage extends StatefulWidget {
  static const routeName = "/serverpage";

  @override
  State<StatefulWidget> createState() {
    return ServerPageState();
  }
}

class ServerPageState extends State<ServerPage> {
  List<ServerData> getList() {
    return Main.getServerList();
  }

  List<ServerData> serverDataList;

  @override
  Widget build(BuildContext context) {

    FileHandler fileHandler = Main.fileHandler;

    serverDataList = getList();

    final Iterable<ListTile> tiles = getList().map<ListTile>(
      (ServerData serverData) {
        return ListTile(
          title: Text(
            serverData.ip,
          ),
          subtitle: Text(serverData.port.toString()),
          /*trailing: InkWell(
              onTap: () {
                int index = fileHandler.getDataIndex(serverData);
                print("Index of building is $index");
                print("UUID is $serverData");
                print("UUID2adaw22 is $serverData");
                // When the user taps the button, navigate to a named route
                // and provide the arguments as an optional parameter.
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ServerEditPage(),
                    // Pass the arguments as part of the RouteSettings. The
                    // ExtractArgumentScreen reads the arguments from these
                    // settings.
                    settings: RouteSettings(arguments: [index,serverData]),
                  ),
                );
              },
              child: Column(
                children: [Icon(
                  // Add the lines from here...
                  Icons.edit,
                  color: Colors.grey,
                ),
                ]
              )
          ),*/
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
                itemCount: getList().length,
                itemBuilder: (context, index) {
                  return Dismissible(
                    background: stackBehindDismiss(),
                    key: ObjectKey(getList()[index]),
                    child: divided[index],
                    onDismissed: (direction) {
                      var item = getList()[index];
                      //To delete
                      deleteItem(item);
                      //To show a snackbar with the UNDO button
                      Scaffold.of(context).showSnackBar(SnackBar(
                          content: Text("Item deleted"),
                          action: SnackBarAction(
                              label: "UNDO",
                              onPressed: () {
                                //To undo deletion
                                undoDeletion(index, item);
                              })));
                      setState(() {
                        serverDataList = getList();
                      });
                    },
                  );
                }),
          )),
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

  int deleteItem(ServerData data) {
    /*
    By implementing this method, it ensures that upon being dismissed from our widget tree,
    the item is removed from our list of items and our list is updated, hence
    preventing the "Dismissed widget still in widget tree error" when we reload.
    */
    setState(() {
      int index = getList().indexOf(data);
      getList().removeAt(index);
      Main.fileHandler.writeServerData(getList());
      return index;
    });
  }

  void undoDeletion(int index, ServerData item) {
    /*
    This method accepts the parameters index and item and re-inserts the {item} at
    index {index}
    */
    setState(() {
      getList().insert(index, item);
      Main.fileHandler.writeServerData(getList());
    });
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
