import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_core/firebase_core.dart';

class AuthService {
  // Lazily access FirebaseAuth.instance so constructing this class doesn't
  // throw when Firebase hasn't been initialized (e.g. during development
  // before running `flutterfire configure`).
  FirebaseAuth? get _auth {
    try {
      return FirebaseAuth.instance;
    } on FirebaseException catch (_) {
      return null;
    } catch (_) {
      return null;
    }
  }

  String? get userId => _auth?.currentUser?.uid;

  bool get isSignedIn => _auth?.currentUser != null;

  bool get isAnonymous => _auth?.currentUser?.isAnonymous ?? false;

  Future<String> signInAnonymouslyIfNeeded() async {
    final auth = _auth;
    if (auth == null) {
      throw FirebaseException(
        plugin: 'firebase_core',
        code: 'no-app',
        message: 'Firebase not initialized',
      );
    }
    final current = auth.currentUser;
    if (current != null) return current.uid;
    final cred = await auth.signInAnonymously();
    return cred.user!.uid;
  }

  Future<String> signUp(String email, String password) async {
    final auth = _auth;
    if (auth == null)
      throw FirebaseException(
        plugin: 'firebase_core',
        code: 'no-app',
        message: 'Firebase not initialized',
      );
    final cred = await auth.createUserWithEmailAndPassword(
      email: email,
      password: password,
    );
    return cred.user!.uid;
  }

  Future<String> signIn(String email, String password) async {
    final auth = _auth;
    if (auth == null)
      throw FirebaseException(
        plugin: 'firebase_core',
        code: 'no-app',
        message: 'Firebase not initialized',
      );
    final cred = await auth.signInWithEmailAndPassword(
      email: email,
      password: password,
    );
    return cred.user!.uid;
  }

  Future<void> signOut() async {
    final auth = _auth;
    if (auth == null) return;
    await auth.signOut();
  }
}
