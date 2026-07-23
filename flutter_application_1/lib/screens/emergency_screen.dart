import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';

class EmergencyScreen extends StatefulWidget {
  const EmergencyScreen({super.key});

  @override
  State<EmergencyScreen> createState() => _EmergencyScreenState();
}

class _EmergencyScreenState extends State<EmergencyScreen> with SingleTickerProviderStateMixin {
  Position? _currentPosition;
  bool _isLoadingGPS = false;
  String _gpsStatus = "Cliquez sur 'Actualiser' pour récupérer votre position";
  late AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);
    _getGPSLocation(silent: true);
  }

  @override
  void dispose() {
    _pulseController.dispose();
    super.dispose();
  }

  Future<void> _getGPSLocation({bool silent = false}) async {
    if (!mounted) return;
    setState(() {
      _isLoadingGPS = true;
      _gpsStatus = "Recherche des coordonnées GPS...";
    });

    try {
      final permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        final request = await Geolocator.requestPermission();
        if (request == LocationPermission.denied ||
            request == LocationPermission.deniedForever) {
          setState(() {
            _gpsStatus = "Permission GPS refusée par l'utilisateur.";
            _isLoadingGPS = false;
          });
          return;
        }
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
        timeLimit: const Duration(seconds: 8),
      );

      if (!mounted) return;
      setState(() {
        _currentPosition = position;
        _isLoadingGPS = false;
        _gpsStatus = "Position acquise avec succès !";
      });
      
      if (!silent) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Position GPS mise à jour.')),
        );
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _gpsStatus = "Erreur lors de la localisation : $e";
        _isLoadingGPS = false;
      });
    }
  }

  Future<void> _callNumber(String phoneNumber) async {
    final Uri url = Uri(scheme: 'tel', path: phoneNumber.replaceAll(' ', ''));
    if (await canLaunchUrl(url)) {
      await launchUrl(url);
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Impossible d\'appeler le $phoneNumber')),
        );
      }
    }
  }

  void _shareLocation() {
    if (_currentPosition == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Veuillez d\'abord actualiser votre position GPS.'),
          backgroundColor: Colors.redAccent,
        ),
      );
      return;
    }

    final lat = _currentPosition!.latitude;
    final lng = _currentPosition!.longitude;
    final shareText = "🔴 URGENCE PANNE AUTO - MALI 🔴\n"
        "Je suis en panne et j'ai besoin d'assistance.\n"
        "Voici ma position actuelle :\n"
        "📍 Lien Google Maps : https://maps.google.com/?q=$lat,$lng\n"
        "Coordonnées : Lat $lat, Lng $lng";

    Share.share(shareText);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Assistance Panne & Urgence'),
        backgroundColor: const Color(0xFFD32F2F),
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Alert Card
            Card(
              color: Colors.red.shade50,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(color: Colors.red.shade200, width: 1.5),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    AnimatedBuilder(
                      animation: _pulseController,
                      builder: (context, child) {
                        return Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.red.withOpacity(0.1 + (0.2 * _pulseController.value)),
                            shape: BoxShape.circle,
                          ),
                          child: Icon(
                            Icons.warning_amber_rounded,
                            color: Colors.red.shade700,
                            size: 36,
                          ),
                        );
                      },
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Besoin d\'assistance immédiate ?',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: Colors.red.shade900,
                            ),
                          ),
                          const SizedBox(height: 4),
                          const Text(
                            'Partagez votre position GPS avec un mécanicien ou un dépanneur, ou contactez directement les services ci-dessous.',
                            style: TextStyle(fontSize: 12, height: 1.4),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // GPS Location Card
            const Text(
              'Ma localisation GPS',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Card(
              elevation: 3,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              color: Colors.white,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          children: [
                            const Icon(Icons.my_location, color: Colors.blueAccent),
                            const SizedBox(width: 8),
                            Text(
                              _currentPosition != null ? 'Position capturée' : 'Position inconnue',
                              style: const TextStyle(fontWeight: FontWeight.bold),
                            ),
                          ],
                        ),
                        if (_isLoadingGPS)
                          const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        else
                          IconButton(
                            icon: const Icon(Icons.refresh, color: Color(0xFF1565C0)),
                            onPressed: () => _getGPSLocation(),
                            tooltip: 'Actualiser le GPS',
                          ),
                      ],
                    ),
                    const Divider(height: 20),
                    if (_currentPosition != null) ...[
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceAround,
                        children: [
                          Column(
                            children: [
                              const Text('Latitude', style: TextStyle(color: Colors.grey, fontSize: 12)),
                              const SizedBox(height: 4),
                              Text(
                                _currentPosition!.latitude.toStringAsFixed(6),
                                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                              ),
                            ],
                          ),
                          Column(
                            children: [
                              const Text('Longitude', style: TextStyle(color: Colors.grey, fontSize: 12)),
                              const SizedBox(height: 4),
                              Text(
                                _currentPosition!.longitude.toStringAsFixed(6),
                                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                              ),
                            ],
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                    ],
                    Text(
                      _gpsStatus,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 12,
                        color: _currentPosition != null ? Colors.green.shade700 : Colors.grey.shade600,
                        fontStyle: FontStyle.italic,
                      ),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: _currentPosition != null ? _shareLocation : null,
                      icon: const Icon(Icons.share),
                      label: const Text('Partager ma position par SMS/WhatsApp'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFD32F2F),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                        elevation: 2,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Towing Services
            const Text(
              'Services de Dépannage & Remorquage',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            _buildTowingServiceList(),
            const SizedBox(height: 24),

            // Emergency Contacts
            const Text(
              'Numéros d\'Urgence Nationaux',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            _buildEmergencyContactsList(),
          ],
        ),
      ),
    );
  }

  Widget _buildTowingServiceList() {
    final services = [
      {'name': 'Remorquage Auto Mali 24/7', 'phone': '+223 70 00 01 02', 'region': 'Bamako & environs'},
      {'name': 'Dépannage Rapide Koulikoro', 'phone': '+223 60 10 20 30', 'region': 'Koulikoro / Kati'},
      {'name': 'Assistance Routière Ségou', 'phone': '+223 80 20 30 40', 'region': 'Ségou & Centre'},
      {'name': 'SOS Dépannage Mopti', 'phone': '+223 75 56 78 90', 'region': 'Mopti / Sevaré'},
    ];

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      color: Colors.white,
      child: ListView.separated(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        itemCount: services.length,
        separatorBuilder: (context, index) => const Divider(height: 1),
        itemBuilder: (context, index) {
          final s = services[index];
          return ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.orange.shade50,
              child: const Icon(Icons.car_repair, color: Colors.orange),
            ),
            title: Text(s['name']!, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Text('📍 ${s['region']!} • ${s['phone']!}'),
            trailing: IconButton(
              icon: const Icon(Icons.phone, color: Colors.green),
              onPressed: () => _callNumber(s['phone']!),
            ),
          );
        },
      ),
    );
  }

  Widget _buildEmergencyContactsList() {
    final contacts = [
      {'name': 'Protection Civile (Pompiers)', 'number': '18', 'desc': 'Secours, incendies et accidents'},
      {'name': 'Police Secours', 'number': '17', 'desc': 'Sécurité et agressions'},
      {'name': 'Gendarmerie Nationale', 'number': '80 00 11 15', 'desc': 'Assistance sur les axes routiers'},
      {'name': 'SAMU / Urgences Médicales', 'number': '15', 'desc': 'Urgences médicales et ambulances'},
    ];

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      color: Colors.white,
      child: ListView.separated(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        itemCount: contacts.length,
        separatorBuilder: (context, index) => const Divider(height: 1),
        itemBuilder: (context, index) {
          final c = contacts[index];
          return ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.red.shade50,
              child: const Icon(Icons.local_phone, color: Colors.red),
            ),
            title: Text(c['name']!, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Text('${c['desc']!} • N° : ${c['number']!}'),
            trailing: IconButton(
              icon: const Icon(Icons.phone, color: Colors.green),
              onPressed: () => _callNumber(c['number']!),
            ),
          );
        },
      ),
    );
  }
}
