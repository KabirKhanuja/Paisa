import 'package:flutter/material.dart';

import '../theme.dart';

class NumericKeypad extends StatelessWidget {
  const NumericKeypad({
    super.key,
    required this.onDigit,
    required this.onBackspace,
    required this.onDecimal,
  });

  final ValueChanged<String> onDigit;
  final VoidCallback onBackspace;
  final VoidCallback onDecimal;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        _row(['1', '2', '3']),
        _row(['4', '5', '6']),
        _row(['7', '8', '9']),
        Row(
          children: [
            _key(label: '.', onTap: onDecimal),
            _key(label: '0', onTap: () => onDigit('0')),
            _key(child: const Icon(Icons.backspace_outlined), onTap: onBackspace),
          ],
        ),
      ],
    );
  }

  Widget _row(List<String> digits) {
    return Row(
      children: digits.map((d) => _key(label: d, onTap: () => onDigit(d))).toList(),
    );
  }

  Widget _key({String? label, Widget? child, required VoidCallback onTap}) {
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.all(6),
        child: Material(
          color: PaisaColors.surface,
          borderRadius: BorderRadius.circular(16),
          child: InkWell(
            borderRadius: BorderRadius.circular(16),
            onTap: onTap,
            child: SizedBox(
              height: 64,
              child: Center(
                child: child ??
                    Text(
                      label ?? '',
                      style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w500),
                    ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
