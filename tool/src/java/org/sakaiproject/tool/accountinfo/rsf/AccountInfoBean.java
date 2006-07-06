/*
 * Created on May 29, 2006
 */
package org.sakaiproject.tool.accountinfo.rsf;


import java.util.Date;
import java.util.Map;

//import org.sakaiproject.tool.tasklist.api.Task;
//import org.sakaiproject.tool.tasklist.api.TaskListManager;
//import org.sakaiproject.tool.tasklist.impl.TaskImpl;

public class AccountInfoBean {
  /** A holder for the single new task that may be in creation **/
  
  public String siteID;
  
  public Long[] deleteids;
  
  
  
  public String processActionAdd() {
	  return "OK";
  }
  
  public void processActionDelete() {

  }
}
