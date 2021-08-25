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
	private Map< String, Component > componentsByAPIKEY;
	/** */
	Properties properties;
	
	/** */
	public HCRepository(){
		components = new ArrayList<>();
		componentsByName = new HashMap<>();
		componentsByAPIKEY = new HashMap<>();
		loadComponents();
		loadProperties();
	}
	

	/**
	 * 
	 */
	private void loadComponents() {
		
	    try {
	    	String filename = "etc/components.json";
	    	
			final ObjectMapper mapper = new ObjectMapper();
			File f = new File( filename );
			if ( f.exists() ){
				components = mapper.readValue( new File( filename ),  new TypeReference<List<Component>>(){} );	
				logger.info( "Loaded Components JSON file from: " + f.getAbsolutePath() );			
			} else{			
				mapper.writeValue(  new File( filename ) , components);
				logger.info( "Components JSON file created at: " + f.getAbsolutePath() );
			}
			
			for (Component component : this.components) {
				logger.info( "Loaded component JSON file from: " + component.getName() );
				component.maskAPIKey();
				componentsByName.put(component.getName(), component);	
				componentsByAPIKEY.put(component.getApikeySecret()  , component);
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
	    	
		File f = new File("etc/healthcheck.properties");
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


	public Map<String, Component>  getComponentsByAPIKEY() {
		return componentsByAPIKEY;
	}

	
	
	
}
