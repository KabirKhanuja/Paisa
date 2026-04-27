import 'package:flutter/material.dart';

class PaisaColors {
  static const primary = Color(0xFF2563EB);
  static const background = Color(0xFFFFFFFF);
  static const surface = Color(0xFFF8FAFC);
  static const income = Color(0xFF16A34A);
  static const expense = Color(0xFFDC2626);
}

ThemeData buildPaisaTheme() {
  final scheme = ColorScheme.fromSeed(
    seedColor: PaisaColors.primary,
    brightness: Brightness.light,
    primary: PaisaColors.primary,
    surface: PaisaColors.surface,
  );

  return ThemeData(
    useMaterial3: true,
    colorScheme: scheme,
    scaffoldBackgroundColor: PaisaColors.background,
    appBarTheme: const AppBarTheme(
      backgroundColor: PaisaColors.background,
      foregroundColor: Colors.black87,
      elevation: 0,
      scrolledUnderElevation: 0,
      centerTitle: false,
    ),
    cardTheme: CardThemeData(
      elevation: 0,
      color: PaisaColors.surface,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        minimumSize: const Size.fromHeight(56),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
      ),
    ),
  );
}
