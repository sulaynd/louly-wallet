import { decimalsForCurrency, roundingIncrementFor, roundForCurrency } from './currency-utils';

describe('currency-utils', () => {
  describe('decimalsForCurrency', () => {
    it('returns 0 for zero-decimal currencies (XOF, JPY, etc.)', () => {
      expect(decimalsForCurrency('XOF')).toBe(0);
      expect(decimalsForCurrency('JPY')).toBe(0);
      expect(decimalsForCurrency('XAF')).toBe(0);
    });

    it('returns 2 for standard two-decimal currencies', () => {
      expect(decimalsForCurrency('CAD')).toBe(2);
      expect(decimalsForCurrency('EUR')).toBe(2);
      expect(decimalsForCurrency('USD')).toBe(2);
    });

    it('is case-insensitive', () => {
      expect(decimalsForCurrency('xof')).toBe(0);
      expect(decimalsForCurrency('cad')).toBe(2);
    });

    it('defaults to 2 decimals for null/undefined', () => {
      expect(decimalsForCurrency(null)).toBe(2);
      expect(decimalsForCurrency(undefined)).toBe(2);
    });
  });

  describe('roundingIncrementFor', () => {
    it('returns 5 for XOF/XAF — no 1-franc coin exists', () => {
      expect(roundingIncrementFor('XOF')).toBe(5);
      expect(roundingIncrementFor('XAF')).toBe(5);
    });

    it('returns 1 for other zero-decimal currencies (e.g. JPY)', () => {
      expect(roundingIncrementFor('JPY')).toBe(1);
    });

    it('returns 0.01 for standard two-decimal currencies', () => {
      expect(roundingIncrementFor('CAD')).toBe(0.01);
      expect(roundingIncrementFor('EUR')).toBe(0.01);
    });
  });

  describe('roundForCurrency', () => {
    it('rounds CAD to the nearest cent', () => {
      expect(roundForCurrency(10.567, 'CAD')).toBeCloseTo(10.57, 5);
      expect(roundForCurrency(10.561, 'CAD')).toBeCloseTo(10.56, 5);
    });

    it('rounds XOF to the nearest multiple of 5 (no 1-franc coin)', () => {
      expect(roundForCurrency(1002, 'XOF')).toBe(1000);
      expect(roundForCurrency(1003, 'XOF')).toBe(1005);
      expect(roundForCurrency(1007.5, 'XOF')).toBe(1010);
    });

    it('rounds JPY to the nearest whole yen', () => {
      expect(roundForCurrency(1000.6, 'JPY')).toBe(1001);
      expect(roundForCurrency(1000.4, 'JPY')).toBe(1000);
    });

    it('matches the confirmed real scenario: 20 000 XOF fee stays a clean multiple of 5', () => {
      // From the fee-tier work earlier: 20 000 XOF @ 3% = 600 XOF exactly, already a multiple of 5.
      expect(roundForCurrency(600, 'XOF')).toBe(600);
    });
  });
});
