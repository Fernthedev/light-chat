///
//  Generated code. Do not modify.
//  source: Packet.proto
///
// ignore_for_file: camel_case_types,non_constant_identifier_names,library_prefixes,unused_import,unused_shown_name

import 'dart:core' as $core show bool, Deprecated, double, int, List, Map, override, String;

import 'package:protobuf/protobuf.dart' as $pb;

import 'LightCandidate.pb.dart' as $0;

import 'Packet.pbenum.dart';

export 'Packet.pbenum.dart';

class MessagePacket extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo('MessagePacket', package: const $pb.PackageName('lightclient.packets'))
    ..aOS(1, 'message')
    ..aOB(2, 'command')
    ..hasRequiredFields = false
  ;

  MessagePacket() : super();
  MessagePacket.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromBuffer(i, r);
  MessagePacket.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromJson(i, r);
  MessagePacket clone() => MessagePacket()..mergeFromMessage(this);
  MessagePacket copyWith(void Function(MessagePacket) updates) => super.copyWith((message) => updates(message as MessagePacket));
  $pb.BuilderInfo get info_ => _i;
  static MessagePacket create() => MessagePacket();
  MessagePacket createEmptyInstance() => create();
  static $pb.PbList<MessagePacket> createRepeated() => $pb.PbList<MessagePacket>();
  static MessagePacket getDefault() => _defaultInstance ??= create()..freeze();
  static MessagePacket _defaultInstance;

  $core.String get message => $_getS(0, '');
  set message($core.String v) { $_setString(0, v); }
  $core.bool hasMessage() => $_has(0);
  void clearMessage() => clearField(1);

  $core.bool get command => $_get(1, false);
  set command($core.bool v) { $_setBool(1, v); }
  $core.bool hasCommand() => $_has(1);
  void clearCommand() => clearField(2);
}

class AutoCompletePacket extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo('AutoCompletePacket', package: const $pb.PackageName('lightclient.packets'))
    ..pc<$0.LightCandidateData>(2, 'candidateList', $pb.PbFieldType.PM,$0.LightCandidateData.create)
    ..pPS(3, 'wordsListJson')
    ..hasRequiredFields = false
  ;

  AutoCompletePacket() : super();
  AutoCompletePacket.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromBuffer(i, r);
  AutoCompletePacket.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromJson(i, r);
  AutoCompletePacket clone() => AutoCompletePacket()..mergeFromMessage(this);
  AutoCompletePacket copyWith(void Function(AutoCompletePacket) updates) => super.copyWith((message) => updates(message as AutoCompletePacket));
  $pb.BuilderInfo get info_ => _i;
  static AutoCompletePacket create() => AutoCompletePacket();
  AutoCompletePacket createEmptyInstance() => create();
  static $pb.PbList<AutoCompletePacket> createRepeated() => $pb.PbList<AutoCompletePacket>();
  static AutoCompletePacket getDefault() => _defaultInstance ??= create()..freeze();
  static AutoCompletePacket _defaultInstance;

  $core.List<$0.LightCandidateData> get candidateList => $_getList(0);

  $core.List<$core.String> get wordsListJson => $_getList(1);
}

class ConnectedPacket extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo('ConnectedPacket', package: const $pb.PackageName('lightclient.packets'))
    ..aOS(4, 'name')
    ..aOS(5, 'os')
    ..aOS(6, 'uuid')
    ..aOS(7, 'privateKey')
    ..hasRequiredFields = false
  ;

  ConnectedPacket() : super();
  ConnectedPacket.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromBuffer(i, r);
  ConnectedPacket.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromJson(i, r);
  ConnectedPacket clone() => ConnectedPacket()..mergeFromMessage(this);
  ConnectedPacket copyWith(void Function(ConnectedPacket) updates) => super.copyWith((message) => updates(message as ConnectedPacket));
  $pb.BuilderInfo get info_ => _i;
  static ConnectedPacket create() => ConnectedPacket();
  ConnectedPacket createEmptyInstance() => create();
  static $pb.PbList<ConnectedPacket> createRepeated() => $pb.PbList<ConnectedPacket>();
  static ConnectedPacket getDefault() => _defaultInstance ??= create()..freeze();
  static ConnectedPacket _defaultInstance;

  $core.String get name => $_getS(0, '');
  set name($core.String v) { $_setString(0, v); }
  $core.bool hasName() => $_has(0);
  void clearName() => clearField(4);

  $core.String get os => $_getS(1, '');
  set os($core.String v) { $_setString(1, v); }
  $core.bool hasOs() => $_has(1);
  void clearOs() => clearField(5);

  $core.String get uuid => $_getS(2, '');
  set uuid($core.String v) { $_setString(2, v); }
  $core.bool hasUuid() => $_has(2);
  void clearUuid() => clearField(6);

  $core.String get privateKey => $_getS(3, '');
  set privateKey($core.String v) { $_setString(3, v); }
  $core.bool hasPrivateKey() => $_has(3);
  void clearPrivateKey() => clearField(7);
}

