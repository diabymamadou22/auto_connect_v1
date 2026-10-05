package com.example.autoconnect.data

import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider

object SampleData {
    val sampleProviders = listOf(
        ServiceProvider(
            id = "p1",
            name = "Pièces Auto Mali",
            category = ServiceCategory.PIECES,
            description = "Vente complète de pièces détachées, accessoires et batteries pour tous les modèles.",
            city = "Bamako",
            phone = "+223 76 12 34 56",
            rating = 4.8,
            latitude = 12.6552,
            longitude = -8.0029,
            hours = "08:00 - 19:00",
            servicesOffered = "Vente pièces détachées, Batteries, Filtres, Amortisseurs"
        ),
        ServiceProvider(
            id = "g1",
            name = "Garage Mécanique Koulikoro",
            category = ServiceCategory.MECANICIEN,
            description = "Réparation moteur, entretien complet, diagnostic et révision.",
            city = "Koulikoro",
            phone = "+223 75 23 45 67",
            rating = 4.6,
            latitude = 12.8699,
            longitude = -8.0026,
            hours = "07:30 - 18:30",
            servicesOffered = "Diagnostic électronique, Révision moteur, Freinage",
            isMine = true
        ),
        ServiceProvider(
            id = "p2",
            name = "Pneus Experts",
            category = ServiceCategory.PNEUMATIQUE,
            description = "Changement de pneus, équilibrage, réparation et vulcanisation.",
            city = "Bamako",
            phone = "+223 77 34 56 78",
            rating = 4.7,
            latitude = 12.6650,
            longitude = -7.9950,
            hours = "08:00 - 20:00",
            servicesOffered = "Montage pneus, Équilibrage, Parallélisme, Vulcanisation"
        ),
        ServiceProvider(
            id = "c1",
            name = "Carrosserie Excellence",
            category = ServiceCategory.AUTRE,
            description = "Carrosserie, peinture, redressage, nettoyage et services d'assistance.",
            city = "Ségou",
            phone = "+223 76 45 67 89",
            rating = 4.5,
            latitude = 13.4549,
            longitude = -6.2630,
            hours = "08:00 - 17:30",
            servicesOffered = "Peinture au four, Redressage châssis, Tolerie"
        ),
        ServiceProvider(
            id = "t1",
            name = "Technic Pièces Mopti",
            category = ServiceCategory.PIECES,
            description = "Pièces de rechange originales pour voitures asiatiques et européennes.",
            city = "Mopti",
            phone = "+223 75 56 78 90",
            rating = 4.4,
            latitude = 14.2738,
            longitude = -4.1895,
            hours = "08:00 - 18:00",
            servicesOffered = "Pièces Toyota, Hyundai, Peugeot, Renault"
        ),
        ServiceProvider(
            id = "m1",
            name = "Mécanique Rapide Kayes",
            category = ServiceCategory.MECANICIEN,
            description = "Entretien périodique, freins, suspension, vidange et diagnostic.",
            city = "Kayes",
            phone = "+223 76 67 89 01",
            rating = 4.3,
            latitude = 14.1450,
            longitude = -11.4373,
            hours = "07:00 - 19:00",
            servicesOffered = "Vidange express, Freins, Embrayage, Climatisation"
        ),
        ServiceProvider(
            id = "m2",
            name = "Atelier Mécanique ACI 2000",
            category = ServiceCategory.MECANICIEN,
            description = "Atelier moderne haute précision : valise diagnostic multimarque, réfection moteur et dépannage rapide.",
            city = "Bamako",
            phone = "+223 70 11 22 33",
            rating = 4.9,
            latitude = 12.6315,
            longitude = -8.0210,
            hours = "08:00 - 19:30",
            servicesOffered = "Diagnostic OBD, Injection diesel, Courroie distribution, Freinage ABS"
        ),
        ServiceProvider(
            id = "p4",
            name = "Comptoir Pièces Badalabougou",
            category = ServiceCategory.PIECES,
            description = "Spécialiste pièces de rechange certifiées, filtration, allumage, amortisseurs et disques.",
            city = "Bamako",
            phone = "+223 72 33 44 55",
            rating = 4.7,
            latitude = 12.6189,
            longitude = -7.9942,
            hours = "08:00 - 18:30",
            servicesOffered = "Plaquettes de frein, Filtres Bosch, Amortisseurs KYB, Bougies NGK"
        ),
        ServiceProvider(
            id = "g2",
            name = "Garage Électro-Mécanique Faladié",
            category = ServiceCategory.MECANICIEN,
            description = "Électriciens et mécaniciens qualifiés : alternateurs, démarreurs, recharge clim gaz 134a et calculateurs.",
            city = "Bamako",
            phone = "+223 78 44 55 66",
            rating = 4.8,
            latitude = 12.5930,
            longitude = -7.9620,
            hours = "07:30 - 18:00",
            servicesOffered = "Électricité auto, Climatisation R134a, Alternateur & Démarreur, Câblage"
        ),
        ServiceProvider(
            id = "p5",
            name = "Central Pièces Toyota & Coréennes",
            category = ServiceCategory.PIECES,
            description = "Stock permanent pièces d'origine Toyota, Hyundai, Kia, Nissan et Mitsubishi.",
            city = "Bamako",
            phone = "+223 79 55 66 77",
            rating = 4.9,
            latitude = 12.6510,
            longitude = -7.9890,
            hours = "08:00 - 19:00",
            servicesOffered = "Pièces Toyota d'origine, Kits d'embrayage, Radiateurs, Pare-chocs & Optiques"
        ),
        ServiceProvider(
            id = "m3",
            name = "Garage Moderne Sikasso",
            category = ServiceCategory.MECANICIEN,
            description = "Spécialiste 4x4, pick-up et berlines. Révision express pour les longs trajets et mécanique générale.",
            city = "Sikasso",
            phone = "+223 71 88 99 00",
            rating = 4.7,
            latitude = 11.3175,
            longitude = -5.6665,
            hours = "07:30 - 18:30",
            servicesOffered = "Suspension tout-terrain, Vidange synthétique, Freinage, Embrayage renforcé"
        ),
        ServiceProvider(
            id = "p6",
            name = "Espace Pièces & Lubrifiants Sikasso",
            category = ServiceCategory.PIECES,
            description = "Vente d'huiles moteurs certifiées (Total, Castrol, Shell), filtres industriels et batteries renforcées.",
            city = "Sikasso",
            phone = "+223 74 66 77 88",
            rating = 4.6,
            latitude = 11.3250,
            longitude = -5.6720,
            hours = "08:00 - 18:00",
            servicesOffered = "Batteries Varta & Bosch, Huiles 5W30/15W40, Filtres Mann, Liquide refroidissement"
        ),
        ServiceProvider(
            id = "p3",
            name = "Pneumatiques Gao",
            category = ServiceCategory.PNEUMATIQUE,
            description = "Vente et réparation de pneus toutes tailles, service rapide.",
            city = "Gao",
            phone = "+223 77 78 89 01",
            rating = 4.6,
            latitude = 16.2742,
            longitude = -0.0471,
            hours = "08:00 - 18:00",
            servicesOffered = "Pneus 4x4, Camions, Réparation crevaison"
        )
    )

