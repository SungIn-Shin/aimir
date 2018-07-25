package com.aimir.schedule.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import com.aimir.constants.CommonConstants.DeviceType;
import com.aimir.dao.AbstractHibernateGenericDao;
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.mvm.BillingBlockTariffDao;
import com.aimir.dao.mvm.DayEMDao;
import com.aimir.dao.mvm.LpEMDao;
import com.aimir.dao.mvm.impl.DayEMDaoImpl;
import com.aimir.dao.mvm.impl.LpEMDaoImpl;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.PrepaymentLogDao;
import com.aimir.dao.system.TariffEMDao;
import com.aimir.dao.system.TariffTypeDao;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.FMPProperty;
import com.aimir.model.device.Meter;
import com.aimir.model.mvm.BillingBlockTariff;
import com.aimir.model.mvm.LpEM;
import com.aimir.model.system.Code;
import com.aimir.model.system.Contract;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.TariffEM;
import com.aimir.model.system.TariffType;
import com.aimir.util.Condition;
import com.aimir.util.Condition.Restriction;
import com.aimir.util.DateTimeUtil;

/**
 * @STS 일반미터제외한 STS미터를 대상으로 함.
 * ECG의 Block Tariff간 선불 요금 차액을 계산하는 클래스.
 * 기존 ECG의 Tariff 변경 및 정산주기는 매월 1일이고, 이에 따라 각 스케줄이 개발되어있다. 
 * 그로인해 1일이 아닌 월중간에 Tariff 변경이 발생하면 신규 Tariff를 즉각 반영하지 못하는 문제 발생.
 * 따라서 Old, New Tariff를 변경 일자기준으로 각각 분할계산하고, 두 요금의 차액을 PrepaymentLog에 생성해준다.
 * 생성한 PrepaymentLog를 영수증으로 출력하면 'Refund' 항목으로 나타나게함.
 * 계산식은 "ECGBlockDailyEMBillingInfoSaveV4Task"를 따름.
 * 
 * @parameter targetDate:변경일자(yyyyMMdd)
 * @fixed targetDate, LastDay Of Month, AppliedDate of Tariff
 * @table Tariff_em_future: New Tariff를 임시 저장할 테이블 
 *
 * @author sejin
 *
 */
@Service
public class ECGBlockDailyEMBillingV4_TariffChange_STS extends ScheduleTask {
	protected static Log log = LogFactory.getLog(ECGBlockDailyEMBillingV4_TariffChange_STS.class);
	private static final String SERVICE_TYPE_EM = "Electricity";
	
	@Resource(name="transactionManager")
    HibernateTransactionManager txmanager;
    
    @Autowired
    ContractDao contractDao;
    
    private boolean isNowRunning = false;
    
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"spring-billingtask.xml"}); 
        DataUtil.setApplicationContext(ctx);
        
        ECGBlockDailyEMBillingV4_TariffChange_STS task = ctx.getBean(ECGBlockDailyEMBillingV4_TariffChange_STS.class);
        task.execute(null);
        System.exit(0);
    }
	
    public void execute(JobExecutionContext context) {
        if(isNowRunning){
            log.info("########### ECGBlockDailyEMV4 TariffChange_STS is already running...");
            return;
        }
        isNowRunning = true;
        log.info("########### START ECGBlockDailyEM TariffChange_STS ###############");
        
        // 일별 전기 요금 정보 등록
        this.calcEmBillingDayInfo();
        
        log.info("########### END ECGBlockDailyEM TariffChange_STS ############");
        isNowRunning = false;        
    }//execute end
    
    
    public void calcEmBillingDayInfo() {
        
        // 전기 계약 정보 취득
        List<Integer> em_contractIds = this.getSTSContractInfos(SERVICE_TYPE_EM); 

        int poolSize = Integer.parseInt(FMPProperty.getProperty("block.billing.thread.pool.size", ""+5));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize, poolSize, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        
        for(Integer contract_id : em_contractIds) {
            // log.info("ContractId[" + contract_id + "]");
            try {
                executor.execute(new BlockBillingThread_TariffChange_STS(contract_id));
            }
            catch (Exception e) {
                log.error(e, e);
            }
        }
        
        try {
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        }
        catch (Exception e) {}
        // setSuccessResult();
    }
    
    private List<Integer> getSTSContractInfos(String serviceType) {
    	TransactionStatus txstatus = null;
        List<Integer> contractIds = new ArrayList<Integer>();
        
        try {
            txstatus = txmanager.getTransaction(null);
            Set<Condition> condition = new HashSet<Condition>();
            
            condition.add(new Condition("serviceTypeCode", new Object[]{"s"}, null, Restriction.ALIAS));
            condition.add(new Condition("s.name", new Object[]{serviceType}, null, Restriction.EQ));
            condition.add(new Condition("creditType", new Object[]{"c"}, null, Restriction.ALIAS));
            condition.add(new Condition("c.code", new Object[]{Code.PREPAYMENT, Code.EMERGENCY_CREDIT}, null, Restriction.IN));
            // STS 전용 
            condition.add(new Condition("isSts", new Object[]{true}, null, Restriction.EQ));
            // NetMetering 제외
            condition.add(new Condition("isNetMetering", new Object[]{false}, null, Restriction.EQ));
            List<Contract> contracts = contractDao.findByConditions(condition);
            txmanager.commit(txstatus);
            
            for (Contract c : contracts) {
                contractIds.add(c.getId());
            }
            
        }catch (Exception e) {
            log.error(e, e);
            if (txstatus != null) txmanager.rollback(txstatus);
        }
        return contractIds;
	}
    
    
}


class BlockBillingThread_TariffChange_STS implements Runnable {
    private static Log log = LogFactory.getLog(BlockBillingThread_TariffChange_STS.class);
    
    HibernateTransactionManager txmanager = (HibernateTransactionManager)DataUtil.getBean("transactionManager");
    ContractDao contractDao = DataUtil.getBean(ContractDao.class);
    LpEMDao lpEMDao = DataUtil.getBean(LpEMDao.class);
    DayEMDao dayEMDao = DataUtil.getBean(DayEMDao.class);
    BillingBlockTariffDao billingBlockTariffDao = DataUtil.getBean(BillingBlockTariffDao.class);
    MeterDao meterDao = DataUtil.getBean(MeterDao.class);
    TariffTypeDao tariffTypeDao = DataUtil.getBean(TariffTypeDao.class);
    TariffEMDao tariffEMDao = DataUtil.getBean(TariffEMDao.class);
    PrepaymentLogDao prepaymentLogDao = DataUtil.getBean(PrepaymentLogDao.class);
    
