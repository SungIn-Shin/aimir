CREATE TABLESPACE OACDAT
DATAFILE '/home/oracle/dbf/oacdat.dbf' SIZE 2048M;
ALTER DATABASE DATAFILE '/home/oracle/dbf/oacdat.dbf' AUTOEXTEND ON;


CREATE TEMPORARY TABLESPACE OACTMP
TEMPFILE '/home/oracle/dbf/oactemp.dbf' SIZE 4096M;


alter session set "_ORACLE_SCRIPT"=true;  

CREATE USER oac IDENTIFIED BY oac
 DEFAULT     TABLESPACE OACDAT
 TEMPORARY   TABLESPACE OACTMP;
 


ALTER USER oac QUOTA UNLIMITED ON OAC;

GRANT RESOURCE,CREATE SESSION,CREATE JOB TO oac;


-- ALTER DATABASE DATAFILE '/home/dbf/aimirpart.dbf' RESIZE 8096M;
-- ALTER DATABASE  TEMPFILE '/home/dbf/aimirtemp.dbf' RESIZE 4096M;
