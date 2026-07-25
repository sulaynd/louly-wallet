import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface ApiCountry {
  id: number;
  name: string;
  flagEmoji: string;
  currencyCode: string;
  callingCode: string;
  active: boolean;
}

export interface ApiReceptionMode {
  id: number;
  name: string;
  descriptionFr: string;
  descriptionEn: string;
  livrable: boolean;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class CountryService {
  constructor(private http: HttpClient) {}

  /** Public endpoint — only returns active countries. Safe to call before login. */
  list(): Observable<ApiCountry[]> {
    return this.http.get<ApiCountry[]>(`${environment.apiUrl}/countries`);
  }

  receptionModes(countryId: number): Observable<ApiReceptionMode[]> {
    return this.http.get<ApiReceptionMode[]>(`${environment.apiUrl}/countries/${countryId}/reception-modes`);
  }

  /** Only the receptionModes flagged "livrable" — used for the "Partenaire de livraison" dropdown. */
  deliverableReceptionModes(): Observable<ApiReceptionMode[]> {
    return this.http.get<ApiReceptionMode[]>(`${environment.apiUrl}/reception-modes/deliverable`);
  }
}