    private int contract_id;
    
    public BlockBillingThread_TariffChange_STS(int contractId) {
        this.contract_id = contractId;
    }
    
    /**
     * 
     * @author jiae
     * @desc   가나 ECG 에서 사용하는 채널
     *
     */
    enum KamstrupChannel {
        ActiveEnergyImp(1), ActiveEnergyExp(2), ReactiveEnergyImp(3), ReactiveEnergyExp(4);
        
        private Integer channel;
        
        KamstrupChannel(Integer channel) {
            this.channel = channel;
        }
        
        public Integer getChannel() {
            return this.channel;
        }
    }
    

    @Override
    public void run() {
        calcEmBillingDailyWithTariffEMCumulationCost();
    }
    
    public void calcEmBillingDailyWithTariffEMCumulationCost()
    {
        TransactionStatus txstatus = null;
        try {
            txstatus = txmanager.getTransaction(null);
            txmanager.setDefaultTimeout(-1);
            Contract contract = contractDao.get(contract_id);
            Code code = contract.getCreditType();
            if (contract.getTariffIndexId() != null && contract.getCustomer() != null && contract.getMeter() != null) {
                if(Code.PREPAYMENT.equals(code.getCode()) || Code.EMERGENCY_CREDIT.equals(code.getCode())) { // 선불 요금일 경우
                    log.info("CalcStart: Contract[" + contract.getContractNumber() + "] Meter[" + contract.getMeter().getMdsId() + "]");
                    
                    //STS Number 체크
                	String ihdId = contract.getMeter().getIhdId();
                    if(ihdId == null || ihdId.length()!=11) {
                    	//토큰 생성할 STS Number 없음. 종료.
                    	log.info("Warning(Canceled): Contract["+ contract.getContractNumber() +"] STS Number is not valid.");
                    	throw new Exception();
                    }
                    
                    /**
                     * 1일자, 31일자 Billing_Block_Tariff 생성
                     */
                    // 31일 계산 (30일까진ok)
                    String maxYyyymmddA = "20180331";
                    LpEM[] lastLpA = getLastLp(contract.getMeter().getMdsId(), maxYyyymmddA+"00", maxYyyymmddA+"23");
                    if(lastLpA == null || lastLpA.length != 2) {
                    	maxYyyymmddA = "20180330";
                    	lastLpA = getLastLp(contract.getMeter().getMdsId(), maxYyyymmddA+"00", maxYyyymmddA+"23");
                    }
                    
                    // 1일 LP
                    String baseYyyymmdd = "20180301";
                    LpEM[] baseLp = getLastLp(contract.getMeter().getMdsId(), baseYyyymmdd+"00", baseYyyymmdd+"23");
                    if(baseLp == null || baseLp.length != 2) {
                    	baseYyyymmdd = "20180302";
                    	baseLp = getLastLp(contract.getMeter().getMdsId(), baseYyyymmdd+"00", baseYyyymmdd+"23");
                    }
                    

                    // calc
                    if (lastLpA != null && lastLpA.length == 2 && contract.getMeter().getModem() != null
                    		&& baseLp != null && baseLp.length == 2) {
                        
                    	// 3월1일의 LP로 초기 Billing데이터 생성.
                        BillingBlockTariff prevBill = getStsBillingBlockTariff(contract.getMeter().getMdsId(), baseLp);
                        if(prevBill == null){
                        	//계산할 데이터 없음. 종료.
                        	log.info("Warning(Canceled): Contract["+ contract.getContractNumber() +"] No Base Billing Data.");
                        	throw new Exception();
                        }
                    	
                        //기존 Tariff로 계산 (3월에 사용한 tariff)
                        saveBill(contract, lastLpA, prevBill, "20170501");
                        
                        log.info("Billing finish: Contract[" + contract.getContractNumber() + "]");
                    }else {
                    	log.info("Contract[" + contract.getContractNumber() + "] Meter[" + contract.getMeter().getMdsId() + "] No LP Data");
                    }
                }
            }
            if (!txstatus.isCompleted())
                txmanager.commit(txstatus);
        }
        catch (Exception e) {
            log.error(e, e);
            if (txstatus != null && !txstatus.isCompleted())
                txmanager.rollback(txstatus);
        }
    }
    
    /**
     * 계약일이 널이 아니면 이전 LP 중 제일 마지막 LP를 가져온다.
     * 계약일이 널이면 제일 큰 값으로 가져온다.
     * @param meterId
     * @return
     */
    public LpEM[] getLastLp(String meterId, String fromYyyymmddhh, String toYyyymmddhh) {
        TransactionStatus txstatus = null;
        try {
            txstatus = txmanager.getTransaction(null);
            Set<Condition> conditions = new LinkedHashSet<Condition>();
            conditions.add(new Condition("id.yyyymmddhh", new Object[]{fromYyyymmddhh, toYyyymmddhh}, null, Restriction.BETWEEN));
            conditions.add(new Condition("id.mdevId", new Object[]{meterId}, null, Restriction.EQ));
            conditions.add(new Condition("id.mdevType", new Object[]{DeviceType.Meter}, null, Restriction.EQ));
            conditions.add(new Condition("id.dst", new Object[]{0}, null, Restriction.EQ));
            conditions.add(new Condition("id.channel", new Object[]{KamstrupChannel.ActiveEnergyImp.getChannel()},
                    null, Restriction.EQ));
            
            List<Projection> projections = new ArrayList<Projection>();
            projections.add(Projections.alias(Projections.max("id.yyyymmddhh"), "maxYyyymmddhh"));
            
            List<Map<String, Object>> maxyyyymmddhh = ((LpEMDaoImpl) lpEMDao).findByConditionsAndProjections(conditions, projections);
            
            if (maxyyyymmddhh != null && maxyyyymmddhh.size() == 1) {
                log.info(maxyyyymmddhh.get(0));
                String _yyyymmddhh = (String)maxyyyymmddhh.get(0).get("maxYyyymmddhh");
                if (_yyyymmddhh == null) {
                    txmanager.commit(txstatus);
                    return null;
                }
                
                conditions = new LinkedHashSet<Condition>();
                conditions.add(new Condition("id.yyyymmddhh", new Object[]{_yyyymmddhh}, null, Restriction.EQ));
                conditions.add(new Condition("id.mdevId", new Object[]{meterId}, null, Restriction.EQ));
                conditions.add(new Condition("id.mdevType", new Object[]{DeviceType.Meter}, null, Restriction.EQ));
                conditions.add(new Condition("id.dst", new Object[]{0}, null, Restriction.EQ));
                conditions.add(new Condition("id.channel", new Object[]{KamstrupChannel.ActiveEnergyImp.getChannel(),
                        KamstrupChannel.ActiveEnergyExp.getChannel()}, null, Restriction.IN));
                
                return (LpEM[])lpEMDao.findByConditions(conditions).toArray(new LpEM[0]);
            }
            txmanager.commit(txstatus);
        }
        catch (Exception e) {
            log.error(e, e);
            if (txstatus != null) txmanager.rollback(txstatus);
        }
        return null;
    }
    
