import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/hymn_provider.dart';
import 'providers/settings_provider.dart';
import 'screens/home_screen.dart';
import 'theme/app_theme.dart';
Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final h=HymnProvider(), s=SettingsProvider();
  await Future.wait([h.load(),s.load()]);
  runApp(MultiProvider(providers:[ChangeNotifierProvider.value(value:h),ChangeNotifierProvider.value(value:s)],child:const SahlabuApp()));
}
class SahlabuApp extends StatelessWidget {
  const SahlabuApp({super.key});
  @override Widget build(BuildContext context)=>Consumer<SettingsProvider>(builder:(_,s,__)=>
    MaterialApp(debugShowCheckedModeBanner:false,title:'SAHLABU',theme:AppTheme.light,darkTheme:AppTheme.dark,themeMode:s.themeMode,home:const HomeScreen()));
}
