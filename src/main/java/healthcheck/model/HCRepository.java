package healthcheck.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author ctranoris
 *
 */
public class HCRepository {

	/** */
	private static final transient Log logger = LogFactory.getLog( HCRepository.class.getName());

	/** */
	private List< Component > components;
	/** */
	private Map< String, Component > componentsByName;
	/** */
	Properties properties;
	
	/** */
	public HCRepository(){
		components = new ArrayList<>();
		componentsByName = new HashMap<>();
		loadComponents();
		loadProperties();
	}
	

	/**
	 * 
	 */
	private void loadComponents() {
		
	    try {
	    	
			final ObjectMapper mapper = new ObjectMapper();
			File f = new File("components.json");
			if ( f.exists() ){
				components = mapper.readValue( new File("components.json"),  new TypeReference<List<Component>>(){} );	
				logger.info( "Loaded Components JSON file from: " + f.getAbsolutePath() );			
			} else{			
				mapper.writeValue(  new File("components.json") , components);
				logger.info( "Components JSON file created at: " + f.getAbsolutePath() );
			}
			
			for (Component component : this.components) {
				logger.info( "Loaded component JSON file from: " + component.getName() );	
				componentsByName.put(component.getName(), component);
			}
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 
	 */
	private void loadProperties() {

		properties = new Properties();
	    	
		File f = new File("healthcheck.properties");
		if ( f.exists() ){
			
			logger.info( "Loaded healthcheck.properties file from: " + f.getAbsolutePath() );			
		} else{			

			logger.info( "healthcheck.properties file SHOULD BE at: " + f.getAbsolutePath() );
		}
		
		
		try {
			FileInputStream input;
			input = new FileInputStream( f.getAbsolutePath() );
			// load a properties file
			properties.load(input);
			properties.getProperty("BUGZILLAKEY", "");
			properties.getProperty("BUGZILLAURL", "portal.5ginfire.eu:443/bugstaging");
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}


	/**
	 * @return
	 */
	public List<Component> getComponents() {
		return components;
	}


	/**
	 * @return the componentsByName
	 */
	public Map<String, Component> getComponentsByName() {
		return componentsByName;
	}


	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	
	
	
}
