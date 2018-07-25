drop table LP_EM_TR  CASCADE CONSTRAINTS;

CREATE TABLE LP_EM_TR
(
   METERNO varchar2(20),
   LPTIME varchar2(12),
   DST number(38),
   CH number(10),
   VALUE number(19,4)
);

create or replace TRIGGER LP_TRIGGER 
AFTER INSERT
ON LP_EM 
REFERENCING NEW AS NEW
FOR EACH ROW 
begin
   if :NEW.hh = '00' then
       insert into LP_EM_TR (METERNO, LPTIME, DST, CH, VALUE)
       values (:NEW.mdev_id, :NEW.yyyymmddhh||'00', :NEW.dst, :NEW.channel, :NEW.value);
   end if;
end;