import 'package:cloud_firestore/cloud_firestore.dart';

enum BillingCycle { monthly, yearly }

class Subscription {
  final String id;
  final String name;
  final double amount;
  final BillingCycle cycle;

  const Subscription({
    required this.id,
    required this.name,
    required this.amount,
    required this.cycle,
  });

  Map<String, dynamic> toMap() => {
        'name': name,
        'amount': amount,
        'cycle': cycle.name,
      };

  factory Subscription.fromDoc(DocumentSnapshot<Map<String, dynamic>> doc) {
    final d = doc.data() ?? const {};
    return Subscription(
      id: doc.id,
      name: (d['name'] as String?) ?? '',
      amount: (d['amount'] as num?)?.toDouble() ?? 0,
      cycle: (d['cycle'] == 'yearly') ? BillingCycle.yearly : BillingCycle.monthly,
    );
  }
}