    /**
     * DAY 검침테이블에서 마지막 날짜를 가져온다.
     * @param meterId
     * @return
     */
    public String getMaxYyyymmdd(String meterId, String meterInstallDate) {
        TransactionStatus txstatus = null;
        try {
            txstatus = txmanager.getTransaction(null);
            Set<Condition> conditions = new LinkedHashSet<Condition>();
            conditions.add(new Condition("id.yyyymmdd", new Object[]{meterInstallDate.substring(0, 8)}, null, Restriction.GE));
            conditions.add(new Condition("id.mdevId", new Object[]{meterId}, null, Restriction.EQ));
            conditions.add(new Condition("id.mdevType", new Object[]{DeviceType.Meter}, null, Restriction.EQ));
            conditions.add(new Condition("id.dst", new Object[]{0}, null, Restriction.EQ));
            conditions.add(new Condition("id.channel", new Object[]{KamstrupChannel.ActiveEnergyImp.getChannel()},
                    null, Restriction.EQ));
            
            List<Projection> projections = new ArrayList<Projection>();
            projections.add(Projections.alias(Projections.max("id.yyyymmdd"), "maxYyyymmdd"));
            
            List<Map<String, Object>> maxyyyymmdd = ((DayEMDaoImpl) dayEMDao).findByConditionsAndProjections(conditions, projections);
            
            log.info(maxyyyymmdd.get(0));
            String _yyyymmdd = (String)maxyyyymmdd.get(0).get("maxYyyymmdd");
            txmanager.commit(txstatus);
            
            return _yyyymmdd;
        }
        catch (Exception e) {
            log.error(e, e);
            if (txstatus != null) txmanager.rollback(txstatus);
        }
        return null;
    }
    
