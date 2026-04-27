import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../theme.dart';

class BalanceCard extends StatelessWidget {
  const BalanceCard({super.key, required this.balance});

  final double balance;

  @override
  Widget build(BuildContext context) {
    final fmt = NumberFormat.currency(locale: 'en_IN', symbol: '₹', decimalDigits: 0);
    return Column(
      children: [
        Text(
          fmt.format(balance),
          style: const TextStyle(
            fontSize: 48,
            fontWeight: FontWeight.bold,
            color: PaisaColors.primary,
            height: 1.1,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          'Current Balance',
          style: TextStyle(
            fontSize: 14,
            color: Colors.black.withValues(alpha: 0.55),
          ),
        ),
      ],
    );
  }
}
