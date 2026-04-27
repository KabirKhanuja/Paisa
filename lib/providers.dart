import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'data/auth_service.dart';
import 'data/subscription_repository.dart';
import 'data/transaction_repository.dart';
import 'models/subscription.dart';
import 'models/transaction.dart';

final authServiceProvider = Provider<AuthService>((ref) => AuthService());

final userIdProvider = Provider<String?>((ref) {
  return ref.read(authServiceProvider).userId;
});

final transactionRepoProvider = Provider<TransactionRepository?>((ref) {
  final uid = ref.watch(userIdProvider);
  return uid != null ? TransactionRepository(uid) : null;
});

final subscriptionRepoProvider = Provider<SubscriptionRepository?>((ref) {
  final uid = ref.watch(userIdProvider);
  return uid != null ? SubscriptionRepository(uid) : null;
});

final transactionsProvider = StreamProvider<List<Txn>>((ref) {
  final repo = ref.watch(transactionRepoProvider);
  if (repo == null) return const Stream.empty();
  return repo.watchAll();
});

final subscriptionsProvider = StreamProvider<List<Subscription>>((ref) {
  final repo = ref.watch(subscriptionRepoProvider);
  if (repo == null) return const Stream.empty();
  return repo.watchAll();
});

class MonthStats {
  final double earned;
  final double spent;
  final double balance;
  const MonthStats({
    required this.earned,
    required this.spent,
    required this.balance,
  });
}

final monthStatsProvider = Provider<MonthStats>((ref) {
  final txns = ref.watch(transactionsProvider).valueOrNull ?? const [];
  final now = DateTime.now();
  double earned = 0, spent = 0, balance = 0;
  for (final t in txns) {
    balance += t.signedAmount;
    if (t.timestamp.year == now.year && t.timestamp.month == now.month) {
      if (t.type == TxnType.earn) {
        earned += t.amount;
      } else {
        spent += t.amount;
      }
    }
  }
  return MonthStats(earned: earned, spent: spent, balance: balance);
});
