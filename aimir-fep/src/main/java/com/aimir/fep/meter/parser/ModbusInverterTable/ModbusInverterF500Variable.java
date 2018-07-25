/**
 * (@)# ModbusInverterF500Variable.java
 *
 * 2015. 6. 13.
 *
 * Copyright (c) 2013 NURITELECOM, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of 
 * NURITELECOM, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with NURITELECOM, Inc.
 *
 * For more information on this product, please see
 * http://www.nuritelecom.co.kr
 *
 */
package com.aimir.fep.meter.parser.ModbusInverterTable;


/**
 * @author simhanger
 *
 */
public class ModbusInverterF500Variable {
	/**
	 * 출력전류, 출력 주파수, 출력 전압만 정보로 이용하며
	 * 차후 더 필요한 정보가 있을경우 해당정보를 파싱하는 부분을 
	 * 추가로 구현하여 이용한다.
	 *  
	 * @author simhanger
	 */
    public enum MODBUS_ROCKWELL_CODE {
    	// 이름, 단위, 유닛, 설명
    	OUTPUT_FREQUENCY("0001", "0.01", "Hz", "출력 주파수"),
    	FREQUENCY_DIRECTION("0002", "0.01", "Hz", "지령 주파수"),
        OUTPUT_CURRENT("0003", "0.1", "A", "출력 전류"),        
        OUTPUT_VOLTAGE("0004", "1", "VAC", "출력 전압"),
        DC_BUS_VOLTAGE("0005", "1", "VDC", "DC 버스 전압"),        
    	INVERTER_STATUS("0006", null, null, "인버터 상태"),  
    	ERROR_1_CODE("0007", null, null, "에러 1 코드"),  
        PROCESS_VIEW("0008", null, null, "프로세스 표시"),
        OUTPUT_POWER("000A", "0.1", "Kw", "출력 전력"),        
        ACCUMULATED_MWH("000B", "0.1", "MWh", "누적 MWh"),
        ACCUMULATED_DRIVIN_TIME("000C", "10", "Hr", "누적 운전 시간"),
        TORQUE_CURRENT("000D", "0.1", "A", "토크 전류"),        
        INVERTER_TEMPERATURE("000E", "1", "C", "인버터 온도"),
        ACCUMULATED_KWH("000F", "0.1", "kWh", "누적 kWh"),
        
    	DATE("", null, null, "날짜");// Inverter는 LP가 없지만 시간 정보를 입력하기 위해 사용한다.
        
    	private String code;
        private String unitConst;
        private String unit;
        private String desc;
        
        MODBUS_ROCKWELL_CODE(String code, String unitConst, String unit, String desc) {
            this.code = code;
            this.unitConst = unitConst;
            this.unit = unit;
            this.desc = desc;
        }
        
        public String getCode() {
			return code;
		}

		public String getUnitConst() {
			return unitConst;
		}

		public String getUnit() {
			return unit;
		}

		public String getDesc() {
			return desc;
		}

		public static MODBUS_ROCKWELL_CODE getItem(String code) {
            for (MODBUS_ROCKWELL_CODE mCode : values()) {
                if (mCode.getCode().equals(code)) return mCode;
            }
            return null;
        }
    }

}
