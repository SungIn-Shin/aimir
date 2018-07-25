/**
 * 
 */
package com.aimir.fep.trap.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aimir.constants.CommonConstants.EventStatus;
import com.aimir.constants.CommonConstants.TargetClass;
import com.aimir.dao.device.FirmwareIssueHistoryDao;
import com.aimir.dao.device.MCUDao;
import com.aimir.fep.trap.common.EV_Action;
import com.aimir.fep.util.EventUtil;
import com.aimir.fep.util.FirmwareUtil;
import com.aimir.model.device.Device.DeviceType;
import com.aimir.model.device.EventAlertAttr;
import com.aimir.model.device.EventAlertLog;
import com.aimir.model.device.MCU;
import com.aimir.notification.FMPTrap;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.StringUtil;

/*
 * Event ID : 200.64.0 evtOTADownloadStart Processing 
 * 
 *  1) RequestID   - UINT
 *  2) UpgradeType - BYTE 
 *  3) TargetID    - STRING
 *  4) TargetModel - STRING
 */
@Component
public class EV_TW_200_64_0_Action implements EV_Action {
	private static Logger logger = LoggerFactory.getLogger(EV_TW_200_64_0_Action.class);

	@Autowired
	MCUDao mcuDao;
	
	@Autowired
	EventUtil eventUtil;
	
    @Autowired
    FirmwareIssueHistoryDao firmwareIssueHistoryDao;

    @Autowired
    FirmwareUtil fwUtil;
    
	/*
	 * Please don't change EVENT_MESSAGE message. because of concerned FIRMWARE_ISSUE_HISTORY searching in DB. 
	 */
	private final String EVENT_MESSAGE = "Started writing FW";

