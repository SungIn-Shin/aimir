package com.aimir.fep.logger;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This Class is Nuri Log Sequence
 *
 * @author Seung woo han
 * @since 2018.07.24
 * @version $revision: 1.1.1.1 $
 **/

public class NuriLogSequence {

	
	/*
	 * NuriLogSequence는 세션 또는 쓰레드별 키값을 부여하여 로그 검색시 편의성을 제공합니다.
	 * Key값은 해당 쓰레드(프로세스)의 Hashcode값으며, value는 "S" + 난수(Snowflake)로 작성됩니다
	 * 값은 3분동안 유지되며, 3분 지난 뒤 해당 맵에서 자동 삭제되도록 정의
	 * 서버를 통해 내린 명령(모뎀 또는 DCU의 Trap)은 
	 * GW와 MINA Socket는 서로 다른 쓰레드로 동작하기 때문에 
	 * MINA 세션이 생성된 뒤 GW와 동일한  Value값 적용
	 */
	
	public volatile static NuriLogSequence instance = null;
	private LoadingCache<Integer, String> seq = null;
	private Snowflake snowflake = null;
	
	public static NuriLogSequence getInstance() {
		if(instance == null) {
			synchronized (NuriLogSequence.class) {
				if(instance == null) {
					instance = new NuriLogSequence();
				}
			}
		}
		
		return instance;
	}
	
	private NuriLogSequence() {
		init();
	}
	
	private void init() {
		snowflake = new Snowflake.Builder(0L, Thread.currentThread().getId())
				.setSequenceBits(12L)
				.build();
		
		seq = CacheBuilder.newBuilder()
				.expireAfterAccess(3, TimeUnit.MINUTES)
				.build(new CacheLoader<Integer, String>() {

					@Override
					public String load(Integer key) throws Exception {
						return "S" + String.valueOf(snowflake.nextId());
					}

				});
	}
	
	public void copySequence(int mirrorKey, int targetKey) {
		if(seq != null) {
			seq.put(targetKey, seq.getUnchecked(mirrorKey));
		}
	}
	
	public void setSequence(int key, String value) {
		if(seq != null) {
			seq.put(key, value);
		}
	}

	public LoadingCache<Integer, String> getSeq() {
		return seq;
	}
	
	
}
