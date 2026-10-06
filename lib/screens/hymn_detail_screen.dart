import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/hymn.dart';
import '../providers/hymn_provider.dart';
import '../providers/settings_provider.dart';

const _teal=Color(0xFF087C82);
class HymnDetailScreen extends StatelessWidget {
  final Hymn hymn;
  const HymnDetailScreen({super.key,required this.hymn});
  @override Widget build(BuildContext context){
    final size=context.watch<SettingsProvider>().fontSize;
    final p=context.watch<HymnProvider>();
    return Scaffold(backgroundColor:Colors.white,appBar:AppBar(backgroundColor:_teal,foregroundColor:Colors.white,elevation:0,leading:IconButton(icon:const Icon(Icons.arrow_back,size:34),onPressed:()=>Navigator.pop(context)),actions:[
      IconButton(iconSize:34,onPressed:()=>p.toggleFavorite(hymn.number),icon:Icon(p.isFavorite(hymn.number)?Icons.favorite:Icons.favorite_border)),
      IconButton(iconSize:32,onPressed:(){},icon:const Icon(Icons.refresh)),
      IconButton(iconSize:32,onPressed:(){},icon:const Icon(Icons.share)),
    ]),body:Stack(children:[
      ListView(padding:const EdgeInsets.fromLTRB(24,18,24,100),children:[
        Row(children:[Text(hymn.number.toString(),style:const TextStyle(fontSize:16)),const SizedBox(width:12),Expanded(child:Text(hymn.title.toUpperCase(),textAlign:TextAlign.center,style:const TextStyle(fontSize:20,fontWeight:FontWeight.w900))),const SizedBox(width:24)]),
        const SizedBox(height:18),
        ...hymn.lyrics.map((section)=>Padding(padding:const EdgeInsets.only(bottom:25),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
          if(section.type.toLowerCase()!='verse') Text(section.type.toUpperCase(),style:const TextStyle(fontWeight:FontWeight.bold)),
          ...section.lines.map((line)=>Text(line,style:TextStyle(fontSize:size,height:1.55,color:Colors.black87))),
        ]))),
      ]),
      Positioned(right:18,bottom:18,child:FloatingActionButton.extended(backgroundColor:_teal,foregroundColor:Colors.white,onPressed:(){},icon:const Icon(Icons.music_note),label:const Text('E♭',style:TextStyle(fontWeight:FontWeight.bold,fontSize:17)))),
    ]));
  }
}