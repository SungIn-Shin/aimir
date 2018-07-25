/*
May-08-2018 07:29:21 PM
 에 Aqua Data Studio 17.0.3에 의해 생성된 스크립트데이터베이스: ORCL
스키마: OAC
객체: SEQUENCE, ARRAY_TYPE, OBJECT_TYPE, TABLE_TYPE, TABLE, CLUSTER, VIEW, SYNONYM, PACKAGE, PACKAGE_BODY, PROCEDURE, FUNCTION, INDEX, TRIGGER
*/
DROP TRIGGER "OAC"."LOG_TRG"
GO
DROP TRIGGER "OAC"."DEVICE_GROUP_TRG1"
GO
DROP TRIGGER "OAC"."DEVICE_GROUP_TRG"
GO
DROP TRIGGER "OAC"."DEVICES_TRG2"
GO
DROP TRIGGER "OAC"."CERTIFICATIONS_TRG"
GO
DROP TRIGGER "OAC"."USERS_TRG"
GO
DROP TRIGGER "OAC"."SERVER_CERT_TRG"
GO
DROP TRIGGER "OAC"."PANA_CERT_TRG"
GO
DROP TRIGGER "OAC"."SERVERS_TRG"
GO
DROP TRIGGER "OAC"."DEVICES_TRG"
GO
DROP INDEX "OAC"."USERS_PK"
GO
DROP INDEX "OAC"."UK_DEVICE_GROUP_NAME"
GO
DROP INDEX "OAC"."UK_DEVICES_D_NUMBER"
GO
DROP INDEX "OAC"."TABLE1_PK"
GO
DROP INDEX "OAC"."SERVERS_PK"
GO
DROP INDEX "OAC"."PREP_TRANS_EXT_KEY"
GO
DROP INDEX "OAC"."METER_TERMINAL_PK"
GO
DROP INDEX "OAC"."LOG_PK"
GO
DROP INDEX "OAC"."DEVICE_GROUP_PK"
GO
DROP INDEX "OAC"."DEVICES_PK"
GO
DROP INDEX "OAC"."CERTIFICATIONS_PK"
GO
DROP FUNCTION "OAC"."SQUIRREL_GET_ERROR_OFFSET"
GO
DROP TABLE "OAC"."USERS"
GO
DROP TABLE "OAC"."SERVER_CERT"
GO
DROP TABLE "OAC"."SERVERS"
GO
DROP TABLE "OAC"."PREP_TRANS_EXT"
GO
DROP TABLE "OAC"."PANA_CERT"
GO
DROP TABLE "OAC"."METER_TERMINAL"
GO
DROP TABLE "OAC"."LOG"
GO
DROP TABLE "OAC"."KEY_GEN_SMSINFO"
GO
DROP TABLE "OAC"."HT_METERCONFIG"
GO
DROP TABLE "OAC"."HT_DEVICECONFIG"
GO
DROP TABLE "OAC"."HES"
GO
DROP TABLE "OAC"."DEVICE_GROUP"
GO
DROP TABLE "OAC"."DEVICES"
GO
DROP TABLE "OAC"."CERTIFICATIONS"
GO
DROP SEQUENCE "OAC"."USERS_SEQ"
GO
DROP SEQUENCE "OAC"."SERVER_CERT_SEQ"
GO
DROP SEQUENCE "OAC"."SERVERS_SEQ1"
GO
DROP SEQUENCE "OAC"."SERVERS_SEQ"
GO
DROP SEQUENCE "OAC"."PANA_CERT_SEQ"
GO
DROP SEQUENCE "OAC"."LOG_SEQ"
GO
DROP SEQUENCE "OAC"."DEVICE_GROUP_SEQ"
GO
DROP SEQUENCE "OAC"."DEVICES_SEQ2"
GO
DROP SEQUENCE "OAC"."DEVICES_SEQ1"
GO
DROP SEQUENCE "OAC"."DEVICES_SEQ"
GO
DROP SEQUENCE "OAC"."CERTIFICATIONS_SEQ"
GO

CREATE SEQUENCE "OAC"."CERTIFICATIONS_SEQ"
	INCREMENT BY 1
	START WITH 61
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."DEVICES_SEQ"
	INCREMENT BY 1
	START WITH 128
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."DEVICES_SEQ1"
	INCREMENT BY 1
	START WITH 1
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."DEVICES_SEQ2"
	INCREMENT BY 1
	START WITH 261
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."DEVICE_GROUP_SEQ"
	INCREMENT BY 1
	START WITH 27
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."LOG_SEQ"
	INCREMENT BY 1
	START WITH 161
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."PANA_CERT_SEQ"
	INCREMENT BY 1
	START WITH 33201
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."SERVERS_SEQ"
	INCREMENT BY 1
	START WITH 1
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."SERVERS_SEQ1"
	INCREMENT BY 1
	START WITH 61
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."SERVER_CERT_SEQ"
	INCREMENT BY 1
	START WITH 101
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE SEQUENCE "OAC"."USERS_SEQ"
	INCREMENT BY 1
	START WITH 41
	MAXVALUE 9999999999999999999999999999
	NOMINVALUE
	NOCYCLE
	CACHE 20
	NOORDER
