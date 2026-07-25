import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Recipient, RecipientType } from './models';

export interface CreateRecipientRequest {
  name: string;
  detail: string;
  phoneNumber: string;
  receptionModeName: string;
  deliveryPartner: string;
  address: string;
  city: string;
}

@Injectable({ providedIn: 'root' })
export class RecipientService {
  constructor(private http: HttpClient) {}

  list(type?: RecipientType): Observable<Recipient[]> {
    let params = new HttpParams();
    if (type) {
      params = params.set('type', type);
    }
    return this.http.get<Recipient[]>(`${environment.apiUrl}/recipients`, { params });
  }

  create(request: CreateRecipientRequest): Observable<Recipient> {
    return this.http.post<Recipient>(`${environment.apiUrl}/recipients`, request);
  }
}
