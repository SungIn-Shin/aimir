package com.aimir.fep.meter.entry;

import java.util.Comparator;


//public class MDComparator implements Comparator 
public class MDComparator 
{
    public static final Comparator TIMESTAMP_ORDER= new Comparator()
    {
        public int compare(Object o1,Object o2)
        {
            MeasurementData md1 = (MeasurementData)o1;
            MeasurementData md2 = (MeasurementData)o2;

            String ts1 = md1.getTimeStamp();
            String ts2 = md2.getTimeStamp();

            return ts1.compareTo(ts2);
        }
        public boolean equals(Object objs)
        {
            return this.equals(objs);
        }
    };
}
