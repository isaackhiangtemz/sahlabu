import 'package:flutter/material.dart';
const sahlabuTeal=Color(0xFF087C82);
class AppTheme{
 static final light=ThemeData(useMaterial3:true,brightness:Brightness.light,colorScheme:ColorScheme.fromSeed(seedColor:sahlabuTeal));
 static final dark=ThemeData(useMaterial3:true,brightness:Brightness.dark,colorScheme:ColorScheme.fromSeed(seedColor:sahlabuTeal,brightness:Brightness.dark));
}