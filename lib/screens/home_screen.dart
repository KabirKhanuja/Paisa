import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/transaction.dart';
import '../providers.dart';
import '../theme.dart';
import '../widgets/balance_card.dart';
import '../widgets/monthly_chart.dart';
import '../widgets/quick_stats.dart';
import 'add_transaction_screen.dart';
import 'history_screen.dart';
import 'subscriptions_screen.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stats = ref.watch(monthStatsProvider);
    final txnsAsync = ref.watch(transactionsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Paisa', style: TextStyle(fontWeight: FontWeight.w700)),
        actions: [
          IconButton(
            tooltip: 'history',
            icon: const Icon(Icons.receipt_long_outlined),
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const HistoryScreen()),
            ),
          ),
          IconButton(
            tooltip: 'subscriptions',
            icon: const Icon(Icons.repeat_rounded),
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const SubscriptionsScreen()),
            ),
          ),
        ],
      ),
      body: SafeArea(
        top: false,
        child: txnsAsync.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => _ErrorView(error: e),
          data: (_) => ListView(
            padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
            children: [
              const SizedBox(height: 8),
              BalanceCard(balance: stats.balance),
              const SizedBox(height: 24),
              QuickStats(earned: stats.earned, spent: stats.spent),
              const SizedBox(height: 24),
              Row(
                children: [
                  Expanded(
                    child: _ActionButton(
                      label: 'Earn',
                      color: PaisaColors.income,
                      icon: Icons.add_rounded,
                      onTap: () => _openAdd(context, TxnType.earn),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _ActionButton(
                      label: 'Spend',
                      color: PaisaColors.expense,
                      icon: Icons.remove_rounded,
                      onTap: () => _openAdd(context, TxnType.spend),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 28),
              const _SectionLabel('last 6 months'),
              const SizedBox(height: 8),
              MonthlyChart(transactions: ref.watch(transactionsProvider).valueOrNull ?? const []),
            ],
          ),
        ),
      ),
    );
  }

  void _openAdd(BuildContext context, TxnType type) {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => AddTransactionScreen(type: type)),
    );
  }
}

class _ActionButton extends StatelessWidget {
  const _ActionButton({required this.label, required this.color, required this.icon, required this.onTap});

  final String label;
  final Color color;
  final IconData icon;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return FilledButton.icon(
      onPressed: onTap,
      icon: Icon(icon, color: Colors.white),
      label: Text(label, style: const TextStyle(color: Colors.white)),
      style: FilledButton.styleFrom(
        backgroundColor: color,
        minimumSize: const Size.fromHeight(56),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
    );
  }
}

class _SectionLabel extends StatelessWidget {
  const _SectionLabel(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: TextStyle(
        fontSize: 13,
        fontWeight: FontWeight.w500,
        color: Colors.black.withValues(alpha: 0.55),
      ),
    );
  }
}

class _ErrorView extends StatelessWidget {
  const _ErrorView({required this.error});
  final Object error;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.cloud_off_rounded, size: 36, color: Colors.black45),
            const SizedBox(height: 12),
            const Text(
              'could not load data',
              style: TextStyle(fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 4),
            Text(
              '$error',
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 12, color: Colors.black54),
            ),
            const SizedBox(height: 12),
            const Text(
              'run `flutterfire configure` to wire Firebase',
              style: TextStyle(fontSize: 11, color: Colors.black38),
            ),
          ],
        ),
      ),
    );
  }
}
