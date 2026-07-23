import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:provider/provider.dart';
import 'package:geolocator/geolocator.dart';
import '../models/service_provider.dart';
import '../providers/service_provider.dart' as sn;
import 'provider_detail_screen.dart';

class MapScreen extends StatefulWidget {
  const MapScreen({super.key});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  GoogleMapController? _mapController;
  Position? _userPosition;
  bool _isLoadingLocation = true;

  // Position par défaut sur Bamako, Mali si le GPS est désactivé ou refusé
  static const LatLng _defaultCenter = LatLng(12.6392, -8.0029);

  @override
  void initState() {
    super.initState();
    _determinePosition();
  }

  Future<void> _determinePosition() async {
    try {
      final permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        final request = await Geolocator.requestPermission();
        if (request == LocationPermission.denied ||
            request == LocationPermission.deniedForever) {
          setState(() {
            _isLoadingLocation = false;
          });
          return;
        }
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.medium,
        timeLimit: const Duration(seconds: 5),
      );

      setState(() {
        _userPosition = position;
        _isLoadingLocation = false;
      });

      if (_mapController != null) {
        _mapController!.animateCamera(
          CameraUpdate.newLatLng(
            LatLng(position.latitude, position.longitude),
          ),
        );
      }
    } catch (e) {
      setState(() {
        _isLoadingLocation = false;
      });
    }
  }

  double _getMarkerHue(ServiceCategory category) {
    switch (category) {
      case ServiceCategory.pieces:
        return BitmapDescriptor.hueGreen;
      case ServiceCategory.mecanicien:
        return BitmapDescriptor.hueBlue;
      case ServiceCategory.pneumatique:
        return BitmapDescriptor.hueOrange;
      case ServiceCategory.autre:
        return BitmapDescriptor.hueViolet;
    }
  }

  Set<Marker> _buildMarkers(List<ServiceProvider> providers) {
    final Set<Marker> markers = {};

    // Ajouter la position de l'utilisateur si disponible
    if (_userPosition != null) {
      markers.add(
        Marker(
          markerId: const MarkerId('user_location'),
          position: LatLng(_userPosition!.latitude, _userPosition!.longitude),
          infoWindow: const InfoWindow(title: 'Votre position'),
          icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueRed),
        ),
      );
    }

    // Ajouter les prestataires
    for (final provider in providers) {
      markers.add(
        Marker(
          markerId: MarkerId(provider.id),
          position: LatLng(provider.latitude, provider.longitude),
          infoWindow: InfoWindow(
            title: provider.name,
            snippet: '${provider.category.title} • ${provider.city}',
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => ProviderDetailScreen(
                    provider: provider,
                    distance: _userPosition != null
                        ? provider.getDistance(
                            _userPosition!.latitude,
                            _userPosition!.longitude,
                          )
                        : null,
                  ),
                ),
              );
            },
          ),
          icon: BitmapDescriptor.defaultMarkerWithHue(
            _getMarkerHue(provider.category),
          ),
        ),
      );
    }

    return markers;
  }

  @override
  Widget build(BuildContext context) {
    final providers = context.watch<sn.ServicesNotifier>().services;
    final initialCenter = _userPosition != null
        ? LatLng(_userPosition!.latitude, _userPosition!.longitude)
        : _defaultCenter;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Carte des Prestataires'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.my_location),
            onPressed: () {
              setState(() => _isLoadingLocation = true);
              _determinePosition();
            },
            tooltip: 'Ma position',
          ),
        ],
      ),
      body: Stack(
        children: [
          GoogleMap(
            onMapCreated: (controller) => _mapController = controller,
            initialCameraPosition: CameraPosition(
              target: initialCenter,
              zoom: 12.0,
            ),
            markers: _buildMarkers(providers),
            myLocationEnabled: true,
            myLocationButtonEnabled: false,
            zoomControlsEnabled: true,
          ),
          if (_isLoadingLocation)
            const Center(
              child: Card(
                elevation: 4,
                child: Padding(
                  padding: EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      CircularProgressIndicator(),
                      SizedBox(width: 16),
                      Text('Récupération du GPS...'),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
