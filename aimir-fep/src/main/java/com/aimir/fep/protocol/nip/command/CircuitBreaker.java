package com.aimir.fep.protocol.nip.command;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandFlow;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandType;
import com.aimir.fep.protocol.nip.frame.payload.AbstractCommand;
import com.aimir.fep.protocol.nip.frame.payload.Command;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.Hex;

public class CircuitBreaker  extends AbstractCommand {

	private static Log log = LogFactory.getLog(CircuitBreaker.class);
	
	public CircuitBreaker() {
		super(new byte[] {(byte) 0x30, (byte) 0x05});
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
		int data = Integer.parseInt((String)info.get("data"));
		
    	Command command = new Command();
        Command.Attribute attr = command.newAttribute();
        Command.Attribute.Data[] datas = attr.newData(1);
        
        command.setCommandFlow(CommandFlow.Request);
        command.setCommandType(CommandType.Set);
        datas[0].setId(getAttributeID());
        datas[0].setValue(new byte[]{DataUtil.getByteToInt(data)});
        attr.setData(datas);
        command.setAttribute(attr);
        return command;
	}
	
	@Override
	public Command get(HashMap p) throws Exception { return null; }

	@Override
	public Command set() throws Exception { return null; }

	@Override
	public Command trap() throws Exception { return null; }

	@Override
	public void decode(byte[] data) throws Exception {
		log.debug("[HSW] HHH - byte[] data:"+Hex.decode(data));
	}

	@Override
	public void decode(byte[] data, CommandType commandType) throws Exception {
		log.debug("[HSW] HHH - byte[] data:"+Hex.decode(data)+" | commandType:"+commandType.name());
	}

}
