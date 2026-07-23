import 'dart:async';
import 'package:flutter/foundation.dart' show kIsWeb, debugPrint;
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as path;
import '../models/service_provider.dart';
import '../models/review.dart';
import '../data/sample_data.dart';
import 'password_hasher.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  static Database? _database;
  
  bool _useInMemoryFallback = false;
  Future<Database>? _dbInitFuture;

  // In-memory collections for fallback
  final Map<String, Map<String, dynamic>> _fallbackServices = {};
  final Map<String, Map<String, dynamic>> _fallbackReviews = {};
  final Map<String, Map<String, dynamic>> _fallbackUsers = {};

  factory DatabaseHelper() {
    return _instance;
  }

  DatabaseHelper._internal();

  void _initFallbackData() {
    if (_fallbackServices.isEmpty) {
      for (final service in sampleProviders) {
        _fallbackServices[service.id] = _serviceToMap(service);
      }
      
      // Default users
      _insertUserFallback('u1', 'admin', 'admin', 'admin');
      _insertUserFallback('u2', 'prestataire', 'prestataire', 'prestataire');
      _insertUserFallback('u3', 'client', 'client', 'client');
      debugPrint('DatabaseHelper: In-memory fallback collections initialized.');
    }
  }

  void _insertUserFallback(String id, String username, String password, String role) {
    final normalizedUsername = username.trim().toLowerCase();
    _fallbackUsers[normalizedUsername] = {
      'id': id,
      'username': normalizedUsername,
      'password': PasswordHasher.hash(normalizedUsername, password),
      'role': role,
    };
  }

  Future<Database> get database async {
    if (_useInMemoryFallback) {
      throw Exception("Database helper is in in-memory fallback mode");
    }
    if (_database != null) return _database!;

    _dbInitFuture ??= _initDatabase();
    try {
      // Add a timeout of 3 seconds to database initialization to prevent hangs.
      _database = await _dbInitFuture!.timeout(
        const Duration(seconds: 3),
        onTimeout: () {
          throw TimeoutException("Database initialization timed out after 3 seconds");
        },
      );
      return _database!;
    } catch (e) {
      debugPrint("DatabaseHelper: Database initialization failed or timed out. Switching to in-memory mode. Error: $e");
      _useInMemoryFallback = true;
      _initFallbackData();
      throw Exception("Database helper switched to in-memory fallback");
    }
  }

  Future<Database> _initDatabase() async {
    String dbPath;

    if (kIsWeb) {
      dbPath = inMemoryDatabasePath;
    } else {
      final documentsDirectory = await getApplicationDocumentsDirectory();
      dbPath = path.join(documentsDirectory.path, 'auto_connect.db');
    }

    final options = OpenDatabaseOptions(
      version: 5,
      onCreate: _onCreate,
      onUpgrade: _onUpgrade,
      onOpen: _ensureSchema,
    );

    try {
      return await databaseFactory.openDatabase(dbPath, options: options);
    } catch (e) {
      debugPrint('DatabaseHelper._initDatabase: failed to open $dbPath -> $e');
      if (kIsWeb) {
        debugPrint('DatabaseHelper._initDatabase: falling back to in-memory DB');
        return await databaseFactory.openDatabase(inMemoryDatabasePath, options: options);
      }
      rethrow;
    }
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE services (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        category TEXT NOT NULL,
        description TEXT NOT NULL,
        city TEXT NOT NULL,
        phone TEXT NOT NULL,
        rating REAL NOT NULL,
        latitude REAL NOT NULL,
        longitude REAL NOT NULL,
        isOpen INTEGER NOT NULL,
        createdAt TEXT NOT NULL,
        isFavorite INTEGER NOT NULL DEFAULT 0,
        isMine INTEGER NOT NULL DEFAULT 0,
        hours TEXT,
        servicesOffered TEXT
      )
    ''');

    await db.execute('''
      CREATE TABLE reviews (
        id TEXT PRIMARY KEY,
        serviceId TEXT NOT NULL,
        userName TEXT NOT NULL,
        comment TEXT NOT NULL,
        rating REAL NOT NULL,
        createdAt TEXT NOT NULL,
        FOREIGN KEY (serviceId) REFERENCES services (id) ON DELETE CASCADE
      )
    ''');

    await db.execute('''
      CREATE TABLE users (
        id TEXT PRIMARY KEY,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        role TEXT NOT NULL
      )
    ''');

    await _insertUserRow(db, 'u1', 'admin', 'admin', 'admin');
    await _insertUserRow(db, 'u2', 'prestataire', 'prestataire', 'prestataire');
    await _insertUserRow(db, 'u3', 'client', 'client', 'client');
  }

  Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    if (oldVersion < 2) {
      await _addColumnIfMissing(db, 'services', 'isFavorite', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing(db, 'services', 'isMine', 'INTEGER NOT NULL DEFAULT 0');
      await db.execute('''
        CREATE TABLE IF NOT EXISTS reviews (
          id TEXT PRIMARY KEY,
          serviceId TEXT NOT NULL,
          userName TEXT NOT NULL,
          comment TEXT NOT NULL,
          rating REAL NOT NULL,
          createdAt TEXT NOT NULL,
          FOREIGN KEY (serviceId) REFERENCES services (id) ON DELETE CASCADE
        )
      ''');
    }
    if (oldVersion < 3) {
      await _addColumnIfMissing(db, 'services', 'hours', 'TEXT');
      await _addColumnIfMissing(db, 'services', 'servicesOffered', 'TEXT');
    }
    if (oldVersion < 4) {
      await db.execute('''
        CREATE TABLE IF NOT EXISTS users (
          id TEXT PRIMARY KEY,
          username TEXT UNIQUE NOT NULL,
          password TEXT NOT NULL,
          role TEXT NOT NULL
        )
      ''');
      await _insertUserRow(db, 'u1', 'admin', 'admin', 'admin');
      await _insertUserRow(db, 'u2', 'prestataire', 'prestataire', 'prestataire');
      await _insertUserRow(db, 'u3', 'client', 'client', 'client');
    }
    await _ensureSchema(db);
  }

  Future<void> _ensureSchema(Database db) async {
    await _addColumnIfMissing(db, 'services', 'isFavorite', 'INTEGER NOT NULL DEFAULT 0');
    await _addColumnIfMissing(db, 'services', 'isMine', 'INTEGER NOT NULL DEFAULT 0');
    await _addColumnIfMissing(db, 'services', 'hours', 'TEXT');
    await _addColumnIfMissing(db, 'services', 'servicesOffered', 'TEXT');

    await db.execute('''
      CREATE TABLE IF NOT EXISTS reviews (
        id TEXT PRIMARY KEY,
        serviceId TEXT NOT NULL,
        userName TEXT NOT NULL,
        comment TEXT NOT NULL,
        rating REAL NOT NULL,
        createdAt TEXT NOT NULL,
        FOREIGN KEY (serviceId) REFERENCES services (id) ON DELETE CASCADE
      )
    ''');

    await db.execute('''
      CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        role TEXT NOT NULL
      )
    ''');

    await _insertUserRow(db, 'u1', 'admin', 'admin', 'admin');
    await _insertUserRow(db, 'u2', 'prestataire', 'prestataire', 'prestataire');
    await _insertUserRow(db, 'u3', 'client', 'client', 'client');
  }

  Future<void> _addColumnIfMissing(
    Database db,
    String tableName,
    String columnName,
    String columnDefinition,
  ) async {
    final columns = await db.rawQuery('PRAGMA table_info($tableName)');
    final exists = columns.any((column) => column['name'] == columnName);
    if (!exists) {
      await db.execute('ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition');
    }
  }

  Future<void> _insertUserRow(
    Database db,
    String id,
    String username,
    String password,
    String role,
  ) async {
    final normalizedUsername = username.trim().toLowerCase();
    await db.insert(
      'users',
      {
        'id': id,
        'username': normalizedUsername,
        'password': PasswordHasher.hash(normalizedUsername, password),
        'role': role,
      },
      conflictAlgorithm: ConflictAlgorithm.ignore,
    );
  }

  Future<void> insertService(ServiceProvider service) async {
    if (_useInMemoryFallback) {
      _fallbackServices[service.id] = _serviceToMap(service);
      return;
    }
    try {
      final db = await database;
      await db.insert(
        'services',
        _serviceToMap(service),
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      _fallbackServices[service.id] = _serviceToMap(service);
    }
  }

  Future<List<ServiceProvider>> getAllServices() async {
    if (_useInMemoryFallback) {
      _initFallbackData();
      return _fallbackServices.values.map((map) => _mapToService(map)).toList();
    }
    try {
      final db = await database;
      final maps = await db.query('services');
      return List.generate(maps.length, (i) => _mapToService(maps[i]));
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      return _fallbackServices.values.map((map) => _mapToService(map)).toList();
    }
  }

  Future<List<ServiceProvider>> getServicesByCategory(String category) async {
    if (_useInMemoryFallback) {
      _initFallbackData();
      return _fallbackServices.values
          .map((map) => _mapToService(map))
          .where((s) => s.category.toString().split('.').last == category)
          .toList();
    }
    try {
      final db = await database;
      final maps = await db.query(
        'services',
        where: 'category = ?',
        whereArgs: [category],
      );
      return List.generate(maps.length, (i) => _mapToService(maps[i]));
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      return _fallbackServices.values
          .map((map) => _mapToService(map))
          .where((s) => s.category.toString().split('.').last == category)
          .toList();
    }
  }

  Future<void> updateService(ServiceProvider service) async {
    if (_useInMemoryFallback) {
      _fallbackServices[service.id] = _serviceToUpdateMap(service);
      return;
    }
    try {
      final db = await database;
      await db.update(
        'services',
        _serviceToUpdateMap(service),
        where: 'id = ?',
        whereArgs: [service.id],
      );
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      _fallbackServices[service.id] = _serviceToUpdateMap(service);
    }
  }

  Future<void> deleteService(String id) async {
    if (_useInMemoryFallback) {
      _fallbackServices.remove(id);
      _fallbackReviews.removeWhere((key, review) => review['serviceId'] == id);
      return;
    }
    try {
      final db = await database;
      await db.delete('services', where: 'id = ?', whereArgs: [id]);
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      _fallbackServices.remove(id);
      _fallbackReviews.removeWhere((key, review) => review['serviceId'] == id);
    }
  }

  Future<void> insertReview(Review review) async {
    if (_useInMemoryFallback) {
      _fallbackReviews[review.id] = {
        'id': review.id,
        'serviceId': review.serviceId,
        'userName': review.userName,
        'comment': review.comment,
        'rating': review.rating,
        'createdAt': review.createdAt.toIso8601String(),
      };
      return;
    }
    try {
      final db = await database;
      await db.insert(
        'reviews',
        {
          'id': review.id,
          'serviceId': review.serviceId,
          'userName': review.userName,
          'comment': review.comment,
          'rating': review.rating,
          'createdAt': review.createdAt.toIso8601String(),
        },
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      _fallbackReviews[review.id] = {
        'id': review.id,
        'serviceId': review.serviceId,
        'userName': review.userName,
        'comment': review.comment,
        'rating': review.rating,
        'createdAt': review.createdAt.toIso8601String(),
      };
    }
  }

  Future<List<Review>> getReviewsForService(String serviceId) async {
    if (_useInMemoryFallback) {
      _initFallbackData();
      final list = _fallbackReviews.values
          .where((r) => r['serviceId'] == serviceId)
          .map((m) => Review(
                id: m['id'] as String,
                serviceId: m['serviceId'] as String,
                userName: m['userName'] as String,
                comment: m['comment'] as String,
                rating: (m['rating'] as num).toDouble(),
                createdAt: DateTime.parse(m['createdAt'] as String),
              ))
          .toList();
      list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
      return list;
    }
    try {
      final db = await database;
      final maps = await db.query(
        'reviews',
        where: 'serviceId = ?',
        whereArgs: [serviceId],
        orderBy: 'createdAt DESC',
      );
      return List.generate(maps.length, (i) {
        return Review(
          id: maps[i]['id'] as String,
          serviceId: maps[i]['serviceId'] as String,
          userName: maps[i]['userName'] as String,
          comment: maps[i]['comment'] as String,
          rating: (maps[i]['rating'] as num).toDouble(),
          createdAt: DateTime.parse(maps[i]['createdAt'] as String),
        );
      });
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      final list = _fallbackReviews.values
          .where((r) => r['serviceId'] == serviceId)
          .map((m) => Review(
                id: m['id'] as String,
                serviceId: m['serviceId'] as String,
                userName: m['userName'] as String,
                comment: m['comment'] as String,
                rating: (m['rating'] as num).toDouble(),
                createdAt: DateTime.parse(m['createdAt'] as String),
              ))
          .toList();
      list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
      return list;
    }
  }

  Future<Map<String, dynamic>?> getUserByUsername(String username) async {
    final normalizedUsername = username.trim().toLowerCase();
    if (_useInMemoryFallback) {
      _initFallbackData();
      return _fallbackUsers[normalizedUsername];
    }
    try {
      final db = await database;
      final maps = await db.query(
        'users',
        where: 'username = ?',
        whereArgs: [normalizedUsername],
      );
      if (maps.isNotEmpty) {
        return maps.first;
      }
      return null;
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      return _fallbackUsers[normalizedUsername];
    }
  }

  Future<void> insertUser(String id, String username, String password, String role) async {
    final normalizedUsername = username.trim().toLowerCase();
    if (_useInMemoryFallback) {
      _insertUserFallback(id, username, password, role);
      return;
    }
    try {
      final db = await database;
      await db.insert(
        'users',
        {
          'id': id,
          'username': normalizedUsername,
          'password': PasswordHasher.hash(normalizedUsername, password),
          'role': role,
        },
      );
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      _insertUserFallback(id, username, password, role);
    }
  }

  Future<void> updateUserPassword(String username, String password) async {
    final normalizedUsername = username.trim().toLowerCase();
    if (_useInMemoryFallback) {
      if (_fallbackUsers.containsKey(normalizedUsername)) {
        _fallbackUsers[normalizedUsername]!['password'] =
            PasswordHasher.hash(normalizedUsername, password);
      }
      return;
    }
    try {
      final db = await database;
      await db.update(
        'users',
        {'password': PasswordHasher.hash(normalizedUsername, password)},
        where: 'username = ?',
        whereArgs: [normalizedUsername],
      );
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      if (_fallbackUsers.containsKey(normalizedUsername)) {
        _fallbackUsers[normalizedUsername]!['password'] =
            PasswordHasher.hash(normalizedUsername, password);
      }
    }
  }

  Future<List<Review>> getAllReviews() async {
    if (_useInMemoryFallback) {
      _initFallbackData();
      final list = _fallbackReviews.values
          .map((m) => Review(
                id: m['id'] as String,
                serviceId: m['serviceId'] as String,
                userName: m['userName'] as String,
                comment: m['comment'] as String,
                rating: (m['rating'] as num).toDouble(),
                createdAt: DateTime.parse(m['createdAt'] as String),
              ))
          .toList();
      list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
      return list;
    }
    try {
      final db = await database;
      final maps = await db.query('reviews', orderBy: 'createdAt DESC');
      return List.generate(maps.length, (i) {
        return Review(
          id: maps[i]['id'] as String,
          serviceId: maps[i]['serviceId'] as String,
          userName: maps[i]['userName'] as String,
          comment: maps[i]['comment'] as String,
          rating: (maps[i]['rating'] as num).toDouble(),
          createdAt: DateTime.parse(maps[i]['createdAt'] as String),
        );
      });
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      final list = _fallbackReviews.values
          .map((m) => Review(
                id: m['id'] as String,
                serviceId: m['serviceId'] as String,
                userName: m['userName'] as String,
                comment: m['comment'] as String,
                rating: (m['rating'] as num).toDouble(),
                createdAt: DateTime.parse(m['createdAt'] as String),
              ))
          .toList();
      list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
      return list;
    }
  }

  Future<void> deleteReview(String id) async {
    if (_useInMemoryFallback) {
      _fallbackReviews.remove(id);
      return;
    }
    try {
      final db = await database;
      await db.delete('reviews', where: 'id = ?', whereArgs: [id]);
    } catch (e) {
      _useInMemoryFallback = true;
      _initFallbackData();
      _fallbackReviews.remove(id);
    }
  }

  Future<void> close() async {
    if (_useInMemoryFallback) return;
    try {
      final db = await database;
      await db.close();
      _database = null;
    } catch (_) {}
  }

  Map<String, dynamic> _serviceToMap(ServiceProvider service) {
    return {
      'id': service.id,
      'name': service.name,
      'category': service.category.toString().split('.').last,
      'description': service.description,
      'city': service.city,
      'phone': service.phone,
      'rating': service.rating,
      'latitude': service.latitude,
      'longitude': service.longitude,
      'isOpen': service.isOpen ? 1 : 0,
      'isFavorite': service.isFavorite ? 1 : 0,
      'isMine': service.isMine ? 1 : 0,
      'hours': service.hours,
      'servicesOffered': service.servicesOffered,
      'createdAt': DateTime.now().toIso8601String(),
    };
  }

  Map<String, dynamic> _serviceToUpdateMap(ServiceProvider service) {
    return {
      'id': service.id,
      'name': service.name,
      'category': service.category.toString().split('.').last,
      'description': service.description,
      'city': service.city,
      'phone': service.phone,
      'rating': service.rating,
      'latitude': service.latitude,
      'longitude': service.longitude,
      'isOpen': service.isOpen ? 1 : 0,
      'isFavorite': service.isFavorite ? 1 : 0,
      'isMine': service.isMine ? 1 : 0,
      'hours': service.hours,
      'servicesOffered': service.servicesOffered,
    };
  }

  ServiceProvider _mapToService(Map<String, dynamic> map) {
    return ServiceProvider(
      id: map['id'] as String,
      name: map['name'] as String,
      category: _stringToCategory(map['category'] as String),
      description: map['description'] as String,
      city: map['city'] as String,
      phone: map['phone'] as String,
      rating: (map['rating'] as num).toDouble(),
      latitude: (map['latitude'] as num).toDouble(),
      longitude: (map['longitude'] as num).toDouble(),
      isOpen: (map['isOpen'] as int) == 1,
      isFavorite: map['isFavorite'] != null ? (map['isFavorite'] as int) == 1 : false,
      isMine: map['isMine'] != null ? (map['isMine'] as int) == 1 : false,
      hours: map['hours'] as String?,
      servicesOffered: map['servicesOffered'] as String?,
    );
  }

  ServiceCategory _stringToCategory(String categoryString) {
    switch (categoryString) {
      case 'pieces':
        return ServiceCategory.pieces;
      case 'mecanicien':
        return ServiceCategory.mecanicien;
      case 'pneumatique':
        return ServiceCategory.pneumatique;
      case 'autre':
        return ServiceCategory.autre;
      default:
        return ServiceCategory.autre;
    }
  }
}
