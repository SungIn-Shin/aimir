package com.aimir.fep.logger;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;

public class NuriPatternLayout extends PatternLayout{

	@Override
	protected PatternParser createPatternParser(String pattern) {		
		return new NuriPatternParser(pattern);
	}

}
