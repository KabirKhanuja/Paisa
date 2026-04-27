import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../models/subscription.dart';
import '../providers.dart';
import '../theme.dart';

class SubscriptionsScreen extends ConsumerWidget {
  const SubscriptionsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final subsAsync = ref.watch(subscriptionsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Subscriptions')),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddSheet(context, ref),
        child: const Icon(Icons.add),
      ),
      body: subsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('error: $e')),
        data: (subs) {
          if (subs.isEmpty) {
            return Center(
              child: Text(
                'no subscriptions tracked',
                style: TextStyle(color: Colors.black.withValues(alpha: 0.5)),
              ),
            );
          }
          return ListView.separated(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: subs.length,
            separatorBuilder: (_, _) => const Divider(height: 1, indent: 20, endIndent: 20),
            itemBuilder: (context, i) {
              final s = subs[i];
              return Dismissible(
                key: ValueKey(s.id),
                direction: DismissDirection.endToStart,
                background: Container(
                  color: PaisaColors.expense.withValues(alpha: 0.1),
                  alignment: Alignment.centerRight,
                  padding: const EdgeInsets.only(right: 24),
                  child: const Icon(Icons.delete_outline, color: PaisaColors.expense),
                ),
                onDismissed: (_) {
                  ref.read(subscriptionRepoProvider)?.delete(s.id);
                },
                child: _SubTile(sub: s),
              );
            },
          );
        },
      ),
    );
  }

  Future<void> _showAddSheet(BuildContext context, WidgetRef ref) async {
    final nameCtrl = TextEditingController();
    final amountCtrl = TextEditingController();
    BillingCycle cycle = BillingCycle.monthly;

    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (ctx) {
        return Padding(
          padding: EdgeInsets.only(
            left: 20,
            right: 20,
            top: 8,
            bottom: MediaQuery.of(ctx).viewInsets.bottom + 20,
          ),
          child: StatefulBuilder(
            builder: (ctx, setState) => Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text('add subscription', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
                const SizedBox(height: 16),
                TextField(
                  controller: nameCtrl,
                  decoration: const InputDecoration(labelText: 'name', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: amountCtrl,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(labelText: 'amount', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 12),
                SegmentedButton<BillingCycle>(
                  segments: const [
                    ButtonSegment(value: BillingCycle.monthly, label: Text('monthly')),
                    ButtonSegment(value: BillingCycle.yearly, label: Text('yearly')),
                  ],
                  selected: {cycle},
                  onSelectionChanged: (s) => setState(() => cycle = s.first),
                ),
                const SizedBox(height: 20),
                FilledButton(
                  onPressed: () async {
                    final name = nameCtrl.text.trim();
                    final amount = double.tryParse(amountCtrl.text.trim()) ?? 0;
                    if (name.isEmpty || amount <= 0) return;
                    await ref.read(subscriptionRepoProvider)?.add(
                          Subscription(id: '', name: name, amount: amount, cycle: cycle),
                        );
                    if (ctx.mounted) Navigator.pop(ctx);
                  },
                  child: const Text('save'),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _SubTile extends StatelessWidget {
  const _SubTile({required this.sub});
  final Subscription sub;

  @override
  Widget build(BuildContext context) {
    final fmt = NumberFormat.currency(locale: 'en_IN', symbol: '₹', decimalDigits: 0);
    return ListTile(
      leading: const CircleAvatar(
        backgroundColor: Color(0x1A2563EB),
        child: Icon(Icons.repeat_rounded, color: PaisaColors.primary),
      ),
      title: Text(sub.name, style: const TextStyle(fontWeight: FontWeight.w600)),
      subtitle: Text(sub.cycle.name),
      trailing: Text(
        fmt.format(sub.amount),
        style: const TextStyle(fontWeight: FontWeight.w700),
      ),
    );
  }
}
