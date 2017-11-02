package tst.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.sagovski.inf5040.assignments.spread.CurrencyCode;

public class CurrencyCodeTests {

	@Test
	public void testCurrencyCodeExchange_nokToUsd_returnValidExchangeRate() {
		BigDecimal expectedExchangeRate = new BigDecimal("0.12");
		BigDecimal actualExchangeRate = CurrencyCode.getExchangeRate(CurrencyCode.NOK, CurrencyCode.USD);

		Assert.assertTrue(actualExchangeRate.toString(), actualExchangeRate.compareTo(expectedExchangeRate) == 0);
	}

	@Test
	public void testCurrencyCodeExchange_nokToNok_returnValidExchangeRate() {
		BigDecimal expectedExchangeRate = new BigDecimal("1");
		BigDecimal actualExchangeRate = CurrencyCode.getExchangeRate(CurrencyCode.NOK, CurrencyCode.NOK);

		Assert.assertTrue(actualExchangeRate.toString(), actualExchangeRate.compareTo(expectedExchangeRate) == 0);
	}

	@Test
	public void testCurrencyCodeExchange_nokToEur_returnValidExchangeRate() {
		BigDecimal expectedExchangeRate = new BigDecimal("0.10");
		BigDecimal actualExchangeRate = CurrencyCode.getExchangeRate(CurrencyCode.NOK, CurrencyCode.EUR);

		Assert.assertTrue(actualExchangeRate.toString(), actualExchangeRate.compareTo(expectedExchangeRate) == 0);
	}
}
