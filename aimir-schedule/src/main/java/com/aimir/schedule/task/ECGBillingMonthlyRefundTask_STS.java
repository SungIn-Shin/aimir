package com.aimir.schedule.task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import org.springframework.transaction.TransactionStatus;

import com.aimir.constants.CommonConstants.DeviceType;
import com.aimir.dao.mvm.BillingBlockTariffDao;
import com.aimir.dao.mvm.DayEMDao;
import com.aimir.dao.mvm.MonthEMDao;
import com.aimir.dao.mvm.impl.BillingBlockTariffDaoImpl;
import com.aimir.dao.system.CodeDao;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.CustomerDao;
import com.aimir.dao.system.OperatorDao;
import com.aimir.dao.system.PrepaymentLogDao;
import com.aimir.dao.system.TariffEMDao;
import com.aimir.dao.system.TariffTypeDao;
import com.aimir.fep.util.DataUtil;
import com.aimir.model.device.Meter;
import com.aimir.model.system.Code;
import com.aimir.model.system.Contract;
import com.aimir.model.system.Customer;
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.TariffEM;
import com.aimir.model.system.TariffType;
import com.aimir.util.Condition;
import com.aimir.util.Condition.Restriction;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.STSToken;
import com.aimir.util.StringUtil;

public class ECGBillingMonthlyRefundTask_STS extends ScheduleTask {

private static Log log = LogFactory.getLog(ECGBillingMonthlyRefundTask_STS.class);
    
    @Resource(name="transactionManager")
    HibernateTransactionManager txManager;
    
    @Autowired
    DayEMDao dayEMDao;
    
	@Autowired
	MonthEMDao monthEMDao;
	
	@Autowired
	ContractDao contractDao;

	@Autowired
	TariffEMDao tariffEmDao;
	
	@Autowired
	PrepaymentLogDao prepaymentLogDao;
	
	@Autowired
	BillingBlockTariffDao bbtDao;
	
	@Autowired
	OperatorDao operatorDao;
	
	@Autowired
	CustomerDao customerDao;
	
	@Autowired
	TariffTypeDao tariffTypeDao;
	
	@Autowired
	CodeDao codeDao;
	
	TransactionStatus txStatus = null;
	private boolean isNowRunning = false;
	private static final String PAYTYPE_REFUND = "Tariff Refund CR";
	private static final String SERVICE_TYPE_EM = "Electricity";
	
