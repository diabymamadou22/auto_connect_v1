import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:share_plus/share_plus.dart';
import 'package:uuid/uuid.dart';
import '../models/service_provider.dart';
import '../models/review.dart';
import '../providers/service_provider.dart' as sn;
import '../providers/auth_provider.dart';
import 'add_service_screen.dart';

class ProviderDetailScreen extends StatefulWidget {
  final ServiceProvider provider;
  final double? distance;

  const ProviderDetailScreen({
    super.key,
    required this.provider,
    this.distance,
  });

  @override
  State<ProviderDetailScreen> createState() => _ProviderDetailScreenState();
}

class _ProviderDetailScreenState extends State<ProviderDetailScreen> {
  late ServiceProvider _currentProvider;
  List<Review> _reviews = [];
  bool _isLoadingReviews = true;

  String _getCategoryImage(ServiceCategory category) {
    switch (category) {
      case ServiceCategory.pieces:
        return 'https://images.unsplash.com/photo-1507136566006-cfc505b114fc?auto=format&fit=crop&q=80&w=600';
      case ServiceCategory.mecanicien:
        return 'https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&q=80&w=600';
      case ServiceCategory.pneumatique:
        return 'https://images.unsplash.com/photo-1578844251758-2f71da64c96f?auto=format&fit=crop&q=80&w=600';
      case ServiceCategory.autre:
        return 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&q=80&w=600';
    }
  }

  @override
  void initState() {
    super.initState();
    _currentProvider = widget.provider;
    _loadReviews();
  }

  Future<void> _loadReviews() async {
    setState(() => _isLoadingReviews = true);
    try {
      final reviews = await context.read<sn.ServicesNotifier>().getReviews(_currentProvider.id);
      setState(() {
        _reviews = reviews;
        _isLoadingReviews = false;
      });
    } catch (e) {
      setState(() => _isLoadingReviews = false);
    }
  }

  Future<void> _launchPhone(String phoneNumber) async {
    final Uri url = Uri(scheme: 'tel', path: phoneNumber);
    if (await canLaunchUrl(url)) {
      await launchUrl(url);
    }
  }

  Future<void> _launchNavigation(double lat, double lng) async {
    final Uri url = Uri.parse(
      'https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving',
    );
    if (await canLaunchUrl(url)) {
      await launchUrl(url, mode: LaunchMode.externalApplication);
    }
  }

