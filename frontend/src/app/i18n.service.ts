import { Injectable } from '@angular/core';

export type Lang = 'en' | 'fr';

type Dict = Record<string, Record<Lang, string>>;

const TRANSLATIONS: Dict = {
  'tab.send': { en: 'Send', fr: 'Envoyer' },
  'tab.recipients': { en: 'Recipients', fr: 'Bénéficiaires' },
  'tab.track': { en: 'Track', fr: 'Suivi' },
  'tab.profile': { en: 'Profile', fr: 'Profil' },

  'send.title': { en: 'Send money', fr: "Envoyer de l'argent" },
  'send.segment.national': { en: 'Within Canada', fr: 'Au Canada' },
  'send.segment.international': { en: 'International', fr: 'International' },
  'send.from': { en: 'From', fr: 'De' },
  'send.to': { en: 'To', fr: 'À' },
  'send.youSend': { en: 'You send', fr: 'Vous envoyez' },
  'send.transactionHistory': { en: 'Transactions', fr: 'Transaction' },
  'send.seeMore': { en: 'See more', fr: 'Voir +' },
  'send.viewAllTransactions': { en: 'View all transactions', fr: 'Voir toutes les transactions' },
  'send.totalSent': { en: 'Total sent', fr: 'Total envoyé' },
  'track.totalSent': { en: 'Total sent', fr: 'Total envoyé' },
  'send.status.CONFIRMED': { en: 'Confirmed', fr: 'Confirmé' },
  'send.status.CONVERTED': { en: 'Converted', fr: 'Converti' },
  'send.status.SENT': { en: 'Sent', fr: 'Transmis' },
  'send.status.DELIVERED': { en: 'Delivered', fr: 'Livré' },
  'send.payFrom': { en: 'Pay from', fr: 'Payer depuis' },
  'send.depotAccount': { en: 'Deposit account', fr: 'Compte dépôt' },
  'send.addCard': { en: 'Add a card', fr: 'Ajouter une carte' },
  'send.cardHolderName': { en: 'Cardholder name', fr: 'Nom du titulaire' },
  'send.cardNumber': { en: 'Card number', fr: 'Numéro de carte' },
  'send.expiryMonth': { en: 'MM', fr: 'MM' },
  'send.expiryYear': { en: 'YYYY', fr: 'AAAA' },
  'send.expiryDate': { en: 'Expiration date', fr: "Date d'expiration" },
  'send.cvc': { en: 'CVC', fr: 'CVC' },
  'send.save': { en: 'Save', fr: 'Enregistrer' },
  'send.theyReceive': { en: 'They receive', fr: 'Ils reçoivent' },
  'send.fee': { en: 'Transfer fee', fr: 'Frais de transfert' },
  'send.deliverySpeed': { en: 'Delivery speed', fr: 'Délai de livraison' },
  'send.instant': { en: 'Instant', fr: 'Instantané' },
  'send.minutes': { en: 'Minutes', fr: 'Quelques minutes' },
  'send.total': { en: 'Total to pay', fr: 'Total à payer' },
  'send.continue': { en: 'Review transfer', fr: 'Vérifier le transfert' },
  'send.review.title': { en: 'Review transfer', fr: 'Récapitulatif du transfert' },
  'send.review.subtitle': { en: 'Nothing has been sent yet — check the details below.', fr: "Rien n'a encore été envoyé — vérifiez les détails ci-dessous." },
  'send.review.edit': { en: 'Edit', fr: 'Modifier' },
  'send.review.confirm': { en: 'Confirm and send', fr: "Confirmer et envoyer" },
  'send.review.confirming': { en: 'Sending…', fr: 'Envoi en cours…' },
  'send.sending': { en: 'Sending…', fr: 'Envoi en cours…' },
  'send.loadingRecipients': { en: 'Loading recipients from the backend…', fr: 'Chargement des bénéficiaires…' },
  'send.errorGeneric': {
    en: "Couldn't send transfer. Check the backend is running.",
    fr: "Échec de l'envoi. Vérifiez que le serveur backend fonctionne.",
  },
  'send.amountRequired': { en: 'Enter an amount.', fr: 'Saisissez un montant.' },
  'send.amountPositive': { en: 'Amount must be greater than 0.', fr: 'Le montant doit être supérieur à 0.' },
  'send.amountExceedsMax': { en: 'Amount cannot exceed {max} per transfer.', fr: 'Le montant ne peut pas dépasser {max} par transfert.' },
  'send.amountBelowMin': { en: 'Amount must be at least {min} per transfer.', fr: 'Le montant doit être d\'au moins {min} par transfert.' },
  'send.searchPlaceholder': { en: 'Search contacts by name or phone number', fr: 'Rechercher un contact par nom ou numéro' },
  'send.noContactsFound': { en: 'No contacts found.', fr: 'Aucun contact trouvé.' },
  'send.change': { en: 'Change', fr: 'Changer' },
  'send.beneficiaryName': { en: 'Beneficiary name', fr: 'Nom bénéficiaire' },
  'send.addBeneficiary': { en: 'Add a new beneficiary', fr: 'Ajouter un nouveau bénéficiaire' },

  'send.confirm.title': { en: 'Transfer complete', fr: 'Transfert effectué' },
  'send.confirm.subtitle': { en: 'Your money is on its way.', fr: 'Votre argent est en route.' },
  'send.confirm.sentLabel': { en: 'You sent', fr: 'Vous avez envoyé' },
  'send.confirm.receivedLabel': { en: 'They will receive', fr: 'Le bénéficiaire recevra' },
  'send.confirm.reference': { en: 'Reference', fr: 'Référence' },
  'send.confirm.track': { en: 'Track this transfer', fr: 'Suivre ce transfert' },
  'send.confirm.sendAnother': { en: 'Send another transfer', fr: 'Envoyer un autre transfert' },

  'recipients.title': { en: 'Recipients', fr: 'Bénéficiaires' },
  'recipients.national': { en: 'National', fr: 'National' },
  'recipients.international': { en: 'International', fr: 'International' },
  'recipients.add': { en: 'Add recipient', fr: 'Ajouter un bénéficiaire' },
  'recipients.loading': { en: 'Loading recipients…', fr: 'Chargement des bénéficiaires…' },
  'recipients.interac': { en: 'Interac', fr: 'Interac' },
  'recipients.emptyNational': { en: 'No national recipients yet.', fr: 'Aucun bénéficiaire national pour le moment.' },
  'recipients.emptyInternational': { en: 'No international recipients yet.', fr: 'Aucun bénéficiaire international pour le moment.' },
  'recipients.form.title': { en: 'New recipient', fr: 'Nouveau bénéficiaire' },
  'recipients.form.name': { en: 'Full name', fr: 'Nom complet' },
  'recipients.form.detail': { en: 'Bank / account detail (optional)', fr: 'Détail bancaire (facultatif)' },
  'recipients.form.detailPlaceholder': { en: 'e.g. RBC •••• 4471', fr: 'ex. RBC •••• 4471' },
  'recipients.form.selectReceptionMode': { en: 'Select a reception mode', fr: 'Sélectionner un mode de réception' },
  'recipients.form.loadingReceptionModes': { en: 'Loading reception modes…', fr: 'Chargement des modes de réception…' },
  'recipients.form.noReceptionModes': { en: 'No reception modes available for this country yet.', fr: 'Aucun mode de réception disponible pour ce pays pour le moment.' },
  'recipients.form.deliveryPartner': { en: 'Delivery partner', fr: 'Partenaire de livraison' },
  'recipients.form.address': { en: 'Address', fr: 'Adresse' },
  'recipients.form.city': { en: 'City', fr: 'Ville' },
  'recipients.form.phoneHint': {
    en: "Include the country calling code (+1, +33, +63, +91, +221) — we'll detect the country automatically.",
    fr: 'Incluez l\u2019indicatif du pays (+1, +33, +63, +91, +221) — le pays est détecté automatiquement.',
  },
  'recipients.form.save': { en: 'Save recipient', fr: 'Enregistrer le bénéficiaire' },
  'recipients.form.saving': { en: 'Saving…', fr: 'Enregistrement…' },
  'recipients.form.cancel': { en: 'Cancel', fr: 'Annuler' },
  'recipients.form.error': { en: "Couldn't save the recipient. Check the backend is running.", fr: "Échec de l'enregistrement. Vérifiez que le serveur backend fonctionne." },

  'track.title': { en: 'Track transfer', fr: 'Suivre le transfert' },
  'track.loading': { en: 'Loading transfer…', fr: 'Chargement du transfert…' },
  'track.empty': {
    en: 'No transfers yet — send money first to see it tracked here.',
    fr: "Aucun transfert pour l'instant — envoyez de l'argent pour le voir ici.",
  },
  'track.to': { en: 'to', fr: 'à' },
  'track.eta.international': { en: 'Arriving in ~4 minutes', fr: 'Arrivée dans ~4 minutes' },
  'track.eta.national': { en: 'Delivered instantly', fr: 'Livré instantanément' },
  'track.back': { en: 'Back to history', fr: "Retour à l'historique" },
  'track.status.done': { en: 'Delivered', fr: 'Livré' },
  'track.status.pending': { en: 'Pending', fr: 'En cours' },

  'track.event.paymentConfirmed.title': { en: 'Payment confirmed', fr: 'Paiement confirmé' },
  'track.event.paymentConfirmed.subtitle': { en: 'Just now', fr: "À l'instant" },
  'track.event.converted.title': { en: 'Converted to {currency}', fr: 'Converti en {currency}' },
  'track.event.converted.subtitle': { en: 'Rate locked at {rate}', fr: 'Taux verrouillé à {rate}' },
  'track.event.sent.title': { en: 'Sent to {detail}', fr: 'Envoyé à {detail}' },
  'track.event.sent.subtitle': { en: 'In progress', fr: 'En cours' },
  'track.event.delivered.title': { en: 'Delivered to {name}', fr: 'Livré à {name}' },
  'track.event.delivered.subtitle': { en: 'Pending', fr: 'En attente' },

  'auth.login': { en: 'Log in', fr: 'Se connecter' },
  'auth.register': { en: 'Create account', fr: 'Créer un compte' },
  'auth.username': { en: 'Username', fr: "Nom d'utilisateur" },
  'auth.password': { en: 'Password', fr: 'Mot de passe' },
  'auth.displayName': { en: 'Display name', fr: 'Nom affiché' },
  'auth.country': { en: 'Country', fr: 'Pays' },
  'auth.phoneNumber': { en: 'Phone number', fr: 'Numéro de téléphone' },
  'auth.errorPhoneRequired': { en: 'Enter your phone number.', fr: 'Saisissez votre numéro de téléphone.' },
  'auth.errorRequired': { en: 'Enter a username and password.', fr: 'Saisissez un nom d\u2019utilisateur et un mot de passe.' },
  'auth.errorLogin': { en: 'Incorrect username or password.', fr: 'Nom d\u2019utilisateur ou mot de passe incorrect.' },
  'auth.errorRegister': { en: "Couldn't create the account. Check the backend is running.", fr: "Échec de la création du compte. Vérifiez que le serveur backend fonctionne." },
  'auth.errorTaken': { en: 'That username is already taken.', fr: 'Ce nom d\u2019utilisateur est déjà pris.' },
  'auth.registerSuccess': { en: 'Account created — you can log in now.', fr: 'Compte créé — vous pouvez vous connecter.' },
  'auth.demoHint': { en: 'Demo account: demo / password', fr: 'Compte démo\u00a0: demo / password' },
  'auth.logout': { en: 'Log out', fr: 'Se déconnecter' },

  'tab.accounting': { en: 'Accounting', fr: 'Comptabilité' },
  'accounting.title': { en: 'Accounting', fr: 'Comptabilité' },
  'accounting.loading': { en: 'Loading…', fr: 'Chargement…' },
  'accounting.transactions': { en: 'Transactions', fr: 'Transactions' },
  'accounting.revenue': { en: 'Platform revenue', fr: 'Revenu plateforme' },
  'accounting.commissionExpense': { en: 'ReceptionMode commission expense:', fr: 'Commissions versées aux partenaires :' },
  'accounting.balances': { en: 'ReceptionMode balances', fr: 'Soldes par partenaire' },
  'accounting.noBalances': { en: 'No outstanding balances.', fr: 'Aucun solde en cours.' },
  'accounting.settle': { en: 'Settle', fr: 'Régler' },
  'accounting.settling': { en: 'Recording…', fr: 'Enregistrement…' },
  'accounting.confirmSettle': { en: 'Record payment', fr: 'Enregistrer le paiement' },
  'accounting.notePlaceholder': { en: 'Note (optional)', fr: 'Note (facultatif)' },
  'accounting.ledger': { en: 'Recent ledger entries', fr: 'Dernières écritures' },
  'accounting.noLedger': { en: 'No ledger entries yet.', fr: 'Aucune écriture pour le moment.' },
  'accounting.error': { en: "Couldn't record the settlement.", fr: "Échec de l'enregistrement du règlement." },
};

@Injectable({ providedIn: 'root' })
export class I18nService {
  lang: Lang = 'en';

  toggle(): void {
    this.lang = this.lang === 'en' ? 'fr' : 'en';
  }

  t(key: string, params?: Record<string, string | number>): string {
    let value = TRANSLATIONS[key]?.[this.lang] ?? key;
    if (params) {
      for (const [name, val] of Object.entries(params)) {
        value = value.replace(`{${name}}`, String(val));
      }
    }
    return value;
  }
}
