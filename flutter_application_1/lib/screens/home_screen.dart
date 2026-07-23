import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:geolocator/geolocator.dart';
import '../providers/auth_provider.dart';
import '../providers/service_provider.dart' as sn;
import '../models/service_provider.dart';
import 'nearby_screen.dart';
import 'add_service_screen.dart';
import 'map_screen.dart';
import 'provider_portal_screen.dart';
import 'provider_detail_screen.dart';
import 'provider_list_screen.dart';
import 'emergency_screen.dart';
import 'admin_dashboard_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';

  String _selectedCity = 'Toutes';
  bool _onlyOpen = false;
  String _sortBy = 'rating';
  Position? _userPosition;

  @override
  void initState() {
    super.initState();
    _searchController.addListener(() {
      setState(() {
        _searchQuery = _searchController.text;
      });
    });
    _determinePosition();
  }

  Future<void> _determinePosition() async {
    try {
      final permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.always ||
          permission == LocationPermission.whileInUse) {
        final position = await Geolocator.getCurrentPosition(
          desiredAccuracy: LocationAccuracy.medium,
        );
        setState(() {
          _userPosition = position;
        });
      }
    } catch (_) {}
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

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

  void _showFiltersSheet(List<String> cities) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      backgroundColor: Colors.white,
      builder: (context) {
        return StatefulBuilder(
          builder: (ctx, setStateSheet) {
            return Container(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Filtres',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: Colors.black87,
                        ),
                      ),
                      TextButton(
                        onPressed: () {
                          setStateSheet(() {
                            _selectedCity = 'Toutes';
                            _onlyOpen = false;
                            _sortBy = 'rating';
                          });
                          setState(() {
                            _selectedCity = 'Toutes';
                            _onlyOpen = false;
                            _sortBy = 'rating';
                          });
                        },
                        child: const Text('Réinitialiser', style: TextStyle(color: Colors.blue, fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  const Text('Ville', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black87, fontSize: 14)),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    value: _selectedCity,
                    dropdownColor: Colors.white,
                    style: const TextStyle(color: Colors.black87),
                    items: cities.map((city) {
                      return DropdownMenuItem(
                        value: city,
                        child: Text(city),
                      );
                    }).toList(),
                    onChanged: (value) {
                      if (value != null) {
                        setStateSheet(() => _selectedCity = value);
                        setState(() => _selectedCity = value);
                      }
                    },
                    decoration: InputDecoration(
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: BorderSide(color: Colors.grey[300]!),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: BorderSide(color: Colors.grey[300]!),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: const BorderSide(color: Colors.blue),
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),

                  SwitchListTile(
                    title: const Text(
                      'Ouvert actuellement',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Colors.black87),
                    ),
                    contentPadding: EdgeInsets.zero,
                    value: _onlyOpen,
                    activeColor: Colors.blue,
                    onChanged: (value) {
                      setStateSheet(() => _onlyOpen = value);
                      setState(() => _onlyOpen = value);
                    },
                  ),
                  const SizedBox(height: 20),

                  const Text('Trier par', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black87, fontSize: 14)),
                  const SizedBox(height: 8),
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: Row(
                      children: [
                        ChoiceChip(
                          label: const Text('Note'),
                          selected: _sortBy == 'rating',
                          selectedColor: Colors.blue[100],
                          labelStyle: TextStyle(color: _sortBy == 'rating' ? Colors.blue[900] : Colors.black54),
                          onSelected: (selected) {
                            if (selected) {
                              setStateSheet(() => _sortBy = 'rating');
                              setState(() => _sortBy = 'rating');
                            }
                          },
                        ),
                        const SizedBox(width: 8),
                        ChoiceChip(
                          label: const Text('Distance'),
                          selected: _sortBy == 'distance',
                          selectedColor: Colors.blue[100],
                          labelStyle: TextStyle(color: _sortBy == 'distance' ? Colors.blue[900] : Colors.black54),
                          onSelected: (selected) {
                            if (selected) {
                              setStateSheet(() => _sortBy = 'distance');
                              setState(() => _sortBy = 'distance');
                            }
                          },
                        ),
                        const SizedBox(width: 8),
                        ChoiceChip(
                          label: const Text('Nom'),
                          selected: _sortBy == 'name',
                          selectedColor: Colors.blue[100],
                          labelStyle: TextStyle(color: _sortBy == 'name' ? Colors.blue[900] : Colors.black54),
                          onSelected: (selected) {
                            if (selected) {
                              setStateSheet(() => _sortBy = 'name');
                              setState(() => _sortBy = 'name');
                            }
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),

                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton(
                      onPressed: () => Navigator.of(context).pop(),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      child: const Text('Appliquer', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.read<AuthNotifier>();
    final currentUser = auth.currentUser;
    final username = currentUser?.username ?? 'PILOTE';
    final roleName = currentUser?.role == 'admin'
        ? 'Admin'
        : (currentUser?.role == 'prestataire' ? 'Prestataire' : 'Utilisateur');

    return Scaffold(
      backgroundColor: Colors.white,
      drawer: _buildDrawer(username, roleName, currentUser?.role),
      body: Consumer<sn.ServicesNotifier>(
        builder: (context, servicesNotifier, child) {
          final allServices = servicesNotifier.services;

          final services = allServices.where((s) {
            final query = _searchQuery.toLowerCase();
            final matchesQuery = _searchQuery.isEmpty ||
                s.name.toLowerCase().contains(query) ||
                s.description.toLowerCase().contains(query) ||
                s.city.toLowerCase().contains(query);
            final matchesCity = _selectedCity == 'Toutes' ||
                s.city.toLowerCase() == _selectedCity.toLowerCase();
            final matchesOpen = !_onlyOpen || s.isOpen;
            return matchesQuery && matchesCity && matchesOpen;
          }).toList();

          if (_sortBy == 'name') {
            services.sort((a, b) => a.name.toLowerCase().compareTo(b.name.toLowerCase()));
          } else if (_sortBy == 'distance' && _userPosition != null) {
            services.sort((a, b) => a
                .getDistance(_userPosition!.latitude, _userPosition!.longitude)
                .compareTo(b.getDistance(_userPosition!.latitude, _userPosition!.longitude)));
          } else {
            services.sort((a, b) => b.rating.compareTo(a.rating));
          }

          final topRatedServices = List<ServiceProvider>.from(allServices)
            ..sort((a, b) => b.rating.compareTo(a.rating));
          final featuredProviders = topRatedServices.take(5).toList();
          final favorites = allServices.where((s) => s.isFavorite).toList();
          final cities = ['Toutes'] + allServices.map((s) => s.city).toSet().toList();

          return CustomScrollView(
            physics: const BouncingScrollPhysics(),
            slivers: [
              SliverAppBar(
                expandedHeight: 180,
                floating: false,
                pinned: true,
                stretch: true,
                backgroundColor: const Color(0xFF1565C0),
                actions: [
                  IconButton(
                    icon: const Icon(Icons.map, color: Colors.white),
                    onPressed: () {
                      Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => const MapScreen()),
                      );
                    },
                  ),
                ],
                title: const Text(
                  'Auto Connect',
                  style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 24),
                ),
                flexibleSpace: FlexibleSpaceBar(
                  background: Padding(
                    padding: const EdgeInsets.only(top: 90.0, left: 20, right: 20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Trouvez les meilleurs prestataires automobiles',
                          style: TextStyle(color: Colors.white, fontSize: 14),
                        ),
                        const SizedBox(height: 16),
                        Container(
                          height: 50,
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: TextField(
                            controller: _searchController,
                            decoration: InputDecoration(
                              hintText: 'Rechercher un service...',
                              hintStyle: TextStyle(color: Colors.grey[400]),
                              border: InputBorder.none,
                              prefixIcon: Icon(Icons.search, color: Colors.grey[400]),
                              suffixIcon: IconButton(
                                icon: Icon(Icons.tune, color: Colors.grey[400]),
                                onPressed: () => _showFiltersSheet(cities),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // SOS Urgence (Added to prevent loss of functionality)
                      GestureDetector(
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(builder: (_) => const EmergencyScreen()),
                          );
                        },
                        child: Container(
                          margin: const EdgeInsets.only(bottom: 16),
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              colors: [Colors.orange[700]!, Colors.red[700]!],
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                            ),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  color: Colors.white.withOpacity(0.2),
                                  shape: BoxShape.circle,
                                ),
                                child: const Icon(Icons.bolt, color: Colors.white, size: 28),
                              ),
                              const SizedBox(width: 16),
                              const Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      'SIGNAL SOS : PANNE ACTIVE',
                                      style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                                    ),
                                    SizedBox(height: 4),
                                    Text(
                                      'Transmettre la position GPS aux dépanneurs',
                                      style: TextStyle(color: Colors.white, fontSize: 11),
                                    ),
                                  ],
                                ),
                              ),
                              const Icon(Icons.chevron_right, color: Colors.white),
                            ],
                          ),
                        ),
                      ),

                      // Red Banner (Nearby)
                      GestureDetector(
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(builder: (_) => const NearbyScreen()),
                          );
                        },
                        child: Container(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: const Color(0xFFE53935),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.my_location, color: Colors.white, size: 28),
                              const SizedBox(width: 16),
                              const Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      'Prestataires à proximité',
                                      style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
                                    ),
                                    SizedBox(height: 4),
                                    Text(
                                      'Trouvez les services les plus proches',
                                      style: TextStyle(color: Colors.white, fontSize: 12),
                                    ),
                                  ],
                                ),
                              ),
                              const Icon(Icons.chevron_right, color: Colors.white),
                            ],
                          ),
                        ),
                      ),
                      
                      const SizedBox(height: 24),
                      const Text('Catégories de services', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.black87)),
                      const SizedBox(height: 16),
                      
                      GridView.count(
                        shrinkWrap: true,
                        primary: false,
                        crossAxisCount: 2,
                        crossAxisSpacing: 12,
                        mainAxisSpacing: 12,
                        childAspectRatio: 1.1,
                        children: [
                          _buildCategoryCard(context, 'Pièces auto', Icons.build, Colors.green[400]!, ServiceCategory.pieces),
                          _buildCategoryCard(context, 'Mécaniciens', Icons.handyman, Colors.blue[400]!, ServiceCategory.mecanicien),
                          _buildCategoryCard(context, 'Pneumatique', Icons.tire_repair, Colors.orange[400]!, ServiceCategory.pneumatique),
                          _buildCategoryCard(context, 'Autres services', Icons.more_horiz, Colors.purple[400]!, ServiceCategory.autre),
                        ],
                      ),
                      
                      const SizedBox(height: 24),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Top prestataires', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.black87)),
                          TextButton(
                            onPressed: () {
                              setState(() {
                                _selectedCity = 'Toutes';
                                _onlyOpen = false;
                                _sortBy = 'rating';
                              });
                            },
                            child: const Text('AFFICHER TOUT', style: TextStyle(color: Colors.blue, fontWeight: FontWeight.bold, fontSize: 12)),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      
                      if (featuredProviders.isEmpty)
                        const Center(child: Padding(padding: EdgeInsets.all(24.0), child: Text('Aucun prestataire trouvé', style: TextStyle(color: Colors.grey))))
                      else
                        SizedBox(
                          height: 200,
                          child: ListView.builder(
                            scrollDirection: Axis.horizontal,
                            physics: const BouncingScrollPhysics(),
                            itemCount: featuredProviders.length,
                            itemBuilder: (context, index) {
                              final provider = featuredProviders[index];
                              return _buildFeaturedProviderCard(context, provider);
                            },
                          ),
                        ),
                        
                      const SizedBox(height: 24),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Tous les prestataires', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.black87)),
                          IconButton(
                            icon: const Icon(Icons.tune, color: Colors.blue),
                            onPressed: () => _showFiltersSheet(cities),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      // Filtres récents in a single row
                      SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: Row(
                          children: [
                            if (_selectedCity != 'Toutes')
                              Padding(
                                padding: const EdgeInsets.only(right: 8.0),
                                child: Chip(
                                  label: Text(_selectedCity),
                                  onDeleted: () => setState(() => _selectedCity = 'Toutes'),
                                  backgroundColor: Colors.blue[50],
                                  deleteIcon: const Icon(Icons.close, size: 16),
                                ),
                              ),
                            if (_onlyOpen)
                              Padding(
                                padding: const EdgeInsets.only(right: 8.0),
                                child: Chip(
                                  label: const Text('Ouvert'),
                                  onDeleted: () => setState(() => _onlyOpen = false),
                                  backgroundColor: Colors.green[50],
                                  deleteIcon: const Icon(Icons.close, size: 16),
                                ),
                              ),
                            Padding(
                              padding: const EdgeInsets.only(right: 8.0),
                              child: Chip(
                                label: Text(_sortBy == 'rating' ? 'Mieux notés' : (_sortBy == 'distance' ? 'Plus proches' : 'Nom')),
                                onDeleted: _sortBy == 'rating' ? null : () => setState(() => _sortBy = 'rating'),
                                deleteIcon: const Icon(Icons.close, size: 16),
                                backgroundColor: Colors.blue[50],
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                      if (services.isEmpty)
                        const Center(child: Padding(padding: EdgeInsets.all(24.0), child: Text('Aucun prestataire trouvé', style: TextStyle(color: Colors.grey))))
                      else
                        ListView.builder(
                          shrinkWrap: true,
                          physics: const NeverScrollableScrollPhysics(),
                          itemCount: services.length,
                          itemBuilder: (context, index) {
                            final provider = services[index];
                            return _buildListProviderCard(context, provider);
                          },
                        ),

                      const SizedBox(height: 80),
                    ],
                  ),
                ),
              ),
            ],
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () async {
          final notifier = context.read<sn.ServicesNotifier>();
          final result = await Navigator.of(context).push<bool>(
            MaterialPageRoute(builder: (_) => const AddServiceScreen()),
          );
          if ((result ?? false) && mounted) {
            notifier.refreshServices();
          }
        },
        icon: const Icon(Icons.add, color: Colors.white),
        label: const Text('Ajouter un service', style: TextStyle(color: Colors.white)),
        backgroundColor: const Color(0xFF1565C0),
      ),
    );
  }
  
  Widget _buildCategoryCard(BuildContext context, String title, IconData icon, Color color, ServiceCategory category) {
    return GestureDetector(
      onTap: () {
        Navigator.of(context).push(MaterialPageRoute(builder: (_) => ProviderListScreen(category: category)));
      },
      child: Container(
        decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(16)),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(color: Colors.white.withOpacity(0.2), borderRadius: BorderRadius.circular(8)),
              child: Icon(icon, color: Colors.white, size: 28),
            ),
            Text(title, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
          ],
        ),
      ),
    );
  }
  
  Widget _buildFeaturedProviderCard(BuildContext context, ServiceProvider provider) {
    return GestureDetector(
      onTap: () {
        Navigator.of(context).push(MaterialPageRoute(builder: (_) => ProviderDetailScreen(provider: provider)));
      },
      child: Container(
        width: 260,
        margin: const EdgeInsets.only(right: 16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4))],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 110,
              decoration: BoxDecoration(
                borderRadius: const BorderRadius.only(topLeft: Radius.circular(16), topRight: Radius.circular(16)),
                image: DecorationImage(image: NetworkImage(_getCategoryImage(provider.category)), fit: BoxFit.cover),
              ),
              child: Stack(
                children: [
                  Positioned(
                    bottom: 8,
                    left: 8,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(color: provider.category.color, borderRadius: BorderRadius.circular(4)),
                      child: Text(provider.category.title.toUpperCase(), style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold)),
                    ),
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(provider.name, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.black87), maxLines: 1, overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.location_on, size: 12, color: Colors.grey),
                          const SizedBox(width: 4),
                          Text(provider.city, style: const TextStyle(color: Colors.grey, fontSize: 11)),
                        ],
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(color: Colors.blue[50], borderRadius: BorderRadius.circular(6)),
                        child: Row(
                          children: [
                            const Icon(Icons.star, size: 12, color: Colors.amber),
                            const SizedBox(width: 4),
                            Text(provider.rating.toString(), style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11, color: Colors.blue[900])),
                          ],
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildListProviderCard(BuildContext context, ServiceProvider provider) {
    return GestureDetector(
      onTap: () {
        Navigator.of(context).push(MaterialPageRoute(builder: (_) => ProviderDetailScreen(provider: provider)));
      },
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.grey[200]!),
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: provider.category.color.withOpacity(0.1), shape: BoxShape.circle),
              child: Icon(provider.category.icon, color: provider.category.color, size: 28),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(provider.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.black87)),
                  const SizedBox(height: 6),
                  Row(
                    children: [
                      const Icon(Icons.location_on, size: 14, color: Colors.grey),
                      const SizedBox(width: 4),
                      Text(provider.city, style: const TextStyle(fontSize: 12, color: Colors.grey)),
                      const SizedBox(width: 16),
                      const Icon(Icons.star, size: 14, color: Colors.amber),
                      const SizedBox(width: 4),
                      Text(provider.rating.toString(), style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.black87)),
                    ],
                  ),
                ],
              ),
            ),
            const Icon(Icons.chevron_right, color: Colors.grey),
          ],
        ),
      ),
    );
  }

  Drawer _buildDrawer(String username, String roleName, String? rawRole) {
    return Drawer(
      child: Container(
        color: Colors.white,
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            DrawerHeader(
              decoration: const BoxDecoration(
                color: Color(0xFF1565C0),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        backgroundColor: Colors.white,
                        radius: 26,
                        child: Text(
                          username.substring(0, 1).toUpperCase(),
                          style: const TextStyle(color: Color(0xFF1565C0), fontWeight: FontWeight.bold, fontSize: 20),
                        ),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              username,
                              style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            const SizedBox(height: 4),
                            Text(
                              roleName,
                              style: const TextStyle(color: Colors.white70, fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            ListTile(
              leading: const Icon(Icons.dashboard, color: Colors.blue),
              title: const Text('Accueil', style: TextStyle(fontWeight: FontWeight.bold)),
              onTap: () => Navigator.of(context).pop(),
            ),
            ListTile(
              leading: const Icon(Icons.map, color: Colors.blue),
              title: const Text('Carte'),
              onTap: () {
                Navigator.of(context).pop();
                Navigator.of(context).push(MaterialPageRoute(builder: (_) => const MapScreen()));
              },
            ),
            ListTile(
              leading: const Icon(Icons.my_location, color: Colors.blue),
              title: const Text('Prestataires à proximité'),
              onTap: () {
                Navigator.of(context).pop();
                Navigator.of(context).push(MaterialPageRoute(builder: (_) => const NearbyScreen()));
              },
            ),
            ListTile(
              leading: const Icon(Icons.bolt, color: Colors.red),
              title: const Text('Urgence SOS', style: TextStyle(fontWeight: FontWeight.bold)),
              onTap: () {
                Navigator.of(context).pop();
                Navigator.of(context).push(MaterialPageRoute(builder: (_) => const EmergencyScreen()));
              },
            ),
            if (rawRole == 'admin') ...[
              const Divider(),
              ListTile(
                leading: const Icon(Icons.admin_panel_settings, color: Colors.purple),
                title: const Text('Administration'),
                onTap: () {
                  Navigator.of(context).pop();
                  Navigator.of(context).push(MaterialPageRoute(builder: (_) => const AdminDashboardScreen()));
                },
              ),
            ],
            const Divider(),
            ListTile(
              leading: const Icon(Icons.business, color: Colors.orange),
              title: const Text('Portail Prestataire'),
              onTap: () {
                Navigator.of(context).pop();
                if (rawRole != 'prestataire' && rawRole != 'admin') {
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Accès refusé.')));
                  return;
                }
                Navigator.of(context).push(MaterialPageRoute(builder: (_) => const ProviderPortalScreen()));
              },
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.logout, color: Colors.red),
              title: const Text('Déconnexion', style: TextStyle(color: Colors.red)),
              onTap: () {
                Navigator.of(context).pop();
                context.read<AuthNotifier>().logout();
              },
            ),
          ],
        ),
      ),
    );
  }
}
