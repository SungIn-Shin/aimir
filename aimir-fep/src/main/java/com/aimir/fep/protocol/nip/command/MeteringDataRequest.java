package com.aimir.fep.protocol.nip.command;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import com.aimir.fep.meter.data.MeterTimeSyncData;
import com.aimir.fep.protocol.nip.command.ResponseResult.Status;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandFlow;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandType;
import com.aimir.fep.protocol.nip.frame.payload.AbstractCommand;
import com.aimir.fep.protocol.nip.frame.payload.Command;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.Hex;

public class MeteringDataRequest extends AbstractCommand{

	public enum HeaderType {
		None((byte) 0x00),
		IF4((byte) 0x01),
		Reserved((byte) 0x02);
		
		private byte code;
		HeaderType(byte code) {
			this.code = code;
		}
		
		public static HeaderType getItem(byte code) {
			for(HeaderType ht : HeaderType.values()) {
				if(ht.code == code) {
					return ht;
				}
			}
			
			return Reserved;
		}
	}
	
	public enum MeteringDataType {
		None((byte) 0x00),
		DLMSMeter((byte) 0x01),
		PulseMeter((byte) 0x02),		
		KEPCO_DLMSGMeter((byte) 0x03),
		MBUSMeter((byte) 0x05),
		DummyMeter((byte) 0xAA),
		Reserved((byte) 0x06); //(0x06 ~ 0xFF)
		
		private byte code;
		MeteringDataType(byte code) {
			this.code = code;
		}
		
		public static MeteringDataType getItem(byte code) {
			for(MeteringDataType mdt : MeteringDataType.values()) {
				if(mdt.code == code) {
					return mdt;
				}
			}
			return Reserved;
		}
	}
	
    public MeteringDataRequest() {
        super(new byte[] {(byte)0xC2, (byte)0x04});
    }
    