    /**
     * 고객의 계약일 기준으로 날짜가 크거나 또는 계약 관계가 널인 것인 최초의 LP를 가져온다.
     * @param meterId
     * @return
     */
    public LpEM[] getFirstLp(String meterId, String contractNumber, String contractDate) {
        Set<Condition> conditions = new LinkedHashSet<Condition>();
        
        // 계약정보가 널인 것으로 먼저 찾고
        conditions.add(new Condition("id.yyyymmddhh", new Object[]{contractDate.substring(0, 10)}, null, Restriction.GE));
        conditions.add(new Condition("id.mdevId", new Object[]{meterId}, null, Restriction.EQ));
        conditions.add(new Condition("id.mdevType", new Object[]{DeviceType.Meter}, null, Restriction.EQ));
        conditions.add(new Condition("id.dst", new Object[]{0}, null, Restriction.EQ));
        conditions.add(new Condition("id.channel", new Object[]{KamstrupChannel.ActiveEnergyImp.getChannel()},
                null, Restriction.EQ));
        conditions.add(new Condition("contract", null, null, Restriction.NULL));
        // conditions.add(new Condition("c.contractNumber", null, null, Restriction.NULL));
        
        List<Projection> projections = new ArrayList<Projection>();
        projections.add(Projections.alias(Projections.min("id.yyyymmddhh"), "minYyyymmddhh"));
        
        List<Map<String, Object>> minyyyymmddhh = ((LpEMDaoImpl) lpEMDao).findByConditionsAndProjections(conditions, projections);
        
        String yyyymmddhh_contractnull = (String)minyyyymmddhh.get(0).get("minYyyymmddhh");
        
        // 계약정보가 있는 것으로 찾는다.
        conditions = new LinkedHashSet<Condition>();
        conditions.add(new Condition("id.yyyymmddhh", new Object[]{contractDate.substring(0, 10)}, null, Restriction.GE));
        conditions.add(new Condition("id.mdevId", new Object[]{meterId}, null, Restriction.EQ));
        conditions.add(new Condition("id.mdevType", new Object[]{DeviceType.Meter}, null, Restriction.EQ));
        conditions.add(new Condition("id.dst", new Object[]{0}, null, Restriction.EQ));
        conditions.add(new Condition("id.channel", new Object[]{KamstrupChannel.ActiveEnergyImp.getChannel()},
                null, Restriction.EQ));
        conditions.add(new Condition("contract", new Object[]{"c"}, null, Restriction.ALIAS));
        conditions.add(new Condition("c.contractNumber", new Object[]{contractNumber}, null, Restriction.EQ));
        
        minyyyymmddhh = ((LpEMDaoImpl) lpEMDao).findByConditionsAndProjections(conditions, projections);
        
        String yyyymmddhh_contract = (String)minyyyymmddhh.get(0).get("minYyyymmddhh");
        
        String _yyyymmddhh = null;
        if (yyyymmddhh_contractnull == null && yyyymmddhh_contract == null)
            return null;
        else if (yyyymmddhh_contractnull != null && yyyymmddhh_contract == null)
            _yyyymmddhh = yyyymmddhh_contractnull;
        else if (yyyymmddhh_contractnull == null && yyyymmddhh_contract != null)
            _yyyymmddhh = yyyymmddhh_contract;
        else {
            if (yyyymmddhh_contractnull.compareTo(yyyymmddhh_contract) > 0)
                _yyyymmddhh = yyyymmddhh_contract;
            else
                _yyyymmddhh = yyyymmddhh_contractnull;
        }

        log.info(_yyyymmddhh);
        conditions = new LinkedHashSet<Condition>();
        conditions.add(new Condition("id.yyyymmddhh", new Object[]{_yyyymmddhh}, null, Restriction.EQ));
        conditions.add(new Condition("id.mdevId", new Object[]{meterId}, null, Restriction.EQ));
        conditions.add(new Condition("id.mdevType", new Object[]{DeviceType.Meter}, null, Restriction.EQ));
        conditions.add(new Condition("id.dst", new Object[]{0}, null, Restriction.EQ));
        conditions.add(new Condition("id.channel", new Object[]{KamstrupChannel.ActiveEnergyImp.getChannel(),
                KamstrupChannel.ActiveEnergyExp.getChannel()}, null, Restriction.IN));
        
        return (LpEM[])lpEMDao.findByConditions(conditions).toArray(new LpEM[0]);
    }
    
    
    /**
     * STS용 BillingBlockTariff를 만들어준다.
     * STS는 서버계산이 아니므로, BLT가 없기때문에 LP를 이용하여 필요한 정보를 세팅해준다.
     * (모듈이 자체계산하여 업로드한 잔액정보를 PrepaymentLog에 저장)
     * @param lastLp는 반드시 1일자 또는 맨처음이여야 함.(누적 ActiveEnergy를 LP값으로 세팅하기때문)
     * @return
     */
    public BillingBlockTariff getStsBillingBlockTariff(String meterId, LpEM[] lastLp) throws Exception {
        
    	// STS계약을 위한 BillingBlockTariff를 LP정보를 이용해 생성해준다.
        if (lastLp[0].getContract() != null && lastLp[0].getYyyymmdd() != null && lastLp[0].getHour() != null) {
        	BillingBlockTariff bbt = new BillingBlockTariff();
            bbt.setMDevType("Meter");
            bbt.setMDevId(meterId);
            
            String lp_yyyymmdd = lastLp[0].getYyyymmdd();
            bbt.setYyyymmdd(lp_yyyymmdd);
            
            String lp_hhmmss = lastLp[0].getHour() + "0000";
            bbt.setHhmmss(lp_hhmmss);

            bbt.setAccumulateBill(0.0);
            bbt.setAccumulateUsage(0.0);
            
            double actEnergy = lastLp[0].getValue() + lastLp[1].getValue();
            double actEnergyImport = lastLp[0].getValue();
            double actEnergyExport = lastLp[1].getValue();
            bbt.setActiveEnergy(actEnergy);
            bbt.setActiveEnergyImport(actEnergyImport);
            bbt.setActiveEnergyExport(actEnergyExport);
            
            return bbt;
        }

        return null;        
    }
    
    
    /**
     * 마지막 BillingBlockTariff를 가져온다.
     * 만약 BillingBlockTariff 정보가 없으면 고객의 계약기간에 해당하는 LP를 가져와서 누적치를 이용하는데
     * 이것도 없으면 미터 설치시간값을 넘기고 active energy값은 0이다.
     * 
     * @param meterId
     * @return
     */
    public BillingBlockTariff getLastBillingBlockTariff(String meterId, LpEM[] lastLp) throws Exception {
        // 미터 시리얼번호로 가장 최근 빌링 정보를 가져온다.
        List<Map<String, Object>> list = billingBlockTariffDao.getLastAccumulateBill(meterId);

        Map<String, Object> map = null;
        String contractNumber = null;
        if (list != null && list.size() == 1) {
            map = list.get(0);
            contractNumber = (String)map.get("CONTRACTNUMBER");
        }
        // 기 빌링정보와 LP의 계약 정보가 같은 경우만 누적치를 적용하고 나머지는 전부 새로 생성한다. 
        if (map != null && contractNumber != null && 
                lastLp[0].getContract() != null &&
                lastLp[0].getContract().getContractNumber().equals(contractNumber)) {
            BillingBlockTariff bbt = new BillingBlockTariff();
            bbt.setMDevType("Meter");
            bbt.setMDevId(meterId);
            bbt.setYyyymmdd((String)map.get("YYYYMMDD"));
            bbt.setHhmmss((String)map.get("HHMMSS"));
            bbt.setAccumulateBill((Double)map.get("ACCUMULATEBILL"));
            bbt.setAccumulateUsage((Double)map.get("ACCUMULATEUSAGE"));
            
            // 고객 계약이 같지 않지만 billingblocktariff의 마지막 누적값을 이용해야 한다.
            Object activeEnergy = map.get("ACTIVEENERGY");
            Object activeEnergyImport = map.get("ACTIVEENERGYIMPORT");
            Object activeEnergyExport = map.get("ACTIVEENERGYEXPORT");
            bbt.setActiveEnergy(activeEnergy == null? 0.0:(Double)activeEnergy);
            bbt.setActiveEnergyImport(activeEnergyImport == null? 0.0:(Double)activeEnergyImport);
            bbt.setActiveEnergyExport(activeEnergyExport == null? 0.0:(Double)activeEnergyExport);
            
            return bbt;
        }
        // 고객의 billingblocktariff가 없으면 미터에 대해서 선불계산한 적이 없는 최초 설치이기 때문에
        // 고객의 계약 기준으로 최초값을 가져온다.
        else {
            Meter meter = meterDao.get(meterId);
            Contract contract = meter.getContract();
            
            if (contract == null) return null;
            
            String contractDate = contract.getContractDate();
            
            BillingBlockTariff bbt = new BillingBlockTariff();
            bbt.setMDevType("Meter");
            bbt.setMDevId(meterId);
            bbt.setAccumulateBill(0.0);
            bbt.setAccumulateUsage(0.0);
            
            if (contractDate != null && !"".equals(contractDate)) {
                bbt.setYyyymmdd(contractDate.substring(0, 8));
                bbt.setHhmmss(contractDate.substring(8, 10) + "0000");
                
                // 미터설치일 이후와 계약일 이전 LP중 마지막 LP를 가져온다.
                LpEM[] lps = getLastLp(meterId, meter.getInstallDate(), contractDate.substring(0, 10));
                
                // 고객 계약일 이전 미터가 설치된 경우 또는 고객이 이사하여 미터가 변경된 경우로
                // 계약일이 반드시 변경되거나 새로 생성되어야 한다.
                if (lps != null && lps.length == 2) {
                    double activeEnergy = lps[0].getValue() + lps[1].getValue();
                    // lp 날짜와 계약일자의 차이가 발생하면 일평균을 구하여 차이만큼을 누적치에서 더한다.
                    // contractDate가 무조건 더 크다.
                    if (contractDate.substring(0, 8).compareTo(lps[0].getYyyymmdd()) == 0) {
                        bbt.setYyyymmdd(lps[0].getYyyymmdd());
                        bbt.setHhmmss(lps[0].getYyyymmddhh().substring(8, 10) + "0000");
                        bbt.setActiveEnergy(activeEnergy);
                        bbt.setActiveEnergyImport(lps[0].getValue());
                        bbt.setActiveEnergyExport(lps[1].getValue());
                    }
                    // contractDate와 lp의 yyyymmdd의 차이 일수만큼 activeEnergy 값을 만든다. 
                    else {
                        double lastActiveEnergy = lastLp[0].getValue() + lastLp[1].getValue() -
                                lps[0].getValue() - lps[1].getValue();
                        double usageFromLpDateContractDate = calUsageFromLpDateContractDate(lastActiveEnergy, 
                                lps[0].getYyyymmddhh(), lastLp[0].getYyyymmddhh(), contractDate);
                        activeEnergy += usageFromLpDateContractDate;
                        bbt.setActiveEnergy(activeEnergy);
                        bbt.setActiveEnergyImport(lps[0].getValue() + usageFromLpDateContractDate);
                        bbt.setActiveEnergyExport(lps[1].getValue());
                    }
                }
                // 고객의 계약일보다 작은 마지막 lp가 없으면 계약일 이후 LP 중 최초값을 가져온다.
                // 미터정보가 먼저 생성되어 계약이 먼저 생성된 경우나 미터가 교체된 경우에 해당할 것 같다.
                else {
                    // LP는 있는데 고객이 다르면 미터와 고객이 매핑된 첫번재 값을 가져온다.
                    LpEM[] firstLps = getFirstLp(meterId, contract.getContractNumber(), contract.getContractDate());
                 
                    // 평균값을 빼서 계약일 기준값으로 만든다.
                    double lastActiveEnergy = lastLp[0].getValue() + lastLp[1].getValue() -
                            firstLps[0].getValue() - firstLps[1].getValue();
                    
                    // 미터 설치일이 계약일보다 빠르면 계약일부터 계산하기 위해 평균치를 계산하여 일수만큼의 사용량을 구한다.
                    if (meter.getInstallDate().substring(0, 8).compareTo(contract.getContractDate().substring(0, 8)) < 0) {
                        double usageContractFromLpDate = 
                                calContractDateFromLpDate(lastActiveEnergy, firstLps[0].getYyyymmddhh(),
                                        lastLp[0].getYyyymmddhh(), contractDate);
                        // 계약일부터 사용량을 구하지만 첫번째 지침값보다 크면 첫번째 지침값을 그대로 사용한다.
                        if (firstLps[0].getValue() + firstLps[1].getValue() - usageContractFromLpDate < 0) {
                            bbt.setYyyymmdd(firstLps[0].getYyyymmdd());
                            bbt.setHhmmss(firstLps[0].getYyyymmddhh().substring(8, 10) + "0000");
                            bbt.setActiveEnergy(0);
                        }
                        else
                            bbt.setActiveEnergy(firstLps[0].getValue() + firstLps[1].getValue() - usageContractFromLpDate);
                        bbt.setActiveEnergyImport(firstLps[0].getValue());
                        bbt.setActiveEnergyExport(firstLps[1].getValue());
                    }
                    else {
                        bbt.setActiveEnergy(firstLps[0].getValue() + firstLps[1].getValue());
                        bbt.setActiveEnergyImport(firstLps[0].getValue());
                        bbt.setActiveEnergyExport(firstLps[1].getValue());
                    }
                }
                
                return bbt;
            }
            
            // 계약일이 없으면 미터 실치 기준일과 0부터 시작한다.
            if (meter.getInstallDate() == null || "".equals(meter.getInstallDate()) 
                    || meter.getInstallDate().length() != 14)
                throw new Exception("check install date of METER[" + meterId + "]");
            
            bbt.setYyyymmdd(meter.getInstallDate().substring(0, 8));
            bbt.setHhmmss(meter.getInstallDate().substring(8));
            bbt.setActiveEnergy(0.0);
            bbt.setActiveEnergyImport(0.0);
            bbt.setActiveEnergyExport(0.0);
            
            return bbt;
        }
    }
    
