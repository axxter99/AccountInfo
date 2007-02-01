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
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
//import org.sakaiproject.tool.tasklist.api.Task;
///import org.sakaiproject.tool.tasklist.api.TaskListManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.stringutil.StringList;

import org.sakaiproject.tool.accountinfo.rsf.UCTLDAPUser;
import org.sakaiproject.tool.cover.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import edu.amc.sakai.user.JLDAPDirectoryProvider;


public class AccountInfoProducer implements ViewComponentProducer,
    NavigationCaseReporter, DefaultView {
  public static final String VIEW_ID = "AccountInfo";
  private UserDirectoryService userDirectoryService;
  private ToolManager toolManager;
  private MessageLocator messageLocator;
  private LocaleGetter localegetter;
  
  private long CACHETTL = 300000;

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

  
  public void setToolManager(ToolManager toolManager) {
    this.toolManager = toolManager;
  }

  public void setLocaleGetter(LocaleGetter localegetter) {
    this.localegetter = localegetter;
  }

  public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) 
  {
	  
	  User user = userDirectoryService.getCurrentUser();
	  String username = user.getDisplayName();
		Session session = SessionManager.getCurrentSession();
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
			  UIOutput.make(tofill, "ldap-gracelogins-remaining", messageLocator.getMessage("grace_logins_label")+ " " + uctUser.getGraceLoginsTotal() + "/" + uctUser.getGraceLoginsRemaining());
		  } 
	  
	  }
	  
	  Date dob = uctUser.getDOB();
	  DateFormat monthday = new SimpleDateFormat("MMMMdd");
	  String dobStr = monthday.format(dob);
	  String todayStr = monthday.format(new Date());
	  
	  if (dob != null && dobStr.equals(todayStr)) {
		  m_log.info(" its this users BirthDay!");
	  }
    

  }

  public List reportNavigationCases() {
    List togo = new ArrayList(); // Always navigate back to this view.
    togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
    return togo;
  }

}
