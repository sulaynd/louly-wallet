import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RecipientService } from '../recipient.service';
import { I18nService } from '../i18n.service';
import { Recipient } from '../models';
import { CountryService, ApiCountry, ApiReceptionMode } from '../country.service';

type FormStep = 'closed' | 'receptionMode' | 'details';

@Component({
  selector: 'app-recipients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './recipients.component.html',
  styleUrl: './recipients.component.css',
})
export class RecipientsComponent implements OnInit {
  /** When true (e.g. navigated here from the Send screen's "add beneficiary" link), the
   *  add-recipient flow opens immediately instead of showing the list first. */
  @Input() autoOpenForm = false;

  national: Recipient[] = [];
  international: Recipient[] = [];
  loading = true;

  countries: ApiCountry[] = [];

  deliveryPartners: ApiReceptionMode[] = [];

  /** 'closed' = showing the list. 'receptionMode' = pick country then reception mode.
   *  'details' = the rest of the recipient form, once a reception mode's been chosen. */
  formStep: FormStep = 'closed';
  saving = false;
  errorMessage = '';

  formCountryName = '';
  receptionModes: ApiReceptionMode[] = [];
  receptionModesLoading = false;

  formName = '';
  formDetail = '';
  /** Just the local part the person types — the calling code is prefixed automatically. */
  formLocalPhone = '';
  formReceptionModeName = '';
  formDeliveryPartner = '';
  formAddress = '';
  formCity = '';

  constructor(
    private recipientService: RecipientService,
    private countryService: CountryService,
    public i18n: I18nService
  ) {}

  ngOnInit(): void {
    this.loadLists();
    this.countryService.list().subscribe((countries) => (this.countries = countries));
    this.countryService.deliverableReceptionModes().subscribe((receptionModes) => (this.deliveryPartners = receptionModes));
    if (this.autoOpenForm) {
      this.openForm();
    }
  }

  get selectedCountry(): ApiCountry | undefined {
    return this.countries.find((c) => c.name === this.formCountryName);
  }

  get callingCode(): string {
    return this.selectedCountry?.callingCode ?? '';
  }

  private loadLists(): void {
    this.loading = true;
    this.recipientService.list('NATIONAL').subscribe((r) => (this.national = r));
    this.recipientService.list('INTERNATIONAL').subscribe((r) => {
      this.international = r;
      this.loading = false;
    });
  }

  /** Step 1: opens on the country/receptionMode picker, not the full form directly. */
  openForm(): void {
    this.formStep = 'receptionMode';
    this.errorMessage = '';
  }

  /** Country chosen on step 1 — fetches that country's receptionModes. */
  onCountrySelected(): void {
    const country = this.selectedCountry;
    this.receptionModes = [];
    this.formReceptionModeName = '';
    if (!country) {
      return;
    }
    this.receptionModesLoading = true;
    this.countryService.receptionModes(country.id).subscribe({
      next: (receptionModes) => {
        this.receptionModes = receptionModes;
        this.receptionModesLoading = false;
      },
      error: () => {
        this.receptionModesLoading = false;
      },
    });
  }

  /** Picks a fitting icon per receptionMode type — purely presentational, not stored in the DB. */
  receptionModeIcon(name: string): string {
    const lower = name.toLowerCase();
    if (lower.includes('louly') || lower.includes('wave') || lower.includes('orange')) {
      return 'ti-wallet';
    }
    if (lower.includes('cash') || lower.includes('pickup')) {
      return 'ti-map-pin';
    }
    if (lower.includes('banc') || lower.includes('bank')) {
      return 'ti-building-bank';
    }
    return 'ti-building-store';
  }

  /** Bilingual description — falls back to whichever language has content if one is missing. */
  receptionModeDescription(receptionMode: ApiReceptionMode): string {
    const preferred = this.i18n.lang === 'fr' ? receptionMode.descriptionFr : receptionMode.descriptionEn;
    return preferred || receptionMode.descriptionFr || receptionMode.descriptionEn || '';
  }

  /** Tapping a receptionMode chip — moves to step 2, the rest of the recipient details. */
  selectReceptionMode(receptionModeName: string): void {
    this.formReceptionModeName = receptionModeName;
    this.formStep = 'details';
  }

  /** Back from the details step to the receptionMode picker (keeps the country/receptionMode chosen). */
  backToReceptionModeStep(): void {
    this.formStep = 'receptionMode';
  }

  cancelForm(): void {
    this.formStep = 'closed';
    this.formName = '';
    this.formDetail = '';
    this.formLocalPhone = '';
    this.formCountryName = '';
    this.receptionModes = [];
    this.formReceptionModeName = '';
    this.formDeliveryPartner = '';
    this.formAddress = '';
    this.formCity = '';
    this.errorMessage = '';
  }

  get formValid(): boolean {
    return (
      !!this.formName.trim() &&
      !!this.formLocalPhone.trim() &&
      !!this.formAddress.trim() &&
      !!this.formCity.trim() &&
      !!this.formDeliveryPartner
    );
  }

  submit(): void {
    if (!this.formValid) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';

    this.recipientService
      .create({
        name: this.formName.trim(),
        detail: this.formDetail.trim(),
        phoneNumber: `${this.callingCode} ${this.formLocalPhone.trim()}`,
        receptionModeName: this.formReceptionModeName,
        deliveryPartner: this.formDeliveryPartner,
        address: this.formAddress.trim(),
        city: this.formCity.trim(),
      })
      .subscribe({
        next: () => {
          this.saving = false;
          this.cancelForm();
          this.loadLists();
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.error?.error ?? this.i18n.t('recipients.form.error');
        },
      });
  }
}
