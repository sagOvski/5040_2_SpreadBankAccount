package org.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

public class CaptivatorsBankAccount implements BankAccount {

	private static final Logger logger = LogManager.getLogger(CaptivatorsBankAccount.class);

	private BankCurrency accountBalance;

	public CaptivatorsBankAccount() {
		accountBalance = new BankCurrency(BigDecimal.ZERO, CurrencyCode.NOK);
		logger.info("Client initialized with accountBalance: " + accountBalance.toString());
	}

	@Override
	public BankCurrency getAccountBalance() {
		return accountBalance;
	}

	private void setAccountBalance(final BankCurrency updatedAccountBalance) {
		this.accountBalance = updatedAccountBalance;
	}

	@Override
	public BankCurrency deposit(final BankCurrency depositAmount) {
		logger.info(String.format("Balance before depositing: %s", this.accountBalance.toString()));
		this.setAccountBalance(accountBalance.add(depositAmount));
		logger.info(
				String.format("Deposited '%s', new balance: %s", depositAmount.toString(), accountBalance.toString()));

		return accountBalance;
	}

	@Override
	public BankCurrency withdraw(final BankCurrency withdrawAmount) {
		logger.info(String.format("Balance before withdrawing: %s", this.accountBalance.toString()));
		this.setAccountBalance(accountBalance.remove(withdrawAmount));
		logger.info(
				String.format("Withdrew '%s', new balance: %s", withdrawAmount.toString(), accountBalance.toString()));

		return accountBalance;
	}

	@Override
	public BankCurrency exchange(final CurrencyCode fromCurrencyCode, final CurrencyCode toCurrencyCode) {
		if (toCurrencyCode == this.accountBalance.getCurrencyCode()) {
			String sameCurCodeErrorMsg = String.format(
					"*** exchange() operation should happen on different currencyCodes, but happening with same currencyCode '%s'!! *** \n"
							+ "*** Not proceeding with the exchange operation, please enter a new valid operation!! *** ",
					fromCurrencyCode.toString());
			logger.error(sameCurCodeErrorMsg);
			return this.getAccountBalance();
		} else if (fromCurrencyCode != this.accountBalance.getCurrencyCode()) {
			String diffFromCurCodeErrorMsg = String.format(
					"*** fromCurrencyCode '%s' is different from bank account's currencyCode '%s'!! *** \n"
							+ "*** Not proceeding with exchange operation, please enter currencyCode same as that of bankAccount!! *** ",
					fromCurrencyCode.toString(), this.accountBalance.getCurrencyCode().toString());
			logger.error(diffFromCurCodeErrorMsg);
			return this.getAccountBalance();
		}

		BigDecimal exchangeRate = CurrencyCode.getExchangeRate(fromCurrencyCode, toCurrencyCode);
		BankCurrency convertedBankCurrency = new BankCurrency(
				this.accountBalance.getCurrencyAmount().multiply(exchangeRate), toCurrencyCode);

		logger.info(String.format("Balance before exchange: %s", this.accountBalance.toString()));
		this.setAccountBalance(convertedBankCurrency);
		logger.info(String.format("Balance after successful exchange: %s", this.accountBalance.toString()));

		return convertedBankCurrency;
	}

	private BankCurrency getInterestAmount(final BankCurrency accountBalance, final BigDecimal interestPercent) {
		Assert.assertNotNull(accountBalance);

		BankCurrency interestAmount = new BankCurrency(
				accountBalance.getCurrencyAmount().multiply(interestPercent.divide(BigDecimal.valueOf(100))),
				accountBalance.getCurrencyCode());
		logger.info(String.format("Calculated interest is '%s' for amount '%s' with interest percent '%s'",
				interestAmount.toString(), accountBalance.toString(), interestPercent.toString()));
		return interestAmount;
	}

	@Override
	public BankCurrency addInterest(final BigDecimal interestPercent) {
		if (null == interestPercent) {
			logger.error(
					"*** interestPercent can't be null, not proceeding with addInterest() operation, please provide valid interestPercent!! *** ");
			return this.getAccountBalance();
		}

		BankCurrency interest = this.getInterestAmount(accountBalance, interestPercent);
		this.setAccountBalance(accountBalance.add(interest));
		logger.info(String.format("Updated balance after adding interest: '%s'", this.accountBalance.toString()));
		return accountBalance;
	}

	@Override
	public void sleep(int seconds) {
		try {
			logger.info(String.format("Sleeping for %d seconds!", seconds));
			Thread.sleep(seconds * 1000);
			logger.info("Awaken from sleep!");
		} catch (InterruptedException e) {
			logger.error(ExceptionUtils.getStrStackTrace(e));
			e.printStackTrace();
			System.exit(1);
		}
	}

}
