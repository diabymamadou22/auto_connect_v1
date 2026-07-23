import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/service_provider.dart';
import '../providers/service_provider.dart' as sn;
import 'add_service_screen.dart';
import 'provider_detail_screen.dart';

class ProviderPortalScreen extends StatelessWidget {
  const ProviderPortalScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final servicesNotifier = context.watch<sn.ServicesNotifier>();
    final myServices = servicesNotifier.services.where((s) => s.isMine).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Espace Professionnel'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
      ),
      body: myServices.isEmpty
          ? Center(
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(
                      Icons.storefront,
                      size: 72,
                      color: Colors.grey,
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Aucun service professionnel enregistré',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Vous pouvez enregistrer votre garage, atelier mécanique ou boutique de pièces pour commencer à les gérer depuis cet espace.',
                      style: TextStyle(color: Colors.grey, fontSize: 14),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 24),
                    ElevatedButton.icon(
                      onPressed: () async {
                        final notifier = context.read<sn.ServicesNotifier>();
                        final result = await Navigator.of(context).push<bool>(
                          MaterialPageRoute(
                            builder: (_) => const AddServiceScreen(isMine: true),
                          ),
                        );
                        if ((result ?? false) && context.mounted) {
                          await notifier.refreshServices();
                        }
                      },
                      icon: const Icon(Icons.add),
                      label: const Text('Enregistrer mon service'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF1565C0),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                      ),
                    ),
                  ],
                ),
              ),
            )
          : ListView.builder(
              padding: const EdgeInsets.all(12),
              itemCount: myServices.length,
              itemBuilder: (context, index) {
                final provider = myServices[index];
                return Card(
                  elevation: 3,
                  margin: const EdgeInsets.only(bottom: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Column(
                    children: [
                      ListTile(
                        contentPadding: const EdgeInsets.all(12),
                        leading: CircleAvatar(
                          backgroundColor: provider.category.color.withAlpha(25),
                          radius: 24,
                          child: Icon(provider.category.icon, color: provider.category.color, size: 28),
                        ),
                        title: Text(
                          provider.name,
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                        ),
                        subtitle: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const SizedBox(height: 4),
                            Text('📍 ${provider.city} • 📞 ${provider.phone}'),
                            if (provider.hours != null) ...[
                              const SizedBox(height: 4),
                              Row(
                                children: [
                                  const Icon(Icons.access_time, size: 12, color: Colors.grey),
                                  const SizedBox(width: 4),
                                  Text(
                                    provider.hours!,
                                    style: const TextStyle(fontSize: 12, color: Colors.grey),
                                  ),
                                ],
                              ),
                            ],
                            const SizedBox(height: 4),
                            Text(
                              provider.description,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(fontSize: 12, color: Colors.grey),
                            ),
                          ],
                        ),
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) => ProviderDetailScreen(provider: provider),
                            ),
                          );
                        },
                      ),
                      const Divider(height: 1),
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Row(
                              children: [
                                const Text(
                                  'Statut :',
                                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                                ),
                                const SizedBox(width: 8),
                                Switch(
                                  value: provider.isOpen,
                                  onChanged: (value) async {
                                    await context.read<sn.ServicesNotifier>().toggleServiceStatus(provider.id, value);
                                    if (context.mounted) {
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        SnackBar(
                                          content: Text(value
                                              ? 'Votre service est désormais ouvert'
                                              : 'Votre service est désormais fermé'),
                                          duration: const Duration(seconds: 1),
                                        ),
                                      );
                                    }
                                  },
                                ),
                                Text(
                                  provider.isOpen ? 'Ouvert' : 'Fermé',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    color: provider.isOpen ? Colors.green : Colors.red,
                                    fontSize: 13,
                                  ),
                                ),
                              ],
                            ),
                            Row(
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.edit, color: Colors.blue),
                                  onPressed: () async {
                                    final notifier = context.read<sn.ServicesNotifier>();
                                    final result = await Navigator.of(context).push<bool>(
                                      MaterialPageRoute(
                                        builder: (_) => AddServiceScreen(service: provider, isMine: true),
                                      ),
                                    );
                                    if ((result ?? false) && context.mounted) {
                                      await notifier.refreshServices();
                                    }
                                  },
                                  tooltip: 'Modifier',
                                ),
                                IconButton(
                                  icon: const Icon(Icons.delete, color: Colors.red),
                                  onPressed: () async {
                                    final notifier = context.read<sn.ServicesNotifier>();
                                    final confirm = await showDialog<bool>(
                                      context: context,
                                      builder: (ctx) => AlertDialog(
                                        title: const Text('Supprimer mon service ?'),
                                        content: const Text('Voulez-vous vraiment supprimer définitivement ce service pro ?'),
                                        actions: [
                                          TextButton(
                                            onPressed: () => Navigator.of(ctx).pop(false),
                                            child: const Text('Annuler'),
                                          ),
                                          TextButton(
                                            onPressed: () => Navigator.of(ctx).pop(true),
                                            child: const Text('Supprimer'),
                                          ),
                                        ],
                                      ),
                                    );
                                    if (confirm == true) {
                                      await notifier.deleteService(provider.id);
                                    }
                                  },
                                  tooltip: 'Supprimer',
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
      floatingActionButton: myServices.isEmpty
          ? null
          : FloatingActionButton.extended(
              onPressed: () async {
                final notifier = context.read<sn.ServicesNotifier>();
                final result = await Navigator.of(context).push<bool>(
                  MaterialPageRoute(
                    builder: (_) => const AddServiceScreen(isMine: true),
                  ),
                );
                if ((result ?? false) && context.mounted) {
                  await notifier.refreshServices();
                }
              },
              icon: const Icon(Icons.add),
              label: const Text('Ajouter un service'),
              backgroundColor: const Color(0xFF1565C0),
            ),
    );
  }
}
