import 'dart:convert';
import 'dart:io';

import 'package:light_chat_client/data/serverdata.dart';
import 'package:path_provider/path_provider.dart';

class FileHandler {
  Future<void> getFiles() async {
    if (_cachedServersFile == null) _cachedServersFile = (await _serversFile);

    return Future.value();
  }

  File _cachedServersFile;
  File get cachedServersFile => _cachedServersFile;

  Future<String> get _localPath async {
    final Directory directory = await getApplicationDocumentsDirectory();

    if (!await directory.exists()) {
      await directory.create(recursive: true);
    }

    return directory.path;
  }

  Future<File> get _serversFile async {
    final path = await _localPath;
    File file = File('$path/servers.txt');

    if (!await file.exists()) {
      file = await file.create(recursive: true);
    }

    return file;
  }

  void setToDefaultIfNecessary() {
    bool necessary = !cachedServersFile.existsSync();

    var data;

    try {
      data = readServerData();
    } on Exception {
      data = null;
    }

    necessary = necessary || data == null;

    if (necessary) {
      print("Setting defaults");

      createDefaults();
      saveData();
    }
  }

  void writeServerData(Map<String, ServerData> serverData) {
    final file = cachedServersFile;

    // Write the file
    file.writeAsStringSync(jsonEncode(serverData), flush: true);
  }

  void addServerData(ServerData serverDataE) async {
    if (_serverDataList.containsKey(serverDataE.uuid))
      throw ArgumentError(
          "Serverdata is already added. Overwrite using updateServerData()");

    print("Saving ${serverDataE.uuid} to memory");

    _serverDataList[serverDataE.uuid] = serverDataE;
  }

  void updateServerData(ServerData serverDataE) async {
    if (!_serverDataList.containsKey(serverDataE.uuid))
      throw ArgumentError(
          "Serverdata not is already added. Add using addServerData()");

    _serverDataList.update(serverDataE.uuid, (_) => serverDataE);
  }

  void removeServerData(ServerData serverDataE) async {
    _serverDataList.remove(serverDataE.uuid);
  }

  Map<String, ServerData> _serverDataList;

  Map<String, ServerData> get serverDataMap {
    return _serverDataList;
  }

  Map<String, ServerData> createDefaults() {
    if (_serverDataList == null) {
      _serverDataList = new Map<String, ServerData>();
      addServerData(ServerData("192.168.0.17", 2000, "test1"));
      addServerData(ServerData("192.168.3.11", 2005, "no u"));
      addServerData(ServerData("192.168.5.11", 1942, "test2"));
    }

    if (_serverDataList.isEmpty) {
      _serverDataList = new Map<String, ServerData>();
      addServerData(ServerData("192.168.0.17", 2000, "no wawwasu"));
      addServerData(ServerData("192.168.3.11", 2005, "no u"));
    }

    return _serverDataList;
  }

  reloadData() {
    _serverDataList = readServerData();
    saveData();
  }

  saveData() {
    writeServerData(_serverDataList);
    Map<String, ServerData> writtenMap = readServerData();
    if (writtenMap == null)
      throw "The server data was not written or read correctly.";
  }

  Map<String, ServerData> readServerData() {
    print("Reading");
    File file = cachedServersFile;

    // Read the file
    String contents = file.readAsStringSync() ?? "";

    Map<String, dynamic> l = json.decode(contents) ?? [];

//      List<ServerData> serverDataList =
//          l.map((f) => ServerData.fromJson(f)).toList() ?? [];

    Map<String, ServerData> map = l.map(
        (uuid, serverData) => MapEntry(uuid, ServerData.fromJson(serverData)));

    for (var uuid in map.keys) {
      if (map[uuid].uuid != uuid)
        throw "UUIDs are not equal $uuid vs ${map[uuid].uuid}";
    }

    return map;

//      serverDataList.forEach((f) => {map[f.uuid] = f});
  }

  Future clearAppFolder() async {
    var appDir = await _localPath;
    new Directory(appDir).delete(recursive: true);
  }

  bool containsServerData(ServerData serverData) =>
      _serverDataList.containsKey(serverData.uuid) &&
      _serverDataList[serverData.uuid] == serverData;

//  int getDataIndex(ServerData serverDataVar) {
//    print("Going to check $_serverDataList");
//    for (var uuid in _serverDataList.keys) {
//      var f = _serverDataList[uuid];
//      print("Checking $f.uuid and $serverDataVar.uuid");
//      if (f.uuid == serverDataVar.uuid) {
//        print("Found");
//        return _serverDataList.indexOf(f);
//      },
//    }
//
//    _serverDataList.forEach((uuid, f) => {
//
//    });
//
//    return -2;
//  }
}
