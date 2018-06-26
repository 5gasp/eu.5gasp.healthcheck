package healthcheck.model;

import java.util.Date;

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
	private Date lastSeen;
	/** in seconds */
	private long failoverThreshold;
	
		
	
	public Component() {
		super();
		this.failoverThreshold = 60000;
		this.mode = HealthCheckMode.PASSIVE;
		this.status = ComponentStatus.FAIL;
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
	public Date getLastSeen() {
		return lastSeen;
	}
	/**
	 * @param lastSeen the lastSeen to set
	 */
	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
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
	
	
	
	

}
