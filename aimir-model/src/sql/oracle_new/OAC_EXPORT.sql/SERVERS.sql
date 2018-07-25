CREATE TABLE OAC.SERVERS  ( 
	S_ID       	NUMBER NOT NULL,
	SERVER_NAME	VARCHAR2(50) NOT NULL,
	IP         	VARCHAR2(50) NOT NULL,
	DOMAIN     	VARCHAR2(63) NOT NULL,
	CRT_DATE   	TIMESTAMP(6) NOT NULL,
	MDF_DATE   	TIMESTAMP(6) NULL,
	DEL_YN     	CHAR(1) DEFAULT 'N' NULL 
	)
GO
INSERT INTO OAC.SERVERS(S_ID, SERVER_NAME, IP, DOMAIN, CRT_DATE, MDF_DATE, DEL_YN)
  VALUES(61, 'AHOPE_TEST_SERVER', '172.11.11.1', 'www.ahope.co.kr', TO_TIMESTAMP('2018-05-29 10:23:46:0','YYYY-MM-DD HH24:MI:SS:FF'), NULL, 'N')
GO
INSERT INTO OAC.SERVERS(S_ID, SERVER_NAME, IP, DOMAIN, CRT_DATE, MDF_DATE, DEL_YN)
  VALUES(81, 'SERVER_RSA_TOMCAT', '172.16.10.139', 'FEP', TO_TIMESTAMP('2018-06-05 13:03:58:0','YYYY-MM-DD HH24:MI:SS:FF'), NULL, 'N')
GO
