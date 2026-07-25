import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface CurrencyAmount {
  currency: string;
  amount: number;
}

export interface AccountingSummary {
  transactionCount: number;
  totalPlatformRevenueCAD: number;
  receptionModeCommissionExpenseByCurrency: CurrencyAmount[];
  receptionModePrincipalOwedByCurrency: CurrencyAmount[];
}

export interface ReceptionModeBalance {
  receptionModeId: number;
  receptionModeName: string;
  currency: string;
  totalOwed: number;
  totalSettled: number;
  currentBalance: number;
}

export interface LedgerEntry {
  id: number;
  receptionModeName: string;
  transferId: number | null;
  type: 'COMMISSION_OWED' | 'SETTLEMENT_PAYMENT';
  amount: number;
  currency: string;
  createdAt: string;
  note: string | null;
  recordedBy: string | null;
}

@Injectable({ providedIn: 'root' })
export class AccountingService {
  constructor(private http: HttpClient) {}

  summary(): Observable<AccountingSummary> {
    return this.http.get<AccountingSummary>(`${environment.apiUrl}/admin/accounting/summary`);
  }

  balances(): Observable<ReceptionModeBalance[]> {
    return this.http.get<ReceptionModeBalance[]>(`${environment.apiUrl}/admin/accounting/balances`);
  }

  ledger(): Observable<LedgerEntry[]> {
    return this.http.get<LedgerEntry[]>(`${environment.apiUrl}/admin/accounting/ledger`);
  }

  recordSettlement(receptionModeId: number, amount: number, currency: string, note: string): Observable<LedgerEntry> {
    return this.http.post<LedgerEntry>(`${environment.apiUrl}/admin/accounting/settlements`, {
      receptionModeId,
      amount,
      currency,
      note,
    });
  }
}
