/*
 * Created on May 29, 2006
 */
package org.sakaiproject.tool.accountinfo.rsf;

import java.text.DateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.api.ToolManager;
//import org.sakaiproject.tool.tasklist.api.Task;
///import org.sakaiproject.tool.tasklist.api.TaskListManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.org.ponder.errorutil.MessageLocator;
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
import uk.org.ponder.stringutil.LocaleGetter;
import uk.org.ponder.stringutil.StringList;

import org.sakaiproject.tool.accountinfo.rsf.UCTLDAPUser;
import edu.amc.sakai.user.JLDAPDirectoryProvider;

public class AccountInfoProducer implements ViewComponentProducer,
    NavigationCaseReporter, DefaultView {
  public static final String VIEW_ID = "AccountInfo";
  private UserDirectoryService userDirectoryService;
  private ToolManager toolManager;
  private MessageLocator messageLocator;
  private LocaleGetter localegetter;

  public String getViewID() {
	 System.out.println("GOT View " + VIEW_ID);
    return VIEW_ID;
  }

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
	  UCTLDAPUser uctUser = new UCTLDAPUser(user);
    
    
	  UIOutput.make(tofill, "current-username", username);
	  UIOutput.make(tofill, "ldap-gracelogins-remaining", uctUser.getGraceLoginsRemaining());
	  UIOutput.make(tofill, "ldap-gracelogins-total", uctUser.getGraceLoginsTotal());
	  Date passExp = uctUser.getAccountExpiry();
	  DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, localegetter.get());
	  UIOutput.make(tofill, "ldap-pass-expires", df.format(passExp));
	  if (uctUser.getAccountIsExpired()==true) {
		  UIOutput.make(tofill, "ldap-password-good", "your password has expired. You should update it emediatly");
	  }
	  
	  

    

  }

  public List reportNavigationCases() {
    List togo = new ArrayList(); // Always navigate back to this view.
    togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
    return togo;
  }

}
