import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/service_provider.dart' as sn;
import '../models/service_provider.dart';
import '../models/review.dart';
import '../services/database_helper.dart';
import 'provider_detail_screen.dart';
import 'add_service_screen.dart';

class AdminDashboardScreen extends StatefulWidget {
  const AdminDashboardScreen({super.key});

  @override
  State<AdminDashboardScreen> createState() => _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends State<AdminDashboardScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<Review> _allReviews = [];
  bool _isLoadingReviews = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadAllReviews();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadAllReviews() async {
    setState(() => _isLoadingReviews = true);
    try {
      final reviews = await DatabaseHelper().getAllReviews();
      setState(() {
        _allReviews = reviews;
        _isLoadingReviews = false;
      });
    } catch (_) {
      setState(() => _isLoadingReviews = false);
    }
  }

  Future<void> _deleteReview(Review review) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Modération - Supprimer cet avis ?'),
        content: Text('Voulez-vous supprimer définitivement l\'avis de "${review.userName}" ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            style: TextButton.styleFrom(foregroundColor: Colors.redAccent),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );

    if (confirm == true && mounted) {
      try {
        await context.read<sn.ServicesNotifier>().removeReview(review.id, review.serviceId);
        _loadAllReviews();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Avis supprimé avec succès (modéré)')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Erreur: $e')),
          );
        }
      }
    }
  }

  Future<void> _deleteProvider(ServiceProvider provider) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Administration - Supprimer ?'),
        content: Text('Voulez-vous supprimer définitivement "${provider.name}" ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            style: TextButton.styleFrom(foregroundColor: Colors.redAccent),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );

    if (confirm == true && mounted) {
      try {
        await context.read<sn.ServicesNotifier>().deleteService(provider.id);
        _loadAllReviews(); // Rafraîchir car les avis liés ont été supprimés par CASCADE
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Prestataire supprimé globalement')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Erreur: $e')),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final servicesNotifier = context.watch<sn.ServicesNotifier>();
    final allServices = servicesNotifier.services;

    // Calculs stats
    final totalServices = allServices.length;
    final openServices = allServices.where((s) => s.isOpen).length;
    final totalReviewsCount = _allReviews.length;
    final double avgRating = totalServices == 0
        ? 0.0
        : double.parse((allServices.fold(0.0, (acc, s) => acc + s.rating) / totalServices).toStringAsFixed(1));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Console Administration'),
        backgroundColor: const Color(0xFF0F172A),
        foregroundColor: Colors.white,
        bottom: TabBar(
          controller: _tabController,
          labelColor: const Color(0xFF00B0FF),
          unselectedLabelColor: Colors.white54,
          indicatorColor: const Color(0xFF00B0FF),
          tabs: const [
            Tab(icon: Icon(Icons.storefront), text: 'Prestataires'),
            Tab(icon: Icon(Icons.rate_review), text: 'Modération Avis'),
          ],
        ),
      ),
      body: Container(
        width: double.infinity,
        height: double.infinity,
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFF0F172A), Color(0xFF070A13)],
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
          ),
        ),
        child: Column(
          children: [
            // Section Stats Globale
            Padding(
              padding: const EdgeInsets.all(16),
              child: GridView.count(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                crossAxisCount: 2,
                crossAxisSpacing: 12,
                mainAxisSpacing: 12,
                childAspectRatio: 2.2,
                children: [
                  _buildStatCard('Prestataires', '$totalServices', Icons.business, const Color(0xFF00B0FF)),
                  _buildStatCard('Ouverts', '$openServices', Icons.store, const Color(0xFF00E676)),
                  _buildStatCard('Note Moyenne', '$avgRating/5', Icons.star, const Color(0xFFFF9100)),
                  _buildStatCard('Avis Totaux', '$totalReviewsCount', Icons.message, const Color(0xFF9C27B0)),
                ],
              ),
            ),

            // Contenu des onglets
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  // Onglet Prestataires
                  _buildProvidersTab(allServices),
                  // Onglet Avis
                  _buildReviewsTab(allServices),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatCard(String label, String value, IconData icon, Color color) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.03),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.06), width: 1.2),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Row(
        children: [
          CircleAvatar(
            backgroundColor: color.withOpacity(0.12),
            radius: 18,
            child: Icon(icon, color: color, size: 18),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  value,
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
                ),
                Text(
                  label,
                  style: const TextStyle(color: Colors.white54, fontSize: 10),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProvidersTab(List<ServiceProvider> providers) {
    if (providers.isEmpty) {
      return const Center(child: Text('Aucun prestataire en base.', style: TextStyle(color: Colors.white54)));
    }

    return ListView.builder(
      padding: const EdgeInsets.all(12),
      itemCount: providers.length,
      itemBuilder: (context, index) {
        final p = providers[index];
        return Card(
          color: Colors.white.withOpacity(0.02),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: BorderSide(color: Colors.white.withOpacity(0.05)),
          ),
          margin: const EdgeInsets.only(bottom: 12),
          child: ListTile(
            contentPadding: const EdgeInsets.all(8),
            leading: CircleAvatar(
              backgroundColor: p.category.color.withOpacity(0.15),
              child: Icon(p.category.icon, color: p.category.color),
            ),
            title: Text(p.name, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14)),
            subtitle: Text('📍 ${p.city} • ${p.category.title}', style: const TextStyle(color: Colors.white54, fontSize: 12)),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.edit, color: Colors.blueAccent),
                  onPressed: () async {
                    final notifier = context.read<sn.ServicesNotifier>();
                    final result = await Navigator.of(context).push<bool>(
                      MaterialPageRoute(
                        builder: (_) => AddServiceScreen(service: p, isMine: p.isMine),
                      ),
                    );
                    if ((result ?? false) && mounted) {
                      await notifier.refreshServices();
                    }
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.delete_outline, color: Colors.redAccent),
                  onPressed: () => _deleteProvider(p),
                ),
              ],
            ),
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => ProviderDetailScreen(provider: p),
                ),
              );
            },
          ),
        );
      },
    );
  }

  Widget _buildReviewsTab(List<ServiceProvider> providers) {
    if (_isLoadingReviews) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_allReviews.isEmpty) {
      return const Center(child: Text('Aucun avis à modérer.', style: TextStyle(color: Colors.white54)));
    }

    return ListView.builder(
      padding: const EdgeInsets.all(12),
      itemCount: _allReviews.length,
      itemBuilder: (context, index) {
        final r = _allReviews[index];
        
        // Trouver le prestataire lié
        final provider = providers.firstWhere(
          (s) => s.id == r.serviceId,
          orElse: () => ServiceProvider(
            id: '',
            name: 'Service Inconnu',
            category: ServiceCategory.autre,
            description: '',
            city: '',
            phone: '',
            rating: 0.0,
            latitude: 0,
            longitude: 0,
          ),
        );

        return Card(
          color: Colors.white.withOpacity(0.02),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: BorderSide(color: Colors.white.withOpacity(0.05)),
          ),
          margin: const EdgeInsets.only(bottom: 12),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      r.userName,
                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13),
                    ),
                    Row(
                      children: List.generate(5, (starIndex) {
                        return Icon(
                          starIndex < r.rating ? Icons.star : Icons.star_border,
                          color: Colors.orange,
                          size: 14,
                        );
                      }),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  r.comment,
                  style: const TextStyle(color: Colors.white70, fontSize: 13, height: 1.4),
                ),
                const Divider(height: 16, color: Colors.white10),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Text(
                        'Concerne : ${provider.name}',
                        style: const TextStyle(color: Colors.white38, fontSize: 11, fontStyle: FontStyle.italic),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.delete_sweep, color: Colors.redAccent, size: 20),
                      onPressed: () => _deleteReview(r),
                      tooltip: 'Modérer cet avis',
                      constraints: const BoxConstraints(),
                      padding: EdgeInsets.zero,
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
