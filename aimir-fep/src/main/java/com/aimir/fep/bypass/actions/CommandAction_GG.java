package com.aimir.fep.bypass.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.aimir.constants.CommonConstants.MeterStatus;
import com.aimir.constants.CommonConstants.OperatorType;
import com.aimir.constants.CommonConstants.Protocol;
import com.aimir.constants.CommonConstants.ResultStatus;
import com.aimir.constants.CommonConstants.TR_STATE;
import com.aimir.dao.device.AsyncCommandLogDao;
import com.aimir.dao.device.AsyncCommandParamDao;
import com.aimir.dao.device.AsyncCommandResultDao;
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.device.MeterTimeSyncLogDao;
import com.aimir.dao.device.ModemDao;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.CustomerDao;
import com.aimir.dao.system.EcgSTSLogDao;
import com.aimir.dao.system.OperatorDao;
import com.aimir.dao.system.PrepaymentLogDao;
import com.aimir.dao.system.TariffEMDao;
import com.aimir.dao.system.TariffTypeDao;
import com.aimir.fep.bypass.BypassDevice;
import com.aimir.fep.bypass.sts.STSDataFrame;
import com.aimir.fep.bypass.sts.STSException;
import com.aimir.fep.bypass.sts.cmd.GetCIUCommStateHistoryReq;
import com.aimir.fep.bypass.sts.cmd.GetCIUCommStateHistoryRes;
import com.aimir.fep.bypass.sts.cmd.GetEmergencyCreditReq;
import com.aimir.fep.bypass.sts.cmd.GetEmergencyCreditRes;
import com.aimir.fep.bypass.sts.cmd.GetFriendlyCreditScheduleReq;
import com.aimir.fep.bypass.sts.cmd.GetFriendlyCreditScheduleRes;
import com.aimir.fep.bypass.sts.cmd.GetPaymentModeReq;
import com.aimir.fep.bypass.sts.cmd.GetPaymentModeRes;
import com.aimir.fep.bypass.sts.cmd.GetPreviousMonthNetChargeReq;
import com.aimir.fep.bypass.sts.cmd.GetPreviousMonthNetChargeRes;
import com.aimir.fep.bypass.sts.cmd.GetRFSetupReq;
import com.aimir.fep.bypass.sts.cmd.GetRFSetupRes;
import com.aimir.fep.bypass.sts.cmd.GetRemainingCreditReq;
import com.aimir.fep.bypass.sts.cmd.GetRemainingCreditRes;
import com.aimir.fep.bypass.sts.cmd.GetSTSSetupReq;
import com.aimir.fep.bypass.sts.cmd.GetSTSSetupRes;
import com.aimir.fep.bypass.sts.cmd.GetSTSTokenReq;
import com.aimir.fep.bypass.sts.cmd.GetSTSTokenRes;
import com.aimir.fep.bypass.sts.cmd.GetSpecificMonthNetChargeReq;
import com.aimir.fep.bypass.sts.cmd.GetSpecificMonthNetChargeRes;
import com.aimir.fep.bypass.sts.cmd.GetSuniFirmwareUpdateInfoReq;
import com.aimir.fep.bypass.sts.cmd.GetSuniFirmwareUpdateInfoRes;
import com.aimir.fep.bypass.sts.cmd.GetTariffReq;
import com.aimir.fep.bypass.sts.cmd.GetTariffRes;
import com.aimir.fep.bypass.sts.cmd.SetEmergencyCreditReq;
import com.aimir.fep.bypass.sts.cmd.SetEmergencyCreditRes;
import com.aimir.fep.bypass.sts.cmd.SetFriendlyCreditScheduleReq;
import com.aimir.fep.bypass.sts.cmd.SetFriendlyCreditScheduleRes;
import com.aimir.fep.bypass.sts.cmd.SetMessageReq;
import com.aimir.fep.bypass.sts.cmd.SetMessageRes;
import com.aimir.fep.bypass.sts.cmd.SetPaymentModeReq;
import com.aimir.fep.bypass.sts.cmd.SetPaymentModeRes;
import com.aimir.fep.bypass.sts.cmd.SetRFSetupReq;
import com.aimir.fep.bypass.sts.cmd.SetRFSetupRes;
import com.aimir.fep.bypass.sts.cmd.SetSTSSetupReq;
import com.aimir.fep.bypass.sts.cmd.SetSTSSetupRes;
import com.aimir.fep.bypass.sts.cmd.SetSTSTokenReq;
import com.aimir.fep.bypass.sts.cmd.SetSTSTokenRes;
import com.aimir.fep.bypass.sts.cmd.SetTariffReq;
import com.aimir.fep.bypass.sts.cmd.SetTariffRes;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateControlReq;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateControlRes;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateFileBlockReadReq;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateFileBlockReadRes;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateFileBlockWriteReq;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateFileBlockWriteRes;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateKeyReadReq;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateKeyReadRes;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateKeyWriteReq;
import com.aimir.fep.bypass.sts.cmd.SuniFirmwareUpdateKeyWriteRes;
import com.aimir.fep.command.conf.KamstrupCIDMeta;
import com.aimir.fep.command.conf.KamstrupCIDMeta.CID;
import com.aimir.fep.meter.data.MeterTimeSyncData;
import com.aimir.fep.protocol.fmp.client.Client;
import com.aimir.fep.protocol.fmp.client.bypass.BYPASSClient;
import com.aimir.fep.protocol.fmp.datatype.OPAQUE;
import com.aimir.fep.protocol.fmp.datatype.SMIValue;
import com.aimir.fep.protocol.fmp.datatype.STREAM;
import com.aimir.fep.protocol.fmp.datatype.WORD;
import com.aimir.fep.protocol.fmp.frame.ControlDataConstants;
import com.aimir.fep.protocol.fmp.frame.ControlDataFrame;
import com.aimir.fep.protocol.fmp.frame.ErrorCode;
import com.aimir.fep.protocol.fmp.frame.service.CommandData;
import com.aimir.fep.protocol.fmp.frame.service.MDData;
import com.aimir.fep.protocol.fmp.processor.ProcessorHandler;
import com.aimir.fep.util.CRCUtil;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.FMPProperty;
import com.aimir.fep.util.Hex;
import com.aimir.fep.util.XMLProperty;
import com.aimir.model.device.AsyncCommandLog;
import com.aimir.model.device.AsyncCommandParam;
import com.aimir.model.device.AsyncCommandResult;
import com.aimir.model.device.Meter;
import com.aimir.model.device.MeterTimeSyncLog;
import com.aimir.model.device.Modem;
import com.aimir.model.system.Contract;
import com.aimir.model.system.Customer;
import com.aimir.model.system.EcgSTSLog;
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.TariffEM;
import com.aimir.model.system.TariffType;
import com.aimir.util.Condition;
import com.aimir.util.Condition.Restriction;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.StringUtil;
import com.aimir.util.TimeUtil;


public class CommandAction_GG extends CommandAction {
    private static Log log = LogFactory.getLog(CommandAction_GG.class);
    
	@Autowired
	STSDataFrame stsDataFrame;
	
	String totalReceivedFrameStr = "";
	int totalReceivedFrameLen = 0;
	int cycleCnt = 1;
	int totalLen = 0;
    private String cmd = "";
    
    public void cmdSetBypassStart(IoSession session) throws Exception
    {
        BypassDevice bd = (BypassDevice)session.getAttribute(session.getRemoteAddress());
        int timeout = 10;
        
        if (bd.getArgs().size() > 0) {
            timeout = (Integer)bd.getArgs().get(0);
        }
        
        String ns = (String)session.getAttribute("nameSpace");
        List<SMIValue> params = new ArrayList<SMIValue>();
        params.add(DataUtil.getSMIValueByObject(ns, "cmdSetBypassStartTimeout", Integer.toString(timeout)));
        sendCommand(session, "cmdSetBypassStart", params);
    }
    
    
	@Override
	public void executeBypass(byte[] frame, IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String bypassCmd = (String) session.getAttribute("bypassCmd");
		log.debug(bypassCmd);

		if (bypassCmd.equals("cmdSetMeterTime")) {
			boolean isSuccess = false;
			if (frame[0] == 0x06) {
				log.debug("###### MeterTimeSync is Success ######");
				if (bd.getAsyncTrId() != 0 && bd.getModemId() != null) {
					// Async_Result 저장
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("Result", "MeterTimeSync is Success");
					addResult(bd.getModemId(), map, bd.getAsyncTrId());
				}
				isSuccess = true;
			} else {
				log.debug("##### MeterTimeSync is fail ######");
				// Async_Result 저장
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "MeterTimeSync is Fail[" + Hex.decode(frame) + "]");
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
			}
			// timeSync_Log 저장
			saveMeterTimeSyncLog(bd.getModemId(), isSuccess);
		} else if (bypassCmd.equals("cmdGetMaxDemand")) {
			session.write(new ControlDataFrame(ControlDataConstants.CODE_EOT));
			log.debug(Hex.decode(frame));
			if (frame[0] == 0x40) {
				byte[] bx = new byte[frame.length - 6];
				System.arraycopy(frame, 3, bx, 0, bx.length);
				Object[] result = CID.GetRegister.getResponse(bx);
				// result[1]이 kVA 값이다.
			}
		} else if (bypassCmd.equals("cmdSetPaymentMode")) {
			try {
				SetPaymentModeRes res = new SetPaymentModeRes(frame);

				// ECG_STS_LOG가 있기 때문에 찾아서 갱신해야 한다.
				updateEcgStsLog(bypassCmd, bd.getMeterId(), res.getResult(), "", bd.getAsyncTrId(),
						bd.getAsyncCreateDate());
			} catch (STSException e) {
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetPaymentMode")) {
			try {
				GetPaymentModeRes res = new GetPaymentModeRes(frame);
				int mode = res.getMode();
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), mode, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, null, bd.getAsyncTrId(), bd.getAsyncCreateDate(), null,
						null, /* remaining credit */
						null, null, null, null, null, null, /* net charge */
						null, null, null, null, null, /* friendly credit */
						null, null, null, null, null);
				// mode를 비교하여 틀리면 SetPaymentModeReq 실행
				validatePaymentMode(bd.getMeterId(), mode);
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetRemainingCredit")) {
			try {
				GetRemainingCreditRes res = new GetRemainingCreditRes(frame);
				String date = res.getDt();
				double credit = res.getCredit();
				// 현재 잔액을 갱신한다.
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, null, bd.getAsyncTrId(), bd.getAsyncCreateDate(), date + "00",
						credit, /* remaining credit */
						null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
				updateCurrentCredit(bd.getMeterId(), date, credit);

				// 비동기 내역에도 결과 저장하기
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "SUCCESS");
				map.put("ResponseDate", "" + date);
				map.put("ResponseCredit", "" + credit);
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetSTSToken")) {
			try {
				SetSTSTokenRes res = new SetSTSTokenRes(frame);
				String token = res.getToken();

				// 요청한 토큰과 같은지 비교
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate());
				String userCmd = "SetInitialCredit";
				// SetInitialCredit 함수인지 체크(web에서 set initial credit 기능의 실제기능은
				// token발행임)
				updateEcgStsLog(userCmd, bd.getMeterId(), 0, "" + token, bd.getAsyncTrId(), bd.getAsyncCreateDate());
				// 기존 send sts token이 실행되었는지 체크
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 0, "" + token, bd.getAsyncTrId(), bd.getAsyncCreateDate());
				// 요청한 토큰과 같은지 비교
				// ..?

