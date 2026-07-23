import 'package:flutter/material.dart';
import '../models/service_provider.dart';
import '../models/review.dart';
import '../services/database_helper.dart';
import '../data/sample_data.dart';

class ServicesNotifier extends ChangeNotifier {
  List<ServiceProvider> _services = [];
  bool _isLoading = false;

  List<ServiceProvider> get services => _services;
  bool get isLoading => _isLoading;

  ServicesNotifier({
    List<ServiceProvider>? initialServices,
    bool autoLoad = true,
  }) : _services = initialServices ?? [] {
    if (autoLoad) {
      _initializeServices();
    }
  }

  Future<void> _initializeServices() async {
    _isLoading = true;
    notifyListeners();

    try {
      final dbServices = await DatabaseHelper().getAllServices();
      if (dbServices.isEmpty) {
        // Si la base est vide, charger les données d'exemple
        for (final service in sampleProviders) {
          await DatabaseHelper().insertService(service);
        }
        _services = sampleProviders;
      } else {
        _services = dbServices;
      }
    } catch (e) {
      // En cas d'erreur DB, conserver les données déjà en mémoire
      // et utiliser sampleProviders seulement si la liste est vide
      if (_services.isEmpty) {
        _services = sampleProviders;
      }
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<void> addService(ServiceProvider service) async {
    await DatabaseHelper().insertService(service);
    _services = [..._services, service];
    notifyListeners();
  }

  Future<void> deleteService(String id) async {
    try {
      await DatabaseHelper().deleteService(id);
      _services.removeWhere((s) => s.id == id);
      notifyListeners();
    } catch (e) {
      rethrow;
    }
  }

  Future<void> updateService(ServiceProvider service) async {
    try {
      await DatabaseHelper().updateService(service);
      final index = _services.indexWhere((s) => s.id == service.id);
      if (index != -1) {
        _services[index] = service;
        notifyListeners();
      }
    } catch (e) {
      rethrow;
    }
  }

  // Fonctionnalité 2 : Favoris Hors Ligne
  Future<void> toggleFavorite(String id) async {
    final index = _services.indexWhere((s) => s.id == id);
    if (index != -1) {
      final updated = _services[index].copyWith(isFavorite: !_services[index].isFavorite);
      try {
        await DatabaseHelper().updateService(updated);
        _services[index] = updated;
        notifyListeners();
      } catch (e) {
        rethrow;
      }
    }
  }

  // Fonctionnalité 3 : Avis & Commentaires
  Future<List<Review>> getReviews(String serviceId) async {
    return await DatabaseHelper().getReviewsForService(serviceId);
  }

  Future<void> addReview(Review review) async {
    try {
      await DatabaseHelper().insertReview(review);
      // Recalculer la note moyenne du prestataire
      final reviews = await DatabaseHelper().getReviewsForService(review.serviceId);
      if (reviews.isNotEmpty) {
        final double sum = reviews.fold(0.0, (acc, r) => acc + r.rating);
        final double average = double.parse((sum / reviews.length).toStringAsFixed(1));
        
        final index = _services.indexWhere((s) => s.id == review.serviceId);
        if (index != -1) {
          final updated = _services[index].copyWith(rating: average);
          await DatabaseHelper().updateService(updated);
          _services[index] = updated;
          notifyListeners();
        }
      }
    } catch (e) {
      rethrow;
    }
  }

  Future<void> removeReview(String reviewId, String serviceId) async {
    try {
      await DatabaseHelper().deleteReview(reviewId);
      // Recalculer la note moyenne du prestataire
      final reviews = await DatabaseHelper().getReviewsForService(serviceId);
      final index = _services.indexWhere((s) => s.id == serviceId);
      if (index != -1) {
        double average = 5.0;
        if (reviews.isNotEmpty) {
          final double sum = reviews.fold(0.0, (acc, r) => acc + r.rating);
          average = double.parse((sum / reviews.length).toStringAsFixed(1));
        }
        final updated = _services[index].copyWith(rating: average);
        await DatabaseHelper().updateService(updated);
        _services[index] = updated;
        notifyListeners();
      }
    } catch (e) {
      rethrow;
    }
  }

  // Fonctionnalité 5 : Commutateur Statut Ouvert/Fermé (Portail Prestataire)
  Future<void> toggleServiceStatus(String id, bool isOpen) async {
    final index = _services.indexWhere((s) => s.id == id);
    if (index != -1) {
      final updated = _services[index].copyWith(isOpen: isOpen);
      try {
        await DatabaseHelper().updateService(updated);
        _services[index] = updated;
        notifyListeners();
      } catch (e) {
        rethrow;
      }
    }
  }

  List<ServiceProvider> getServicesByCategory(ServiceCategory category) {
    return _services.where((s) => s.category == category).toList();
  }

  Future<void> refreshServices() async {
    await _initializeServices();
  }
}
