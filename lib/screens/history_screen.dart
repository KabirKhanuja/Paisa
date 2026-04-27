import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../models/transaction.dart';
import '../providers.dart';
import '../theme.dart';
import 'add_transaction_screen.dart';

class HistoryScreen extends ConsumerWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final txnsAsync = ref.watch(transactionsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('History')),
      body: txnsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('error: $e')),
        data: (txns) {
          if (txns.isEmpty) {
            return Center(
              child: Text(
                'no transactions yet',
                style: TextStyle(color: Colors.black.withValues(alpha: 0.5)),
              ),
            );
          }
          return ListView.separated(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: txns.length,
            separatorBuilder: (_, _) => const Divider(height: 1, indent: 20, endIndent: 20),
            itemBuilder: (context, i) {
              final t = txns[i];
              return Dismissible(
                key: ValueKey(t.id),
                direction: DismissDirection.endToStart,
                background: Container(
                  color: PaisaColors.expense.withValues(alpha: 0.1),
                  alignment: Alignment.centerRight,
                  padding: const EdgeInsets.only(right: 24),
                  child: const Icon(Icons.delete_outline, color: PaisaColors.expense),
                ),
                confirmDismiss: (_) async {
                  return await showDialog<bool>(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: const Text('delete this transaction?'),
                          actions: [
                            TextButton(
                              onPressed: () => Navigator.pop(ctx, false),
                              child: const Text('cancel'),
                            ),
                            FilledButton(
                              onPressed: () => Navigator.pop(ctx, true),
                              style: FilledButton.styleFrom(backgroundColor: PaisaColors.expense),
                              child: const Text('delete'),
                            ),
                          ],
                        ),
                      ) ??
                      false;
                },
                onDismissed: (_) {
                  ref.read(transactionRepoProvider)?.delete(t.id);
                },
                child: _TxnTile(
                  txn: t,
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => AddTransactionScreen(
                        type: t.type,
                        editingId: t.id,
                        initialAmount: t.amount,
                      ),
                    ),
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}

class _TxnTile extends StatelessWidget {
  const _TxnTile({required this.txn, required this.onTap});
  final Txn txn;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final isEarn = txn.type == TxnType.earn;
    final color = isEarn ? PaisaColors.income : PaisaColors.expense;
    final fmt = NumberFormat.currency(locale: 'en_IN', symbol: '₹', decimalDigits: 0);
    final dateStr = DateFormat('d MMM, h:mm a').format(txn.timestamp);

    return ListTile(
      onTap: onTap,
      leading: CircleAvatar(
        backgroundColor: color.withValues(alpha: 0.1),
        child: Icon(
          isEarn ? Icons.arrow_upward_rounded : Icons.arrow_downward_rounded,
          color: color,
        ),
      ),
      title: Text(
        '${isEarn ? '+' : '-'}${fmt.format(txn.amount)}',
        style: TextStyle(fontWeight: FontWeight.w700, color: color),
      ),
      subtitle: Text(
        '${isEarn ? 'Earn' : 'Spend'} · $dateStr',
        style: const TextStyle(fontSize: 12),
      ),
    );
  }
}
