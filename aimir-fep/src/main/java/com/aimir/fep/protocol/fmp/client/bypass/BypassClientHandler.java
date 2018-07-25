package com.aimir.fep.protocol.fmp.client.bypass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.aimir.fep.bypass.BypassDevice;
import com.aimir.fep.bypass.actions.CommandAction;
import com.aimir.fep.protocol.fmp.datatype.OCTET;
import com.aimir.fep.protocol.fmp.exception.FMPACKTimeoutException;
import com.aimir.fep.protocol.fmp.exception.FMPException;
import com.aimir.fep.protocol.fmp.exception.FMPResponseTimeoutException;
import com.aimir.fep.protocol.fmp.frame.ControlDataConstants;
import com.aimir.fep.protocol.fmp.frame.ControlDataFrame;
import com.aimir.fep.protocol.fmp.frame.GeneralDataConstants;
import com.aimir.fep.protocol.fmp.frame.ServiceDataFrame;
import com.aimir.fep.protocol.fmp.frame.service.CommandData;
import com.aimir.fep.protocol.fmp.frame.service.ServiceData;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.FMPProperty;
import com.aimir.fep.util.FrameUtil;
import com.aimir.fep.util.Hex;

public class BypassClientHandler implements IoHandler {

    private static Log log = LogFactory.getLog(BypassClientHandler.class);
    
    private final int IDLE_TIME = Integer.parseInt(FMPProperty.getProperty(
            "protocol.idle.time","5"));
    private final int BYPASS_WAIT_TIME = Integer.parseInt(FMPProperty.getProperty(
            "protocol.bypass.waittime","30")) ;
    private int responseTimeout = Integer.parseInt(
            FMPProperty.getProperty("bypass.response.timeout","60"));
    private int ackTimeout = Integer.parseInt(
            FMPProperty.getProperty("protocol.ack.timeout","3"));
    
    private Object ackMonitor = new Object();
    private ControlDataFrame ack = null;
    private CommandAction action = null;
    
    private Object resMonitor = new Object();
    private Hashtable response = new Hashtable();
    
    /**
     * wait ACK ControlDataFrame
     */
    public void waitAck()
    {
        synchronized(ackMonitor)
        { 
            try { 
                //log.debug("ACK Wait");
                ackMonitor.wait(500); 
                //log.debug("ACK Received");
            } catch(InterruptedException ie) {ie.printStackTrace();}
        }
    }
    
    /**
     * wait util received ACK ControlDataFrame
     *
     * @param session <code>IoSession</code> session
     * @param sequence <code>int</code> wait ack sequence
     */
    public void waitAck(IoSession session, int sequence)
        throws Exception
    { 
        //log.debug("waitAck "+ sequence);
        int waitAckCnt = 0;
        while(session.isConnected())
        {
            if(ack == null)
            {
                waitAck();
                waitAckCnt++;
                if((waitAckCnt / 2) > ackTimeout)
                { 
                    throw new FMPACKTimeoutException(
                        "ACK Wait Timeout[" +ackTimeout +"]");
                }
            }
            else
            {
                int ackseq = FrameUtil.getAckSequence(this.ack);
                //log.debug("ackseq : " + ackseq);
                if(sequence == ackseq)
                {
                    setAck(null);
                    break;
                }
                else
                    setAck(null);
            }
        }
        //log.debug("finished waitAck "+ sequence);
    }
    
