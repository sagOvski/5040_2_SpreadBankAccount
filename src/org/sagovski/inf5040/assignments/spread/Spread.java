package org.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class Spread {

	public static void main(String args[]) throws UnknownHostException, SpreadException {

//		 final String groupName = "group";
//		 final SpreadGroup group = new SpreadGroup();
//		
//		 SpreadConnection conn1 = new SpreadConnection();
//		 conn1.connect(InetAddress.getByName("127.0.0.1"), 0, "conn1", false, true);
//		 group.join(conn1, groupName);
//		
//		 // SpreadConnection conn2 = new SpreadConnection();
//		 // conn2.connect(InetAddress.getByName("127.0.0.1"), 0, "conn2", false,true);
//		 // group.join(conn2, groupName);
//		 //
//		 // SpreadConnection conn3 = new SpreadConnection();
//		 // conn3.connect(InetAddress.getByName("127.0.0.1"), 0, "conn3", false,true);
//		 // group.join(conn3, groupName);
//		
//		 SpreadMessage msg = new SpreadMessage();
//		 msg.setSafe();
//		 msg.addGroup(group);
//		 msg.setData("test_world".getBytes());
//		 try {
//		 Thread.sleep(5000);
//		 conn1.multicast(msg);
//		 System.out.println("Message sent!!");
//		 Thread.sleep(5000);
//		 } catch (Exception e) {
//		 e.printStackTrace();
//		 }

//		CaptivatorsBankAccount account = new CaptivatorsBankAccount();
//		account.deposit(new BankCurrency(BigDecimal.TEN, CurrencyCode.NOK));
//		System.out.println(account.getAccountBalance().toString());
//		account.sleep(new Long(10));
	}
}