    val sampleTutorials = listOf(
        com.example.autoconnect.data.local.TutorialEntity(
            id = "tut_1",
            title = "Changer une roue en cas de crevaison",
            category = "Pneumatique",
            difficulty = "Facile",
            estimatedTimeMinutes = 20,
            toolsNeeded = "Cric mécanique, clé démonte-roue, roue de secours gonflée, gilet & triangle",
            summary = "Procédure pas-à-pas pour remplacer une roue crevée en toute sécurité sur le bord de la route.",
            stepsListRaw = "Immobiliser le véhicule sur un sol plat et serrer le frein à main.|Placer le triangle de signalisation à 30 mètres à l'arrière.|Retirer l'enjoliveur et desserrer légèrement les écrous avant d'élever la voiture.|Placer le cric sous le point d'ancrage du châssis indiqué et monter le véhicule.|Dévisser totalement les écrous et retirer la roue crevée.|Monter la roue de secours et vissez les écrous à la main.|Abaisser doucement le cric jusqu'au sol.|Bloquer fermement les écrous en croix avec la clé.|Vérifier la pression dès que possible dans la station la plus proche.",
            safetyWarning = "Ne travaillez jamais sous un véhicule uniquement soutenu par un cric. N'intervenez pas sur une pente raide."
        ),
        com.example.autoconnect.data.local.TutorialEntity(
            id = "tut_2",
            title = "Démarrer avec des câbles de secours (Batterie à plat)",
            category = "Batterie",
            difficulty = "Facile",
            estimatedTimeMinutes = 15,
            toolsNeeded = "Jeu de câbles de démarrage isolés, véhicule de secours fonctionnel",
            summary = "Méthode sécurisée pour relier deux batteries 12V sans endommager l'électronique de bord.",
            stepsListRaw = "Positionner les deux véhicules face à face sans qu'ils ne se touchent.|Couper le moteur et tous les équipements électriques du véhicule dépanneur.|Connecter la pince ROUGE sur la borne positive (+) de la batterie à plat.|Connecter l'autre pince ROUGE sur la borne positive (+) de la batterie de secours.|Connecter la pince NOIRE sur la borne négative (-) de la batterie de secours.|Connecter l'autre pince NOIRE sur un bloc métallique non peint (masse) du moteur en panne.|Démarrer le véhicule dépanneur et laisser tourner le moteur à 2000 tr/min pendant 3 min.|Tenter de démarrer le véhicule en panne.|Débrancher les câbles exactement dans l'ordre inverse.",
            safetyWarning = "Ne laissez jamais les pinces rouge et noire se toucher. Ne fumez pas à proximité d'une batterie."
        ),
        com.example.autoconnect.data.local.TutorialEntity(
            id = "tut_3",
            title = "Réagir face à une surchauffe moteur par forte chaleur",
            category = "Moteur",
            difficulty = "Urgent",
            estimatedTimeMinutes = 30,
            toolsNeeded = "Chiffon épais, gants, bouteille de liquide de refroidissement ou eau propre à froid",
            summary = "Que faire immédiatement lorsque l'aiguille de température monte dans le rouge ou le voyant STOP s'allume.",
            stepsListRaw = "Couper immédiatement le moteur et se garer en lieu sûr à l'ombre si possible.|Allumer les feux de détresse pour avertir les autres usagers.|Attendre impérativement au moins 25 à 30 minutes avant de toucher au capot.|Ouvrir le capot avec précaution à l'aide d'un chiffon épais.|Inspecter visuellement si une durite est percée ou s'il y a une fuite d'eau importante.|Devisser très lentement le bouchon du vase d'expansion (premier cran pour libérer la pression).|Appoint en liquide de refroidissement à froid uniquement jusqu'au repère MAX.|Si la surchauffe réapparaît après redémarrage, faites appel immédiatement à un remorqueur ou mécanicien.",
            safetyWarning = "ATTENTION : N'ouvrez JAMAIS le bouchon du radiateur d'un moteur chaud ! Le liquide sous pression jaillirait à plus de 100°C et peut causer de graves brûlures."
        ),
        com.example.autoconnect.data.local.TutorialEntity(
            id = "tut_4",
            title = "Remplacer un fusible grillé (Avertisseur / Phares)",
            category = "Électrique",
            difficulty = "Facile",
            estimatedTimeMinutes = 10,
            toolsNeeded = "Pince d'extraction en plastique, coffret fusibles de rechange",
            summary = "Identifier et changer un fusible lorsque vos feux, essuie-glaces ou claxon ne fonctionnent plus.",
            stepsListRaw = "Couper le contact et retirer la clé du neiman.|Ouvrir le boîtier à fusibles situé sous le volant ou dans le compartiment moteur.|Consulter le schéma figurant au dos du couvercle pour repérer l'emplacement.|Saisir le fusible suspect à l'aide de la pince en plastique et le tirer.|Observer la lamelle métallique au centre du fusible : si elle est coupée, il est grillé.|Remplacer par un fusible neuf possédant STRICTEMENT le même ampérage (ex: 10A, 15A, 20A).|Refermer le boîtier et tester la fonction électrique.",
            safetyWarning = "Ne remplacez JAMAIS un fusible par un fusible d'ampérage supérieur ou un morceau de papier aluminium. Risque d'incendie électrique !"
        ),
        com.example.autoconnect.data.local.TutorialEntity(
            id = "tut_5",
            title = "Purger et réamorcer le circuit de gazole",
            category = "Carburant",
            difficulty = "Intermédiaire",
            estimatedTimeMinutes = 15,
            toolsNeeded = "Chiffon propre, tournevis plat",
            summary = "Éliminer l'air emprisonné dans les durites après une panne de carburant ou le changement du filtre à gazole.",
            stepsListRaw = "Localiser la poire manuelle de réamorçage sur le filtre à gazole dans le moteur.|Compresser et relâcher la poire plusieurs fois jusqu'à ce qu'elle devienne très dure.|Desserer légèrement la vis de purge d'air située au-dessus du filtre.|Pomper à nouveau pour faire sortir l'air jusqu'à ce que du gazole pur s'écoule.|Resserrer la vis de purge.|Essayer de démarrer le moteur en maintenant la clé quelques secondes.|Si le moteur tousse, accélérer légèrement pour chasser le reste des bulles d'air.",
            safetyWarning = "Essuyez tout écoulement de gazole sur les durites en caoutchouc et ne laissez aucun chiffon imbibé dans le compartiment moteur."
        )
    )

