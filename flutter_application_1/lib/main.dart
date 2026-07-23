import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'screens/login_screen.dart';
import 'screens/home_screen.dart';
import 'providers/service_provider.dart';
import 'providers/auth_provider.dart';
// la gestion des produit et des comptes sont incluedans la fonction principal

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const AutoConnectApp());
}

class AutoConnectApp extends StatelessWidget {
  final ServicesNotifier? servicesNotifier;
  final AuthNotifier? authNotifier;

  const AutoConnectApp({
    super.key,
    this.servicesNotifier,
    this.authNotifier,
  });
  //
  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => authNotifier ?? AuthNotifier(),
        ),
        ChangeNotifierProvider(
          create: (_) => servicesNotifier ?? ServicesNotifier(),
        ),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'Auto Connect Mali',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF1565C0),
            brightness: Brightness.light,
          ),
          useMaterial3: true,
          appBarTheme: const AppBarTheme(elevation: 0, centerTitle: false),
        ),
        home: Consumer<AuthNotifier>(
          builder: (context, auth, _) {
            return auth.isAuthenticated
                ? const HomeScreen()
                : const LoginScreen();
          },
        ),
      ),
    );
  }
}
