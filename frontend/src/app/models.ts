export type RecipientType = 'NATIONAL' | 'INTERNATIONAL';
export type TransferStatus = 'CONFIRMED' | 'CONVERTED' | 'SENT' | 'DELIVERED';

export interface Recipient {
  id: number;
  name: string;
  type: RecipientType;
  detail: string | null;
  flagEmoji: string;
  currencyCode: string;
  phoneNumber: string;
  receptionModeName: string;
  receptionModeId: number | null;
  deliveryPartner: string;
  address: string;
  city: string;
}

export interface Me {
  username: string;
  displayName: string;
  phoneNumber: string;
  country: string;
  flagEmoji: string;
}

export interface RateQuote {
  fromCurrency: string;
  toCurrency: string;
  rate: number;
  fee: number;
}

export type TransferEventType = 'PAYMENT_CONFIRMED' | 'CONVERTED' | 'SENT' | 'DELIVERED';

export interface TransferEvent {
  type: TransferEventType;
  title: string;
  subtitle: string;
  pending: boolean;
}

export type AccountType = 'DEPOT' | 'BANCAIRE';

export interface UserAccount {
  id: number;
  type: AccountType;
  currencyCode: string | null;
  balance: number | null;
  /** Display string — "Compte dépôt Louly Express" for DEPOT, "Visa •••• 1234" for BANCAIRE. */
  label: string;
  /** BANCAIRE only — null for DEPOT. */
  cardNetwork: string | null;
  cardLast4: string | null;
  cardExpiryMonth: string | null;
  cardExpiryYear: string | null;
}

export interface Transfer {
  id: number;
  recipient: Recipient;
  mode: RecipientType;
  amountSent: number;
  amountReceived: number;
  sourceCurrency: string;
  targetCurrency: string;
  exchangeRate: number;
  fee: number;
  totalCharged: number;
  status: TransferStatus;
  createdAt: string;
  sourceAccountType: AccountType | null;
  sourceAccountLabel: string | null;
  /** Set only when funded from a BANCAIRE account — the simulated bank's authorization reference. */
  bankAuthorizationReference: string | null;
  events: TransferEvent[];
}

export interface SendMoneyRequest {
  recipientId: number;
  sourceAccountId: number;
  amount: number;
  amountReceived: number;
  rate: number;
  fee: number;
}
