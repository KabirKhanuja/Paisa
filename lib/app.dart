import 'package:flutter/material.dart';

import 'theme.dart';
import 'screens/lock_screen.dart';

class PaisaApp extends StatelessWidget {
  const PaisaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Paisa',
      debugShowCheckedModeBanner: false,
      theme: buildPaisaTheme(),
      home: const LockScreen(),
    );
  }
}
