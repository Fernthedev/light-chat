import 'package:pointycastle/api.dart';

abstract class IKeyEncriptionHolder {
  bool isEncryptionKeyRegistered();

  KeyParameter getKey();
}
