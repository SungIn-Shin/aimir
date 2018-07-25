package com.aimir.mm.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aimir.dao.device.MeterDao;

@Service(value = "meterCheckReqParameterValidator")
public class MeterCheckReqParameterValidator {
	
	@Autowired
	private MeterDao meterDao;

	public void validator(String meter_number) throws Exception {
		if (meter_number == null || "".equals(meter_number)) {
			throw new Exception("Meter Number is empty");
		}

		if (meterDao.findByCondition("mdsId", meter_number) == null) {
			throw new Exception("Meter Number is invalid");
		}
	}

}
