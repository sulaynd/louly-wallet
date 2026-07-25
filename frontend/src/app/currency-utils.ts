/** Currencies with no minor unit (no cents/centimes) — e.g. the West African CFA franc. */
const ZERO_DECIMAL_CURRENCIES = ['XOF', 'XAF', 'JPY', 'KRW', 'VND', 'CLP'];

/**
 * Currencies whose smallest *actual circulating* denomination isn't 1 unit — e.g. the CFA
 * franc has no 1-franc coin; the smallest coins are 5, 10, 25, 50... So amounts in these
 * currencies should round to the nearest multiple of this increment, not just the nearest
 * whole unit.
 */
const ROUNDING_INCREMENT_BY_CURRENCY: Record<string, number> = {
  XOF: 5,
  XAF: 5,
};

export function decimalsForCurrency(code: string | null | undefined): number {
  return ZERO_DECIMAL_CURRENCIES.includes((code ?? '').toUpperCase()) ? 0 : 2;
}

/** The smallest real-world increment for this currency (e.g. 5 for XOF, 0.01 for CAD/EUR, 1 for JPY). */
export function roundingIncrementFor(code: string | null | undefined): number {
  const upper = (code ?? '').toUpperCase();
  if (ROUNDING_INCREMENT_BY_CURRENCY[upper]) {
    return ROUNDING_INCREMENT_BY_CURRENCY[upper];
  }
  return decimalsForCurrency(upper) === 0 ? 1 : 0.01;
}

/** Rounds a monetary amount to the nearest real, spendable denomination of the given currency. */
export function roundForCurrency(value: number, code: string | null | undefined): number {
  const increment = roundingIncrementFor(code);
  return Math.round(value / increment) * increment;
}
