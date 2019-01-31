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

package healthcheck.util;

import java.util.concurrent.Future;

import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.model.ModelCamelContext;

import healthcheck.model.Component;


/**
 *  * 
 * @author ctranoris
 * 
 * 
 *
 */
public class BusController {

	/** */
	private static BusController instance;
	
	/** the Camel Context configure via Spring. See bean.xml*/	
	private static ModelCamelContext actx;



	/**
	 * @return
	 */
	public static synchronized BusController getInstance() {
		if (instance == null) {
			instance = new BusController();
		}
		return instance;
	}
	

	/**
	 * @return
	 */
	public static ModelCamelContext getActx() {
		return actx;
	}

	/**
	 * @param actx
	 */
	public static void setActx(ModelCamelContext actx) {
		BusController.actx = actx;
	}


	/**
	 * 
	 * utility function to stop ProducerTemplate
	 * @param result
	 * @param template
	 */
	private void waitAndStopForTemplate(Future<Exchange> result, FluentProducerTemplate template) {
		while (true) {			
			if (result.isDone()) {
				//logger.info( "waitAndStopForTemplate: " + template.toString() + " [STOPPED]");
				try {
					template.stop();
					template.clearAll();
					template.cleanUp();
					break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				//logger.info( "waitAndStopForTemplate: " + template.toString() + " [WAITING...]");
				Thread.sleep( 5000 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
	}
	
	/**
	 * @param component
	 */
	public void componentSeen(Component component) {
		FluentProducerTemplate template = actx.createFluentProducerTemplate().to("seda:componentseen?multipleConsumers=true");
		Future<Exchange> result = template.withBody( component ).asyncSend();
		waitAndStopForTemplate(result, template);
	}


	/**
	 * @param component
	 */
	public void componentStatusChanged(Component component) {
		FluentProducerTemplate template = actx.createFluentProducerTemplate().to("seda:componentchangedstatus?multipleConsumers=true");
		Future<Exchange> result = template.withBody( component ).asyncSend();
		waitAndStopForTemplate(result, template);
		
	}

	
}
