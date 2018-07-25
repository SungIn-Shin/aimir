/**
 * 
 */
package com.aimir.fep.protocol.nip.client.NotiPlug;

import java.util.Map;

/**
 * @author simhanger
 *
 */
public interface NotiObserver {
	public abstract void observerNotify(String notiGeneratorName, Map<?, ?> params);

	public abstract String getNotiObserverName();
	
	public abstract Map<?,?> getNotiParams();
}
