import 'package:lightchat_client/data/packetdata.dart';
import 'package:version/version.dart';

class Variables {
  static final VersionData versionData = VersionData.fromString("1.5.2", "1.5.2");

  static VersionRange getVersionRangeStatusSingle(VersionData otherVersion) {
    return getVersionRangeStatus(versionData, otherVersion);
  }

  static VersionRange getVersionRangeStatus(VersionData versionData, VersionData otherVersion) {
    Version current = versionData.version;
    Version minCurrent = versionData.minVersion;

    Version otherCurrent = otherVersion.version;
    Version otherMin = otherVersion.minVersion;

    /// Current version is smaller than the server's required minimum
    if(current < otherMin) {
      return VersionRange.WE_ARE_LOWER;
    } else

    /// Current version is larger than server's minimum version
    if (minCurrent > otherCurrent) {
      return VersionRange.WE_ARE_HIGHER;
    } else return VersionRange.MATCH_REQUIREMENTS;
  }

  static final int AES_KEY_SIZE = 256;
}

enum VersionRange {
  WE_ARE_HIGHER,
  MATCH_REQUIREMENTS,
  WE_ARE_LOWER,
}