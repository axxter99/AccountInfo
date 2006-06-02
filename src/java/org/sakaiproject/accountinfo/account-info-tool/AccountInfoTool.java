package edu.sakiproject.AccountInfo;

import java.util.Date;


import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPAttribute;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.entity.api.EntityProducer;

public class AccountInfoTool
{
	private int graceLoginsRemaining;
	private int graceLoginsTotal;
	private Date accountExiry;
	private int unReadEmail;
	private boolean accountIsExpired = false;
	
	
	private String ldapHost = "rep1.uct.ac.za"; //address of ldap server
	private int ldapPort = 389; //port to connect to ldap server on
	private String keystoreLocation = "/usr/local/sakai"; // keystore location (only needed for SSL connections)
	private String keystorePassword = "changeit"; // keystore password (only needed for SSL connections)
	private String basePath = ""; //base path to start lookups on
	private boolean secureConnection = true; //whether or not we are using SSL
	private int operationTimeout = 5000; //default timeout for operations (in ms)

	
	/*
	 *  Get the number of grae logins remaining
	 */
	
	public int setGraceloginsRemaining() {
		//get the grace logins from ldap
		
		Session session = SessionManager.getCurrentSession();
		String userId = session.getUserEid();
		
		//string array of attribs to get from the directory
		String[] attrList = new String[] {	
				"distinguishedName",
				"objectClass",
				"aliasedObjectName",
				"graceLogins",
				"graceloginremaining",
				"accountexpirydate"
			
		};
		return 6;
	} //end set date 
	
	//internal methods adopted from the Jldap porvidor
	//search the directory to get an entry
	private LDAPEntry getEntryFromDirectory(String searchFilter, String[] attribs, LDAPConnection conn)
		throws LDAPException
	{
		LDAPEntry nextEntry = null;
		LDAPSearchConstraints cons = new LDAPSearchConstraints();
		cons.setDereference(LDAPSearchConstraints.DEREF_NEVER);		
		cons.setTimeLimit(operationTimeout);
		
		LDAPSearchResults searchResults =
			conn.search(getBasePath(),
					LDAPConnection.SCOPE_SUB,
					searchFilter,
					attribs,
			        false,
					cons);
		
		if(searchResults.hasMore()){
            nextEntry = searchResults.next();            
		 }
		return nextEntry;
	}
	/**
	 * @return Returns the basePath.
	 */
	public String getBasePath() {
		return "o=uct";
	}
	
} //end class