package com.aimir.fep.bypass.sts.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aimir.fep.bypass.sts.STSDataFrame;
import com.aimir.fep.bypass.sts.STSException;
import com.aimir.fep.util.DataUtil;

public class SuniFirmwareUpdateFileBlockWriteRes extends STSDataFrame {
    private static Log log = LogFactory.getLog(SuniFirmwareUpdateFileBlockWriteRes.class);
    
    private int number = 0;
    private int length = 0;
    private byte[] data = null;
    
	public SuniFirmwareUpdateFileBlockWriteRes(byte[] bx) throws Exception {
		super.decode(bx);
		// parse();
    }
    
    public int getResult() throws Exception {
        if (res.getResult() != 0x00) throw new STSException(res.getRdata());
        else return res.getResult();
    }

    // bypass response format (success) - SUNI Interface Protocol 문서 참조 
	// 40 FE 01 02 00 03 00 AB 01 1E 03 8D 00 F5 83 0D
    private void parse() throws STSException {
        if (res.getResult() == 0x00) {
            int pos = 0;
            
            byte[] b = new byte[2];
            System.arraycopy(res.getRdata(), pos, b, 0, b.length);
            
            pos += b.length;
            number = DataUtil.getIntTo2Byte(b);
            log.info("NUMBER[" + number + "]");
            
            b = new byte[2];
            System.arraycopy(res.getRdata(), pos, b, 0, b.length);
            pos += b.length;
            length = DataUtil.getIntTo2Byte(b);
            log.info("LENGTH[" + length + "]");
            
            data = new byte[length];
            System.arraycopy(res.getRdata(), pos, data, 0, b.length);
            pos += b.length;
        }
        else throw new STSException(res.getRdata());
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
