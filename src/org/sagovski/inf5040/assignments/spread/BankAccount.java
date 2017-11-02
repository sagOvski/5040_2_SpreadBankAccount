package org.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;

public interface BankAccount {

	public BankCurrency getAccountBalance();

	public BankCurrency deposit(final BankCurrency depositAmount);

	public BankCurrency withdraw(final BankCurrency withdrawAmount);

	public BankCurrency exchange(final BankCurrency fromCurrencyAmount, final CurrencyCode toCurrencyCode);

	public BankCurrency addInterest(final BigDecimal interestPercent);

}
