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
import java.math.BigDecimal;
import java.security.*;

import com.novell.groupwise.ws.Authentication;
import com.novell.groupwise.ws.PlainText;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPAttribute;
import com.novell.groupwise.ws.*;
import javax.xml.rpc.Stub;
import com.sun.xml.rpc.client.BasicService;

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
import com.sun.mail.imap.*;
import java.text.SimpleDateFormat;
import com.maintainet.gwsoap.EasySoap;

//import javax.rmi.CORBA.Stub;
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
	private String gwPostOffice;
	/*
	 *  Values for the user
	 */
	private String graceLoginsRemaining;
	private String graceLoginsTotal;
	private Date accountExpiry;
	private int unReadEmail;
	private boolean accountIsExpired = false;
	private Integer newMailMessages;
	
	
	
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
				PASSWORDEXPIRATIONTIME,
				"nGWPostOffice"
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
			//DOB = sp.getDOB();
		DOB = new Date();

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
			
			//find the users postoffice: nGWPostOffice: cn=STAFFP1,ou=Gware,ou=gw,ou=services,o=uct
			String po = thisLdap.getAttribute("nGWPostOffice").getStringValue();
			//we need to parse this
			po = po.substring(3, po.indexOf(','));
			m_log.info("got po" + po);
			
			} else {
				m_log.warn("not found in LDAP: " + user.getDisplayId());
			}
			//close the ldap connection
			conn.disconnect();

			/*
			 * Get LDAP details
			 * 
			*/
			

			
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		String PO= "staffp1";
		try {
		EasySoap es = new EasySoap(PO + ".uct.ac.za","7191","",user.getEid(),"connect5");
		m_log.info("logged in as " + es.getUser());
		es.logout();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//this.getUserNewMail(user.getEid(), PO);
		
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
	
	
	public Integer getNewMailMessages(){
		return newMailMessages;
	}
	
	public void setNewMailMessages(Integer newVal){
		newMailMessages = newVal;
	}
	
	
	public Date getDOB() {
		return this.DOB;
	}
	
	//internal methods adopted from the Jldap porvidor
	//get a specific entry from the directory 
	private LDAPEntry getEntryFromDirectory(String dn, LDAPConnection conn)
		throws LDAPException
	{
		LDAPEntry nextEntry = null;
		m_log.debug("About to get entry for " + dn);
		nextEntry = conn.read(dn);
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
	
	private Integer getUserNewMail(String userId, String PO) {
		//First setup access to GroupWise server

		Stub clientStub = (Stub)new GroupwiseService_Impl().getGroupwiseSOAPPort();

		// Default port is 7191

		 clientStub._setProperty( javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + PO +"uct.ac.za:7191/soap");
		 GroupWisePortType gwService = (GroupWisePortType)clientStub;

		  // Now setup the login credentials

		  PlainText ptLogin = new PlainText();
		  
		  ptLogin.setUsername(userId);

		  ptLogin.setPassword("connect5");

		  // Make the call to the loginRequest
		  try {
			  LoginResponse loginRes = gwService.loginRequest(( Authentication)ptLogin, 
					  "us", 
					  new BigDecimal(1.0), 
					  "Our GW Client", 
					  false );

			  if ( loginRes.getStatus().getCode() == 0 ) {

				  // Within here we can pull out the various response values like the following

				  //loginRes.getUserInfo().getName();

			  }
		  }
		  catch (Exception e ) {
			  e.printStackTrace();
		  }

		  return null;
	}

}