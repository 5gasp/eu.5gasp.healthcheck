package healthcheck.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author ctranoris
 *
 */
public class HealthcheckAPI {
	
	/** */
	private static final transient Log logger = LogFactory.getLog( HealthcheckAPI.class.getName());
	/** */
	private HCRepository hcRepository;


	@Context
	protected SecurityContext securityContext;
	

	/**
	 * @return the hcRepository
	 */
	public HCRepository getHcRepository() {
		return hcRepository;
	}

	/**
	 * @param hcRepository the hcRepository to set
	 */
	public void setHcRepository(HCRepository hcRepository) {
		this.hcRepository = hcRepository;
	}
	
	@GET
	@Path("/components/")
	@Produces("application/json")
	public Response getComponents() {

		if (securityContext != null) {
			if (securityContext.getUserPrincipal() != null)
				logger.info(" securityContext.getComponents()");

		}

		return Response.ok().entity( hcRepository.getComponents()).build();
	}
	

}
