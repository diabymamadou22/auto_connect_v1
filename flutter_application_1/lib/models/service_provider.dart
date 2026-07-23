import 'package:flutter/material.dart';
import 'dart:math' as math;

enum ServiceCategory { pieces, mecanicien, pneumatique, autre }

extension ServiceCategoryExtension on ServiceCategory {
  String get title {
    switch (this) {
      case ServiceCategory.pieces:
        return 'Pièces auto';
      case ServiceCategory.mecanicien:
        return 'Mécaniciens';
      case ServiceCategory.pneumatique:
        return 'Pneumatique';
      case ServiceCategory.autre:
        return 'Autres services';
    }
  }

  IconData get icon {
    switch (this) {
      case ServiceCategory.pieces:
        return Icons.build;
      case ServiceCategory.mecanicien:
        return Icons.handyman;
      case ServiceCategory.pneumatique:
        return Icons.tire_repair;
      case ServiceCategory.autre:
        return Icons.more_horiz;
    }
  }

  Color get color {
    switch (this) {
      case ServiceCategory.pieces:
        return const Color(0xFF4CAF50);
      case ServiceCategory.mecanicien:
        return const Color(0xFF2196F3);
      case ServiceCategory.pneumatique:
        return const Color(0xFFFF9800);
      case ServiceCategory.autre:
        return const Color(0xFF9C27B0);
    }
  }
}

class ServiceProvider {
  final String id;
  final String name;
  final ServiceCategory category;
  final String description;
  final String city;
  final String phone;
  final double rating;
  final double latitude;
  final double longitude;
  final bool isOpen;
  final bool isFavorite;
  final bool isMine;
  final String? hours;
  final String? servicesOffered;

  ServiceProvider({
    required this.id,
    required this.name,
    required this.category,
    required this.description,
    required this.city,
    required this.phone,
    required this.rating,
    required this.latitude,
    required this.longitude,
    this.isOpen = true,
    this.isFavorite = false,
    this.isMine = false,
    this.hours,
    this.servicesOffered,
  });

  ServiceProvider copyWith({
    String? id,
    String? name,
    ServiceCategory? category,
    String? description,
    String? city,
    String? phone,
    double? rating,
    double? latitude,
    double? longitude,
    bool? isOpen,
    bool? isFavorite,
    bool? isMine,
    String? hours,
    String? servicesOffered,
  }) {
    return ServiceProvider(
      id: id ?? this.id,
      name: name ?? this.name,
      category: category ?? this.category,
      description: description ?? this.description,
      city: city ?? this.city,
      phone: phone ?? this.phone,
      rating: rating ?? this.rating,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      isOpen: isOpen ?? this.isOpen,
      isFavorite: isFavorite ?? this.isFavorite,
      isMine: isMine ?? this.isMine,
      hours: hours ?? this.hours,
      servicesOffered: servicesOffered ?? this.servicesOffered,
    );
  }

  double getDistance(double userLat, double userLng) {
    const double earthRadius = 6371;
    final double dLat = _degreesToRadians(latitude - userLat);
    final double dLng = _degreesToRadians(longitude - userLng);
    final double a =
        (math.sin(dLat / 2) * math.sin(dLat / 2)) +
        (math.cos(_degreesToRadians(userLat)) *
            math.cos(_degreesToRadians(latitude)) *
            math.sin(dLng / 2) *
            math.sin(dLng / 2));
    final double c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a));
    return earthRadius * c;
  }

  double _degreesToRadians(double degrees) {
    return degrees * (math.pi / 180);
  }
}
