package org.sagovski.inf5040.assignments.spread;

import java.math.BigDecimal;

public enum CurrencyCode {

	NOK, USD, EUR;

	// --- NOK USD EUR
	// NOK 00 01 02
	// USD 10 11 13
	// EUR 20 21 22
	static final BigDecimal exchangeRates[][] = new BigDecimal[][] {
			{ new BigDecimal("1"), new BigDecimal("0.12"), new BigDecimal("0.10") },
			{ new BigDecimal("7.87"), new BigDecimal("1"), new BigDecimal("0.83") },
			{ new BigDecimal("8.55"), new BigDecimal("1"), new BigDecimal("1") }, };

	static public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to) {
		return exchangeRates[from.ordinal()][to.ordinal()];
	}

}
