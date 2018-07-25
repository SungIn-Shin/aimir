package com.aimir.fep.protocol.nip.command;

import java.util.HashMap;

import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandFlow;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandType;
import com.aimir.fep.protocol.nip.frame.payload.AbstractCommand;
import com.aimir.fep.protocol.nip.frame.payload.Command;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.Hex;

public class TestDataUpload extends AbstractCommand{
    
    public TestDataUpload() {
        super(new byte[] {(byte)0x50, (byte)0x02});
    }
    
    private String dataInterval;
    private String dataCount;
    private DummyMeter dummyMeter;
    private DummyTimeData[] dummyTimeData;
    private String[] timeStr;
    
	public String getDataInterval() {
		return dataInterval;
	}

	public void setDataInterval(String dataInterval) {
		this.dataInterval = dataInterval;
	}

	public String getDataCount() {
		return dataCount;
	}

	public void setDataCount(String dataCount) {
		this.dataCount = dataCount;
	}
	
	public DummyTimeData[] getDummyTimeData() {
		return dummyTimeData;
	}
	public void setDummyTimeData(DummyTimeData[] dummyTimeData) {
		this.dummyTimeData = dummyTimeData;
	}

	@Override
	public void decode(byte[] bx) {
        int pos = 0;
        byte[] b = new byte[8];
        System.arraycopy(bx, pos, b, 0, b.length);
        dummyMeter.meterSerial = String.valueOf(DataUtil.getIntTo8Byte(b));
        pos += b.length;
        
        b = new byte[2];
        System.arraycopy(bx, pos, b, 0, b.length);
        dummyMeter.offset = DataUtil.getIntTo2Byte(b);
        pos += b.length;
        
        b = new byte[2];
        System.arraycopy(bx, pos, b, 0, b.length);
        dummyMeter.count = DataUtil.getIntTo2Byte(b);
        pos += b.length;
        
        dummyTimeData = new DummyTimeData[dummyMeter.count];
        
        for(int i=0 ; i < dummyTimeData.length-1;i++){
        	dummyTimeData[i] = new DummyTimeData();
            b = new byte[2];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setYear(DataUtil.getIntTo2Byte(b));
            pos += b.length;
            
            b = new byte[1];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setMonth(DataUtil.getIntToByte(b[0]));
            pos += b.length;
            
            b = new byte[1];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setDay(DataUtil.getIntToByte(b[0]));
            pos += b.length;
            
            b = new byte[1];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setHour(DataUtil.getIntToByte(b[0]));
            pos += b.length;
            
            b = new byte[1];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setMin(DataUtil.getIntToByte(b[0]));
            pos += b.length;
            
            b = new byte[1];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setSec(DataUtil.getIntToByte(b[0]));
            pos += b.length;
            
            b = new byte[4];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setData(String.valueOf(DataUtil.getIntTo4Byte(b)));
            pos += b.length;
        }
        
    }
	
	// SP-575 add start
	public void decode2(byte[] bx) {
        int pos = 0;
        byte[] b = new byte[8];
        System.arraycopy(bx, pos, b, 0, b.length);
        dummyMeter = new DummyMeter();
        dummyMeter.meterSerial = Hex.decode(b);
        pos += b.length;
        
        b = new byte[2];
        System.arraycopy(bx, pos, b, 0, b.length);
        dummyMeter.offset = DataUtil.getIntTo2Byte(b);
        pos += b.length;
        
        b = new byte[2];
        System.arraycopy(bx, pos, b, 0, b.length);
        dummyMeter.count = DataUtil.getIntTo2Byte(b);
        pos += b.length;

        dummyTimeData = new DummyTimeData[dummyMeter.count];
        timeStr = new String[dummyMeter.count];
        for(int i=0 ; i < dummyTimeData.length;i++){
        	dummyTimeData[i] = new DummyTimeData();
        	b = new byte[7];
        	System.arraycopy(bx, pos, b, 0, b.length);
        	timeStr[i] = CreateDateFormat(b);
            pos += b.length;
            
            b = new byte[4];
            System.arraycopy(bx, pos, b, 0, b.length);
            dummyTimeData[i].setData(Hex.decode(b));
            pos += b.length;
        }
    }
	// SP-575 add end

	@Override
	public String toString() {
		 StringBuffer rtn= new StringBuffer();
	    for(int i=0; i< dummyTimeData.length-1 ;i++){
	    	rtn.append("[i:"+i+"]");
        	rtn.append("[year:"+((DummyTimeData)dummyTimeData[i]).getYear()+"]");
        	rtn.append("[month:"+((DummyTimeData)dummyTimeData[i]).getMonth()+"]");
        	rtn.append("[day:"+((DummyTimeData)dummyTimeData[i]).getDay()+"]");
        	rtn.append("[hour:"+((DummyTimeData)dummyTimeData[i]).getHour()+"]");
        	rtn.append("[min:"+((DummyTimeData)dummyTimeData[i]).getMin()+"]");
        	rtn.append("[sec:"+((DummyTimeData)dummyTimeData[i]).getSec()+"]");
        	rtn.append("[data:"+((DummyTimeData)dummyTimeData[i]).getData()+"]");
	    }
	    return "[TestDataUpload]"+
	 		   "[meterSerial:"+dummyMeter.meterSerial+"]"+
	 		   "[offset:"+dummyMeter.offset+"]"+
	 		   "[count:"+dummyMeter.count+"]"+
	 		   rtn.toString();	   
	}

	// SP-575 add start
	public String toString2() {
        StringBuffer rtn= new StringBuffer();

        rtn.append("\n");
		for(int i=0; i< dummyTimeData.length ;i++){
	        rtn.append("[Time: ");
	        rtn.append(timeStr[i]+", ");
	        rtn.append("Data: "+((DummyTimeData)dummyTimeData[i]).getData()+"]");
	        if(i != (dummyTimeData.length-1)){
		        rtn.append(", \n");	
	        }
        }
		return "Meter Serial: "+dummyMeter.meterSerial+", "+
		 	   "Offset: "+dummyMeter.offset+", "+
		 	   "Count: "+dummyMeter.count+", "+
		 	   rtn.toString();	   		
	}
	
    private String CreateDateFormat(byte[] b){
        String convertStr = String.format("%04d-%02d-%02d %02d:%02d:%02d"
							,Integer.parseInt(String.format("%02x%02x", b[0], b[1]), 16)
							,b[2]
							,b[3]
							,b[4]
							,b[5]
							,b[6]
							);

        return convertStr;
    }
	// SP-575 add end
	
	@Override
	public Command get(HashMap info) throws Exception {
		
		int offset = (int)info.get("offset");
		int count = (int)info.get("count");
        Command command = new Command();
        Command.Attribute attr = command.newAttribute();
        Command.Attribute.Data[] datas = attr.newData(1);
        
        command.setCommandFlow(CommandFlow.Request);
        command.setCommandType(CommandType.Get);
        datas[0].setId(getAttributeID());
        datas[0].setValue(DataUtil.get2ByteToInt(offset));
        datas[0].setValue(DataUtil.get2ByteToInt(count));
        attr.setData(datas);
        command.setAttribute(attr);
        return command;
    }

	@Override
	public void decode(byte[] p1, CommandType p2) throws Exception{}

	@Override
	public Command get() throws Exception{return null;}

	@Override
	public Command set() throws Exception{return null;}

	@Override
	public Command set(HashMap p) throws Exception{return null;}

	@Override
	public Command trap() throws Exception{return null;}
}