class IllegalConnectionPacket extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo('IllegalConnectionPacket', package: const $pb.PackageName('lightclient.packets'))
    ..aOS(8, 'message')
    ..hasRequiredFields = false
  ;

  IllegalConnectionPacket() : super();
  IllegalConnectionPacket.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromBuffer(i, r);
  IllegalConnectionPacket.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromJson(i, r);
  IllegalConnectionPacket clone() => IllegalConnectionPacket()..mergeFromMessage(this);
  IllegalConnectionPacket copyWith(void Function(IllegalConnectionPacket) updates) => super.copyWith((message) => updates(message as IllegalConnectionPacket));
  $pb.BuilderInfo get info_ => _i;
  static IllegalConnectionPacket create() => IllegalConnectionPacket();
  IllegalConnectionPacket createEmptyInstance() => create();
  static $pb.PbList<IllegalConnectionPacket> createRepeated() => $pb.PbList<IllegalConnectionPacket>();
  static IllegalConnectionPacket getDefault() => _defaultInstance ??= create()..freeze();
  static IllegalConnectionPacket _defaultInstance;

  $core.String get message => $_getS(0, '');
  set message($core.String v) { $_setString(0, v); }
  $core.bool hasMessage() => $_has(0);
  void clearMessage() => clearField(8);
}

class RequestInfoPacket extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo('RequestInfoPacket', package: const $pb.PackageName('lightclient.packets'))
    ..aOS(9, 'encryptionKey')
    ..hasRequiredFields = false
  ;

  RequestInfoPacket() : super();
  RequestInfoPacket.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromBuffer(i, r);
  RequestInfoPacket.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromJson(i, r);
  RequestInfoPacket clone() => RequestInfoPacket()..mergeFromMessage(this);
  RequestInfoPacket copyWith(void Function(RequestInfoPacket) updates) => super.copyWith((message) => updates(message as RequestInfoPacket));
  $pb.BuilderInfo get info_ => _i;
  static RequestInfoPacket create() => RequestInfoPacket();
  RequestInfoPacket createEmptyInstance() => create();
  static $pb.PbList<RequestInfoPacket> createRepeated() => $pb.PbList<RequestInfoPacket>();
  static RequestInfoPacket getDefault() => _defaultInstance ??= create()..freeze();
  static RequestInfoPacket _defaultInstance;

  $core.String get encryptionKey => $_getS(0, '');
  set encryptionKey($core.String v) { $_setString(0, v); }
  $core.bool hasEncryptionKey() => $_has(0);
  void clearEncryptionKey() => clearField(9);
}

class SelfMessagePacket extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo('SelfMessagePacket', package: const $pb.PackageName('lightclient.packets'))
    ..e<SelfMessageType>(10, 'messageType', $pb.PbFieldType.OE, SelfMessageType.FillPasswordPacket, SelfMessageType.valueOf, SelfMessageType.values)
    ..hasRequiredFields = false
  ;

  SelfMessagePacket() : super();
  SelfMessagePacket.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromBuffer(i, r);
  SelfMessagePacket.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromJson(i, r);
  SelfMessagePacket clone() => SelfMessagePacket()..mergeFromMessage(this);
  SelfMessagePacket copyWith(void Function(SelfMessagePacket) updates) => super.copyWith((message) => updates(message as SelfMessagePacket));
  $pb.BuilderInfo get info_ => _i;
  static SelfMessagePacket create() => SelfMessagePacket();
  SelfMessagePacket createEmptyInstance() => create();
  static $pb.PbList<SelfMessagePacket> createRepeated() => $pb.PbList<SelfMessagePacket>();
  static SelfMessagePacket getDefault() => _defaultInstance ??= create()..freeze();
  static SelfMessagePacket _defaultInstance;

  SelfMessageType get messageType => $_getN(0);
  set messageType(SelfMessageType v) { setField(10, v); }
  $core.bool hasMessageType() => $_has(0);
  void clearMessageType() => clearField(10);
}

