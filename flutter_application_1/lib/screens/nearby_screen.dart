import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:provider/provider.dart';
import '../models/service_provider.dart';
import '../providers/service_provider.dart' as sn;
import 'provider_detail_screen.dart';

class NearbyScreen extends StatefulWidget {
  const NearbyScreen({super.key});

  @override
  State<NearbyScreen> createState() => _NearbyScreenState();
}

class _NearbyScreenState extends State<NearbyScreen> {
  Position? userPosition;
  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    _getCurrentLocation();
  }

  Future<void> _getCurrentLocation() async {
    try {
      final permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        await Geolocator.requestPermission();
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
        timeLimit: const Duration(seconds: 10),
      );

      setState(() {
        userPosition = position;
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        errorMessage = 'Impossible d\'accéder à votre localisation : $e';
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return Scaffold(
        appBar: AppBar(title: const Text('Prestataires à proximité')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (errorMessage != null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Prestataires à proximité')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(errorMessage!),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () {
                  setState(() => isLoading = true);
                  _getCurrentLocation();
                },
                child: const Text('Réessayer'),
              ),
            ],
          ),
        ),
      );
    }

    if (userPosition == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Prestataires à proximité')),
        body: const Center(child: Text('Localisation non disponible')),
      );
    }

    final services = context.watch<sn.ServicesNotifier>().services;

    final sortedProviders = services.map((provider) {
      final distance = provider.getDistance(
        userPosition!.latitude,
        userPosition!.longitude,
      );
      return MapEntry(provider, distance);
    }).toList()..sort((a, b) => a.value.compareTo(b.value));

    return Scaffold(
      appBar: AppBar(title: const Text('Prestataires à proximité')),
      body: ListView.separated(
        padding: const EdgeInsets.all(12),
        itemCount: sortedProviders.length,
        separatorBuilder: (context, index) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final provider = sortedProviders[index].key;
          final distance = sortedProviders[index].value;

          return Card(
            elevation: 4,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
            child: ListTile(
              leading: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: provider.category.color.withAlpha(51),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(
                  provider.category.icon,
                  color: provider.category.color,
                  size: 28,
                ),
              ),
              title: Text(
                provider.name,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 4),
                  Text(
                    '📍 ${provider.city} • ${distance.toStringAsFixed(1)} km',
                    style: const TextStyle(fontSize: 12),
                  ),
                  Row(
                    children: [
                      const Icon(Icons.star, size: 14, color: Colors.orange),
                      const SizedBox(width: 4),
                      Text(
                        '${provider.rating}/5',
                        style: const TextStyle(fontSize: 12),
                      ),
                    ],
                  ),
                ],
              ),
              trailing: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: provider.isOpen
                          ? Colors.green.withAlpha(51)
                          : Colors.red.withAlpha(51),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Text(
                      provider.isOpen ? 'Ouvert' : 'Fermé',
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: provider.isOpen ? Colors.green : Colors.red,
                      ),
                    ),
                  ),
                ],
              ),
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => ProviderDetailScreen(
                      provider: provider,
                      distance: distance,
                    ),
                  ),
                );
              },
            ),
          );
        },
      ),
    );
  }
}
