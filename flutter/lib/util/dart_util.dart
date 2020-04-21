import 'package:flutter/material.dart';

class Utilities {
  static void showInfoDialogue(BuildContext context, String msg,
      [List<Text> widgets]) {
    showDialog<void>(
      context: context,
      barrierDismissible: true, //must tap = false
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
                Navigator.of(contexte).pop(true);
              },
            ),
          ],
        );
      },
    );
  }
}
