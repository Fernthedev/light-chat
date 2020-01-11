import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:light_chat_client/EventListener.dart';
import 'package:light_chat_client/data/packetdata.dart';
import 'package:light_chat_client/data/serverdata.dart';
import 'package:light_chat_client/packets/other_packets.dart';
import 'package:light_chat_client/packets/packets.dart';
import 'package:light_chat_client_flutter/main.dart';

class ChatPage extends StatefulWidget {


  ServerData _serverData;

  @override
  _ChatPageState createState() => _ChatPageState(_serverData);


  static const String routeName = "/chatpage";

  ChatPage(this._serverData);
}

//class ChatArguments {
//  final ServerData serverData;
//
//  ChatArguments(this.serverData);
//}

class _ChatPageState extends State<ChatPage> implements PacketListener {

//  ChatArguments _chatArguments;

  ServerData _serverData;

  List<String> messages = [];

  final textController = TextEditingController();

  _ChatPageState(this._serverData) {
    Main.client.addPacketListener(this);
  }

  @override
  void dispose() {
    // Clean up the controller when the widget is disposed.
    Main.client.removePacketListener(this);
    Main.client.close();
    textController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
//    _chatArguments = ModalRoute
//        .of(context)
//        .settings
//        .arguments;

    List<Widget> textWidgets = messages.map((string) =>
        Text(string, style: TextStyle(fontSize: 20))
    ).toList();

    var chatTextFormField = TextField(
      controller: textController,
      decoration: const InputDecoration(
        hintText: 'Message',
        labelText: 'Message:',
        border: OutlineInputBorder(),
      ),
      keyboardType: TextInputType.text,
    );

    return MaterialApp(
        home: Scaffold(
          appBar: AppBar(
            leading: IconButton(
              icon: Icon(Icons.arrow_back_ios),
              tooltip: 'Back',
              onPressed: () {
                Navigator.of(context).pop(true);
              },
            ),
            title: Text(
                'Connected to ${_serverData.ipPortName()}'),
//            actions: <Widget>[
//              IconButton(
//                icon: Icon(Icons.search),
//                tooltip: 'Search',
//                onPressed: null,
//              ),
//            ],
          ),
          // body is the majority of the screen.
          body: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: <Widget>[
                Expanded(
                    child: Align(
                        alignment: Alignment.topCenter,
                        child: FractionallySizedBox(
                          heightFactor: 0.975,
                          child: Container(
//                            color: Color.fromARGB(255, 200, 200, 200),
                              child: SingleChildScrollView(
                                child: ListBody(
                                children: textWidgets,
                                )
                              )
                          ),
                        )
                    )
                ),
                SizedBox(height: 10.0),
                chatTextFormField,
              ],
            ),
          ),

          floatingActionButton: FloatingActionButton(
            tooltip: 'Send', // used by assistive technologies
            child: Icon(Icons.send),
            onPressed: () {
              Main.client.sendMessage(textController.value.text);
              textController.text = '';
            },
          ),
        )
    );
  }

  @override
  void handle(Packet p, [Object result]) {
    if (p is MessagePacket) {
      setState(() {
        messages.add(p.message);
      });
    }

    if (p is SelfMessagePacket) {
      switch (p.messageType) {

        case MessageType.FILL_PASSWORD:
          showDialog<void>(
            context: context,
            barrierDismissible: false, //must tap = false
            builder: (BuildContext contexte) {
              return AlertDialog(
                title: Text('Please enter your password'),
                content: SingleChildScrollView(
                  child: ListBody(
                    children: <Widget>[
//                      Text('You haven\'t saved changes yet'),
//                      Text('Do you want to exit?'),
                    ],
                  ),
                ),
                actions: <Widget>[
                  FlatButton(
                    child: Text('Cancel'),
                    onPressed: () {
                      Main.client.close();
                      Navigator.of(context).pop(true);
                      Navigator.of(context).pop();
                    },
                  ),
                  TextFormField(
                    decoration: const InputDecoration(
                      hintText: 'Password',
                      labelText: 'Password:',
                      border: OutlineInputBorder(),
                    ),
                    onSaved: (String value) {
                      Main.client.send(HashedPasswordPacket.create(HashedPassword(value)));
                    },
//                  validator: (String value) {
//
//                  },
                    keyboardType: TextInputType.visiblePassword,
//                  initialValue: "",
                  ),
                  FlatButton(
                    child: Text('Ok'),
                    onPressed: () {
                      Navigator.of(context).pop();
                      Navigator.of(context).pop();
                    },
                  ),
                ],
              );
            },
          );
          break;

        case MessageType.LOST_SERVER_CONNECTION:
          showDisconnectDialogue("Lost connection.", [
            Text("The connection has been closed from the server side.")
          ]);
          break;
        case MessageType.REGISTER_PACKET:
          break;
        case MessageType.TIMED_OUT_REGISTRATION:
          showDisconnectDialogue("Timed out at registration process", [
            Text("The client-server connnection took too long to sucessfully finish.")
          ]);
          break;
      }
    }
  }

  void showDisconnectDialogue(String msg, [List<Text> widgets]) {
    showDialog<void>(
      context: context,
      barrierDismissible: false, //must tap = false
      builder: (BuildContext contexte) {
        return AlertDialog(
          title: Text(msg),
          content: SingleChildScrollView(
            child: ListBody(
              children: widgets,
            ),
          ),
          actions: <Widget>[
            FlatButton(
              child: Text('Ok'),
              onPressed: () {
                Main.client.close();
                Navigator.of(context).pop(true);
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }
}
