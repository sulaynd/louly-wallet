import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RecipientService } from '../recipient.service';
import { TransferService } from '../transfer.service';
import { CountryService, ApiCountry } from '../country.service';
import { AccountService } from '../account.service';
import { AuthService } from '../auth.service';
import { I18nService } from '../i18n.service';
import { Recipient, Transfer, UserAccount } from '../models';
import { decimalsForCurrency, roundForCurrency } from '../currency-utils';
import { MoneyPipe } from '../money.pipe';

@Component({
  selector: 'app-send',
  standalone: true,
  imports: [CommonModule, FormsModule, MoneyPipe],
  templateUrl: './send.component.html',
  styleUrl: './send.component.css',
})
export class SendComponent implements OnInit {
  @Output() transferSent = new EventEmitter<number>();
  @Output() addBeneficiaryRequested = new EventEmitter<void>();

  amount = 0;
  receivedAmount = 0;

  recipients: Recipient[] = [];
  recipientsLoaded = false;

  /** The recipient chosen via the contact search below — null until the person picks one.
   *  Whether the transfer is national or international is derived from this recipient's own
   *  type, not chosen manually. */
  selectedRecipient: Recipient | null = null;
  searchTerm = '';
  showContactPicker = false;

  rate = 1;
  fee = 0;
  /** The sender's own currency — resolved from their account's country, not assumed to be CAD. */
  sourceCurrency = 'CAD';
  loading = false;
  sending = false;
  errorMessage = '';

  /** Fetched from the backend so the cap is never out of sync with what the server enforces. */
  maxAmount = Infinity;
  /** The lowest tier's floor for the sender's own country — 0 until fee tiers are fetched. */
  minAmount = 0;

  /** True once the person clicks "Review transfer" — shows a recap, nothing sent yet. */
  reviewMode = false;

  completedTransfer: Transfer | null = null;

  /** The sender's own funding sources — compte dépôt (always present) plus any bank references
   *  they've added. */
  accounts: UserAccount[] = [];
  selectedAccount: UserAccount | null = null;
  showAddCardForm = false;
  newCardHolderName = '';
  newCardNumber = '';
  newCardExpiryMonth = '';
  newCardExpiryYear = '';
  /** Sent once to verify the card at add-time (as a real payment gateway would need), but never
   *  persisted server-side — see UserAccountController.verifyCard(). */
  newCardCvc = '';
  newCardCountryId: number | null = null;
  savingCard = false;

  /** Full country list, for the "add card" form's country selector — defaults to the person's
   *  own country but is changeable, since a card isn't always issued where you live. */
  countries: ApiCountry[] = [];

  constructor(
    private recipientService: RecipientService,
    private transferService: TransferService,
    private countryService: CountryService,
    private accountService: AccountService,
    private auth: AuthService,
    public i18n: I18nService
  ) {}

  /** Recent transaction history, shown below the beneficiary field before a recipient is picked. */
  recentTransfers: Transfer[] = [];

  ngOnInit(): void {
    this.recipientService.list().subscribe((recipients) => {
      this.recipients = recipients;
      this.recipientsLoaded = true;
    });
    this.transferService.getLimits().subscribe((limits) => {
      this.minAmount = limits.minAmount;
      this.maxAmount = limits.maxAmount;
    });
    this.countryService.list().subscribe((countries) => {
      this.countries = countries;
      const own = countries.find((c) => c.name === this.auth.country);
      if (own) {
        this.sourceCurrency = own.currencyCode;
        this.newCardCountryId = own.id;
      }
    });
    this.transferService.getHistory().subscribe((transfers) => {
      this.recentTransfers = transfers;
    });
    this.loadAccounts();
  }

  private loadAccounts(): void {
    this.accountService.list().subscribe((accounts) => {
      this.accounts = accounts;
      // Default to the deposit account if the person hasn't already picked something else.
      if (!this.selectedAccount) {
        this.selectedAccount = accounts.find((a) => a.type === 'DEPOT') ?? accounts[0] ?? null;
      }
    });
  }

  selectAccount(account: UserAccount): void {
    this.selectedAccount = account;
  }

  openAddCardForm(): void {
    this.showAddCardForm = true;
    this.newCardHolderName = '';
    this.newCardNumber = '';
    this.newCardExpiryMonth = '';
    this.newCardExpiryYear = '';
    this.newCardCvc = '';
    // newCardCountryId is left as-is — already defaulted to the person's own country in ngOnInit.
  }

  cancelAddCardForm(): void {
    this.showAddCardForm = false;
  }

  /** The underlying value with spaces stripped — what's actually validated and sent. */
  get cardNumberDigitsOnly(): string {
    return this.newCardNumber.replace(/\s/g, '');
  }

