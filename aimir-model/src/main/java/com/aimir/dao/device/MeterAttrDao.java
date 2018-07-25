package com.aimir.dao.device;

import com.aimir.dao.GenericDao;
import com.aimir.model.device.MeterAttr;

/**
 * SP-898
 * @author
 *
 */
public interface MeterAttrDao extends GenericDao<MeterAttr, Long> {
    public MeterAttr getByMeterId(Integer meter_id);
    public MeterAttr getByMdsId(String mdsId);
}
