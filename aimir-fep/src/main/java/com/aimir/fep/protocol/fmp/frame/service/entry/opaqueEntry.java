package com.aimir.fep.protocol.fmp.frame.service.entry;

import com.aimir.fep.protocol.fmp.frame.service.Entry;

/**
 * opaqueEntry
 * generated by MIB Tool, Do not modify
 *
 * @author Y.S Kim (sorimo@nuritelecom.com)
 * @version $Rev: 1 $, $Date: 2005-11-21 15:59:15 +0900 $,
 */
public class opaqueEntry extends Entry {



    public String toString()
    {
        StringBuffer sb = new StringBuffer();

		sb.append("CLASS["+this.getClass().getName()+"]\n");


        return sb.toString();
    }
}