  /** Auto-inserts a space every 4 digits as the person types, e.g. "4111 1111 1111 1111" —
   *  same convention Stripe, PayPal, and most card forms use. The underlying value sent to the
   *  backend is always the digits-only version (see cardNumberDigitsOnly). */
  onCardNumberInput(): void {
    const digits = this.newCardNumber.replace(/\D/g, '').slice(0, 19);
    this.newCardNumber = digits.replace(/(.{4})/g, '$1 ').trim();
  }

  get cardFormValid(): boolean {
    return (
      this.newCardHolderName.trim().length > 0 &&
      /^\d{13,19}$/.test(this.cardNumberDigitsOnly) &&
      /^(0[1-9]|1[0-2])$/.test(this.newCardExpiryMonth) &&
      /^\d{4}$/.test(this.newCardExpiryYear) &&
      /^\d{3,4}$/.test(this.newCardCvc) &&
      this.newCardCountryId !== null
    );
  }

  addCard(): void {
    if (!this.cardFormValid || this.newCardCountryId === null) {
      return;
    }
    this.savingCard = true;
    this.accountService
      .addCard(this.newCardHolderName.trim(), this.cardNumberDigitsOnly, this.newCardExpiryMonth, this.newCardExpiryYear, this.newCardCvc, this.newCardCountryId)
      .subscribe({
        next: (account) => {
          this.savingCard = false;
          this.showAddCardForm = false;
          this.accounts = [...this.accounts, account];
          this.selectedAccount = account;
        },
        error: () => {
          this.savingCard = false;
        },
      });
  }

  openContactPicker(): void {
    this.showContactPicker = true;
  }

  goToAddBeneficiary(): void {
    this.addBeneficiaryRequested.emit();
  }

  goToAllTransactions(): void {
    this.showAllTransactions = true;
  }

  /** True once "Voir +" / "Voir toutes les transactions" is clicked — expands the list in
   *  place instead of navigating away. */
  showAllTransactions = false;

  /** 5 by default; every one of them once expanded. */
  get displayedTransfers(): Transfer[] {
    return this.showAllTransactions ? this.recentTransfers : this.recentTransfers.slice(0, 5);
  }

  /** All-time total actually debited (principal + fees) — always in the sender's own currency,
   *  since every one of their own transfers shares the same sourceCurrency. Matches the sum of
   *  WITHDRAWAL movements on their deposit account. Only shown once the full list is expanded. */
  get totalSent(): number {
    return this.recentTransfers.reduce((sum, t) => sum + t.totalCharged, 0);
  }

