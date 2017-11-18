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

	private String BANK_PASS_BOOK_PATH = PathUtils.getAbsolutePath("bank_pass_book_");
	private File bankPassBook;

	private static int actualReplicaCount = 0;

	private String connectionId = "";

	private SpreadConnection spreadConnection;
	private SpreadGroup spreadGroup;

	private CaptivatorsBankAccount bankAccount;

	private SpreadGroup[] groupInfo;

	public CaptivatorsBankAccountManager(final String hostName, final String groupName) {
		bankAccount = new CaptivatorsBankAccount();

		spreadConnection = new SpreadConnection();
		spreadGroup = new SpreadGroup();
		Random random = new Random();
		connectionId = Integer.toString(random.nextInt(1000));

		try {
			spreadConnection.connect(InetAddress.getByName(hostName), 0, connectionId, false, true);
			spreadGroup.join(spreadConnection, groupName);
			spreadConnection.add(this);
		} catch (UnknownHostException | SpreadException e) {
			logger.error(ExceptionUtils.getStrStackTrace(e));
			e.printStackTrace();
			System.exit(1);
		}
		logger.info(String.format("Client connected with server '%s' and joined group '%s' with connectionId '%s'",
				hostName, groupName, connectionId));

		// Create a new bank pass book with name appended with connectionId
		try {
			BANK_PASS_BOOK_PATH = BANK_PASS_BOOK_PATH + connectionId;
			bankPassBook = new File(BANK_PASS_BOOK_PATH);
			bankPassBook.createNewFile();
			logger.info("Created a new bank pass book: " + bankPassBook.getAbsolutePath());
		} catch (IOException e) {
			logger.error(ExceptionUtils.getStrStackTrace(e));
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String args[]) {
		final String hostName = args[0];
		final String groupName = args[1];
		int intendedReplicaCount = Integer.parseInt(args[2]);

		File inputFile = args.length > 3 ? new File(args[3]) : new File("");
		CaptivatorsBankAccountManager manager = new CaptivatorsBankAccountManager(hostName, groupName);
		if (manager.spreadConnection.isConnected()) {
			while (actualReplicaCount < intendedReplicaCount) {
				logger.info(String.format("Expected replicaCount is '%d', but only '%d' joined", intendedReplicaCount,
						actualReplicaCount));
				manager.bankAccount.sleep(2);
				continue;
			}
		}

		if (inputFile.exists()) {
			List<String> instructions = manager.getBankInstructionsFromFile(inputFile);
			for (String insruction : instructions) {
				manager.notifyReplicas(insruction);
			}
		} else {
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					String strInstruction = scanner.nextLine().trim();
					manager.notifyReplicas(strInstruction);
				}
			}
		}
	}

	public List<String> getBankInstructionsFromFile(final File commandsInputFile) {
		List<String> instructions = new ArrayList<String>();
		try (Scanner scanner = new Scanner(commandsInputFile)) {
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
	public void executeInstruction(final String strBankInstruction) {
		final String tokens[] = strBankInstruction.split(" ");
		final String command = tokens[0];
		switch (command) {

		case "balance":
			String balEnquiryMsg = String.format("Current balance in the account is: %s\n",
					bankAccount.getAccountBalance().toString());
			logger.info(balEnquiryMsg);
			try {
				Files.write(Paths.get(bankPassBook.getAbsolutePath()), balEnquiryMsg.getBytes(),
						StandardOpenOption.APPEND);
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

		case "addInterest":
			BigDecimal interestPercent = new BigDecimal(tokens[1]);
			bankAccount.addInterest(interestPercent);
			break;

		case "memberinfo":
			logger.info("Printing groupInfo!");
			for (SpreadGroup host : groupInfo) {
				logger.info(host.toString());
			}
			break;

		case "exit":
			logger.info("Exiting!");
			System.exit(0);

		default:
			final String invalidOpMsg = String.format("Unknown operation: '%s'! Please enter a valid operation!",
					strBankInstruction);
			logger.error(invalidOpMsg);
		}
	}

	@Override
	public void notifyReplicas(final String bankInstruction) {
		SpreadMessage notification = new SpreadMessage();
		notification.addGroup(spreadGroup);
		notification.setData(bankInstruction.getBytes());
		notification.setAgreed();
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
		if (notification.isSafe() || notification.isReliable()) {
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
		logger.info("Executing remote instruction..! " + msg);
		this.executeInstruction(msg);
	}

	public void handleMembershipNotification(final SpreadMessage notification) {
		String msg = new String(notification.getData());
		logger.info("Member added " + msg);
		MembershipInfo info = notification.getMembershipInfo();
		actualReplicaCount = info.getMembers().length;
		groupInfo = info.getMembers();
	}

}
