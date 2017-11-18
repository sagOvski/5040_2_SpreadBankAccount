package org.sagovski.inf5040.assignments.spread;

public interface BankAccountManager {

	public void executeInstruction(final String strBankInstruction);

	public void notifyReplicas(final String strBankInstruction);

}
