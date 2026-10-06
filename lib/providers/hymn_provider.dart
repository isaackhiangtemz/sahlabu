import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/hymn.dart';
import '../services/hymn_service.dart';
class HymnProvider extends ChangeNotifier {
  final HymnService _service = HymnService();
  List<Hymn> _hymns=[]; List<Hymn> _filtered=[]; final Set<int> _favorites={}; bool _loading=true;
  List<Hymn> get hymns=>_filtered; bool get loading=>_loading; bool isFavorite(int n)=>_favorites.contains(n);
  Future<void> load() async {
    _hymns=await _service.loadHymns(); _filtered=List.of(_hymns);
    final p=await SharedPreferences.getInstance(); _favorites..clear()..addAll(p.getStringList('favorites')?.map(int.parse)??[]);
    _loading=false; notifyListeners();
  }
  void search(String q){final s=q.trim().toLowerCase(); _filtered=s.isEmpty?List.of(_hymns):_hymns.where((h)=>h.searchableText.contains(s)).toList(); notifyListeners();}
  Future<void> toggleFavorite(int n) async {if(_favorites.contains(n)){_favorites.remove(n);}else{_favorites.add(n);} final p=await SharedPreferences.getInstance(); await p.setStringList('favorites',_favorites.map((e)=>e.toString()).toList()); notifyListeners();}
}
