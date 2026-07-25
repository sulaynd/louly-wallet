import { Pipe, PipeTransform } from '@angular/core';
import { decimalsForCurrency } from './currency-utils';

/**
 * Formats a monetary amount with the right number of decimals for its currency, and a thousands
 * separator matching real-world convention for that currency — a space for XOF/XAF (how CFA
 * franc amounts are actually written in Francophone Africa, e.g. "10 000 XOF"), regardless of
 * whatever language the interface itself is currently set to.
 *
 * Usage: {{ amount | money: currencyCode }}  →  "10 000 XOF" or "1 234,56 CAD"
 */
@Pipe({ name: 'money', standalone: true })
export class MoneyPipe implements PipeTransform {
  private static readonly SPACE_GROUPED_CURRENCIES = ['XOF', 'XAF'];

  transform(value: number | null | undefined, currencyCode: string | null | undefined): string {
    if (value === null || value === undefined || isNaN(value)) {
      return '';
    }
    const code = (currencyCode ?? '').toUpperCase();
    const decimals = decimalsForCurrency(code);
    const locale = MoneyPipe.SPACE_GROUPED_CURRENCIES.includes(code) ? 'fr-FR' : undefined;

    const formatted = value.toLocaleString(locale, {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals,
    });
    return `${formatted} ${code}`;
  }
}