    val sampleBookings = listOf(
        com.example.autoconnect.data.local.BookingEntity(
            id = "b_1",
            providerId = "1",
            providerName = "Garage Mamadou & Frères",
            serviceType = "Vidange complète & Diagnostic Moteur",
            clientName = "Mamadou Diaby",
            clientPhone = "+223 76 12 34 56",
            date = "2026-07-25",
            timeSlot = "09:00 - 10:30",
            status = "CONFIRME",
            notes = "Toyota RAV4 2018 - Vérifier aussi le bruit au freinage avant"
        ),
        com.example.autoconnect.data.local.BookingEntity(
            id = "b_2",
            providerId = "3",
            providerName = "Pneumatique Express Bamako",
            serviceType = "Parallélisme & Changement 2 Pneus",
            clientName = "Mamadou Diaby",
            clientPhone = "+223 76 12 34 56",
            date = "2026-07-28",
            timeSlot = "14:00 - 15:00",
            status = "EN_ATTENTE",
            notes = "Taille pneus 215/65 R16"
        )
    )

    val sampleChatMessages = listOf(
        com.example.autoconnect.data.local.ChatMessageEntity(
            id = "c_1",
            providerId = "1",
            providerName = "Garage Mamadou & Frères",
            sender = "user",
            message = "Bonjour Maître Mamadou, j'ai un voyant orange moteur d'allumé sur ma Toyota RAV4.",
            timestamp = System.currentTimeMillis() - 3600000 * 5
        ),
        com.example.autoconnect.data.local.ChatMessageEntity(
            id = "c_2",
            providerId = "1",
            providerName = "Garage Mamadou & Frères",
            sender = "mechanic",
            message = "Bonjour ! Aucun problème, apportez le véhicule à notre atelier de Badalabougou. Nous passerons la valise OBD pour lire les codes défauts.",
            timestamp = System.currentTimeMillis() - 3600000 * 4
        ),
        com.example.autoconnect.data.local.ChatMessageEntity(
            id = "c_3",
            providerId = "1",
            providerName = "Garage Mamadou & Frères",
            sender = "mechanic",
            message = "Voici notre devis estimatif pour passage valise & contrôle des capteurs :",
            timestamp = System.currentTimeMillis() - 3600000 * 3,
            isQuote = true,
            quoteAmount = "15 000 FCFA"
        ),
        com.example.autoconnect.data.local.ChatMessageEntity(
            id = "c_4",
            providerId = "3",
            providerName = "Pneumatique Express Bamako",
            sender = "user",
            message = "Avez-vous des pneus neufs taille 215/65 R16 en stock ?",
            timestamp = System.currentTimeMillis() - 3600000 * 24
        ),
        com.example.autoconnect.data.local.ChatMessageEntity(
            id = "c_5",
            providerId = "3",
            providerName = "Pneumatique Express Bamako",
            sender = "mechanic",
            message = "Oui, disponible en marque Michelin et Dunlop avec montage & équilibrage offerts à Hamdallaye ACI 2000.",
            timestamp = System.currentTimeMillis() - 3600000 * 22,
            isQuote = true,
            quoteAmount = "45 000 FCFA / pneu"
        )
    )

