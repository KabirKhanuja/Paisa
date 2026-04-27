import 'package:cloud_firestore/cloud_firestore.dart';

import '../models/transaction.dart';

class TransactionRepository {
  TransactionRepository(this.userId);
  final String userId;

  CollectionReference<Map<String, dynamic>> get _col => FirebaseFirestore
      .instance
      .collection('users')
      .doc(userId)
      .collection('transactions');

  Stream<List<Txn>> watchAll() {
    return _col
        .orderBy('timestamp', descending: true)
        .snapshots()
        .map((snap) => snap.docs.map(Txn.fromDoc).toList());
  }

  Future<void> add({required double amount, required TxnType type}) async {
    await _col.add({
      'amount': amount,
      'type': type.name,
      'timestamp': Timestamp.fromDate(DateTime.now()),
    });
  }

  Future<void> update(String id, {required double amount, required TxnType type}) async {
    await _col.doc(id).update({
      'amount': amount,
      'type': type.name,
    });
  }

  Future<void> delete(String id) => _col.doc(id).delete();
}
