package com.aimir.schedule.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.mvm.BillingBlockTariffDao;
import com.aimir.dao.mvm.DayEMDao;
import com.aimir.dao.mvm.LpEMDao;
import com.aimir.dao.mvm.impl.LpEMDaoImpl;
import com.aimir.dao.system.CodeDao;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.OperatorDao;
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
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.TariffEM;
import com.aimir.model.system.TariffType;
import com.aimir.util.Condition;
import com.aimir.util.Condition.Restriction;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.StringUtil;


/**
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
public class ECGBlockDailyEMBillingV4_TariffChange extends ScheduleTask {

	protected static Log log = LogFactory.getLog(ECGBlockDailyEMBillingV4_TariffChange.class);
	private static final String SERVICE_TYPE_EM = "Electricity";
	
	@Resource(name="transactionManager")
    HibernateTransactionManager txmanager;
    
    @Autowired
    ContractDao contractDao;
    
    private boolean isNowRunning = false;
    
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"spring-billingtask.xml"}); 
        DataUtil.setApplicationContext(ctx);
        
        ECGBlockDailyEMBillingV4_TariffChange task = ctx.getBean(ECGBlockDailyEMBillingV4_TariffChange.class);
        task.execute(null);
        System.exit(0);
    }
	
	@Override
	public void execute(JobExecutionContext context) {
		 if(isNowRunning){
	            log.info("### ECGBlockDailyEMBillingV4_TariffChange: Task is already running...");
	            return;
		 }

		 isNowRunning = true;
		 log.info("### START - ECGBlockDailyEMBillingV4_TariffChange ###");
		 
		 // 일별 전기 요금 계산
		 this.calcEmBillingDayInfo();
	        
		 log.info("### END - ECGBlockDailyEMBillingV4_TariffChange ###");
		 isNowRunning = false;      
	} // ~ execute

	
	private void calcEmBillingDayInfo() {
		// 전기 계약 정보 취득
        List<Integer> em_contractIds = this.getContractInfos(SERVICE_TYPE_EM);
        
        int poolSize = Integer.parseInt(FMPProperty.getProperty("block.billing.thread.pool.size", ""+5));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize, poolSize, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        
        for(Integer contract_id : em_contractIds) {        
        	// log.info("ContractId[" + contract_id + "]");
            try {
            	//계약별 계산
                executor.execute(new BlockBillingThread_TariffChange(contract_id));
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
		
	}

	private List<Integer> getContractInfos(String serviceType) {
		TransactionStatus txstatus = null;
        List<Integer> contractIds = new ArrayList<Integer>();
        
        try {
            txstatus = txmanager.getTransaction(null);
            Set<Condition> condition = new HashSet<Condition>();
            
            condition.add(new Condition("serviceTypeCode", new Object[]{"s"}, null, Restriction.ALIAS));
            condition.add(new Condition("s.name", new Object[]{serviceType}, null, Restriction.EQ));
            condition.add(new Condition("creditType", new Object[]{"c"}, null, Restriction.ALIAS));
            condition.add(new Condition("c.code", new Object[]{Code.PREPAYMENT, Code.EMERGENCY_CREDIT}, null, Restriction.IN));
            // STS 및 NetMetering 제외 
            condition.add(new Condition("isSts", new Object[]{false}, null, Restriction.EQ));
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




class BlockBillingThread_TariffChange implements Runnable {

	private static Log log = LogFactory.getLog(BlockBillingThread_TariffChange.class);
    
    HibernateTransactionManager txmanager = (HibernateTransactionManager)DataUtil.getBean("transactionManager");
    ContractDao contractDao = DataUtil.getBean(ContractDao.class);
    LpEMDao lpEMDao = DataUtil.getBean(LpEMDao.class);
    DayEMDao dayEMDao = DataUtil.getBean(DayEMDao.class);
    BillingBlockTariffDao billingBlockTariffDao = DataUtil.getBean(BillingBlockTariffDao.class);
    MeterDao meterDao = DataUtil.getBean(MeterDao.class);
    TariffTypeDao tariffTypeDao = DataUtil.getBean(TariffTypeDao.class);
    TariffEMDao tariffEMDao = DataUtil.getBean(TariffEMDao.class);
    PrepaymentLogDao prepaymentLogDao = DataUtil.getBean(PrepaymentLogDao.class);
    CodeDao codeDao = DataUtil.getBean(CodeDao.class);
    OperatorDao operatorDao = DataUtil.getBean(OperatorDao.class); 
    
    private int contract_id;
    private static final String PAYTYPE_REFUND = "Refund";
    
	public BlockBillingThread_TariffChange(int contractId) {
        this.contract_id = contractId;
    }
	
	/**
     * @author jiae
     * @desc   가나 ECG 에서 사용하는 채널
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
                    log.info("Contract[" + contract.getContractNumber() + "] Meter[" + contract.getMeter().getMdsId() + "] Calc start");
                    
                    //TODO Parameter 정의(변경일자, 변경월의 마지막날, 비교할 BillingBlockTariff가 저장된 테이블)
                    // 1~대상일자까지의 계산
                    String maxYyyymmddA = "20180314";
                    LpEM[] lastLpA = getLastLp(contract.getMeter().getMdsId(), maxYyyymmddA+"00", maxYyyymmddA+"23");
                    
                    // 1~말일까지의 계산(누적계산 방식이라서, "전체요금-부분요금"처럼 구해야함)
                    String maxYyyymmddB = "20180331"; //20180324
                    LpEM[] lastLpB = getLastLp(contract.getMeter().getMdsId(), maxYyyymmddB+"00", maxYyyymmddB+"23");
                    
                    // 각 구간별 요금
                    if (lastLpA != null && lastLpA.length == 2 && contract.getMeter().getModem() != null && lastLpB != null) {
                    	// billing_block_tariff_future에 별도로 복사해놓은 데이터를 가져옴.
                        BillingBlockTariff prevBill = getSpecificBillingBlockTariff(contract.getMeter().getMdsId(), lastLpA);
                        if(prevBill == null){
                        	//계산할 데이터 없음. 종료.
                        	log.info("Warning(Canceled): Contract["+ contract.getContractNumber() +"] No Billing Data.");
                        	throw new Exception();
                        }
                        double billA = saveBill(contract, lastLpA, prevBill);
                        double billB = saveBill(contract, lastLpB, prevBill);
                        
                        if(billA < 0 || billB < 0) {
                        	//두 값중 하나라도 정상이 아니면 종료.
                        	log.info("Warning(Canceled): Contract["+ contract.getContractNumber() +"] Cost is not valid.");
                        	throw new Exception();
                        }
                        
                        //new tariff로 계산했을때의 누적요금(31th-14th)
                        double new_diff = billB - billA;
                        
                        //기존 Tariff로 계산한 BLT를 가져온다..(prevBill과 조회하는 테이블 다르다는 것을 주의)
                        //old 14th last,31th last
                        BillingBlockTariff prevBill_oldA = getLastBillingBlockTariff(contract.getMeter().getMdsId(), lastLpB, maxYyyymmddA);
                        BillingBlockTariff prevBill_oldB = getLastBillingBlockTariff(contract.getMeter().getMdsId(), lastLpB, maxYyyymmddB);
                        if(prevBill_oldA == null || prevBill_oldB == null) {
                        	//하나라도 데이터가 없으면 종료
                        	log.info("Warning(Canceled): Contract["+ contract.getContractNumber() +"] PrevBill_old Data is Null.");
                        	throw new Exception();
                        }
                        
                        //31th에서 14th를 빼고..
                        double old_diff = prevBill_oldB.getAccumulateBill() - prevBill_oldA.getAccumulateBill(); 

                        //service charge 여부 계산(ACCU 50kw이하인 경우 0.0033(2.1333-2.1300)cedi를 추가환급
                        double serviceCharge=0.0d;
                        double monthAccu = (lastLpB[0].getValue()+lastLpB[1].getValue())-prevBill.getAccumulateUsage();
                        if(monthAccu >= 1 && monthAccu <=50) {
                        	serviceCharge=0.0033d;
                        }
                        
                        /**
                         *  old - new = 환급액 확정
                         *  refund가 0이상일때는 contract를 갱신하고, 아니면 prepaymentLog만 생성함..
                         */
                        double refund = old_diff - new_diff + serviceCharge;

                        /**
                         *  Prepaymentlog 저장 ('Refund' 타입, 'ECG Service' 명칭으로 저장됨)
                         *  이미 해당월에 refund 기록이 있으면 저장하지 않도록함.
                         */
                        log.info("Receipt Info: Contract[" + contract.getContractNumber() + "] Refund[" + refund +
            	                "] new tariff bill[" + new_diff + "] old tariff bill[" + old_diff +
            	                "] serviceCharge diff[" + serviceCharge + "]");
                        
                        //Refund 기록 조회
                        Set<Condition> condition = new HashSet<Condition>();
                        condition.add(new Condition("contractId", new Object[]{contract.getId()}, null, Restriction.EQ));
                        condition.add(new Condition("lastLpTime", new Object[]{maxYyyymmddA.substring(0,6)}, null, Restriction.EQ));
                        condition.add(new Condition("payType", new Object[]{"y"}, null, Restriction.ALIAS));
                        condition.add(new Condition("y.name", new Object[]{PAYTYPE_REFUND}, null, Restriction.EQ));
                        
                        List<Object> refundLogCount = prepaymentLogDao.getPrepaymentLogCountByListCondition(condition);
                        int logCount = Integer.parseInt(String.valueOf(refundLogCount.get(0)));
                        
                        if(logCount>0) {
                        	//동일한 LPTIME에 해당하는 Refund기록이 있으면 종료함.
                        	log.info("Exception: Contract[" + contract.getContractNumber() + "] has refund history.");
                        	throw new Exception();
                        }
                        
                        //PrepaymentLog 저장
                        saveRefundPrepaymentLog(contract, 0.0, 0.0, maxYyyymmddB.substring(0,6), 
                        		DateTimeUtil.getDateString(new Date()), lastLpB[0].getValue(), lastLpB[1].getValue(),
                        		old_diff, new_diff, refund);
                        
                        
                        /**
                         *  Contract 갱신 (잔액 추가)
                         */
                        if(refund > 0 && logCount < 1) {
                        	Double newBal = contract.getCurrentCredit() + refund;
                        	log.info("Update(Balance): Contract[" + contract.getContractNumber() + "] Refund[" + refund +
                	                "] before Balance[" + contract.getCurrentCredit() + "] After Refund[" + newBal + "]");
            	        	contract.setCurrentCredit(contract.getCurrentCredit() + refund );
            	        	contractDao.updateCurrentCredit(contract.getId(), contract.getCurrentCredit());
                        }
                        
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
        
        
        //End-.
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
                log.info("MeterID[" + meterId + "] " + maxyyyymmddhh.get(0));
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
     * 특정 데이터만 저장된 테이블에서 BillingBlockTariff를 가져온다.
     * 없으면 종료. (기존 로직은 계약일을 고려하여 새로 BillingBlockTariff를 만들지만..) 
     * 
     * @param meterId
     * @return
     */
    public BillingBlockTariff getSpecificBillingBlockTariff(String meterId, LpEM[] lastLp) throws Exception {
        // MdevId에 해당하는 가장 최근 빌링 정보를 가져온다.
        List<Map<String, Object>> list = billingBlockTariffDao.getSpecificAccumulateBill(meterId);

        Map<String, Object> map = null;
        String contractNumber = null;
        if (list != null && list.size() == 1) {
            map = list.get(0);
            contractNumber = String.valueOf(map.get("CONTRACTID"));  //number대신ID
        }
        // 기 빌링정보와 LP의 계약 정보가 같은 경우만 누적치를 적용하고 나머지는 전부 새로 생성한다. 
        if (map != null && contractNumber != null && 
                lastLp[0].getContract() != null &&
                lastLp[0].getContract().getId().toString().equals(contractNumber)) {
            BillingBlockTariff bbt = new BillingBlockTariff();
            bbt.setMDevType("Meter");
            bbt.setMDevId(meterId);
            bbt.setYyyymmdd((String)map.get("YYYYMMDD"));
            bbt.setHhmmss((String)map.get("HHMMSS"));

            String accBill = String.valueOf(map.get("ACCUMULATEBILL"));
            bbt.setAccumulateBill(Double.parseDouble(accBill));
            
            String accUsage = String.valueOf(map.get("ACCUMULATEUSAGE"));
            bbt.setAccumulateUsage(Double.parseDouble(accUsage));
            
            // 고객 계약이 같지 않지만 billingblocktariff의 마지막 누적값을 이용해야 한다.
            //Object activeEnergy = map.get("ACTIVEENERGY");
            String actEnergy = String.valueOf(map.get("ACTIVEENERGY"));
            //Object activeEnergyImport = map.get("ACTIVEENERGYIMPORT");
            String actEnergyImport = String.valueOf(map.get("ACTIVEENERGYIMPORT"));
            //Object activeEnergyExport = map.get("ACTIVEENERGYEXPORT");
            String actEnergyExport = String.valueOf(map.get("ACTIVEENERGYEXPORT"));
            bbt.setActiveEnergy(actEnergy == null? 0.0:Double.parseDouble(actEnergy));
            bbt.setActiveEnergyImport(actEnergyImport == null? 0.0:Double.parseDouble(actEnergyImport));
            bbt.setActiveEnergyExport(actEnergyExport == null? 0.0:Double.parseDouble(actEnergyExport));
            
            return bbt;
        }
        // 고객의 billingblocktariff가 없으면 미터에 대해서 선불계산한 적이 없으며, 
        // 기존 내역이 없는 계약은 차액을 계산하는 대상이 아님.
        return null;        
        
    }
    
    /**
     * 기존 테이블에서 마지막 BillingBlockTariff를 가져온다.
     * 없으면 종료. (기존 로직은 계약일을 고려하여 새로 BillingBlockTariff를 만들지만..) 
     * 
     * @param meterId
     * @return
     */
    public BillingBlockTariff getLastBillingBlockTariff(String meterId, LpEM[] lastLp, String yyyymmdd) throws Exception {
        // MdevId에 해당하는 가장 최근 빌링 정보를 가져온다.
        List<Map<String, Object>> list = billingBlockTariffDao.getLastDateAccumulateBill(meterId,yyyymmdd);

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
        // 고객의 billingblocktariff가 없으면 미터에 대해서 선불계산한 적이 없으며, 
        // 기존 내역이 없는 계약은 차액을 계산하는 대상이 아님.
        return null;        
        
    }
    
    // BLT와 LP의 날짜비교하여 계산할지 여부를 구분.
    public double saveBill(Contract contract, LpEM[] lastLp, BillingBlockTariff prevBill) throws Exception
    {
    	// 시스템상의 신규 Tariff 날짜(고정..)
    	String appliedDate = "20180401";
    	
    	
    	// 날짜 비교
        String lptime = lastLp[0].getYyyymmddhh();
        String prevBillTime = prevBill.getYyyymmdd() + prevBill.getHhmmss().substring(0, 2);
        
        // 마지막 LP시간이 이미 계산한 것이라면 종료한다.
        if (lptime.equals(prevBillTime))
            return -1;
        else {
            String lp_yyyymm = lptime.substring(0, 6);
            String prevBillTime_yyyymm = prevBillTime.substring(0, 6);
            
            // 두 개의 yyyymm을 비교하여 같으면 lp의 누적치에서 prevBill의 누적치의 차를 이용하여 계산한다.
            if (lp_yyyymm.equals(prevBillTime_yyyymm)) {
                double cost = savePrebill(contract.getTariffIndex().getName(),
                        contract.getMeter(), lptime, prevBill.getAccumulateBill(),
                        prevBill.getAccumulateUsage(), prevBill.getActiveEnergy(),
                        lastLp[0].getValue(), lastLp[1].getValue(), getTariff(contract.getTariffIndexId(),
                        		appliedDate), DateTimeUtil.getDateString(new Date()));
                
                if (cost > 0) {
                	return cost;
                }
            }
            
        }
        
        //아무것도 안걸리면.
        return -1;
    }
    
    
    // yyyymm의 월 사용량을 가져와 선불을 계산하고, 실제 차감되어야할 요금을 반환한다.
    private double savePrebill(String tariffName, Meter meter, String lastLpTime,
            double blockBillAccumulateBill, double blockBillAccumulateUsage, double blockBillActiveEnergy,
            double lastLpActiveEnergyImport, double lastLpActiveEnergyExport,
            List<TariffEM> tariffEMList, String lastTokenDate)
    throws Exception
    {
        log.info("####savePrebill####");
        log.info("MeterId[" + meter.getMdsId() + "] LAST_LPTIME[" + lastLpTime + "]");
        
        Double activeD = lastLpActiveEnergyImport + lastLpActiveEnergyExport;
        if (activeD - blockBillActiveEnergy < 0) {
            log.info("Warning(Billing Canceled): ["+ meter.getMdsId() +"] Previous LpActiveEnergy >= LastLpActiveEnergy");
            return -1;
        }else if( (blockBillActiveEnergy > 100) &&  (activeD >= (blockBillActiveEnergy*10)) ){
            // 직전 ActiveEnergy에 비해 급격한 차이가 발생하면... (단 직전값이 최소 100이상이어야 함)
            log.info("Critical(Billing Canceled): ["+ meter.getMdsId() +"] LastLpActiveEnergy suspicious...  ");
            return -1;
        }
        
        // 월 사용량
        double usageD = lastLpActiveEnergyImport + lastLpActiveEnergyExport - blockBillActiveEnergy + blockBillAccumulateUsage;
        
        // 사용량이 0보다 작으면 종료한다.
        if (usageD < 0) {
            log.info("Negative: MONTH_USAGE[" + usageD + "] < 0");
            return -1;
        }
        
        int usage = (int)usageD;
        // 월 선불금액 계산 (int usage를 double usage로 대체해본다)
        double monthBill = blockBill(tariffName, tariffEMList, usageD);
        
                
        // 잔액을 차감한다.
/*        log.info("contract_number[" + contract.getContractNumber() + 
                "] MonthBill[" + monthBill + "] blockBillccumulateBill[" + blockBillAccumulateBill+ "]");
        contract.setCurrentCredit(contract.getCurrentCredit() - (monthBill - blockBillAccumulateBill));
        contractDao.updateCurrentCredit(contract.getId(), contract.getCurrentCredit());*/
        
        // 차감해야할 금액을 리턴
        double cost = monthBill-blockBillAccumulateBill;
        return cost;
    }
    
    
    // 사용량에 따른 Tariff 계산
    public double blockBill(String tariffName, List<TariffEM> tariffEMList, double usage) {
        double returnBill = 0.0;
        double vatBill = 0.0;
        log.info("TariffEM LENGTH: " + tariffEMList.size());
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
                
                if (tariffEMList.get(cnt).getYyyymmdd().compareTo("20160101") >= 0) {
                    activeEnergy = activeEnergy + activeEnergy * publicLevy + activeEnergy * govLevy;
                    log.info("ActiveEnergyCharge with levy: " + activeEnergy);
                }
                
                blockBill = blockUsage * activeEnergy;
                returnBill = returnBill + blockBill;
                log.info("Block Usage[" + blockUsage + "]");
                log.info("ActiveEnergyCharge: " + tariffEMList.get(cnt).getActiveEnergyCharge());
                log.info("block bill[" + blockBill + "]");
                
                if (tariffEMList.get(cnt).getYyyymmdd().compareTo("20160101") >= 0) {
                    blockVat = blockBill * vat;
                    vatBill = vatBill + blockVat; 
                    log.info("block vat[" + blockVat + "]");
                }
                
            }
        }

        log.info("returnBill[" + returnBill + "], vatBill[" + vatBill + "], monthBill[" + (returnBill + vatBill) +"]");
        return (returnBill + vatBill);
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
    
    
    
    // 입력달의 마지막날
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
    
    
    //Prepaymentlog 저장 (환급 기록)
    private void saveRefundPrepaymentLog(Contract contract, double usage, double bill,
							            String lastLpTime, String lastTokenDate, 
							            double activeEnergyImport, double activeEnergyExport,
							            double old_diff, double new_diff, double refund) {
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
    }
    
    
  
    
}


