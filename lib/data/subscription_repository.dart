import 'package:cloud_firestore/cloud_firestore.dart';

import '../models/subscription.dart';

class SubscriptionRepository {
  SubscriptionRepository(this.userId);
  final String userId;

  CollectionReference<Map<String, dynamic>> get _col => FirebaseFirestore
      .instance
      .collection('users')
      .doc(userId)
      .collection('subscriptions');

  Stream<List<Subscription>> watchAll() {
    return _col
        .orderBy('name')
        .snapshots()
        .map((snap) => snap.docs.map(Subscription.fromDoc).toList());
  }

  Future<void> add(Subscription sub) async {
    await _col.add(sub.toMap());
  }

  Future<void> delete(String id) => _col.doc(id).delete();
}
