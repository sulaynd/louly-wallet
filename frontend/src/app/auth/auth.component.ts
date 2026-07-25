import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { I18nService } from '../i18n.service';
import { CountryService, ApiCountry } from '../country.service';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css',
})
export class AuthComponent implements OnInit {
  mode: AuthMode = 'login';
  countries: ApiCountry[] = [];
  countriesLoading = true;

  username = '';
  password = '';
  displayName = '';
  /** Just the local part the person types — the calling code is prefixed automatically. */
  localPhone = '';
  country = '';

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(public auth: AuthService, public i18n: I18nService, private countryService: CountryService) {}

  ngOnInit(): void {
    this.countryService.list().subscribe((countries) => {
      this.countries = countries;
      this.countriesLoading = false;
      if (countries.length > 0) {
        this.country = countries[0].name;
      }
    });
  }

  get callingCode(): string {
    return this.countries.find((c) => c.name === this.country)?.callingCode ?? '';
  }

  setMode(mode: AuthMode): void {
    this.mode = mode;
    this.errorMessage = '';
    this.successMessage = '';
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.username || !this.password) {
      this.errorMessage = this.i18n.t('auth.errorRequired');
      return;
    }
    if (this.mode === 'register' && !this.localPhone) {
      this.errorMessage = this.i18n.t('auth.errorPhoneRequired');
      return;
    }

    this.loading = true;

    if (this.mode === 'login') {
      this.auth.login(this.username, this.password).subscribe({
        next: () => {
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.errorMessage = this.i18n.t('auth.errorLogin');
        },
      });
    } else {
      const fullPhone = `${this.callingCode} ${this.localPhone.trim()}`;
      this.auth.register(this.username, this.password, this.displayName, fullPhone, this.country).subscribe({
        next: () => {
          this.loading = false;
          this.successMessage = this.i18n.t('auth.registerSuccess');
          this.mode = 'login';
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage =
            err?.status === 409 ? this.i18n.t('auth.errorTaken') : this.i18n.t('auth.errorRegister');
        },
      });
    }
  }
}
