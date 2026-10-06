import 'dart:convert';
import 'package:flutter/services.dart';
import '../models/hymn.dart';
class HymnService {
  Future<List<Hymn>> loadHymns() async {
    final raw = await rootBundle.loadString('assets/data/hymns.json');
    final data = jsonDecode(raw) as List<dynamic>;
    return data.map((e) => Hymn.fromJson(e as Map<String, dynamic>)).toList();
  }
}
