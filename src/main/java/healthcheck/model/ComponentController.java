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

package healthcheck.model;

import java.time.Instant;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import healthcheck.util.BusController;

/**
 * @author ctranoris
 *
 */
public class ComponentController {

	@Autowired
	HCRepository hcRepository;

	/** */
	private static final transient Log logger = LogFactory.getLog(ComponentController.class.getName());

	/**
	 * A component is seen
	 * @param c
	 * @return
	 */
	public Component componentSeen(Component c) {

		logger.info( "TRIGGERED ComponentController.componentSeen " + c.getName());
		Component comp = hcRepository.getComponentsByName().get(c.getName());
		comp.setLastSeen( Instant.now() );

		return comp;

	}

	/**
	 * check all components against LastSeen
	 */
	public void refreshComponentsStatus() {

		logger.info( "TRIGGERED ComponentController.refreshComponentsStatus");
		for (Component c : hcRepository.getComponents()) {
			ComponentStatus prevStatus = c.getStatus();
			c.updateStatusSinceLastSeen();

			if (!prevStatus.equals(c.getStatus())) {
				BusController.getInstance().componentStatusChanged(c);
			}			

			logger.info( "Checked component " + c.getName() + ". NEW Status = " + c.getStatus() + " [PREVIOUS = " + prevStatus + "]" );

		}
	}
	
	
	/**
	 * change the issue raised to TRUE
	 * @param c
	 * @return
	 */
	public Component issueRaised(Component c) {

		logger.info( "TRIGGERED ComponentController.issueRaised " + c.getName());
		Component comp = hcRepository.getComponentsByName().get(c.getName());
		comp.setIssueRaised( true );
		return comp;

	}
}
