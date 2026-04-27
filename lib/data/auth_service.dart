import 'package:firebase_auth/firebase_auth.dart';

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;

  String? get userId => _auth.currentUser?.uid;

  Future<String> signInAnonymouslyIfNeeded() async {
    final current = _auth.currentUser;
    if (current != null) return current.uid;
    final cred = await _auth.signInAnonymously();
    return cred.user!.uid;
  }
}
