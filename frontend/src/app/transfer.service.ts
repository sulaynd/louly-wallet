import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';
import { RateQuote, SendMoneyRequest, Transfer } from './models';

@Injectable({ providedIn: 'root' })
export class TransferService {
  /** Id of the most recently created transfer, so the Track tab can show it immediately. */
  private lastTransferIdSubject = new BehaviorSubject<number | null>(null);
  lastTransferId$ = this.lastTransferIdSubject.asObservable();

  constructor(private http: HttpClient) {}

  getQuote(from: string, to: string, amount: number): Observable<RateQuote> {
    return this.http.get<RateQuote>(`${environment.apiUrl}/rates`, { params: { from, to, amount } });
  }

  send(request: SendMoneyRequest): Observable<Transfer> {
    return this.http.post<Transfer>(`${environment.apiUrl}/transfers`, request).pipe(
      tap((transfer) => this.lastTransferIdSubject.next(transfer.id))
    );
  }

  getTransfer(id: number): Observable<Transfer> {
    return this.http.get<Transfer>(`${environment.apiUrl}/transfers/${id}`);
  }

  getLatestTransfer(): Observable<Transfer> {
    return this.http.get<Transfer>(`${environment.apiUrl}/transfers/latest`);
  }

  getHistory(): Observable<Transfer[]> {
    return this.http.get<Transfer[]>(`${environment.apiUrl}/transfers`);
  }

  getLimits(): Observable<{ minAmount: number; maxAmount: number }> {
    return this.http.get<{ minAmount: number; maxAmount: number }>(`${environment.apiUrl}/transfers/limits`);
  }
}