  /** Full directory, filtered by the contact search — no manual national/international split. */
  get filteredRecipients(): Recipient[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return this.recipients;
    }
    return this.recipients.filter(
      (r) => r.name.toLowerCase().includes(term) || r.phoneNumber.toLowerCase().includes(term)
    );
  }

  get isInternational(): boolean {
    return this.selectedRecipient?.currencyCode !== this.sourceCurrency;
  }

  /** Number of decimal places the recipient's currency actually uses. */
  get receivedDecimals(): number {
    return decimalsForCurrency(this.selectedRecipient?.currencyCode);
  }

  private roundTo(value: number, decimals: number): number {
    const factor = Math.pow(10, decimals);
    return Math.round(value * factor) / factor;
  }

  decimalsForCurrency(code: string): number {
    return decimalsForCurrency(code);
  }

  statusLabel(status: string): string {
    return this.i18n.t('send.status.' + status);
  }

  selectRecipient(recipient: Recipient): void {
    this.selectedRecipient = recipient;
    this.searchTerm = '';
    this.showContactPicker = false;
    this.amount = 0;
    this.receivedAmount = 0;
    this.refreshQuote();
  }

  clearRecipient(): void {
    this.selectedRecipient = null;
    this.searchTerm = '';
    this.showContactPicker = false;
  }

  changeRecipient(): void {
    this.selectedRecipient = null;
    this.searchTerm = '';
    this.showContactPicker = true;
  }

  get total(): number {
    return Math.round((this.amount + this.fee) * 100) / 100;
  }

  get amountValid(): boolean {
    return (
      typeof this.amount === 'number' &&
      !isNaN(this.amount) &&
      this.amount > 0 &&
      this.amount >= this.minAmount &&
      this.amount <= this.maxAmount
    );
  }

  /** Clears the field when it's focused while still showing the default 0, so the person can
   *  start typing immediately instead of having to delete the 0 first. */
  onAmountSentFocus(): void {
    if (this.amount === 0) {
      this.amount = null as unknown as number;
    }
  }

  onAmountReceivedFocus(): void {
    if (this.receivedAmount === 0) {
      this.receivedAmount = null as unknown as number;
    }
  }

  get amountErrorMessage(): string {
    if (this.amount === null || this.amount === undefined || isNaN(this.amount)) {
      return this.i18n.t('send.amountRequired');
    }
    if (this.amount <= 0) {
      return this.i18n.t('send.amountPositive');
    }
    if (this.amount < this.minAmount) {
      return this.i18n.t('send.amountBelowMin').replace('{min}', `${this.minAmount} ${this.sourceCurrency}`);
    }
    if (this.amount > this.maxAmount) {
      return this.i18n.t('send.amountExceedsMax').replace('{max}', `${this.maxAmount} ${this.sourceCurrency}`);
    }
    return '';
  }

  /** Called when the person edits "You send" — recalculates what the recipient gets and the fee.
   *  Never touches `amount` itself here: that field is two-way bound and Angular already
   *  updated it before this handler runs. Rewriting it again mid-keystroke is what caused
   *  digits to scramble while typing. */
  onAmountSentChange(): void {
    this.receivedAmount = this.amountValid
      ? roundForCurrency(this.amount * this.rate, this.selectedRecipient?.currencyCode)
      : this.amount;
    this.scheduleFeeRefresh();
  }

  /** Called when the person edits "They receive" — back-calculates how much to send.
   *  Same rule: never reassign `receivedAmount` here, only derive `amount` from it. */
  onAmountReceivedChange(): void {
    const received = this.receivedAmount;
    if (received === null || received === undefined || isNaN(received) || this.rate <= 0) {
      this.amount = received;
      return;
    }
    this.amount = this.roundTo(received / this.rate, 2);
    this.scheduleFeeRefresh();
  }

  private feeRefreshTimer: ReturnType<typeof setTimeout> | null = null;

  /** Debounced — the fee tiers are denominated in CAD, so matching the right tier for a non-CAD
   *  amount (e.g. XOF) requires the backend's currency conversion. Waits briefly after the last
   *  keystroke instead of calling on every single one. */
  private scheduleFeeRefresh(): void {
    if (this.feeRefreshTimer) {
      clearTimeout(this.feeRefreshTimer);
    }
    this.feeRefreshTimer = setTimeout(() => {
      if (this.amountValid) {
        this.transferService.getQuote(this.sourceCurrency, this.selectedRecipient?.currencyCode ?? this.sourceCurrency, this.amount).subscribe({
          next: (quote) => (this.fee = quote.fee),
          error: () => {},
        });
      }
    }, 300);
  }

  refreshQuote(): void {
    const recipient = this.selectedRecipient;
    if (!recipient) {
      return;
    }
    this.loading = true;
    this.transferService.getQuote(this.sourceCurrency, recipient.currencyCode, this.amount || 0).subscribe({
      next: (quote) => {
        this.rate = quote.rate;
        this.fee = quote.fee;
        this.receivedAmount = roundForCurrency(this.amount * this.rate, recipient.currencyCode);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  /** Shows the recap screen — nothing is sent to the backend yet. */
  goToReview(): void {
    if (!this.selectedRecipient || !this.amountValid || !this.selectedAccount) {
      return;
    }
    this.errorMessage = '';
    this.reviewMode = true;
  }

  /** Goes back from the recap to editing the amount/recipient. */
  editTransfer(): void {
    this.reviewMode = false;
  }

  /** The recap's "Confirm" button — this is the actual send. */
  confirmAndSend(): void {
    const recipient = this.selectedRecipient;
    if (!recipient || !this.amountValid || !this.selectedAccount) {
      return;
    }
    this.sending = true;
    this.errorMessage = '';
    this.transferService
      .send({
        recipientId: recipient.id,
        sourceAccountId: this.selectedAccount.id,
        amount: this.amount,
        amountReceived: this.receivedAmount,
        rate: this.rate,
        fee: this.fee,
      })
      .subscribe({
        next: (transfer) => {
          this.sending = false;
          this.reviewMode = false;
          this.completedTransfer = transfer;
          this.loadAccounts(); // refresh balance if the deposit account was just debited
          this.transferService.getHistory().subscribe((transfers) => (this.recentTransfers = transfers));
        },
        error: (err) => {
          this.sending = false;
          this.errorMessage = err?.error?.error ?? this.i18n.t('send.errorGeneric');
        },
      });
  }

  goToTracking(): void {
    if (this.completedTransfer) {
      this.transferSent.emit(this.completedTransfer.id);
    }
  }

  sendAnother(): void {
    this.completedTransfer = null;
    this.reviewMode = false;
    this.amount = 0;
    this.receivedAmount = 0;
    this.clearRecipient();
  }
}
