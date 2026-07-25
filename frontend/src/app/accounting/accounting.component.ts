import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountingService, AccountingSummary, ReceptionModeBalance, LedgerEntry } from '../accounting.service';
import { I18nService } from '../i18n.service';

@Component({
  selector: 'app-accounting',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './accounting.component.html',
  styleUrl: './accounting.component.css',
})
export class AccountingComponent implements OnInit {
  loading = true;
  summary: AccountingSummary | null = null;
  balances: ReceptionModeBalance[] = [];
  ledger: LedgerEntry[] = [];

  settlingReceptionModeId: number | null = null;
  settleAmount = 0;
  settleNote = '';
  saving = false;
  errorMessage = '';

  constructor(private accountingService: AccountingService, public i18n: I18nService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.loading = true;
    this.accountingService.summary().subscribe((s) => (this.summary = s));
    this.accountingService.balances().subscribe((b) => (this.balances = b));
    this.accountingService.ledger().subscribe((l) => {
      this.ledger = l;
      this.loading = false;
    });
  }

  openSettle(balance: ReceptionModeBalance): void {
    this.settlingReceptionModeId = balance.receptionModeId;
    this.settleAmount = Math.max(balance.currentBalance, 0);
    this.settleNote = '';
    this.errorMessage = '';
  }

  cancelSettle(): void {
    this.settlingReceptionModeId = null;
  }

  confirmSettle(balance: ReceptionModeBalance): void {
    if (this.settleAmount <= 0) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.accountingService.recordSettlement(balance.receptionModeId, this.settleAmount, balance.currency, this.settleNote).subscribe({
      next: () => {
        this.saving = false;
        this.settlingReceptionModeId = null;
        this.loadAll();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.error ?? this.i18n.t('accounting.error');
      },
    });
  }
}
