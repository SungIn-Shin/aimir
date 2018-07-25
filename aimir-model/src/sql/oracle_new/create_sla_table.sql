create sequence sla_target_seq;

drop table sla_target;
create table sla_target (
  meter_type varchar2(31) not null,
  meter_id varchar2(255) not null,
  location_id varchar2(255),
  devicemodel_id varchar2(255),
  supplier_id varchar2(255),
  targetsla number(1),
  constraint sla_target_pk primary key (meter_id)
);

drop table sla_target_log;

DECLARE
  STMT_DDL VARCHAR2(2000);
BEGIN
  STMT_DDL := 'create table sla_target_log ( '||
              '  yyyymmdd varchar2(8), '||
              '  meter_type varchar2(31) not null, '||
              '  meter_id varchar2(255) not null, '||
              '  location_id varchar2(255), '||
              '  devicemodel_id varchar2(255), '||
              '  supplier_id varchar2(255), '||
              '  expect_mv_count number(5) default 24 not null, '||
              '  last_update_date varchar2(14), '||
              '  constraint sla_target_log_pk primary key(yyyymmdd,meter_id) '||
              ') '||
              'PARTITION BY RANGE (yyyymmdd) '||
              '( '||
              '  PARTITION PART'||to_char(CURRENT_DATE,'YYYYMMDD')||' VALUES LESS THAN ('''||TO_CHAR(CURRENT_DATE+1,'YYYYMMDD')||''') '||
              ') TABLESPACE AIMIRPART ';
  EXECUTE IMMEDIATE STMT_DDL;
END;
/

DECLARE
  STMT_DDL VARCHAR2(2000);
BEGIN
  STMT_DDL := 'create table sla_rawdata ( '||
              '  yyyymmdd varchar2(8), '||
              '  meter_id varchar2(255) not null, '||
              '  current_mv_count number(5) default 0 not null, '||
              '  last_update_date varchar2(14), '||
              '  constraint sla_rawdata_pk primary key(yyyymmdd,meter_id) '||
              ') '||
              'PARTITION BY RANGE (yyyymmdd) '||
              '( '||
              '  PARTITION PART'||to_char(CURRENT_DATE,'YYYYMMDD')||' VALUES LESS THAN ('''||TO_CHAR(CURRENT_DATE+1,'YYYYMMDD')||''') '||
              ') TABLESPACE AIMIRPART ';
  EXECUTE IMMEDIATE STMT_DDL;
END;
/

drop table sla_stat;
create table sla_stat (
  yyyymmdd varchar2(8) not null,
  meter_type varchar2(31) not null,
  location_id varchar2(255),
  devicemodel_id varchar2(255),
  supplier_id varchar2(255),
  meter_count number(14) default 0 not null,
  expect_mv_count number(14) default 0 not null,
  sla_6h number(14) default 0,
  sla_1d number(14) default 0,
  sla_2d number(14) default 0,
  sla_3d number(14) default 0,
  sla_4d number(14) default 0,
  sla_5d number(14) default 0,
  sla_final number(14) default 0,
  sla_last_update_date varchar2(14),
  constraint sla_stat_uk unique(yyyymmdd,meter_type,location_id,supplier_id,devicemodel_id)
);

drop table sla_stat_refresh;
create table sla_stat_refresh(
  yyyymmdd varchar2(8) not null,
  constraint sla_stat_refresh_pk primary key(yyyymmdd)
);

INSERT INTO PARTITION_CONTROL (ID,PARTITIONINGINTERVAL,SQLCOMMAND,STARTDATETRUNCATE,ITERATIONS,STARTDATEOFFSET,ACTIVE) values ('SLA_TARGET_LOG','DAILY','ALTER TABLE SLA_TARGET_LOG ADD PARTITION PART{STARTDATE} VALUES LESS THAN(''{NEXTPERIODDATE}'') update indexes','dd',10,0,1);
INSERT INTO PARTITION_CONTROL (ID,PARTITIONINGINTERVAL,SQLCOMMAND,STARTDATETRUNCATE,ITERATIONS,STARTDATEOFFSET,ACTIVE) values ('SLA_TARGET_LOG DROP','DAILY','ALTER TABLE SLA_TARGET_LOG DROP PARTITION PART{STARTDATE} update indexes','dd',15,-365,1);

INSERT INTO PARTITION_CONTROL (ID,PARTITIONINGINTERVAL,SQLCOMMAND,STARTDATETRUNCATE,ITERATIONS,STARTDATEOFFSET,ACTIVE) values ('SLA_RAWDATA','DAILY','ALTER TABLE SLA_RAWDATA ADD PARTITION PART{STARTDATE} VALUES LESS THAN(''{NEXTPERIODDATE}'') update indexes','dd',10,0,1);
INSERT INTO PARTITION_CONTROL (ID,PARTITIONINGINTERVAL,SQLCOMMAND,STARTDATETRUNCATE,ITERATIONS,STARTDATEOFFSET,ACTIVE) values ('SLA_RAWDATA DROP','DAILY','ALTER TABLE SLA_RAWDATA DROP PARTITION PART{STARTDATE} update indexes','dd',15,-365,1);
