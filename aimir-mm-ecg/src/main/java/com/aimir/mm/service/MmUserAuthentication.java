package com.aimir.mm.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.EncryptionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aimir.dao.system.OperatorDao;
import com.aimir.mm.model.RequestAuthToken;
import com.aimir.model.system.User;

@Service
@Transactional(value = "transactionManager")
public class MmUserAuthentication {

    private static Log log = LogFactory.getLog(MmService.class);
    
    @Autowired
    OperatorDao operatorDao;
	
	public void userAuthentication(RequestAuthToken requestAuthToken) throws Exception{
		
		log.info("Mobile Money Interface Authentication..");
		
    	if(requestAuthToken == null){
    		throw new Exception("AuthCred is empty");
    	}

    	if(requestAuthToken.getUserName() == null ||  "".equals(requestAuthToken.getUserName())){
    		throw new Exception("UserName is empty");
    	}
    	
		log.info("User Name = " + requestAuthToken.getUserName());

		if (requestAuthToken.getPassword() == null || "".equals(requestAuthToken.getPassword())) {
			throw new Exception("Password is empty");
		} 
    	
		log.info("Password = " + requestAuthToken.getPassword());

		verifyAccount(requestAuthToken);
		log.info("Mobile MOney Interface Authentication End..");
	}
	
	
	public boolean verifyAccount(RequestAuthToken requestAuthToken) throws Exception {
		User loginUser = null;
		
		try {
			loginUser = operatorDao.getOperatorByLoginId(requestAuthToken.getUserName());
		} catch (Exception e) {
			throw new Exception("Internal Service Error = " + e.getMessage());
		}
		
		if (loginUser == null) {
			throw new Exception("UserName is invalid");
		}
		
		if (loginUser.getPassword().equals(requestAuthToken.getPassword())) {
			return true;
		} else
			try {
				if (loginUser.getPassword().equals(hashPassword(requestAuthToken.getPassword(), requestAuthToken.getUserName()))) {
					return true;
				} else {
					throw new Exception("Password is invalid");
				}
			} catch (EncryptionException e) {
				throw new Exception("Password Encription Failed");
			}
	}

	public String hashPassword(String password, String accountName) throws EncryptionException {
		String salt = accountName.toLowerCase();
		return ESAPI.encryptor().hash(password, salt);
	}

}