    val sampleReviews = listOf(
        com.example.autoconnect.data.local.ReviewEntity(
            id = "r_1",
            serviceId = "p1",
            userName = "Ousmane Traoré",
            comment = "⚡ Service très rapide ! Pièces d'origine Toyota trouvées immédiatement au bon prix. Je recommande vivement.",
            rating = 5.0,
            createdAt = "2026-08-01 10:15"
        ),
        com.example.autoconnect.data.local.ReviewEntity(
            id = "r_2",
            serviceId = "p1",
            userName = "Awa Keita",
            comment = "💰 Prix très honnête et personnel accueillant à Bamako.",
            rating = 4.5,
            createdAt = "2026-08-05 14:30"
        ),
        com.example.autoconnect.data.local.ReviewEntity(
            id = "r_3",
            serviceId = "g1",
            userName = "Sékou Coulibaly",
            comment = "👨‍🔧 Excellent mécanicien ! Diagnostic précis de la panne d'injecteur.",
            rating = 5.0,
            createdAt = "2026-08-02 09:20"
        ),
        com.example.autoconnect.data.local.ReviewEntity(
            id = "r_4",
            serviceId = "g1",
            userName = "Fanta Sidibé",
            comment = "🛠️ Bon travail sur les freins. Petit temps d'attente mais travail garanti.",
            rating = 4.0,
            createdAt = "2026-08-07 16:45"
        ),
        com.example.autoconnect.data.local.ReviewEntity(
            id = "r_5",
            serviceId = "p2",
            userName = "Ibrahima Diallo",
            comment = "⚡ Montage et équilibrage de 4 pneus en moins de 30 minutes. Impeccable !",
            rating = 5.0,
            createdAt = "2026-08-08 11:10"
        )
    )

