import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/theme/app_theme.dart';
import 'providers/auth_provider.dart';
import 'providers/user_provider.dart';
import 'providers/user_document_provider.dart';
import 'providers/dashboard_provider.dart';
import 'providers/chat_provider.dart';
import 'providers/player_provider.dart';
import 'providers/alert_provider.dart';
import 'providers/theme_provider.dart';
import 'providers/document_provider.dart' as original_doc;
import 'screens/auth/login_screen.dart';
import 'screens/auth/register_screen.dart';
import 'screens/auth/activate_account_screen.dart';
import 'screens/home/home_screen.dart';
import 'screens/users/users_screen.dart';
import 'screens/equipes/equipes_screen.dart';
import 'screens/joueurs/joueurs_screen.dart';
import 'screens/entrainements/entrainements_screen.dart';
import 'screens/entrainements/mes_entrainements_screen.dart';
import 'screens/matchs/matchs_screen.dart';
import 'screens/cotisations/cotisations_screen.dart';
import 'screens/calendar/calendar_screen.dart';
import 'screens/profile/profile_screen.dart';
import 'screens/admin/dashboard_screen.dart';
import 'screens/encadrant/encadrant_dashboard.dart';
import 'screens/encadrant/encadrant_entrainements_screen.dart';
import 'screens/adherent/adherent_dashboard.dart';
import 'screens/inscrit/inscrit_dashboard.dart';
// Temporarily commented out - need to be updated to use new UserDocumentProvider
// import 'screens/admin/documents/documents_list_screen.dart';
import 'screens/messages_screen.dart';
import 'screens/add_user_screen.dart';
import 'screens/user_documents_screen.dart';
import 'services/user_service.dart';
import 'services/document_service_api.dart' as doc_api;
import 'config/api_config.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    // Initialize services
    final userService = UserService(
      baseUrl: ApiConfig.serverBaseUrl,
      token: null, // Will be set from AuthProvider
    );
    
    final documentService = doc_api.DocumentService(
      baseUrl: ApiConfig.serverBaseUrl,
      token: null, // Will be set from AuthProvider
    );

    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => AuthProvider()..checkAuthentication()),
        ChangeNotifierProvider(create: (_) => DashboardProvider()),
        // Original DocumentProvider for existing admin screens
        ChangeNotifierProvider(create: (_) => original_doc.DocumentProvider()),
        // New UserDocumentProvider for user document management - needs AuthProvider
        ChangeNotifierProvider(
          create: (context) => UserDocumentProvider(context.read<AuthProvider>()),
        ),
        // New UserProvider for user management - needs AuthProvider for token
        ChangeNotifierProvider(
          create: (context) => UserProvider(context.read<AuthProvider>()),
        ),
        ChangeNotifierProvider(create: (_) => ChatProvider()),
        ChangeNotifierProvider(create: (_) => PlayerProvider()),
        ChangeNotifierProvider(create: (_) => AlertProvider()),
      ],
      child: Consumer<ThemeProvider>(
        builder: (context, themeProvider, child) {
          return MaterialApp(
            title: 'MAS de Fès',
            debugShowCheckedModeBanner: false,
            theme: AppTheme.lightTheme,
            darkTheme: AppTheme.darkTheme,
            themeMode: themeProvider.themeMode,
            initialRoute: '/login',
            routes: {
              '/login': (context) => const LoginScreen(),
              '/register': (context) => const RegisterScreen(),
              '/activate-account': (context) => const ActivateAccountScreen(),
              '/home': (context) => const HomeScreen(),
              '/users': (context) => const UsersScreen(),
              '/equipes': (context) => const EquipesScreen(),
              '/joueurs': (context) => const JoueursScreen(),
              '/entrainements': (context) => const EntrainementsScreen(),
              '/mes-entrainements': (context) => const MesEntrainementsScreen(),
              '/encadrant/entrainements': (context) => const EncadrantEntrainementsScreen(),
              '/matchs': (context) => const MatchsScreen(),
              '/cotisations': (context) => const CotisationsScreen(),
              '/calendar': (context) => const CalendarScreen(),
              '/profile': (context) => const ProfileScreen(),
              '/admin/dashboard': (context) => const AdminDashboardScreen(),
              '/encadrant/dashboard': (context) => const EncadrantDashboard(),
              '/adherent/dashboard': (context) => const AdherentDashboard(),
              '/inscrit/dashboard': (context) => const InscritDashboard(),
              // '/admin/documents': (context) => const DocumentsListScreen(), // Temporarily disabled
              '/messages': (context) => const MessagesScreen(),
              '/add-user': (context) => AddUserScreen(),
            },
          );
        },
      ),
    );
  }
}
