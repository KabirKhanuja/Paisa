import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../theme.dart';

class QuickStats extends StatelessWidget {
  const QuickStats({super.key, required this.earned, required this.spent});

  final double earned;
  final double spent;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(child: _StatTile(label: 'Earned', amount: earned, color: PaisaColors.income, icon: Icons.arrow_upward_rounded)),
        const SizedBox(width: 12),
        Expanded(child: _StatTile(label: 'Spent', amount: spent, color: PaisaColors.expense, icon: Icons.arrow_downward_rounded)),
      ],
    );
  }
}

class _StatTile extends StatelessWidget {
  const _StatTile({required this.label, required this.amount, required this.color, required this.icon});

  final String label;
  final double amount;
  final Color color;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    final fmt = NumberFormat.currency(locale: 'en_IN', symbol: '₹', decimalDigits: 0);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: PaisaColors.surface,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: TextStyle(fontSize: 12, color: Colors.black.withValues(alpha: 0.55))),
                Text(
                  fmt.format(amount),
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: color),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
