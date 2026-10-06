import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/hymn_provider.dart';
import '../providers/settings_provider.dart';
import 'hymn_detail_screen.dart';

const _teal = Color(0xFF087C82);

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override State<HomeScreen> createState() => _HomeScreenState();
}
class _HomeScreenState extends State<HomeScreen> {
  int tab = 0;
  @override Widget build(BuildContext context) {
    final p = context.watch<HymnProvider>();
    final s = context.watch<SettingsProvider>();
    final groups = <String, List<dynamic>>{};
    for (final h in p.hymns) { groups.putIfAbsent(h.title.trim().isEmpty ? '#' : h.title.trim()[0].toUpperCase(), () => []).add(h); }
    final keys = groups.keys.toList()..sort();
    return Scaffold(
      backgroundColor: Theme.of(context).brightness == Brightness.dark ? const Color(0xFF202124) : const Color(0xFFE0E0E0),
      appBar: PreferredSize(preferredSize: const Size.fromHeight(116), child: Container(color: _teal, padding: const EdgeInsets.fromLTRB(16,8,16,12), child: SafeArea(bottom:false, child: Column(children:[
        Row(children:[
          IconButton(color:Colors.white, iconSize:31, onPressed:(){}, icon:const Icon(Icons.menu)),
          const Expanded(child:Text('VÂNRAM NGAIH HLA', textAlign:TextAlign.center, style:TextStyle(color:Colors.white,fontSize:24,fontWeight:FontWeight.w900))),
          Switch(value:s.themeMode==ThemeMode.dark,onChanged:(_)=>s.toggleTheme(),activeThumbColor:Colors.white,inactiveThumbColor:Colors.white),
        ]),
        SizedBox(height:54, child:TextField(onChanged:p.search,style:const TextStyle(color:Colors.white,fontSize:18),decoration:InputDecoration(filled:true,fillColor:Colors.white24,hintText:'Hla bul, nambar leh thupui h...',hintStyle:const TextStyle(color:Colors.white,fontSize:18),prefixIcon:const Icon(Icons.search,color:Colors.white,size:30),suffixIcon:const Icon(Icons.tune,color:Colors.white),border:OutlineInputBorder(borderRadius:BorderRadius.all(Radius.circular(30)),borderSide:BorderSide.none)))),
      ]))),
      body:p.loading ? const Center(child:CircularProgressIndicator()) : Row(children:[
        Expanded(child:ListView(padding:const EdgeInsets.fromLTRB(10,10,8,20),children:keys.expand((k)=>[
          Text(k,style:const TextStyle(fontSize:27,fontWeight:FontWeight.w900)),
          ...groups[k]!.map((h)=>Padding(padding:const EdgeInsets.only(bottom:9),child:Material(color:Theme.of(context).brightness==Brightness.dark?const Color(0xFF303134):Colors.white,borderRadius:BorderRadius.circular(28),elevation:5,child:InkWell(borderRadius:BorderRadius.circular(28),onTap:()=>Navigator.push(context,MaterialPageRoute(builder:(_)=>HymnDetailScreen(hymn:h))),child:Padding(padding:const EdgeInsets.symmetric(horizontal:12,vertical:12),child:Row(children:[
            CircleAvatar(radius:30,backgroundColor:_teal,child:Text(h.number.toString(),style:const TextStyle(color:Colors.white,fontSize:20,fontWeight:FontWeight.w700))),
            const SizedBox(width:18),Expanded(child:Text(h.title,style:const TextStyle(fontSize:19,fontWeight:FontWeight.w800))),
          ]))))),
        ]).toList())),
        Container(width:58,margin:const EdgeInsets.fromLTRB(0,12,8,12),decoration:BoxDecoration(color:Colors.grey.shade300,borderRadius:BorderRadius.circular(30)),child:ListView(padding:const EdgeInsets.symmetric(vertical:7),children:['A','Aw','B','Ch','D','E','F','Ng','H','I','J','K','L','M','N','P','R','S','T','T.','V','Z'].map((x)=>Padding(padding:const EdgeInsets.symmetric(vertical:2),child:Text(x,textAlign:TextAlign.center,style:const TextStyle(fontSize:17,fontWeight:FontWeight.w800)))).toList())),
      ]),
      bottomNavigationBar:NavigationBar(height:74,backgroundColor:_teal,indicatorColor:Colors.white,selectedIndex:tab,onDestinationSelected:(i)=>setState(()=>tab=i),destinations:const[
        NavigationDestination(icon:Icon(Icons.local_fire_department),selectedIcon:Icon(Icons.local_fire_department,color:_teal),label:''),
        NavigationDestination(icon:Icon(Icons.flutter_dash),selectedIcon:Icon(Icons.flutter_dash,color:_teal),label:''),
        NavigationDestination(icon:Icon(Icons.menu_book),selectedIcon:Icon(Icons.menu_book,color:_teal),label:''),
        NavigationDestination(icon:Icon(Icons.add_box),selectedIcon:Icon(Icons.add_box,color:_teal),label:''),
        NavigationDestination(icon:Icon(Icons.more_horiz),selectedIcon:Icon(Icons.more_horiz,color:_teal),label:''),
      ]),
    );
  }
}