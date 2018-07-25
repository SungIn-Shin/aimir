package com.aimir.fep.meter.parser.DLMSECGTable;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class DLMSVARIABLE {

    public enum OBIS {
    	TOKEN_GATEWAY("0000132800FF", "Token Gateway"), // 0.0.19.40.0.255
    	REMAINING_CREDIT("0141018C01FF", "Remaining Credit"), // 1.65.1.140.1.255
    	EMERGENCY_CREDIT("0141018C08FF", "Emergency Credit"), // 1.65.1.140.8.255
    	
    	/*  Meter Information */
        WEAK_LQI_VALUE("00001D0200FF","G3 PLC LQI VALUE"),
        DEVICE_INFO("0000600100FF", "Device Information"),        
        MANUFACTURE_SERIAL("0000600101FF","Manufacturer serial"),
        METER_TIME("0000010000FF", "Meter Time"),
        METER_MODEL("0000600103FF","Meter Model"),
        PHASE_TYPE("0100000204FF","Meter Phase Type"),
        LOGICAL_NUMBER("00002A0000FF","Logical Device Number"),
        FW_VERSION("0101000200FF","Firmware Version"),
        //HDLC_SETUP("0000160000FF","HDLC Setup"),
        //SERVICEPOINT_SERIAL("0000600102FF","SP serial number"),
        //CT_RATIO_NUM("0100000402FF","CT Ratio Number"),
        //VT_RATIO_NUM("0100000403FF","VT Ratio Number"),
        //CT_RATIO_DEN("0100000405FF","CT Ratio Den"),
        //VT_RATIO_DEN("0100000406FF","VT Ratio Den"),          
        //OVERAL_TRANS_NUM("0100000404FF","Overall transformer ratio (num)"),
        
        /* Load Profile */
        ENERGY_LOAD_PROFILE("0100630100FF","Energy Load Profile"),    
        POWER_LOAD_PROFILE("0100630200FF","Power Load Profile"),
        DAILY_LOAD_PROFILE("0100620101FF","Daily load profile"),
        MONTHLY_BILLING("0000620100FF","Monthly biling event log"),        
        ENERGY_OVERFLOW_EVENTLOG("0100636219FF","Energy overflow eventlog"), 
        //MONTHLY_ENERGY_PROFILE("0000620101FF", "Monthly Energy Profile"),
        //MONTHLY_DEMAND_PROFILE("0000620102FF", "Monthly Demand Profile"),         

      //Error register1(push)	0.0.97.97.0.255
    	//Error register2(push)	0.0.97.97.5.255
    	//Alarm register1	0.0.97.98.0.255
    	//Alarm register2	0.0.97.98.1.255
    	//Alarm filter1	0.0.97.98.10.255
    	//Alarm filter2	0.0.97.98.11.255
    	//Alarm descriptor1	0.0.97.98.20.255
    	//Alarm descriptor2	0.0.97.98.21.255
        
        /* Meter Alarm Information */
		MEASUREMENT_STATUS("0000600A07FF","Measurement Status"),
		DRIVE_STATUS("0000600A06FF","Drive status"),
        METER_STATUS("0000600A05FF","Meter Status"),
        RELAY_STATUS("000060030AFF","Relay Status"),  //0.0.96.3.10.255 Disconnect/reconnect 70 RW
        //FUNCTION_STATUS("","Function Status"), //TODO SET        
        //EXTERNAL_RELAY_STATUS("000360030AFF","External Relay Status"), //0.3.96.3.10.255 External Disconnect/reconnect
        
        /* Meter Event */
        STANDARD_EVENT("0000636200FF","Standard Event"), //0.0.99.98.0.255
        FRAUDDETECTIONLOGEVENT("0000636201FF","Fraud detection Log event"),//0.0.99.98.1.255        
        RELAY_EVENT("0000636202FF","Disconnector control log"), //0.0.99.98.2.255    
        MEASUREMENT_EVENT("0100630102FF", "Measurement event log"), //1.0.99.1.2.255
        POWERFAILURE_LOG("0100636100FF","Power Failure log"),  // 1.0.99.97.0.255: Power failure logs for single-phase/poly-phase
    	//TAMPER_EVENT("0100636215FF","Tamper event log"),        //1.0.99.98.21.255    	
        //POWER_QUALITY_LOG("0000636203FF","Power Quality Log"), //0.0.99.98.3.255: power quality logs
        //INCORRECT_PHASE_ROTATION_EVENT("0100636220FF", "Incorrect phase rotation log"),
    	//TIME_CHANGE_BEFORE("0100636203FF", "Time Change Before"),
    	//TIME_CHANGE_AFTER("0100636204FF", "Time Change After"),
    	//MANUAL_DEMAND_RESET("0100636206FF", "Manual Demand Reset"),
    	//POWER_FAILURE("0100636100FF","Power Failure"),
    	//POWER_RESTORE("0100636202FF","Power Restore"),
    	//BATTERY_FAILURE("0100636224FF","Battery failure event"),
        //RELAY_CONTROL_SCRIPT("00000A006AFF","Relay Status"),//0.0.10.0.106.255 Disconnect control script table 9 RW

        /* Channel Information */                
        VOLTAGE_L1("0100200700FF","L1 voltage"),
        VOLTAGE_L2("0100340700FF","L2 voltage"),
        VOLTAGE_L3("0100480700FF","L3 voltage"),
        CURRENT_L1("01001F0700FF","L1 current"),
        CURRENT_L2("0100330700FF","L2 current"),
        CURRENT_L3("0100470700FF","L3 current"),
        CT("0100000402FF","CT"),
        VT("0100000403FF","VT"),
        CT_DEN("0100000405FF","CT_DEN"),
        VT_DEN("0100000406FF","VT_DEN"),
        
        ACTIVE_POWER_L1("0100150700FF","L1 active power"),        
        ACTIVE_POWER_L2("0100290700FF","L2 active power"),
        ACTIVE_POWER_L3("01003D0700FF","L3 active power"),        
        ACTIVE_POWER_EXPORT_L1("0100160700FF","L1 active power-"),
        ACTIVE_POWER_EXPORT_L2("01002A0700FF","L1 active power-"),
        ACTIVE_POWER_EXPORT_L3("01003E0700FF","L1 active power-"),
        
        ACTIVEPOWER_IMPORT("0100010700FF","Active Power Import"),
        REACTIVE_POWER_L1("0100170700FF","L1 reactive power"),
        REACTIVE_POWER_L2("01002B0700FF","L2 reactive power"),
        REACTIVE_POWER_L3("01003F0700FF","L3 reactive power"),
        REACTIVEPOWER_IMPORT("0100030700FF","Reactive Power Import"),
        
        REACTIVE_POWER_EXPORT_L1("0100180700FF","L1 reactive power-"),
        REACTIVE_POWER_EXPORT_L2("01002C0700FF","L2 reactive power-"),
        REACTIVE_POWER_EXPORT_L3("0100400700FF","L3 reactive power-"),
        REACTIVEPOWER_EXPORT("0100040700FF","Reactive Power Export"),
        
        TOTAL_IMPORT_APPARENT_POWER("0100090700FF", "Total import apparent power (QI+QIV)"),
        TOTAL_EXPORT_APPARENT_POWER("01000A0700FF", "Total export apparent power (QII+QIII)"), 
        
        APPARENT_POWER_L1("01001D0700FF","L1 apparent power"),
        APPARENT_POWER_L2("0100310700FF","L2 apparent power"),
        APPARENT_POWER_L3("0100450700FF","L3 apparent power"),
        
        APPARENT_POWER_EXPORT_L1("01001E0700FF","L1 apparent power-"),
        APPARENT_POWER_EXPORT_L2("0100320700FF","L2 apparent power-"),
        APPARENT_POWER_EXPORT_L3("0100460700FF","L3 apparent power-"),
        
        POWER_FACTOR_L1("0100210700FF","L1 power factor"),
        POWER_FACTOR_L2("0100350700FF","L2 power factor"),
        POWER_FACTOR_L3("0100490700FF","L3 power factor"),
        TOTAL_POWER_FACTOR("01000D0700FF","Total power factor"),
        ACTIVEPOWER_EXPORT("0100020700FF","Active Power Export"),
        
        SUPPLY_FREQUENCY_L1("0100220700FF","L1 supply frequency"),
        SUPPLY_FREQUENCY_L2("0100360700FF","L2 supply frequency"),
        SUPPLY_FREQUENCY_L3("01004A0700FF","L3 supply frequency"),
        TOTAL_SUPPLY_FREQUENCY("01000E0700FF","Total supply frequency"),
        
        ANGLE_L1("0101510704FF","Angle of I(L1)-U(L1)"),
        ANGLE_L2("010151070FFF","Angle of I(L2)-U(L2)"),
        ANGLE_L3("010151071AFF","Angle of I(L3)-U(L3)"),                       
        
        LASTMONTH_ACTIVEENERGY_IMPORT1("010001080001","Last month total import active energy (QI+QIV)1"),
        LASTMONTH_ACTIVEENERGY_IMPORT2("010001080065","Last month total import active energy (QI+QIV)2"),
        LASTMONTH_ACTIVEENERGY_EXPORT1("010002080001","Last month total export active energy (QII+QIII)1"),
        LASTMONTH_ACTIVEENERGY_EXPORT2("010002080065","Last month total export active energy (QII+QIII)2"),
        LASTMONTH_REACTIVEENERGY_IMPORT1("010003080001","Last month total import reactive energy (QI+QII)1"),
        LASTMONTH_REACTIVEENERGY_IMPORT2("010003080065","Last month total import reactive energy (QI+QII)2"),
        LASTMONTH_REACTIVEENERGY_EXPORT1("010004080001","Last month total export reactive energy (QIII+QIV)1"),
        LASTMONTH_REACTIVEENERGY_EXPORT2("010004080065","Last month total export reactive energy (QIII+QIV)2"),
        
        CUMULATIVE_ACTIVEENERGY_IMPORT1("0100010801FF","Cumulative active energy -import1"),
        CUMULATIVE_ACTIVEENERGY_IMPORT2("0100010802FF","Cumulative active energy -import2"),        
        CUMULATIVE_ACTIVEENERGY_IMPORT3("0100010803FF","Cumulative active energy -import3"),
        TOTAL_CUMULATIVE_ACTIVEENERGY_IMPORT("0100010800FF","Cumulative active energy -import"),
        
        CUMULATIVE_ACTIVEENERGY_EXPORT1("0100020801FF","Cumulative active energy -export1"),
        CUMULATIVE_ACTIVEENERGY_EXPORT2("0100020802FF","Cumulative active energy -export2"),
        CUMULATIVE_ACTIVEENERGY_EXPORT3("0100020803FF","Cumulative active energy -export3"),
        TOTAL_CUMULATIVE_ACTIVEENERGY_EXPORT("0100020800FF","Cumulative active energy -export"),
        
        CUMULATIVE_REACTIVEENERGY_IMPORT1("0100030801FF","Cumulative reactive energy -import1"),
        CUMULATIVE_REACTIVEENERGY_IMPORT2("0100030802FF","Cumulative reactive energy -import2"),        
        CUMULATIVE_REACTIVEENERGY_IMPORT3("0100030803FF","Cumulative reactive energy -import3"),
        TOTAL_CUMULATIVE_REACTIVEENERGY_IMPORT("0100030800FF","Cumulative reactive energy -import"),
        
        CUMULATIVE_REACTIVEENERGY_EXPORT1("0100040801FF","Cumulative reactive energy -export1"),
        CUMULATIVE_REACTIVEENERGY_EXPORT2("0100040802FF","Cumulative reactive energy -export2"),
        CUMULATIVE_REACTIVEENERGY_EXPORT3("0100040803FF","Cumulative reactive energy -export3"),
        TOTAL_CUMULATIVE_REACTIVEENERGY_EXPORT("0100040800FF","Cumulative reactive energy -export"),
        
        TOTAL_MAX_DEMAND_IMPORT("0100010600FF","Total max demand +A"),
        TOTAL_MAX_DEMAND_IMPORT_T1("0100010601FF","Total maximum demand +A T1"),
        TOTAL_MAX_DEMAND_IMPORT_T2("0100010602FF","Total maximum demand +A T2"),
        TOTAL_MAX_DEMAND_IMPORT_T3("0100010603FF","Total maximum demand +A T3"),
        
        TOTAL_ACTIVEENERGY_IMPORT("01000F0800FF","Total energy +A"),
        TOTAL_ACTIVEENERGY_EXPORT("0100100800FF","Total energy -A"),
        TOTAL_MAX_ACTIVEDEMAND_EXPORT("0100020600FF","Total max demand -A"),
        TOTAL_MAX_REACTIVEDEMAND_IMPORT("0100030600FF","Total max demand +R"),
        TOTAL_MAX_REACTIVEDEMAND_EXPORT("0100040600FF","Total max demand -R"),
        TOTAL_CUM_ACTIVEDEMAND_IMPORT  ("0100010200FF","Total cumulative demand +A"),
        TOTAL_CUM_ACTIVEDEMAND_EXPORT  ("0100020200FF","Total cumulative demand -A"),
        TOTAL_CUM_REACTIVEDEMAND_IMPORT("0100030200FF","Total cumulative demand +R"),
        TOTAL_CUM_REACTIVEDEMAND_EXPORT("0100040200FF","Total cumulative demand -R"),
        
        MANUAL_DEMAND_RESET_EVENT_LOG("0100636206FF","Manual demand reset event log"), 

        PAYMENT_MODE_SETTING("0000603244FF","Payment mode Setting"),
        TOKEN_CREDIT_HISTORY("0100630F00FF", "STS Token Credit History"),
        OWE_CREDIT_THRESHOLD("0141018C08FF", "STS owe credit"),
        FRIENDLY_CREDIT_THRESHOLD("0141018C0BFF", "STS Friendly credit"),
        TOTAL_OWE_CREDIT("0141018C04FF","Total owe credit"), //total used(spend) credit

        CLOCK("0000010000FF","Clock"),
    	
        ACTIVE_FIRMWARE_DATE("0100000280FF", "Wasion Active Firmware Version"),
    	/* 
    	 * START STS TARIFF OBIS
    	 */
        THRESHOLD_SWITCH_TIME("0080600207FF","Threshold of Step Switch Time"),  
    	ACTIVE_THRESHOLD_STEP1("0141007101FF","Active threshold of step1"),
    	ACTIVE_THRESHOLD_STEP2("0141007102FF","Active threshold of step2"),
    	ACTIVE_THRESHOLD_STEP3("0141007103FF","Active threshold of step3"),
    	ACTIVE_THRESHOLD_STEP4("0141007104FF","Active threshold of step4"),
    	ACTIVE_THRESHOLD_STEP5("0141007105FF","Active threshold of step5"),
    	ACTIVE_THRESHOLD_STEP6("0141007106FF","Active threshold of step6"),
    	ACTIVE_THRESHOLD_STEP7("0141007107FF","Active threshold of step7"),
    	ACTIVE_THRESHOLD_STEP8("0141007108FF","Active threshold of step8"),
    	ACTIVE_THRESHOLD_STEP9("0141007109FF","Active threshold of step9"),
    	PASSIVE_THRESHOLD_STEP1("0141006F01FF","Passive threshold of step1"),
    	PASSIVE_THRESHOLD_STEP2("0141006F02FF","Passive threshold of step2"),
    	PASSIVE_THRESHOLD_STEP3("0141006F03FF","Passive threshold of step3"),
    	PASSIVE_THRESHOLD_STEP4("0141006F04FF","Passive threshold of step4"),
    	PASSIVE_THRESHOLD_STEP5("0141006F05FF","Passive threshold of step5"),
    	PASSIVE_THRESHOLD_STEP6("0141006F06FF","Passive threshold of step6"),
    	PASSIVE_THRESHOLD_STEP7("0141006F07FF","Passive threshold of step7"),
    	PASSIVE_THRESHOLD_STEP8("0141006F08FF","Passive threshold of step8"),
    	PASSIVE_THRESHOLD_STEP9("0141006F09FF","Passive threshold of step9"),
    	
    	RATE_SWITCH_TIME("0080600208FF","Rate price Switch Time"),
    	ACTIVE_RATE_PRICE1("0141006701FF","Active rate1 price"),
    	ACTIVE_RATE_PRICE2("0141006702FF","Active rate2 price"),
    	ACTIVE_RATE_PRICE3("0141006703FF","Active rate3 price"),
    	ACTIVE_RATE_PRICE4("0141006704FF","Active rate4 price"),
    	ACTIVE_RATE_PRICE5("0141006705FF","Active rate5 price"),
    	ACTIVE_RATE_PRICE6("0141006706FF","Active rate6 price"),
    	ACTIVE_RATE_PRICE7("0141006707FF","Active rate7 price"),
    	ACTIVE_RATE_PRICE8("0141006708FF","Active rate8 price"),
    	ACTIVE_RATE_PRICE9("0141006709FF","Active rate9 price"),
    	ACTIVE_RATE_PRICE10("014100670AFF","Active rate10 price"),
    	PASSIVE_RATE_PRICE1("0141006501FF","Passive rate1 price"),
    	PASSIVE_RATE_PRICE2("0141006502FF","Passive rate2 price"),
    	PASSIVE_RATE_PRICE3("0141006503FF","Passive rate3 price"),
    	PASSIVE_RATE_PRICE4("0141006504FF","Passive rate4 price"),
    	PASSIVE_RATE_PRICE5("0141006505FF","Passive rate5 price"),
    	PASSIVE_RATE_PRICE6("0141006506FF","Passive rate6 price"),
    	PASSIVE_RATE_PRICE7("0141006507FF","Passive rate7 price"),
    	PASSIVE_RATE_PRICE8("0141006508FF","Passive rate8 price"),
    	PASSIVE_RATE_PRICE9("0141006509FF","Passive rate9 price"),
    	PASSIVE_RATE_PRICE10("014100650AFF","Passive rate10 price"),
    	
    	SERVICE_CHARGE_SWITCH_TIME("008060020AFF","Service Charge Switch Time"),
    	ACTIVE_SERVICE_CHARGE1("01BE008201FF","Active Service Charge Step1"),
    	ACTIVE_SERVICE_CHARGE2("01BE008202FF","Active Service Charge Step2"),
    	ACTIVE_SERVICE_CHARGE3("01BE008203FF","Active Service Charge Step3"),
    	ACTIVE_SERVICE_CHARGE4("01BE008204FF","Active Service Charge Step4"),
    	ACTIVE_SERVICE_CHARGE5("01BE008205FF","Active Service Charge Step5"),
    	ACTIVE_SERVICE_CHARGE6("01BE008206FF","Active Service Charge Step6"),
    	ACTIVE_SERVICE_CHARGE7("01BE008207FF","Active Service Charge Step7"),
    	ACTIVE_SERVICE_CHARGE8("01BE008208FF","Active Service Charge Step8"),
    	ACTIVE_SERVICE_CHARGE9("01BE008209FF","Active Service Charge Step9"),
    	ACTIVE_SERVICE_CHARGE10("01BE00820AFF","Active Service Charge Step10"),
    	PASSIVE_SERVICE_CHARGE1("01BE018201FF","Passive Service Charge Step1"),
    	PASSIVE_SERVICE_CHARGE2("01BE018202FF","Passive Service Charge Step2"),
    	PASSIVE_SERVICE_CHARGE3("01BE018203FF","Passive Service Charge Step3"),
    	PASSIVE_SERVICE_CHARGE4("01BE018204FF","Passive Service Charge Step4"),
    	PASSIVE_SERVICE_CHARGE5("01BE018205FF","Passive Service Charge Step5"),
    	PASSIVE_SERVICE_CHARGE6("01BE018206FF","Passive Service Charge Step6"),
    	PASSIVE_SERVICE_CHARGE7("01BE018207FF","Passive Service Charge Step7"),
    	PASSIVE_SERVICE_CHARGE8("01BE018208FF","Passive Service Charge Step8"),
    	PASSIVE_SERVICE_CHARGE9("01BE018209FF","Passive Service Charge Step9"),
    	PASSIVE_SERVICE_CHARGE10("01BE01820AFF","Passive Service Charge Step10"),
    	
    	GOV_LEVY_SWITCH_TIME("008060020BFF","Gov Levy Switch Time"),
    	ACTIVE_GOV_LEVY("01BE008301FF","Active Gov Levy"),
    	PASSIVE_GOV_LEVY("01BE018301FF","Passive Gov Levy"),

    	STREET_LIGHT_SWITCH_TIME("008060020CFF","Street Light Switch Time"),
    	ACTIVE_STREET_LIGHT("01BE008401FF","Active Street Light"),
    	PASSIVE_STREET_LIGHT("01BE018401FF","Passive Street Light"),

    	VAT_SWITCH_TIME("008060020DFF","Active Vat Switch Time"),
    	ACTIVE_VAT("01BE008501FF","Vat of Residential"),
    	PASSIVE_VAT("01BE018501FF","Passive Vat of Residential"),
    	
    	LIFELINE_SUBSIDY_SWITCH_TIME("008060020EFF","Lifeline Subsidy Switch Time"),
    	ACTIVE_LIFELINE_SUBSIDY1("01BE008C01FF","Active Lifeline Subsidy Step1"),
    	ACTIVE_LIFELINE_SUBSIDY2("01BE008C02FF","Active Lifeline Subsidy Step2"),
    	ACTIVE_LIFELINE_SUBSIDY3("01BE008C03FF","Active Lifeline Subsidy Step3"),
    	ACTIVE_LIFELINE_SUBSIDY4("01BE008C04FF","Active Lifeline Subsidy Step4"),
    	ACTIVE_LIFELINE_SUBSIDY5("01BE008C05FF","Active Lifeline Subsidy Step5"),
    	ACTIVE_LIFELINE_SUBSIDY6("01BE008C06FF","Active Lifeline Subsidy Step6"),
    	ACTIVE_LIFELINE_SUBSIDY7("01BE008C07FF","Active Lifeline Subsidy Step7"),
    	ACTIVE_LIFELINE_SUBSIDY8("01BE008C08FF","Active Lifeline Subsidy Step8"),
    	ACTIVE_LIFELINE_SUBSIDY9("01BE008C09FF","Active Lifeline Subsidy Step9"),
    	ACTIVE_LIFELINE_SUBSIDY10("01BE008C0AFF","Active Lifeline Subsidy Step10"),
    	PASSIVE_LIFELINE_SUBSIDY1("01BE018C01FF","Passive Lifeline Subsidy Step1"),
    	PASSIVE_LIFELINE_SUBSIDY2("01BE018C02FF","Passive Lifeline Subsidy Step2"),
    	PASSIVE_LIFELINE_SUBSIDY3("01BE018C03FF","Passive Lifeline Subsidy Step3"),
    	PASSIVE_LIFELINE_SUBSIDY4("01BE018C04FF","Passive Lifeline Subsidy Step4"),
    	PASSIVE_LIFELINE_SUBSIDY5("01BE018C05FF","Passive Lifeline Subsidy Step5"),
    	PASSIVE_LIFELINE_SUBSIDY6("01BE018C06FF","Passive Lifeline Subsidy Step6"),
    	PASSIVE_LIFELINE_SUBSIDY7("01BE018C07FF","Passive Lifeline Subsidy Step7"),
    	PASSIVE_LIFELINE_SUBSIDY8("01BE018C08FF","Passive Lifeline Subsidy Step8"),
    	PASSIVE_LIFELINE_SUBSIDY9("01BE018C09FF","Passive Lifeline Subsidy Step9"),
    	PASSIVE_LIFELINE_SUBSIDY10("01BE018C0AFF","Passive Lifeline Subsidy Step10"),

    	NORMAL_SUBSIDY_SWITCH_TIME("008060020FFF","Normal Subsidy Switch Time"),
    	ACTIVE_NORMAL_SUBSIDY1("01BE008D01FF","Active Normal Subsidy Step1"),
    	ACTIVE_NORMAL_SUBSIDY2("01BE008D02FF","Active Normal Subsidy Step2"),
    	ACTIVE_NORMAL_SUBSIDY3("01BE008D03FF","Active Normal Subsidy Step3"),
    	ACTIVE_NORMAL_SUBSIDY4("01BE008D04FF","Active Normal Subsidy Step4"),
    	ACTIVE_NORMAL_SUBSIDY5("01BE008D05FF","Active Normal Subsidy Step5"),
    	ACTIVE_NORMAL_SUBSIDY6("01BE008D06FF","Active Normal Subsidy Step6"),
    	ACTIVE_NORMAL_SUBSIDY7("01BE008D07FF","Active Normal Subsidy Step7"),
    	ACTIVE_NORMAL_SUBSIDY8("01BE008D08FF","Active Normal Subsidy Step8"),
    	ACTIVE_NORMAL_SUBSIDY9("01BE008D09FF","Active Normal Subsidy Step9"),
    	ACTIVE_NORMAL_SUBSIDY10("01BE008D0AFF","Active Normal Subsidy Step10"),
    	PASSIVE_NORMAL_SUBSIDY1("01BE018D01FF","Passive Normal Subsidy Step1"),
    	PASSIVE_NORMAL_SUBSIDY2("01BE018D02FF","Passive Normal Subsidy Step2"),
    	PASSIVE_NORMAL_SUBSIDY3("01BE018D03FF","Passive Normal Subsidy Step3"),
    	PASSIVE_NORMAL_SUBSIDY4("01BE018D04FF","Passive Normal Subsidy Step4"),
    	PASSIVE_NORMAL_SUBSIDY5("01BE018D05FF","Passive Normal Subsidy Step5"),
    	PASSIVE_NORMAL_SUBSIDY6("01BE018D06FF","Passive Normal Subsidy Step6"),
    	PASSIVE_NORMAL_SUBSIDY7("01BE018D07FF","Passive Normal Subsidy Step7"),
    	PASSIVE_NORMAL_SUBSIDY8("01BE018D08FF","Passive Normal Subsidy Step8"),
    	PASSIVE_NORMAL_SUBSIDY9("01BE018D09FF","Passive Normal Subsidy Step9"),
    	PASSIVE_NORMAL_SUBSIDY10("01BE018D0AFF","Passive Normal Subsidy Step10"),

    	UTILITY_RELIEF_SUBSIDY_SWITCH_TIME("0080600210FF","Utility Relief Switch Time"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY1("01BE008E01FF","Active Utility Relief Step1"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY2("01BE008E02FF","Active Utility Relief Step2"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY3("01BE008E03FF","Active Utility Relief Step3"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY4("01BE008E04FF","Active Utility Relief Step4"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY5("01BE008E05FF","Active Utility Relief Step5"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY6("01BE008E06FF","Active Utility Relief Step6"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY7("01BE008E07FF","Active Utility Relief Step7"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY8("01BE008E08FF","Active Utility Relief Step8"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY9("01BE008E09FF","Active Utility Relief Step9"),
    	ACTIVE_UTILITY_RELIEF_SUBSIDY10("01BE008E0AFF","Active Utility Relief Step10"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY1("01BE018E01FF","Passive Utility Relief Step1"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY2("01BE018E02FF","Passive Utility Relief Step2"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY3("01BE018E03FF","Passive Utility Relief Step3"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY4("01BE018E04FF","Passive Utility Relief Step4"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY5("01BE018E05FF","Passive Utility Relief Step5"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY6("01BE018E06FF","Passive Utility Relief Step6"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY7("01BE018E07FF","Passive Utility Relief Step7"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY8("01BE018E08FF","Passive Utility Relief Step8"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY9("01BE018E09FF","Passive Utility Relief Step9"),
    	PASSIVE_UTILITY_RELIEF_SUBSIDY10("01BE018E0AFF","Passive Utility Relief Step10"),
    	/* 
    	 * END STS TARIFF OBIS
    	 */
    	
    	/*
    	 * STS Friendly OBIS
    	 */
    	STS_FRIENDLY_DAY_TABLE("00010B0000FF","Friendly Days Table"),
    	STS_FRIENDLY_TIME("00010B0000FF","Friendly Time"),
    	STS_FRIENDLY_WEEK("00010B0000FF","Friendly Weekday"),
    	/*
    	 * END STS Friendly OBIS
    	 */

    	/*
    	 * STS Payment Mode OBIS
    	 */
    	ACTIVE_STS_PAYMENTMODE_SETTING("0000603244FF","Active Payment Mode Setting"),
    	PASSIVE_STS_PAYMENTMODE_SETTING("0000603245FF","Passive Payment Mode Setting"),
    	PASSIVE_STS_PAYMENTMODE_SWITCH_TIME("0080600209FF","Payment Mode Switch Time");
    	/*
    	 * END STS Payment Mode OBIS
    	 */
    	
    	
    	//Error register1(push)	0.0.97.97.0.255
    	//Error register2(push)	0.0.97.97.5.255
    	//Alarm register1	0.0.97.98.0.255
    	//Alarm register2	0.0.97.98.1.255
    	//Alarm filter1	0.0.97.98.10.255
    	//Alarm filter2	0.0.97.98.11.255
    	//Alarm descriptor1	0.0.97.98.20.255
    	//Alarm descriptor2	0.0.97.98.21.255

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
        
        public static OBIS getObis(String code) {
            for (OBIS obis : values()) {
                if (obis.getCode().equals(code)) return obis;
            }
            return null;
        }
    }
    
    public enum OBIS_GROUP {
    	CMD_ACTIVE_THRESHOLD_STEP("ACTIVE THRESHOLD STEP"),
    	CMD_PASSIVE_THRESHOLD_STEP("PASSIVE THRESHOLD STEP"),
    	CMD_ACTIVE_RATE_PRICE("ACTIVE RATE PRICE"),
    	CMD_PASSIVE_RATE_PRICE("PASSIVE RATE PRICE"),
    	CMD_ACTIVE_SERVICE_CHARGE("ACTIVE SERVICE CHARGE"),
    	CMD_PASSIVE_SERVICE_CHARGE("PASSIVE SERVICE CHARGE"),
    	CMD_ACTIVE_GOV_LEVY("ACTIVE GOV LEVY"),
    	CMD_PASSIVE_GOV_LEVY("PASSIVE GOV LEVY"),
    	CMD_ACTIVE_STREET_LIGHT("ACTIVE STREET LIGHT"),
    	CMD_PASSIVE_STREET_LIGHT("PASSIVE STREET LIGHT"),
    	CMD_ACTIVE_VAT("ACTIVE VAT"),
    	CMD_PASSIVE_VAT("PASSIVE VAT"),
    	CMD_ACTIVE_LIFELINE_SUBSIDY("ACTIVE LIFELINE SUBSIDY"),
    	CMD_PASSIVE_LIFELINE_SUBSIDY("PASSIVE LIFELINE SUBSIDY"),
    	CMD_ACTIVE_NORMAL_SUBSIDY("ACTIVE NORMAL SUBSIDY"),
    	CMD_PASSIVE_NORMAL_SUBSIDY("PASSIVE NORMAL SUBSIDY"),
    	CMD_ACTIVE_UTILITY_RELIEF_SUBSIDY("ACTIVE UTILITY RELIEF SUBSIDY"),
    	CMD_PASSIVE_UTILITY_RELIEF_SUBSIDY("PASSIVE UTILITY RELIEF SUBSIDY");
    	
		private String name;

		OBIS_GROUP(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

    }
    
    public enum METER_EVENT_LOG {
    	Poweroff(1, "Power off"),
    	PowerOn(2, "Power on"),
    	DaylightTimeChange (3, "Daylight Saveing Time Change"),    	
    	ClockOldAdjusted(4, "Clock adjusted (old date/time)"),
    	ClockNewAdjusted(5, "Clock adjusted (new date/time)"),
    	ClockInvalid(6, "Clock invalidn"),
    	ReplaceBattery(7, "Replace Battery"),
    	BatteryVoltageLow(8, "Battery voltage low"),
    	ActivatedTOU(9, "tariff shift times (TOU) activated"),
    	ErrorRegisterCleared(10, "Error register cleared"),
    	
    	AlarmRegisterCleared(11, "Alarm register cleared"),
    	RAM_Error(13, "RAM error"),
    	NV_MemoryError(14, "NV memory error"),
    	WatchdogRrror(15, "Watchdog error"),
    	MeasurementError(16, "Measurement system error"),
    	FirmwareReadyActivation(17, "Firmware ready for activation"),
    	FirmwareActivated(18, "Firmware activated"),
    	ChangeTOU(19, "Change of tariff shift times has occurred"),
    	Successful_Selfcheck_After_Firmwareupdate(19, "Successful  selfcheck after Firmwareupdate"),
    	ExternalAlertDetected(20, "External alert detected"),
    	
    	TerminalCoverRemoved(40, "Terminal cover removed"),
    	TerminalCoverClosed(41, "Terminal cover closed"),
    	StrongDCFieldDetected(42, "Strong DC field detected"),
    	NostrongDCFieldAnymore(43, "No strong DC field anymore"),
    	MeterCoverRemoved(44, "Meter cover removed"),
    	MeterCoverClosed(45, "Meter cover closed"),
    	FailedLoginAttempt(46, "Failed login attempt"),
    	ConfigurationChange(47, "Configuration change"),
    	DecryptionFailure(49, "Decryption  failure"),
    	ReplayAttack(50, "Replay attack"),
    	LocalCommunicationAttemp(53, "Local communication attemp"),
    	ReadyReconnect(59, "Ready for reconnect"),
    	    	
    	ManualDisconnection(60, "Manual disconnection"),
    	ManualConnection(61, "Manual connection"),
    	RemoteDisconnection(62, "Remote disconnection"),
    	RemoteConnection(63, "Remote connection"),
    	LocalDisconnection(64, "Local disconnection"),
    	LimiterThreshold_Exceeded(65, "Limiter threshold exceeded"),
    	LimiterThreshold_OK(66, "Limiter threshold ok"),
    	LimiterThreshold_Changed(67, "Limiter threshold changed"),
    	limiterThresholdOver(69, "Manual close the relay failed,because of the Input voltage high than 115% or the output have the voltage"),
    	    	
    	UnderVoltage_L1(76, "under voltage L1"),
    	UnderVoltage_L2(77, "under voltage L2"),
    	UnderVoltage_L3(78, "under voltage L3"),
    	OverVoltage_L1(79, "over voltage L1"),
    	OverVoltage_L2(80, "over voltage L2"),
    	OverVoltage_L3(81, "over voltage L3"),
    	MissingVoltage_L1(82, "missing voltage L1"),
    	MissingVoltage_L2(83, "missing voltage L2"),
    	MissingVoltage_L3(84, "missing voltage L3"),
    	VoltageNormal_L1(85, "voltage normal L1"),
    	VoltageNormal_L2(86, "voltage normal L2"),
    	VoltageNormal_L3(87, "voltage normal L3"),
    	
    	PhaseAsymmetry(90, "phase asymmetry"),
    	CurrentReversal(91, "Current reversal"),
    	BadVoltageQuality_L1(92, "bad voltage quality L1"),
    	BadVoltageQuality_L2(93, "bad voltage quality L2"),
    	BadVoltageQuality_L3(94, "bad voltage quality L3"),
    	
    	PulsOver(230, "Pulse over"),
    	InterBastteryLow(231, "inter basttery low"),
    	CODE_RTCHARDWARE_ERR(232, "CODE_RTCHARDWARE_ERR"),
    	RELAYRemoteNotclose(236, "RELAY remote notclose"),
    	RelayManualNotclose(237, "relay manual notclose"),
    	RelayLocalNotclose(238, "relay local notclose"),
    	RelayMaintainDisconnect(239, "relay maintain disconnect"),
    	
    	AstronomicalDisconnect (240, "Astronomical calendar Disconnect"),
    	AstronomicalReconnect(241, "Astronomical calendar Reconnect");
    	
        private int flag;
        private String msg;
        
        METER_EVENT_LOG(int flag, String msg) {
            this.flag = flag;
            this.msg = msg;
        }
        
        public int getFlag() {
            return this.flag; 
        }
        
        public String getMsg() {
            return this.msg;
        }
    }   
    
    public final static String UNDEFINED = "undefined";

    public enum METER_CONSTANT {
        ActiveC,
        ReactiveC;
    }
    
    /**
     * Drive(Device) Alarm Message
     * @author simhanger
     *
     */
    public enum DEVICE_STATUS_ALARM{
    	EPROMError(1011,"EPROM error"),
    	ClockError(1012,"Clock error"),
    	BatteryError(1013,"Battery error"),
    	ReadCardError(1014,"Read card error"),
    	DataAbnormal(1015,"Data abnormal"),
    	// 2016.03.15 추가분 필요시 주석처리.
    	ExternalBatteryStatus(1016, "External Battery Status"),
    	HighLowLevelInput(1017, "High low level input"),
    	VoltageDetectInput(1018, "Voltage detect input");

    	
        private int flag;
        private String msg;
        
        DEVICE_STATUS_ALARM(int flag, String msg) {
            this.flag = flag;
            this.msg = msg;
        }
        
        public int getFlag() {
            return this.flag; 
        }
        
        public String getMsg() {
            return this.msg;
        }
    }
    
    /**
     * Function Alarm Message
     * @author simhanger
     *
     */
    public enum FUNCTION_STATUS_ALARM{
    	L1RelayError(2011,"L1 relay error"),
    	L2RelayError(2012,"L2 relay error"),
    	L3RelayError(2013,"L3 relay error"),
    	ExternalRelayError(2014,"External relay error"),
    	OpenTerminalCover(2015,"Open terminal cover"),
    	OpenTerminalCoverInPowerOff(2016,"Open terminal cover in power off"),    	
    	OpenTopCover(2017,"Open top cover"),
    	OpenTopCoverInPowerOff(2018,"Open top cover in power off"),    	
    	MagneticDetection1(2019,"Magnetic detection 1"),
    	MagneticDetection2(2020,"Magnetic detection 2"),
    	
    	
    	// 2016.03.15 추가분 필요시 주석처리.
    	L1RelayStatus(2021, "L1 relay status"),
    	L2RelayStatus(2022, "L2 relay status"),
    	L3RelayStatus(2023, "L3 relay status"),
    	ExternalRelayStatus(2024, "External relay status"),
    	Program(2025 , "program"),
    	FactoryStatus(2026,"Factory status");
    	
        private int flag;
        private String msg;
        
        FUNCTION_STATUS_ALARM(int flag, String msg) {
            this.flag = flag;
            this.msg = msg;
        }
        
        public int getFlag() {
            return this.flag; 
        }
        
        public String getMsg() {
            return this.msg;
        }
    }

    /**
     * Measurement Alarm Message
     * @author simhanger
     *
     */
    public enum MEASUREMENT_STATUS_ALARM{    	
    	L1VoltageLoss(3011,"L1 voltage loss"),
    	L2VoltageLoss(3012,"L2 voltage loss"),
    	L3VoltageLoss(3013,"L3 voltage loss"),
    	L1CurrentLoss(3014,"L1 current loss"),
    	L2CurrentLoss(3015,"L2 current loss"),
    	L3CurrentLoss(3016,"L3 current loss"),
    	L1VoltageCut(3017,"L1 voltage cut"),
    	L2VoltageCut(3018,"L2 voltage cut"),
    	L3VoltageCut(3019,"L3 voltage cut"),
    	VoltageReversePhaseSequence(3020,"Voltage reverse phase sequence"),
    	CurrentReversePhaseSequence(3021,"Current reverse phase sequence"),
    	VoltageAsymmetric(3022,"Voltage asymmetric"),
    	CurrentAsymmetric(3023,"Current asymmetric"),
    	L1OverCurrent(3024,"L1 over current"),
    	L2OverCurrent(3025,"L2 over current"),
    	L3OverCurrent(3026,"L3 over current"),
    	L1CurrentCut(3027,"L1 current cut"),
    	L2CurrentCut(3028,"L2 current cut"),
    	L3CurrentCut(3029,"L3 current cut"),
    	L1OverVoltage(3030,"L1 over voltage"),
    	L2OverVoltage(3031,"L2 over voltage"),
    	L3OverVoltage(3032,"L3 over voltage"),
    	L1UnderVoltage(3033,"L1 under voltage"),
    	L2UnderVoltage(3034,"L2 under voltage"),
    	L3UnderVoltage(3035,"L3 under voltage"),
    	AllPhasesVoltageLoss(3036,"All phases voltage loss"),
    	L1OverLoad(3037,"L1 over load"),
    	L2OverLoad(3038,"L2 over load"),
    	L3OverLoad(3039,"L3 over load"),
    	TotalPowerFactorExceeded(3040,"Total power factor exceeded"),
    	L1VoltageSuperTopLimit(3041,"L1 voltage super top limit"),
    	L2VoltageSuperTopLimit(3042,"L2 voltage super top limit"),
    	L3VoltageSuperTopLimit(3043,"L3 voltage super top limit"),

    	// 2016.03.15 추가분 필요시 주석처리.
    	L1VoltageQualification(3044,"L1 voltage qualification"),
    	L2VoltageQualification(3045,"L2 voltage qualification"),
    	L3VoltageQualification(3046,"L3 voltage qualification"),
    	
    	L1VoltageSuperLowLimit(3047,"L1 voltage super low limit"),
    	L2VoltageSuperLowLimit(3048,"L2 voltage super low limit"),
    	L3VoltageSuperLowLimit(3049,"L3 voltage super low limit"),
    	NeutralCurrentUnbalance(3050,"Neutral current unbalance"),
    	L1ReverseCurrent(3051,"L1 reverse current"),
    	L2ReverseCurrent(3052,"L2 reverse current"),
    	L3ReverseCurrent(3053,"L3 reverse current");
    	
        private int flag;
        private String msg;
        
        MEASUREMENT_STATUS_ALARM(int flag, String msg) {
            this.flag = flag;
            this.msg = msg;
        }
        
        public int getFlag() {
            return this.flag; 
        }
        
        public String getMsg() {
            return this.msg;
        }
    }
    
    /**
     * Load Profile Status Message
     * @author simhanger
     *
     */
    public enum LP_STATUS{
    	CriticalError(4011,"Critical error"),
    	ClockInvalid(4012,"Clock invalid"),
    	DataNotValid(4013,"Data not valid"),
    	DaylightSaving(4014,"Daylight saving"),
    	NotUsed04(4015,"Not used04"),
    	ClockAdjusted(4016,"Clock adjusted"),    	
    	NotUsed06(4017,"Not used06"),
    	PowernDown(4018,"Power Down");
    	
        private int flag;
        private String msg;
        
        LP_STATUS(int flag, String msg) {
            this.flag = flag;
            this.msg = msg;
        }
        
        public int getFlag() {
            return this.flag; 
        }
        
        public String getMsg() {
            return this.msg;
        }
    }

    public enum EVENT_LOG {

        PowerFailure(1, "Power Down"),
        PowerRestore(2, "Power Up"),
        DSTSET(3,"Daylight saving time enabled or disabled"),
        TimeChangeFrom(4, "Clock adjusted old time"),
        TimeChangeTo(5, "Clock adjusted new time"),
        ClockInvalid(6,"Clock invalid"),
        ReplaceBattery(7,"Replace Battery"),
        BatteryVoltageLow(8,"Battery Voltage Low"),
        TariffShiftTimeActivated(9,"tariff shift times (TOU) activated"),
        
        ErrorRegisterCleared(10,"Error register cleared"),
        AlarmRegisterCleared(11,"Alarm register cleared"),
        
        ProgramMemoryError(12,"Program memory error"),
        RAMError(13,"RAM Error"),
        NVMemoryError(14,"NV memory error"),
        WatchdogError(15,"Watchdog error"),
        MeasurementSystemError(16,"Measurement system error"),
        
        FirmwareReadyForActivation(17,"Firmware ready for activation"),
        FirmwareActivated(18,"Firmware activated"),
        TariffShiftTime(19,"Tariff Shift Time (TOU)"),
        SuccessfullSelfcheckAfterFirmwareUpdate(20,"Succesfull selfcheck after Firmware update"),
        GPRSModemConnected(21,"GPRS modem connected"),
        GPRSModemDisconnected(22,"GPRS modem disconnected"),
        G3ModemConnected(23,"G3 modem connected"),
        G3ModemDisconnected(24,"G3 modem disconnected"),
        RFModemConnected(25,"RF modem connected"),
        RFModemDisconnected(26,"RF modem disconnected"),
        
        MeterTerminalCoverRemoved(40, "Meter terminal cover removed"),
        MeterTerminalCoverClosed(41, "Meter terminal cover closed"),
        StrongDCFieldDetected(42,"Indicates that the strong magnetic DC field has appeared."),
        NoStringDCField(43,"Indicates that the strong magnetic DC field has disappeared."),
        MeterCoverRemoved(44, "Meter cover removed"),
        MeterCoverClosed(45, "Meter cover closed"),
        FailedLoginAttempt(46,"Failed login attempt"),
        ConfigurationChange(47,"Configuration change"),
        SuccessfulLogin(48,"successful  login"),
        ManualDisconnection(60,"Manual disconnection"),
        ManualConnection(61,"Manual connection"),
        RemoteDisconnection(62,"Remote disconnection"),
        RemoteConnection(63,"Remote connection"),
        LocalDisconnection(64,"Local disconnection"),
        
        LimiterThresholdExceeded(65,"Limiter threshold exceeded"),
        LimiterThresholdOK(66,"Limiter threshold ok"),
        LimiterThresholdChanged(67,"Limiter threshold changed"),
        
        DeviceAlarm(101,"Device Alarm"),
        FunctionAlarm(201,"Function Alarm"),
        MeasurementAlarm(301,"Measurement Alarm"),
    	LPStatus(401,"Load Profile Status"),
    	RolloverEventAlarm(501,"Rollover Event Alarm");
        
        private int flag;
        private String msg;
        
        EVENT_LOG(int flag, String msg) {
            this.flag = flag;
            this.msg = msg;
        }
        
        public int getFlag() {
            return this.flag; 
        }
        
        public String getMsg() {
            return this.msg;
        }
    }

    /**
     * 채널변경시 함께 수정해주어야 함.
     * @author simhanger
     *
     */
    public enum ENERGY_LOAD_PROFILE {
        DateTime(1, "Date Time"),
        ActiveEnergyImport(2, "Active Energy -Import"),
        ActiveEnergyExport(3, "Active Energy -Export"),
        ReactiveEnergyImport(4, "Reactive Energy -Import"),
        ReactiveEnergyExport(5, "Reactive Energy -Export"),
        Status(6, "Status");

        private int code;
        private String name;
        
        ENERGY_LOAD_PROFILE(int code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public int getCode() {
            return this.code;
        }
        
        public String getName(){
            return this.name;
        }
    }
    
    
   /**
    * ENERGY_OVERFLOW_EVENTLOG_OBIS
    */
   public enum ENERGY_OVERFLOW_EVENTLOG_OBIS {
       
	   DateTime(1, "Date Time"),
       CumulativeActiveEnergyExport(2, "Cumulative active energy -export"),
       CumulativeReactiveEnergyExport(3, "Cumulative reactive energy -export"),
       CumulativeActiveEnergyImport(4, "Cumulative active energy -import"),
       CumulativeActiveEnergyImport1(5, "Cumulative active energy -import rate 1"),
       CumulativeActiveEnergyImport2(6, "Cumulative active energy -import rate 2"),
       CumulativeActiveEnergyImport3(7, "Cumulative active energy -import rate 3"),
       CumulativeActiveEnergyImport4(8, "Cumulative active energy -import rate 4"),        
       CumulativeReactiveEnergyImport(9, "Cumulative reactive energy -import"),
       CumulativeReactiveEnergyImport1(10, "Cumulative reactive energy -import rate 1"),
       CumulativeReactiveEnergyImport2(11, "Cumulative reactive energy -import rate 2"),
       CumulativeReactiveEnergyImport3(12, "Cumulative reactive energy -import rate 3"),
       CumulativeReactiveEnergyImport4(13, "Cumulative reactive energy -import rate 4");
       
       private int code;
       private String name;
       
       ENERGY_OVERFLOW_EVENTLOG_OBIS(int code, String name) {
           this.code = code;
           this.name = name;
       }
       
       public int getCode() {
           return this.code;
       }
       
       public String getName(){
           return this.name;
       }
   }
   
    
    
    
    
    /*
    public enum METER_TIME {
        BeforeTime(0, "beforeTime"),
        AfterTime(1, "afterTime");
        
        private int code;
        private String name;
        
        METER_TIME(int code, String name) {
            this.code = code;
        }
        
        public int getCode() {
            return this.code;
        }
        
        public String getName(){
            return this.name;
        }
    }
    */
    
    public enum DLMS_CLASS {
    	TOKEN_GATEWAY(115),
    	
        DATA(1),
        REGISTER(3),
        EXTEND_REGISTER(4),
        DEMAND_REGISTER(5),
        REGISTER_ACTIVATION(6),
        PROFILE_GENERIC(7),
        CLOCK(8),
        SCRIPT_TABLE(9),
        SCHEDULE(10),
        SPECIAL_DAY(11),
        ACTIVITY_CALENDAR(20),
        ASSOCIATION_LN(15),
        ASSOCIATION_SN(12),
        REGISTER_MONITOR(21),
        SAP_ASSIGN(17),
        UTILITY_TABLE(26),
        SINGLE_ACTION_SCHEDULE(22),
        HDLC(23),
        RELAY_CLASS(70),
        G3_PLC_6LoWPAN(92);
        
        private int clazz;
        
        DLMS_CLASS(int clazz) {
            this.clazz = clazz;
        }
        
        public int getClazz() {
            return this.clazz;
        }
    }
    
    public enum DLMS_CLASS_ATTR {
    	TOKEN_ATTR01(1),
    	TOKEN_ATTR02(2),
    	
        DATA_ATTR01(2),            // value
        REGISTER_ATTR02(2),        // value
        REGISTER_ATTR03(3),        // scalar unit
        REGISTER_ATTR04(4),        // status
        REGISTER_ATTR05(5),        // time
        PROFILE_GENERIC_ATTR02(2), // buffer
        PROFILE_GENERIC_ATTR03(3),        // capture_objects array
        PROFILE_GENERIC_ATTR04(4),        // value
        PROFILE_GENERIC_ATTR07(7), // entries in use 
        CLOCK_ATTR01(1),//logical name
        CLOCK_ATTR02(2),//time
        CLOCK_ATTR03(3),//time_zone
        CLOCK_ATTR04(4),//status
        CLOCK_ATTR05(5),//dst_start
        CLOCK_ATTR06(6),//dst end
        CLOCK_ATTR07(7),//dst deviation
        CLOCK_ATTR08(8),//dst enable
        CLOCK_ATTR09(9),//clock base
        SCRIPT_TABLE_ATTR01(1),
        SCRIPT_TABLE_ATTR02(2),
        ADP_WEAK_LQI_VALUE(3),
        HDLC_ATTR01(1),
        HDLC_ATTR02(2),
        HDLC_ATTR03(3),
        HDLC_ATTR04(4),
        HDLC_ATTR05(5),
        HDLC_ATTR06(6),
        HDLC_ATTR07(7),
        HDLC_ATTR08(8),
        HDLC_ATTR09(9);

        private int attr;
        
        DLMS_CLASS_ATTR(int attr) {
            this.attr = attr;
        }
        
        public int getAttr() {
            return this.attr;
        }
    }

    public enum DLMS_TAG_TYPE {
        Null(0, 1),
        Array(1, 1),
        CompactArray(19, 1),
        Structure(2, 1),
        Enum(22, 1),
        Group(128, 1),
        BitString(4, 0),
        OctetString(9, 0),
        VisibleString(10, 0),
        BCD(13, 1),
        Boolean(3, 1),
        INT8(15, 1),
        UINT8(17, 1),
        INT16(16, 2),
        UINT16(18, 2),
        INT32(5, 4),
        UINT32(6, 4),
        FLOAT32(23, 4),
        INT64(20, 8),
        UINT64(21, 8),
        Datetime(25, 12),
        Date(26, 5),
        Time(27, 4);
        
        private int value;
        private int len;
        
        DLMS_TAG_TYPE(int value, int len) {
            this.value = value;
            this.len = len;
        }
        
        public int getValue() {
            return this.value;
        }
        
        public int getLen() {
            return this.len;
        }
    }
    
    public DLMSVARIABLE() {

    }

/*
    public static String getDataName(OBIS obis, int cnt) {

        if(obis == OBIS.STANDARD_EVENT 
                || obis == OBIS.FRAUDDETECTIONLOGEVENT 
                || obis == OBIS.RELAY_EVENT
        		|| obis == OBIS.POWER_FAILURE 
        		|| obis == OBIS.POWER_RESTORE
                || obis == OBIS.TIME_CHANGE_BEFORE 
                || obis == OBIS.TIME_CHANGE_AFTER
                || obis == OBIS.MANUAL_DEMAND_RESET
                || obis == OBIS.TAMPER_EVENT) {
            // 데이타 구조가 array, structure, date, cnt
            int mod = (cnt+1) % 3;
            int value = (cnt+1)/ 3;
            
            if (value >= 1) {
                switch (obis) {
                    case POWER_FAILURE :
                        if (mod == 0) return EVENT_LOG.PowerFailure.name()+value;
                        //else if (mod == 1) return EVENT_LOG.PowerFailureCnt.name()+value;
                        break;
                    case POWER_RESTORE :
                        if (mod == 0) return EVENT_LOG.PowerRestore.name()+value;
                        //else if (mod == 1) return EVENT_LOG.PowerRestoreCnt.name()+value;
                        break;
                    case TIME_CHANGE_BEFORE :
                        if (mod == 0) return EVENT_LOG.TimeChangeFrom.name()+value;
                        //else if (mod == 1) return EVENT_LOG.TimeChangeFromCnt.name()+value;
                        break;
                    case TIME_CHANGE_AFTER :
                        if (mod == 0) return EVENT_LOG.TimeChangeTo.name()+value;
                        //else if (mod == 1) return EVENT_LOG.TimeChangeToCnt.name()+value;
                        break;
                    //case MANUAL_DEMAND_RESET :
                    //    if (mod == 0) return EVENT_LOG.ManualDemandReset.name()+value;
                        //else if (mod == 1) return EVENT_LOG.ManualDemandResetCnt.name()+value;
                    //   break;
                    case BATTERY_FAILURE:
                    	//if (mod == 0) return EVENT_LOG.
                    	break;
                    case TAMPER_EVENT:
                    	break;
                    case RELAY_EVENT:
                    	break;
                    case STANDARD_EVENT :
                    	System.out.println("STANDARD EVENT="+mod+","+value);
                    	break;
                    case FRAUDDETECTIONLOGEVENT : //TODO ADD
                    	break;
                    default:
                    	break;
                }
            }
        }
        
        return UNDEFINED+" ["+cnt+"]";
    }
    */

    public static String getUnit(int unit) {

        if (unit == 1)
            return "a";
        else if (unit == 2)
            return "mo";
        else if (unit == 3)
            return "wk";
        else if (unit == 4)
            return "d";
        else if (unit == 5)
            return "h";
        else if (unit == 6)
            return "min.";
        else if (unit == 7)
            return "s";
        else if (unit == 8)
            return "°";
        else if (unit == 9)
            return "°C";
        else if (unit == 10)
            return "currency";
        else if (unit == 11)
            return "m";
        else if (unit == 12)
            return "m/s";
        else if (unit == 13)
            return "m3";
        else if (unit == 14)
            return "m3";
        else if (unit == 15)
            return "m3/h";
        else if (unit == 16)
            return "m3/h";
        else if (unit == 17)
            return "m3/d";
        else if (unit == 18)
            return "m3/d";
        else if (unit == 19)
            return "l";
        else if (unit == 20)
            return "kg";
        else if (unit == 21)
            return "N";
        else if (unit == 22)
            return "Nm";
        else if (unit == 23)
            return "Pa";
        else if (unit == 24)
            return "bar";
        else if (unit == 25)
            return "J";
        else if (unit == 26)
            return "J/h";
        else if (unit == 27)
            return "W";
        else if (unit == 28)
            return "VA";
        else if (unit == 29)
            return "var";
        else if (unit == 30)
            return "Wh";
        else if (unit == 31)
            return "VAh";
        else if (unit == 32)
            return "varh";
        else if (unit == 33)
            return "A";
        else if (unit == 34)
            return "C";
        else if (unit == 35)
            return "V";
        else if (unit == 36)
            return "V/m";
        else if (unit == 37)
            return "F";
        else if (unit == 38)
            return "Ω";
        else if (unit == 39)
            return "Ωm2/m";
        else if (unit == 40)
            return "Wb";
        else if (unit == 41)
            return "T";
        else if (unit == 42)
            return "A/m";
        else if (unit == 43)
            return "H";
        else if (unit == 44)
            return "Hz";
        else if (unit == 45)
            return "1/(Wh)";
        else if (unit == 46)
            return "1/(varh)";
        else if (unit == 47)
            return "1/(VAh)";
        else if (unit == 48)
            return "V2h";
        else if (unit == 49)
            return "A2h";
        else if (unit == 50)
            return "kg/s";
        else if (unit == 51)
            return "S, mho";
        else if (unit == 52)
            return "K";
        else if (unit == 53)
            return "1/(V2h)";
        else if (unit == 54)
            return "1/(A2h)";
        else if (unit == 55)
            return "1/m3";
        else if (unit == 56)
            return "percentage";
        else if (unit == 57)
            return "Ah";
        else if (unit == 60)
            return "Wh/m3";
        else if (unit == 61)
            return "J/m3";
        else if (unit == 62)
            return "Mol %";
        else if (unit == 63)
            return "g/m3";
        else if (unit == 64)
            return "Pa s";
        else if (unit == 253)
            return "reserved";
        else if (unit == 254)
            return "other";
        else if (unit == 255)
            return "unitless";
        else
            return "unitless";
    }    
    
    public enum EXTERNAL_RELAY_STATUS {
        NONE(0),            // 설정없음 
        REMOTE_CONTROL(4),  // 원격부하 개폐신호
        TIME_SWITCH(8),     // 타임스위치 개폐신호
        CURRENT_LIMIT(16);  // 전류제한 기능
        
        int code;
        
        EXTERNAL_RELAY_STATUS(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return this.code;
        }
        
        public static EXTERNAL_RELAY_STATUS getValue(int code) {
            for (EXTERNAL_RELAY_STATUS a : EXTERNAL_RELAY_STATUS.values()) {
                if (a.getCode() == code)
                    return a;
            }
            return null;
        }
    }
    
    public static String[] LP_STATUS_BIT = new String[]{
    	"Power down", //bit 7
    	"Not used",
    	"Clock adjusted",
    	"Not used",
    	"Daylight saving",
    	"Data not valid",
    	"Clock invalid",
    	"Critical error" //  bit 0
    };
    
    /*
    (Byte0) Bit0	L1 voltage loss
    (Byte0) Bit1	L2 voltage loss
    (Byte0) Bit2	L3 voltage loss
    (Byte0) Bit3	L1 current loss
    (Byte0) Bit4	L2 current loss
    (Byte0) Bit5	L3 current loss
    (Byte0) Bit6	L1 voltage cut
    (Byte0) Bit7	L2 voltage cut    
    (Byte1) Bit0	L3 voltage cut
    (Byte1) Bit1	Voltage reverse phase sequence 
    (Byte1) Bit2	Current reverse phase sequence
    (Byte1) Bit3	Voltage asymmetric
    (Byte1) Bit4	Current asymmetric
    (Byte1) Bit5	L1 over current
    (Byte1) Bit6	L2 over current
    (Byte1) Bit7	L3 over current    
    (Byte2) Bit0	L1 current cut
    (Byte2) Bit1	L2 current cut
    (Byte2) Bit2	L3 current cut
    (Byte2) Bit3	L1 over voltage
    (Byte2) Bit4	L2 over voltage
    (Byte2) Bit5	L3 over voltage
    (Byte2) Bit6	L1 under voltage
    (Byte2) Bit7	L2 under voltage    
    (Byte3) Bit0	L3 under voltage
    (Byte3) Bit1	All phases voltage loss
    (Byte3) Bit2	L1 over load
    (Byte3) Bit3	L2 over load
    (Byte3) Bit4	L3 over load
    (Byte3) Bit5	Total power factor exceeded
    (Byte3) Bit6	L1 voltage super top limit
    (Byte3) Bit7	L2 voltage super top limit    
    (Byte4) Bit0	L3 voltage super top limit
    (Byte4) Bit1	L1 voltage qualification
    (Byte4) Bit2	L2 voltage qualification
    (Byte4) Bit3	L3 voltage qualification
    (Byte4) Bit4	L1 voltage super low limit
    (Byte4) Bit5	L2 voltage super low limit
    (Byte4) Bit6	L3 voltage super low limit
    (Byte4) Bit7	Neutral current unbalance
    (Byte5) Bit0	L1 reverse current
    (Byte5) Bit1	L2 reverse current
    (Byte5) Bit2	L3 reverse current
    */

    public static String[] MEASUREMENT_STATUS_BYTE_0 = new String[]{
    	"L2 voltage cut",
    	"L1 voltage cut",
    	"L3 current loss",
    	"L2 current loss",
    	"L1 current loss",
    	"L3 voltage loss",
    	"L2 voltage loss",
    	"L1 voltage loss"
    };
    
    public static String[] MEASUREMENT_STATUS_BYTE_1 = new String[]{
    	"L3 over current",
    	"L2 over current",
    	"L1 over current",
    	"Current asymmetric",
    	"Voltage asymmetric",
    	"Current reverse phase sequence",
    	"Voltage reverse phase sequence",
    	"L3 voltage cut"
    };
    
    public static String[] MEASUREMENT_STATUS_BYTE_2 = new String[]{
    	"L2 under voltage",
    	"L1 under voltage",
    	"L3 over voltage",
    	"L2 over voltage",
    	"L1 over voltage",
    	"L3 current cut",
    	"L2 current cut",
    	"L1 current cut"
    };
    
    public static String[] MEASUREMENT_STATUS_BYTE_3 = new String[]{
    	"L2 voltage super top limit",
    	"L1 voltage super top limit",
    	"Total power factor exceeded",
    	"L3 over load",
    	"L2 over load",
    	"L1 over load",
    	"All phases voltage loss",
    	"L3 under voltage"
    };
    
    public static String[] MEASUREMENT_STATUS_BYTE_4 = new String[]{
    	"Neutral current unbalance",
    	"L3 voltage super low limit",
    	"L2 voltage super low limit",
    	"L1 voltage super low limit",
    	"L3 voltage qualification",
    	"L2 voltage qualification",
    	"L1 voltage qualification",
    	"L3 voltage super top limit"
    };
    
    public static String[] MEASUREMENT_STATUS_BYTE_5 = new String[]{
    	"",
    	"",
    	"",
    	"",
    	"",
    	"L3 reverse current",
    	"L2 reverse current",
    	"L1 reverse current"
    };
    

    /*
    Function status，Function status config，Alarm Function status	Bit No	Function
	(Byte0) Bit0	L1 relay status
	(Byte0) Bit1	L2 relay status
	(Byte0) Bit2	L3 relay status
	(Byte0) Bit3	External relay status
	(Byte0) Bit4	L1 relay error
	(Byte0) Bit5	L2 relay error
	(Byte0) Bit6	L3 relay error
	(Byte0) Bit7	External relay error
	(Byte1) Bit0	Open terminal cover
	(Byte1) Bit1	Open terminal cover in power off 
	(Byte1) Bit2	Open top cover
	(Byte1) Bit3	Open top cover in power off
	(Byte1) Bit4	Magnetic detection 1
	(Byte1) Bit5	Magnetic detection 2
	(Byte1) Bit6	program
	(Byte1) Bit7	Factory status
	*/
    public static String[] FUNCTION_STATUS_BYTE_0 = new String[] {
    	"External relay error",
    	"L3 relay error",
    	"L2 relay error",
    	"L1 relay error",
    	"External relay status",
    	"L3 relay status",
    	"L2 relay status",
    	"L1 relay status"
    };
    public static String[] FUNCTION_STATUS_BYTE_1 = new String[] {
    	"Factory status",
    	"program",
    	"Magnetic detection 2",
    	"Magnetic detection 1",
    	"Open top cover in power off",
    	"Open top cover",
    	"Open terminal cover in power off ",
    	"Open terminal cover"
    };    

    /*
	Drive status，Drive status config，Alarm Drive status	Bit No	Function
	Bit0	EPROM error
	Bit1	Clock error
	Bit2	Battery error
	Bit3	Read card error
	Bit4	Data abnormal
	Bit5	External Battery Status
	Bit6	High low level input
	Bit7	Voltage detect input
	*/
    public static String[] DRIVE_STATUS_BYTE_0 = new String[] {
    	"Voltage detect input",
    	"High low level input",
    	"External Battery Status",
    	"Data abnormal",
    	"Read card error",
    	"Battery error",
    	"Clock error",
    	"EPROM error"
    };

    
    @XmlType(name = "RELAY_STATUS_WASION")
    @XmlEnum
    public enum RELAY_STATUS_WASION {
        @XmlEnumValue("Disconnected")
        Disconnected(0),
        @XmlEnumValue("Connected")
        Connected(1);
        
        int code;
        
        RELAY_STATUS_WASION(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return this.code;
        }
        
        public static RELAY_STATUS_WASION getValue(int code) {
            for (RELAY_STATUS_WASION a : RELAY_STATUS_WASION.values()) {
                if (a.getCode() == code)
                    return a;
            }
            return null;
        }
    }
}