	public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"spring-forcrontab.xml"}); 
        DataUtil.setApplicationContext(ctx);
        
        ECGBillingMonthlyRefundTask_STS task = ctx.getBean(ECGBillingMonthlyRefundTask_STS.class);
        task.execute(null);
        System.exit(0);
    }
	
	private List<Integer> getSTSContractInfos(String serviceType) {
		TransactionStatus txstatus = null;
        List<Integer> contractIds = new ArrayList<Integer>();
        
        try {
            txstatus = txManager.getTransaction(null);
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
            txManager.commit(txstatus);
            
            for (Contract c : contracts) {
                contractIds.add(c.getId());
            }
            
        }catch (Exception e) {
            log.error(e, e);
            if (txstatus != null) txManager.rollback(txstatus);
        }
        return contractIds;
	}
	
	@Override
	public void execute(JobExecutionContext context) {
		if(isNowRunning){
			log.info("########### ECGBillingMonthlyTask_STSRefund is already running...");
			return;
		}
		isNowRunning = true;
		
		log.info("###### Start Monthly Refund Billing for STS ######");
    	String now = DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss");

		String yyyymm = "";
		
		try {
			yyyymm = DateTimeUtil.getPreDay(now, 28).substring(0, 6);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info("\n now: " + now
				+ "\n yyyymm: " + yyyymm);
		
		// 만약 이전에 한번 월간 정산을 한번 했다면 그 이력에 대해서 rollback한다.
		//STS는 롤백안함...
		//rollbackMonthlyAdjustedLog(now.substring(0,6));
		List<Contract> list = contractDao.getECGContract();
		// List<Contract> list = new ArrayList<Contract>();
		// 52560 고객에 대해서만 월정산 실행
		// list.add(contractDao.get(52560));
		
    	String lastTokenDate = now;
    	//lastTokenDate = lastTokenDate.substring(0, 6) + "01000000";
    	
//    	List<Integer> sampleId = new ArrayList<Integer>();
//        sampleId.add(127790);
//        sampleId.add(139866);
//        sampleId.add(49129);
//        sampleId.add(49234);
//        sampleId.add(48094);
//        sampleId.add(49000);
//        sampleId.add(140511);
//        sampleId.add(38565);
//        sampleId.add(49079);
//        sampleId.add(131981);
//        sampleId.add(40588);
//        sampleId.add(42407);
//        sampleId.add(28647);
//        sampleId.add(41164);
//        sampleId.add(43930);
//        sampleId.add(38501);
//        sampleId.add(43916);
//        sampleId.add(51025);
//        sampleId.add(39160);
//        sampleId.add(4509);
//        sampleId.add(38042);
//        sampleId.add(126553);
//        sampleId.add(40638);
//        sampleId.add(40694);
        
        //STS Contract list
        List<Integer> contractIdList = this.getSTSContractInfos(SERVICE_TYPE_EM);
        int size = contractIdList.size();
        int index = 0;
        
        Map<String, List<TariffEM>> tariff = getTariff();
        for ( Contract contract : list  ) {
        	txStatus = null;
        	
        	try {
        	    if (contractIdList.contains(contract.getId())) {
            		txStatus = txManager.getTransaction(null);
            		Operator operator = operatorDao.getOperatorByLoginId("admin");
            		calculateByContract(contract, ++index, size, index, yyyymm+"31",
            		        tariff, yyyymm, lastTokenDate, operator);
            		txManager.commit(txStatus);
        	    }
        	} catch (Exception e){
        		txManager.rollback(txStatus);
        		log.error("Contract ID: " + contract.getId(), e);
        	}
        	
        }
        log.info("###### End Monthly Refund Billing for STS ######");
        isNowRunning = false;
	}
	
	
	private Map<String, List<TariffEM>> getTariff() {
	    List<TariffEM> residential = new ArrayList<TariffEM>();
        // 0 ~ 50
        TariffEM t0 = new TariffEM();
        t0.setYyyymmdd("20180401");
        t0.setActiveEnergyCharge(0.3356);
        t0.setSupplySizeMin(0.0);
        t0.setSupplySizeMax(50.0);
        t0.setDistributionNetworkCharge(0.03);
        t0.setTransmissionNetworkCharge(0.02);
        t0.setAdminCharge(0.0163);
        t0.setReactiveEnergyCharge(0.8532);
        t0.setServiceCharge(2.13);
        t0.setMaxDemand(0.0299);
        residential.add(t0);
        
        // 50 ~ 150
        TariffEM t1 = new TariffEM();
        t1.setYyyymmdd("20180401");
        t1.setActiveEnergyCharge(0.6733);
        t1.setSupplySizeMin(50.0);
        t1.setSupplySizeMax(150.0);
        t1.setDistributionNetworkCharge(0.03);
        t1.setTransmissionNetworkCharge(0.02);
        t1.setAdminCharge(0.0413);
        t1.setServiceCharge(6.33);
        t1.setMaxDemand(0.0389);
        residential.add(t1);
        
        // 150 ~ 300
        TariffEM t2 = new TariffEM();
        t2.setYyyymmdd("20180401");
        t2.setActiveEnergyCharge(0.6733);
        t2.setSupplySizeMin(150.0);
        t2.setSupplySizeMax(300.0);
        t2.setDistributionNetworkCharge(0.03);
        t2.setTransmissionNetworkCharge(0.02);
        t2.setServiceCharge(6.33);
        t2.setMaxDemand(0.0389);
        residential.add(t2);
        
        // 300 ~ 600
        TariffEM t3 = new TariffEM();
        t3.setYyyymmdd("20180401");
        t3.setActiveEnergyCharge(0.8738);
        t3.setSupplySizeMin(300.0);
        t3.setSupplySizeMax(600.0);
        t3.setDistributionNetworkCharge(0.03);
        t3.setTransmissionNetworkCharge(0.02);
        t3.setServiceCharge(6.33);
        residential.add(t3);
        
        // 600 ~
        TariffEM t4 = new TariffEM();
        t4.setYyyymmdd("20180401");
        t4.setActiveEnergyCharge(0.9709);
        t4.setSupplySizeMin(600.0);
        t4.setDistributionNetworkCharge(0.03);
        t4.setTransmissionNetworkCharge(0.02);
        t4.setServiceCharge(6.33);
        residential.add(t4);
        
        List<TariffEM> nonResidential = new ArrayList<TariffEM>();
        // 0 ~ 300
        TariffEM t5 = new TariffEM();
        t5.setYyyymmdd("20180401");
        t5.setActiveEnergyCharge(0.9680);
        t5.setSupplySizeMin(0.0);
        t5.setSupplySizeMax(300.0);
        t5.setDistributionNetworkCharge(0.03);
        t5.setTransmissionNetworkCharge(0.02);
        t5.setRateRebalancingLevy(0.0);
        t5.setServiceCharge(10.5529);
        t5.setEnergyDemandCharge(0.175);
        t5.setReactiveEnergyCharge(0.0498);
        nonResidential.add(t5);
        
        // 300 ~ 600
        TariffEM t6 = new TariffEM();
        t6.setYyyymmdd("20180401");
        t6.setActiveEnergyCharge(1.03);
        t6.setSupplySizeMin(300.0);
        t6.setSupplySizeMax(600.0);
        t6.setDistributionNetworkCharge(0.03);
        t6.setTransmissionNetworkCharge(0.02);
        t6.setRateRebalancingLevy(0.0);
        t6.setServiceCharge(10.5529);
        t6.setEnergyDemandCharge(0.175);
        t6.setReactiveEnergyCharge(0.0265);
        nonResidential.add(t6);
        
        // 600 ~ 
        TariffEM t7 = new TariffEM();
        t7.setYyyymmdd("20180401");
        t7.setActiveEnergyCharge(1.6251);
        t7.setSupplySizeMin(600.0);
        t7.setDistributionNetworkCharge(0.03);
        t7.setTransmissionNetworkCharge(0.02);
        t7.setRateRebalancingLevy(0.0);
        t7.setServiceCharge(10.5529);
        t7.setEnergyDemandCharge(0.175);
        t7.setReactiveEnergyCharge(0.0209);
        nonResidential.add(t7);
        
        Map<String, List<TariffEM>> tariff = new HashMap<String, List<TariffEM>>();
        tariff.put("Residential", residential);
        tariff.put("Non Residential", nonResidential);
        
        return tariff;
	}
	
	/**
	 * tariffEMList에 3월달 activeEnergy값과 , 4월달 subsidy 값을 입력한다.
	 * @param tariffName
	 * @param tariffEMList
	 * @param usage
	 * @return
	 */
    public double[] blockBillWithLevy(String tariffName, List<TariffEM> tariffEMList, double usage) {
        double returnGovLevy = 0.0;
        double returnPublicLevy = 0.0;
        double returnUtilityRelief = 0.0;
        double returnGovSubsidy_s2 = 0.0;
        double returnNewSubsidy_s3 = 0.0;
        double returnEnergyCharge = 0.0;
        double returnOldSubsidy_s3 = 0.0;
        
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
                
                Double activeEnergy = tariffEMList.get(cnt).getActiveEnergyCharge() == null ? 0d : tariffEMList.get(cnt).getActiveEnergyCharge();
                Double govLevy = tariffEMList.get(cnt).getTransmissionNetworkCharge() == null ? 0d : tariffEMList.get(cnt).getTransmissionNetworkCharge();
                Double publicLevy = tariffEMList.get(cnt).getDistributionNetworkCharge() == null ? 0d : tariffEMList.get(cnt).getDistributionNetworkCharge();
                Double utilityRelief = tariffEMList.get(cnt).getMaxDemand() == null ? 0d : tariffEMList.get(cnt).getMaxDemand();
                Double subsidy_s2 = tariffEMList.get(cnt).getAdminCharge() == null ? 0d : tariffEMList.get(cnt).getAdminCharge();
                Double subsidy_s3 = tariffEMList.get(cnt).getRateRebalancingLevy() == null ? 0d : tariffEMList.get(cnt).getRateRebalancingLevy();
                Double old_subsidy_s3 = tariffEMList.get(cnt).getReactiveEnergyCharge() == null ? 0d : tariffEMList.get(cnt).getReactiveEnergyCharge();
                
                // Double tariffGovLevy = activeEnergy*govLevy;
                // Double tariffPublicLevy = activeEnergy*publicLevy;
                activeEnergy = blockUsage * activeEnergy;
                utilityRelief = blockUsage * utilityRelief;
                subsidy_s2 = blockUsage * subsidy_s2;
                subsidy_s3 = blockUsage * subsidy_s3;
                old_subsidy_s3 = blockUsage * old_subsidy_s3;
                
                returnEnergyCharge = returnEnergyCharge + activeEnergy;
                returnUtilityRelief = returnUtilityRelief + utilityRelief;
                returnGovSubsidy_s2 = returnGovSubsidy_s2 + subsidy_s2;
                returnNewSubsidy_s3 = returnNewSubsidy_s3 + subsidy_s3;
                returnOldSubsidy_s3 = returnOldSubsidy_s3 + old_subsidy_s3;
                returnGovLevy = govLevy;
                returnPublicLevy = publicLevy;

                log.info("EnergyCharge: " + returnEnergyCharge);
                log.info("govLevy: " + returnGovLevy);
                log.info("publicLevy: " + returnPublicLevy);
                log.info("utilityRelief: " + returnUtilityRelief);
                log.info("subsidy_s2: " + returnGovSubsidy_s2);
                log.info("subsidy_s3: " + returnNewSubsidy_s3);
                log.info("subsidy_s3(old): " + returnOldSubsidy_s3);
            }
        }
        
        
        return new double[]{returnEnergyCharge, returnGovLevy,returnPublicLevy,returnUtilityRelief, returnGovSubsidy_s2, returnNewSubsidy_s3, returnOldSubsidy_s3};
    }
    
    private TariffEM getTariffByUsage(List<TariffEM> tariffList, Double usage) {
    	TariffEM result = null;
    	
    	for ( TariffEM tariff : tariffList ) {
    		Double min = tariff.getSupplySizeMin();
    		Double max = tariff.getSupplySizeMax();
    		
    		if ( min == null && max == null ) {
    			continue;
    		}
    		
    		if (( min == null && usage <= max )|| (min == 0 && usage <=max)) {
    			result =  tariff;
    		} else if ( max == null && usage > min ) {
    			result =  tariff;
    		} else if ( usage > min && usage <= max ) {
    			result =  tariff;
    		} 
    	}
    	return result;
    }
    
    private void calculateByContract(Contract contract, 
    		int index,
    		int size, 
    		int proccessRate, 
    		String tariffDay, 
    		Map<String, List<TariffEM>> tariff,
    		String yyyymm,
    		String lastTokenDate,
    		Operator operator) {
    	
    	log.info("###" + index +"/" + size +"###");
    	Map<String ,Object> param = new HashMap<String, Object>();
    	
    	Customer customer = null;
		customer = customerDao.get(contract.getCustomerId());
		contract = contractDao.get(contract.getId());
		Meter meter = contract.getMeter();
		
		//IHDID==STSNumber, STSNum이 없으면 안돌림.
        if ( customer == null || meter == null || meter.getIhdId() == null || meter.getIhdId().length()!=11) {
        	log.info("Warning(Canceled): Contract["+ contract.getContractNumber() +"] Device or STS Number is not valid.");
        	return;
        }
		
    	param.put("tariffIndex", contract.getTariffIndex());
    	param.put("searchDate", tariffDay);
    	
    	TariffType tariffType = tariffTypeDao.get(contract.getTariffIndexId());
		String tariffName = tariffType.getName();
    	
    	List<TariffEM> tariffList = tariffEmDao.getApplyedTariff(param);
    	if ( tariffList == null || tariffList.size() < 1 ) {
    		return;
    	}
    	
    	Double[] billUsage = getMaxUsageBill(meter.getMdsId(), yyyymm);
    	double totalUsageD = billUsage[1];
    	
    	int totalUsage = (int)totalUsageD;
    	
    	// BillingBlockTariff의 월누적 사용량과 선불요금을 가져오는 것으로 변경함. 2016.03.03
    	// totalUsage = StringUtil.nullToDoubleZero(prepaymentLogDao.getMonthlyUsageByContract(contract, yyyymm));
    	TariffEM levyTariff = getTariffByUsage(tariffList, totalUsageD);
    	if (levyTariff == null) {
    		log.info("skip \n" +
    				"totalUsageD: " + totalUsageD +
    				"totalUsage: " + totalUsage +
    				"tariffId: " + contract.getTariffIndexId());
    	}
    	// totalAmount = blockBill(tariffName, tariffList, totalUsage);
    	double bill[] = blockBillWithLevy(tariffName,tariff.get(tariffName),totalUsage);
    	
    	// step 1 old tariff로 계산한 energy charge (block usage * energy charge)
    	// Residential = energy charge * 17.5%
    	// Non-Residential = energy charge * 30%
    	Double energyCharge = bill[0];
    	Double oldTariffEnergyCharge = bill[0];
    	// 신규 타리프는 UtilityRelief가 없기 때문에 17/31 일치만큼 빼야 함.
    	Double utilityRelief = bill[3] * 17d / 31d;
    	if("Residential".equals(tariffName))
            energyCharge *= 0.175;
        else if ("Non Residential".equals(tariffName))
            energyCharge *= 0.3;
    	
    	if ("Residential".equals(contract.getTariffIndex().getName()) && totalUsage < 50)
            utilityRelief = 0d;
        else if ("Non Residential".equals(contract.getTariffIndex().getName()) && totalUsage >= 10000)
            utilityRelief = 0d;
    	
    	// step 2
    	// step1의 결과에 17/31 을 곱하여 15일부터 31일까지의 energy charge를 구한다.
    	energyCharge *= (17d / 31d);
    	        
    	// step 3
    	// step2의 결과에 street light levy (public levy : 3%)를 곱한다.
    	// step2의 결과에 national electricfication levy (gov levy : 2%)를 곱한다.
    	Double govLevy = bill[1] * energyCharge;
    	Double publicLevy = bill[2] * energyCharge;
    	
    	// step 4
    	// Non-Residential의 경우 17.5%의 세금을 energy charge에 곱한다.
    	Double vat = 0.0;
		vat = StringUtil.nullToDoubleZero(levyTariff.getEnergyDemandCharge()) * energyCharge;
    	
		// step 5
		// subsidy에 17/31을 곱한다.
		double subsidy_s1 = 0.0;
        // Residential이고 사용량이 50 이하면 life line Subsidy를 계산한다.
        if ("Residential".equals(tariffName) && totalUsage <= 50) {
            subsidy_s1 = (0.8532 - 0.61) * 17d / 31d;
        }
        
		// Residential인 경우 이미 반영됐기 때문에 환급에서 제외한다.
		Double subsidy_s2 = 0.0; // bill[4] * 17d / 31d;
		// Non-Residential인 경우
		Double subsidy_s3 = bill[5] * 17d / 31d;
		// Non-Residential인 경우 이전달과 차이가 있기 때문에 Old타리프의 subsidy 금액만큼을 빼고, 새로운 Subsidy로 계산해서 환급해줘야 한다.
		Double old_subsidy_s3 = bill[6] * 17d / 31d;
        
		// Residential은 s3가 무조건 0, Non Residential이면서 사용량이 10000 이상인 경우 subsidy는 0
        if("Residential".equals(tariffName) || ("Non Residential".equals(tariffName) && totalUsage >= 10000)) {
            subsidy_s3 = 0d;
            old_subsidy_s3 = 0d;
        }
            
    	// Residential Refund = step 2 + step 3
    	// Non-Residential Refund = step2 + step 3 + step 4 - step 5
    	double refund = energyCharge + govLevy + publicLevy + vat + subsidy_s3 + subsidy_s2  + subsidy_s1 - old_subsidy_s3 - utilityRelief;
        
    	// 2015.04.08 월정산중에 충전을 시도하면 충전금액이 반영되지 않을 수 있다.
        // 최근 잔액을 가져오도록 한다.
        contract = contractDao.get(contract.getId());
        
        Double beforeCredit = StringUtil.nullToDoubleZero(contract.getCurrentCredit());
    	Double currentCredit =  beforeCredit + refund;
    	DecimalFormat df = new DecimalFormat("0.####");
    	
    	log.debug("\n=== Contract Number: " + contract.getContractNumber() + " ==="
    	+ "\n Before Credit: " + StringUtil.nullToDoubleZero(beforeCredit)
    	+ "\n After Credit: " + df.format(currentCredit)
    	+ "\n Total Usage: " + df.format(totalUsage)
        + "\n Old Tariff Energy Charge: " + df.format(oldTariffEnergyCharge)
    	+ "\n Diff Energy Charge: " + df.format(energyCharge)
    	+ "\n Public Levy(Street Light): " + df.format(publicLevy)
        + "\n Gov. Levy: " + df.format(govLevy)
        + "\n VAT: " + df.format(vat)
        + "\n New_Subsidy(s1):" + df.format(subsidy_s1)
        + "\n New_Subsidy(s2):" + df.format(subsidy_s2)
        + "\n New_Subsidy(s3):" + df.format(subsidy_s3)
        + "\n Old_Subsidy(s3):" + df.format(old_subsidy_s3)
        + "\n UtilityRelief:" + utilityRelief
    	+ "\n refund:" + df.format(refund)
    	);
    	
    	contract.setCurrentCredit(currentCredit);
        contractDao.update(contract);
    	
    	PrepaymentLog prepaymentLog = new PrepaymentLog();
    	prepaymentLog.setId(Long.parseLong(Integer.toString(contract.getId())
                + Long.toString(System.currentTimeMillis())));
    	prepaymentLog.setLastTokenDate(lastTokenDate);
    	prepaymentLog.setCustomer(customer);
    	prepaymentLog.setContract(contract);        	
    	prepaymentLog.setPreBalance(beforeCredit);
    	prepaymentLog.setBalance(currentCredit);
    	prepaymentLog.setUsedConsumption(totalUsageD);
    	prepaymentLog.setPublicLevy(publicLevy);
    	prepaymentLog.setGovLevy(govLevy);
    	prepaymentLog.setLifeLineSubsidy(subsidy_s1);
    	prepaymentLog.setSubsidy(subsidy_s1+subsidy_s2+subsidy_s3-old_subsidy_s3);
    	prepaymentLog.setVat(vat);
        prepaymentLog.setLocation(contract.getLocation());
        prepaymentLog.setTariffIndex(contract.getTariffIndex());
        prepaymentLog.setUtilityRelief(utilityRelief);
        prepaymentLog.setActiveEnergyImport(oldTariffEnergyCharge);
        prepaymentLog.setActiveEnergyExport(energyCharge);
        prepaymentLog.setCancelReason("Consumption:"+df.format(totalUsageD) + 
                ",OldTariffCharge:"+df.format(oldTariffEnergyCharge) +
                ",(+)EnergyCharge:"+df.format(energyCharge) +
                ",(+)PublicLevy:" + df.format(publicLevy) +
                ",(+)GovLevy:" + df.format(govLevy) +
                ",(+)N_Subsidy(s1):" + df.format(subsidy_s1) +
                ",(+)N_Subsidy(s2):" + df.format(subsidy_s2) +
                ",(+)N_Subsidy(s3):" + df.format(subsidy_s3) +
                ",(-)O_Subsidy(s3):" + df.format(old_subsidy_s3) +
                ",(-)UR: " + df.format(utilityRelief) +
                ",(+)VAT:" + df.format(vat) +
                ",Refund:" + df.format(refund));
        Code payType = codeDao.getCodeByName(PAYTYPE_REFUND);
        prepaymentLog.setPayType(payType);
        operator = operatorDao.getOperatorByLoginId("ECG Service");
        prepaymentLog.setOperator(operator);
        prepaymentLog.setTariffIndex(contract.getTariffIndex());
        prepaymentLog.setChargedCredit(refund);
        prepaymentLog.setDescr("monthly refund for STS meter");
        
        //환급용 토큰 생성
        if(refund > 0) {
        	String ihdId = meter.getIhdId();
        	String token = createToken(ihdId, refund);
        	prepaymentLog.setToken(token);
        	
        	log.info("Token(Balance): Contract[" + contract.getContractNumber() + "] Refund[" + refund +
	                "] STS Number[" + ihdId + "]");
        }
        
    	prepaymentLogDao.add(prepaymentLog);
    }
    
    private Double[] getMaxUsageBill(String meterId, String yyyymm) {
        Set<Condition> conditions = new HashSet<Condition>();
        
        // 계약정보가 널인 것으로 먼저 찾고
        conditions.add(new Condition("id.mdevType", new Object[]{DeviceType.Meter}, null, Restriction.EQ));
        conditions.add(new Condition("id.mdevId", new Object[]{meterId}, null, Restriction.EQ));
        conditions.add(new Condition("id.yyyymmdd", new Object[]{yyyymm+"%"}, null, Restriction.LIKE));
        
        List<Projection> projections = new ArrayList<Projection>();
        projections.add(Projections.alias(Projections.max("accumulateBill"), "bill"));
        projections.add(Projections.alias(Projections.max("accumulateUsage"), "usage"));
        
        List<Map<String, Object>> usageBill = ((BillingBlockTariffDaoImpl) bbtDao).findByConditionsAndProjections(conditions, projections);
        
        if (usageBill == null || usageBill.size() == 0)
            return new Double[]{0.0, 0.0};
        else {
            return new Double[]{(Double)usageBill.get(0).get("bill"), (Double)usageBill.get(0).get("usage")};
        }
    }
    
    /**
     * @MethodName rollbackMonthlyAdjustedLog
     * @Date 2013. 11. 21.
     * @param yyyymm
     * @Modified
     * @Description 월간 정산으로 차감된 Contract의 선금을 복원하고, PrepaymentLog를 삭제한다. 
     */
    private void rollbackMonthlyAdjustedLog(String yyyymm) {
    	
    	try {
    		txStatus = txManager.getTransaction(null);
			
    		LinkedHashSet<Condition> condition = new LinkedHashSet<Condition>();
            condition.add(new Condition("lastTokenDate", new Object[] {yyyymm+"%"}, null, Restriction.LIKE));
            condition.add(new Condition("payType", new Object[] {"code"}, null, Restriction.ALIAS));
            condition.add(new Condition("code.name", new Object[] {PAYTYPE_REFUND}, null, Restriction.EQ));
            List<PrepaymentLog> refundLog = prepaymentLogDao.findByConditions(condition);;
            
			log.info("\n\n Rollback previous Monthly transaction for this month \n" + 
			"Log Size: " + refundLog.size() + "\n\n");
			int cnt= 0;
			/*List<Integer> sampleId = new ArrayList<Integer>();
			sampleId.add(127790);
			sampleId.add(139866);
			sampleId.add(49129);
			sampleId.add(49234);
			sampleId.add(48094);
			sampleId.add(49000);
			sampleId.add(140511);
			sampleId.add(38565);
			sampleId.add(49079);
			sampleId.add(131981);
			sampleId.add(40588);
			sampleId.add(42407);
			sampleId.add(28647);
			sampleId.add(41164);
			sampleId.add(43930);
			sampleId.add(38501);
			sampleId.add(43916);
			sampleId.add(51025);
			sampleId.add(39160);
			sampleId.add(4509);
			sampleId.add(38042);
			sampleId.add(126553);
			sampleId.add(40638);
			sampleId.add(40694);*/
			
			//STS Contract list
	        List<Integer> contractIdList = this.getSTSContractInfos(SERVICE_TYPE_EM);
			
			for ( PrepaymentLog logData : refundLog ) {
			    if (contractIdList.contains(logData.getContractId())) {
    				Contract contract  = contractDao.get(logData.getContractId());
    				
    				log.debug("\n==rollback contract " + cnt + " ==\n" +
                            "contractNumber: " + contract.getContractNumber() + "\n" +
                            "preCredit: " + logData.getPreBalance() + "\n" + 
                            "refund: " + logData.getChargedCredit() + "\n" +  
                            "credit: " + contract.getCurrentCredit() + "\n" );
    				
    				if (refundLog != null) {
				        contract.setCurrentCredit(contract.getCurrentCredit() - logData.getChargedCredit());
				        prepaymentLogDao.delete(logData);
    				}
    				
    				contractDao.update(contract);
    				
    				if ((++cnt)%1000 == 0) {
                        contractDao.flushAndClear();
                        prepaymentLogDao.flushAndClear();
                    }
			    }
			}
			txManager.commit(txStatus);
		} catch (Exception e) {
			txManager.rollback(txStatus);
			e.printStackTrace();
		}
    	
    }
    
    
    // 주어진 STS Number에 대한 토큰 생성
    private String createToken(String _stsNumber, double amount) {
  		String token = "";
  		String sendURL = "";

  		String TCT = "02";
  		String EA = "07";
  		String MfrCode = "96";
  		String TI = "01";
  		String KRN = "1";
  		String subClass = "4"; // default 4(전기미터) 5(수도)

  		try {
  			Properties prop = new Properties();
  			prop.load(getClass().getClassLoader().getResourceAsStream("config/schedule.properties"));
  			String stsBaseUrl = prop.getProperty("sts.baseUrl");
  			String SGC = prop.getProperty("sts.sgcNumber") == null ? "" : prop.getProperty("sts.sgcNumber");
  			// String stsBaseUrl = "http://192.168.7.48:8080/stsvend/";
  			// String SGC = "600550";

  			String DSN = _stsNumber;

  			Double credit = amount;
  			Double currencyUnits = 0.01;
  			Double chargedFormatCredit = Double.parseDouble(String.format("%.2f", credit));
  			Double value = chargedFormatCredit / currencyUnits;
  			String tokenValue = String.valueOf(Math.round(value));
  			// 필요시 변경 -> CMSService에는 10.0으로 하드코딩되어있음.
  			// String tokenValue = "10.00";

  			String tokenDate = DateTimeUtil.getDateString(new Date());
  			String DOE = STSToken.getDOE(tokenDate);
  			String idRecord = STSToken.getIdRecord(TCT, EA, SGC, KRN, DSN, DOE, MfrCode, TI);

  			if (credit <= 0.0) {
  				// 초기 설정된 Credit이 0.0이면 토큰 생성할 필요없음. 음수는 잘못 입력된것으로 판단.
  				log.info("##-- Warning : Invalid Credit on Properties");
  				return "";
  			}

  			if (stsBaseUrl != null && !"".equals(stsBaseUrl)) {
  				// TSM에 request 전송
  				sendURL = stsBaseUrl.trim() + "VendCredit.ini?";
  				sendURL += "meterId=" + idRecord + "&subclass=" + subClass + "&value=" + tokenValue;
  				token = STSToken.makeToken(sendURL);
  			}

  			if (token != null && !token.isEmpty()) {
  				return token;
  			}
  		} catch (Exception e) {
  			log.info("##-- Error : getTokenValue");
  			log.error(e, e);
  		} 

  		return "";
  	}
    
	
}
