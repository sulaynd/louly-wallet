import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransferService } from '../transfer.service';
import { I18nService } from '../i18n.service';
import { Transfer, TransferEvent } from '../models';
import { decimalsForCurrency } from '../currency-utils';

type TrackView = 'list' | 'detail';

@Component({
  selector: 'app-track',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './track.component.html',
  styleUrl: './track.component.css',
})
export class TrackComponent implements OnInit, OnChanges {
  /** Optional: jump straight to a specific transfer's detail (e.g. right after sending one).
   *  When absent, the history list is shown first. */
  @Input() transferId: number | null = null;

  view: TrackView = 'list';

  history: Transfer[] = [];
  historyLoading = true;
  historyError = '';

  transfer: Transfer | null = null;
  detailLoading = false;
  detailError = '';

  constructor(private transferService: TransferService, public i18n: I18nService) {}

  decimalsForCurrency(code: string): number {
    return decimalsForCurrency(code);
  }

  /** All-time total sent, across the full history — always in the same currency since every
   *  one of the account's own transfers shares the same sourceCurrency. */
  get totalSent(): number {
    return this.history.reduce((sum, t) => sum + t.amountSent, 0);
  }

  get totalSentCurrency(): string {
    return this.history[0]?.sourceCurrency ?? 'CAD';
  }

  /** Translated title for a timeline step, built from its type — never the stored English text. */
  eventTitle(step: TransferEvent, transfer: Transfer): string {
    switch (step.type) {
      case 'PAYMENT_CONFIRMED':
        return this.i18n.t('track.event.paymentConfirmed.title');
      case 'CONVERTED':
        return this.i18n.t('track.event.converted.title', { currency: transfer.targetCurrency });
      case 'SENT':
        return this.i18n.t('track.event.sent.title', {
          detail: transfer.recipient.detail || transfer.recipient.name,
        });
      case 'DELIVERED':
        return this.i18n.t('track.event.delivered.title', { name: transfer.recipient.name });
      default:
        return step.title;
    }
  }

  eventSubtitle(step: TransferEvent, transfer: Transfer): string {
    switch (step.type) {
      case 'PAYMENT_CONFIRMED':
        return this.i18n.t('track.event.paymentConfirmed.subtitle');
      case 'CONVERTED':
        return this.i18n.t('track.event.converted.subtitle', { rate: transfer.exchangeRate });
      case 'SENT':
        return this.i18n.t('track.event.sent.subtitle');
      case 'DELIVERED':
        return this.i18n.t('track.event.delivered.subtitle');
      default:
        return step.subtitle;
    }
  }

  ngOnInit(): void {
    this.loadHistory();
    if (this.transferId) {
      this.openDetail(this.transferId);
    }
  }

  ngOnChanges(): void {
    if (this.transferId) {
      this.openDetail(this.transferId);
    }
  }

  private loadHistory(): void {
    this.historyLoading = true;
    this.historyError = '';
    this.transferService.getHistory().subscribe({
      next: (transfers) => {
        this.history = transfers;
        this.historyLoading = false;
      },
      error: () => {
        this.historyLoading = false;
        this.historyError = this.i18n.t('track.empty');
      },
    });
  }

  openDetail(id: number): void {
    this.view = 'detail';
    this.detailLoading = true;
    this.detailError = '';
    this.transferService.getTransfer(id).subscribe({
      next: (transfer) => {
        this.transfer = transfer;
        this.detailLoading = false;
      },
      error: () => {
        this.detailLoading = false;
        this.detailError = this.i18n.t('track.empty');
      },
    });
  }

  backToList(): void {
    this.view = 'list';
    this.transfer = null;
    this.loadHistory();
  }
}
