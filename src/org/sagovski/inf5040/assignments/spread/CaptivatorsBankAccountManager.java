package org.sagovski.inf5040.assignments.spread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spread.BasicMessageListener;
import spread.MembershipInfo;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class CaptivatorsBankAccountManager implements BankAccountManager, BasicMessageListener {

	private static final Logger logger = LogManager.getLogger(CaptivatorsBankAccountManager.class);

	private final String HOST_NAME = "127.0.0.1";
	private final String GROUP_NAME = "Captivators";

	private final static File INPUT_FILE = new File(PathUtils.getAbsolutePath("input.txt"));
	private final String BANK_PASS_BOOK = PathUtils.getAbsolutePath("bank_pass_book.txt");

	private final static int INTENDED_REPLICA_COUNT = 2;

	private static int actualReplicaCount = 0;

	private String connectionId = "";

	private SpreadConnection spreadConnection;
	private SpreadGroup spreadGroup;

	private CaptivatorsBankAccount bankAccount;

	public CaptivatorsBankAccountManager() {
		bankAccount = new CaptivatorsBankAccount();

		spreadConnection = new SpreadConnection();
		spreadGroup = new SpreadGroup();
		Random random = new Random();
		connectionId = Integer.toString(random.nextInt(1000));

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
		if (manager.spreadConnection.isConnected()) {
			while (actualReplicaCount < INTENDED_REPLICA_COUNT) {
				manager.bankAccount.sleep(2);
				continue;
			}
		}
		if (INPUT_FILE.exists()) {
			List<String> ins = manager.getBankInstructionsFromFile(INPUT_FILE);
			for (String in : ins) {
				manager.executeInstruction(in, true);
			}
		} else {
			while (true)
				;
		}

	}

	public List<String> getBankInstructionsFromFile(final File commandsInputFile) {
		List<String> instructions = new ArrayList<String>();
		try (Scanner scanner = new Scanner(INPUT_FILE)) {
			while (scanner.hasNextLine()) {
				instructions.add(scanner.nextLine());
			}
		} catch (FileNotFoundException e) {
			logger.error(ExceptionUtils.getStrStackTrace(e));
			e.printStackTrace();
			System.exit(1);
		}
		return instructions;
	}

	@Override
	public void executeInstruction(final String strBankInstruction, boolean shouldPropagateInstruction) {
		final String tokens[] = strBankInstruction.split(" ");
		final String command = tokens[0];
		switch (command) {

		case "balance":
			String balEnquiryMsg = String.format("Current balance in the account is: %s\n",
					bankAccount.getAccountBalance().toString());
			logger.info(balEnquiryMsg);
			if (shouldPropagateInstruction)
				this.notifyReplicas(strBankInstruction);
			try {
				Files.write(Paths.get(BANK_PASS_BOOK), balEnquiryMsg.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				logger.error(ExceptionUtils.getStrStackTrace(e));
				e.printStackTrace();
				System.exit(1);
			}
			break;

		case "deposit":
			BankCurrency depositAmount = new BankCurrency(new BigDecimal(tokens[1]),
					this.bankAccount.getAccountBalance().getCurrencyCode());
			bankAccount.deposit(depositAmount);
			if (shouldPropagateInstruction)
				this.notifyReplicas(strBankInstruction);
			break;

		case "withdraw":
			BankCurrency withdrawalAmount = new BankCurrency(new BigDecimal(tokens[1]),
					this.bankAccount.getAccountBalance().getCurrencyCode());
			bankAccount.withdraw(withdrawalAmount);
			if (shouldPropagateInstruction)
				this.notifyReplicas(strBankInstruction);
			break;

		case "exchange":
			CurrencyCode fromCurCode = CurrencyCode.valueOf(tokens[1]);
			CurrencyCode toCurCode = CurrencyCode.valueOf(tokens[2]);
			bankAccount.exchange(fromCurCode, toCurCode);
			if (shouldPropagateInstruction)
				this.notifyReplicas(strBankInstruction);
			break;

		case "sleep":
			bankAccount.sleep(Integer.parseInt(tokens[1]));
			break;

		case "addInterest":
			BigDecimal interestPercent = new BigDecimal(tokens[1]);
			bankAccount.addInterest(interestPercent);
			if (shouldPropagateInstruction)
				this.notifyReplicas(strBankInstruction);
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
		notification.setSafe();
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
		logger.info("Notification received!! " + new String(notification.getData()));
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
		String msg = new String(notification.getData());
		if (!notification.getSender().toString().split("#")[1].trim().equals(connectionId)) {
			logger.info("Executing remote instruction..! " + msg);
			this.executeInstruction(msg, false);
		}
	}

	public void handleMembershipNotification(final SpreadMessage notification) {
		String msg = new String(notification.getData());
		logger.info("Member added " + msg);
		MembershipInfo info = notification.getMembershipInfo();
		actualReplicaCount = info.getMembers().length;
	}

}
