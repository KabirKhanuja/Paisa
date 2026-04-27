import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../models/transaction.dart';
import '../providers.dart';
import '../theme.dart';
import '../widgets/numeric_keypad.dart';

class AddTransactionScreen extends ConsumerStatefulWidget {
  const AddTransactionScreen({super.key, required this.type, this.editingId, this.initialAmount});

  final TxnType type;
  final String? editingId;
  final double? initialAmount;

  @override
  ConsumerState<AddTransactionScreen> createState() => _AddTransactionScreenState();
}

class _AddTransactionScreenState extends ConsumerState<AddTransactionScreen> {
  late String _input;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _input = widget.initialAmount != null ? _formatInitial(widget.initialAmount!) : '0';
  }

  String _formatInitial(double v) {
    if (v == v.roundToDouble()) return v.toInt().toString();
    return v.toString();
  }

  bool get _isEarn => widget.type == TxnType.earn;
  Color get _accent => _isEarn ? PaisaColors.income : PaisaColors.expense;
  String get _title => widget.editingId != null
      ? 'edit ${_isEarn ? 'earn' : 'spend'}'
      : (_isEarn ? 'Earn' : 'Spend');

  void _addDigit(String d) {
    setState(() {
      if (_input == '0') {
        _input = d;
      } else if (_input.length < 12) {
        _input = _input + d;
      }
    });
  }

  void _addDecimal() {
    if (_input.contains('.')) return;
    setState(() => _input = '$_input.');
  }

  void _backspace() {
    setState(() {
      if (_input.length <= 1) {
        _input = '0';
      } else {
        _input = _input.substring(0, _input.length - 1);
      }
    });
  }

  void _quickAdd(int n) {
    final current = double.tryParse(_input) ?? 0;
    final updated = current + n;
    setState(() => _input = _formatInitial(updated));
  }

  Future<void> _save() async {
    final amount = double.tryParse(_input) ?? 0;
    if (amount <= 0 || _saving) return;
    setState(() => _saving = true);
    final repo = ref.read(transactionRepoProvider);
    if (repo == null) {
      setState(() => _saving = false);
      return;
    }
    try {
      if (widget.editingId != null) {
        await repo.update(widget.editingId!, amount: amount, type: widget.type);
      } else {
        await repo.add(amount: amount, type: widget.type);
      }
      if (mounted) Navigator.of(context).pop();
    } catch (e) {
      if (!mounted) return;
      setState(() => _saving = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('failed: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final fmt = NumberFormat.currency(locale: 'en_IN', symbol: '₹', decimalDigits: 0);
    final amount = double.tryParse(_input) ?? 0;

    return Scaffold(
      appBar: AppBar(title: Text(_title)),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 16),
          child: Column(
            children: [
              const SizedBox(height: 8),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 16),
                decoration: BoxDecoration(
                  color: PaisaColors.surface,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Column(
                  children: [
                    Text(
                      _isEarn ? 'adding to balance' : 'subtracting from balance',
                      style: TextStyle(fontSize: 12, color: Colors.black.withValues(alpha: 0.5)),
                    ),
                    const SizedBox(height: 8),
                    FittedBox(
                      child: Text(
                        fmt.format(amount),
                        style: TextStyle(fontSize: 44, fontWeight: FontWeight.bold, color: _accent),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  _QuickChip(label: '+100', onTap: () => _quickAdd(100)),
                  _QuickChip(label: '+500', onTap: () => _quickAdd(500)),
                  _QuickChip(label: '+1000', onTap: () => _quickAdd(1000)),
                ],
              ),
              const SizedBox(height: 12),
              NumericKeypad(
                onDigit: _addDigit,
                onBackspace: _backspace,
                onDecimal: _addDecimal,
              ),
              const SizedBox(height: 12),
              FilledButton(
                onPressed: _saving ? null : _save,
                style: FilledButton.styleFrom(
                  backgroundColor: _accent,
                  minimumSize: const Size.fromHeight(56),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                ),
                child: _saving
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : const Text('Add', style: TextStyle(color: Colors.white)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _QuickChip extends StatelessWidget {
  const _QuickChip({required this.label, required this.onTap});
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4),
        child: OutlinedButton(
          onPressed: onTap,
          style: OutlinedButton.styleFrom(
            minimumSize: const Size.fromHeight(40),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            side: BorderSide(color: Colors.black.withValues(alpha: 0.1)),
          ),
          child: Text(label, style: const TextStyle(fontWeight: FontWeight.w500)),
        ),
      ),
    );
  }
}
