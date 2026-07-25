import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { UserAccount } from './models';

@Injectable({ providedIn: 'root' })
export class AccountService {
  constructor(private http: HttpClient) {}

  list(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(`${environment.apiUrl}/accounts`);
  }

  addCard(
    cardHolderName: string,
    cardNumber: string,
    expiryMonth: string,
    expiryYear: string,
    cvc: string,
    countryId: number
  ): Observable<UserAccount> {
    return this.http.post<UserAccount>(`${environment.apiUrl}/accounts/card`, {
      cardHolderName,
      cardNumber,
      expiryMonth,
      expiryYear,
      cvc,
      countryId,
    });
  }
}
