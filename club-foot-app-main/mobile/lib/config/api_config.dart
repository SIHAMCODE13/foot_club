import 'package:flutter/foundation.dart';

class ApiConfig {
  // IMPORTANT:
  // - Android Emulator: utiliser 10.0.2.2 pour accéder à localhost du PC
  // - Web/Desktop/iOS simulator: localhost
  static const int _port = 8082;

  /// Base serveur sans `/api` (ex: `http://localhost:8082`)
  static String get serverBaseUrl {
    if (kIsWeb) return 'http://localhost:$_port';

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        // Pour téléphone physique, utiliser l'IP du PC
        // Pour émulateur, utiliser 10.0.2.2
        return 'http://192.168.1.16:$_port';
      case TargetPlatform.iOS:
      case TargetPlatform.macOS:
      case TargetPlatform.windows:
      case TargetPlatform.linux:
      case TargetPlatform.fuchsia:
        return 'http://localhost:$_port';
    }
  }

  /// Base API avec `/api` (ex: `http://localhost:8082/api`)
  static String get apiBaseUrl => '${serverBaseUrl}/api';

  /// Compat: ancien nom utilisé dans le code
  static String get baseUrl => apiBaseUrl;
  
  // Auth endpoints
  static String get login => '$apiBaseUrl/auth/login';
  static String get register => '$apiBaseUrl/auth/register';
  static String get me => '$apiBaseUrl/auth/me';
  
  // Users endpoints
  static String get users => '$apiBaseUrl/users';
  
  // Admin endpoints
  static String get adminUsers => '$apiBaseUrl/admin/users';
  static String get adminJoueurs => '$apiBaseUrl/admin/joueurs';
  static String get adminEquipes => '$apiBaseUrl/admin/equipes';
  static String get adminEntrainements => '$apiBaseUrl/admin/entrainements';
  static String get adminMatchs => '$apiBaseUrl/admin/matchs';
  static String get adminCotisations => '$apiBaseUrl/admin/cotisations';
  static String get adminDocuments => '$apiBaseUrl/admin/documents';
  static String get adminDocumentsUpload => '$adminDocuments/upload';
  static String get adminDocumentsExpiring => '$adminDocuments/expiring-soon';
  static String get adminMessages => '$apiBaseUrl/admin/messages';
  static String get adminMessagesBroadcast => '$adminMessages/broadcast';
  static String get adminMessagesSent => '$adminMessages/sent';
  static String get adminMessagesStats => '$adminMessages/stats';
  
  // Chat endpoints
  static String get chatHistory => '$apiBaseUrl/chat/history';
  
  // Encadrant endpoints
  static String get encadrantJoueurs => '$apiBaseUrl/encadrant/joueurs';
  static String get encadrantEquipes => '$apiBaseUrl/encadrant/equipes';
  static String get encadrantEntrainements => '$apiBaseUrl/encadrant/entrainements';
  static String get encadrantMatchs => '$apiBaseUrl/encadrant/matchs';
  static String encadrantEntrainementsByEncadrant(int encadrantId) => 
      '$apiBaseUrl/encadrant/entrainements/mes-seances/$encadrantId';
  
  // Adherent endpoints
  static String get adherentProfil => '$apiBaseUrl/adherent/profil';
  static String get adherentJoueurs => '$apiBaseUrl/adherent/joueurs';
  static String get adherentEquipes => '$apiBaseUrl/adherent/equipes';
  static String get adherentEntrainements => '$apiBaseUrl/adherent/entrainements';
  static String get adherentMatchs => '$apiBaseUrl/adherent/matchs';
  static String get adherentCotisations => '$apiBaseUrl/adherent/cotisations';
}