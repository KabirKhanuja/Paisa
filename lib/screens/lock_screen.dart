import 'dart:convert';

import 'package:crypto/crypto.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:local_auth/local_auth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../theme.dart';
import '../providers.dart';
import 'home_screen.dart';

class LockScreen extends ConsumerStatefulWidget {
  const LockScreen({super.key});

  @override
  ConsumerState<LockScreen> createState() => _LockScreenState();
}

class _LockScreenState extends ConsumerState<LockScreen> {
  final LocalAuthentication _auth = LocalAuthentication();
  bool _busy = false;
  String? _error;

  Future<void> _enter() async {
    if (_busy) return;
    setState(() {
      _busy = true;
      _error = null;
    });

    bool ok = false;
    try {
      final canCheck =
          await _auth.canCheckBiometrics || await _auth.isDeviceSupported();
      if (canCheck) {
        ok = await _auth.authenticate(
          localizedReason: 'Unlock Paisa',
          options: const AuthenticationOptions(
            biometricOnly: false,
            stickyAuth: true,
          ),
        );
      } else {
        ok = await _showPinFallback();
      }
    } on PlatformException {
      ok = await _showPinFallback();
    }

    if (!mounted) return;
    if (ok) {
      Navigator.of(
        context,
      ).pushReplacement(MaterialPageRoute(builder: (_) => const HomeScreen()));
    } else {
      setState(() {
        _busy = false;
        _error = 'authentication failed';
      });
    }
  }

  String _hashPin(String pin, String uid) {
    final bytes = utf8.encode(uid + pin);
    return sha256.convert(bytes).toString();
  }

  Future<bool> _showPinFallback() async {
    // Ensure we have a user id (anonymous sign-in if needed)
    final uid = await ref.read(authServiceProvider).signInAnonymouslyIfNeeded();
    final docRef = FirebaseFirestore.instance.collection('users').doc(uid);
    final snapshot = await docRef.get();
    final storedHash = snapshot.data()?['pinHash'] as String?;

    if (storedHash == null) {
      // No PIN set — ask user to create a 4-digit PIN
      final set = await _showSetPinDialog();
      if (!set) return false;
      final controller = TextEditingController();
      final pin = await showDialog<String?>(
        context: context,
        barrierDismissible: false,
        builder: (ctx) => AlertDialog(
          title: const Text('Set 4-digit PIN'),
          content: TextField(
            controller: controller,
            autofocus: true,
            obscureText: true,
            keyboardType: TextInputType.number,
            maxLength: 4,
            decoration: const InputDecoration(hintText: '1234'),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx, null),
              child: const Text('cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(ctx, controller.text),
              child: const Text('save'),
            ),
          ],
        ),
      );
      if (pin == null || pin.length != 4 || int.tryParse(pin) == null)
        return false;
      final hash = _hashPin(pin, uid);
      await docRef.set({'pinHash': hash}, SetOptions(merge: true));
      return true;
    }

    // PIN exists — prompt for entry and verify
    final controller = TextEditingController();
    final result = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Enter 4-digit PIN'),
        content: TextField(
          controller: controller,
          autofocus: true,
          obscureText: true,
          keyboardType: TextInputType.number,
          maxLength: 4,
          decoration: const InputDecoration(hintText: '0000'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('cancel'),
          ),
          FilledButton(
            onPressed: () {
              final p = controller.text;
              if (p.length == 4 && int.tryParse(p) != null) {
                final h = _hashPin(p, uid);
                Navigator.pop(ctx, h == storedHash);
              } else {
                Navigator.pop(ctx, false);
              }
            },
            child: const Text('unlock'),
          ),
        ],
      ),
    );
    return result ?? false;
  }

  Future<bool> _showSetPinDialog() async {
    final result = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('No PIN set'),
        content: const Text(
          'You don\'t have a PIN set. Would you like to create a 4-digit PIN?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('no'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('yes'),
          ),
        ],
      ),
    );
    return result ?? false;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32),
          child: Column(
            children: [
              const Spacer(),
              const Text(
                'wanted to keep it simple',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 12),
              Text(
                'track only what matters',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.black.withValues(alpha: 0.5),
                ),
              ),
              const Spacer(),
              if (_error != null) ...[
                Text(
                  _error!,
                  style: const TextStyle(color: PaisaColors.expense),
                ),
                const SizedBox(height: 12),
              ],
              FilledButton(
                onPressed: _busy ? null : _enter,
                child: _busy
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Text('enter'),
              ),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }
}
