package com.aimir.fep.protocol.nip.command;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandFlow;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandType;
import com.aimir.fep.protocol.nip.frame.payload.AbstractCommand;
import com.aimir.fep.protocol.nip.frame.payload.Command;
import com.aimir.fep.util.DataUtil;

public class BypassMeterInterface extends AbstractCommand{

	public enum BMInterface {
		NONE(0, (byte) 0x00),
		METER_INTERFACE_UART_01(1, (byte) 0x01),
		METER_INTERFACE_UART_02(2, (byte) 0x02);
		
		private int id;
		private byte code;
		BMInterface(int id, byte code) {
			this.id = id;
			this.code = code;
		}
		
		public int getId() {
			return id;
		}

		public static BMInterface getItem(byte code) {
			for(BMInterface bm : BMInterface.values()) {
				if(bm.code == code) {
					return bm;
				}
			}
			return BMInterface.NONE;
		}
	}
	
	public enum BMStatus {
		ENABLE(0, (byte) 0x00),
		DISABLE(1, (byte) 0xFF);
		
		private int id;
		private byte code;
		BMStatus(int id, byte code) {
			this.id = id;
			this.code = code;
		}
		
		public int getId() {
			return id;
		}
		
		public static BMStatus getItem(byte code) {
			for(BMStatus bs : BMStatus.values()) {
				if(bs.code == code) {
					return bs;
				}
			}
			return BMStatus.DISABLE;
		}
	}
	
	private BMInterface mBMInterface;
	private BMStatus mBMStatus;
	
	public BypassMeterInterface() {
		super(new byte[] { (byte) 0x00, (byte) 0x0C });
	}

	@Override
	public Command get() throws Exception {
		Command command = new Command();
		Command.Attribute attr = command.newAttribute();
		Command.Attribute.Data[] datas = attr.newData(1);

		command.setCommandFlow(CommandFlow.Request);
		command.setCommandType(CommandType.Get);
		datas[0].setId(getAttributeID());

		attr.setData(datas);
		command.setAttribute(attr);
		return command;
	}

	@Override
	public Command set(HashMap info) throws Exception {
		Command command = new Command();
		Command.Attribute attr = command.newAttribute();
		Command.Attribute.Data[] datas = attr.newData(1);

		command.setCommandFlow(CommandFlow.Request);
		command.setCommandType(CommandType.Set);
		datas[0].setId(getAttributeID());
		
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			out.write(DataUtil.get2ByteToInt((int) info.get("interface")));
			datas[0].setValue(out.toByteArray());
			attr.setData(datas);
			command.setAttribute(attr);
			return command;
		} finally {
			if (out != null)
				out.close();
		}
	}


	@Override
	public void decode(byte[] bx) throws Exception {
		int pos = 0;
		
		byte[] bm = new byte[1];
		System.arraycopy(bx, pos, bm, 0, bm.length);
		pos += bm.length;
		mBMInterface = BMInterface.getItem(bm[0]); 
		
		byte[] bs = new byte[1];
		System.arraycopy(bx, pos, bs, 0, bs.length);
		pos += bm.length;
		mBMStatus = BMStatus.getItem(bs[0]);
	}

	public BMInterface getmBMInterface() {
		return mBMInterface;
	}

	public BMStatus getmBMStatus() {
		return mBMStatus;
	}

	@Override
	public Command get(HashMap p) throws Exception { return null; }

	@Override
	public Command set() throws Exception { return null; }
	
	@Override
	public Command trap() throws Exception { return null; }
	
	@Override
	public void decode(byte[] p1, CommandType commandType) throws Exception { }

}
