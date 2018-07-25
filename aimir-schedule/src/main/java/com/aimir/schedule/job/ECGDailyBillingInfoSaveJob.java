package com.aimir.schedule.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.aimir.fep.util.DataUtil;
import com.aimir.schedule.task.ECGBlockDailyEMBillingInfoSaveV2Task;

public class ECGDailyBillingInfoSaveJob extends QuartzJobBean {
	private static Log log = LogFactory.getLog(ECGDailyBillingInfoSaveJob.class);

	@Override
	protected void executeInternal(JobExecutionContext context)
	throws JobExecutionException {
        log.debug("@@@@@@ ECGBlockDailyEMBillingInfoSaveTask Start @@@@@@");
		
		//EMDailyBillingSave
		ECGBlockDailyEMBillingInfoSaveV2Task emTask = DataUtil.getBean(ECGBlockDailyEMBillingInfoSaveV2Task.class);
		emTask.executeTask(context);

		//GMDailyBillingSave
		//DailyGMBillingInfoSaveTask gmTask = DataUtil.getBean(DailyGMBillingInfoSaveTask.class);
		//gmTask.execute();
		
		//WMDailyBillingSave
		//DailyWMBillingInfoSaveTask wmTask = DataUtil.getBean(DailyWMBillingInfoSaveTask.class);
		//wmTask.execute();

	}

		
	
}
