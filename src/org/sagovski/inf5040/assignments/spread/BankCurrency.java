package org.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;

import org.junit.Assert;

public final class BankCurrency {

	private BigDecimal currencyAmount;
	private CurrencyCode currencyCode;

	public BankCurrency(BigDecimal currencyAmount, CurrencyCode currencyCode) {
		this.currencyAmount = currencyAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		this.currencyCode = currencyCode;
	}

	private void setCurrencyAmount(final BigDecimal currencyAmount) {
		this.currencyAmount = currencyAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getCurrencyAmount() {
		return currencyAmount;
	}

	public CurrencyCode getCurrencyCode() {
		return currencyCode;
	}

	public BankCurrency add(final BankCurrency addAmount) {
		final String errorMsg = "";
		Assert.assertTrue(errorMsg, addAmount.currencyCode == this.currencyCode);
		this.setCurrencyAmount(currencyAmount.add(addAmount.getCurrencyAmount()));
		return this;
	}

	public BankCurrency remove(final BankCurrency removeAmount) {
		final String errorMsg = "";
		Assert.assertTrue(errorMsg, removeAmount.currencyCode == this.currencyCode);
		Assert.assertTrue(this.getCurrencyAmount().compareTo(removeAmount.getCurrencyAmount()) >= 0);
		this.setCurrencyAmount(currencyAmount.subtract(removeAmount.getCurrencyAmount()));
		return this;
	}

	public static BankCurrency toObject(final String strBankCurrency) {
		String[] tokens = strBankCurrency.split(" ");
		return new BankCurrency(new BigDecimal(tokens[0]), CurrencyCode.valueOf(tokens[1]));
	}

	@Override
	public String toString() {
		return String.format("%s %s", currencyAmount.toString(), currencyCode.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currencyAmount == null) ? 0 : currencyAmount.hashCode());
		result = prime * result + ((currencyCode == null) ? 0 : currencyCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BankCurrency other = (BankCurrency) obj;
		if (currencyAmount == null) {
			if (other.currencyAmount != null)
				return false;
		} else if (!currencyAmount.equals(other.currencyAmount))
			return false;
		if (currencyCode != other.currencyCode)
			return false;
		return true;
	}

}
