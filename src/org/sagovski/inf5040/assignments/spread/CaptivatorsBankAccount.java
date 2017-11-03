package org.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.junit.Assert;

import spread.BasicMessageListener;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class CaptivatorsBankAccount implements BankAccount, BasicMessageListener {

	private final String HOST_NAME = "localhost";
	private final String GROUP_NAME = "Captivators";

	private BankCurrency accountBalance;
	private SpreadConnection spreadConnection;
	private SpreadGroup spreadGroup;

	public CaptivatorsBankAccount() {
		Random random = new Random();
		int randomInt = random.nextInt(111);
		spreadConnection = new SpreadConnection();
		spreadGroup = new SpreadGroup();

		try {
			spreadConnection.connect(InetAddress.getByName(HOST_NAME), 0, GROUP_NAME + randomInt, false, true);
			spreadGroup.join(spreadConnection, GROUP_NAME);
			spreadConnection.add(this);
		} catch (UnknownHostException | SpreadException e) {
			e.printStackTrace();
		}
	}

	@Override
	public BankCurrency getAccountBalance() {
		return accountBalance;
	}

	private void setAccountBalance(final BankCurrency updatedAccountBalance) {
		this.accountBalance = updatedAccountBalance;
		this.notifyReplicas();
	}

	@Override
	public BankCurrency deposit(final BankCurrency depositAmount) {
		this.setAccountBalance(accountBalance.add(depositAmount));
		return accountBalance;
	}

	@Override
	public BankCurrency withdraw(final BankCurrency withdrawAmount) {
		this.setAccountBalance(accountBalance.remove(withdrawAmount));
		return accountBalance;
	}

	@Override
	public BankCurrency exchange(final CurrencyCode fromCurrencyCode, final CurrencyCode toCurrencyCode) {
		String errorMsg = "";
		Assert.assertTrue(errorMsg, fromCurrencyCode == this.accountBalance.getCurrencyCode());

		BigDecimal exchangeRate = CurrencyCode.getExchangeRate(fromCurrencyCode, toCurrencyCode);
		BankCurrency convertedBankCurrency = new BankCurrency(
				this.accountBalance.getCurrencyAmount().multiply(exchangeRate), toCurrencyCode);

		this.setAccountBalance(convertedBankCurrency);

		return convertedBankCurrency;
	}

	private BankCurrency getInterestAmount(final BankCurrency accountBalance, final BigDecimal interestPercent) {
		return null;
	}

	@Override
	public BankCurrency addInterest(BigDecimal interestPercent) {
		BankCurrency interest = this.getInterestAmount(accountBalance, interestPercent);
		this.setAccountBalance(accountBalance.add(interest));
		return accountBalance;
	}

	@Override
	public void notifyReplicas() {
		SpreadMessage notification = new SpreadMessage();
		notification.addGroup(spreadGroup);
		notification.setData(accountBalance.toString().getBytes());
		try {
			spreadConnection.multicast(notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sleep(Long seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void messageReceived(final SpreadMessage notification) {
		if (notification.isRegular()) {
			final String message = new String(notification.getData());
			this.setAccountBalance(BankCurrency.toObject(message));
		}
	}

}