    private int paramCount;
    public int getParamCount() {
        return paramCount;
    }
    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }
        
    private String[] parameters;   
    public String[] getParameters() {
        return parameters;
    }
    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }
    
    private byte[] data; 
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }    

    private Status status;
    private int statusCode;
    private HeaderType headerType = null;
    private MeteringDataType meteringDataType = null;
    private MeteringIF4 meteringIF4 = null;
    
    public Status getStatus() {
        return status;
    }
    
    @Override
    public Command get(HashMap info) throws Exception {
        Command command = new Command();
        Command.Attribute attr = command.newAttribute();
        Command.Attribute.Data[] datas = attr.newData(1);
        
        command.setCommandFlow(CommandFlow.Request);
        command.setCommandType(CommandType.Get);
        datas[0].setId(getAttributeID());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
    	out.write(DataUtil.getByteToInt(1)); // Header Type 0x01 (fixed for SORIA)
    	out.write(DataUtil.getByteToInt(1)); // Metering Data Type 0x01 (fixed for SORIA)       
        
        Object obj = info.get("count");
        if (obj instanceof Integer){
        	out.write(DataUtil.getByteToInt((int)obj));
        }else if (obj instanceof String){
        	out.write(DataUtil.getByteToInt(Integer.parseInt((String)obj)));
        }
        
        obj = info.get("parameters");
        String[] parameters = (String[])obj;
        for(int i=0; i < parameters.length; i++) {
        	out.write(DataUtil.get2ByteToInt(Integer.parseInt(parameters[i].substring(0,4))));	// YYYY
        	out.write(DataUtil.getByteToInt(Integer.parseInt(parameters[i].substring(4,6))));	// MM
        	out.write(DataUtil.getByteToInt(Integer.parseInt(parameters[i].substring(6,8))));	// DD
        	out.write(DataUtil.getByteToInt(Integer.parseInt(parameters[i].substring(8,10))));	// hh
        	out.write(DataUtil.getByteToInt(Integer.parseInt(parameters[i].substring(10,12))));	// mm
        }
        
        //NGM-54
        String meterId = (String)info.get("meterId");
        if(meterId != null && !"".equals(meterId)) {
            out.write(DataUtil.setByteLength(meterId.getBytes(), 20));
        }
        
        datas[0].setValue(out.toByteArray());
        
        attr.setData(datas);
        command.setAttribute(attr);
        return command;
    }
    
    @Override
    public void decode(byte[] bx) {
    	
    	decode2(bx);
    	    	
    	/*
        int pos = 0;
        byte[] b = new byte[2];
        System.arraycopy(bx, 0, b, 0, b.length);
        // SP-575 add start
        statusCode = DataUtil.getIntTo2Byte(b);
        // SP-575 add end
        for (Status s : Status.values()) {
            if (s.getCode()[0] == b[0] && s.getCode()[1] == b[1]) {
                status = s;
                break;
            }
        }
        
        pos += b.length;
        
        b = new byte[bx.length - pos];
        System.arraycopy(bx, pos, b, 0, b.length);
        data = b;
        */
    }
    
    //START NGM-59
    public void decode2(byte[] bx) {    	 
    	 int pos = 0;
    	 byte[] b = new byte[2];
         System.arraycopy(bx, 0, b, 0, b.length);
         pos += b.length;
         
         // SP-575 add start
         statusCode = DataUtil.getIntTo2Byte(b);
         // SP-575 add end
         for (Status s : Status.values()) {
             if (s.getCode()[0] == b[0] && s.getCode()[1] == b[1]) {
                 status = s;
                 break;
             }
         }
         
         byte[] hederType = new byte[1];
         System.arraycopy(bx, pos, hederType, 0, hederType.length);
         pos += hederType.length;
         this.headerType = HeaderType.getItem(hederType[0]);
         
         byte[] meteringDataType = new byte[1];
         System.arraycopy(bx, pos, meteringDataType, 0, meteringDataType.length);
         pos += meteringDataType.length;
         this.meteringDataType = MeteringDataType.getItem(meteringDataType[0]);
         log.debug("HeaderType["+this.headerType.name()+"] MeteringDataType["+this.meteringDataType+"]");         
         
         b = new byte[bx.length - pos];
         System.arraycopy(bx, pos, b, 0, b.length);
         
         if(headerType == HeaderType.IF4) {
        	 meteringIF4 = new MeteringIF4();        	 
        	 data = meteringIF4.decode(b);
         } else {
             b = new byte[bx.length - pos];
             System.arraycopy(bx, pos, b, 0, b.length);
             data = b;
         }
    }
    //END NGM-59
    
    @Override
    public String toString() {
        StringBuffer buf= new StringBuffer();

        // SP-575 add start
        String statusStr = "";
        String resStr = "";
        statusStr = getStatusStr();
        resStr = Hex.decode(data);
        buf.append("Status: ");
        buf.append(statusStr);
        if(status != null){
            if(status.name().equals("Success")){
                buf.append(", "); 
                buf.append("Response Data: ");
                buf.append(resStr);
            }
        }
        // SP-575 add end
        return buf.toString();
    }

    // SP-575 add start    
    private String getStatusStr(){
    	String rtnStr = "";
    	if(status == null){
			rtnStr = "0x" + String.format("%04x", statusCode);
			return rtnStr;
    	}
    	switch(status){
    	case Success:
    		rtnStr = "Success(0x0000)";
			break;
    	case FormatError:
    		rtnStr = "Format Error(0x1001)";
			break;
    	case ParameterError:
    		rtnStr = "Parameter Error(0x1002)";
			break;
    	case ValueOverflow:
    		rtnStr = "Value Overflow Error(0x1003)";
			break;
    	case InvalidAttrId:
    		rtnStr = "Invalid Attribute Id(0x1004)";
			break;
    	case AuthorizationError:
    		rtnStr = "Authorization Error(0x10005)";
			break;
    	case NoDataError:
    		rtnStr = "No Data Error(0x1006)";
			break;
    	case MeteringBusy:
    		rtnStr = "Metering Busy(0x2000)";
			break;
    	case Unknown:
    		rtnStr = "Unknown(0xFF00)";
			break;
    	}
    	return rtnStr;
    }
    // SP-575 add end
 
    @Override
    public Command get() throws Exception{return null;}
    @Override
    public Command set() throws Exception{return null;}
    @Override
    public Command set(HashMap p) throws Exception{return null;}
    @Override
    public Command trap() throws Exception{return null;}
    @Override
    public void decode(byte[] p1, CommandType commandType)
                    throws Exception {
        // TODO Auto-generated method stub        
    }
    
    //2018.04.13 아직  enum으로 구현하지 않았음
    //필요시 enum으로 구현
    public class MeteringIF4 {
    	private String SID;			//모뎀의 EUI
    	private String MID;			//Meter ID
    	private int sType; 			//0x01:전기미터,  0x05:펄스식 가스/수도미터
    	private int svc; 			//1:전기, 2:가스, 3:수도
    	private int vendor; 		//“Meter Vender Code” 참조
    	private int dataCnt; 		//[Length | TimeStamp | DATA] 의 개수
    	private int length; 		//[Timestamp.length + DATA.length]
    	private String timeStamp;	//모뎀에서 수집한 시간
    	
    	public byte[] decode(byte[] bx) {
    		int pos = 0;
    		byte[] payload = null;
    		try {
           	 	byte[] mSid = new byte[8];
           	 	System.arraycopy(bx, pos, mSid, 0, mSid.length);
           	 	pos += mSid.length;
           	 	SID = new String(mSid);
           	 	
           	 	byte[] mMid = new byte[20]; 
           	 	System.arraycopy(bx, pos, mMid, 0, mMid.length);
           	 	pos += mMid.length;
           	 	MID = new String(mMid);
           	 	
           	 	byte[] msType = new byte[1];
           	 	System.arraycopy(bx, pos, msType, 0, msType.length);
           	 	pos += msType.length;
           	 	sType = DataUtil.getIntToByte(msType[0]);
           	 	
           	 	byte[] mSvc = new byte[1];
           	 	System.arraycopy(bx, pos, mSvc, 0, mSvc.length);
           	 	pos += mSvc.length;
           	 	svc = DataUtil.getIntToByte(mSvc[0]);
           	 	
           	 	byte[] mVendor = new byte[1];
           	 	System.arraycopy(bx, pos, mVendor, 0, mVendor.length);
           	 	pos += mVendor.length;
           	 	vendor = DataUtil.getIntToByte(mVendor[0]); 
           	 	
           	 	byte[] mDataCnt = new byte[2];
           	 	System.arraycopy(bx, pos, mDataCnt, 0, mDataCnt.length);
           	 	pos += mDataCnt.length;
           	 	dataCnt = DataUtil.getIntTo2Byte(mDataCnt); 
           	 	
           	 	byte[] mLength = new byte[2];
           	 	System.arraycopy(bx, pos, mLength, 0, mLength.length);
           	 	pos += mLength.length;
           	 	length = DataUtil.getIntTo2Byte(mLength);
           	 	
           	 	byte[] mTimeStamp = new byte[7];
           	 	System.arraycopy(bx, pos, mTimeStamp, 0, mTimeStamp.length);
           	 	pos += mTimeStamp.length;
        		timeStamp = DataUtil.getEMnvModemDate(mTimeStamp);
        		
        		payload = new byte[bx.length - pos];
        		System.arraycopy(bx, pos, payload, 0, payload.length);
    		}catch(Exception e) {
    			log.debug(e, e);
    		}
    		    		
    		return payload;
    	}

		public String getSID() {
			return SID;
		}

		public String getMID() {
			return MID;
		}

		public int getsType() {
			return sType;
		}

		public int getSvc() {
			return svc;
		}

		public int getVendor() {
			return vendor;
		}

		public int getDataCnt() {
			return dataCnt;
		}

		public int getLength() {
			return length;
		}

		public String getTimeStamp() {
			return timeStamp;
		}
    }
    
}
