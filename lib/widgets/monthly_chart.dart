import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../models/transaction.dart';
import '../theme.dart';

class MonthlyChart extends StatelessWidget {
  const MonthlyChart({super.key, required this.transactions});

  final List<Txn> transactions;

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();
    final months = List.generate(6, (i) {
      final m = DateTime(now.year, now.month - (5 - i), 1);
      return m;
    });

    double earnedFor(DateTime m) => transactions
        .where((t) => t.type == TxnType.earn && t.timestamp.year == m.year && t.timestamp.month == m.month)
        .fold(0.0, (a, b) => a + b.amount);

    double spentFor(DateTime m) => transactions
        .where((t) => t.type == TxnType.spend && t.timestamp.year == m.year && t.timestamp.month == m.month)
        .fold(0.0, (a, b) => a + b.amount);

    final earnedVals = months.map(earnedFor).toList();
    final spentVals = months.map(spentFor).toList();
    final maxVal = [
      ...earnedVals,
      ...spentVals,
    ].fold<double>(0, (a, b) => b > a ? b : a);
    final maxY = maxVal == 0 ? 100.0 : maxVal * 1.2;

    final groups = <BarChartGroupData>[];
    for (var i = 0; i < months.length; i++) {
      groups.add(BarChartGroupData(
        x: i,
        barsSpace: 4,
        barRods: [
          BarChartRodData(
            toY: earnedVals[i],
            color: PaisaColors.income,
            width: 8,
            borderRadius: BorderRadius.circular(2),
          ),
          BarChartRodData(
            toY: spentVals[i],
            color: PaisaColors.expense,
            width: 8,
            borderRadius: BorderRadius.circular(2),
          ),
        ],
      ));
    }

    return Container(
      padding: const EdgeInsets.fromLTRB(12, 16, 12, 8),
      decoration: BoxDecoration(
        color: PaisaColors.surface,
        borderRadius: BorderRadius.circular(20),
      ),
      height: 200,
      child: BarChart(
        BarChartData(
          maxY: maxY,
          borderData: FlBorderData(show: false),
          gridData: const FlGridData(show: false),
          titlesData: FlTitlesData(
            leftTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
            topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
            rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                reservedSize: 24,
                getTitlesWidget: (value, meta) {
                  final i = value.toInt();
                  if (i < 0 || i >= months.length) return const SizedBox.shrink();
                  return Padding(
                    padding: const EdgeInsets.only(top: 4),
                    child: Text(
                      DateFormat.MMM().format(months[i]),
                      style: const TextStyle(fontSize: 11, color: Colors.black54),
                    ),
                  );
                },
              ),
            ),
          ),
          barGroups: groups,
        ),
      ),
    );
  }
}
