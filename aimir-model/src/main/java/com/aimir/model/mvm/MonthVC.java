package com.aimir.model.mvm;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

/**
 * Volume Corrector의 월별 total 검침값, 일별 검침값 (1일부터 말일)을 저장하는 클래스
 * 
 * @author YeonKyoung Park(goodjob)
 *
 */
@Entity
@Table(name = "MONTH_VC")
@Indexes({
    @Index(name="IDX_MONTH_VC_01", columnNames={"mdev_type", "mdev_id", "dst", "yyyymm","channel","full_location"})
})
public class MonthVC extends MeteringMonth {

}
