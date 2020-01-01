import 'dart:convert';
import 'dart:io';
import 'package:lightchat_client/data/serverdata.dart';
import 'package:path_provider/path_provider.dart';

class FileHandler {
  Future<String> get _localPath async {
    final Directory directory = await getApplicationDocumentsDirectory();

    if(!await directory.exists()) {
      await directory.create(recursive: true);
    }

    return directory.path;
  }

  Future<File> get _serversFile async {
    final path = await _localPath;
    File file = File('$path/servers.txt');

    if(!await file.exists()) {
      file = await file.create(recursive: true);
    }

    bool t = await file.exists();

    print("$file $t");

    return file;
  }

  Future<File> writeServerData(List<ServerData> serverData) async {
    final file = await _serversFile;

    // Write the file
    return file.writeAsString(jsonEncode(serverData));
  }

  Future<File> addServerData(ServerData serverDataE) async {
    List<ServerData> serverDataList = serverData;

    serverDataList.add(serverDataE);
    return writeServerData(serverDataList);
  }

  Future<File> removeServerData(ServerData serverDataE) async {
    List<ServerData> serverDataList = serverData;

    serverDataList.remove(serverDataE);
    return writeServerData(serverDataList);
  }
  List<ServerData> _serverDataList;

  List<ServerData> get serverData {
    if(_serverDataList == null) {
      reloadData();
    }
    return _serverDataList;
  }


  set serverData(List<ServerData> value) {
    _serverDataList = value;
  }

  void reloadData() async {
    _serverDataList = await readServerData();
    writeServerData(_serverDataList);
  }

  void saveData() async {
    writeServerData(_serverDataList);
  }

  Future<List<ServerData>> readServerData() async {
    try {
      final file = await _serversFile;

      // Read the file
      String contents = await file.readAsString() ?? "";

      Iterable l = json.decode(contents) ?? [];
      List<ServerData> serverDataList = l.map((f)=> ServerData.fromJson(f)).toList() ?? [];


      return serverDataList;
    } catch (e) {
      // If encountering an error, return 0
      return null;
    }
  }

  Future clearAppFolder() async {
    var appDir = await _localPath;
    new Directory(appDir).delete(recursive: true);
  }

  int getDataIndex(ServerData serverDataVar) {

    print("Going to check $_serverDataList");
    for(ServerData f in _serverDataList) {
      print("Checking $f.uuid and $serverDataVar.uuid");
      if(f.uuid == serverDataVar.uuid) {
        print("Found");
        return _serverDataList.indexOf(f);
      }
    }

    return -2;
  }
}