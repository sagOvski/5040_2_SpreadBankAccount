package tst.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;

import org.junit.Test;
import org.sagovski.inf5040.assignments.spread.BankCurrency;
import org.sagovski.inf5040.assignments.spread.CurrencyCode;

public class BankCurrencyTests {

	@Test
	public void testBankCurrency() {
		BankCurrency currency = new BankCurrency(new BigDecimal("23.22544"), CurrencyCode.NOK);
		System.out.println(currency.toString());
	}

}
