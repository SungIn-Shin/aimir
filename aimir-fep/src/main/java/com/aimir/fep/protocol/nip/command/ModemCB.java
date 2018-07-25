package com.aimir.fep.protocol.nip.command;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandType;
import com.aimir.fep.protocol.nip.frame.payload.AbstractCommand;
import com.aimir.fep.protocol.nip.frame.payload.Command;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.Hex;

public class ModemCB extends AbstractCommand {

	private static Log log = LogFactory.getLog(ModemCB.class);
	private int result;
	
	public ModemCB() {
		super(new byte[] {(byte)0xFF, (byte)0x01});
	}
	
	@Override
	public Command get() throws Exception {
		log.debug("[HSW] HHH - get() Method ");
		return null;
	}

	@Override
	public Command get(HashMap p) throws Exception {
		log.debug("[HSW] HHH - get(HashMap p) Method ");
		return null;
	}

	@Override
	public Command set() throws Exception {
		log.debug("[HSW] HHH - set() Method ");
		return null;
	}

	@Override
	public Command set(HashMap p) throws Exception {
		log.debug("[HSW] HHH  - set(HashMap p) Method ");
		return null;
	}

	@Override
	public Command trap() throws Exception { return null; }

	@Override
	public void decode(byte[] bx) throws Exception {
		log.debug("[HSW] HHH - byte[] : "+Hex.decode(bx));
		
		byte[] b = new byte[1];
		System.arraycopy(bx, 0, b, 0, 1);
		
		result = DataUtil.getIntToByte(b[0]);
	}

	@Override
	public void decode(byte[] bx, CommandType commandType) throws Exception {
		log.debug("[HSW] HHH - bx:"+Hex.decode(bx)+" ||  commandType:"+commandType.name());
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
	
}
