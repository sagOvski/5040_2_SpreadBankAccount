package org.sagovski.inf5040.assignments.spread;

import java.io.File;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spread.BasicMessageListener;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class CaptivatorsBankAccountManager implements BankAccountManager, BasicMessageListener {

	private static final Logger logger = LogManager.getLogger(CaptivatorsBankAccountManager.class);

	private final String HOST_NAME = "127.0.0.1";
	private final String GROUP_NAME = "Captivators";

	private SpreadConnection spreadConnection;
	private SpreadGroup spreadGroup;

	private CaptivatorsBankAccount bankAccount;

	public CaptivatorsBankAccountManager() {
		bankAccount = new CaptivatorsBankAccount();

		spreadConnection = new SpreadConnection();
		spreadGroup = new SpreadGroup();
		Random random = new Random();
		String connectionId = GROUP_NAME + random.nextInt(1000);

		try {
			spreadConnection.connect(InetAddress.getByName(HOST_NAME), 0, connectionId, false, true);
			spreadGroup.join(spreadConnection, GROUP_NAME);
			spreadConnection.add(this);
		} catch (UnknownHostException | SpreadException e) {
			logger.error(ExceptionUtils.getStrStackTrace(e));
			e.printStackTrace();
			System.exit(1);
		}
		logger.info(String.format("Client connected with server '%s' and joined group '%s' with connectionId '%s'",
				HOST_NAME, GROUP_NAME, connectionId));
	}

	public static void main(String args[]) {
		CaptivatorsBankAccountManager manager = new CaptivatorsBankAccountManager();
		List<String> ins = manager.getBankCommandsFromFile(new File(""));
		for (String in : ins) {
			manager.executeInstruction(in);
		}
	}

	public List<String> getBankCommandsFromFile(final File commandsInputFile) {
		List<String> commands = new ArrayList<String>();
		commands.add("deposit 100");
		commands.add("withdraw 20");
		commands.add("addinterest 10");
		return commands;
	}

	@Override
	public void executeInstruction(final String strBankInstruction) {
		final String tokens[] = strBankInstruction.split(" ");
		final String command = tokens[0];
		switch (command) {

		case "balance":
			String balEnquiryMsg = String.format("Current balance in the account is: %s",
					bankAccount.getAccountBalance().toString());
			logger.info(balEnquiryMsg);
			break;

		case "deposit":
			BankCurrency depositAmount = new BankCurrency(new BigDecimal(tokens[1]),
					this.bankAccount.getAccountBalance().getCurrencyCode());
			bankAccount.deposit(depositAmount);
			break;

		case "withdraw":
			BankCurrency withdrawalAmount = new BankCurrency(new BigDecimal(tokens[1]),
					this.bankAccount.getAccountBalance().getCurrencyCode());
			bankAccount.withdraw(withdrawalAmount);
			break;

		case "exchange":
			CurrencyCode fromCurCode = CurrencyCode.valueOf(tokens[1]);
			CurrencyCode toCurCode = CurrencyCode.valueOf(tokens[2]);
			bankAccount.exchange(fromCurCode, toCurCode);
			break;

		case "sleep":
			bankAccount.sleep(Integer.parseInt(tokens[1]));
			break;

		case "addinterest":
			BigDecimal interestPercent = new BigDecimal(tokens[1]);
			bankAccount.addInterest(interestPercent);
			break;

		case "memberinfo":
			break;

		default:
			final String exceptionMsg = String.format("Unknown operation: '%s'! Exiting!", strBankInstruction);
			throw new RuntimeException(exceptionMsg);
		}
	}

	@Override
	public void notifyReplicas(final String bankCommand) {
		SpreadMessage notification = new SpreadMessage();
		notification.addGroup(spreadGroup);
		notification.setData(bankCommand.getBytes());
		try {
			spreadConnection.multicast(notification);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStrStackTrace(e));
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void messageReceived(final SpreadMessage notification) {
		if (notification.isRegular()) {
			this.handleRegularNotification(notification);
		} else if (notification.isMembership()) {
			this.handleMembershipNotification(notification);
		} else {
			final String excpMsg = String.format("Notification is neither isRegular() nor isMembership() : %s",
					notification.toString());
			throw new RuntimeException(excpMsg);
		}
	}

	public void handleRegularNotification(final SpreadMessage notification) {

	}

	public void handleMembershipNotification(final SpreadMessage notification) {

	}

}