    /**
     * set ACK ControlDataFrame
     *
     * @param ack <code>ControlDataFrame</code> ack
     */
    public void setAck(ControlDataFrame ack)
    {
        this.ack = ack;
    }

    
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		log.info("    ");
		log.info("    ");
		log.info("    ");
		log.info("############################## Logging Start ~!! ################################################");
		
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	Date d = new Date(session.getLastWriterIdleTime());
    	log.info("### sessionOpened : " + session.getRemoteAddress() + ", lastWriteIdleTime : " + sf.format(d));
		
        
        session.setAttribute(session.getRemoteAddress(), new BypassDevice());
        session.getConfig().setWriteTimeout(BYPASS_WAIT_TIME);
        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, BYPASS_WAIT_TIME);
        
        session.write(new ControlDataFrame(ControlDataConstants.CODE_ENQ));
        this.ack = null;		
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
        log.info("### Bye Bye ~ Client session closed from " + session.getRemoteAddress().toString());
        synchronized(ackMonitor) { ackMonitor.notify(); }
        synchronized(resMonitor) { resMonitor.notify(); }
        
        session.removeAttribute(session.getRemoteAddress());
        //CloseFuture future = session.close(false);		
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.info("### Bye Bye ~ Client session closed from " + session.getRemoteAddress().toString());
        session.removeAttribute(session.getRemoteAddress());
        session.closeNow();
		
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        session.removeAttribute(session.getRemoteAddress());
        CloseFuture future = session.closeNow();
	}

	@Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	log.info("### [MESSAGE_RECEIVED] from=" + session.getRemoteAddress().toString() + "  SessionId=" +  session.getId() + " ###");

    	String modemType = session.getAttribute("modemType") ==  null ? "" : (String)session.getAttribute("modemType");
    	
    	if (modemType.equals("ZigBee") || modemType.equals("ZRU")) { // ECG ZigBee STS
			if (message instanceof byte[]) {
				byte[] frame = (byte[]) message;
				log.debug("BypassFrame[" + Hex.decode(frame) + "]");
				
				if (action == null) {
					action = (CommandAction) Class.forName("com.aimir.fep.bypass.actions.CommandAction_GG").newInstance();
				}
				
				action.executeBypass2(frame, session);
			}
		} else {
			if (message instanceof ControlDataFrame) {
	            ControlDataFrame cdf = (ControlDataFrame)message;
	            byte code = cdf.getCode();
	            log.info("==> Control Frame Code=["+code+"]");
	            if(code == ControlDataConstants.CODE_NEG)
	            {
	            	log.debug("CODE_NEG Received ");
	                receiveNEG(session, cdf);   
	            }
	            else if (code == ControlDataConstants.CODE_EOT) {
	                log.debug("CODE_EOT Received ");
	                session.closeNow();
	            }
			} else if (message instanceof ServiceDataFrame) {
	            ServiceDataFrame sdf = (ServiceDataFrame)message;
	            // 모뎀이나 미터 시리얼번호 응답이 오면 bypassService를 실행한다.
	            String ns = (String)session.getAttribute("nameSpace");
	            ServiceData sd = ServiceData.decode(ns, sdf, session.getRemoteAddress().toString());
	            log.info("==> ServiceData Frame");
	            session.setAttribute("ServiceData", sd);
	            if (action == null) {               
	                action = (CommandAction)Class.forName("com.aimir.fep.bypass.actions.CommandAction_"+ns).newInstance();
	            }
	            CommandData cd = action.executeBypassClient(null, session);
	            
	            if(cd != null)
	            {	log.info(cd.toString());
	            	response.put(String.valueOf(session.getId()), cd);
	                synchronized(resMonitor) 
	                { 
	                    resMonitor.notify(); 
	                }
	            } else {
	            	 log.info("cd is null");
	            }
			} else if (message instanceof byte[]) {
	            byte[] frame = (byte[])message;
	            log.debug("BypassFrame[" + Hex.decode(frame) + "]");
	            if (action == null) {
	                String ns = (String)session.getAttribute("nameSpace");
	                action = (CommandAction)Class.forName("com.aimir.fep.bypass.actions.CommandAction_"+ns).newInstance();
	            }
	            CommandData cd = action.executeBypassClient(frame, session);
	            
	            if(cd != null)
	            {	log.info(cd.toString());
	            	response.put(String.valueOf(session.getId()), cd);
	                synchronized(resMonitor) 
	                { 
	                    resMonitor.notify(); 
	                }
	            } else {
	            	 log.info("cd is null");
	            }
	        }
		}
    }
	
	
    
    /*
     * NEG 프레임을 수신하면 프레임 사이즈와 윈도우 개수를 저장하여 활용한다.
     * 바이패스 핸들러는 명령을 수행하기 위한 것으로 모뎀이 접속하면 어떤 명령을 수행하려 했는지
     * 비동기 내역에서 찾아야 하는데 이때 접속한 모뎀의 시리얼 번호를 먼저 가져와야 한다.
     */
    private void receiveNEG(IoSession session, ControlDataFrame cdf) 
    throws Exception
    {
        byte[] args = cdf.getArg().getValue();
        if(args != null){
            log.info("NEG[" + Hex.decode(args) + "]");
        }

        // enq 버전이 1.2 인 경우 frame size와 window size를 session에 저장한다.
        if (args[0] == 0x01 && args[1] == 0x02) {
            int frameMaxLen = DataUtil.getIntTo2Byte(new byte[] {args[3], args[2]});
            int frameWinSize = DataUtil.getIntToByte(args[4]);
            String nameSpace = new String(DataUtil.select(args, 5, 2));
            session.setAttribute("frameMaxLen", frameMaxLen);
            session.setAttribute("frameWinSize", frameWinSize);
            session.setAttribute("nameSpace", nameSpace);
            log.info("NEG V1.2 Frame Size[" + frameMaxLen + "] Window Size[" + frameWinSize + "] NameSpace["+nameSpace+"]");
            
            // NEG에 대한 응답을 보내고
            session.write(FrameUtil.getNEGR());
        }
        else {
            session.setAttribute("frameMaxLen", GeneralDataConstants.FRAME_MAX_LEN);
            session.setAttribute("frameWinSize", GeneralDataConstants.FRAME_WINSIZE);
            ControlDataFrame negrFrame = FrameUtil.getNEGR();
            negrFrame.setArg(new OCTET(new byte[]{ControlDataConstants.NEG_R_UNSUPPORTED_VERSION}));
            session.write(negrFrame);
            
            Thread.sleep(1000);
        } 
    }
	

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
        log.info("### Bye Bye ~ Client session closed from " + session.getRemoteAddress().toString());
        session.removeAttribute(session.getRemoteAddress());
        session.closeNow();		
	}
	
	public ServiceData getResponse(IoSession session) throws FMPException
	{

		String key = String.valueOf(session.getId());
        long stime = System.currentTimeMillis();
        long ctime = 0;
        int waitResponseCnt = 0;
        
        ServiceData ret = null;

//        BypassDevice bd = (BypassDevice)session.getAttribute(session.getRemoteAddress());
//        Client client = bd.getClient();
//        
//        if (bd.getFrameFactory() instanceof BypassEVNFactory ) {
//            ret = getResponseEVN(session, bd);
//            return ret;
//        }
        
        while(session.isConnected())
        {
        	if(response.containsKey(key))
        	{
        		
        		Object obj = (ServiceData)response.get(key);
        		log.debug((ServiceData)obj);
        		response.remove(key);
        		
        		if(obj == null)
        			continue;
        		
        		return (ServiceData)obj;
        	}
        	else
        	{
        		 waitResponse();
                 ctime = System.currentTimeMillis();
                 
                 if(((ctime - stime)/1000) > responseTimeout)
                 {
                	 response.remove(key);
               		 
                	 throw new FMPResponseTimeoutException(" tid : " 
                             + key +" Response Timeout[" 
                             +BYPASS_WAIT_TIME +"]");
                 }
        	}
        }
		
		return null;
	}