  void _shareProvider() {
    final text = 'Auto Connect Mali\n'
        'Prestataire : ${_currentProvider.name}\n'
        'Catégorie : ${_currentProvider.category.title}\n'
        'Ville : ${_currentProvider.city}\n'
        'Tél : ${_currentProvider.phone}\n'
        'Note : ${_currentProvider.rating}/5\n'
        'Localisation : https://maps.google.com/?q=${_currentProvider.latitude},${_currentProvider.longitude}';
    Share.share(text);
  }  void _showAddReviewSheet() {
    final nameController = TextEditingController();
    final commentController = TextEditingController();
    double selectedRating = 5.0;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: const Color(0xFF1E293B),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) {
        return StatefulBuilder(
          builder: (ctx, setModalState) {
            return Padding(
              padding: EdgeInsets.only(
                left: 20,
                right: 20,
                top: 20,
                bottom: MediaQuery.of(context).viewInsets.bottom + 20,
              ),
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Center(
                      child: Container(
                        width: 40,
                        height: 5,
                        decoration: BoxDecoration(
                          color: Colors.white24,
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Laisser un avis',
                      style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white),
                    ),
                    const SizedBox(height: 20),
                    TextField(
                      controller: nameController,
                      style: const TextStyle(color: Colors.white),
                      decoration: InputDecoration(
                        labelText: 'Votre Nom',
                        labelStyle: const TextStyle(color: Colors.white54),
                        prefixIcon: const Icon(Icons.person, color: Colors.white54),
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: Colors.white.withOpacity(0.1)),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: _currentProvider.category.color),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Note globale',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Colors.white70),
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: List.generate(5, (index) {
                        final starValue = index + 1.0;
                        return IconButton(
                          icon: Icon(
                            selectedRating >= starValue ? Icons.star : Icons.star_border,
                            color: Colors.amber,
                            size: 36,
                          ),
                          onPressed: () {
                            setModalState(() {
                              selectedRating = starValue;
                            });
                          },
                        );
                      }),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: commentController,
                      style: const TextStyle(color: Colors.white),
                      decoration: InputDecoration(
                        labelText: 'Votre Commentaire',
                        labelStyle: const TextStyle(color: Colors.white54),
                        prefixIcon: const Icon(Icons.comment, color: Colors.white54),
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: Colors.white.withOpacity(0.1)),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: _currentProvider.category.color),
                        ),
                      ),
                      maxLines: 3,
                    ),
                    const SizedBox(height: 24),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: () async {
                          final messenger = ScaffoldMessenger.of(context);
                          final navigator = Navigator.of(context);
                          final notifier = context.read<sn.ServicesNotifier>();

                          if (nameController.text.trim().isEmpty ||
                              commentController.text.trim().isEmpty) {
                            messenger.showSnackBar(
                              const SnackBar(content: Text('Veuillez remplir tous les champs')),
                            );
                            return;
                          }

                          final review = Review(
                            id: const Uuid().v4(),
                            serviceId: _currentProvider.id,
                            userName: nameController.text.trim(),
                            comment: commentController.text.trim(),
                            rating: selectedRating,
                            createdAt: DateTime.now(),
                          );

                          navigator.pop();

                          try {
                            await notifier.addReview(review);
                            // Mettre à jour la note du prestataire en local
                            final updatedService = notifier.services.firstWhere((s) => s.id == _currentProvider.id);
                            if (mounted) {
                              setState(() {
                                _currentProvider = updatedService;
                              });
                              _loadReviews();
                              messenger.showSnackBar(
                                const SnackBar(content: Text('Avis ajouté avec succès !')),
                              );
                            }
                          } catch (e) {
                            if (mounted) {
                              messenger.showSnackBar(
                                SnackBar(content: Text('Erreur: $e')),
                              );
                            }
                          }
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: _currentProvider.category.color,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(vertical: 14),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                        ),
                        child: const Text('Soumettre l\'avis'),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final currentUser = context.watch<AuthNotifier>().currentUser;
    final categoryColor = _currentProvider.category.color;

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.white,
        title: Text(
          _currentProvider.name,
          style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white),
        ),
        actions: [
          // Bouton Favori
          IconButton(
            icon: Icon(
              _currentProvider.isFavorite ? Icons.favorite : Icons.favorite_border,
              color: _currentProvider.isFavorite ? Colors.red : null,
            ),
            onPressed: () async {
              final notifier = context.read<sn.ServicesNotifier>();
              await notifier.toggleFavorite(_currentProvider.id);
              if (!mounted) return;
              final updated = notifier.services.firstWhere((s) => s.id == _currentProvider.id);
              setState(() {
                _currentProvider = updated;
              });
            },
            tooltip: 'Favori',
          ),
          // Bouton Partager
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: _shareProvider,
            tooltip: 'Partager',
          ),
          // Boutons de gestion (Si c'est mon service OU si c'est un admin)
          if (_currentProvider.isMine || currentUser?.role == 'admin') ...[
            IconButton(
              icon: const Icon(Icons.edit),
              onPressed: () async {
                final navigator = Navigator.of(context);
                final notifier = context.read<sn.ServicesNotifier>();
                final result = await navigator.push<bool>(
                  MaterialPageRoute(
                    builder: (_) => AddServiceScreen(service: _currentProvider),
                  ),
                );
                if (result == true && mounted) {
                  final updated = notifier.services.firstWhere((s) => s.id == _currentProvider.id);
                  setState(() {
                    _currentProvider = updated;
                  });
                }
              },
              tooltip: 'Modifier',
            ),
            IconButton(
              icon: const Icon(Icons.delete),
              onPressed: () async {
                final navigator = Navigator.of(context);
                final messenger = ScaffoldMessenger.of(context);
                final notifier = context.read<sn.ServicesNotifier>();
                final delete = await showDialog<bool>(
                  context: context,
                  builder: (dialogContext) => AlertDialog(
                    title: const Text('Supprimer ce service ?'),
                    content: const Text(
                      'Cette action est irréversible. Voulez-vous vraiment supprimer ce service ?',
                    ),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.of(dialogContext).pop(false),
                        child: const Text('Annuler'),
                      ),
                      TextButton(
                        onPressed: () => Navigator.of(dialogContext).pop(true),
                        child: const Text('Supprimer'),
                      ),
                    ],
                  ),
                );
                if (delete == true) {
                  await notifier.deleteService(_currentProvider.id);
                  if (mounted) {
                    messenger.showSnackBar(
                      const SnackBar(content: Text('Service supprimé')),
                    );
                    navigator.pop(true);
                  }
                }
              },
              tooltip: 'Supprimer',
            ),
          ],
        ],
      ),
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFF0F172A), Color(0xFF070A13)],
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
          ),
        ),
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header
              SizedBox(
                height: 240,
                width: double.infinity,
                child: Stack(
                  children: [
                    // Image de couverture Unsplash
                    Positioned.fill(
                      child: Image.network(
                        _getCategoryImage(_currentProvider.category),
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Container(color: _currentProvider.category.color),
                      ),
                    ),
                    // Overlay dégradé
                    Positioned.fill(
                      child: Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            colors: [Colors.transparent, Colors.black.withOpacity(0.85)],
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                          ),
                        ),
                      ),
                    ),
                    // Infos de l'en-tête
                    Positioned(
                      bottom: 24,
                      left: 24,
                      right: 24,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: _currentProvider.category.color,
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(_currentProvider.category.icon, color: Colors.white, size: 16),
                                const SizedBox(width: 6),
                                Text(
                                  _currentProvider.category.title,
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                    fontSize: 12,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 12),
                          Text(
                            _currentProvider.name,
                            style: const TextStyle(
                              fontSize: 26,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // Contenu
              Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Statut et Note
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: _currentProvider.isOpen
                                ? Colors.green.withAlpha(51)
                                : Colors.red.withAlpha(51),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            _currentProvider.isOpen ? '🟢 Ouvert' : '🔴 Fermé',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: _currentProvider.isOpen ? Colors.green : Colors.red,
                            ),
                          ),
                        ),
                        Row(
                          children: [
                            const Icon(
                              Icons.star,
                              color: Colors.amber,
                              size: 24,
                            ),
                            const SizedBox(width: 4),
                            Text(
                              '${_currentProvider.rating}/5',
                              style: const TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                                color: Colors.white,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),

                    const SizedBox(height: 16),

                    // Horaires d'ouverture
                    Container(
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      width: double.infinity,
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.04),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: Colors.white.withOpacity(0.08),
                          width: 1.2,
                        ),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Row(
                          children: [
                            Icon(Icons.access_time, color: categoryColor),
                            const SizedBox(width: 16),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    'Horaires d\'ouverture',
                                    style: TextStyle(
                                      fontSize: 12,
                                      fontWeight: FontWeight.bold,
                                      color: Colors.white54,
                                    ),
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    _currentProvider.hours ?? 'Non spécifiés',
                                    style: const TextStyle(
                                      fontSize: 15,
                                      fontWeight: FontWeight.w500,
                                      color: Colors.white,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),

                    const SizedBox(height: 12),

                    // Localisation & Mini-carte
                    Container(
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      width: double.infinity,
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.04),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: Colors.white.withOpacity(0.08),
                          width: 1.2,
                        ),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Localisation',
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: Colors.white54,
                              ),
                            ),
                            const SizedBox(height: 12),
                            Row(
                              children: [
                                const Icon(
                                  Icons.location_on,
                                  color: Colors.redAccent,
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  _currentProvider.city,
                                  style: const TextStyle(fontSize: 16, color: Colors.white),
                                ),
                              ],
                            ),
                            if (widget.distance != null) ...[
                              const SizedBox(height: 8),
                              Row(
                                children: [
                                  const Icon(
                                    Icons.straight,
                                    color: Colors.blueAccent,
                                  ),
                                  const SizedBox(width: 8),
                                  Text(
                                    '${widget.distance!.toStringAsFixed(1)} km de vous',
                                    style: const TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.w500,
                                      color: Colors.white,
                                    ),
                                  ),
                                ],
                              ),
                            ],
                            const SizedBox(height: 16),
                            SizedBox(
                              height: 160,
                              child: ClipRRect(
                                borderRadius: BorderRadius.circular(12),
                                child: GoogleMap(
                                  initialCameraPosition: CameraPosition(
                                    target: LatLng(_currentProvider.latitude, _currentProvider.longitude),
                                    zoom: 14.0,
                                  ),
                                  markers: {
                                    Marker(
                                      markerId: MarkerId(_currentProvider.id),
                                      position: LatLng(_currentProvider.latitude, _currentProvider.longitude),
                                      infoWindow: InfoWindow(title: _currentProvider.name),
                                    ),
                                  },
                                  liteModeEnabled: true,
                                  zoomControlsEnabled: false,
                                  myLocationButtonEnabled: false,
                                  scrollGesturesEnabled: false,
                                  zoomGesturesEnabled: false,
                                  tiltGesturesEnabled: false,
                                  rotateGesturesEnabled: false,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),

                    const SizedBox(height: 16),

                    // Services spécifiques proposés (Spécialités)
                    if (_currentProvider.servicesOffered != null && _currentProvider.servicesOffered!.isNotEmpty) ...[
                      const Text(
                        'Prestations proposées',
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
                      ),
                      const SizedBox(height: 8),
                      Wrap(
                        spacing: 8,
                        runSpacing: 4,
                        children: _currentProvider.servicesOffered!
                            .split(',')
                            .map((s) => s.trim())
                            .where((s) => s.isNotEmpty)
                            .map((serviceName) {
                              return Chip(
                                label: Text(
                                  serviceName,
                                  style: const TextStyle(fontSize: 12, color: Colors.white),
                                ),
                                backgroundColor: categoryColor.withOpacity(0.15),
                                side: BorderSide(color: categoryColor.withOpacity(0.3), width: 1),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(8),
                                ),
                              );
                            }).toList(),
                      ),
                      const SizedBox(height: 16),
                    ],

                    // Description
                    const Text(
                      'À propos',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      _currentProvider.description,
                      style: const TextStyle(fontSize: 14, height: 1.6, color: Colors.white70),
                    ),

                    const SizedBox(height: 20),

                    // Contact
                    Container(
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      width: double.infinity,
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.04),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: Colors.white.withOpacity(0.08),
                          width: 1.2,
                        ),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Contact',
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: Colors.white54,
                              ),
                            ),
                            const SizedBox(height: 12),
                            Row(
                              children: [
                                const Icon(Icons.phone, color: Colors.greenAccent),
                                const SizedBox(width: 12),
                                Text(
                                  _currentProvider.phone,
                                  style: const TextStyle(fontSize: 16, color: Colors.white, fontWeight: FontWeight.w500),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),

                    const SizedBox(height: 24),

                    // Actions Guidage & Téléphone
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: () => _launchPhone(_currentProvider.phone),
                            icon: const Icon(Icons.phone),
                            label: const Text('Appeler'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 14),
                              backgroundColor: Colors.green,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(10),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: () => _launchNavigation(_currentProvider.latitude, _currentProvider.longitude),
                            icon: const Icon(Icons.directions),
                            label: const Text('Itinéraire'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 14),
                              backgroundColor: categoryColor,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(10),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 32),
                    const Divider(color: Colors.white10),
                    const SizedBox(height: 16),

                    // Section des Avis
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Avis & Commentaires',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white),
                        ),
                        TextButton.icon(
                          onPressed: _showAddReviewSheet,
                          icon: Icon(Icons.rate_review, color: categoryColor),
                          label: Text(
                            'Donner mon avis',
                            style: TextStyle(color: categoryColor, fontWeight: FontWeight.bold),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),

                    if (_isLoadingReviews)
                      const Center(child: CircularProgressIndicator())
                    else if (_reviews.isEmpty)
                      const Padding(
                        padding: EdgeInsets.symmetric(vertical: 24.0),
                        child: Center(
                          child: Text(
                            'Aucun avis pour le moment.\nSoyez le premier à donner votre avis !',
                            textAlign: TextAlign.center,
                            style: TextStyle(color: Colors.white38, height: 1.4),
                          ),
                        ),
                      )
                    else
                      ListView.separated(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: _reviews.length,
                        separatorBuilder: (_, __) => const Divider(color: Colors.white10, height: 24),
                        itemBuilder: (context, index) {
                          final r = _reviews[index];
                          return Container(
                            padding: const EdgeInsets.all(12),
                            margin: const EdgeInsets.symmetric(vertical: 4),
                            decoration: BoxDecoration(
                              color: Colors.white.withOpacity(0.02),
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(color: Colors.white.withOpacity(0.04)),
                            ),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text(
                                      r.userName,
                                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Colors.white),
                                    ),
                                    Row(
                                      children: [
                                        ...List.generate(5, (starIndex) {
                                          return Icon(
                                            starIndex < r.rating ? Icons.star : Icons.star_border,
                                            color: Colors.orange,
                                            size: 14,
                                          );
                                        }),
                                        if (currentUser?.role == 'admin') ...[
                                          const SizedBox(width: 8),
                                          IconButton(
                                            icon: const Icon(Icons.delete_sweep, color: Colors.redAccent, size: 20),
                                            padding: EdgeInsets.zero,
                                            constraints: const BoxConstraints(),
                                            onPressed: () async {
                                              // Capturer les références avant l'await pour éviter
                                              // l'utilisation de BuildContext après un gap async
                                              final notifier = context.read<sn.ServicesNotifier>();
                                              final messenger = ScaffoldMessenger.of(context);
                                              final confirm = await showDialog<bool>(
                                                context: context,
                                                builder: (dialogContext) => AlertDialog(
                                                  title: const Text('Supprimer ce commentaire ?'),
                                                  content: const Text(
                                                    'Voulez-vous vraiment supprimer cet avis ? Cette action est irréversible.',
                                                  ),
                                                  actions: [
                                                    TextButton(
                                                      onPressed: () => Navigator.of(dialogContext).pop(false),
                                                      child: const Text('Annuler'),
                                                    ),
                                                    TextButton(
                                                      onPressed: () => Navigator.of(dialogContext).pop(true),
                                                      child: const Text('Supprimer'),
                                                    ),
                                                  ],
                                                ),
                                              );
                                              if (confirm == true) {
                                                try {
                                                  await notifier.removeReview(r.id, _currentProvider.id);
                                                  final updated = notifier.services.firstWhere((s) => s.id == _currentProvider.id);
                                                  if (mounted) {
                                                    setState(() {
                                                      _currentProvider = updated;
                                                    });
                                                    _loadReviews();
                                                    messenger.showSnackBar(
                                                      const SnackBar(content: Text('Avis supprimé avec succès')),
                                                    );
                                                  }
                                                } catch (e) {
                                                  if (mounted) {
                                                    messenger.showSnackBar(
                                                      SnackBar(content: Text('Erreur lors de la suppression : $e')),
                                                    );
                                                  }
                                                }
                                              }
                                            },
                                            tooltip: 'Supprimer l\'avis',
                                          ),
                                        ],
                                      ],
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 6),
                                Text(
                                  r.comment,
                                  style: const TextStyle(fontSize: 13, height: 1.4, color: Colors.white70),
                                ),
                                const SizedBox(height: 6),
                                Text(
                                  '${r.createdAt.day}/${r.createdAt.month}/${r.createdAt.year}',
                                  style: const TextStyle(fontSize: 11, color: Colors.white38),
                                ),
                              ],
                            ),
                          );
                        },
                      ),
                    const SizedBox(height: 24),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