GO
CREATE TABLE "OAC"."CERTIFICATIONS"  ( 
	"C_ID"              	NUMBER NOT NULL,
	"D_ID"              	NUMBER NOT NULL,
	"PERIOD_OF_VALIDITY"	CHAR(20) NULL,
	"PIN_ARG"           	VARCHAR2(128) NULL,
	"DISCARD_STATE"     	CHAR(1) NOT NULL,
	"CRT_DATE"          	TIMESTAMP(6) NOT NULL,
	"MDF_DATE"          	TIMESTAMP(6) NULL,
	"CERT_BINARY"       	VARCHAR2(2500) NULL,
	"KEY_BINARY"        	VARCHAR2(1000) NULL,
	"ISSUER_DN"         	VARCHAR2(50) NULL,
	"SALT_BINARY"       	VARCHAR2(100) NULL,
	"CERT_SN"           	VARCHAR2(50) NULL,
	"POLICY_NAME"       	VARCHAR2(50) NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."DEVICES"  ( 
	"D_ID"    	NUMBER NOT NULL,
	"DG_ID"   	NUMBER NULL,
	"D_NUMBER"	VARCHAR2(50) NOT NULL,
	"COM_PORT"	NUMBER NOT NULL,
	"CRT_DATE"	TIMESTAMP(6) NOT NULL,
	"MDF_DATE"	TIMESTAMP(6) NULL,
	"DEL_YN"  	CHAR(1) DEFAULT 'N' NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."DEVICE_GROUP"  ( 
	"DG_ID"      	NUMBER NOT NULL,
	"NAME"       	VARCHAR2(128) NOT NULL,
	"MODEL_NAME" 	VARCHAR2(50) NOT NULL,
	"GSM_MODULE" 	CHAR(1) NOT NULL,
	"TEST_TYPE"  	VARCHAR2(128) NULL,
	"MOBILE_TYPE"	NUMBER NULL,
	"VENDER"     	VARCHAR2(30) NULL,
	"PATH"       	VARCHAR2(128) NOT NULL,
	"MEMORY_SIZE"	NUMBER NULL,
	"DEL_YN"     	CHAR(1) DEFAULT 'N' NULL,
	"CRT_DATE"   	TIMESTAMP(6) NOT NULL,
	"MDF_DATE"   	TIMESTAMP(6) NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."HES"  ( 
	"D_NUMBER"  	VARCHAR2(50) NOT NULL,
	"KEY_BINARY"	VARCHAR2(1000) NULL,
	"ISSUER_DN" 	VARCHAR2(50) NULL,
	"CERT_SN"   	VARCHAR2(50) NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645 BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE GLOBAL TEMPORARY TABLE "OAC"."HT_DEVICECONFIG"  ( 
	"ID"	NUMBER(10) NOT NULL 
	)
ON COMMIT DELETE ROWS
GO
CREATE GLOBAL TEMPORARY TABLE "OAC"."HT_METERCONFIG"  ( 
	"ID"	NUMBER(10) NOT NULL 
	)
ON COMMIT DELETE ROWS
GO
CREATE TABLE "OAC"."KEY_GEN_SMSINFO"  ( 
	"SEQ_NAME" 	VARCHAR2(50) NOT NULL,
	"SEQ_COUNT"	NUMBER(38) NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645 BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."LOG"  ( 
	"LOG_ID"        	NUMBER NOT NULL,
	"OCCUR_LOCATION"	CHAR(2) NOT NULL,
	"OCCUR_TYPE"    	CHAR(2) NOT NULL,
	"USER_ID"       	VARCHAR2(50) NOT NULL,
	"OCCUR_CONTENT" 	VARCHAR2(1000) NOT NULL,
	"OCCUR_DATE"    	TIMESTAMP(6) NOT NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645 BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."METER_TERMINAL"  ( 
	"M_NUMBER"             	VARCHAR2(50) NOT NULL,
	"MASTER_KEY"           	VARCHAR2(250) NULL,
	"GLOBAL_KEY"           	VARCHAR2(250) NULL,
	"UNICAST_KEY"          	VARCHAR2(250) NULL,
	"LLS_SECRET_KEY"       	VARCHAR2(250) NULL,
	"HLS_SECRET_GLOBAL_KEY"	VARCHAR2(250) NULL,
	"HLS_SECRET_UNIQUE_KEY"	VARCHAR2(250) NULL,
	"AUTHENTICATION_KEY"   	VARCHAR2(250) NULL,
	"MDF_DATE"             	TIMESTAMP(6) NULL,
	"CRT_DATE"             	TIMESTAMP(6) NOT NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."PANA_CERT"  ( 
	"PC_ID"             	NUMBER NULL,
	"D_ID"              	NUMBER NOT NULL,
	"PERIOD_OF_VALIDITY"	CHAR(20) NULL,
	"DISCARD_STATE"     	CHAR(1) NOT NULL,
	"CRT_DATE"          	TIMESTAMP(6) NOT NULL,
	"MDF_DATE"          	TIMESTAMP(6) NULL,
	"CERT_BINARY"       	VARCHAR2(2500) NULL,
	"KEY_BINARY"        	VARCHAR2(1000) NULL,
	"ISSUER_DN"         	VARCHAR2(50) NULL,
	"CERT_SN"           	VARCHAR2(50) NULL,
	"POLICY_NAME"       	VARCHAR2(50) NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."PREP_TRANS_EXT"  ( 
	"TS"               	DATE NULL,
	"TRANSACTION_ID"   	NUMBER(15) NOT NULL,
	"NUM_APA"          	VARCHAR2(20) NULL,
	"CO_MARCA"         	VARCHAR2(5) NULL,
	"CUST_NAME"        	VARCHAR2(100) NULL,
	"CUSTOMER_NUMBER"  	VARCHAR2(80) NULL,
	"SERVICE_POINT_NO" 	VARCHAR2(80) NULL,
	"VENDOR_ID"        	VARCHAR2(50) NULL,
	"RECPT_NO"         	VARCHAR2(50) NULL,
	"TOKEN_NO"         	VARCHAR2(20) NULL,
	"PMETHOD"          	VARCHAR2(5) NULL,
	"CO_CONCEPTO"      	VARCHAR2(15) NOT NULL,
	"CSMO_FACT"        	NUMBER(12,3) NULL,
	"CYCLE"            	NUMBER(2) NULL,
	"CYCLE_DATE"       	DATE NULL,
	"IMP_CONCEPTO"     	NUMBER(15,3) NULL,
	"DEBT_REF_NO"      	VARCHAR2(50) NULL,
	"COD_UNICOM"       	NUMBER NULL,
	"OPERATOR_NAME"    	VARCHAR2(50) NULL,
	"CO_SISTEMA"       	VARCHAR2(5) NOT NULL,
	"NUM_CHEQUE"       	VARCHAR2(25) NULL,
	"COD_CENCOBRO_BANK"	NUMBER NULL,
	"EXPORTED"         	NUMBER NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."SERVERS"  ( 
	"S_ID"       	NUMBER NOT NULL,
	"SERVER_NAME"	VARCHAR2(50) NOT NULL,
	"IP"         	VARCHAR2(50) NOT NULL,
	"DOMAIN"     	VARCHAR2(63) NOT NULL,
	"CRT_DATE"   	TIMESTAMP(6) NOT NULL,
	"MDF_DATE"   	TIMESTAMP(6) NULL,
	"DEL_YN"     	CHAR(1) DEFAULT 'N' NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."SERVER_CERT"  ( 
	"SC_ID"             	NUMBER NOT NULL,
	"S_ID"              	NUMBER NOT NULL,
	"PERIOD_OF_VALIDITY"	CHAR(20) NULL,
	"DISCARD_STATE"     	CHAR(1) NOT NULL,
	"CRT_DATE"          	TIMESTAMP(6) NOT NULL,
	"MDF_DATE"          	TIMESTAMP(6) NULL,
	"CERT_BINARY"       	VARCHAR2(2500) NULL,
	"KEY_BINARY"        	VARCHAR2(3000) NULL,
	"ISSUER_DN"         	VARCHAR2(50) NULL,
	"CERT_SN"           	VARCHAR2(50) NULL,
	"POLICY_NAME"       	VARCHAR2(50) NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
CREATE TABLE "OAC"."USERS"  ( 
	"U_ID"      	NUMBER NOT NULL,
	"ID"        	VARCHAR2(50) NOT NULL,
	"PWD"       	VARCHAR2(128) NOT NULL,
	"NAME"      	VARCHAR2(30) NOT NULL,
	"DEPARTMENT"	VARCHAR2(30) NULL,
	"POSITION"  	VARCHAR2(30) NULL,
	"TEL"       	VARCHAR2(13) NULL,
	"AUTH"      	CHAR(2) DEFAULT 00 NOT NULL,	 /* 00:??, 01:??? */
	"CRT_DATE"  	TIMESTAMP(6) NOT NULL,
	"MDF_DATE"  	TIMESTAMP(6) NULL 
	)
TABLESPACE "OACDAT" NOCOMPRESS PCTFREE 10 INITRANS 1 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645 BUFFER_POOL DEFAULT )
NOPARALLEL
LOGGING 
NOCACHE 
MONITORING 
NOROWDEPENDENCIES
DISABLE ROW MOVEMENT 
GO
COMMENT ON COLUMN "OAC"."USERS"."AUTH" IS '00:??, 01:???'
GO
CREATE FUNCTION "OAC"."SQUIRREL_GET_ERROR_OFFSET" (query IN varchar2) return number authid current_user is      l_theCursor     integer default dbms_sql.open_cursor;      l_status        integer; begin          begin          dbms_sql.parse(  l_theCursor, query, dbms_sql.native );          exception                  when others then l_status := dbms_sql.last_error_position;          end;          dbms_sql.close_cursor( l_theCursor );          return l_status; end;
GO


CREATE UNIQUE INDEX "OAC"."CERTIFICATIONS_PK"
	ON "OAC"."CERTIFICATIONS"("C_ID")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."DEVICES_PK"
	ON "OAC"."DEVICES"("D_ID")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."DEVICE_GROUP_PK"
	ON "OAC"."DEVICE_GROUP"("DG_ID")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."LOG_PK"
	ON "OAC"."LOG"("LOG_ID")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."METER_TERMINAL_PK"
	ON "OAC"."METER_TERMINAL"("M_NUMBER")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."PREP_TRANS_EXT_KEY"
	ON "OAC"."PREP_TRANS_EXT"("TRANSACTION_ID", "CO_CONCEPTO", "CO_SISTEMA")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."SERVERS_PK"
	ON "OAC"."SERVERS"("S_ID")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."TABLE1_PK"
	ON "OAC"."HES"("D_NUMBER")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."UK_DEVICES_D_NUMBER"
	ON "OAC"."DEVICES"("D_NUMBER")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."UK_DEVICE_GROUP_NAME"
	ON "OAC"."DEVICE_GROUP"("NAME")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE UNIQUE INDEX "OAC"."USERS_PK"
	ON "OAC"."USERS"("U_ID")
TABLESPACE "OACIDX" NOCOMPRESS PCTFREE 10 INITRANS 2 MAXTRANS 255 
STORAGE( INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS UNLIMITED BUFFER_POOL DEFAULT )
VISIBLE
NOPARALLEL
LOGGING 
GO
CREATE TRIGGER "OAC"."DEVICES_TRG" 
BEFORE INSERT ON DEVICES 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.D_ID IS NULL THEN
      SELECT DEVICES_SEQ2.NEXTVAL INTO :NEW.D_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."SERVERS_TRG" 
BEFORE INSERT ON SERVERS 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.S_ID IS NULL THEN
      SELECT SERVERS_SEQ1.NEXTVAL INTO :NEW.S_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."PANA_CERT_TRG" 
BEFORE INSERT ON PANA_CERT 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.PC_ID IS NULL THEN
      SELECT PANA_CERT_SEQ.NEXTVAL INTO :NEW.PC_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."SERVER_CERT_TRG" 
BEFORE INSERT ON SERVER_CERT 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.SC_ID IS NULL THEN
      SELECT SERVER_CERT_SEQ.NEXTVAL INTO :NEW.SC_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."USERS_TRG" 
BEFORE INSERT ON USERS 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.U_ID IS NULL THEN
      SELECT USERS_SEQ.NEXTVAL INTO :NEW.U_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."CERTIFICATIONS_TRG" 
BEFORE INSERT ON CERTIFICATIONS 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.C_ID IS NULL THEN
      SELECT CERTIFICATIONS_SEQ.NEXTVAL INTO :NEW.C_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."DEVICES_TRG2" 
BEFORE INSERT ON DEVICES 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    NULL;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."DEVICE_GROUP_TRG" 
BEFORE INSERT ON DEVICE_GROUP 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    NULL;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."DEVICE_GROUP_TRG1" 
BEFORE INSERT ON DEVICE_GROUP 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.DG_ID IS NULL THEN
      SELECT DEVICE_GROUP_SEQ.NEXTVAL INTO :NEW.DG_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO
CREATE TRIGGER "OAC"."LOG_TRG" 
BEFORE INSERT ON LOG 
FOR EACH ROW 
BEGIN
  <<COLUMN_SEQUENCES>>
  BEGIN
    IF INSERTING AND :NEW.LOG_ID IS NULL THEN
      SELECT LOG_SEQ.NEXTVAL INTO :NEW.LOG_ID FROM SYS.DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;
GO

