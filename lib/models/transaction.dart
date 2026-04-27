import 'package:cloud_firestore/cloud_firestore.dart';

enum TxnType { earn, spend }

class Txn {
  final String id;
  final double amount;
  final TxnType type;
  final DateTime timestamp;

  const Txn({
    required this.id,
    required this.amount,
    required this.type,
    required this.timestamp,
  });

  double get signedAmount => type == TxnType.earn ? amount : -amount;

  Map<String, dynamic> toMap() => {
        'amount': amount,
        'type': type.name,
        'timestamp': Timestamp.fromDate(timestamp),
      };

  factory Txn.fromDoc(DocumentSnapshot<Map<String, dynamic>> doc) {
    final d = doc.data() ?? const {};
    final ts = d['timestamp'];
    return Txn(
      id: doc.id,
      amount: (d['amount'] as num?)?.toDouble() ?? 0,
      type: (d['type'] == 'earn') ? TxnType.earn : TxnType.spend,
      timestamp: ts is Timestamp ? ts.toDate() : DateTime.now(),
    );
  }
}
