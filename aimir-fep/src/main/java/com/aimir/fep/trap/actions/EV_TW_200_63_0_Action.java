package com.aimir.fep.trap.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aimir.constants.CommonConstants.EventStatus;
import com.aimir.constants.CommonConstants.TargetClass;
import com.aimir.dao.device.MCUDao;
import com.aimir.fep.trap.common.EV_Action;
import com.aimir.fep.util.EventUtil;
import com.aimir.fep.util.FirmwareUtil;
import com.aimir.model.device.EventAlertAttr;
import com.aimir.model.device.EventAlertLog;
import com.aimir.model.device.MCU;
import com.aimir.notification.FMPTrap;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.StringUtil;

/*
 * Event ID : 200.63.0 evtOTADownload Processing
 * 
 * 1) RequestID   - UINT 
 * 2) UpgradeType - BYTE 
 * 3) ImageUrl    - STRING
 * 4) TargetModel - STRING
 */
@Component
public class EV_TW_200_63_0_Action implements EV_Action {
	private static Logger logger = LoggerFactory.getLogger(EV_TW_200_63_0_Action.class);

	@Autowired
	MCUDao mcuDao;
	
	@Autowired
	EventUtil eventUtil;
	
	@Autowired
	FirmwareUtil fwUtil;

	/*
	 * Please don't change EVENT_MESSAGE message. because of concerned FIRMWARE_ISSUE_HISTORY searching in DB. 
	 */
	private final String EVENT_MESSAGE = "Took OTA Command";

	@Override
	public void execute(FMPTrap trap, EventAlertLog event) throws Exception {
		logger.debug("[EV_TW_200_63_0_Action][evtOTADownload][{}] Execute.", EVENT_MESSAGE);

		try {
			String issueDate = DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss");

			String mcuId = trap.getMcuId();
			MCU mcu = mcuDao.get(mcuId);

			logger.debug("[EV_TW_200_63_0_Action][evtOTADownload] DCU = {}({}), EventCode = {}", trap.getMcuId(), trap.getIpAddr(), trap.getCode());

			String requestId = StringUtil.nullToBlank(event.getEventAttrValue("uintEntry"));
			logger.debug("[EV_TW_200_63_0_Action] requestId={}", requestId);

			String upgradeType = StringUtil.nullToBlank(event.getEventAttrValue("byteEntry"));
			logger.debug("[EV_TW_200_63_0_Action] upgradeType={}, TargetClass={}", OTA_UPGRADE_TYPE.getItem(upgradeType), OTA_UPGRADE_TYPE.getItem(upgradeType).getTargetClass().name());

			String imageUrl = StringUtil.nullToBlank(event.getEventAttrValue("stringEntry"));
			logger.debug("[EV_TW_200_63_0_Action] imageUrl={}", imageUrl);

			String targetModel = StringUtil.nullToBlank(event.getEventAttrValue("stringEntry.1")); // Thirdparty 방식일때만 값이 채워져서 올라옴.
			logger.debug("[EV_TW_200_63_0_Action] targetModel={}", targetModel);

			if (mcu != null) {
				/*
				 * DCU Last Comm date update.
				 */
				mcu.setLastCommDate(issueDate);

				/*
				 * Event save.
				 */
				event.setActivatorType(OTA_UPGRADE_TYPE.getItem(upgradeType).getTargetClass());
				event.setActivatorId(trap.getSourceId());
				event.setLocation(mcu.getLocation());

				String msg = getEventMessage(OTA_UPGRADE_TYPE.getItem(upgradeType).getTargetClass(), "DCU") + ", RequestId=" + requestId;

				EventAlertAttr ea = EventUtil.makeEventAlertAttr("message", "java.lang.String", msg);
				event.append(ea);

				/*
				 * Update OTA History save.
				 */
				fwUtil.updateOTAHistory(null, null, issueDate, requestId, EVENT_MESSAGE);
			} else {
				logger.debug("[EV_TW_200_63_0_Action][evtOTADownload] DCU = {}({}) : Unknown MCU", trap.getMcuId(), trap.getIpAddr());
			}

		} catch (Exception e) {
			logger.error("[EV_TW_200_63_0_Action][evtOTADownload] Error - ", e);
		}

	}

	/**
	 * EV_TW_200_63_0 Event make
	 * 
	 * @param activatorType
	 * @param activatorId
	 * @param targetType
	 * @param openTime
	 * @param operatorType
	 *            - HES or DCU
	 */
	public void makeEvent(TargetClass activatorType, String activatorId, TargetClass targetType, String openTime, String operatorType) {
		logger.debug("[EV_TW_200_63_0_Action][evtOTADownload] MakeEvent.");
		logger.debug("TargetClass = {}, activatorId={}, targetType={}, openTime={}, operatorType={}", activatorType, activatorId, targetType, openTime, operatorType);

		String resultValue = getEventMessage(targetType, operatorType);

		EventAlertLog eventAlertLog = new EventAlertLog();
		eventAlertLog.setStatus(EventStatus.Open);
		eventAlertLog.setOpenTime(openTime);

		eventUtil.sendEvent("OTA", activatorType, activatorId, openTime, new String[][] { { "message", resultValue } }, eventAlertLog);
		logger.debug("[EV_TW_200_63_0_Action][openTime={}] evtOTADownload - {}", openTime, resultValue);
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
