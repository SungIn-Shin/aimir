select * from devices
GO


select * from pana_cert 
GO

delete from pana_cert where d_id = (select d_id from devices where d_number='000B1200000100') 
GO


update pana_cert set discard_state='Y' where d_id = (select d_id from devices where d_number='000B1200000100')
���̺���� �ĳ��������� pana_cert
3pass ��������
certifications