    private int getLastDay(String yyyymm) {
        int yyyy = Integer.parseInt(yyyymm.substring(0, 4));
        int mm = Integer.parseInt(yyyymm.substring(4, 6));

        int day = 1;
        switch (mm) {
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            day = 31;
            break;
        case 2:
            if (yyyy % 4 == 0) day = 29;
            else day = 28;
            break;
        default : day = 30;
        }
        
        return day;
    }
    
    public void saveBill(Contract contract, LpEM[] lastLp, BillingBlockTariff prevBill, String tariffDate) 
    throws Exception
    {
    	// 시스템상의 Tariff 날짜(고정..)
    	String appliedDate = tariffDate;
    	
        // 날짜 비교
        String lptime = lastLp[0].getYyyymmddhh();
        String prevBillTime = prevBill.getYyyymmdd() + prevBill.getHhmmss().substring(0, 2);
        
        // 마지막 LP시간이 이미 계산한 것이라면 종료한다.
        if (lptime.equals(prevBillTime))
            return;
        else {
            String lp_yyyymm = lptime.substring(0, 6);
            String prevBillTime_yyyymm = prevBillTime.substring(0, 6);
            
            // 두 개의 yyyymm을 비교하여 같으면 lp의 누적치에서 prevBill의 누적치의 차를 이용하여 계산한다.
            if (lp_yyyymm.equals(prevBillTime_yyyymm)) {
                savePrebill(contract.getTariffIndex().getName(),
                        contract.getMeter(), lptime, prevBill.getAccumulateBill(),
                        prevBill.getAccumulateUsage(), prevBill.getActiveEnergy(),
                        lastLp[0].getValue(), lastLp[1].getValue(), getTariff(contract.getTariffIndexId(),
                        		appliedDate), DateTimeUtil.getDateString(new Date()));
            }
            else {
                                                
            }
        }
    }
    
    /*
     * 시작 년월부터 마지막 LP의 년월까지의 일수를 계산하여 LP 전월까지의 월 평균 사용량을 계산하고
     * LP 시작부터 계약일까지의 사용량을 구한다. 이때 계약일이 LP 시작일과 마지막 일자 사이에 있어야 한다.
     */
    private double calUsageFromLpDateContractDate(double usage, String fromLpDate, String toLpDate, String contractDate)
            throws ParseException {
        // 마지막 LP의 일자를 가져온다.
        int days = 0;
        int sumDays = 0;
        double avg = 0.0;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(fromLpDate.substring(0, 6)));
        Map<String, Double> yyyymmDays = new HashMap<String, Double>();
        
