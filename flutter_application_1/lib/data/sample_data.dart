import '../models/service_provider.dart';

final List<ServiceProvider> sampleProviders = [
  ServiceProvider(
    id: 'p1',
    name: 'Pièces Auto Mali',
    category: ServiceCategory.pieces,
    description:
        'Vente complète de pièces détachées, accessoires et batteries pour tous les modèles.',
    city: 'Bamako',
    phone: '+223 76 12 34 56',
    rating: 4.8,
    latitude: 12.6552,
    longitude: -8.0029,
  ),
  ServiceProvider(
    id: 'g1',
    name: 'Garage Mécanique Koulikoro',
    category: ServiceCategory.mecanicien,
    description:
        'Réparation moteur, entretien complet, diagnostic et révision.',
    city: 'Koulikoro',
    phone: '+223 75 23 45 67',
    rating: 4.6,
    latitude: 12.8699,
    longitude: -8.0026,
  ),
  ServiceProvider(
    id: 'p2',
    name: 'Pneus Experts',
    category: ServiceCategory.pneumatique,
    description:
        'Changement de pneus, équilibrage, réparation et vulcanisation.',
    city: 'Bamako',
    phone: '+223 77 34 56 78',
    rating: 4.7,
    latitude: 12.6650,
    longitude: -7.9950,
  ),
  ServiceProvider(
    id: 'c1',
    name: 'Carrosserie Excellence',
    category: ServiceCategory.autre,
    description:
        'Carrosserie, peinture, redressage, nettoyage et services d\'assistance.',
    city: 'Ségou',
    phone: '+223 76 45 67 89',
    rating: 4.5,
    latitude: 13.4549,
    longitude: -6.2630,
  ),
  ServiceProvider(
    id: 't1',
    name: 'Technic Pièces Mopti',
    category: ServiceCategory.pieces,
    description:
        'Pièces de rechange originales pour voitures asiatiques et européennes.',
    city: 'Mopti',
    phone: '+223 75 56 78 90',
    rating: 4.4,
    latitude: 14.2738,
    longitude: -4.1895,
  ),
  ServiceProvider(
    id: 'm1',
    name: 'Mécanique Rapide Kayes',
    category: ServiceCategory.mecanicien,
    description:
        'Entretien périodique, freins, suspension, vidange et diagnostic.',
    city: 'Kayes',
    phone: '+223 76 67 89 01',
    rating: 4.3,
    latitude: 14.1450,
    longitude: -11.4373,
  ),
  ServiceProvider(
    id: 'p3',
    name: 'Pneumatiques Gao',
    category: ServiceCategory.pneumatique,
    description: 'Vente et réparation de pneus toutes tailles, service rapide.',
    city: 'Gao',
    phone: '+223 77 78 89 01',
    rating: 4.6,
    latitude: 16.2742,
    longitude: -0.0471,
    
  ),
];
