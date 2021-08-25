/**
 * Copyright 2017 University of Patras 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package healthcheck.routes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import healthcheck.bugzilla.model.Bug;
import healthcheck.bugzilla.model.Comment;
import healthcheck.model.Component;
public class BugzillaClient {

	private static final transient Log logger = LogFactory.getLog(BugzillaClient.class.getName());

	/** */
	private static BugzillaClient instance;

	/** */
	private static final String BUGHEADER =   "*************************************************\n"
											+ "THIS IS AN AUTOMATED ISSUE CREATED BY PORTAL API.\n"
											+ "*************************************************\n";


	public static BugzillaClient getInstance() {
		if (instance == null) {
			instance = new BugzillaClient();
			
		}
		return instance;
	}
	
	
		
	/**
	 * @param c
	 * @return
	 */
	public static Bug transformComponent2BugBody(Component c) {

		logger.info( "BugzillaClient.transformComponent2BugBody Component: " + c.getName() );
		String product = c.getOnIssueNotificationProduct();
		String component = c.getOnIssueNotificationComponent() ;
		String summary = "[HEALTHCHECK] Component " + c.getName() + ", Status: " + c.getStatus();

		String description = BUGHEADER
				+ "\nComponent: " + c.getName()
				+ "\nStatus: " + c.getStatus()
				+ "\nLastSeen: " + c.getLastSeen() ;
		
		String status= "CONFIRMED";
		String resolution = null;
		
		Bug b = createBug(product, component, summary, null, description, null, status, resolution);
		
		return b;
	}
	
	
	
	/**
	 * @param product
	 * @param component
	 * @param summary
	 * @param alias
	 * @param description
	 * @param ccemail
	 * @return
	 */
	public static Bug createBug(String product, String component, String summary, String alias, String description, String ccemail, String status, String resolution ) {
		
		Bug b = new Bug();
		b.setProduct(product);
		b.setComponent(component);
		b.setSummary(summary);
		b.setVersion( "unspecified" );
		if ( alias != null )
		{
			List<Object> aliaslist = new ArrayList<>();
			aliaslist.add(alias);		
			b.setAlias( aliaslist );
		}
		if ( ccemail != null ) 
		{
			List<String> cclist = new ArrayList<>();
			cclist.add( ccemail );		
			b.setCc(cclist);
		}
		b.setDescription(description.toString());		
		b.setStatus(status);
		b.setResolution(resolution);
				
		return b;
	}
	


	public static Comment createComment( String description ) {
		
		Comment c = new Comment();
		c.setComment(description);
		c.setIs_markdown( false );
		c.setIs_private( false );	
		return c;
	}
	
	

}
