package com.aimir.fep.meter;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.aimir.fep.meter.parser.TS_PulseMeter;
import com.aimir.fep.util.Hex;
import com.aimir.model.device.GasMeter;
import com.aimir.util.DateTimeUtil;

public class TSPulseMeterTest {
    private static Log log = LogFactory.getLog(TSPulseMeterTest.class);
    
    @Test
    public void test() throws Exception {
        byte[] bx = Hex.encode("0000000000000000C78904020007DF09080000C7890000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFF0000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF0107DF09070000C78900000000000000000000000000000000000000000000000000000000000000000000000000000000FFFF0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFF0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        TS_PulseMeter parser = new TS_PulseMeter();
        parser.setMeteringTime(DateTimeUtil.getDateString(new Date()));
        GasMeter meter = new GasMeter();
        meter.setPulseConstant(100.0);
        parser.setMeter(meter);
        parser.parse(bx);
        log.info(parser.toString());
    }
}
