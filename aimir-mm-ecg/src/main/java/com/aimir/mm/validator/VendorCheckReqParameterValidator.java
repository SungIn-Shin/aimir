package com.aimir.mm.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aimir.dao.prepayment.VendorCasherDao;

@Service(value = "vendorCheckReqParameterValidator")
public class VendorCheckReqParameterValidator {

	@Autowired
	VendorCasherDao vendorCasherDao;

	public void validator(String vendor_id) throws Exception {

		if (vendor_id == null || "".equals(vendor_id)) {
			throw new Exception("Vendor ID is empty");
		}

//		if (vendorCasherDao.findByCondition("vendorId", Integer.parseInt(vendor_id)) == null) {
//			throw new Exception("Vendor ID is invalid");
//		}
	}

}
