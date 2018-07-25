CREATE TABLESPACE AIMIR
DATAFILE '/oradata/aimirdat/aimir.dbf' SIZE 2048M;
ALTER DATABASE DATAFILE '/oradata/aimirdat/aimir.dbf' AUTOEXTEND ON;



CREATE TABLESPACE AIMIRPART
DATAFILE '/oradata/aimirdat/aimirpart.dbf' SIZE 8096M;
ALTER DATABASE DATAFILE '/oradata/aimirdat/aimirpart.dbf' AUTOEXTEND ON;

CREATE TEMPORARY TABLESPACE AIMIRTMP
TEMPFILE '/oradata/aimirdat/aimirtemp.dbf' SIZE 4096M;


alter session set "_ORACLE_SCRIPT"=true;


CREATE USER aimir IDENTIFIED BY aimir
DEFAULT     TABLESPACE AIMIR
TEMPORARY   TABLESPACE AIMIRTMP;

ALTER USER aimir QUOTA UNLIMITED ON AIMIR;
ALTER USER aimir QUOTA UNLIMITED ON AIMIRPART;

GRANT RESOURCE,CREATE SESSION,CREATE JOB TO aimir;


-- ALTER DATABASE DATAFILE '/home/dbf/aimirpart.dbf' RESIZE 8096M;
-- ALTER DATABASE  TEMPFILE '/home/dbf/aimirtemp.dbf' RESIZE 4096M;
