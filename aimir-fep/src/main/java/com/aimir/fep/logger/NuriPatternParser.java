package com.aimir.fep.logger;

import org.apache.log4j.helpers.PatternParser;

public class NuriPatternParser extends PatternParser{

	private static final char DEV_PART_CHAR = 's';
	
	public NuriPatternParser(String pattern) {
		super(pattern);
	}
	
	@Override
	protected void finalizeConverter(char c) {
		switch(c) {
		case DEV_PART_CHAR:
			currentLiteral.setLength(0);
			addConverter(new NuriPatternConverter());
			break;
		default:
			super.finalizeConverter(c);
			break;
		}
	}
	
}