    val sampleOfferedServices = listOf(
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_1",
            providerId = "g1",
            providerName = "Garage Mécanique Koulikoro",
            title = "Vidange Moteur Complète + Filtre à Huile",
            description = "Huile haute performance 10W40/15W40, remplacement filtre à huile, contrôle niveau liquide de freins et lave-glace.",
            priceCfa = 15000,
            durationMinutes = "30 min",
            category = "Mécanique",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_2",
            providerId = "g1",
            providerName = "Garage Mécanique Koulikoro",
            title = "Diagnostic Électronique Valise OBD2",
            description = "Lecture et effacement des codes défauts moteur, ABS, Airbag, analyse des données en direct des capteurs.",
            priceCfa = 10000,
            durationMinutes = "20 min",
            category = "Diagnostic",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_3",
            providerId = "g1",
            providerName = "Garage Mécanique Koulikoro",
            title = "Révision Complète Freinage Avant & Arrière",
            description = "Changement plaquettes de frein, contrôle disques, dépoussiérage des tambours arrières et purge du circuit.",
            priceCfa = 20000,
            durationMinutes = "45 min",
            category = "Freinage",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_4",
            providerId = "m2",
            providerName = "Atelier Mécanique ACI 2000",
            title = "Diagnostic Valise OBD & Contrôle Électronique",
            description = "Banc diagnostic multimarque complet (Toyota, Mercedes, Hyundai, Nissan), rapport d'erreurs détaillé imprimé.",
            priceCfa = 10000,
            durationMinutes = "30 min",
            category = "Diagnostic",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_5",
            providerId = "m2",
            providerName = "Atelier Mécanique ACI 2000",
            title = "Recharge Climatisation R134a + Détection Fuite",
            description = "Tirage au vide du circuit de clim, injection gaz frigorigène R134a, huile compresseur et traceur UV anti-fuite.",
            priceCfa = 20000,
            durationMinutes = "45 min",
            category = "Climatisation",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_6",
            providerId = "m2",
            providerName = "Atelier Mécanique ACI 2000",
            title = "Nettoyage & Tarage des Injecteurs Diesel",
            description = "Banc de test ultrason pour injecteurs Common Rail, changement des joints pare-feu et optimisation consommation.",
            priceCfa = 25000,
            durationMinutes = "1h30",
            category = "Injection",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_7",
            providerId = "p1",
            providerName = "Pièces Auto Mali",
            title = "Batterie 12V 70Ah Neuve avec Installation",
            description = "Batterie certifiée garantie 12 mois, contrôle alternateur et test de charge électrique du véhicule inclus.",
            priceCfa = 45000,
            durationMinutes = "20 min",
            category = "Batterie",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_8",
            providerId = "p1",
            providerName = "Pièces Auto Mali",
            title = "Kit Filtres Entretien Complet (Air + Huile + Gasoil)",
            description = "Pack filtration d'origine Bosch/Mann pour Toyota, Hyundai ou Peugeot, protection optimale du moteur.",
            priceCfa = 22000,
            durationMinutes = "20 min",
            category = "Pièces",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_9",
            providerId = "p2",
            providerName = "Pneus Experts",
            title = "Montage + Équilibrage Électronique 4 Pneus",
            description = "Démontage, montage sur jante tôle ou alu, équilibrage dynamique au plomb écologique et gonflage pression optimale.",
            priceCfa = 12000,
            durationMinutes = "40 min",
            category = "Pneumatique",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_10",
            providerId = "p2",
            providerName = "Pneus Experts",
            title = "Parallélisme & Géométrie Laser Train Avant",
            description = "Réglage précision par caméras 3D pour éviter l'usure asymétrique des pneus et assurer la tenue de route.",
            priceCfa = 15000,
            durationMinutes = "35 min",
            category = "Pneumatique",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_11",
            providerId = "g2",
            providerName = "Garage Électro-Mécanique Faladié",
            title = "Révision et Rénovation Alternateur 12V",
            description = "Changement régulateur de tension, pont de diodes, roulements et test de tension stabilisée sur banc.",
            priceCfa = 25000,
            durationMinutes = "1h30",
            category = "Électricité",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_12",
            providerId = "g2",
            providerName = "Garage Électro-Mécanique Faladié",
            title = "Dépannage Démarreur & Solénoïde",
            description = "Remplacement des balais charbons, nettoyage induit et test d'entraînement pignon.",
            priceCfa = 18000,
            durationMinutes = "1h",
            category = "Électricité",
            isAvailable = true
        ),
        com.example.autoconnect.data.local.OfferedServiceEntity(
            id = "s_13",
            providerId = "c1",
            providerName = "Carrosserie Excellence",
            title = "Peinture Complète Éléments au Four",
            description = "Préparation tôle, apprêt antirouille, peinture teinte constructeur vernie et étuvage au four.",
            priceCfa = 35000,
            durationMinutes = "24h",
            category = "Carrosserie",
            isAvailable = true
        )
    )
}

