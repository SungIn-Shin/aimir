/**
 * (@)# ModbusInverterN700VVariable.java
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
public class ModbusInverterN700VVariable {
	/**
	 * 출력전류, 출력 주파수, 출력 전압만 정보로 이용하며
	 * 차후 더 필요한 정보가 있을경우 해당정보를 파싱하는 부분을 
	 * 추가로 구현하여 이용한다.
	 *  
	 * @author simhanger
	 */
	
    public enum MODBUS_HYUNDAI_CODE {
    	// 이름, 단위, 유닛, 설명
    	OUTPUT_FREQUENCY_MONITOR("0101", "0.01", "Hz", "Output Frequency Monitor"),  
    	DRIVING_DIRECTION_MONITOR("0102", "1", null, "Driving Direction Monitor"),  
        OUTPUT_CURRENT_MONITOR("0103", "0.1", "A", "Output Current Monitor"),        
        OUTPUT_VOLTAGE_MONITOR("0104", "0.1", "V", "Output Voltage Monitor"),
        DC_LINK_VOLTAGE_MONITOR("0105", "0.1", "V", "DC Link Voltage Monitor"),
        INPUT_POWER_MONITOR("0106", "0.1", "kW", "Input Power Monitor"),
        OUTPUT_TORQUE_MONITOR("0107", "1", "%", "Output Torque Monitor"),
        NUMBER_OF_MOTOR_ROTATION("0108", "1", "RPM", "Number Of Motor Rotation"),
        PID_FEEDBACK_MONITOR("0109", null, null, "PID Feedback Monitor"),
        INTELLIGENT_INPUT_TERMINAL_MONITOR("010A", "1", null, "Intelligent Input Terminal Monitor"),
        INTELLIGENT_OUTPUT_TERMINAL_MONITOR("010B", "1", null, "Intelligent Output Terminal Monitor"),
        FREQUENCY_CONVERSION_MONITOR("010C", "0.01", "Hz", "Frequency Conversion Monitor"),
        ACCUMULATED_TIME_MONITOR_DURING_RUN_HR("010D", "1", "Hour", "Accumulated Time Monitor During RUN Hr"),
        ACCUMULATED_TIME_MONITOR_DURING_RUN_MIN("010E", "1", "Minute", "Accumulated Time Monitor During RUN Min"),
        
    	DATE("", null, null, "날짜");// Inverter는 LP가 없지만 시간 정보를 입력하기 위해 사용한다.
        
    	private String code;
        private String unitConst;
        private String unit;
        private String desc;
        
        MODBUS_HYUNDAI_CODE(String code, String unitConst, String unit, String desc) {
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

		public static MODBUS_HYUNDAI_CODE getItem(String code) {
            for (MODBUS_HYUNDAI_CODE mCode : values()) {
                if (mCode.getCode().equals(code)) return mCode;
            }
            return null;
        }
    }
    
    
    

}
