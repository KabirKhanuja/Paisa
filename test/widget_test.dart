import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:paisa/screens/lock_screen.dart';

void main() {
  testWidgets('Lock screen renders entry text', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(home: LockScreen()),
      ),
    );

    expect(find.text('wanted to keep it simple'), findsOneWidget);
    expect(find.text('enter'), findsOneWidget);
  });
}
