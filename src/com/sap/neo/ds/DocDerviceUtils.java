package com.sap.neo.ds;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.sap.ecm.api.EcmService;
import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;

public class DocDerviceUtils {

	public Session getSession(String uniqueName, String secretKey) {
//		String uniqueName = "GoSign";
//		String secretKey = "0a11edd0-fd08-435a-a9bd-b609255ef4e7";
		Session openCmisSession = null;
		EcmService ecmSvc = null;
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			String lookupName = "java:comp/env/" + "EcmService";
			ecmSvc = (EcmService) ctx.lookup(lookupName);

		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		try {
			// connect to my repository
			openCmisSession = ecmSvc.connect(uniqueName, secretKey);
		} catch (CmisObjectNotFoundException e) {
			// repository does not exist, so try to create it
			RepositoryOptions options = new RepositoryOptions();
			options.setUniqueName(uniqueName);
			options.setRepositoryKey(secretKey);
			options.setVisibility(Visibility.PROTECTED);
			ecmSvc.createRepository(options);
			// should be created now, so connect to it
			openCmisSession = ecmSvc.connect(uniqueName, secretKey);
		}
		return openCmisSession;

	}
	
	
	
}