//	private ServiceData getResponseEVN(IoSession session, BypassDevice bd) throws FMPResponseTimeoutException {
//        int retryPerFrame = Integer.parseInt(FMPProperty.getProperty("protocol.bypass.frame.retry", "3"));
//        String key = String.valueOf(session.getId());
//        long stime = System.currentTimeMillis();
//        long ctime = 0;
//        int retry = 0;
////        BypassEVNFactory factory = (BypassEVNFactory)bd.getFrameFactory();
//        String prevStep = factory.getStepName();
//        boolean isSendFrameRetry = factory.isSendFrameRetry();
//        int frameTimeOut = factory.getFrameTimeout();
//
//        if (isSendFrameRetry){
//        	log.info("SendFrameRetry = true");
//            while(session.isConnected())
//            {
//                if(response.containsKey(key))
//                {
//                    Object obj = (ServiceData)response.get(key);
//                    log.debug((ServiceData)obj);
////                    response.remove(key);
//    
//                    if(obj == null) {
//                    	continue;
//                    } else {
//                    	response.remove(key); // OPF-497
//                    }
//                        
//                    return (ServiceData)obj;
//                }
//                else
//                {
//                    waitResponse();
//                    ctime = System.currentTimeMillis();
//    
//                    if(((ctime - factory.getSendTime())/1000) >= frameTimeOut ) {
//                        retry = factory.getSendFrameRetryCount();
//                        if (retry < 0 ){
//                            log.error("METER return error, DLMS failed.");
//                            response.remove(key);
//                            throw new FMPResponseTimeoutException(" tid : " 
//                                    + key +" METER returned error, DLMS failed.");
//                        }
//                        if (retry >= retryPerFrame) {
//                            log.error("DLMS retry failed. Step[" +  factory.getStepName() + "]" 
//                                    +  " Timeout : " + frameTimeOut + "x" + retryPerFrame +"]");
//                            response.remove(key);
//    
//                            throw new FMPResponseTimeoutException(" tid : " 
//                                    + key +" Response Timeout[" 
//                                    + frameTimeOut + "x" + retryPerFrame  +"]");
//                        }
//                        try {
//                            factory.retrySendBypass(retryPerFrame);
//                            }
//                        catch (Exception e){
//                            throw new FMPResponseTimeoutException(e.getMessage());
//                        }
//                    }
//                }
//            }
//        }
//        else {
//            log.info("SendFrameRetry = false");
//            int handshakeTimeout = Integer.parseInt(FMPProperty.getProperty("protocol.bypass.handshake.timeout", "20"));
//            int handshakeRetry = Integer.parseInt(FMPProperty.getProperty("protocol.bypass.handshake.retry", "2"));
//
//            long prevtime = System.currentTimeMillis();
//            while (session.isConnected()) {
//                if (factory.isHandshakeFinish()) {
//                    break;
//                }
//            	else {
//            		waitResponse();
//                    ctime = System.currentTimeMillis();
//                    
//                    if(((ctime - prevtime)/1000) >= handshakeTimeout) {
//                    	log.debug("DLMS handshake responese Timeout. Step[" +  factory.getStepName() + "]");
//                    	if (!prevStep.equals(factory.getStepName())){
//                    		retry = 0;
//                    	}
//                    	
//                    	if (retry >= handshakeRetry) {
//                        	log.debug("DLMS handshake retry failed. Step[" +  factory.getStepName() + "]");
//                            throw new FMPResponseTimeoutException(" tid : " 
//                                    + key +" Response Timeout[" 
//                                    + handshakeTimeout + "x" + handshakeRetry  +"]");
//                    	}
//                    	log.debug("DLMS handshake retry start. Retry Times[" + (retry+1) + "] Step["+  factory.getStepName() + "]");
//                        try {
//                            factory.retryHandshake();
//                        }
//                        catch (Exception e){
//                            throw new FMPResponseTimeoutException(e.getMessage());
//                        }
//                    	retry++;
//                    	prevStep = factory.getStepName();                    	
//                    	prevtime = System.currentTimeMillis();
//                    }
//            	}
//            }
//            // Wait after handshake 
//            while(session.isConnected()){
//                if(response.containsKey(key))
//                {
//                    
//                    Object obj = (ServiceData)response.get(key);
//                    log.debug((ServiceData)obj);                    
//                    
//                    if(obj == null) {
//                    	continue;
//                    } else {
//                    	response.remove(key);
//                    }
//                        
//                    
//                    return (ServiceData)obj;
//                }
//                else
//                {
//                     waitResponse();
//                     ctime = System.currentTimeMillis();
//                     
//                     if(((ctime - stime)/1000) > responseTimeout)
//                     {
//                         response.remove(key);
//                            
//                         throw new FMPResponseTimeoutException(" tid : " 
//                                 + key +" Response Timeout[" 
//                                 +BYPASS_WAIT_TIME +"]");
//                     }
//                }
//            }
//        }
//        
//        log.debug("return null!! Result retry confirm....");
//        waitResponse();
//        if(response.containsKey(key))
//        {
//            Object obj = (ServiceData)response.get(key);
//            log.debug((ServiceData)obj);
//            response.remove(key);
//            
//            return (ServiceData)obj;
//        }	
//        return null;
//    }
    
    /**
     * wait util received command response data
     */
    public void waitResponse()
    {
        synchronized(resMonitor)
        { 
            try { resMonitor.wait(500); 
            } catch(InterruptedException ie) {ie.printStackTrace();}
        }
    }

}