        int year = 0;
        int month = 1;
        String yearmonth;
        while (true) {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
            yearmonth = String.format("%04d%02d", year, month);
            days = getLastDay(yearmonth);
            
            // 시작년월이 같으면 일자만큼 빼고 나머지 일수를 사용할 수 있도록 한다.
            if (yearmonth.equals(fromLpDate.substring(0, 6)) && 
                    yearmonth.equals(toLpDate.substring(0, 6))) {
                days = Integer.parseInt(toLpDate.substring(6,8)) - Integer.parseInt(fromLpDate.substring(6, 8));
            }
            else if (yearmonth.equals(fromLpDate.substring(0, 6))) {
                days = days - Integer.parseInt(fromLpDate.substring(6, 8));
            }
            else if (yearmonth.equals(toLpDate.substring(0, 6))) {
                days = Integer.parseInt(toLpDate.substring(6, 8));
            }
            
            sumDays += days;
            yyyymmDays.put(yearmonth, (double)days);
            log.debug("YYYYMM[" + yearmonth + "] DAYS[" + days + "] SUM_DAYS[" + sumDays + "]");
            
            // LP와 같은 년월이면 종료한다.
            if (yearmonth.equals(toLpDate.substring(0,6))) {
                break;
            }
            
            cal.add(Calendar.MONTH, 1);
        }
        
        log.info("FromLPDate[" + fromLpDate + "] ToLPDate[" + toLpDate + "] TotalLPDays[" + sumDays + "]");
        if (sumDays != 0) avg = usage / (double)sumDays;
        log.info("TotalUsage[" + usage + "] AvgUsage[" + avg + "]");
        
        String yyyymm = null;
        double usageFromLpDateContractDate = 0.0;
        for (Iterator<String> i = yyyymmDays.keySet().iterator(); i.hasNext(); ) {
            yyyymm = i.next();
            if (yyyymm.equals(contractDate.substring(0, 6))) {
                if (yyyymm.equals(fromLpDate.substring(0, 6))) {
                    usageFromLpDateContractDate += avg * 
                            (Integer.parseInt(contractDate.substring(6, 8)) - Integer.parseInt(fromLpDate.substring(6, 8)) + 1);
                }
                else {
                    usageFromLpDateContractDate += avg * Integer.parseInt(contractDate.substring(6,8));
                }
                break;
            }
            else if (yyyymm.compareTo(contractDate.substring(0, 6)) < 0)
                usageFromLpDateContractDate += avg * yyyymmDays.get(yyyymm);
        }
        
