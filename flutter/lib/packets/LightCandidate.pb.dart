///
//  Generated code. Do not modify.
//  source: LightCandidate.proto
///
// ignore_for_file: camel_case_types,non_constant_identifier_names,library_prefixes,unused_import,unused_shown_name

import 'dart:core' as $core show bool, Deprecated, double, int, List, Map, override, String;

import 'package:protobuf/protobuf.dart' as $pb;

class LightCandidateData extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo('LightCandidateData', package: const $pb.PackageName('lightclient.packets'))
    ..aOS(1, 'value')
    ..aOS(2, 'displ')
    ..aOS(3, 'group')
    ..aOS(4, 'descr')
    ..aOS(5, 'suffix')
    ..aOS(6, 'key')
    ..aOB(7, 'complete')
    ..hasRequiredFields = false
  ;

  LightCandidateData() : super();
  LightCandidateData.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromBuffer(i, r);
  LightCandidateData.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) : super.fromJson(i, r);
  LightCandidateData clone() => LightCandidateData()..mergeFromMessage(this);
  LightCandidateData copyWith(void Function(LightCandidateData) updates) => super.copyWith((message) => updates(message as LightCandidateData));
  $pb.BuilderInfo get info_ => _i;
  static LightCandidateData create() => LightCandidateData();
  LightCandidateData createEmptyInstance() => create();
  static $pb.PbList<LightCandidateData> createRepeated() => $pb.PbList<LightCandidateData>();
  static LightCandidateData getDefault() => _defaultInstance ??= create()..freeze();
  static LightCandidateData _defaultInstance;

  $core.String get value => $_getS(0, '');
  set value($core.String v) { $_setString(0, v); }
  $core.bool hasValue() => $_has(0);
  void clearValue() => clearField(1);

  $core.String get displ => $_getS(1, '');
  set displ($core.String v) { $_setString(1, v); }
  $core.bool hasDispl() => $_has(1);
  void clearDispl() => clearField(2);

  $core.String get group => $_getS(2, '');
  set group($core.String v) { $_setString(2, v); }
  $core.bool hasGroup() => $_has(2);
  void clearGroup() => clearField(3);

  $core.String get descr => $_getS(3, '');
  set descr($core.String v) { $_setString(3, v); }
  $core.bool hasDescr() => $_has(3);
  void clearDescr() => clearField(4);

  $core.String get suffix => $_getS(4, '');
  set suffix($core.String v) { $_setString(4, v); }
  $core.bool hasSuffix() => $_has(4);
  void clearSuffix() => clearField(5);

  $core.String get key => $_getS(5, '');
  set key($core.String v) { $_setString(5, v); }
  $core.bool hasKey() => $_has(5);
  void clearKey() => clearField(6);

  $core.bool get complete => $_get(6, false);
  set complete($core.bool v) { $_setBool(6, v); }
  $core.bool hasComplete() => $_has(6);
  void clearComplete() => clearField(7);
}

