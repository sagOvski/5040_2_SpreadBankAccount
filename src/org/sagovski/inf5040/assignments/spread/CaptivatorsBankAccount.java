package org.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;

public class CaptivatorsBankAccount implements BankAccount {

	private BankCurrency accountBalance;

	@Override
	public BankCurrency getAccountBalance() {
		return accountBalance;
	}

	@Override
	public BankCurrency deposit(final BankCurrency depositAmount) {
		accountBalance = accountBalance.add(depositAmount);
		return accountBalance;
	}

	@Override
	public BankCurrency withdraw(final BankCurrency withdrawAmount) {
		accountBalance = accountBalance.remove(withdrawAmount);
		return accountBalance;
	}

	@Override
	public BankCurrency exchange(final BankCurrency fromBankCurrency, final CurrencyCode toCurrencyCode) {
		BigDecimal exchangeRate = CurrencyCode.getExchangeRate(fromBankCurrency.getCurrencyCode(), toCurrencyCode);
		BankCurrency convertedBankCurrency = new BankCurrency(
				fromBankCurrency.getCurrencyAmount().multiply(exchangeRate), toCurrencyCode);
		return convertedBankCurrency;
	}

	@Override
	public BankCurrency addInterest(BigDecimal interestPercent) {
		// TODO Auto-generated method stub
		return null;
	}

}
