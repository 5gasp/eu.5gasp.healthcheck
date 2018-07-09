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
import java.time.ZoneOffset;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * Describes a component that we perform a Healtcheck Status
 * @author ctranoris
 *
 */
public class Component {
	
	/** */
	private String name;
	/** */
	private String description;
	/** */
	private String apikey;
	/** */
	private ComponentStatus status;
	/** */
	private ComponentType type;
	/** */
	private HealthCheckMode mode;
	/** */
	private String locationName;
	/** */
	private String checkURL;
	/** */
	private String latitude;
	/** */
	private String longitude;
	/** */
	private Instant lastSeen;
	/** in seconds */
	private long failoverThreshold;
	/** */
	private String onIssueNotificationProduct;
	/** */
	private String onIssueNotificationComponent;
	/** */
	private Boolean issueRaised;
		
	
	public Component() {
		super();
		this.failoverThreshold = 60000;
		this.mode = HealthCheckMode.PASSIVE;
		this.status = ComponentStatus.FAIL;
		this.issueRaised = true;  //we start from true so not to send immediatelly an issue if seen error after service restart
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the apikey
	 */
	@JsonIgnore
	public String getApikey() {
		return apikey;
	}
	/**
	 * @param apikey the apikey to set
	 */
	public void setApikey(String apikey) {
		this.apikey = apikey;
	}
	/**
	 * @return the status
	 */
	public ComponentStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(ComponentStatus status) {
		this.status = status;
	}
	/**
	 * @return the type
	 */
	public ComponentType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(ComponentType type) {
		this.type = type;
	}
	/**
	 * @return the mode
	 */
	public HealthCheckMode getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(HealthCheckMode mode) {
		this.mode = mode;
	}
	/**
	 * @return the locationName
	 */
	public String getLocationName() {
		return locationName;
	}
	/**
	 * @param locationName the locationName to set
	 */
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}


	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the lastSeen
	 */
	public Instant getLastSeen() {
		return lastSeen;
	}
	/**
	 * @param lastSeen the lastSeen to set
	 */
	public void setLastSeen(Instant lastSeen) {
		this.lastSeen = lastSeen;	
		this.updateStatusSinceLastSeen();
		
	}
	
	/**
	 * @return the failoverThreshold
	 */
	public long getFailoverThreshold() {
		return failoverThreshold;
	}
	/**
	 * @param failoverThreshold the failoverThreshold to set
	 */
	public void setFailoverThreshold( long failoverThreshold) {
		this.failoverThreshold = failoverThreshold;
	}
	/**
	 * @return the checkURL
	 */
	public String getCheckURL() {
		return checkURL;
	}
	/**
	 * @param checkURL the checkURL to set
	 */
	public void setCheckURL(String checkURL) {
		this.checkURL = checkURL;
	}
	/**
	 * @return the onIssueNotificationProduct
	 */
	public String getOnIssueNotificationProduct() {
		return onIssueNotificationProduct;
	}
	/**
	 * @param onIssueNotificationProduct the onIssueNotificationProduct to set
	 */
	public void setOnIssueNotificationProduct(String onIssueNotificationProduct) {
		this.onIssueNotificationProduct = onIssueNotificationProduct;
	}
	/**
	 * @return the onIssueNotificationComponent
	 */
	public String getOnIssueNotificationComponent() {
		return onIssueNotificationComponent;
	}
	/**
	 * @param onIssueNotificationComponent the onIssueNotificationComponent to set
	 */
	public void setOnIssueNotificationComponent(String onIssueNotificationComponent) {
		this.onIssueNotificationComponent = onIssueNotificationComponent;
	}
	/**
	 * @return the issueRaised
	 */
	public Boolean getIssueRaised() {
		return issueRaised;
	}
	/**
	 * @param issueRaised the issueRaised to set
	 */
	public void setIssueRaised(Boolean issueRaised) {
		this.issueRaised = issueRaised;
	}
	
	
	
	/**
	 * @return seconds from Last Seen
	 */
	public long getSecondsDiffFromLastSeen() {
		if ( this.getLastSeen() != null ){
			Instant d1 = this.getLastSeen();
			Instant d2 = Instant.now();
			return (d2.getEpochSecond() -d1.getEpochSecond());
		} else {
			return Instant.MIN.getEpochSecond();
		}
		
	}
	

	public void updateStatusSinceLastSeen() {		
		
		if ( (lastSeen != null) && (this.getSecondsDiffFromLastSeen() <= this.failoverThreshold ) )
		{
			issueRaised = false; //clear the flag the we send an Issue about this status
			if (this.type.equals( ComponentType.PROCESS ) ) {
				this.setStatus(  ComponentStatus.PASS );
			} else {
				this.setStatus(  ComponentStatus.UP );			
			}
		} else {
			if (this.type.equals( ComponentType.PROCESS ) ) {
				this.setStatus(  ComponentStatus.FAIL );
			} else {
				this.setStatus(  ComponentStatus.DOWN );			
			}
			
		}
	}
	/**
	 * @return the lastSeenUTC
	 */
	public String getLastSeenUTC() {
		if ( this.getLastSeen() != null ){
			return this.getLastSeen().atOffset( ZoneOffset.UTC ).toString() + " UTC" ; 
		} else
			return "NEVER";
	}
	

}
