/*
 * Created on May 29, 2006
 */
package org.sakaiproject.tool.accountinfo.rsf;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;


public class AccountInfoProducer implements ViewComponentProducer,
    NavigationCaseReporter, DefaultView {
  public static final String VIEW_ID = "AccountInfo";
  private UserDirectoryService userDirectoryService;
  private MessageLocator messageLocator;
  private LocaleGetter localegetter;
  
  private long CACHETTL = 300000;
  
  
  private SessionManager sessionManager;
  public void setSessionManager(SessionManager su) {
	  sessionManager = su;
  }

  public String getViewID() {
	 System.out.println("GOT View " + VIEW_ID);
    return VIEW_ID;
  }

  private static Log m_log  = LogFactory.getLog(AccountInfoProducer.class);
  public void setMessageLocator(MessageLocator messageLocator) {
    this.messageLocator = messageLocator;
  }

  public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
    this.userDirectoryService = userDirectoryService;
  }

  

  public void setLocaleGetter(LocaleGetter localegetter) {
    this.localegetter = localegetter;
  }

 
  public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) 
  {
	  
	  User user = userDirectoryService.getCurrentUser();
	  String username = user.getDisplayName();
		Session session = sessionManager.getCurrentSession();
		UCTLDAPUser uctUser = null;
		if (session.getAttribute("ldapUser")==null) {	
		  uctUser = new UCTLDAPUser(user);
		  session.setAttribute("ldapUser",uctUser);
		} else {
			uctUser = (UCTLDAPUser) session.getAttribute("ldapUser");
			long cache = new Date().getTime();
			if (uctUser.getCacheTime().before(new Date(cache - CACHETTL))) {
				uctUser = new UCTLDAPUser(user);
				session.setAttribute("ldapUser",uctUser);
			}
		}
    
	  UIOutput.make(tofill, "current-username", username);
	  
	  
	  Date passExp = uctUser.getAccountExpiry();
	  if (passExp != null)
	  {
		  DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, localegetter.get());
		  UIOutput.make(tofill, "ldap-pass-expires", df.format(passExp));
		  if (uctUser.getAccountIsExpired()==true) {
			  UIOutput.make(tofill, "ldap-password-good", messageLocator.getMessage("passwd_exp_msg"));
			  Object[] rep = new Object[]{
					  (Object) uctUser.getGraceLoginsTotal(),
					  (Object) uctUser.getGraceLoginsRemaining()};
			  
			  UIOutput.make(tofill, "ldap-gracelogins-remaining", messageLocator.getMessage("grace_logins_label", rep));
		  } 
	  
	  }
	  
	  Date dob = uctUser.getDOB();
	  if (dob != null) {
		  DateFormat monthday = new SimpleDateFormat("MMMMdd");
		  String dobStr = monthday.format(dob);
		  String todayStr = monthday.format(new Date());
		  
		  if (dob != null && dobStr.equals(todayStr)) {
			  m_log.info("its this users Birthday!");
			  UIOutput.make(tofill, "bday");
		  }
	  }

	  UILink pLink = UILink.make(tofill, "password_link",messageLocator.getMessage("pwd_selfs_text"), messageLocator.getMessage("pwd_selfs_url"));
	  //pLink.decorators = new DecoratorList(new UITargetDecorator("_blank"));
	  
	  if (user.getType().equals("student") || user.getType().equals("staff")) 	
		  UIOutput.make(tofill, "seperator");
	  
	  if (user.getType().equals("student")) {
		  
		  UILink psLink = UILink.make(tofill, "ps_login", messageLocator.getMessage("ps_link_text"), messageLocator.getMessage("ps_student_link"));
		  
	  } else if (user.getType().equals("staff")) {
		  UILink.make(tofill,"ps_login", messageLocator.getMessage("ps_link_text"), messageLocator.getMessage("ps_staff_link"));
	  }
	  		
	  
  }

  public List reportNavigationCases() {
    List togo = new ArrayList(); // Always navigate back to this view.
    togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
    return togo;
  }

}
