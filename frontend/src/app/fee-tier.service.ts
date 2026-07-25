import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface ApiFeeTier {
  id: number;
  countryId: number;
  countryName: string;
  minAmount: number;
  maxAmount: number | null;
  feePercent: number;
}

@Injectable({ providedIn: 'root' })
export class FeeTierService {
  constructor(private http: HttpClient) {}

  /** Public endpoint — a specific country's grid, sorted ascending by minAmount. */
  forCountry(countryId: number): Observable<ApiFeeTier[]> {
    return this.http.get<ApiFeeTier[]>(`${environment.apiUrl}/fee-tiers`, {
      params: { countryId },
    });
  }
}
