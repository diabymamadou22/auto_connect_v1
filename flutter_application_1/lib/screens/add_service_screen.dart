import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:uuid/uuid.dart';
import 'package:geolocator/geolocator.dart';
import '../models/service_provider.dart';
import '../providers/service_provider.dart' as sn;

class AddServiceScreen extends StatefulWidget {
  final ServiceProvider? service;
  final bool isMine;

  const AddServiceScreen({super.key, this.service, this.isMine = false});

  @override
  State<AddServiceScreen> createState() => _AddServiceScreenState();
}

class _AddServiceScreenState extends State<AddServiceScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _phoneController = TextEditingController();
  final _cityController = TextEditingController();
  final _hoursController = TextEditingController();

  ServiceCategory _selectedCategory = ServiceCategory.pieces;
  double _rating = 5.0;
  double _latitude = 12.6552;
  double _longitude = -8.0029;
  bool _isLoading = false;
  bool _isOpen = true;

  final List<String> _availableSubServices = [
    'Diagnostic complet',
    'Vidange moteur',
    'Changement de pneus',
    'Système de freinage',
    'Climatisation auto',
    'Parallélisme / Équilibrage',
    'Électricité / Batterie',
    'Peinture / Carrosserie',
    'Dépannage / Remorquage',
    'Pièces neuves',
    'Pièces d\'occasion',
  ];
  List<String> _selectedSubServices = [];

  @override
  void initState() {
    super.initState();
    final service = widget.service;
    if (service != null) {
      _nameController.text = service.name;
      _descriptionController.text = service.description;
      _phoneController.text = service.phone;
      _cityController.text = service.city;
      _hoursController.text = service.hours ?? '';
      _selectedCategory = service.category;
      _rating = service.rating;
      _latitude = service.latitude;
      _longitude = service.longitude;
      _isOpen = service.isOpen;
      if (service.servicesOffered != null) {
        _selectedSubServices = service.servicesOffered!
            .split(',')
            .map((s) => s.trim())
            .where((s) => s.isNotEmpty)
            .toList();
      }
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    _phoneController.dispose();
    _cityController.dispose();
    _hoursController.dispose();
    super.dispose();
  }

  Future<void> _getCurrentLocation() async {
    setState(() => _isLoading = true);
    try {
      final permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        final request = await Geolocator.requestPermission();
        if (request == LocationPermission.denied ||
            request == LocationPermission.deniedForever) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Permission GPS refusée')),
            );
          }
          return;
        }
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
        timeLimit: const Duration(seconds: 10),
      );

      if (mounted) {
        setState(() {
          _latitude = position.latitude;
          _longitude = position.longitude;
        });
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Localisation mise à jour')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur GPS: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _saveService() async {
    if (!_formKey.currentState!.validate()) return;

    // Capturer les références AVANT les gaps async
    final notifier = context.read<sn.ServicesNotifier>();
    final messenger = ScaffoldMessenger.of(context);

    setState(() => _isLoading = true);

    try {
      final service = ServiceProvider(
        id: widget.service?.id ?? const Uuid().v4(),
        name: _nameController.text.trim(),
        category: _selectedCategory,
        description: _descriptionController.text.trim(),
        city: _cityController.text.trim(),
        phone: _phoneController.text.trim(),
        rating: _rating,
        latitude: _latitude,
        longitude: _longitude,
        isOpen: _isOpen,
        isFavorite: widget.service?.isFavorite ?? false,
        isMine: widget.service?.isMine ?? widget.isMine,
        hours: _hoursController.text.trim().isEmpty
            ? null
            : _hoursController.text.trim(),
        servicesOffered:
            _selectedSubServices.isEmpty ? null : _selectedSubServices.join(','),
      );

      if (widget.service == null) {
        await notifier.addService(service);
      } else {
        await notifier.updateService(service);
      }

      messenger.showSnackBar(
        SnackBar(
          content: Text(
            widget.service == null
                ? 'Service ajouté avec succès'
                : 'Service modifié avec succès',
          ),
        ),
      );

      if (mounted) Navigator.of(context).pop(true);
    } catch (e) {
      messenger.showSnackBar(SnackBar(content: Text('Erreur: $e')));
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
          widget.service == null ? 'Ajouter un service' : 'Modifier le service',
        ),
        backgroundColor: const Color(0xFF1565C0),
      ),
      body: Form(
        key: _formKey,
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Catégorie
              const Text(
                'Catégorie de service',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              ),
              const SizedBox(height: 8),
              DropdownButtonFormField<ServiceCategory>(
                value: _selectedCategory,
                items: ServiceCategory.values.map((category) {
                  return DropdownMenuItem(
                    value: category,
                    child: Row(
                      children: [
                        Icon(category.icon, color: category.color),
                        const SizedBox(width: 8),
                        Text(category.title),
                      ],
                    ),
                  );
                }).toList(),
                onChanged: (value) {
                  if (value != null) {
                    setState(() => _selectedCategory = value);
                  }
                },
                decoration: InputDecoration(
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  contentPadding: const EdgeInsets.symmetric(horizontal: 12),
                ),
              ),
              const SizedBox(height: 20),

              // Nom
              TextFormField(
                controller: _nameController,
                decoration: InputDecoration(
                  labelText: 'Nom du service',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  prefixIcon: const Icon(Icons.business),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Le nom est obligatoire';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              // Description
              TextFormField(
                controller: _descriptionController,
                decoration: InputDecoration(
                  labelText: 'Description',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  prefixIcon: const Icon(Icons.description),
                ),
                maxLines: 3,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'La description est obligatoire';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              // Téléphone
              TextFormField(
                controller: _phoneController,
                decoration: InputDecoration(
                  labelText: 'Téléphone',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  prefixIcon: const Icon(Icons.phone),
                ),
                keyboardType: TextInputType.phone,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Le téléphone est obligatoire';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              // Ville
              TextFormField(
                controller: _cityController,
                decoration: InputDecoration(
                  labelText: 'Ville',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  prefixIcon: const Icon(Icons.location_city),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'La ville est obligatoire';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              // Horaires de travail
              TextFormField(
                controller: _hoursController,
                decoration: InputDecoration(
                  labelText: 'Horaires d\'ouverture (Ex: Lun-Sam 8h-18h)',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  prefixIcon: const Icon(Icons.access_time),
                ),
              ),
              const SizedBox(height: 16),

              // Sous-services/Spécialités
              Card(
                margin: const EdgeInsets.only(bottom: 16),
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Prestations proposées (Spécialités)',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Wrap(
                        spacing: 8,
                        runSpacing: 4,
                        children: _availableSubServices.map((subService) {
                          final isSelected =
                              _selectedSubServices.contains(subService);
                          return FilterChip(
                            label: Text(
                              subService,
                              style: const TextStyle(fontSize: 12),
                            ),
                            selected: isSelected,
                            onSelected: (selected) {
                              setState(() {
                                if (selected) {
                                  _selectedSubServices.add(subService);
                                } else {
                                  _selectedSubServices.remove(subService);
                                }
                              });
                            },
                            selectedColor:
                                _selectedCategory.color.withOpacity(0.25),
                            checkmarkColor: _selectedCategory.color,
                          );
                        }).toList(),
                      ),
                    ],
                  ),
                ),
              ),

              // Localisation GPS
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Localisation GPS',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Latitude: ${_latitude.toStringAsFixed(4)}',
                        style: const TextStyle(fontSize: 12),
                      ),
                      Text(
                        'Longitude: ${_longitude.toStringAsFixed(4)}',
                        style: const TextStyle(fontSize: 12),
                      ),
                      const SizedBox(height: 12),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          onPressed: _isLoading ? null : _getCurrentLocation,
                          icon: const Icon(Icons.my_location),
                          label: const Text('Obtenir ma localisation'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF2196F3),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Note
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text(
                            'Note initiale',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 14,
                            ),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 12,
                              vertical: 4,
                            ),
                            decoration: BoxDecoration(
                              color: Colors.orange.withAlpha(51),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              '${_rating.toStringAsFixed(1)}/5',
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 14,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Slider(
                        value: _rating,
                        min: 1,
                        max: 5,
                        divisions: 8,
                        onChanged: (value) {
                          setState(() => _rating = value);
                        },
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Statut
              CheckboxListTile(
                title: const Text('Actuellement ouvert'),
                value: _isOpen,
                onChanged: (value) {
                  setState(() => _isOpen = value ?? true);
                },
                controlAffinity: ListTileControlAffinity.leading,
              ),
              const SizedBox(height: 24),

              // Bouton de sauvegarde
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: _isLoading ? null : _saveService,
                  icon: _isLoading
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2,
                            valueColor:
                                AlwaysStoppedAnimation<Color>(Colors.white),
                          ),
                        )
                      : const Icon(Icons.save),
                  label: _isLoading
                      ? const Text('Enregistrement...')
                      : Text(
                          widget.service == null
                              ? 'Ajouter le service'
                              : 'Enregistrer les modifications',
                        ),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: _selectedCategory.color,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
