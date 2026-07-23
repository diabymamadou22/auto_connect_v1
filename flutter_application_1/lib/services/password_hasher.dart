import 'dart:convert';

import 'package:crypto/crypto.dart';

class PasswordHasher {
  static const String _prefix = r'sha256$';
  static const String _appSalt = 'auto_connect_mali_local_auth';

  static String hash(String username, String password) {
    final normalizedUsername = username.trim().toLowerCase();
    final bytes = utf8.encode('$_appSalt::$normalizedUsername::$password');
    return '$_prefix${sha256.convert(bytes)}';
  }

  static bool isHash(String value) {
    return value.startsWith(_prefix);
  }

  static bool verify({
    required String username,
    required String password,
    required String storedPassword,
  }) {
    if (!isHash(storedPassword)) {
      return storedPassword == password;
    }
    return storedPassword == hash(username, password);
  }
}
