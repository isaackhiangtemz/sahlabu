import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/hymn.dart';
import '../providers/hymn_provider.dart';
import '../providers/settings_provider.dart';

class HymnDetailScreen extends StatelessWidget {
  final Hymn hymn;
  const HymnDetailScreen({super.key, required this.hymn});

  @override
  Widget build(BuildContext context) {
    final size = context.watch<SettingsProvider>().fontSize;
    final p = context.watch<HymnProvider>();
    return Scaffold(
      appBar: AppBar(
        title: Text(hymn.number.toString() + '. ' + hymn.title),
        actions: [
          IconButton(
            onPressed: () => p.toggleFavorite(hymn.number),
            icon: Icon(p.isFavorite(hymn.number) ? Icons.star : Icons.star_border),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 40),
        children: hymn.lyrics.map((section) => Padding(
          padding: const EdgeInsets.only(bottom: 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                section.type.toLowerCase() == 'chorus'
                    ? 'CHORUS'
                    : 'VERSE ' + (section.number?.toString() ?? ''),
                style: const TextStyle(fontWeight: FontWeight.bold, letterSpacing: 1.2),
              ),
              const SizedBox(height: 8),
              ...section.lines.map((line) => Text(
                line,
                style: TextStyle(fontSize: size, height: 1.55),
              )),
            ],
          ),
        )).toList(),
      ),
    );
  }
}
