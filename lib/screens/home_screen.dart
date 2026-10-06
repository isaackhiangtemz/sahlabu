import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/hymn_provider.dart';
import '../providers/settings_provider.dart';
import 'hymn_detail_screen.dart';
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});
  @override Widget build(BuildContext context){
    final p=context.watch<HymnProvider>(),s=context.watch<SettingsProvider>();
    return Scaffold(appBar:AppBar(title:const Text('SAHLABU',style:TextStyle(fontWeight:FontWeight.bold)),actions:[
      IconButton(onPressed:()=>s.changeFontSize(s.fontSize-1),icon:const Icon(Icons.text_decrease)),
      IconButton(onPressed:()=>s.changeFontSize(s.fontSize+1),icon:const Icon(Icons.text_increase)),
      IconButton(onPressed:s.toggleTheme,icon:Icon(s.themeMode==ThemeMode.dark?Icons.light_mode:Icons.dark_mode)),
    ]),body:Column(children:[
      Padding(padding:const EdgeInsets.all(16),child:TextField(onChanged:p.search,decoration:InputDecoration(hintText:'Search hymn number, title or lyrics...',prefixIcon:const Icon(Icons.search),border:OutlineInputBorder(borderRadius:BorderRadius.all(Radius.circular(16)))))),
      Expanded(child:p.loading?const Center(child:CircularProgressIndicator()):ListView.builder(itemCount:p.hymns.length,itemBuilder:(_,i){
        final h=p.hymns[i]; return ListTile(leading:CircleAvatar(child:Text(h.number.toString())),title:Text(h.title,style:TextStyle(fontSize:s.fontSize)),trailing:IconButton(icon:Icon(p.isFavorite(h.number)?Icons.star:Icons.star_border),onPressed:()=>p.toggleFavorite(h.number)),onTap:()=>Navigator.push(context,MaterialPageRoute(builder:(_)=>HymnDetailScreen(hymn:h))));
      }))
    ]));
  }
}
