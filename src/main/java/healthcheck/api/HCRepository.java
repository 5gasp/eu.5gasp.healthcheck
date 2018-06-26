package healthcheck.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import healthcheck.model.Component;


/**
 * @author ctranoris
 *
 */
public class HCRepository {

	/** */
	private static final transient Log logger = LogFactory.getLog( HCRepository.class.getName());

	private List< Component > components;
	/** */
	private Map< String, Component > componentsByName;
	
	public HCRepository(){
		components = new ArrayList<>();
		componentsByName = new HashMap<>();
		loadComponents();
	}
	

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


	public Object getComponents() {
		return components;
	}


	/**
	 * @return the componentsByName
	 */
	public Map<String, Component> getComponentsByName() {
		return componentsByName;
	}


	
	
	
}
