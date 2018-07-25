package com.aimir.fep.meter;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.aimir.dao.device.MeterDao;
import com.aimir.fep.BaseTestCase;
import com.aimir.fep.meter.parser.SX2;
import com.aimir.fep.util.Hex;
import com.aimir.model.device.Meter;

public class SXMetaTest extends BaseTestCase {
    private static Log log = LogFactory.getLog(SXMetaTest.class);
    
    private static final String meta = "861C020000DC070A100D2B29040100011E010000000C7E021E0100000000010DFE6400000000000C236400000000000B216400000000325B2164000000003007DC0A1000000A900000000100000064000059D400000000000000000005000000645A38000000000004000000645A2F000000000005000000645A24000000000004000000645A57000000000004000000645A78000000000005000000645A99000000000004000000645AC6000000000005000000645A9E000000000004000000645AA0000000000004000000645A93000000000005000000645A81000000000004000000645AA4000000000005000000645AD1000000000004000000645AA7000000000004000000645A97000000000005000000645AAB000000000004000000645A93000000000005000000645AA2000000000004000000645A7A000000000005000000645A53000000000004000000645A4C000000000004000000645A1B0000000000050000006459ED0000000000040000006459D40000000000040000006459D90000000000050000006459D90000000000040000006459A60000000000040000006459C70000000000050000006459AA0000000000040000006459BA0000000000050000006459E40000000000040000006459A20000000000040000006459260000000000040000006459180000000000090000006458B000000000000B0000006458D600000000000B0000006458B000000000000B00000064588600000000000F00000041592A0033003100130000004058F30033003100120000003F58F70034003200120000003F58EC00330031FFFFFFFFFFFFFFFFFFFFFFFF00230000003F59210033003100120000003F591B0033003100120000004159560033003000120000004059890033003000120000003F59990033003100120000003F597C0033003100120000003F59B80033003100120000003F59C900330031001200000041597E0033003000120000004159230033003000120000004158EE00330030FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF08003239313030333607023038303831320D343030303130313032333831330AD73030303030333139390AD53030303030333139380AD630303030303030303106D2303030353006D1303030343805EC302E363403F4303103353031";

    @Test
    public void test() {
        SX2 sx2 = new SX2();
        try {
            MeterDao meterDao = this.applicationContext.getBean(MeterDao.class);
            Meter meter = meterDao.get("2910036");
            
            sx2.setMeter(meter);
            sx2.parse(Hex.encode(meta));
            LinkedHashMap map = sx2.getData();
            String key = null;
            for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
                key = (String)i.next();
                log.debug(key + " " + map.get(key));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
