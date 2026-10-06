class Hymn {
  final int number; final String title; final List<HymnSection> lyrics;
  const Hymn({required this.number, required this.title, required this.lyrics});
  factory Hymn.fromJson(Map<String, dynamic> json) => Hymn(
    number: json['number'] as int, title: json['title'] as String,
    lyrics: (json['lyrics'] as List<dynamic>).map((e) => HymnSection.fromJson(e)).toList());
  String get searchableText => [number.toString(), title, ...lyrics.expand((s) => s.lines)].join(' ').toLowerCase();
}
class HymnSection {
  final String type; final int? number; final List<String> lines;
  const HymnSection({required this.type, this.number, required this.lines});
  factory HymnSection.fromJson(Map<String, dynamic> json) => HymnSection(
    type: json['type'] as String, number: json['number'] as int?, lines: List<String>.from(json['lines'] as List));
}
