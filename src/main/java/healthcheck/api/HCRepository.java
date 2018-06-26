package healthcheck.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import healthcheck.model.Component;


/**
 * @author ctranoris
 *
 */
public class HCRepository {

	/** */
	private static final transient Log logger = LogFactory.getLog( HCRepository.class.getName());
	
	private List< Component > components;
	
	public HCRepository(){
		components = new ArrayList<>();
	}
	

	public Object getComponents() {
		return components;
	}
	
	
}
