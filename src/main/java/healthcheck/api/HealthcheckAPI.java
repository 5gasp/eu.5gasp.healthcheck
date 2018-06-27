package healthcheck.api;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import healthcheck.model.Component;
import healthcheck.model.HCPassiveMessage;
import healthcheck.model.HCRepository;
import healthcheck.util.BusController;


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
	
	@GET
	@Path("/admin/components/{componentname}/{apikey}")
	@Produces("application/json")
	@Consumes("application/json")
	public Response setHCStatus( @PathParam("componentname") String componentname , @PathParam("apikey") String apikey) {

		HCPassiveMessage msg = new HCPassiveMessage();
		msg.setComponentName( componentname );
		msg.setApiKey(apikey);
		
		logger.info("Received POST for HCPassiveMessage: " + msg.getComponentName() );
		logger.info("Received POST for HCPassiveMessage keu: " + msg.getApiKey() );
		
		
		if ( (msg.getApiKey() == null) ||  (msg.getComponentName() == null) || (msg.getComponentName().equals("") )) {
			ResponseBuilder builder = Response.status(Status.BAD_REQUEST);
			builder.entity(" HCPassiveMessage cannot be registered");
			logger.info("HCPassiveMessage for " + msg.getComponentName() + " cannot be registered BAD_REQUEST.");
			throw new WebApplicationException(builder.build());
		}
		
		Component component = this.hcRepository.getComponentsByName().get( msg.getComponentName() );

		if ( ( component != null  ) && ( component.getApikey().equals( msg.getApiKey() ) )){
			BusController.getInstance().componentSeen( component );
			return Response.ok( msg ).build();
		}else{			
			return Response.status(Status.BAD_REQUEST).entity("Component not found or API KEY is wrong").build();
		}

	
	}
	

}