        return usageFromLpDateContractDate;
    }
    
    /*
     * 시작 년월부터 마지막 LP의 년월까지의 일수를 계산하여 LP 전월까지의 월 평균 사용량을 계산하고
     * 계약일부터 LP 시작일까지의 사용량을 계산한다.
     */
    private double calContractDateFromLpDate(double usage, String fromLpDate, String toLpDate, String contractDate)
            throws ParseException {
        // 마지막 LP의 일자를 가져온다.
        int days = 0;
        int year = 0;
        int month = 0;
        String yearmonth = null;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar cal = Calendar.getInstance();
        
        double avg = avgDay(usage, fromLpDate, toLpDate);
        double usageContractDateFromLpDate = 0.0;
        cal = Calendar.getInstance();
        cal.setTime(sdf.parse(contractDate.substring(0, 6)));
        while (true) {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
            yearmonth = String.format("%04d%02d", year, month);
            days = getLastDay(yearmonth);
            
            // 계약일과 lp 시작일이 같은 년월이면 기간만큼의 사용량을 구한다.
            if (yearmonth.equals(contractDate.substring(0, 6)) 
                    && yearmonth.equals(fromLpDate.substring(0, 6))) {
                usageContractDateFromLpDate += ((days - Integer.parseInt(contractDate.substring(6,8)) + 1) * avg);
                usageContractDateFromLpDate -= ((days - Integer.parseInt(fromLpDate.substring(6,8))) * avg);
                break;
            }
            // 계약일과만 같으면 말일에서 계약일자만큼의 일수에 대한 사용량을 더한다.
            else if (yearmonth.equals(contractDate.substring(0, 6))) {
                usageContractDateFromLpDate += ((days - Integer.parseInt(contractDate.substring(6,8)) + 1) * avg);
            }
            // LP 시작일과 같으면 1일부터 시작일까지의 일수만큼의 사용량을 더한다.
            else if (yearmonth.equals(fromLpDate.substring(0, 6))) {
                usageContractDateFromLpDate += (Integer.parseInt(fromLpDate.substring(6,8)) * avg);
                break;
            }
            else {
                usageContractDateFromLpDate += (days * avg);
            }
            
            cal.add(Calendar.MONTH, 1);
        }
        return usageContractDateFromLpDate;
    }
    
    private double avgDay(double usage, String fromLpDate, String toLpDate) throws ParseException {
     // 마지막 LP의 일자를 가져온다.
        int days = 0;
        int sumDays = 0;
        double avg = 0.0;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(fromLpDate.substring(0, 6)));
        Map<String, Double> yyyymmDays = new HashMap<String, Double>();
        
        int year = 0;
        int month = 1;
        String yearmonth;
        while (true) {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
            yearmonth = String.format("%04d%02d", year, month);
            days = getLastDay(yearmonth);
            
            // 시작년월이 같으면 일자만큼 빼고 나머지 일수를 사용할 수 있도록 한다.
            if (yearmonth.equals(fromLpDate.substring(0, 6)) && 
                    yearmonth.equals(toLpDate.substring(0, 6))) {
                days = Integer.parseInt(toLpDate.substring(6,8)) - Integer.parseInt(fromLpDate.substring(6, 8)) + 1;
            }
            else if (yearmonth.equals(fromLpDate.substring(0, 6))) {
                days = days - Integer.parseInt(fromLpDate.substring(6, 8)) + 1;
            }
            else if (yearmonth.equals(toLpDate.substring(0, 6))) {
                days = Integer.parseInt(toLpDate.substring(6, 8));
            }
            
            sumDays += days;
            yyyymmDays.put(yearmonth, (double)days);
            log.debug("YYYYMM[" + yearmonth + "] DAYS[" + days + "] SUM_DAYS[" + sumDays + "]");
            
            // LP와 같은 년월이면 종료한다.
            if (yearmonth.equals(toLpDate.substring(0,6))) {
                break;
            }
            
            cal.add(Calendar.MONTH, 1);
        }
        
        log.info("FromLPDate[" + fromLpDate + "] ToLPDate[" + toLpDate + "] TotalLPDays[" + sumDays + "]");
        if (sumDays != 0) avg = usage / (double)sumDays;
        log.info("TotalUsage[" + usage + "] AvgUsage[" + avg + "]");
        
        return avg;
    }
    
    private List<TariffEM> getTariff(Integer tariffIndexId, String yyyymmdd) {
        TariffType tariffType = tariffTypeDao.get(tariffIndexId);
        Integer tariffTypeCode = tariffType.getCode();
        
        Map<String, Object> tariffParam = new HashMap<String, Object>();

        tariffParam.put("tariffTypeCode", tariffTypeCode);
        tariffParam.put("tariffIndex", tariffType);
        tariffParam.put("searchDate", yyyymmdd);
        
        return tariffEMDao.getApplyedTariff(tariffParam); 
    }
    
    // yyyymm의 월 사용량을 가져와 선불을 계산하고 마지막 선불요금을 뺀다.
    private void savePrebill(String tariffName, Meter meter, String lastLpTime,
            double blockBillAccumulateBill, double blockBillAccumulateUsage, double blockBillActiveEnergy,
            double lastLpActiveEnergyImport, double lastLpActiveEnergyExport,
            List<TariffEM> tariffEMList, String lastTokenDate)
    throws Exception
    {
        log.info("####savePrebill####");
        log.info("MeterId[" + meter.getMdsId() + "] LAST_LPTIME[" + lastLpTime + "]");
        
        Double activeD = lastLpActiveEnergyImport + lastLpActiveEnergyExport;
        if (activeD - blockBillActiveEnergy < 0) {
            log.warn("Warning(Billing Canceled): ["+ meter.getMdsId() +"] Previous LpActiveEnergy >= LastLpActiveEnergy");
            return;
        }else if( (blockBillActiveEnergy > 100) &&  (activeD >= (blockBillActiveEnergy*10)) ){
            // 직전 ActiveEnergy에 비해 급격한 차이가 발생하면... (단 직전값이 최소 100이상이어야 함)
            log.warn("Critical(Billing Canceled): ["+ meter.getMdsId() +"] LastLpActiveEnergy suspicious...  ");
            return;
        }
        
        // 월 사용량
        double usageD = lastLpActiveEnergyImport + lastLpActiveEnergyExport - blockBillActiveEnergy + blockBillAccumulateUsage;
        
        // 사용량이 0보다 작으면 종료한다.
        if (usageD < 0) {
            log.warn("Negative: MONTH_USAGE[" + usageD + "] < 0");
            return;
        }
        
        int usage = (int)usageD;
        // 월 선불금액 계산
        double monthBill = blockBill(tariffName, tariffEMList, usage);
        
        // LP 날짜로 Billing Day를 생성하거나 업데이트한다.
        saveBillingBlockTariff(meter, usageD, lastLpActiveEnergyImport, lastLpActiveEnergyExport,
                lastLpTime, monthBill, blockBillAccumulateBill,lastLpActiveEnergyImport + 
                lastLpActiveEnergyExport - blockBillActiveEnergy,monthBill-blockBillAccumulateBill);
        
        // 잔액을 차감한다.
//        log.info("contract_number[" + contract.getContractNumber() + 
//                "] MonthBill[" + monthBill + "] blockBillccumulateBill[" + blockBillAccumulateBill+ "]");
//        contract.setCurrentCredit(contract.getCurrentCredit() - (monthBill - blockBillAccumulateBill));
//        contractDao.updateCurrentCredit(contract.getId(), contract.getCurrentCredit());
        
        // 선불로그 기록
//        savePrepyamentLog(contract, lastLpActiveEnergyImport + lastLpActiveEnergyExport - blockBillActiveEnergy,
//            monthBill-blockBillAccumulateBill, lastLpTime, lastTokenDate, lastLpActiveEnergyImport, lastLpActiveEnergyExport);
    }
    
    private void saveBillingBlockTariff(Meter meter, double lastAccumulateUsage, 
            double activeEnergyImport, double activeEnergyExport,
            String lastLpTime, double newbill, double oldbill, double usage, double balance) {
        BillingBlockTariff bill = new BillingBlockTariff();
        
        bill.setMDevId(meter.getMdsId());
        bill.setYyyymmdd(lastLpTime.substring(0, 8));
        bill.setHhmmss(lastLpTime.substring(8, 10)+"0000");
        bill.setMDevType(DeviceType.Meter.name());
        bill.setSupplier(meter.getSupplier());
        bill.setLocation(meter.getLocation());
        bill.setMeter(meter);
        bill.setModem((meter == null) ? null : meter.getModem());
        bill.setWriteDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
        bill.setAccumulateUsage(lastAccumulateUsage);
        bill.setActiveEnergy(activeEnergyImport + activeEnergyExport);
        bill.setActiveEnergyImport(activeEnergyImport);
        bill.setActiveEnergyExport(activeEnergyExport);
        bill.setUsage(usage);
        
        if (meter.getContract() != null)
            bill.setContract(meter.getContract());
        
        //현재누적액 - 어제누적액
        bill.setBill(newbill - oldbill);
        bill.setAccumulateBill(newbill);
    
        log.info("MeterId[" + bill.getMDevId() + "] BillDay[" + bill.getYyyymmdd() + 
                "] BillTime[" + bill.getHhmmss() + "] AccumulateUsage[" + bill.getAccumulateUsage() +
                "] AccumulateBill[" + bill.getAccumulateBill() + "] CurrentBill[" + bill.getBill() + 
                "] ActiveEnergyImport[" + activeEnergyImport + "] ActiveEnergyExport[" + activeEnergyExport + "]");
        
        billingBlockTariffDao.saveOrUpdate(bill);
    }
    
    private void savePrepyamentLog(Contract contract, double usage, double bill,
            String lastLpTime, String lastTokenDate, double activeEnergyImport, double activeEnergyExport) {
        PrepaymentLog prepaymentLog = new PrepaymentLog();
        prepaymentLog.setId(Long.parseLong(Integer.toString(contract.getId())
                + Long.toString(System.currentTimeMillis())));
        prepaymentLog.setUsedConsumption(usage);
        prepaymentLog.setBalance(contract.getCurrentCredit());
        prepaymentLog.setChargedCredit(Double.parseDouble("0"));
        prepaymentLog.setLastTokenDate(lastTokenDate);
        prepaymentLog.setContract(contract);
        prepaymentLog.setCustomer(contract.getCustomer());
        prepaymentLog.setUsedCost(bill);
        prepaymentLog.setLastLpTime(lastLpTime.substring(0, 6));
        prepaymentLog.setLocation(contract.getLocation());
        prepaymentLog.setTariffIndex(contract.getTariffIndex());
        prepaymentLog.setActiveEnergyImport(activeEnergyImport);
        prepaymentLog.setActiveEnergyExport(activeEnergyExport);
        
        log.info(prepaymentLog.toString());
        
        prepaymentLogDao.add(prepaymentLog);
    }
    
    public double blockBill(String tariffName, List<TariffEM> tariffEMList, int usage) {
        double returnBill = 0.0;
        double vatBill = 0.0;
        Collections.sort(tariffEMList, new Comparator<TariffEM>() {

            @Override
            public int compare(TariffEM t1, TariffEM t2) {
                return t1.getSupplySizeMin() < t2.getSupplySizeMin()? -1:1;
            }
        });
 
        double supplyMin = 0.0;
        double supplyMax = 0.0;
        double block = 0.0;
        double blockUsage = 0.0;
        Double energyCharge = 0.0;
        Double activeEnergy = 0.0;
        Double publicLevy = 0.0;
        Double govLevy = 0.0;
        Double blockBill = 0.0;
        Double blockVat = 0.0;
        Double vat = 0.0;
        
        for(int cnt=0 ; cnt < tariffEMList.size(); cnt++){
            supplyMin = tariffEMList.get(cnt).getSupplySizeMin() == null ? 0.0 : tariffEMList.get(cnt).getSupplySizeMin();
            supplyMax = tariffEMList.get(cnt).getSupplySizeMax() == null ? 0.0 : tariffEMList.get(cnt).getSupplySizeMax();
            
            log.info("[" + cnt + "] supplyMin : " + supplyMin + ", supplyMax : " + supplyMax);
            
            //Tariff 첫 구간
            if (usage >= supplyMin) {
                if(supplyMax != 0) {
                    block = supplyMax - supplyMin;
                    
                    blockUsage = usage - supplyMax;
                    
                    if (blockUsage < 0) blockUsage = usage - supplyMin;
                    else blockUsage = block;
                } else {
                    blockUsage = usage - supplyMin;
                }
                
                activeEnergy = tariffEMList.get(cnt).getActiveEnergyCharge() == null ? 0d : tariffEMList.get(cnt).getActiveEnergyCharge();
                publicLevy = tariffEMList.get(cnt).getDistributionNetworkCharge() == null ? 0d : tariffEMList.get(cnt).getDistributionNetworkCharge();
                govLevy = tariffEMList.get(cnt).getTransmissionNetworkCharge() == null ? 0d : tariffEMList.get(cnt).getTransmissionNetworkCharge();
                vat = tariffEMList.get(cnt).getEnergyDemandCharge() == null ? 0d : tariffEMList.get(cnt).getEnergyDemandCharge();
                
                /*
                if (tariffEMList.get(cnt).getYyyymmdd().compareTo("20160101") >= 0) {
                    activeEnergy *= blockUsage;
                    publicLevy *= activeEnergy;
                    govLevy *= activeEnergy;
                    // activeEnergy = activeEnergy + activeEnergy * publicLevy + activeEnergy * govLevy;
                    log.info("ActiveEnergyCharge with levy: " + activeEnergy);
                }
                */
                energyCharge = activeEnergy * blockUsage;
                publicLevy *= energyCharge;
                govLevy *= energyCharge;
                log.info("EnergyCharge[" + energyCharge + "] PublicLevy[" + publicLevy + "] GovLevy[" + govLevy + "]");
                
                // blockBill = blockUsage * activeEnergy;
                blockBill = energyCharge + publicLevy + govLevy;
                returnBill = returnBill + blockBill;
                log.info("Block Usage[" + blockUsage + "]");
                log.info("ActiveEnergyCharge: " + tariffEMList.get(cnt).getActiveEnergyCharge());
                log.info("block bill[" + blockBill + "]");
                
                if (tariffEMList.get(cnt).getYyyymmdd().compareTo("20160101") >= 0) {
                    // blockVat = blockBill * vat;
                    blockVat = energyCharge * vat;
                    vatBill = vatBill + blockVat; 
                    log.info("block vat[" + blockVat + "]");
                }
                
            }
        }

        log.info("returnBill[" + returnBill + "], vatBill[" + vatBill + "], monthBill[" + (returnBill + vatBill) +"]");
        return (returnBill + vatBill);
    }
    
    
    
    //Prepaymentlog 저장 (환급 기록)
    /*private void saveRefundPrepaymentLog(Contract contract, double usage, double bill,
							            String lastLpTime, String lastTokenDate, 
							            double activeEnergyImport, double activeEnergyExport,
							            double old_diff, double new_diff, double refund,
							            String token) {
        PrepaymentLog prepaymentLog = new PrepaymentLog();
        
        //Prepaymentlog의 ID 생성방법 변경(id+시스템시간)
        prepaymentLog.setId(Long.parseLong(Integer.toString(contract.getId())
        					+ Long.toString(System.currentTimeMillis())));
        prepaymentLog.setUsedConsumption(usage);
        prepaymentLog.setLastTokenDate(lastTokenDate);
        prepaymentLog.setContract(contract);
        prepaymentLog.setCustomer(contract.getCustomer());
        prepaymentLog.setUsedCost(bill);
        prepaymentLog.setLastLpTime(lastLpTime.substring(0, 6));
        prepaymentLog.setLocation(contract.getLocation());
        prepaymentLog.setTariffIndex(contract.getTariffIndex());
        prepaymentLog.setActiveEnergyImport(activeEnergyImport);
        prepaymentLog.setActiveEnergyExport(activeEnergyExport);
        prepaymentLog.setToken(token);
        
        Code payType = codeDao.getCodeByName(PAYTYPE_REFUND);
        prepaymentLog.setPayType(payType);
        
        Operator operator = operatorDao.getOperatorByLoginId("ECG Service");
        prepaymentLog.setOperator(operator);
        
        //prepaymentLog.setVendorCasher(vendorCasher);
        
        Double preCredit = StringUtil.nullToDoubleZero(contract.getCurrentCredit());
        prepaymentLog.setPreBalance(preCredit);
        prepaymentLog.setBalance(preCredit + refund);
        
        prepaymentLog.setChargedCredit(refund);
        prepaymentLog.setDescr("Old Tariff Bill[" + old_diff + "], New Tariff Bill[" + new_diff + "]");
        
        log.info(prepaymentLog.toString());
        
        prepaymentLogDao.add(prepaymentLog);
    }*/
    
    
    
}







