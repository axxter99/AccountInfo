package org.sakaiproject.tool.accountinfo.rsf;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPAttribute;

import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.User; 
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;

import java.util.Properties;
import javax.mail.*;
import com.sun.mail.imap.*;

/*
 * a method to get a decorated user for LDAP.
 * 
 */
public class UCTLDAPUser  {

	private String ldapHost = "rep1.uct.ac.za edir1.uct.ac.za"; //address of ldap server
	private int ldapPort = 389; //port to connect to ldap server on
	private String keystoreLocation = "/usr/local/sakai"; // keystore location (only needed for SSL connections)
	private String keystorePassword = "changeit"; // keystore password (only needed for SSL connections)
	private String basePath = ""; //base path to start lookups on
	private boolean secureConnection = true; //whether or not we are using SSL
	private int operationTimeout = 5000; //default timeout for operations (in ms)
	
	
	/*
	 *  Values for the user
	 */
	private String graceLoginsRemaining;
	private String graceLoginsTotal;
	private Date accountExpiry;
	private int unReadEmail;
	private boolean accountIsExpired = false;
	private int newMailMessages;
	
	
	
	private static final String GRACELOGINSREMAINING = "loginGraceRemaining";
	private static final String GRACELOGINSTOTAL = "loginGraceLimit";
	private static final String PASSWORDEXPIRATIONTIME = "passwordExpirationTime";
	
	private static final String IMAP_HOST = "mail.uct.ac.za";
	
	public UCTLDAPUser(User user)
	{
		//string array of attribs to get from the directory
		String[] attrList = new String[] {	
				"distinguishedName",
				"objectClass",
				"aliasedObjectName",
				GRACELOGINSTOTAL,
				GRACELOGINSREMAINING,
				PASSWORDEXPIRATIONTIME
		};
		Date myDate = null;
		
		//create new ldap connection
		LDAPConnection conn = new LDAPConnection();	
		LDAPConstraints cons = new LDAPConstraints();
		
		cons.setTimeLimit(operationTimeout);
		
		conn.setConstraints(cons);
		String searchFilter = "cn=" + user.getEid();
		//connect to ldap server
		try {
			conn.connect( ldapHost, ldapPort );
			System.out.println("Searching for " + searchFilter);
			LDAPEntry thisLdap = getEntryFromDirectory(searchFilter,attrList,conn);
			if (thisLdap != null) {
			
			LDAPAttribute glAtr = thisLdap.getAttribute(GRACELOGINSREMAINING);
			String glr = "0";
			if (glAtr != null) {
				glr = glAtr.getStringValue();
			} else {
				glr = "6";
			}
			setGraceLoginsRemaining(glr);

			LDAPAttribute gltAtr = thisLdap.getAttribute(GRACELOGINSTOTAL);
			String glt = "0";
			if (gltAtr != null) {
				glt = glAtr.getStringValue();
			} else {
				glt = "6";
			}
			setGraceLoginsTotal(glt);
			
			
			LDAPAttribute atrDate = thisLdap.getAttribute(PASSWORDEXPIRATIONTIME);
			String strDate = null;
			if (atrDate != null ) {
				strDate = atrDate.getStringValue();
			} else {
				strDate ="21060816070250Z";
			}
			
			/*
			String yr = strDate.substring(0,4);
			String mt = strDate.substring(5,7);
			String dy = strDate.substring(8,10);
			System.out.println("Date is " + yr + " " + mt + " " + dy);
			*/
			//setAccountExpiry(new Date(yr,mt,dy));
			// novell format is 20060816070250Z
			String strFormat = "yyyyMMddkm"; //kmSz";
			strDate = strDate.substring(0,12);
			DateFormat myDateFormat = new SimpleDateFormat(strFormat);
			
			
			try {
			     myDate = myDateFormat.parse(strDate);
			} catch (Exception e) {
			     System.out.println("Invalid Date Parser Exception");
			     e.printStackTrace();
			}
			//System.out.println("Finished Date Function " + myDate.getDay());
			setAccountExpiry(myDate);

			} else {
				System.out.println("ERROR: not found in LDAP");
			}
			
			
			if (myDate.before(new Date())) {
				System.out.println("Account has expired!");
				setAccountIsExpired(true);
			}
			/*
			 * Get LDAP details
			 * 
			*/
			
//			 Get a Properties object
			if (TRY_LDAP) {
				Properties props = System.getProperties();
				// Get a Properties object
				props.put("mail.imaps.host",IMAP_HOST);
				props.put("mail.imaps.port", "993");
				
				//Authenticator auth=new myauth(name,passwd);
				// Get a Session object
				javax.mail.Session mailSession = javax.mail.Session.getInstance(props, null);
				mailSession.setDebug(true);
	
				// Get a Store object
				Store store = null;
				
				
				
				store = mailSession.getStore("imaps");
				//user.getEid()
				String pass = (String)SessionManager.getCurrentSession().getAttribute("netPasswd");
				store.connect(IMAP_HOST, "dhorwitz_its_main_uct", pass);
				Folder folder = store.getDefaultFolder();
				int newMessages = folder.getNewMessageCount();
				System.out.println("Got some new messages!" + newMessages);
				setNewMailMessages(newMessages);
				folder.close(true);
				
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		 
		
		
	
	
	} //end constructor
	
	
	/*
	 *  Getters and setters
	 */
	public void setGraceLoginsRemaining(String newVal) {
		graceLoginsRemaining = newVal;
		
	}
	
	public String getGraceLoginsRemaining() {
		return graceLoginsRemaining;
	}
	
	public String getGraceLoginsTotal() {
		return graceLoginsTotal;
		
	}
	
	public void setGraceLoginsTotal(String newVal) {
		
		graceLoginsTotal = newVal;
	}
	
	
	public void setAccountExpiry(Date newDate) {
		accountExpiry = newDate;
	}
	
	public Date getAccountExpiry() {
		return accountExpiry;
	}
	
	public void setAccountIsExpired(boolean newVal) {
		accountIsExpired = newVal;
		
	}
	
	public boolean getAccountIsExpired() {
		return accountIsExpired;
	}
	
	
	public int getNewMailMessages(){
		return newMailMessages;
	}
	
	public void setNewMailMessages(int newVal){
		newMailMessages = newVal;
	}
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
		int i = 0;
		if(searchResults.hasMore()){
			i++;
            nextEntry = searchResults.next();            
		 }
		System.out.println("found " + i + "results");
		return nextEntry;
	}
	/**
	 * @return Returns the basePath.
	 */
	public String getBasePath() {
		return "o=uct";
	}
	
	
}
