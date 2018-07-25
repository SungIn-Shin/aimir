package com.aimir.fep.logger;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;

public class NuriPatternConverter extends PatternConverter {

	@Override
	protected String convert(LoggingEvent arg0) {
		return NuriLogSequence.getInstance().getSeq().getUnchecked(Thread.currentThread().hashCode());
	}

}
