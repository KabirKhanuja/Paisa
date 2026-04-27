import 'dart:convert';

import 'package:crypto/crypto.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:local_auth/local_auth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../theme.dart';
import '../providers.dart';
import '../data/auth_service.dart';
import 'home_screen.dart';

class LockScreen extends ConsumerStatefulWidget {
  const LockScreen({super.key});

  @override
  ConsumerState<LockScreen> createState() => _LockScreenState();
}

class _LockScreenState extends ConsumerState<LockScreen> {
  final LocalAuthentication _auth = LocalAuthentication();
  final FlutterSecureStorage _secure = const FlutterSecureStorage();
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
      }
      if (!ok) {
        ok = await _showPinFallback();
      }
    } catch (_) {
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

  String _hashPin(String pin, String salt) {
    final bytes = utf8.encode(salt + pin);
    return sha256.convert(bytes).toString();
  }

  Future<bool> _showPinFallback() async {
    final auth = ref.read(authServiceProvider);
    final uid = auth.userId; // may be null if not signed-in
    final key = 'pinHash_${uid ?? 'device'}';
    final storedHash = await _secure.read(key: key);

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
      final hash = _hashPin(pin, uid ?? 'device');
      await _secure.write(key: key, value: hash);
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
                final h = _hashPin(
                  p,
                  ref.read(authServiceProvider).userId ?? 'device',
                );
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

  Future<void> _showAuthOptions() async {
    // If user is signed in, do nothing — enter will handle biometric/pin.
    // Otherwise show options to login or create account.
    final auth = ref.read(authServiceProvider);
    if (auth.isSignedIn && !auth.isAnonymous) return;

    await showModalBottomSheet<void>(
      context: context,
      builder: (ctx) {
        return Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                'Sign in to your account',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: FilledButton(
                      onPressed: () => _showEmailAuthDialog(signUp: false),
                      child: const Text('Login'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => _showEmailAuthDialog(signUp: true),
                      child: const Text('Create account'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              TextButton(
                onPressed: () async {
                  // allow anonymous continue — if Firebase isn't configured,
                  // fall back to device-only mode without attempting sign-in.
                  Navigator.pop(ctx);
                  try {
                    await ref
                        .read(authServiceProvider)
                        .signInAnonymouslyIfNeeded();
                  } catch (_) {
                    // ignore: proceed without Firebase
                  }
                  setState(() {});
                },
                child: const Text('Continue anonymously'),
              ),
            ],
          ),
        );
      },
    );
  }

  Future<void> _showEmailAuthDialog({required bool signUp}) async {
    final emailController = TextEditingController();
    final passController = TextEditingController();
    final success = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(signUp ? 'Create account' : 'Login'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: emailController,
              decoration: const InputDecoration(labelText: 'Email'),
            ),
            TextField(
              controller: passController,
              decoration: const InputDecoration(labelText: 'Password'),
              obscureText: true,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('cancel'),
          ),
          FilledButton(
            onPressed: () async {
              try {
                if (signUp) {
                  await ref
                      .read(authServiceProvider)
                      .signUp(emailController.text.trim(), passController.text);
                } else {
                  await ref
                      .read(authServiceProvider)
                      .signIn(emailController.text.trim(), passController.text);
                }
                final prefs = await SharedPreferences.getInstance();
                await prefs.setBool('stayLoggedIn', true);
                Navigator.pop(ctx, true);
                setState(() {});
              } catch (e) {
                Navigator.pop(ctx, false);
              }
            },
            child: const Text('continue'),
          ),
        ],
      ),
    );
    if (success == true) {
      // stay on lock screen; user will press enter to authenticate locally
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = ref.read(authServiceProvider);
    final signedIn = auth.isSignedIn && !auth.isAnonymous;
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
                signedIn
                    ? 'press enter to unlock (fingerprint or PIN)'
                    : 'please login or create an account',
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
              Row(
                children: [
                  Expanded(
                    child: FilledButton(
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
                  ),
                  const SizedBox(width: 12),
                  IconButton(
                    onPressed: _showAuthOptions,
                    icon: const Icon(Icons.person),
                  ),
                ],
              ),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }
}
