package com.aimir.fep.meter.parser.DLMSECGTable;

public class DLMSSCALAR {
	public enum OBIS {
		  CUMULATIVE_ACTIVEENERGY_IMPORT("0100010800FF", "Cumulative active energy -import ")
		, CUMULATIVE_ACTIVEENERGY_IMPORT1("0100010801FF", "Cumulative active energy -import rate 1 ")
		, CUMULATIVE_ACTIVEENERGY_IMPORT2("0100010802FF", "Cumulative active energy -import rate 2 ")
		, CUMULATIVE_ACTIVEENERGY_IMPORT3("0100010803FF", "Cumulative active energy -import rate 3 ")
		, CUMULATIVE_REACTIVEENERGY_IMPORT("0100030800FF", "Cumulative reactive energy -import ");

		private String code;
		private String name;

		OBIS(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public String getCode() {
			return this.code;
		}

		public String getName() {
			return this.name;
		}
	}

	public enum OBISSACLAR {
		CUMULATIVE_ACTIVEENERGY_IMPORT("0100010800FF", 0)
 		, CUMULATIVE_ACTIVEENERGY_IMPORT1("0100010801FF", 0)
 		, CUMULATIVE_ACTIVEENERGY_IMPORT2("0100010802FF", 0)
 		, CUMULATIVE_ACTIVEENERGY_IMPORT3("0100010803FF", 0)
		, CUMULATIVE_REACTIVEENERGY_IMPORT("0100030800FF", 0);

		private String code;
		private int scalar;

		OBISSACLAR(String code, int scalar) {
			this.code = code;
			this.scalar = scalar;
		}

		public String getCode() {
			return this.code;
		}

		public int getScalar() {
			return this.scalar;
		}

		public static double getOBISScalar(String code) {
			if (code.isEmpty())
				return 1;

			for (OBISSACLAR obis : values()) {
				if (obis.getCode().equals(code)) {
					return (1.0 / Math.pow(10, obis.scalar));
				}
			}
			return 1;
		}
	}

}
