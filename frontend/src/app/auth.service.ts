import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';

interface LoginResponse {
  token: string;
  username: string;
  displayName: string;
  phoneNumber: string;
  country: string;
  flagEmoji: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  /** Held in memory only — reloading the page logs the person out, same as before with Basic auth. */
  private token: string | null = null;

  private authedSubject = new BehaviorSubject<boolean>(false);
  authed$ = this.authedSubject.asObservable();

  username = '';
  displayName = '';
  phoneNumber = '';
  country = '';
  flagEmoji = '';

  constructor(private http: HttpClient) {}

  get isAuthenticated(): boolean {
    return this.token !== null;
  }

  /** Bearer header for the current JWT, or {} if not logged in. */
  authHeader(): Record<string, string> {
    return this.token ? { Authorization: `Bearer ${this.token}` } : {};
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { username, password }).pipe(
      tap((res) => {
        this.token = res.token;
        this.username = res.username;
        this.displayName = res.displayName;
        this.phoneNumber = res.phoneNumber;
        this.country = res.country;
        this.flagEmoji = res.flagEmoji;
        this.authedSubject.next(true);
      })
    );
  }

  register(
    username: string,
    password: string,
    displayName: string,
    phoneNumber: string,
    country: string
  ): Observable<unknown> {
    return this.http.post(`${environment.apiUrl}/auth/register`, {
      username,
      password,
      displayName,
      phoneNumber,
      country,
    });
  }

  logout(): void {
    this.token = null;
    this.username = '';
    this.displayName = '';
    this.phoneNumber = '';
    this.country = '';
    this.flagEmoji = '';
    this.authedSubject.next(false);
  }
}
