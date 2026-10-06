import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsProvider extends ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.system;
  double _fontSize = 18;

  ThemeMode get themeMode => _themeMode;
  double get fontSize => _fontSize;

  Future<void> load() async {
    final p = await SharedPreferences.getInstance();
    _fontSize = p.getDouble('fontSize') ?? 18;
    final d = p.getBool('darkMode');
    if (d != null) _themeMode = d ? ThemeMode.dark : ThemeMode.light;
    notifyListeners();
  }

  Future<void> toggleTheme() async {
    _themeMode = _themeMode == ThemeMode.dark ? ThemeMode.light : ThemeMode.dark;
    final p = await SharedPreferences.getInstance();
    await p.setBool('darkMode', _themeMode == ThemeMode.dark);
    notifyListeners();
  }

  Future<void> changeFontSize(double v) async {
    _fontSize = v.clamp(12.0, 32.0).toDouble();
    final p = await SharedPreferences.getInstance();
    await p.setDouble('fontSize', _fontSize);
    notifyListeners();
  }
}
