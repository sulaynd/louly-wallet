import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SendComponent } from './send/send.component';
import { RecipientsComponent } from './recipients/recipients.component';
import { TrackComponent } from './track/track.component';
import { AuthComponent } from './auth/auth.component';
import { AuthService } from './auth.service';
import { I18nService } from './i18n.service';

type TabView = 'send' | 'recipients' | 'track';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, SendComponent, RecipientsComponent, TrackComponent, AuthComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  activeView: TabView = 'send';
  lastTransferId: number | null = null;
  openRecipientFormOnNav = false;

  tabs: { view: TabView; labelKey: string; icon: string }[] = [
    { view: 'send', labelKey: 'tab.send', icon: 'ti-send' },
    { view: 'recipients', labelKey: 'tab.recipients', icon: 'ti-users' },
    { view: 'track', labelKey: 'tab.track', icon: 'ti-map-pin' },
  ];

  constructor(public i18n: I18nService, public auth: AuthService) {}

  setView(view: TabView): void {
    this.openRecipientFormOnNav = false;
    this.activeView = view;
  }

  onTransferSent(transferId: number): void {
    this.lastTransferId = transferId;
    this.activeView = 'track';
  }

  goToAddBeneficiary(): void {
    this.openRecipientFormOnNav = true;
    this.activeView = 'recipients';
  }

  logout(): void {
    this.auth.logout();
    this.activeView = 'send';
    this.lastTransferId = null;
  }
}
