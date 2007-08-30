/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The University of Cape Town.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.accountinfo.rsf;

import java.util.Date;
import java.text.DateFormat;
import java.security.*;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.User; 
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.component.cover.ServerConfigurationService;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.manager.Persistable;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.component.cover.ComponentManager;

import java.util.Properties;
import javax.mail.*;
import com.sun.mail.imap.*;
import java.text.SimpleDateFormat;
/*
 * a method to get a decorated user for LDAP.
 * 
 */
public class UCTLDAPUser  {

	private String ldapHost = ServerConfigurationService.getString("accountInfo.ldapServers"); //address of ldap server
	private int ldapPort = 389; //port to connect to ldap server on
	private String keystoreLocation = "/usr/local/sakai"; // keystore location (only needed for SSL connections)
	private String keystorePassword = "changeit"; // keystore password (only needed for SSL connections)
	private String basePath = ""; //base path to start lookups on
	private boolean secureConnection = true; //whether or not we are using SSL
	private int operationTimeout = 5000; //default timeout for operations (in ms)
	private Date cacheTime;
	private Date DOB;
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
	private static final boolean TRY_LDAP = false;
	

	private static Log m_log  = LogFactory.getLog(UCTLDAPUser.class);
	
	private SakaiPersonManager sakaiPersonManager;
	public void setSakaiPersonManager(SakaiPersonManager s){
		sakaiPersonManager = s;
	}
	
	private SakaiPersonManager getSakaiPersonManager() {
		if (sakaiPersonManager == null ) {
			sakaiPersonManager = (SakaiPersonManager) ComponentManager.get(SakaiPersonManager.class.getName());
		}
		return this.sakaiPersonManager;
	}
	
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
		//lets get the system Profile
		sakaiPersonManager = getSakaiPersonManager();
		SakaiPerson sp = sakaiPersonManager.getSakaiPerson(user.getId(), sakaiPersonManager.getSystemMutableType());
		if (sp !=null)
			DOB = sp.getDateOfBirth();
		

		//connect to ldap server
		try {
			conn.connect( ldapHost, ldapPort );
			//System.out.println("Searching for " + searchFilter);
			
			String dn = (String)SessionManager.getCurrentSession().getAttribute("netDn");
			LDAPEntry thisLdap = null;
			if (dn != null )
			{
				thisLdap = getEntryFromDirectory(dn,conn);
			} else {
				//get the user eid
				
				String eid = user.getEid();
				thisLdap = this.getEntryFromDirectory("cn="+ eid,attrList,conn);
				
			}
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
				     m_log.error("Invalid Date Parser Exception");
				     e.printStackTrace();
				}
				//System.out.println("Finished Date Function " + myDate.getDay());
				setAccountExpiry(myDate);
				if (myDate.before(new Date())) {
					m_log.debug("Account has expired! " + user.getEid());
					setAccountIsExpired(true);
				}
			} else {
				//strDate ="21060816070250Z";
			}
			


			} else {
				m_log.warn("not found in LDAP: " + user.getDisplayId());
			}
			//close the ldap connection
			conn.disconnect();

			/*
			 * Get LDAP details
			 * 
			*/
			
//			 Get a Properties object
			if (TRY_LDAP) {
				
				  Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider());
				  String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
			      
			      Properties props = new Properties();
			 
			      props.setProperty("mail.store.protocol", "imap");
			 
			      props.setProperty("mail.imap.host", IMAP_HOST);
			      props.setProperty("mail.imap.port", "993");
			 
			      props.setProperty( "mail.imap.socketFactory.class", SSL_FACTORY);
			 
			      props.setProperty( "mail.imap.socketFactory.fallback", "false");
			 
			      props.setProperty( "mail.imap.socketFactory.port", "993");
			        
			      java.security.Security.setProperty( "ssl.SocketFactory.provider", SSL_FACTORY);
			      javax.mail.Session s = javax.mail.Session.getDefaultInstance(props, null);
			      s.setDebug(true);
			      
			      Store store = s.getStore("imap");
			      
			      try
			      {
			    	  String pass = (String)SessionManager.getCurrentSession().getAttribute("netPasswd");
			    	  m_log.info(IMAP_HOST + " " + "dhorwitz_its_main_uct " + pass);
			    	  store.connect(IMAP_HOST, 993,"dhorwitz_its_main_uct", pass);
			          Folder folder = store.getDefaultFolder();
			          int newMessages = folder.getNewMessageCount();
			          m_log.info("Got some new messages!" + newMessages);
			          setNewMailMessages(newMessages);
			          folder.close(true);
			      }
			      catch (AuthenticationFailedException afe)
			      {
			          // no valid authentication
			    	  m_log.warn("Auth failed for " + user.getEid() + " on mail" + afe);
			    	  afe.printStackTrace();
			      }
			      catch (Exception ge)
			      
			      {
			    	  ge.printStackTrace();
			      }
			}
			
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		 
		
		this.cacheTime=new Date();	
	
	
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
	//get a specific entry from the directory 
	private LDAPEntry getEntryFromDirectory(String dn, LDAPConnection conn)
		throws LDAPException
	{
		LDAPEntry nextEntry = null;
		m_log.debug("About to get entry for " + dn);
		nextEntry = conn.read(dn);
		//System.out.println("found " + i + "results");
		return nextEntry;
	}
	
	
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
	
	public Date getCacheTime() {
		return this.cacheTime;
	}
	
	/**
	 * @return Returns the basePath.
	 */
	public String getBasePath() {
		return "o=uct";
	}
	
	public Date getDOB() {
		return this.DOB;
	}
	
}