	@Override
	public void execute(FMPTrap trap, EventAlertLog event) throws Exception {
		logger.debug("[EV_SP_200_64_0_Action][evtOTADownloadStart][{}] Execute start.", EVENT_MESSAGE);

		try {
			String issueDate = DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss");

			String mcuId = trap.getMcuId();
			MCU mcu = mcuDao.get(mcuId);

			logger.debug("[EV_SP_200_64_0_Action][evtOTADownloadStart] DCU = {}({}), EventCode = {}", mcuId, trap.getIpAddr(), trap.getCode());

			String requestId = StringUtil.nullToBlank(event.getEventAttrValue("uintEntry"));
			logger.debug("[EV_SP_200_64_0_Action] requestId={}", requestId);

			String upgradeType = StringUtil.nullToBlank(event.getEventAttrValue("byteEntry"));
			logger.debug("[EV_SP_200_64_0_Action] upgradeType={}, TargetClass={}", OTA_UPGRADE_TYPE.getItem(upgradeType), OTA_UPGRADE_TYPE.getItem(upgradeType).getTargetClass().name());

			String targetId = StringUtil.nullToBlank(event.getEventAttrValue("stringEntry"));
			logger.debug("[EV_SP_200_64_0_Action] targetId={}", targetId);

			String targetModel = StringUtil.nullToBlank(event.getEventAttrValue("stringEntry.1")); // Thirdparty 방식일때만 값이 채워져서 올라옴.
			logger.debug("[EV_SP_200_64_0_Action] targetModel={}", targetModel);
			if (mcu != null) {
				DeviceType deviceType = null;
				switch (OTA_UPGRADE_TYPE.getItem(upgradeType)) {
				case METER:
					deviceType = DeviceType.Meter;
					break;
				case MODEM:
					deviceType = DeviceType.Modem;
					break;
				case DCU_FW:
					deviceType = DeviceType.MCU;
					break;
				case DCU_KERNEL:
					deviceType = DeviceType.MCU;
					break;
				case DCU_COORDINATE:
					deviceType = DeviceType.MCU;
					break;
				case THIRD_PARTY_COORDINATE:
				case THIRD_PARTY_MODEM:
					if (targetModel == null || targetModel.equals("")) {
						throw new Exception("Device target model is null.");
					}

					if (targetModel.equals("NAMR-P20CSR") || targetModel.equals("NAMR-W401LT") || targetModel.equals("NAMR-P20DSR")) { // RF || Mbb || split meter Modem
						deviceType = DeviceType.Modem;
					} else if (targetModel.indexOf("233") >= 0) { // Ghana country code id 233 fixed value
						deviceType = DeviceType.Meter;
					} else {
						throw new Exception("Unknown device target model.");
					}
					break;
				default:
					throw new Exception("Unknown device type.");
				}

				/*
				 * DCU Last Comm date update.
				 */
				mcu.setLastCommDate(issueDate);

				/*
				 * Event save.
				 */
				if (OTA_UPGRADE_TYPE.getItem(upgradeType) == OTA_UPGRADE_TYPE.DCU_FW
						|| OTA_UPGRADE_TYPE.getItem(upgradeType) == OTA_UPGRADE_TYPE.DCU_KERNEL 
						|| OTA_UPGRADE_TYPE.getItem(upgradeType) == OTA_UPGRADE_TYPE.DCU_COORDINATE 
						|| OTA_UPGRADE_TYPE.getItem(upgradeType) == OTA_UPGRADE_TYPE.THIRD_PARTY_MODEM
						|| OTA_UPGRADE_TYPE.getItem(upgradeType) == OTA_UPGRADE_TYPE.THIRD_PARTY_COORDINATE) {
					targetId = mcuId; // Coordinator 가 하는 경우 DCU로 이벤트 처리.					
				}
				event.setActivatorType(OTA_UPGRADE_TYPE.getItem(upgradeType).getTargetClass());
				event.setActivatorId(targetId);
				event.setLocation(mcu.getLocation());

				EventAlertAttr ea = EventUtil.makeEventAlertAttr("message", "java.lang.String", getEventMessage(OTA_UPGRADE_TYPE.getItem(upgradeType).getTargetClass(), "DCU"));
				event.append(ea);

				/*
				 * Update OTA History save.
				 */
				fwUtil.updateOTAHistory(targetId, deviceType, issueDate, requestId, EVENT_MESSAGE);

			} else {
				logger.debug("[EV_SP_200_64_0_Action][evtOTADownloadStart] DCU = {}({}) : Unknown DCU", trap.getMcuId(), trap.getIpAddr());
			}
		} catch (Exception e) {
			logger.error("[EV_SP_200_64_0_Action][evtOTADownloadStart] Error - ", e);
		}
		logger.debug("[EV_SP_200_64_0_Action][evtOTADownloadStart][{}] Execute stop.", EVENT_MESSAGE);
	}

	/**
	 * EV_SP_200_64_0 Event Make
	 * 
	 * @param activatorType
	 * @param activatorId
	 * @param targetType
	 * @param openTime
	 * @param operatorType
	 *            - HES or DCU
	 */
	public void makeEvent(TargetClass activatorType, String activatorId, TargetClass targetType, String openTime, String operatorType) {
		logger.debug("[EV_SP_200_64_0_Action][evtOTADownloadStart] MakeEvent.");

		String resultValue = getEventMessage(targetType, operatorType);

		EventAlertLog eventAlertLog = new EventAlertLog();
		eventAlertLog.setStatus(EventStatus.Open);
		eventAlertLog.setOpenTime(openTime);

		eventUtil.sendEvent("OTA", activatorType, activatorId, openTime, new String[][] { { "message", resultValue } }, eventAlertLog);
		logger.debug("[EV_SP_200_64_0_Action][openTime={}] evtOTADownloadStart - {}", openTime, resultValue);
	}

	/**
	 * Event message make
	 * 
	 * @param targetType
	 * @param operatorType
	 * @return
	 */
	private String getEventMessage(TargetClass targetType, String operatorType) {
		StringBuilder builder = new StringBuilder();
		builder.append("[" + EVENT_MESSAGE + "]");
		builder.append("Target Type=[" + targetType.name() + "]");
		builder.append(", OperatorType=[" + operatorType + "]");

		return builder.toString();
	}
}
