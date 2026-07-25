import { SendComponent } from './send.component';
import { RecipientService } from '../recipient.service';
import { TransferService } from '../transfer.service';
import { CountryService } from '../country.service';
import { AccountService } from '../account.service';
import { AuthService } from '../auth.service';
import { I18nService } from '../i18n.service';

/**
 * These are pure-logic tests on the component's validation getters — no TestBed, no template
 * rendering, no HTTP. The constructor has no side effects (all API calls happen in ngOnInit,
 * which these tests never call), so plain instantiation with empty service stand-ins is enough.
 */
describe('SendComponent validation logic', () => {
  let component: SendComponent;

  beforeEach(() => {
    component = new SendComponent(
      {} as RecipientService,
      {} as TransferService,
      {} as CountryService,
      {} as AccountService,
      {} as AuthService,
      new I18nService()
    );
  });

  describe('cardFormValid', () => {
    function fillValidCard(): void {
      component.newCardHolderName = 'Test User';
      component.newCardNumber = '4111 1111 1111 1111';
      component.newCardExpiryMonth = '09';
      component.newCardExpiryYear = '2028';
      component.newCardCvc = '123';
      component.newCardCountryId = 1;
    }

    it('is true when every field is correctly filled', () => {
      fillValidCard();
      expect(component.cardFormValid).toBeTrue();
    });

    it('strips spaces from the card number before validating length', () => {
      // The exact scenario confirmed manually: typed with auto-inserted spaces every 4 digits.
      fillValidCard();
      component.newCardNumber = '4111 1111 1111 1111';
      expect(component.cardNumberDigitsOnly).toBe('4111111111111111');
      expect(component.cardFormValid).toBeTrue();
    });

    it('is false when the card number has fewer than 13 digits', () => {
      fillValidCard();
      component.newCardNumber = '4111 1111';
      expect(component.cardFormValid).toBeFalse();
    });

    it('is false when the cardholder name is blank', () => {
      fillValidCard();
      component.newCardHolderName = '   ';
      expect(component.cardFormValid).toBeFalse();
    });

    it('is false for an invalid expiry month (must be 01-12)', () => {
      fillValidCard();
      component.newCardExpiryMonth = '13';
      expect(component.cardFormValid).toBeFalse();
    });

    it('is false for a single-digit expiry month (must be zero-padded)', () => {
      fillValidCard();
      component.newCardExpiryMonth = '9';
      expect(component.cardFormValid).toBeFalse();
    });

    it('is false for a 2-digit expiry year (must be 4 digits)', () => {
      fillValidCard();
      component.newCardExpiryYear = '28';
      expect(component.cardFormValid).toBeFalse();
    });

    it('is false when the CVC is missing', () => {
      fillValidCard();
      component.newCardCvc = '';
      expect(component.cardFormValid).toBeFalse();
    });

    it('accepts both 3-digit and 4-digit CVCs (Amex)', () => {
      fillValidCard();
      component.newCardCvc = '1234';
      expect(component.cardFormValid).toBeTrue();
    });

    it('is false when no country is selected', () => {
      fillValidCard();
      component.newCardCountryId = null;
      expect(component.cardFormValid).toBeFalse();
    });
  });

  describe('onCardNumberInput', () => {
    it('auto-inserts a space every 4 digits as the person types', () => {
      component.newCardNumber = '4111111111111111';
      component.onCardNumberInput();
      expect(component.newCardNumber).toBe('4111 1111 1111 1111');
    });

    it('strips any non-digit characters typed', () => {
      component.newCardNumber = '4111-1111-1111';
      component.onCardNumberInput();
      expect(component.newCardNumber).toBe('4111 1111 1111');
    });
  });

  describe('amountValid', () => {
    beforeEach(() => {
      component.minAmount = 1;
      component.maxAmount = 5000;
    });

    it('is true for an amount within bounds', () => {
      component.amount = 100;
      expect(component.amountValid).toBeTrue();
    });

    it('is false when the amount is below the minimum', () => {
      component.minAmount = 500;
      component.amount = 100;
      expect(component.amountValid).toBeFalse();
    });

    it('is false when the amount exceeds the maximum', () => {
      component.maxAmount = 500;
      component.amount = 501;
      expect(component.amountValid).toBeFalse();
    });

    it('is false for zero or negative amounts', () => {
      component.amount = 0;
      expect(component.amountValid).toBeFalse();
      component.amount = -50;
      expect(component.amountValid).toBeFalse();
    });

    it('is false for NaN', () => {
      component.amount = NaN;
      expect(component.amountValid).toBeFalse();
    });
  });
});