				// Async_Result 입력
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "Success");
				map.put("ResponseToken", token);
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
			} catch (STSException e) {
				log.error(e, e);
				String userCmd = "SetInitialCredit";
				updateEcgStsLog(userCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetSTSToken")) {
			try {
				GetSTSTokenRes res = new GetSTSTokenRes(frame);
				String[] dts = res.getDTs();
				String[] tokens = res.getTokens();

				// 토큰 정보를 저장한다.
				for (int i = 0; i < dts.length; i++) {
					createEcgStsLog(bypassCmd, i, bd.getMeterId(), null, dts[i], tokens[i], null, null, null, null,
							null, null, new int[] {}, new double[] {}, new double[] {}, new double[] {},
							new double[] {}, null, null, /* emergency credit */
							0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null);
				}
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetRFSetup")) {
			try {
				SetRFSetupRes res = new SetRFSetupRes(frame);
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (STSException e) {
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetRFSetup")) {
			try {
				GetRFSetupRes res = new GetRFSetupRes(frame);
				// Tariff 정보를 저장한다.
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						null, null, bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, res.getChannel(), res.getPanId());
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetSTSSetup")) {
			try {
				SetSTSSetupRes res = new SetSTSSetupRes(frame);
				updateEcgStsLog(bypassCmd, bd.getMeterId(), res.getResult(), "", bd.getAsyncTrId(),
						bd.getAsyncCreateDate());
			} catch (STSException e) {
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetSTSSetup")) {
			try {
				GetSTSSetupRes res = new GetSTSSetupRes(frame);
				// Tariff 정보를 저장한다.
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, null, bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null, null,
						null, null, null, null, null, null, res.getStsNumber(), null, null, null, null);
				updateSTSNumber(bd.getMeterId(), res.getStsNumber());
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetTariff")) {
			try {
				SetTariffRes res = new SetTariffRes(frame);
				updateEcgStsLog(bypassCmd, bd.getMeterId(), res.getResult(), "", bd.getAsyncTrId(),
						bd.getAsyncCreateDate());

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result Description", "0(Success), Else(Fail)");
				map.put("STS Result", "" + res.getResult());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
			} catch (STSException e) {
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetTariff")) {
			try {
				GetTariffRes res = new GetTariffRes(frame);
				int tariffMode = res.getTariffMode();
				int condLimit1 = res.getCondLimit1();
				int condLimit2 = res.getCondLimit2();
				String yyyymmdd = res.getYyyymmdd();
				int[] cons = res.getCons();
				double[] fixedRate = res.getFixedRate();
				double[] vatRate = res.getVarRate();
				double[] condRate1 = res.getCondRate1();
				double[] condRate2 = res.getCondRate2();

				// Tariff 정보를 저장한다.
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, tariffMode, 0, cons.length, yyyymmdd,
						String.valueOf(condLimit1), String.valueOf(condLimit2), cons, fixedRate, vatRate, condRate1,
						condRate2, null, null, /* emergency credit */
						0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null);

				// Async 결과 저장
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("tariffMode", String.valueOf(tariffMode));
				map.put("tariffDate", yyyymmdd);
				map.put("condLimit1", String.valueOf(condLimit1));
				map.put("condLimit2", String.valueOf(condLimit2));
				// map.put("cons", String.valueOf(cons));
				// map.put("fixedRate", Double.(fixedRate));
				// map.put("varRate", String.valueOf(vatRate));
				// map.put("condRate1", String.valueOf(condRate1));
				// map.put("condRate2", String.valueOf(condRate2));
				addResult(bd.getModemId(), map, bd.getAsyncTrId());

			} catch (STSException e) {
				// Tariff 정보를 저장한다.
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetFriendlyCreditSchedule")) {
			try {
				SetFriendlyCreditScheduleRes res = new SetFriendlyCreditScheduleRes(frame);
				updateEcgStsLog(bypassCmd, bd.getMeterId(), res.getResult(), "", bd.getAsyncTrId(),
						bd.getAsyncCreateDate());
			} catch (STSException e) {
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetFriendlyCreditSchedule")) {
			try {
				GetFriendlyCreditScheduleRes res = new GetFriendlyCreditScheduleRes(frame);
				String yyyymmdd = res.getDate();
				int fcMode = res.getFcMode();
				int[] dayType = res.getDayType();
				String[] fromHHMM = res.getFromTime();
				String[] endHHMM = res.getEndTime();

				// Friendly Credit 스케줄 정보를 저장한다.
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null, null,
						null, yyyymmdd, dayType, fcMode, fromHHMM, endHHMM, null, null, null, null, null);
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetEmergencyCredit")) {
			try {
				SetEmergencyCreditRes res = new SetEmergencyCreditRes(frame);
				updateEcgStsLog(bypassCmd, bd.getMeterId(), res.getResult(), "", bd.getAsyncTrId(),
						bd.getAsyncCreateDate());
			} catch (STSException e) {
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetEmergencyCredit")) {
			try {
				GetEmergencyCreditRes res = new GetEmergencyCreditRes(frame);
				int mode = res.getMode();
				int days = res.getDay();

				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, mode,
						days, /* emergency credit */
						0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null);

				// Emergency Credit 모드를 비교하여 재실행
				validateEmergencyMode(bd.getMeterId(), mode);
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, 0,
						0, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetPreviousMonthNetCharge")) {
			try {
				GetPreviousMonthNetChargeRes res = new GetPreviousMonthNetChargeRes(frame);
				int m_consumption = res.getM_consumption();
				double m_cost = res.getM_cost();

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -1);
				String yyyymm = sdf.format(cal.getTime());

				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, yyyymm, m_consumption, m_cost,
						null, null, null, null, null, null, null, null, null, null, null, null, null);

				// 월정산을 생성해야 한다.
				createMonthlyLog(bd.getMeterId(), yyyymm, m_consumption, m_cost);
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetSpecificMonthNetCharge")) {
			try {
				GetSpecificMonthNetChargeRes res = new GetSpecificMonthNetChargeRes(frame);
				int consumption = res.getM_consumption();
				double cost = res.getM_cost();

				// 특정월의 월정산 비교
				String yyyymm = (String) bd.getArgs().get(0);

				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, "", bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, yyyymm, consumption, cost, null,
						null, null, null, null, null, null, null, null, null, null, null, null);

				// 월정산을 생성해야 한다.
				createMonthlyLog(bd.getMeterId(), yyyymm, consumption, cost);
			} catch (STSException e) {
				createEcgStsLog(bypassCmd, 0, bd.getMeterId(), null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate(), null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
			} catch (Exception e1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Result", "FAIL");
				map.put("Error Message", e1.getMessage());
				addResult(bd.getModemId(), map, bd.getAsyncTrId());
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetMessage")) {
			try {
				SetMessageRes res = new SetMessageRes(frame);
				updateEcgStsLog(bypassCmd, bd.getMeterId(), res.getResult(), "", bd.getAsyncTrId(),
						bd.getAsyncCreateDate());
			} catch (STSException e) {
				updateEcgStsLog(bypassCmd, bd.getMeterId(), 1, e.getMsg(), bd.getAsyncTrId(), bd.getAsyncCreateDate());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		}
		// 펌웨어 업그레이드를 위해 스토리지에 있는 펌웨어 삭제 요청에 대한 응답 수신 후 펌웨어 파일 전송
		else if (bypassCmd.equals("cmdSuniFirmwareUpdateControl_0")) {
			try {
				SuniFirmwareUpdateControlRes res = new SuniFirmwareUpdateControlRes(frame);

				// 정상 응답이 오면 펌웨어 전송 시작
				cmdSuniFirmwareUpdateFileBlockWrite(session);

				// 종룜영령을 안보내기 위해 반환
				return;
			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateFileBlockWrite")) {
			try {
				SuniFirmwareUpdateFileBlockWriteRes res = new SuniFirmwareUpdateFileBlockWriteRes(frame);

				// 정상 응답이 오면 펌웨어 계속 전송
				cmdSuniFirmwareUpdateFileBlockWrite(session);

				// 종룜영령을 안보내기 위해 반환
				return;
			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateControl_2")) {
			try {
				SuniFirmwareUpdateControlRes res = new SuniFirmwareUpdateControlRes(frame);
			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdGetSuniFirmwareUpdateInfo")) {
			try {
				GetSuniFirmwareUpdateInfoRes res = new GetSuniFirmwareUpdateInfoRes(frame);
			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdSuniFirmwareRead")) {
			try {
				GetSuniFirmwareUpdateInfoRes res = new GetSuniFirmwareUpdateInfoRes(frame);
				addResult(bd.getModemId(), res.getDataMap(), bd.getAsyncTrId());
				// 전체 파일 사이즈와 파일을 읽을 위치를 저장한다.
				session.setAttribute("fwfileByteSize", res.getFileByteSize());
				session.setAttribute("fwOffset", 0);
				cmdSuniFirmwareUpdateFileBlockRead(session);

				return;
			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateFileBlockRead")) {
			try {
				SuniFirmwareUpdateFileBlockReadRes res = new SuniFirmwareUpdateFileBlockReadRes(frame);
				session.setAttribute("fwBin." + res.getNumber(), res.getData());

				// 마지막이면 true가 리턴되어 정상 종료할 수 있도록 하고 아니면 다 받을 때까지 실행되어야 함.
				if (!cmdSuniFirmwareUpdateFileBlockRead(session))
					return;
			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateKeyWrite")) {
			try {
				SuniFirmwareUpdateKeyWriteRes res = new SuniFirmwareUpdateKeyWriteRes(frame);

			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateKeyRead")) {
			try {
				SuniFirmwareUpdateKeyReadRes res = new SuniFirmwareUpdateKeyReadRes(frame);

			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdGetCIUCommStateHistory")) {
			try {
				GetCIUCommStateHistoryRes res = new GetCIUCommStateHistoryRes(frame);
			} catch (STSException e) {

			}
		}

		// 명령어에 따라서 단계적으로 실행되어야 한다.
		session.write(new ControlDataFrame(ControlDataConstants.CODE_EOT));
	}

	@Override
	public void execute(String cmd, SMIValue[] smiValues, IoSession session) throws Exception {
		try {
			BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
			// GG 101.2.1 모뎀/미터 식별 응답이 오면 커맨드를 실행한다.
			if (cmd.equals("cmdIdentifyDevice")) {
				bd.setModemId(smiValues[0].getVariable().toString());
				bd.setMeterId(smiValues[1].getVariable().toString());

				// 비동기 내역을 조회한다.
				HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil
						.getBean("transactionManager");
				TransactionStatus txstatus = null;
				AsyncCommandLogDao acld = DataUtil.getBean(AsyncCommandLogDao.class);
				ModemDao modemDao = DataUtil.getBean(ModemDao.class);
				List<AsyncCommandLog> acllist = null;
				try {
					txstatus = txmanager.getTransaction(null);
					Modem modem = modemDao.get(bd.getModemId());

					if (modem != null) {
						modem.setLastLinkTime(DateTimeUtil.getDateString(new Date()));

						// 2017.05.17
						// IMSI, ICC-ID cannot be gotton from modem.
						modem.setIpAddr(
								(((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress()));
						log.debug("Modem[" + modem.getDeviceSerial() + "] IP_ADDR[" + modem.getIpAddr() + "]");
						modemDao.update(modem);

						// 모뎀/미터 시리얼 번호로 조회 (최근에 실행한 순서대로 불러옴)
						Map<String, Object> mapCondition = new HashMap<String, Object>();
						mapCondition.put("meterId", bd.getMeterId());
						mapCondition.put("modemId", bd.getModemId());
						mapCondition.put("state", TR_STATE.Waiting.getCode());

						acllist = acld.getLogListByCondition(mapCondition);

						log.info("ASYNC_SIZE[" + acllist.size() + "]");
						if (acllist.size() > 0) {
							// 명령 건수가 1개여야 한다. 어차피 여러개의 명령 요청이 있더라도 한번 하고 나면
							// 끊어지기 때문에 연속으로 처리할 수 없다.
							AsyncCommandLog acl = null;

							// Async 테이블에서 이전 명령들을 Terminate 처리한다 (동일한 명령만)
							mapCondition = new HashMap<String, Object>();
							mapCondition.put("meterId", bd.getMeterId());
							mapCondition.put("modemId", bd.getModemId());
							mapCondition.put("command", acllist.get(0).getCommand());
							mapCondition.put("state", TR_STATE.Waiting.getCode());

							acllist = acld.getLogListByCondition(mapCondition);
							acl = null;
							for (int i = 0; i < acllist.size(); i++) {
								acl = acllist.get(i);
								if (i == 0)
									acl.setState(TR_STATE.Success.getCode());
								else
									acl.setState(TR_STATE.Terminate.getCode());
								acld.update(acl);
							}
							// 마지막 커맨드를 실행한다.
							acl = acllist.get(0);

							// 비동기내역의 트랜잭션 아이디를 넘긴다.
							bd.setAsyncTrId(acl.getTrId());
							bd.setAsyncCreateDate(acl.getCreateTime());

							bd.setFwVersion(modem.getFwVer());

							Set<Condition> condition = new HashSet<Condition>();
							condition.add(
									new Condition("id.trId", new Object[] { acl.getTrId() }, null, Restriction.EQ));
							condition.add(
									new Condition("id.mcuId", new Object[] { acl.getMcuId() }, null, Restriction.EQ));
							condition.add(new Condition("id.num", null, null, Restriction.ORDERBY));
							AsyncCommandParamDao acpd = DataUtil.getBean(AsyncCommandParamDao.class);
							List<AsyncCommandParam> acplist = acpd.findByConditions(condition);

							for (AsyncCommandParam p : acplist.toArray(new AsyncCommandParam[0])) {
								bd.addArg(p.getParamValue());
							}

							session.setAttribute(session.getRemoteAddress(), bd);

							cmd = acl.getCommand();
							log.info("MeterId[" + bd.getMeterId() + " ModemId[" + bd.getModemId() + "] CMD[" + cmd
									+ "]");
							Method method = this.getClass().getMethod(acl.getCommand(), IoSession.class);

							txmanager.commit(txstatus);
							method.invoke(this, session);
						} else {
							txmanager.commit(txstatus);
							// 실행할 명령이 없으면 EOT 호출하고 종료
							session.write(new ControlDataFrame(ControlDataConstants.CODE_EOT));
						}
					} else {
						txmanager.commit(txstatus);
						// 실행할 명령이 없으면 EOT 호출하고 종료
						session.write(new ControlDataFrame(ControlDataConstants.CODE_EOT));
					}
				} catch (Exception e) {
					log.error(e, e);
					if (txstatus != null && !txstatus.isCompleted())
						txmanager.rollback(txstatus);
				}
			} else if (cmd.equals("gprsOTAStart")) {

				cmd = "cmdOTAStart";
				log.debug("[gprsOTAOnUploadChannel] CMD: " + cmd);

				Method method = this.getClass().getMethod(cmd, IoSession.class);
				method.invoke(this, session);
			} else if (cmd.equals("cmdOTAStart")) {
				log.debug("modemId[" + bd.getModemId() + "] meterId[" + bd.getMeterId() + "]");

				bd.setModemModel(smiValues[0].getVariable().toString());
				bd.setFwVersion(smiValues[1].getVariable().toString());
				bd.setBuildno(smiValues[2].getVariable().toString());
				bd.setHwVersion(smiValues[3].getVariable().toString());
				bd.setPacket_size(Integer.parseInt(smiValues[4].getVariable().toString()));

				session.setAttribute(session.getRemoteAddress(), bd);

				// TODO 위 정보를 모뎀에 갱신한다.
				cmdSendImage(session);
			} else if (cmd.equals("cmdSendImage")) {
				cmdSendImage(session);
			} else if (cmd.equals("cmdOTAEnd")) {
				// 상태값을 받아서 실패하면 다시 시도하도록 한다.
				int status = Integer.parseInt(smiValues[0].getVariable().toString());
			} else if (cmd.equals("cmdReadModemConfiguration")) {
				// 모뎀 설정 정보를 갱신한다.

				Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
				// valueMap.put("0", "No Data");

				// 모뎀 정보를 로깅하기 위해 추가했으나 이벤트로 전달되기 때문에 로깅을 하지 않는다.
				/*for (int i = 0; i < smiValues.length; i++) {
					valueMap.put("i", smiValues[i].getVariable().toString());
					log.info(smiValues[i].getVariable().toString());
				}*/
				
				log.info("evtEUI: " + smiValues[0].getVariable().toString());
				log.info("evtTime: " + smiValues[1].getVariable().toString());
				log.info("evtResetInterval: " + smiValues[2].getVariable().toString());
				log.info("evtMeteringInterval: " + smiValues[3].getVariable().toString());
				log.info("evtServerIP: " + smiValues[4].getVariable().toString());
				log.info("evtServerPort: " + smiValues[5].getVariable().toString());
				log.info("evtAPNAddress: " + smiValues[6].getVariable().toString());
				log.info("evtModelName: " + smiValues[9].getVariable().toString());
				log.info("evtFwVer: " + smiValues[10].getVariable().toString());
				log.info("evtBuildNumber: " + smiValues[11].getVariable().toString());
				log.info("evtHwVer: " + smiValues[12].getVariable().toString());
				log.info("evtGprsIMEI: " + smiValues[13].getVariable().toString());
				log.info("evtSimIMSI: " + smiValues[14].getVariable().toString());
				log.info("evtMeterID: " + smiValues[15].getVariable().toString());
				log.info("evtMeterCommStatus: " + smiValues[16].getVariable().toString());
				log.info("evtStsNumber: " + smiValues[17].getVariable().toString());
				log.info("evtBalance: " + smiValues[18].getVariable().toString());
				log.info("evtGPRSStatus: " + smiValues[19].getVariable().toString());
				log.info("evtSimICCID: " + smiValues[20].getVariable().toString());
				
				valueMap.put("evtEUI", smiValues[0].getVariable().toString());
				valueMap.put("evtTime", smiValues[1].getVariable().toString());
				valueMap.put("evtResetInterval", smiValues[2].getVariable().toString());
				valueMap.put("evtMeteringInterval", smiValues[3].getVariable().toString());
				valueMap.put("evtServerIP", smiValues[4].getVariable().toString());
				valueMap.put("evtServerPort", smiValues[5].getVariable().toString());
				valueMap.put("evtAPNAddress", smiValues[6].getVariable().toString());
				valueMap.put("evtModelName", smiValues[9].getVariable().toString());
				valueMap.put("evtFwVer", smiValues[10].getVariable().toString());
				valueMap.put("evtBuildNumber", smiValues[11].getVariable().toString());
				valueMap.put("evtHwVer", smiValues[12].getVariable().toString());
				valueMap.put("evtGprsIMEI", smiValues[13].getVariable().toString());
				valueMap.put("evtSimIMSI", smiValues[14].getVariable().toString());
				valueMap.put("evtMeterID", smiValues[15].getVariable().toString());
				valueMap.put("evtMeterCommStatus", smiValues[16].getVariable().toString());
				valueMap.put("evtStsNumber", smiValues[17].getVariable().toString());
				valueMap.put("evtBalance", smiValues[18].getVariable().toString());
				valueMap.put("evtGPRSStatus", smiValues[19].getVariable().toString());
				valueMap.put("evtSimICCID", smiValues[20].getVariable().toString());
				
				addResult(bd.getModemId(), valueMap, bd.getAsyncTrId());
			} else if (cmd.equals("cmdRelayStatus")) {
				int status = Integer.parseInt(smiValues[0].getVariable().toString());
				setMeterStatus(((BypassDevice) session.getAttribute(session.getRemoteAddress())).getMeterId(), status);

				// 비동기내역저장. 위에 있는 setMeterStatus 참조하여 설정함.
				Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
				if (status == 1 || status == 8) {
					valueMap.put("Status", MeterStatus.CutOff.name());
				} else {
					valueMap.put("Status", MeterStatus.Normal.name());
				}
				addResult(bd.getModemId(), valueMap, bd.getAsyncTrId());
			} else if (cmd.equals("cmdRelayDisconnect")) {
				int status = Integer.parseInt(smiValues[0].getVariable().toString());
				setMeterStatus(((BypassDevice) session.getAttribute(session.getRemoteAddress())).getMeterId(), status);
			} else if (cmd.equals("cmdRelayReconnect")) {
				int status = Integer.parseInt(smiValues[0].getVariable().toString());
				setMeterStatus(((BypassDevice) session.getAttribute(session.getRemoteAddress())).getMeterId(), status);
			} else if (cmd.equals("cmdUploadMeteringData")) {
				OPAQUE mdv = (OPAQUE) smiValues[0].getVariable();
				log.debug("Get Meter : return ClassName[" + mdv.getClsName() + "] MIB[" + mdv.getMIBName() + "]");

				MDData mdData = new MDData(new WORD(1));
				mdData.setMcuId("0");
				mdData.setMdData(mdv.encode());
				ProcessorHandler handler = DataUtil.getBean(ProcessorHandler.class);
				handler.putServiceData(ProcessorHandler.SERVICE_MEASUREMENTDATA, mdData);
			} else if (cmd.equals("gprsTimeSyncOnUploadChannel")) { // OPF-179
				HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil
						.getBean("transactionManager");
				TransactionStatus txstatus = null;
				try {
					txstatus = txmanager.getTransaction(null);
					String xmlFilePath = FMPProperty.getProperty("gprs.uploadchannel.properties.path", "-");
					String[] key = { "targetFirmwareVer", "threshold" };
					String[] properties = XMLProperty.getProperties(xmlFilePath, "gprsTimeSyncOnUploadChannel", key);

					if (properties == null) {
						log.debug("Properties is null!!!");
						log.debug(
								"################ GPRS Modem meterTimeSync on metering data upload channel (End) ################");
						return;
					}

					String deviceSerial = smiValues[0].getVariable().toString();
					String targetFirmwareVer = properties[0];
					String threshold = properties[1];

					log.debug("[gprsTimeSyncOnUploadChannel] Device Serial:           " + deviceSerial);
					log.debug("[gprsTimeSyncOnUploadChannel] Target Firmware Version: " + targetFirmwareVer);
					log.debug("[gprsTimeSyncOnUploadChannel] Threshold:               " + threshold);

					String[] fwVer = targetFirmwareVer.split(",");

					// MeterTimeSync 대상 체크
					ModemDao modemDao = DataUtil.getBean(ModemDao.class);
					Set<Condition> condition = new HashSet<Condition>();
					condition.add(new Condition("deviceSerial", new Object[] { deviceSerial }, null, Restriction.EQ));
					condition.add(new Condition("protocolType", new Object[] { Protocol.SMS }, null, Restriction.EQ));
					condition.add(new Condition("fwVer", fwVer, null, Restriction.IN));
					List<Modem> list = modemDao.findByConditions(condition);

					if (!list.isEmpty()) {
						Modem modem = list.get(0);
						Set<Meter> meters = modem.getMeter();
						Iterator<Meter> it = meters.iterator();
						boolean timeSync = false;
						Meter meter = null;
						while (it.hasNext()) {
							meter = it.next();
							log.debug("Meter: " + meter.getMdsId() + ", TimeDiff: " + meter.getTimeDiff());
							if (meter.getTimeDiff() != null) {
								if (meter.getTimeDiff() >= Long.parseLong(threshold)
										|| meter.getTimeDiff() <= -Long.parseLong(threshold))
									timeSync = true;
							} else
								timeSync = true; // timeDiff가 null이여도
													// timeSync실행.
						}

						if (timeSync == true) {
							cmd = "cmdSetMeterTime";
							log.debug("[gprsTimeSyncOnUploadChannel] CMD: " + cmd);
							bd.setModemId(deviceSerial);
							session.setAttribute(session.getRemoteAddress(), bd);
							Method method = this.getClass().getMethod(cmd, IoSession.class);
							txmanager.commit(txstatus);
							method.invoke(this, session);
						} else {
							txmanager.commit(txstatus);
							log.debug("[gprsTimeSyncOnUploadChannel] TimeDiff is normal range");
							log.debug(
									"################ GPRS Modem MeterTimeSync on metering data upload channel (End) ################");
						}
					} else {
						log.debug("[gprsTimeSyncOnUploadChannel] This Device[" + deviceSerial
								+ "]  is not a MeterTimeSync Target.");
						txmanager.commit(txstatus);
						log.debug(
								"################ GPRS Modem MeterTimeSync on metering data upload channel (End) ################");
					}
				} catch (Exception e) {
					log.error(e, e);
					if (txstatus != null && !txstatus.isCompleted())
						txmanager.rollback(txstatus);
				}
			} else if (cmd.equals("cmdSetMeterTime")) {
				cmdSetMeterTime(session);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	@Override
	public void executeBypass2(byte[] frame, IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String bypassCmd = (String) session.getAttribute("bypassCmd");
		String mdsId = session.getAttribute("meterId").toString();
		String createDate = DateTimeUtil.getDateString(new Date());
		Long trId = Long.parseLong(createDate);
		log.debug(bypassCmd);

		if (bypassCmd.equals("cmdSetPaymentMode")) {
			try {
				SetPaymentModeRes res = new SetPaymentModeRes(frame);
				
				if (res.getResult() == 0) {
					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E) 
				}
				
				int mode = 0;
				if (bd.getArgs().size() > 0) {
					if (bd.getArgs().get(0) instanceof String)
						mode = Integer.parseInt((String) bd.getArgs().get(0));
					else if (bd.getArgs().get(0) instanceof Integer)
						mode = (Integer) bd.getArgs().get(0);
				} else
					throw new Exception("check mode, there is no mode");
			
				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, mode, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						res.getResult(), null, trId, createDate, null,
						null, /* remaining credit */
						null, null, null, null, null, null, /* net charge */
						null, null, null, null, null, /* friendly credit */
						null, null, null, null, null);
				// STS log 저장 (E)
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetPaymentMode")) {
			try {
				GetPaymentModeRes res = new GetPaymentModeRes(frame);
				int mode = res.getMode();
				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("mode", mode);
				// set response data (E)
				
				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, mode, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, null, trId, createDate, null,
						null, /* remaining credit */
						null, null, null, null, null, null, /* net charge */
						null, null, null, null, null, /* friendly credit */
						null, null, null, null, null);
				// STS log 저장 (E)
				
				// mode를 비교하여 틀리면 SetPaymentModeReq 실행
				// validatePaymentMode(bd.getMeterId(), mode);
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetRemainingCredit")) {
			try {
				GetRemainingCreditRes res = new GetRemainingCreditRes(frame);
				String date = res.getDt();
				double credit = res.getCredit();

				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("dateTime", date);
				session.setAttribute("credit", credit);
				// set response data (E)

				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null, new int[] {},
						new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, null, trId, createDate, date + "00",
						credit, /* remaining credit */
						null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)

				updateCurrentCredit(mdsId, date, credit); // 현재 잔액을 갱신한다.
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetSTSToken")) {
			try {
				SetSTSTokenRes res = new SetSTSTokenRes(frame);
				String token = res.getToken();
				
				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("token", token);
				// set response data (E)
				
				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, null, createDate, token, null, null, null, null,
						null, null, new int[] {}, new double[] {}, new double[] {}, new double[] {},
						new double[] {}, null, null, /* emergency credit */
						0, "", trId, createDate, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetSTSToken")) { /** multi frame */
			try {
				// response length info (S)
				final int frameInterface_len = 16; // KMP_SOF(1) + KMP_ADDR(8) + KMP_CID(2) + Reponse(CMD(1) + RESULT(1)) + KMP_CRC(2) + KMP_EOF(1)
				
				final int tCount_len = 1;
				final int token_len = 20;
				final int dateTime_len = 6;
				int getCount = 0;
				
				if (session.getAttribute("getCount") instanceof String) {
					getCount = Integer.parseInt((String) session.getAttribute("getCount"));
				} else {
					getCount = (Integer) session.getAttribute("getCount");
				}
				
				totalLen = frameInterface_len + tCount_len + (token_len * getCount) + (dateTime_len * getCount);
				// response length info (E)
				
				// check command result code (S)
				// When the first iteration, check the result code. (success: 00, fail: 01)
				// KMP_SOF(1) | KMP_ADDR(8) | KMP_CID(2) | CMD(1) | Result(1)
				if (totalReceivedFrameStr.equals("")) {
					if(Hex.decode(frame).substring(24, 26).equals("01")) {
						byte[] totalFrame = new java.math.BigInteger(Hex.decode(frame), 16).toByteArray();
						GetTariffRes res = new GetTariffRes(totalFrame);
						// return STSException e
					}
				}
				// check command result code (E)
				
				// de-stuffing (S)
				log.info("############################################################");
				byte[] destuff_data = new byte[frame.length];
				System.arraycopy(frame, 0, destuff_data, 0, destuff_data.length);
				log.info("#################### [" + cycleCnt + "] ####################");
				log.info("Received Frame Lenth[" + frame.length + "] Received Frame [" + Hex.decode(frame) + "]");

				destuff_data = stsDataFrame.destuff(frame);
				log.info("After Destuff Frame Lenth[" + destuff_data.length + "] Received Frame [" + Hex.decode(destuff_data) + "]");
				log.info("############################################################");
				cycleCnt++;
				// de-stuffing (E)
				
				if (totalLen != totalReceivedFrameStr.length()) {	
					totalReceivedFrameStr += Hex.decode(destuff_data);
					totalReceivedFrameLen += destuff_data.length;
				}
				
				if (totalLen == totalReceivedFrameLen) {
					log.info("############################################################");
					log.info("Total Frame Length [" + totalLen + "], Received Frame Length [" + totalReceivedFrameLen + "]");
					log.info("############################################################");
					
					byte[] totalFrame = new java.math.BigInteger(totalReceivedFrameStr, 16).toByteArray();

					GetSTSTokenRes res = new GetSTSTokenRes(totalFrame);
					String[] dts = res.getDTs();
					String[] tokens = res.getTokens();

					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);

					int count = dts.length;
					session.setAttribute("count", count);
					
					for (int i = 0; i < count; i++) {
						session.setAttribute("dts["+i+"]", dts[i]);
						session.setAttribute("tokens["+i+"]", tokens[i]);
		            }
					// set response data (E) 
					
					// STS log 저장 (S)
					for (int i = 0; i < dts.length; i++) {
					createEcgStsLog(bypassCmd, i, mdsId, null, dts[i], tokens[i], null, null, null, null,
							null, null, new int[] {}, new double[] {}, new double[] {}, new double[] {},
							new double[] {}, null, null, /* emergency credit */
							0, "", trId, createDate, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null);
					}
					// STS log 저장 (E)
				}
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetSTSSetup")) {
			try {
				SetSTSSetupRes res = new SetSTSSetupRes(frame);
				
				if (res.getResult() == 0) {
					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E) 
				}
				
				// STS log 저장 (S)
				String date = (String) bd.getArgs().get(0);
				String tokenKey1 = (String) bd.getArgs().get(1);
				String tokenKey2 = (String) bd.getArgs().get(2);
				String stsNumber = (String) bd.getArgs().get(3);
				
				createEcgStsLog(bypassCmd, 0, mdsId, null, date, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						res.getResult(), null, trId, createDate, null, null, null, null, null, null, null,
						null, null, null, null, null, null, stsNumber, tokenKey1, tokenKey2, null, null);
				// STS log 저장 (E)
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetSTSSetup")) {
			try {
				GetSTSSetupRes res = new GetSTSSetupRes(frame);
				
				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("date", res.getDate());
				session.setAttribute("stsNumber", res.getStsNumber());
				// set response data (E) 
				
				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, null, trId, createDate, null, null, null, null, null, null, null,
						null, null, null, null, null, null, res.getStsNumber(), null, null, null, null);
				// STS log 저장 (E)
				
				updateSTSNumber(bd.getMeterId(), res.getStsNumber());
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetTariff")) {
			try {
				SetTariffRes res = new SetTariffRes(frame);

				if (res.getResult() == 0) {
					// set response data (S)
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E)
				}

				// STS log 저장 (S)
				String yyyymmdd = null;
//				int condLimit1 = Integer.parseInt(FMPProperty.getProperty("ecg.tariff.gov.subsidy.limit", "0"));
//				int condLimit2 = Integer.parseInt(FMPProperty.getProperty("ecg.tariff.utility.relief.limit", "0"));
				int condLimit1 = 0;
				int condLimit2 = 0;
				
				int[] cons = null;
				double[] fixedRate = new double[] { 0.0, 0.0, 0.0, 0.0 };
				double[] varRate = null;
				String s_cons = null; // 여러개의 사용량이 ,를 이용하여 문자열로 넘어온다.
				String s_fixedRate = null; // 여러개의 요금이 ,를 이용하여 문자열로 넘어온다.
				String s_varRate = null;
				double[] condRate1 = null;
				double[] condRate2 = null;
				String s_condRate1 = null;
				String s_condRate2 = null;

				int count = 0;
				if (bd.getArgs().size() == 8) {
					yyyymmdd = (String) bd.getArgs().get(0);

					if (bd.getArgs().get(1) instanceof String) {
						condLimit1 = Integer.parseInt((String) bd.getArgs().get(1));
					} else if (bd.getArgs().get(1) instanceof Integer) {
						condLimit1 = (Integer) bd.getArgs().get(1);
					}

					if (bd.getArgs().get(2) instanceof String) {
						condLimit2 = Integer.parseInt((String) bd.getArgs().get(2));
					} else if (bd.getArgs().get(2) instanceof Integer) {
						condLimit2 = (Integer) bd.getArgs().get(2);
					}

					s_cons = (String) bd.getArgs().get(3);
					s_fixedRate = (String)bd.getArgs().get(4);
					s_varRate = (String) bd.getArgs().get(5);
					s_condRate1 = (String) bd.getArgs().get(6);
					s_condRate2 = (String) bd.getArgs().get(7);
					
					// ECGSetSTSTariffTask - Bulk Parameters set (S)
					if (s_cons.contains("/")) {
						s_cons = s_cons.replace("/", ",");
					} 
					
					if (s_fixedRate.contains("/")) {
						s_fixedRate = s_fixedRate.replace("/", ",");
					}
					
					if (s_varRate.contains("/")) {
						s_varRate = s_varRate.replace("/", ",");
					} 
					
					if (s_condRate1.contains("/")) {
						s_condRate1 = s_condRate1.replace("/", ",");
					} 
					
					if (s_condRate2.contains("/")) {
						s_condRate2 = s_condRate2.replace("/", ",");
					}
					// ECGSetSTSTariffTask - Bulk Parameters set (E)

					StringTokenizer st = new StringTokenizer(s_cons, ",");
					count = st.countTokens();
					cons = new int[count];

					int i = 0;
					while (st.hasMoreTokens()) {
						cons[i++] = Integer.parseInt(st.nextToken());
					}

					/**
					 * fixed rate는 0으로 설정해야 한다. 2017.11.13 by elevas st = new
					 * StringTokenizer(s_fixedRate, ","); fixedRate = new double[count];
					 * i = 0; while (st.hasMoreTokens()) { fixedRate[i++] =
					 * Double.parseDouble(st.nextToken()); }
					 */

					st = new StringTokenizer(s_varRate, ",");
					varRate = new double[count];
					i = 0;
					while (st.hasMoreTokens()) {
						varRate[i++] = Double.parseDouble(st.nextToken());
					}

					st = new StringTokenizer(s_condRate1, ",");
					condRate1 = new double[count];
					i = 0;
					while (st.hasMoreTokens()) {
						condRate1[i++] = Double.parseDouble(st.nextToken());
					}

					st = new StringTokenizer(s_condRate2, ",");
					condRate2 = new double[count];
					i = 0;
					while (st.hasMoreTokens()) {
						condRate2[i++] = Double.parseDouble(st.nextToken());
					}
				} else
					throw new Exception("Check tariff argement, wrong size");
				
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, cons.length, yyyymmdd,
						String.valueOf(condLimit1), String.valueOf(condLimit2), cons, fixedRate, varRate, condRate1,
						condRate2, null, null, /* emergency credit */
						res.getResult(), "", trId, createDate, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetTariff")) { /** multi frame */
			try {
				// response length info (S)
				final int frameInterface_len = 16; // KMP_SOF(1) + KMP_ADDR(8) + KMP_CID(2) + Reponse(CMD(1) + RESULT(1)) + KMP_CRC(2) + KMP_EOF(1)
				
				final int tariff_mode_len = 1;
				final int apply_date_len = 4;
				final int cond_limit_1_len = 2;
				final int cond_limit_2_len = 2;
				final int count_len = 1;

				final int cons_len = 2;
				final int fixed_rate_len = 5;
				final int var_rate_len = 5;
				final int cond_rate_1_len = 5;
				final int cond_rate_2_len = 5;
				int getCount = 0;
				// response length info (E)
				
				// check command result code (S)
				// When the first iteration, check the result code. (success: 00, fail: 01)
				// KMP_SOF(1) | KMP_ADDR(8) | KMP_CID(2) | CMD(1) | Result(1)
				if (totalReceivedFrameStr.equals("")) {
					if(Hex.decode(frame).substring(24, 26).equals("01")) {
						byte[] totalFrame = new java.math.BigInteger(Hex.decode(frame), 16).toByteArray();
						GetTariffRes res = new GetTariffRes(totalFrame);
						// return STSException e
					}
				}
				// check command result code (E)
				
				// de-stuffing (S)
				log.info("############################################################");
				byte[] destuff_data = new byte[frame.length];
				System.arraycopy(frame, 0, destuff_data, 0, destuff_data.length);
				log.info("#################### [" + cycleCnt + "] ####################");
				log.info("Received Frame Lenth[" + frame.length + "] Received Frame [" + Hex.decode(frame) + "]");

				destuff_data = stsDataFrame.destuff(frame);
				log.info("After Destuff Frame Lenth[" + destuff_data.length + "] Received Frame [" + Hex.decode(destuff_data) + "]");
				log.info("############################################################");
				cycleCnt++;
				// de-stuffing (E)
				
				// first iteration - get repeat count & total frame length
				if (totalReceivedFrameStr.equals("")) {
					getCount = Integer.parseInt(Hex.decode(destuff_data).substring(44, 46));
					
					totalLen = frameInterface_len + tariff_mode_len + apply_date_len + cond_limit_1_len + cond_limit_2_len + count_len
							+ ((cons_len * getCount) + (fixed_rate_len * getCount) + (var_rate_len * getCount) + (cond_rate_1_len * getCount) + (cond_rate_2_len * getCount));
				}
				
				if (totalLen != totalReceivedFrameStr.length()) {	
					totalReceivedFrameStr += Hex.decode(destuff_data);
					totalReceivedFrameLen += destuff_data.length;
				}
				
				if (totalLen == totalReceivedFrameLen) {
					log.info("############################################################");
					log.info("Total Frame Length [" + totalLen + "], Received Frame Length [" + totalReceivedFrameLen + "]");
					log.info("############################################################");
					
					byte[] totalFrame = new java.math.BigInteger(totalReceivedFrameStr, 16).toByteArray();
					
					GetTariffRes res = new GetTariffRes(totalFrame);
					
					int tariffMode = res.getTariffMode();
					int condLimit1 = res.getCondLimit1();
					int condLimit2 = res.getCondLimit2();
					String yyyymmdd = res.getYyyymmdd();
					int[] cons = res.getCons();
					double[] fixedRate = res.getFixedRate();
					double[] vatRate = res.getVarRate();
					double[] condRate1 = res.getCondRate1();
					double[] condRate2 = res.getCondRate2();

					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					
					session.setAttribute("tariffMode", tariffMode);
					session.setAttribute("yyyymmdd", yyyymmdd);
					session.setAttribute("condLimit1", String.valueOf(condLimit1));
					session.setAttribute("condLimit2", String.valueOf(condLimit2));

					int count = res.getCount();
					session.setAttribute("count", count);

					for (int i = 0; i < count; i++) {
						session.setAttribute("cons[" + i + "]", cons[i]);
						session.setAttribute("fixedRate[" + i + "]", fixedRate[i]);
						session.setAttribute("vatRate[" + i + "]", vatRate[i]);
						session.setAttribute("condRate1[" + i + "]", condRate1[i]);
						session.setAttribute("condRate2[" + i + "]", condRate2[i]);
					}
					// set response data (E) 
					
					// STS log 저장 (S)
					createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, tariffMode, 0, cons.length, yyyymmdd,
							String.valueOf(condLimit1), String.valueOf(condLimit2), cons, fixedRate, vatRate, condRate1,
							condRate2, null, null, /* emergency credit */
							0, "", trId, createDate, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null);
					// STS log 저장 (E)
				}
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetFriendlyCreditSchedule")) {
			try {
				SetFriendlyCreditScheduleRes res = new SetFriendlyCreditScheduleRes(frame);
				
				if (res.getResult() == 0) {
					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E) 
				}
				
				// STS log 저장 (S)
				String yyyymmdd = null;
				int[] dayType = null;
				String[] fromHHMM = null;
				String[] endHHMM = null;
				String s_dayType = null;  // dayType은 여러개의 값이 ,를 이용하여 문자열로 넘어온다.
				String s_fromHHMM = null; // fromHHMM은 여러개의 값이 ,를 이용하여 문자열로 넘어온다.
				String s_endHHMM = null;  // endHHMM은 여러개의 값이 ,를 이용하여 문자열로 넘어온다.
				int count = 0;
				if (bd.getArgs().size() == 4) {
					yyyymmdd = (String) bd.getArgs().get(0);
					s_dayType = (String) bd.getArgs().get(1);
					s_fromHHMM = (String) bd.getArgs().get(2);
					s_endHHMM = (String) bd.getArgs().get(3);

					StringTokenizer st = new StringTokenizer(s_dayType, ",");
					count = st.countTokens();
					dayType = new int[count];

					int i = 0;
					while (st.hasMoreTokens()) {
						dayType[i++] = Integer.parseInt(st.nextToken());
					}

					st = new StringTokenizer(s_fromHHMM, ",");
					fromHHMM = new String[count];
					i = 0;
					while (st.hasMoreTokens()) {
						fromHHMM[i++] = st.nextToken();
					}

					st = new StringTokenizer(s_endHHMM, ",");
					endHHMM = new String[count];
					i = 0;
					while (st.hasMoreTokens()) {
						endHHMM[i++] = st.nextToken();
					}
				} else
					throw new Exception("Check friendly credit, wrong size");
				
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, "", trId, createDate, null, null, null, null, null, null, null,
						null, yyyymmdd, dayType, null, fromHHMM, endHHMM, null, null, null, null, null);
				// STS log 저장 (E)
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetFriendlyCreditSchedule")) { /** multi frame */ 
			try {
				// response length info (S)
				final int frameInterface_len = 16; // KMP_SOF(1) + KMP_ADDR(8) + KMP_CID(2) + Reponse(CMD(1) + RESULT(1)) + KMP_CRC(2) + KMP_EOF(1)
				final int fc_mode_len = 1;
				final int e_dt_len = 4;
				final int count_len = 1;

				final int daytype_len = 1;
				final int fromtime_len = 2;
				final int endtime_len = 2;
				int getCount = 0;
				// response length info (E)
				
				// check command result code (S)
				// When the first iteration, check the result code. (success: 00, fail: 01)
				// KMP_SOF(1) | KMP_ADDR(8) | KMP_CID(2) | CMD(1) | Result(1)
				if (totalReceivedFrameStr.equals("")) {
					if(Hex.decode(frame).substring(24, 26).equals("01")) {
						byte[] totalFrame = new java.math.BigInteger(Hex.decode(frame), 16).toByteArray();
						GetFriendlyCreditScheduleRes res = new GetFriendlyCreditScheduleRes(totalFrame);
						// return STSException e
					}
				}
				// check command result code (E)
				
				// de-stuffing (S)
				log.info("############################################################");
				byte[] destuff_data = new byte[frame.length];
				System.arraycopy(frame, 0, destuff_data, 0, destuff_data.length);
				log.info("#################### [" + cycleCnt + "] ####################");
				log.info("Received Frame Lenth[" + frame.length + "] Received Frame [" + Hex.decode(frame) + "]");

				destuff_data = stsDataFrame.destuff(frame);
				log.info("After Destuff Frame Lenth[" + destuff_data.length + "] Received Frame [" + Hex.decode(destuff_data) + "]");
				log.info("############################################################");
				cycleCnt++;
				// de-stuffing (E)
				
				// first iteration - get repeat count & total frame length
				if (totalReceivedFrameStr.equals("")) {
					getCount = Integer.parseInt(Hex.decode(destuff_data).substring(36, 38));
					
					totalLen = frameInterface_len + fc_mode_len + e_dt_len + count_len
							+ ((daytype_len * getCount) + (fromtime_len * getCount) + (endtime_len * getCount));  
				}
				
				if (totalLen != totalReceivedFrameStr.length()) {	
					totalReceivedFrameStr += Hex.decode(destuff_data);
					totalReceivedFrameLen += destuff_data.length;
				}
				
				if (totalLen == totalReceivedFrameLen) {
					log.info("############################################################");
					log.info("Total Frame Length [" + totalLen + "], Received Frame Length [" + totalReceivedFrameLen + "]");
					log.info("############################################################");
					
					byte[] totalFrame = new java.math.BigInteger(totalReceivedFrameStr, 16).toByteArray();
					
					GetFriendlyCreditScheduleRes res = new GetFriendlyCreditScheduleRes(frame);
					String yyyymmdd = res.getDate();
					int fcMode = res.getFcMode();
					int[] dayType = res.getDayType();
					String[] fromHHMM = res.getFromTime();
					String[] endHHMM = res.getEndTime();

					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					
					session.setAttribute("yyyymmdd", yyyymmdd);
					session.setAttribute("fcMode", fcMode);
					
					int count = res.getCount();
					session.setAttribute("count", count);
					
		            for (int i = 0; i < count; i++) {
		            	session.setAttribute("dayType["+i+"]", dayType[i]);
		            	session.setAttribute("fromHHMM["+i+"]", fromHHMM[i]);
		            	session.setAttribute("endHHMM["+i+"]", endHHMM[i]);
		            }
					// set response data (E) 
					
					// STS log 저장 (S)
					createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
							new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
							null, /* emergency credit */
							0, "", trId, createDate, null, null, null, null, null, null, null,
							null, yyyymmdd, dayType, fcMode, fromHHMM, endHHMM, null, null, null, null, null);
					// STS log 저장 (E)
				}
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetEmergencyCredit")) {
			try {
				SetEmergencyCreditRes res = new SetEmergencyCreditRes(frame);
				
				if (res.getResult() == 0) {
					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E) 
				}
				
				// STS log 저장 (S)
				int mode = 0;
				int days = 0;
				if (bd.getArgs().size() > 0) {
					if (bd.getArgs().get(0) instanceof String)
						mode = Integer.parseInt((String) bd.getArgs().get(0));
					else if (bd.getArgs().get(0) instanceof Integer)
						mode = (Integer) bd.getArgs().get(0);

					if (bd.getArgs().size() > 1) {
						if (bd.getArgs().get(1) instanceof String)
							days = Integer.parseInt((String) bd.getArgs().get(1));
						else if (bd.getArgs().get(1) instanceof Integer)
							days = (Integer) bd.getArgs().get(1);
					}
				} else
					throw new Exception("Check mode, there is no mode");
				
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, mode,
						days, /* emergency credit */
						res.getResult(), "", trId, createDate, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetEmergencyCredit")) {
			try {
				GetEmergencyCreditRes res = new GetEmergencyCreditRes(frame);
				int mode = res.getMode();
				int days = res.getDay();

				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("mode", mode);
				session.setAttribute("days", days);
				// set response data (E) 
				
				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, mode,
						days, /* emergency credit */
						0, "", trId, createDate, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)
				
				// validateEmergencyMode(mdsId, mode); // Emergency Credit 모드를 비교하여 재실행
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetPreviousMonthNetCharge")) {
			try {
				GetPreviousMonthNetChargeRes res = new GetPreviousMonthNetChargeRes(frame);
				int m_consumption = res.getM_consumption();
				double m_cost = res.getM_cost();
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -1);
				String yyyymm = sdf.format(cal.getTime());
				
				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("m_consumption", m_consumption);
				session.setAttribute("m_cost", m_cost);
				// set response data (E) 
				
				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, "", trId, createDate, null, null, yyyymm, m_consumption, m_cost,
						null, null, null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)
				
				// 월정산 생성
				createMonthlyLog(mdsId, yyyymm, m_consumption, m_cost);
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetSpecificMonthNetCharge")) {
			try {
				GetSpecificMonthNetChargeRes res = new GetSpecificMonthNetChargeRes(frame);
				int consumption = res.getM_consumption();
				double cost = res.getM_cost();

				// 특정월의 월정산 비교
				String yyyymm = (String) bd.getArgs().get(0);
				
				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("m_consumption", consumption);
				session.setAttribute("m_cost", cost);
				// set response data (E)

				// STS log 저장 (S)
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, "", trId, createDate, null, null, yyyymm, consumption, cost, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)

				// 월정산 생성
				createMonthlyLog(mdsId, yyyymm, consumption, cost);
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdSetMessage")) {
			try {
				SetMessageRes res = new SetMessageRes(frame);
				
				if (res.getResult() == 0) {
					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E) 
				}
				
				// STS log 저장 (S)
				String msg = null;
				if (bd.getArgs().size() > 0) {
					msg = (String) bd.getArgs().get(0);
				} else
					throw new Exception("Check message, there is no message");
				
				createEcgStsLog(bypassCmd, 0, mdsId, null, null, null, null, null, null, null, null, null,
						new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
						null, /* emergency credit */
						0, "", trId, createDate, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null);
				// STS log 저장 (E)
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		}
		// 펌웨어 업그레이드를 위해 스토리지에 있는 펌웨어 삭제 요청에 대한 응답 수신 후 펌웨어 파일 전송
		else if (bypassCmd.equals("cmdSuniFirmwareUpdateControl_0")) {
			log.info("#######################################################");
			log.info("##### cmdSuniFirmwareUpdateControl_0              #####");
			log.info("##### Erase firmware update file storage area.    #####");
			log.info("#######################################################");
			
			try {
				SuniFirmwareUpdateControlRes res = new SuniFirmwareUpdateControlRes(frame);
					
				// 정상 응답이 오면 펌웨어 전송 시작
				cmdSuniFirmwareUpdateFileBlockWrite(session);
				
				// 종룜영령을 안보내기 위해 반환
				return;
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateFileBlockWrite")) {
			try {
				SuniFirmwareUpdateFileBlockWriteRes res = new SuniFirmwareUpdateFileBlockWriteRes(frame);

				// 정상 응답이 오면 펌웨어 계속 전송
				cmdSuniFirmwareUpdateFileBlockWrite(session);

				// 종룜영령을 안보내기 위해 반환
				return;
			} catch (STSException e) {
			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateControl_2")) {
			try {
				log.info("#######################################################");
				log.info("##### cmdSuniFirmwareUpdateControl_2              #####");
				log.info("##### Execute firmware update.                    #####");
				log.info("#######################################################");
				SuniFirmwareUpdateControlRes res = new SuniFirmwareUpdateControlRes(frame);
				
			} catch (STSException e) {

			}
		} else if (bypassCmd.equals("cmdGetSuniFirmwareUpdateInfo")) {
			try {
				GetSuniFirmwareUpdateInfoRes res = new GetSuniFirmwareUpdateInfoRes(frame);

			} catch (STSException e) {
			}
		} else if (bypassCmd.equals("cmdSuniFirmwareRead")) {
			try {
				GetSuniFirmwareUpdateInfoRes res = new GetSuniFirmwareUpdateInfoRes(frame);
			
				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);

				session.setAttribute("status", res.getStatus());
				session.setAttribute("moduleSerial", res.getDataMap().get("moduleSerial"));
				// session.setAttribute("moduleSerial", res.getModuleSerial());
				session.setAttribute("moduleTypeId", res.getModuleTypeId());
				session.setAttribute("moduleRevision", res.getModuleRevision());
				session.setAttribute("moduleMinorVer", res.getModuleMinorVer());
				session.setAttribute("moduleMajorVer", res.getModuleMajorVer());
				session.setAttribute("fileCtrl", res.getFileCtrl());
				session.setAttribute("fileTypeId", res.getFileTypeId());
				session.setAttribute("fileRevision", res.getFileRevision());
				session.setAttribute("fileMinorVer", res.getFileMinorVer());
				session.setAttribute("fileMajorVer", res.getFileMajorVer());
				session.setAttribute("fileByteSize", res.getFileByteSize());
				
				// 전체 파일 사이즈와 파일을 읽을 위치를 저장한다.
				session.setAttribute("fwfileByteSize", res.getFileByteSize());
				session.setAttribute("fwOffset", 0);
				cmdSuniFirmwareUpdateFileBlockRead(session);
				// set response data (E)
			} catch (STSException e) {
				log.info("fail");
				log.info("reason " + e.getMsg());
				
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateFileBlockRead")) { 
			try {
				SuniFirmwareUpdateFileBlockReadRes res = new SuniFirmwareUpdateFileBlockReadRes(frame);
				
				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("fwBin." + res.getNumber(), res.getData());
				// set response data (E)
				
				// 마지막이면 true가 리턴되어 정상 종료할 수 있도록 하고 아니면 다 받을 때까지 실행되어야 함.
				if (!cmdSuniFirmwareUpdateFileBlockRead(session)) {
					// return;
				}
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateKeyWrite")) {
			try {
				SuniFirmwareUpdateKeyWriteRes res = new SuniFirmwareUpdateKeyWriteRes(frame);
				
				if (res.getResult() == 0) {
					// set response data (S) 
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E) 
				}
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			}
		} else if (bypassCmd.equals("cmdSuniFirmwareUpdateKeyRead")) {
			try {
				SuniFirmwareUpdateKeyReadRes res = new SuniFirmwareUpdateKeyReadRes(frame);

				// set response data (S) 
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);
				
				session.setAttribute("keyNumber", res.getNumber());
				session.setAttribute("key", res.getData());
				// set response data (E)
				
			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			}
		} else if (bypassCmd.equals("cmdGetCIUCommStateHistory")) {
			try {
				GetCIUCommStateHistoryRes res = new GetCIUCommStateHistoryRes(frame);
			} catch (STSException e) {
			}
		} else if (bypassCmd.equals("cmdSetRFSetup")) {
			try {
				SetRFSetupRes res = new SetRFSetupRes(frame);

				if (res.getResult() == 0) {
					// set response data (S)
					session.setAttribute("result", "success");
					session.setAttribute("reason", null);
					// set response data (E)
				}

			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		} else if (bypassCmd.equals("cmdGetRFSetup")) {
			try {
				GetRFSetupRes res = new GetRFSetupRes(frame);

				// set response data (S)
				session.setAttribute("result", "success");
				session.setAttribute("reason", null);

				session.setAttribute("channel", res.getChannel());
				session.setAttribute("panId", res.getPanId());
				// set response data (E)

			} catch (STSException e) {
				session.setAttribute("result", "fail");
				session.setAttribute("reason", e.getMsg());
			} catch (Exception e1) {
				log.error(e1, e1);
			}
		}
	}
	
	/*
	 * OTA 시작 명령 for 누리
	 */
	public void cmdOTAStart(IoSession session) throws Exception {
		cmdOTAStart(session, (int) 0x00);
	}

	/*
	 * OTA 시작 명령
	 */
	public void cmdOTAStart(IoSession session, int upgradeType) throws Exception {
		// ota 비동기 이력의 인자에서 파일 경로를 가져온다.
		ByteArrayOutputStream out = null;
		FileInputStream in = null;
		try {
			BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
			// BypassDevice의 args에서 파일명을 가져와야 한다.
			File file = new File((String) bd.getArgs().get(0));
			out = new ByteArrayOutputStream();
			in = new FileInputStream(file);

			int len = 0;
			byte[] b = new byte[1024];
			while ((len = in.read(b)) != -1) {
				out.write(b, 0, len);
			}

			long filelen = file.length();
			// sendImage 에서 사용하기 위해 바이너리를 전역 변수에 넣는다.
			bd.setFw_bin(out.toByteArray());
			// sendImage에서 바이너리를 읽어올 수 있도록 하기 위해 전역 변수에 넣는다.
			bd.setFw_in(new ByteArrayInputStream(bd.getFw_bin(), 0, bd.getFw_bin().length));
			// int crc =
			// DataUtil.getIntTo2Byte(FrameUtil.getCRC(bd.getFw_bin()));
			byte[] crc = CRCUtil.Calculate_ZigBee_Crc(bd.getFw_bin(), (char) 0x0000);
			DataUtil.convertEndian(crc);

			String ns = (String) session.getAttribute("nameSpace");
			List<SMIValue> params = new ArrayList<SMIValue>();

			params.add(DataUtil.getSMIValueByObject(ns, "cmdModemFwImageLength", Long.toString(filelen)));
			params.add(DataUtil.getSMIValueByObject(ns, "cmdModemFwImageCRC",
					Integer.toString(DataUtil.getIntTo2Byte(crc))));

			/*
			 * 수니 모듈이 들어오면서 cmdModemFwUpgradeType가 추가되어 총 3개의 파라미터를 넘겨주게 되었음 그러나
			 * 20버전 미만의 모뎀들은 2개의 파라미터만 받을 수 있고 3개를 넘겨줄 경우 OTA가 실패하므로 20버전 이상의
			 * 모뎀들에만 cmdModemFwUpgradeType 파라미터를 추가시켜 주도록 로직을 변경함.
			 */

			// int addParamVersion = Integer.parseInt(FMPProperty.getProperty("gprs.uploadchannel.addparam.fwver", "20"));
			double modemFwVersion = 0d;
			if (bd.getFwVersion() != null)
				modemFwVersion = Double.parseDouble(bd.getFwVersion());

			if (modemFwVersion >= 20.0) {
				log.debug("Modem Firmware Version is bigger than 20.0");
				params.add(DataUtil.getSMIValueByObject(ns, "cmdModemFwUpgradeType", Integer.toString(upgradeType)));
				log.debug("Add a UpgradeType Parameter");
			}

			sendCommand(session, "cmdOTAStart", params);
		} finally {
			if (out != null)
				out.close();
			if (in != null)
				in.close();
		}
	}

	/*
	 * 펌웨어 바이너리를 보내는 명령 한 패킷 전송 후 응답을 받고 다음 패킷을 보내야 하므로 offset과 fw_in이 전역 변수로
	 * 선언되었다.
	 */
	public void cmdSendImage(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		log.debug("offset[" + bd.getOffset() + "]");
		String ns = (String) session.getAttribute("nameSpace");
		byte[] b = new byte[bd.getPacket_size()];
		int len = -1;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if ((len = bd.getFw_in().read(b)) != -1) {
			out.write(b, 0, len);
			List<SMIValue> params = new ArrayList<SMIValue>();
			params.add(DataUtil.getSMIValueByObject(ns, "cmdImageAddress", Integer.toString(bd.getOffset())));
			params.add(DataUtil.getSMIValueByObject(ns, "cmdImageSize", Integer.toString(len)));
			// 바이트 스트립을 문자열로 변환 후 바이트로 변환시 원본이 손상되어 STREAM을 바로 사용하도록 한다.
			params.add(new SMIValue(DataUtil.getOIDByMIBName(ns, "cmdImageData"), new STREAM(out.toByteArray())));
			bd.setOffset(bd.getOffset() + len);
			sendCommand(session, "cmdSendImage", params);
		}
		out.close();

		// 전송이 끝나면 종료 명령을 보낸다.
		if (bd.getOffset() == bd.getFw_bin().length) {
			bd.getFw_in().close();
			sendCommand(session, "cmdOTAEnd", null);
		}
	}

	public void cmdUploadMeteringData(IoSession session) throws Exception {
		List<SMIValue> params = new ArrayList<SMIValue>();
		sendCommand(session, "cmdUploadMeteringData", params);
	}

	public void cmdResetModem(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		int delayTime = 10;

		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof Integer)
				delayTime = (Integer) bd.getArgs().get(0);
			else if (bd.getArgs().get(0) instanceof String)
				delayTime = Integer.parseInt((String) bd.getArgs().get(0));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdResetModemDelayTime", Integer.toString(delayTime)));
		sendCommand(session, "cmdResetModem", params);
	}

	public void cmdFactorySetting(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		int code = 0x0314;

		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof Integer)
				code = (Integer) bd.getArgs().get(0);
			else if (bd.getArgs().get(0) instanceof String)
				code = Integer.parseInt((String) bd.getArgs().get(0));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdFactorySettingCode", Integer.toString(code)));
		sendCommand(session, "cmdFactorySetting", params);
	}

	public void cmdReadModemConfiguration(IoSession session) throws Exception {
		sendCommand(session, "cmdReadModemConfiguration", null);
	}

	public void cmdSetTime(IoSession session) throws Exception {
		String timestamp = DateTimeUtil.getDateString(new Date());

		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetTimestamp", timestamp));
		sendCommand(session, "cmdSetTime", params);
	}

	public void cmdSetModemResetInterval(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		int interval = 60;

		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof Integer)
				interval = (Integer) bd.getArgs().get(0);
			else if (bd.getArgs().get(0) instanceof String)
				interval = Integer.parseInt((String) bd.getArgs().get(0));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdModemResetIntervalMinute", Integer.toString(interval)));
		sendCommand(session, "cmdModemResetInterval", params);
	}

	public void cmdSetMeteringInterval(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		int interval = 15;

		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof Integer)
				interval = (Integer) bd.getArgs().get(0);
			else if (bd.getArgs().get(0) instanceof String)
				interval = Integer.parseInt((String) bd.getArgs().get(0));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetMeteringIntervalMinute", Integer.toString(interval)));
		sendCommand(session, "cmdSetMeteringInterval", params);
	}

	public void cmdSetServerIpPort(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String ip = "187.1.10.58";
		int port = 8000;

		if (bd.getArgs().size() > 1) {
			ip = (String) bd.getArgs().get(0);
			if (bd.getArgs().get(1) instanceof Integer)
				port = (Integer) bd.getArgs().get(1);
			else if (bd.getArgs().get(1) instanceof String)
				port = Integer.parseInt((String) bd.getArgs().get(1));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetServerIp", ip));
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetServerPort", Integer.toString(port)));
		sendCommand(session, "cmdSetServerIpPort", params);
	}

	public void cmdSet2ndServerIpPort(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String ip = "187.1.10.58";
		int port = 8000;

		if (bd.getArgs().size() > 1) {
			ip = (String) bd.getArgs().get(0);
			if (bd.getArgs().get(1) instanceof Integer)
				port = (Integer) bd.getArgs().get(1);
			else if (bd.getArgs().get(1) instanceof String)
				port = Integer.parseInt((String) bd.getArgs().get(1));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSet2ndServerIp", ip));
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSet2ndServerPort", Integer.toString(port)));
		sendCommand(session, "cmdSet2ndServerIpPort", params);
	}

	public void cmdSetApn(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String apnAddress = "test";
		String apnId = "test";
		String apnPassword = "test";

		if (bd.getArgs().size() > 2) {
			apnAddress = (String) bd.getArgs().get(0);
			apnId = (String) bd.getArgs().get(1);
			apnPassword = (String) bd.getArgs().get(2);
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetApnAddress", apnAddress));
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetApnID", apnId));
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetApnPassword", apnPassword));
		sendCommand(session, "cmdSetApn", params);
	}

	public void cmdSetMeterTime(IoSession session) throws Exception {
		cmdSetBypassStart(session, 10);
		byte[][] req = KamstrupCIDMeta.getRequest(new String[] { "SetClock", "", "" });
		log.debug("REQ[" + Hex.decode(req[0]) + "] VAL[" + Hex.decode(req[1]) + "]");
		session.setAttribute("bypassCmd", "cmdSetMeterTime");
		session.write(KamstrupCIDMeta.makeKmpCmd(req[0], req[1]));
		// session.write(new byte[]{(byte)0x80, (byte)0x3F, (byte)0x10,
		// (byte)0x02, (byte)0x04, (byte)0xBA, (byte)0x00,
		// (byte)0xC7, (byte)0xD7, (byte)0x07, (byte)0x0D});
	}

	public void cmdGetMaxDemand(IoSession session) throws Exception {
		cmdSetBypassStart(session, 10);
		byte[][] req = KamstrupCIDMeta.getRequest(new String[] { "GetRegister", "1326" });
		log.debug("REQ[" + Hex.decode(req[0]) + "] VAL[" + Hex.decode(req[1]) + "]");
		session.setAttribute("bypassCmd", "cmdGetMaxDemand");
		session.write(KamstrupCIDMeta.makeKmpCmd(req[0], req[1]));
		// session.write(new byte[]{(byte)0x80, (byte)0x3F, (byte)0x10,
		// (byte)0x02, (byte)0x04, (byte)0xBA, (byte)0x00,
		// (byte)0xC7, (byte)0xD7, (byte)0x07, (byte)0x0D});
	}

	public void cmdSetBypassStart(IoSession session, int timeout) throws Exception {
		// BypassDevice bd =
		// (BypassDevice)session.getAttribute(session.getRemoteAddress());

		/*
		 * if (bd.getArgs().size() > 0) { if (bd.getArgs().get(0) instanceof
		 * String) timeout = Integer.parseInt((String)bd.getArgs().get(0)); else
		 * if (bd.getArgs().get(0) instanceof Integer) timeout =
		 * (Integer)bd.getArgs().get(0); }
		 */

		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdSetBypassStartTimeout", Integer.toString(timeout)));
		sendCommand(session, "cmdSetBypassStart", params);
	}

	public void cmdOndemandMetering(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		int offset = 0;
		int count = 1;

		if (bd.getArgs().size() > 1) {
			if (bd.getArgs().get(0) instanceof String) {
				offset = Integer.parseInt((String) bd.getArgs().get(0));
			} else {
				offset = (Integer) bd.getArgs().get(0);
			}

			if (bd.getArgs().get(1) instanceof String) {
				count = Integer.parseInt((String) bd.getArgs().get(1));
			} else {
				count = (Integer) bd.getArgs().get(1);
			}

		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdOndemandOffset", Integer.toString(offset)));
		params.add(DataUtil.getSMIValueByObject(ns, "cmdOndemandCount", Integer.toString(count)));
		sendCommand(session, "cmdOndemandMetering", params);
	}

	public void cmdRelayStatus(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		String ns = (String) session.getAttribute("nameSpace");
		sendCommand(session, "cmdRelayStatus", null);
	}

	public void cmdRelayDisconnect(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		int timeout = 5;

		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof Integer)
				timeout = (Integer) bd.getArgs().get(0);
			else if (bd.getArgs().get(0) instanceof String)
				timeout = Integer.parseInt((String) bd.getArgs().get(0));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdRelayDisconnectTimeout", Integer.toString(timeout)));
		sendCommand(session, "cmdRelayDisconnect", params);
	}

	public void cmdRelayReconnect(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		int timeout = 5;

		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof Integer)
				timeout = (Integer) bd.getArgs().get(0);
			else if (bd.getArgs().get(0) instanceof String)
				timeout = Integer.parseInt((String) bd.getArgs().get(0));
		}
		String ns = (String) session.getAttribute("nameSpace");
		List<SMIValue> params = new ArrayList<SMIValue>();
		params.add(DataUtil.getSMIValueByObject(ns, "cmdRelayReconnectTimeout", Integer.toString(timeout)));
		sendCommand(session, "cmdRelayReconnect", params);
	}

	public void cmdSetPaymentMode(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		int mode = 0;
		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof String)
				mode = Integer.parseInt((String) bd.getArgs().get(0));
			else if (bd.getArgs().get(0) instanceof Integer)
				mode = (Integer) bd.getArgs().get(0);
		} else
			throw new Exception("check mode, there is no mode");

		SetPaymentModeReq req = new SetPaymentModeReq(mode);
		session.setAttribute("bypassCmd", "cmdSetPaymentMode");
		session.write(req.encode());
	}

	public void cmdGetPaymentMode(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		GetPaymentModeReq req = new GetPaymentModeReq();
		session.setAttribute("bypassCmd", "cmdGetPaymentMode");
		session.write(req.encode());
	}

	public void cmdGetRemainingCredit(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		GetRemainingCreditReq req = new GetRemainingCreditReq();
		session.setAttribute("bypassCmd", "cmdGetRemainingCredit");
		session.write(req.encode());
	}

	public void cmdSetRFSetup(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		Integer channel = Integer.parseInt((String) bd.getArgs().get(0));
		Integer panId = Integer.parseInt((String) bd.getArgs().get(1));

		SetRFSetupReq req = new SetRFSetupReq(channel, panId);
		session.setAttribute("bypassCmd", "cmdSetRFSetup");
		session.write(req.encode());
	}

	public void cmdGetRFSetup(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		GetRFSetupReq req = new GetRFSetupReq();
		session.setAttribute("bypassCmd", "cmdGetRFSetup");
		session.write(req.encode());
	}

	public void cmdSetSTSSetup(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		String date = (String) bd.getArgs().get(0);
		String tokenKey1 = (String) bd.getArgs().get(1);
		String tokenKey2 = (String) bd.getArgs().get(2);
		String stsNumber = (String) bd.getArgs().get(3);

		SetSTSSetupReq req = new SetSTSSetupReq(date, tokenKey1, tokenKey2, stsNumber);
		session.setAttribute("bypassCmd", "cmdSetSTSSetup");
		session.write(req.encode());
	}

	public void cmdGetSTSSetup(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		GetSTSSetupReq req = new GetSTSSetupReq();
		session.setAttribute("bypassCmd", "cmdGetSTSSetup");
		session.write(req.encode());
	}

	public void cmdSetSTSToken(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String token = null;
		if (bd.getArgs().size() > 0) {
			token = (String) bd.getArgs().get(0);
		} else
			throw new Exception("Check token, There is no token");

		SetSTSTokenReq req = new SetSTSTokenReq(token);
		session.setAttribute("bypassCmd", "cmdSetSTSToken");
		session.write(req.encode());
	}

	public void cmdGetSTSToken(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		int count = 0;
		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof String)
				count = Integer.parseInt((String) bd.getArgs().get(0));
			else if (bd.getArgs().get(0) instanceof Integer)
				count = (Integer) bd.getArgs().get(0);
		} else
			throw new Exception("Check token count, There is no token count");

		GetSTSTokenReq req = new GetSTSTokenReq(count);
		session.setAttribute("bypassCmd", "cmdGetSTSToken");
		session.write(req.encode());
	}

	public void cmdSetTariff(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		String yyyymmdd = null;
//		int condLimit1 = Integer.parseInt(FMPProperty.getProperty("ecg.tariff.gov.subsidy.limit", "0"));
//		int condLimit2 = Integer.parseInt(FMPProperty.getProperty("ecg.tariff.utility.relief.limit", "0"));
		int condLimit1 = 0;
		int condLimit2 = 0;
		
		int[] cons = null;
		double[] fixedRate = null;
		double[] varRate = null;
		String s_cons = null; // 여러개의 사용량이 ,를 이용하여 문자열로 넘어온다.
		String s_fixedRate = null; // 여러개의 요금이 ,를 이용하여 문자열로 넘어온다.
		String s_varRate = null;
		double[] condRate1 = null;
		double[] condRate2 = null;
		String s_condRate1 = null;
		String s_condRate2 = null;

		int count = 0;
		if (bd.getArgs().size() == 8) {
			yyyymmdd = (String) bd.getArgs().get(0);

			if (bd.getArgs().get(1) instanceof String) {
				condLimit1 = Integer.parseInt((String) bd.getArgs().get(1));
			} else if (bd.getArgs().get(1) instanceof Integer) {
				condLimit1 = (Integer) bd.getArgs().get(1);
			}

			if (bd.getArgs().get(2) instanceof String) {
				condLimit2 = Integer.parseInt((String) bd.getArgs().get(2));
			} else if (bd.getArgs().get(2) instanceof Integer) {
				condLimit2 = (Integer) bd.getArgs().get(2);
			}

			s_cons = (String) bd.getArgs().get(3);
			s_fixedRate = (String) bd.getArgs().get(4);
			s_varRate = (String) bd.getArgs().get(5);
			s_condRate1 = (String) bd.getArgs().get(6);
			s_condRate2 = (String) bd.getArgs().get(7);
			
			// ECGSetSTSTariffTask - Bulk Parameters set (S)
			if (s_cons.contains("/")) {
				s_cons = s_cons.replace("/", ",");
			} 
			
			if (s_fixedRate.contains("/")) {
				s_fixedRate = s_fixedRate.replace("/", ",");
			}
			
			if (s_varRate.contains("/")) {
				s_varRate = s_varRate.replace("/", ",");
			} 
			
			if (s_condRate1.contains("/")) {
				s_condRate1 = s_condRate1.replace("/", ",");
			} 
			
			if (s_condRate2.contains("/")) {
				s_condRate2 = s_condRate2.replace("/", ",");
			}
			
			log.info("s_cons[" + s_cons + "]");
			log.info("s_fixedRate[" + s_fixedRate + "]");
			log.info("s_varRate[" + s_varRate + "]");
			log.info("s_condRate1[" + s_condRate1 + "]");
			log.info("s_condRate2[" + s_condRate2 + "]");
			// ECGSetSTSTariffTask - Bulk Parameters set (E)

			StringTokenizer st = new StringTokenizer(s_cons, ",");
			count = st.countTokens();
			cons = new int[count];

			int i = 0;
			while (st.hasMoreTokens()) {
				cons[i++] = Integer.parseInt(st.nextToken());
			}

			/*
			 * fixed rate는 0으로 설정해야 한다.
			 * 2017.11.13 by elevas
			 * 블록 개수가 달라지기 때문에 토큰 개수로 처리하도록 변경한다.
			 * 2018.03.27 by elevas
			 */
			st = new StringTokenizer(s_fixedRate, ",");
			fixedRate = new double[count];
			i = 0;
			while (st.hasMoreTokens()) {
			    fixedRate[i++] = Double.parseDouble(st.nextToken());
			}
			 

			st = new StringTokenizer(s_varRate, ",");
			varRate = new double[count];
			i = 0;
			while (st.hasMoreTokens()) {
				varRate[i++] = Double.parseDouble(st.nextToken());
			}

			st = new StringTokenizer(s_condRate1, ",");
			condRate1 = new double[count];
			i = 0;
			while (st.hasMoreTokens()) {
				condRate1[i++] = Double.parseDouble(st.nextToken());
			}

			st = new StringTokenizer(s_condRate2, ",");
			condRate2 = new double[count];
			i = 0;
			while (st.hasMoreTokens()) {
				condRate2[i++] = Double.parseDouble(st.nextToken());
			}
		} else
			throw new Exception("Check tariff argement, wrong size");

		// 15는 한 패킷의 사이즈가 128을 넘지 않고 처리할 수 있는 최대 타리프 개수
		// 2016.06.08 기준으로 10개이기 때문에 한 패킷으로 처리하는 것으로 하고 15개가 넘어갈 때
		// 수니 모듈도 업그레이드가 되어야 함.
		SetTariffReq req = new SetTariffReq(yyyymmdd, condLimit1, condLimit2, cons, fixedRate, varRate, condRate1, condRate2);
		session.setAttribute("bypassCmd", "cmdSetTariff");
		session.write(req.encode());
	}

	public void cmdGetTariff(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		int tariffMode = 0;
		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof String)
				tariffMode = Integer.parseInt((String) bd.getArgs().get(0));
			else if (bd.getArgs().get(0) instanceof Integer)
				tariffMode = (Integer) bd.getArgs().get(0);
		} else
			throw new Exception("Check tariff mode, There is no tariff mode");

		GetTariffReq req = new GetTariffReq(tariffMode);
		session.setAttribute("bypassCmd", "cmdGetTariff");
		session.write(req.encode());
	}

	public void cmdSetFriendlyCreditSchedule(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		String yyyymmdd = null;
		int[] dayType = null;
		String[] fromHHMM = null;
		String[] endHHMM = null;
		String s_dayType = null; // dayType은 여러개의 값이 ,를 이용하여 문자열로 넘어온다.
		String s_fromHHMM = null; // fromHHMM은 여러개의 값이 ,를 이용하여 문자열로 넘어온다.
		String s_endHHMM = null; // endHHMM은 여러개의 값이 ,를 이용하여 문자열로 넘어온다.
		int count = 0;
		if (bd.getArgs().size() == 4) {
			yyyymmdd = (String) bd.getArgs().get(0);
			s_dayType = (String) bd.getArgs().get(1);
			s_fromHHMM = (String) bd.getArgs().get(2);
			s_endHHMM = (String) bd.getArgs().get(3);
			
			StringTokenizer st = new StringTokenizer(s_dayType, ",");
			count = st.countTokens();
			dayType = new int[count];

			int i = 0;
			while (st.hasMoreTokens()) {
				dayType[i++] = Integer.parseInt(st.nextToken());
			}

			st = new StringTokenizer(s_fromHHMM, ",");
			fromHHMM = new String[count];
			i = 0;
			while (st.hasMoreTokens()) {
				fromHHMM[i++] = st.nextToken();
			}

			st = new StringTokenizer(s_endHHMM, ",");
			endHHMM = new String[count];
			i = 0;
			while (st.hasMoreTokens()) {
				endHHMM[i++] = st.nextToken();
			}
		} else
			throw new Exception("Check friendly credit, wrong size");

		SetFriendlyCreditScheduleReq req = new SetFriendlyCreditScheduleReq(yyyymmdd, dayType, fromHHMM, endHHMM);
		session.setAttribute("bypassCmd", "cmdSetFriendlyCreditSchedule");
		session.write(req.encode());
	}

	public void cmdGetFriendlyCreditSchedule(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		int mode = 0;
		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof String)
				mode = Integer.parseInt((String) bd.getArgs().get(0));
			else if (bd.getArgs().get(0) instanceof Integer)
				mode = (Integer) bd.getArgs().get(0);
		} else
			throw new Exception("Check mode, there is no mode");

		GetFriendlyCreditScheduleReq req = new GetFriendlyCreditScheduleReq(mode);
		session.setAttribute("bypassCmd", "cmdGetFriendlyCreditSchedule");
		session.write(req.encode());
	}

	public void cmdSetEmergencyCredit(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());

		int mode = 0;
		int days = 0;
		if (bd.getArgs().size() > 0) {
			if (bd.getArgs().get(0) instanceof String)
				mode = Integer.parseInt((String) bd.getArgs().get(0));
			else if (bd.getArgs().get(0) instanceof Integer)
				mode = (Integer) bd.getArgs().get(0);

			if (bd.getArgs().size() > 1) {
				if (bd.getArgs().get(1) instanceof String)
					days = Integer.parseInt((String) bd.getArgs().get(1));
				else if (bd.getArgs().get(1) instanceof Integer)
					days = (Integer) bd.getArgs().get(1);
			}
		} else
			throw new Exception("Check mode, there is no mode");

		SetEmergencyCreditReq req = new SetEmergencyCreditReq(mode, days);
		session.setAttribute("bypassCmd", "cmdSetEmergencyCredit");
		session.write(req.encode());
	}

	public void cmdGetEmergencyCredit(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		GetEmergencyCreditReq req = new GetEmergencyCreditReq();
		session.setAttribute("bypassCmd", "cmdGetEmergencyCredit");
		session.write(req.encode());
	}

	public void cmdGetPreviousMonthNetCharge(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		GetPreviousMonthNetChargeReq req = new GetPreviousMonthNetChargeReq();
		session.setAttribute("bypassCmd", "cmdGetPreviousMonthNetCharge");
		session.write(req.encode());
	}

	public void cmdGetSpecificMonthNetCharge(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		
		String yyyymm = null;
		if (bd.getArgs().size() > 0) {
			yyyymm = (String) bd.getArgs().get(0);
		} else
			throw new Exception("Check specific month, there is no specific month");

		GetSpecificMonthNetChargeReq req = new GetSpecificMonthNetChargeReq(yyyymm);
		session.setAttribute("bypassCmd", "cmdGetSpecificMonthNetCharge");
		session.write(req.encode());
	}

	public void cmdSetMessage(IoSession session) throws Exception {
		cmdSetBypassStart(session, 60);
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String msg = null;
		
		if (bd.getArgs().size() > 0) {
			msg = (String) bd.getArgs().get(0);
		} else {
			throw new Exception("Check message, there is no message");
		}

		SetMessageReq req = new SetMessageReq(msg);
		session.setAttribute("bypassCmd", "cmdSetMessage");
		session.write(req.encode());
	}

	/*
	 * SUNI 모듈 펌웨어 업그레이드 시작
	 */
	public void cmdSuniFirmwareWrite(IoSession session) throws Exception {
		String modemType = session.getAttribute("modemType") ==  null ? "" : (String)session.getAttribute("modemType");
		
		if (modemType.equals("ZigBee") || modemType.equals("ZRU")) { // ZigBee STS
			cmdSetBypassStart(session, 180);

			SuniFirmwareUpdateControlReq req = new SuniFirmwareUpdateControlReq(0);
			session.setAttribute("bypassCmd", "cmdSuniFirmwareUpdateControl_0");
			session.write(req.encode());
		} else {
			// 바이패스로 SUNI 모듈 업그레이드 방식에서 누리 모뎀으로 바이너리 전달하는 것으로 변경됨.
			cmdOTAStart(session, 0x01);
		}
	}

	/*
	 * SUNI 모듈 펌웨어 가져오기 펌웨어 정보를 먼저 읽은 후에 길이를 가져와야 한다.
	 */
	public void cmdSuniFirmwareRead(IoSession session) throws Exception {
		cmdSetBypassStart(session, 180);
		cmdGetSuniFirmwareUpdateInfo(session);
		// bypassCmd 변경
		session.setAttribute("bypassCmd", "cmdSuniFirmwareRead");
	}

	public void cmdGetSuniFirmwareUpdateInfo(IoSession session) throws Exception {
		cmdSetBypassStart(session, 30);

		GetSuniFirmwareUpdateInfoReq req = new GetSuniFirmwareUpdateInfoReq();
		session.setAttribute("bypassCmd", "cmdGetSuniFirmwareUpdateInfo");
		session.write(req.encode());
	}

	private void cmdSuniFirmwareUpdateFileBlockWrite(IoSession session) throws Exception {
		log.info("#######################################################");
		log.info("##### cmdSuniFirmwareUpdateFileBlockWrite         #####");
		log.info("#######################################################");
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		byte[] fw_bin = bd.getFw_bin();

		// 실행된 적이 없기 때문에 펌웨어를 읽어서 바이너리로 변환한다.
		if (fw_bin == null || fw_bin.length == 0) {
			String filename = (String) bd.getArgs().get(0);
			log.info("FW_FILE[" + filename + "]");

			ByteArrayOutputStream out = null;
			FileInputStream in = null;
			try {
				// BypassDevice의 args에서 파일명을 가져와야 한다.
				File file = new File(filename);
				out = new ByteArrayOutputStream();
				in = new FileInputStream(file);

				int len = 0;
				byte[] b = new byte[1024];
				while ((len = in.read(b)) != -1) {
					out.write(b, 0, len);
				}

				// sendImage 에서 사용하기 위해 바이너리를 전역 변수에 넣는다.
				fw_bin = out.toByteArray();
				bd.setFw_bin(fw_bin);
				bd.setOffset(0);
			} finally {
				if (out != null) out.close();
				if (in != null)	in.close();
			}
		}

		// 128 바이씩 읽어서 전송, 마지막 패킷은 128보다 작거나 같다.
		int offset = bd.getOffset();
		byte[] fw = null;
		
		log.info("## TOTAL_LEN[" + fw_bin.length + "] OFFSET[" + offset + "] ##");
		if (offset == fw_bin.length) {
			// 펌웨어 바이너리를 전송했으므로 실행 명령을 보낸다.
			SuniFirmwareUpdateControlReq req = new SuniFirmwareUpdateControlReq(2);
			session.setAttribute("bypassCmd", "cmdSuniFirmwareUpdateControl_2");
			session.write(req.encode());
		} else {
			if (fw_bin.length >= offset + 128)
				fw = new byte[128];
			else
				fw = new byte[fw_bin.length - offset];
			System.arraycopy(fw_bin, offset, fw, 0, fw.length);
			// 펌웨어 블락이 쓰여질 순번을 구한다.
			int number = offset / 128;
			// 마지막 블락은 0이 안떨어지면 1더 증가시켜야 한다.
			if (offset > 128 && offset % 128 != 0)
				number++;
			log.info("TOTAL_LEN[" + fw_bin.length + "] OFFSET[" + offset + "] NUMBER[" + number + "] SEND_BYTE[" + fw.length + "]");
			SuniFirmwareUpdateFileBlockWriteReq req = new SuniFirmwareUpdateFileBlockWriteReq(number, fw);
			session.setAttribute("bypassCmd", "cmdSuniFirmwareUpdateFileBlockWrite");
			
			// 다음 블락을 읽기 위해 현재 위치에 전송한 블락 사이즈를 더한다.
			bd.setOffset(offset + fw.length);
			session.setAttribute(session.getRemoteAddress(), bd);
			session.write(req.encode());
		}
	}

	private boolean cmdSuniFirmwareUpdateFileBlockRead(IoSession session) throws Exception {
		int fileByteSize = (Integer) session.getAttribute("fwfileByteSize");
		int offset = (Integer) session.getAttribute("fwOffset");

		// fileByteSize를 128로 나눈 수를 number로 펌웨어를 가져온다.
		if (offset == fileByteSize) {
			// 전체를 가져왔으므로 바이너리 파일로 생성하던가 나중에 처리

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = null;
			for (int i = 0;; i++) {
				b = (byte[]) session.getAttribute("fwBin." + i);
				if (b == null) {
					// 전부 받았으므로 종료
					log.info("FW_BIN_SIZE[" + out.toByteArray().length + "] FILE_BYTE_SIZE[" + fileByteSize + "]");
					out.close();
					break;
				} else {
					out.write(b);
				}
			}
			return true;
		} else {
			int number = offset / 128;
			if (offset > 128 && offset % 128 != 0)
				number++;
			int length = 0;
			if (fileByteSize >= offset + 128)
				length = 128;
			else
				length = fileByteSize - offset;
			session.setAttribute("fwOffset", offset + length);
			session.setAttribute("bypassCmd", "cmdSuniFirmwareUpdateFileBlockRead");
			SuniFirmwareUpdateFileBlockReadReq req = new SuniFirmwareUpdateFileBlockReadReq(number, length);
			session.write(req.encode());

			return false;
		}
	}

	public void cmdSuniFirmwareUpdateKeyWrite(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		session.setAttribute("bypassCmd", "cmdSuniFirmwareUpdateKeyWrite");

		if (bd.getArgs().size() == 2) {
			int number = Integer.parseInt((String) bd.getArgs().get(0));
			byte[] key = Hex.encode((String) bd.getArgs().get(1));

			cmdSetBypassStart(session, 60);

			SuniFirmwareUpdateKeyWriteReq req = new SuniFirmwareUpdateKeyWriteReq(number, key);
			session.write(req.encode());
		}
	}

	public void cmdSuniFirmwareUpdateKeyRead(IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		session.setAttribute("bypassCmd", "cmdSuniFirmwareUpdateKeyRead");

		if (bd.getArgs().size() == 2) {
			int number = Integer.parseInt((String) bd.getArgs().get(0));

			cmdSetBypassStart(session, 60);

			SuniFirmwareUpdateKeyReadReq req = new SuniFirmwareUpdateKeyReadReq(number);
			session.write(req.encode());
		}
	}

	public void cmdGetCIUCommStateHistory(IoSession session) throws Exception {
		session.setAttribute("bypassCmd", "cmdGetCIUCommStateHistory");

		cmdSetBypassStart(session, 60);

		GetCIUCommStateHistoryReq req = new GetCIUCommStateHistoryReq();
		session.write(req.encode());
	}

	/*
	 * GET 명령에 의해 조회된 결과를 생성한다.
	 */
	private void createEcgStsLog(String cmd, int seq, String meterId, Integer payMode, String tokenDate, String token,
			Integer tariffMode, Integer tariffKind, Integer tariffCount, String tariffDate, String condLimit1,
			String condLimit2, int[] consumption, double[] fixedRate, double[] varRate, double[] condRate1,
			double[] condRate2, Integer emergencyCreditMode, Integer emergencyCreditDay, Integer result,
			String failReason, long asyncTrId, String asyncCreateDate, String remainingCreditDate,
			Double remainingCredit, String netChargeYyyymm, Integer netChargeMonthConsumption,
			Double netChargeMonthCost, String netChargeYyyymmdd, Integer netChargeDayConsumption,
			Double netChargeDayCost, String friendlyDate, int[] friendlyDayType, Integer fcMode,
			String[] friendlyFromHHMM, String[] friendlyEndHHMM, String stsNumber, String kct1, String kct2,
			Integer channel, Integer panId) {
		log.debug("createEcgStsLog");
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		TransactionStatus txstatus = null;
		EcgSTSLogDao stslogDao = DataUtil.getBean(EcgSTSLogDao.class);
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);
		try {
			txstatus = txmanager.getTransaction(null);

			Meter meter = meterDao.get(meterId);
			if (meter != null) {
				EcgSTSLog stslog = new EcgSTSLog();
				stslog.setAsyncTrId(asyncTrId);
				stslog.setCmd(cmd);
				stslog.setMeterNumber(meter.getMdsId());
				stslog.setCreateDate(DateTimeUtil.getDateString(new Date()));
				stslog.setSeq(seq);
				stslog.setGetDate(asyncCreateDate);
				stslog.setResult(result);
				stslog.setFailReason(failReason);
				if (payMode != null) {
					stslog.setPayMode(payMode);
				}
				if (tokenDate != null) {
					stslog.setTokenDate(tokenDate);
				}
				if (token != null) {
					stslog.setToken(token);
				}
				if (emergencyCreditMode != null) {
					stslog.setEmergencyCreditMode(emergencyCreditMode);
				}
				if (emergencyCreditDay != null) {
					stslog.setEmergencyCreditDay(emergencyCreditDay);
				}
				stslog.setResultDate(stslog.getCreateDate());
				if (tariffMode != null) {
					stslog.setTariffMode(tariffMode);
				}
				if (tariffKind != null) {
					stslog.setTariffKind(tariffKind);
				}
				if (tariffCount != null) {
					stslog.setTariffCount(tariffCount);
				}
				stslog.setTariffDate(tariffDate);
				stslog.setRemainingCreditDate(remainingCreditDate);
				if (remainingCredit != null) {
					stslog.setRemainingCredit(remainingCredit);
				}

				stslog.setNetChargeYyyymm(netChargeYyyymm);
				if (netChargeMonthConsumption != null) {
					stslog.setNetChargeMonthConsumption(netChargeMonthConsumption);
				}
				if (netChargeMonthCost != null) {
					stslog.setNetChargeMonthCost(netChargeMonthCost);
				}
				stslog.setNetChargeYyyymmdd(netChargeYyyymmdd);
				if (netChargeDayConsumption != null) {
					stslog.setNetChargeDayConsumption(netChargeDayConsumption);
				}
				if (netChargeDayCost != null) {
					stslog.setNetChargeDayCost(netChargeDayCost);
				}
				if (stsNumber != null) {
					stslog.setStsNumber(stsNumber);
				}
				if (kct1 != null) {
					stslog.setKct1(kct1);
				}
				if (kct2 != null) {
					stslog.setKct1(kct2);
				}
				if (channel != null) {
					stslog.setChannel(channel);
				}
				if (panId != null) {
					stslog.setPanId(panId);
				}
				if (condLimit1 != null) {
					stslog.setCondLimit1(condLimit1);
				}
				if (condLimit2 != null) {
					stslog.setCondLimit2(condLimit2);
				}

				if (consumption != null && fixedRate != null && varRate != null && condRate1 != null
						&& condRate2 != null) {
					String s_consumption = "";
					String s_fixedRate = "";
					String s_varRate = "";
					String s_condRate1 = "";
					String s_condRate2 = "";
					for (int i = 0; i < consumption.length; i++) {
						if (i != 0) {
							s_consumption += ",";
							s_fixedRate += ",";
							s_varRate += ",";
							s_condRate1 += ",";
							s_condRate2 += ",";
						}
						s_consumption += consumption[i];
						s_fixedRate += fixedRate[i];
						s_varRate += varRate[i];
						s_condRate1 += condRate1[i];
						s_condRate2 += condRate2[i];
					}
					stslog.setConsumption(s_consumption);
					stslog.setFixedRate(s_fixedRate);
					stslog.setVarRate(s_varRate);
					stslog.setCondRate1(s_condRate1);
					stslog.setCondRate2(s_condRate2);
				}

				stslog.setFriendlyDate(friendlyDate);
				if (fcMode != null) {
					stslog.setFcMode(fcMode);
				}

				if (friendlyDayType != null && friendlyDayType.length > 0) {
					String dayType = "";

					for (int i = 0; i < friendlyDayType.length; i++) {
						if (i != 0)
							dayType += ",";
						dayType += friendlyDayType[i];
					}
					stslog.setFriendlyDayType(dayType);
				}
				if (friendlyFromHHMM != null && friendlyEndHHMM != null) {
					String fromHHMM = "";
					String endHHMM = "";
					for (int i = 0; i < friendlyFromHHMM.length; i++) {
						if (i != 0) {
							fromHHMM += ",";
							endHHMM += ",";
						}
						fromHHMM += friendlyFromHHMM[i];
						endHHMM += friendlyEndHHMM[i];
					}

					stslog.setFriendlyFromHHMM(fromHHMM);
					stslog.setFriendlyEndHHMM(endHHMM);
				}
				stslogDao.add(stslog);
			}
			txmanager.commit(txstatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	/*
	 * SET 명령에 의해 생성한 ECG_STS_LOG의 정보를 찾아서 결과를 저장한다.
	 */
	private void updateEcgStsLog(String cmd, String meterId, int result, String failReason, long asyncTrId,
			String asyncCreateDate) {
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		TransactionStatus txstatus = null;
		EcgSTSLogDao stslogDao = DataUtil.getBean(EcgSTSLogDao.class);
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);
		try {
			txstatus = txmanager.getTransaction(null);

			Meter meter = meterDao.get(meterId);
			if (meter != null) {
				String mdsId = meter.getMdsId();

				Set<Condition> condition = new HashSet<Condition>();
				condition.add(new Condition("id.cmd", new Object[] { cmd }, null, Restriction.EQ));
				condition.add(new Condition("id.meterNumber", new Object[] { mdsId }, null, Restriction.EQ));
				condition.add(new Condition("id.asyncTrId", new Object[] { asyncTrId }, null, Restriction.EQ));
				condition.add(new Condition("id.createDate", new Object[] { asyncCreateDate }, null, Restriction.EQ));

				List<EcgSTSLog> stslogs = stslogDao.findByConditions(condition);
				if (stslogs != null && stslogs.size() == 1) {
					EcgSTSLog stslog = stslogs.get(0);
					stslog.setResult(result);
					stslog.setFailReason(failReason);
					stslog.setResultDate(DateTimeUtil.getDateString(new Date()));
					stslogDao.update(stslog);
				}
				log.info("updateEcgStsLog [" + cmd + ", " + mdsId + ", " + asyncTrId + "]");
			}
			txmanager.commit(txstatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	/*
	 * Remaining Credit 실행 후 현재 잔액에 대한 PrepaymentLog를 생성하고 계약 정보를 갱신한다.
	 */
	private void updateCurrentCredit(String meterId, String remainingCreditDate, double remainingCredit) {
		log.debug("updateCurrentCredit Start");
		PrepaymentLogDao prelogDao = DataUtil.getBean(PrepaymentLogDao.class);
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);
		ContractDao contractDao = DataUtil.getBean(ContractDao.class);
		OperatorDao operatorDao = DataUtil.getBean(OperatorDao.class);
		// HibernateException발생하여, txmanager 구문 추가하였음.
		HibernateTransactionManager txmanager = DataUtil.getBean(HibernateTransactionManager.class);
		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
			Operator operator = operatorDao.getOperatorByLoginId("admin");
			Meter meter = meterDao.get(meterId);
			Contract contract = meter.getContract();

			if (contract != null) {
				contract.setCurrentCredit(remainingCredit);
				contractDao.update(contract);

				PrepaymentLog prelog = new PrepaymentLog();
				// Prepaymentlog의 ID 생성방법 변경(id+시스템시간)
				prelog.setId(
						Long.parseLong(Integer.toString(contract.getId()) + Long.toString(System.currentTimeMillis())));
				prelog.setOperator(operator);
				prelog.setContract(contract);
				prelog.setCustomer(meter.getCustomer());
				prelog.setBalance(remainingCredit);
				prelog.setLastTokenDate(remainingCreditDate + "00");
				prelog.setLocation(contract.getLocation());

				prelogDao.add(prelog);
			}

			txmanager.commit(txstatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	/*
	 * GetSTSSetup 실행 후 STSNumber를 갱신한다.
	 */
	private void updateSTSNumber(String meterId, String stsNumber) {
		log.debug("updateSTSNumber Start");
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);
		// HibernateException발생하여, txmanager 구문 추가하였음.
		HibernateTransactionManager txmanager = DataUtil.getBean(HibernateTransactionManager.class);
		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
			Meter meter = meterDao.get(meterId);
			if (meter != null && !stsNumber.equals(meter.getIhdId())) {
				meter.setIhdId(stsNumber);
				meterDao.update(meter);
			}

			txmanager.commit(txstatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	/**
	 * Async Command Result에 결과값을 저장
	 * 
	 * @param modemId
	 * @param dataMap
	 * @param trId
	 */
	private void addResult(String modemId, Map<String, Object> dataMap, Long trId) {
		log.debug("update Async-Command-Result Start");
		AsyncCommandResultDao resultDao = DataUtil.getBean(AsyncCommandResultDao.class);
		HibernateTransactionManager txmanager = DataUtil.getBean(HibernateTransactionManager.class);
		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
			Iterator<String> iter = dataMap.keySet().iterator();
			int cnt = 0;
			while (iter.hasNext()) {
				String key = iter.next();

				AsyncCommandResult result = new AsyncCommandResult();
				result.setMcuId(modemId);
				result.setNum(cnt);
				result.setTrId(trId);
				result.setResultType("result" + cnt);
				result.setResultValue(key + " : " + String.valueOf(dataMap.get(key)));
				resultDao.add(result);
				cnt = cnt + 1;
			}
			txmanager.commit(txstatus);
		} catch (Exception e) {
			if (txstatus != null && !txstatus.isCompleted())
				txmanager.rollback(txstatus);
			log.error(e, e);
		}
	}

	/**
	 * 월정산에 대한 PrepaymentLog를 생성한다. 12달 전의 월정산도 검색할 수 있으므로 12달 전까지는 언제 검색해도 해당
	 * 월의 Tariff가 적용되도록 관리해야 한다.
	 * 
	 * 2016.10
	 */
	private void createMonthlyLog(String meterId, String yyyymm, int consumption, double cost) {
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		TransactionStatus txstatus = null;		
		
		PrepaymentLogDao prepaymentLogDao = DataUtil.getBean(PrepaymentLogDao.class);
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);
		ContractDao contractDao = DataUtil.getBean(ContractDao.class);
		CustomerDao customerDao = DataUtil.getBean(CustomerDao.class);
		TariffEMDao tariffEmDao = DataUtil.getBean(TariffEMDao.class);
		TariffTypeDao tariffTypeDao = DataUtil.getBean(TariffTypeDao.class);
		OperatorDao operatorDao = DataUtil.getBean(OperatorDao.class);
		EcgSTSLogDao stslogDao = DataUtil.getBean(EcgSTSLogDao.class);
		log.info(">>> createMontlyLog");
		log.info("meterId : " + meterId);
		log.info("yyyymm : " + yyyymm);
		log.info("consumption : " + consumption);
		log.info("cost : " + cost);
		try {
			txstatus = txmanager.getTransaction(null);
			
			Meter meter = meterDao.get(meterId);
			Contract contract = meter.getContract();
			Customer customer = customerDao.get(contract.getCustomerId());
			TariffType tariffType = tariffTypeDao.get(contract.getTariffIndexId());
			String tariffName = tariffType.getName();
			// ECGSTSLog setTariff에서 가장 최근 데이터를 검색.
			EcgSTSLog lastTariffLog = stslogDao.getLastSetTariff(meter.getMdsId());

			List<PrepaymentLog> pLogList = prepaymentLogDao.getMonthlyConsumptionLogByGeocode(yyyymm, tariffName,
					contract.getContractNumber());
			if (pLogList.size() <= 0) {
				Operator operator = operatorDao.getOperatorByLoginId("admin");
				Map<String, Object> param = new HashMap<String, Object>();

				param.put("tariffIndex", tariffType);
				param.put("searchDate", yyyymm + "31");

				// levy, subsidy 등을 계산하기 위해 서버에서 등록된 Tariff 정책을 가져온다.
				// ※**** 단, 실제로 명령을 내린 Tariff 정책과 서버에 업데이트 된 Tariff 정책이 동일해야 한다.
				// *****
				List<TariffEM> tariffList = tariffEmDao.getApplyedTariff(param);

				Double totalAmount = cost;

				int totalUsage = consumption;

				TariffEM levyTariff = getTariffByUsage(tariffList, totalUsage);
				
				if (levyTariff == null) {
					log.info("skip \n" + "totalUsage: " + totalUsage + "tariffId: " + contract.getTariffIndexId());
				}
				
				double bill[] = blockBillWithLevy(tariffName, tariffList, totalUsage);
				Double govLevy = bill[0];
				Double publicLevy = bill[1];
				Double utilityRelief = bill[2];
				Double govSubsidy = bill[3];
				Double newSubsidy = bill[4];

				Double levy = govLevy + publicLevy;
				Double serviceCharge = 0d;
				Double lifeLineSubsidy = 0d;
				
				if (levyTariff != null) {
					serviceCharge = StringUtil.nullToDoubleZero(levyTariff.getServiceCharge());
					lifeLineSubsidy = StringUtil.nullToDoubleZero(levyTariff.getReactiveEnergyCharge());
				}

				// 사용량이 0이면 lifeLineSubsidy는 없다.
				if ("Residential".equals(tariffName) && totalUsage == 0) {
					lifeLineSubsidy = 0d;
				}

				// 사용량이 0~ 50 구간이면 utilityRelief는 없다. => 7월 1일자로 변경된 Tariff
				if ("Residential".equals(tariffName)
						&& (totalUsage < Integer.parseInt(lastTariffLog.getCondLimit2()))) {
					utilityRelief = 0d;
				}

				// 사용량이 150이상이면 govSubsidy는 없다.
				if ("Residential".equals(tariffName) && (totalUsage > Integer.parseInt(lastTariffLog.getCondLimit1()))) {
					govSubsidy = 0d;
				}

				contract = contractDao.get(contract.getId());
				Double vat = 0.0;
				
				if (levyTariff != null) {
					vat = StringUtil.nullToDoubleZero(levyTariff.getEnergyDemandCharge())
							* ((totalAmount - levy) + serviceCharge - newSubsidy);
				}
				
				// subsidy는 모든 subsidy의 합으로 계산한다.
				Double subsidy = govSubsidy + lifeLineSubsidy + newSubsidy;

				Double beforeCredit = 0d;
				/**
				 * 2016.05.01 이전에 현재 잔액 구하던 식 : currentCredit = beforeCredit -
				 * additionalAmount - serviceCharge - vat + subsidy +
				 * utilityRelief;
				 * 
				 * 2016.05.01 이후로 vat가 Daily 차감금액에 포함되어 계산되어 아래와 같이 변경됨.
				 */
				Double currentCredit = beforeCredit - serviceCharge + subsidy + utilityRelief;

				log.debug("\n=== Contract Number: " + contract.getContractNumber() + " ===" + "\n Before Credit: "
						+ StringUtil.nullToDoubleZero(contract.getCurrentCredit()) + "\n After Credit: " + currentCredit
						+ "\n Total Usage: " + totalUsage + "\n Total Amount: " + totalAmount + "\n utilityRelief: "
						+ utilityRelief + "\n Additional Amount: " + 0 + "\n Service Charge Amount: " + serviceCharge
						+ "\n Public Lavy: " + publicLevy + "\n Gov. Levy: " + govLevy + "\n VAT: " + vat
						+ "\n LifeLine Subsidy: " + lifeLineSubsidy + "\n Subsidy: " + govSubsidy + "\n new Subsidy: "
						+ newSubsidy);

				String afterYyyymm = DateTimeUtil.getPreDay(yyyymm + "01073000", -1).substring(0, 6);
				PrepaymentLog prepaymentLog = new PrepaymentLog();
				// Prepaymentlog의 ID 생성방법 변경(contract id+시스템시간)
				prepaymentLog.setId(
						Long.parseLong(Integer.toString(contract.getId()) + Long.toString(System.currentTimeMillis())));

				prepaymentLog.setLastTokenDate(afterYyyymm);
				prepaymentLog.setCustomer(customer);
				prepaymentLog.setContract(contract);
				prepaymentLog.setMonthlyTotalAmount(totalAmount);
				prepaymentLog.setMonthlyPaidAmount(totalAmount);
				prepaymentLog.setMonthlyServiceCharge(serviceCharge);
				prepaymentLog.setUsedConsumption(Double.parseDouble(StringUtil.nullToZero(totalUsage)));
				prepaymentLog.setUsedCost(0d);
				prepaymentLog.setPublicLevy(publicLevy);
				prepaymentLog.setGovLevy(govLevy);
				prepaymentLog.setVat(vat);
				prepaymentLog.setLifeLineSubsidy(lifeLineSubsidy);
				prepaymentLog.setSubsidy(govSubsidy);
				prepaymentLog.setAdditionalSubsidy(newSubsidy);
				prepaymentLog.setOperator(operator);
				prepaymentLog.setLocation(contract.getLocation());
				prepaymentLog.setTariffIndex(contract.getTariffIndex());
				prepaymentLog.setUtilityRelief(utilityRelief);
				prepaymentLogDao.add(prepaymentLog);
				
				txmanager.commit(txstatus);
			} else {
				log.info("already exist log");
			}

		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null)
				txmanager.rollback(txstatus);
			
		}
	}

	/**
	 * 
	 * @param arr
	 * @param i
	 * @param j
	 * @return
	 */
	private Object[] compareArr(Object[] arr, int i, int j) {
		Object temp = null;
		temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;

		return arr;
	}

	private TariffEM getTariffByUsage(List<TariffEM> tariffList, int usage) {
		TariffEM result = null;

		for (TariffEM tariff : tariffList) {
			Double min = tariff.getSupplySizeMin();
			Double max = tariff.getSupplySizeMax();

			if (min == null && max == null) {
				continue;
			}

			if ((min == null && usage <= max) || (min == 0 && usage <= max)) {
				result = tariff;
			} else if (max == null && usage > min) {
				result = tariff;
			} else if (usage > min && usage <= max) {
				result = tariff;
			}
		}
		return result;
	}

	public double[] blockBillWithLevy(String tariffName, List<TariffEM> tariffEMList, double usage) {
		double returnGovLevy = 0.0;
		double returnPublicLevy = 0.0;
		double returnUtilityRelief = 0.0;
		double returnGovSubsidy = 0.0;
		double returnNewSubsidy = 0.0;

		Collections.sort(tariffEMList, new Comparator<TariffEM>() {

			@Override
			public int compare(TariffEM t1, TariffEM t2) {
				return t1.getSupplySizeMin() < t2.getSupplySizeMin() ? -1 : 1;
			}
		});

		double supplyMin = 0.0;
		double supplyMax = 0.0;
		double block = 0.0;
		double blockUsage = 0.0;
		for (int cnt = 0; cnt < tariffEMList.size(); cnt++) {
			supplyMin = tariffEMList.get(cnt).getSupplySizeMin() == null ? 0.0
					: tariffEMList.get(cnt).getSupplySizeMin();
			supplyMax = tariffEMList.get(cnt).getSupplySizeMax() == null ? 0.0
					: tariffEMList.get(cnt).getSupplySizeMax();

			log.info("[" + cnt + "] supplyMin : " + supplyMin + ", supplyMax : " + supplyMax);

			// Tariff 첫 구간
			if (usage >= supplyMin) {
				if (supplyMax != 0) {
					block = supplyMax - supplyMin;

					blockUsage = usage - supplyMax;

					if (blockUsage < 0)
						blockUsage = usage - supplyMin;
					else
						blockUsage = block;
				} else {
					blockUsage = usage - supplyMin;
				}

				Double activeEnergy = tariffEMList.get(cnt).getActiveEnergyCharge() == null ? 0d
						: tariffEMList.get(cnt).getActiveEnergyCharge();
				Double govLevy = tariffEMList.get(cnt).getTransmissionNetworkCharge() == null ? 0d
						: tariffEMList.get(cnt).getTransmissionNetworkCharge();
				Double publicLevy = tariffEMList.get(cnt).getDistributionNetworkCharge() == null ? 0d
						: tariffEMList.get(cnt).getDistributionNetworkCharge();
				Double utilityRelief = tariffEMList.get(cnt).getMaxDemand() == null ? 0d
						: tariffEMList.get(cnt).getMaxDemand();
				Double govSubsidy = tariffEMList.get(cnt).getAdminCharge() == null ? 0d
						: tariffEMList.get(cnt).getAdminCharge();
				Double newSubsidy = tariffEMList.get(cnt).getRateRebalancingLevy() == null ? 0d
						: tariffEMList.get(cnt).getRateRebalancingLevy();

				Double tariffGovLevy = activeEnergy * govLevy;
				Double tariffPublicLevy = activeEnergy * publicLevy;
				govLevy = blockUsage * tariffGovLevy;
				publicLevy = blockUsage * tariffPublicLevy;
				utilityRelief = blockUsage * utilityRelief;
				govSubsidy = blockUsage * govSubsidy;
				newSubsidy = blockUsage * newSubsidy;

				returnGovLevy = returnGovLevy + govLevy;
				returnPublicLevy = returnPublicLevy + publicLevy;
				returnUtilityRelief = returnUtilityRelief + utilityRelief;
				returnGovSubsidy = returnGovSubsidy + govSubsidy;
				returnNewSubsidy = returnNewSubsidy + newSubsidy;

				log.info("govLevy: " + govLevy);
				log.info("publicLevy: " + publicLevy);
				log.info("utilityRelief: " + utilityRelief);
				log.info("govSubsidy: " + govSubsidy);
				log.info("newSubsidy: " + newSubsidy);
			}
		}

		return new double[] { returnGovLevy, returnPublicLevy, returnUtilityRelief, returnGovSubsidy,
				returnNewSubsidy };
	}

	private void validateEmergencyMode(String meterId, int mode) {
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		TransactionStatus txstatus = null;
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);

		try {
			txstatus = txmanager.getTransaction(null);
			Meter meter = meterDao.get(meterId);
			Contract contract = meter.getContract();

			if (contract != null) {
				// 계약정보에 설정된
				if (contract.getEmergencyCreditAvailable() && mode == 0 /* disable */) {
					// 문자 보내고 비동기 내역에 기록한다.
				}
			}
			txmanager.commit(txstatus);
		} catch (Exception e) {
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	private void validatePaymentMode(String meterId, int mode) {
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		TransactionStatus txstatus = null;
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);

		try {
			txstatus = txmanager.getTransaction(null);
			Meter meter = meterDao.get(meterId);
			Contract contract = meter.getContract();

			if (contract != null) {
				// 계약정보에 설정된
				if (contract.getCreditType().getName().equals("prepay") && (mode == 0 || mode == 1) /* disable */) {
					// 문자 보내고 비동기 내역에 기록한다.
				} else if (contract.getCreditType().getName().equals("postpay")
						&& (mode == 1 || mode == 2) /* enable */) {
					// 문자 보내고 비동기 내역에 기록한다.
				}
			}
			txmanager.commit(txstatus);
		} catch (Exception e) {
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	@Override
	public CommandData execute(HashMap<String, String> params, IoSession session, Client client) throws Exception {

		CommandData cd = null;
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		HashMap<String, String> argMap = new HashMap<String, String>();

		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		String cmd = params.get("cmd").toString();
		log.info("cmd: " + cmd);

		bd.setCommand(cmd);
		bd.setClient(client);

		if ("cmdGetMeterFWVersion".equals(cmd)) {
		} else if ("cmdDemandPeriod".equals(cmd)) {
		} else if ("cmdGetDemandPeriod".equals(cmd)) {
		} else if ("cmdGetBillingCycle".equals(cmd)) {
		} else if ("cmdSetBillingCycle".equals(cmd)) {
		} else if ("cmdAlarmReset".equals(cmd)) {
		} else if ("cmdGetMeterFWVersion".equals(cmd)) {
		} else if ("cmdTOUSet".equals(cmd)) {
		} else if ("cmdGetCurrentLoadLimit".equals(cmd)) {
		} else if ("cmdSetCurrentLoadLimit".equals(cmd)) {
		}
		/** ZigBee STS Command */
		else if ("cmdSetPaymentMode".equals(cmd)) {
			bd.addArg(params.get("mode").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSetPaymentMode(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetPaymentMode".equals(cmd)) {
			cmdGetPaymentMode(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetRemainingCredit".equals(cmd)) {
			cmdGetRemainingCredit(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSetSTSToken".equals(cmd)) {
			bd.addArg(params.get("token").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSetSTSToken(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetSTSToken".equals(cmd)) {
			bd.addArg(params.get("tCount").toString());
			session.setAttribute("getCount", params.get("tCount"));
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdGetSTSToken(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSetTariff".equals(cmd)) {
			bd.addArg(params.get("yyyymmdd").toString());
			bd.addArg(params.get("condLimit1").toString());
			bd.addArg(params.get("condLimit2").toString());
			bd.addArg(params.get("cons").toString());
			bd.addArg(params.get("fixedRate").toString());
			bd.addArg(params.get("varRate").toString());
			bd.addArg(params.get("condRate1").toString());
			bd.addArg(params.get("condRate2").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSetTariff(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetTariff".equals(cmd)) {
			bd.addArg(params.get("tariffMode").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdGetTariff(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSetFriendlyCreditSchedule".equals(cmd)) {
			String friendlyInfo = params.get("friendlyInfo").toString();
			log.info("cmdSetFriendlyCreditSchedule - friendlyInfo: " + friendlyInfo);

			String e_dt = "";
			int friendlyDayType = 0;
			String friendlyFromHHMM = null; 
			String friendlyEndHHMM = null;

			String[] firendlyInfoArr = friendlyInfo.split("&");
			for (int i = 0; i < firendlyInfoArr.length; i++) {
				if (i == 0) {
					e_dt = firendlyInfoArr[i];
				} else if (i == 1) {
					friendlyDayType = Integer.parseInt(firendlyInfoArr[i]);
				} else if (i == 2) {
					friendlyFromHHMM = firendlyInfoArr[i];
				} else if (i == 3) {
					friendlyEndHHMM = firendlyInfoArr[i];
				}  
			}

			bd.addArg(e_dt);
			bd.addArg(Integer.toString(friendlyDayType));
			bd.addArg(friendlyFromHHMM);
			bd.addArg(friendlyEndHHMM);
			
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSetFriendlyCreditSchedule(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetFriendlyCreditSchedule".equals(cmd)) {
			bd.addArg(params.get("fcMode").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdGetFriendlyCreditSchedule(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSetEmergencyCredit".equals(cmd)) {
			bd.addArg(params.get("ec_mode").toString());
			bd.addArg(params.get("days").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSetEmergencyCredit(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetEmergencyCredit".equals(cmd)) {
			cmdGetEmergencyCredit(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetPreviousMonthNetCharge".equals(cmd)) {
			cmdGetPreviousMonthNetCharge(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetSpecificMonthNetCharge".equals(cmd)) {
			bd.addArg(params.get("month").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdGetSpecificMonthNetCharge(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSetMessage".equals(cmd)) {
			bd.addArg(params.get("message").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSetMessage(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSuniFirmwareWrite".equals(cmd)) {
			bd.addArg(params.get("filePath").toString());
			session.setAttribute("modemType", params.get("modemType").toString());
			cmdSuniFirmwareWrite(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSuniFirmwareRead".equals(cmd)) {
			cmdSuniFirmwareRead(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSuniFirmwareUpdateKeyWrite".equals(cmd)) {
			bd.addArg(params.get("keyNo").toString());
			bd.addArg(params.get("keyHex").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSuniFirmwareUpdateKeyWrite(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSuniFirmwareUpdateKeyRead".equals(cmd)) {
			cmdSuniFirmwareUpdateKeyRead(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetSuniFirmwareUpdateInfo".equals(cmd)) {
			cmdGetSuniFirmwareUpdateInfo(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetCIUCommStateHistory".equals(cmd)) {
			cmdGetCIUCommStateHistory(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSetSTSSetup".equals(cmd)) {
			bd.addArg(params.get("date").toString());
			bd.addArg(params.get("kct1").toString());
			bd.addArg(params.get("kct2").toString());
			bd.addArg(params.get("stsNumber").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			cmdSetSTSSetup(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetSTSSetup".equals(cmd)) {
			cmdGetSTSSetup(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdSetRFSetup".equals(cmd)) {
			bd.addArg(params.get("channel").toString());
			bd.addArg(params.get("panId").toString());
			session.setAttribute(session.getRemoteAddress(), bd);
			
			cmdSetRFSetup(session);
			((BYPASSClient) client).sendBypass(session);
		} else if ("cmdGetRFSetup".equals(cmd)) {
			cmdGetRFSetup(session);
			((BYPASSClient) client).sendBypass(session);
		}

		return cd;
	}

	protected void saveMeterTimeSyncLog(String modemId, boolean isSuccess) {
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
			ModemDao modemDao = DataUtil.getBean(ModemDao.class);
			Modem modem = modemDao.get(modemId);
			Set<Meter> meters = modem.getMeter();
			Iterator<Meter> it = meters.iterator();
			Meter meter = null;
			MeterTimeSyncData mtdata;

			while (it.hasNext()) {
				meter = it.next();
				mtdata = new MeterTimeSyncData();
				String time = DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss");
				// 미터시간을 받지 않기 때문에 그냥 현재 서버시간 등록
				mtdata.setAtime(" ");
				mtdata.setBtime(" ");
				mtdata.setCtime(time);
				mtdata.setContent("");
				mtdata.setUserID("system");
				mtdata.setTimediff(meter.getTimeDiff());

				if (isSuccess)
					mtdata.setResult(0);
				else
					mtdata.setResult(1);
				saveMeterTimeSyncLog(meter, mtdata);
			}
			txmanager.commit(txstatus);
		} catch (Exception e) {
			log.warn("saveMeterTimeSyncLog error - " + e.getMessage(), e);
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	protected void saveMeterTimeSyncLog(Meter meter, MeterTimeSyncData data) {

		log.debug("save MeterTimeSyncLog[" + meter.getMdsId() + "]");
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		MeterTimeSyncLogDao meterTimeSyncLogDao = DataUtil.getBean(MeterTimeSyncLogDao.class);
		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);

			if (data.getCtime() == null || "".equals(data.getCtime())) { // 주키값이 없으면 insert 할수 없다.
				throw new Exception("Meter time is wrong. check meter time!!");
			}

			MeterTimeSyncLog mt = new MeterTimeSyncLog();
			mt.setAfterDate(data.getAtime());
			mt.setBeforeDate(data.getBtime());
			mt.setDescr(data.getContent());
			mt.setLocation(meter.getLocation());
			mt.setMeter(meter);
			mt.setMeterDate(data.getCtime());
			mt.setOperator(data.getUserID());
			mt.setOperatorType(OperatorType.SYSTEM.name());

			switch (data.getResult()) {
			case 0:
				mt.setResult(ResultStatus.SUCCESS.name());
				break;
			case 1:
				mt.setResult(ResultStatus.FAIL.name());
				break;
			case 2:
				mt.setResult(ResultStatus.INVALID_PARAMETER.name());
				break;
			case 3:
				mt.setResult(ResultStatus.COMMUNICATION_FAIL.name());
				break;
			}

			mt.setSupplier(meter.getSupplier());
			mt.setTimeDiff(data.getTimediff());
			mt.setWriteDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
			mt.setId(TimeUtil.getCurrentLongTime());
			meterTimeSyncLogDao.add(mt);

			txmanager.commit(txstatus);
		} catch (Exception e) {
			log.warn("saveMeterTimeSyncLog error - " + e.getMessage(), e);
			if (txstatus != null)
				txmanager.rollback(txstatus);
		}
	}

	@Override
	public CommandData executeBypassClient(byte[] frame, IoSession session) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(String cmd, int errorCode, IoSession session) throws Exception {
		BypassDevice bd = (BypassDevice) session.getAttribute(session.getRemoteAddress());
		HibernateTransactionManager txmanager = (HibernateTransactionManager) DataUtil.getBean("transactionManager");
		TransactionStatus txstatus = null;

		AsyncCommandLogDao acld = DataUtil.getBean(AsyncCommandLogDao.class);
		ModemDao modemDao = DataUtil.getBean(ModemDao.class);
		List<AsyncCommandLog> acllist = null;
		
		try {
			txstatus = txmanager.getTransaction(null);
			AsyncCommandLog acl = null;
			Set<Condition> condition = new HashSet<Condition>();
			condition.add(new Condition("id.trId", new Object[] { bd.getAsyncTrId() }, null, Restriction.EQ));
			acllist = acld.findByConditions(condition);
			acl = acllist.get(0);

			// Set status, errorcode (S)
			acl.setState(TR_STATE.Success.getCode());
			acl.setErrorCode(errorCode);
			acld.update(acl);
			// Set status, errorcode (E)
			
			// Async_Result 저장 (S)
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Result", ErrorCode.getCodeString(errorCode) + ", " + ErrorCode.getMessage(errorCode));
			addResult(bd.getModemId(), map, bd.getAsyncTrId());
			// Async_Result 저장 (E)

			txmanager.commit(txstatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null && !txstatus.isCompleted())
				txmanager.rollback(txstatus);
		}

	}
}
