import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import '../services/database_helper.dart';
import '../services/password_hasher.dart';

class AppUser {
  final String id;
  final String username;
  final String role; // 'admin', 'prestataire', 'client'

  AppUser({
    required this.id,
    required this.username,
    required this.role,
  });

  factory AppUser.fromMap(Map<String, dynamic> map) {
    return AppUser(
      id: map['id'] as String,
      username: map['username'] as String,
      role: map['role'] as String,
    );
  }
}

class AuthNotifier extends ChangeNotifier {
  AppUser? _currentUser;
  bool _isLoading = false;
  String? _errorMessage;

  AppUser? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  bool get isAuthenticated => _currentUser != null;

  Future<bool> login(String username, String password) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final normalizedUsername = username.trim().toLowerCase();
      // Sur le web, éviter d'utiliser sqflite (service worker manquant dans certains environnements).
      if (kIsWeb) {
        // Comptes de démonstration supportés
        final demoAccounts = {
          'admin': 'admin',
          'prestataire': 'prestataire',
          'client': 'client',
        };
        final expected = demoAccounts[normalizedUsername];
        if (expected == null) {
          _errorMessage = "Utilisateur non trouvé";
          _isLoading = false;
          notifyListeners();
          return false;
        }
        if (password != expected) {
          _errorMessage = "Mot de passe incorrect";
          _isLoading = false;
          notifyListeners();
          return false;
        }
        // Connexion réussie en local pour le web
        _currentUser = AppUser(id: normalizedUsername, username: normalizedUsername, role: normalizedUsername);
        _isLoading = false;
        notifyListeners();
        return true;
      }
      final userMap = await DatabaseHelper().getUserByUsername(normalizedUsername);
      debugPrint('AuthNotifier.login: normalizedUsername=$normalizedUsername');
      debugPrint('AuthNotifier.login: userMap=$userMap');
      if (userMap == null) {
        _errorMessage = "Utilisateur non trouvé";
        _isLoading = false;
        notifyListeners();
        return false;
      }

      final dbPassword = userMap['password'] as String;
      final isValidPassword = PasswordHasher.verify(
        username: normalizedUsername,
        password: password,
        storedPassword: dbPassword,
      );
      debugPrint('AuthNotifier.login: dbPassword=${dbPassword.substring(0, 8)}...');
      debugPrint('AuthNotifier.login: isValidPassword=$isValidPassword');
      if (!isValidPassword) {
        _errorMessage = "Mot de passe incorrect";
        _isLoading = false;
        notifyListeners();
        return false;
      }

      if (!PasswordHasher.isHash(dbPassword)) {
        await DatabaseHelper().updateUserPassword(normalizedUsername, password);
      }

      _currentUser = AppUser.fromMap(userMap);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = "Erreur lors de la connexion : $e";
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> register(String username, String password, String role) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final existingUser = await DatabaseHelper().getUserByUsername(username.trim().toLowerCase());
      if (existingUser != null) {
        _errorMessage = "Ce nom d'utilisateur est déjà pris";
        _isLoading = false;
        notifyListeners();
        return false;
      }

      final id = const Uuid().v4();
      await DatabaseHelper().insertUser(id, username.trim().toLowerCase(), password, role);
      
      // Auto-login after registration
      _currentUser = AppUser(id: id, username: username.trim(), role: role);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = "Erreur d'inscription : $e";
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  // Connexion rapide pour démonstration
  Future<void> loginQuickly(String role) async {
    _isLoading = true;
    notifyListeners();

    // Rôles standard : 'admin', 'prestataire', 'client'
    final username = role.toLowerCase();
    // Sur le web, bypass DB et créer un utilisateur de démonstration
    if (kIsWeb) {
      final id = role == 'admin' ? 'u1' : (role == 'prestataire' ? 'u2' : 'u3');
      _currentUser = AppUser(id: id, username: username, role: role);
      _isLoading = false;
      notifyListeners();
      return;
    }

    // Essayer de charger depuis la base, sinon créer à la volée
    var userMap = await DatabaseHelper().getUserByUsername(username);
    if (userMap == null) {
      final id = role == 'admin' ? 'u1' : (role == 'prestataire' ? 'u2' : 'u3');
      await DatabaseHelper().insertUser(id, username, username, role);
      userMap = {
        'id': id,
        'username': username,
        'role': role,
      };
    }

    _currentUser = AppUser.fromMap(userMap);
    _isLoading = false;
    notifyListeners();
  }

  void logout() {
    _currentUser = null;
    _errorMessage = null;
    notifyListeners();
  }
}
