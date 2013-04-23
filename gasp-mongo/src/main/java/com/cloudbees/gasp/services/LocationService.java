package com.cloudbees.gasp.services;

import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.gasp.model.GaspLocation;
import com.cloudbees.gasp.model.Location;
import com.cloudbees.gasp.model.LocationQuery;
import com.cloudbees.gasp.model.MongoConnection;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.gson.Gson;

@Path("/locations")
public class LocationService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(LocationService.class);
	private MongoConnection mongoConnection = new MongoConnection();

	private GeocodeResponse geocodeService( String queryAddress ) {
		final Geocoder geocoder = new Geocoder();
		GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
												.setAddress(queryAddress)
												.setLanguage("en")
												.getGeocoderRequest();
		GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
		
		if  ( (geocoderResponse.getStatus() == GeocoderStatus.OK)
			  && (geocoderResponse.getResults().size() == 1) ) {
			logger.info("Match found for address: " + queryAddress);
			logger.info("formattedAddress = " + geocoderResponse
												.getResults().get(0)
												.getFormattedAddress());
			logger.info("Location = " + geocoderResponse
										.getResults().get(0)
										.getGeometry()
										.getLocation());
							
			if (logger.isDebugEnabled()) {
				Iterator<GeocoderAddressComponent> iterator = geocoderResponse
																.getResults()
																.get(0)
																.getAddressComponents()
																.iterator();
				while (iterator.hasNext()) {
					GeocoderAddressComponent addressComponent = iterator.next();
					logger.debug("Address Component ["
								 + addressComponent.getTypes().get(0)
								 + "] = "
								 + addressComponent.getShortName() );
					}
			}
		}
		
		return geocoderResponse;
	}
	
	@POST
    @Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addLocation( LocationQuery location ) {
		try {
			logger.debug("Name = " + location.getName());
			logger.debug("AddressString = " + location.getAddressString());
			
			GeocodeResponse geocoderResponse = geocodeService(location.getAddressString());

			switch(geocoderResponse.getStatus()) {
				
				case ZERO_RESULTS:	// No match found: return 204 No Content
									logger.info("No matches found for address: " + location.getAddressString());
							   	    return Response.status(Response.Status.NO_CONTENT).build();
			
				case OK: 	// More than one match found: return 204 No Content
							if (geocoderResponse.getResults().size() > 1) {
								logger.info("Ambiguous: more than one match for address: " 
											+ location.getAddressString());
								return Response.status(Response.Status.NO_CONTENT).build();				
							}

							// Create a Location object from GeocoderResponse
							Gson gson = new Gson();
							String json = gson.toJson(geocoderResponse
															.getResults().get(0)
															.getGeometry()
															.getLocation());
							Location theLocation = gson.fromJson(json, Location.class);
							
							// Get formatted address string from GeocoderResponse
							String formattedAddress = geocoderResponse.getResults().get(0).getFormattedAddress();
							
							// GaspLocation is stored in Mongo and returned to the client
							GaspLocation gaspLocation = new GaspLocation(location.getName(),
													 					 formattedAddress,
													 					 theLocation);
							mongoConnection.connect();
							mongoConnection.newGaspLocation(gaspLocation);
							
							//We have a match: return 200 OK plus GaspLocation data
							return Response
									.status(Response.Status.OK)
									.entity(new Gson().toJson(gaspLocation))
									.build();
					
					// There was a problem with the Google Geocoder Service
					// Return 500 Internal Server Error
					case ERROR:
					case INVALID_REQUEST:
					case OVER_QUERY_LIMIT:
					case REQUEST_DENIED:
					case UNKNOWN_ERROR: 
					default:			
						logger.info("Google Geocoding API returned" + geocoderResponse.getStatus());
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();	
				}
		}
		catch (Exception e){
			logger.error("Exception processing location request", e);
			// Return 500 Internal Server Error
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();		
		}
		finally {
			mongoConnection.getMongo().close();
		}
	}

	@POST
    @Path("/lookup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkLocation( LocationQuery location ) {
		try {
			logger.debug("Name = " + location.getName());
			logger.debug("AddressString = " + location.getAddressString());
			
			GeocodeResponse geocoderResponse = geocodeService(location.getAddressString());

			switch(geocoderResponse.getStatus()) {
				
				case ZERO_RESULTS:	// No match found: return 204 No Content
									logger.info("No matches found for address: " + location.getAddressString());
							   	    return Response.status(Response.Status.NO_CONTENT).build();
			
				case OK: 	// More than one match found: return 204 No Content
							if (geocoderResponse.getResults().size() > 1) {
								logger.info("Ambiguous: more than one match for address: " 
											+ location.getAddressString());
								return Response.status(Response.Status.NO_CONTENT).build();				
							}

							//We have a match: return 200 OK plus full result data
							return Response
									.status(Response.Status.OK)
									.entity(new Gson().toJson(geocoderResponse.getResults().get(0)))
									.build();
					
					// There was a problem with the Google Geocoder Service
					// Return 500 Internal Server Error
					case ERROR:
					case INVALID_REQUEST:
					case OVER_QUERY_LIMIT:
					case REQUEST_DENIED:
					case UNKNOWN_ERROR: 
					default:			
						logger.info("Google Geocoding API returned" + geocoderResponse.getStatus());
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();	
				}
		}
		catch (Exception e){
			logger.error("Exception processing location request", e);
			// Return 500 Internal Server Error
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();		
		}
	}
	
	@POST
    @Path("/latlng")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLatLng( LocationQuery location ) {
		try {
			logger.debug("Name = " + location.getName());
			logger.debug("AddressString = " + location.getAddressString());
			
			GeocodeResponse geocoderResponse = geocodeService(location.getAddressString());

			switch(geocoderResponse.getStatus()) {
				
				case ZERO_RESULTS:	// No match found: return 204 No Content
									logger.info("No matches found for address: " + location.getAddressString());
							   	    return Response.status(Response.Status.NO_CONTENT).build();
			
				case OK: 	// More than one match found: return 204 No Content
							if (geocoderResponse.getResults().size() > 1) {
								logger.info("Ambiguous: more than one match for address: " 
											+ location.getAddressString());
								return Response.status(Response.Status.NO_CONTENT).build();				
							}

							// Create a Location object from GeocoderResponse
							Gson gson = new Gson();
							String json = gson.toJson(geocoderResponse
															.getResults().get(0)
															.getGeometry()
															.getLocation());
							Location theLocation = gson.fromJson(json, Location.class);
							
							//We have a match: return 200 OK plus location data
							return Response
									.status(Response.Status.OK)
									.entity(gson.toJson(theLocation))
									.build();
					
					// There was a problem with the Google Geocoder Service
					// Return 500 Internal Server Error
					case ERROR:
					case INVALID_REQUEST:
					case OVER_QUERY_LIMIT:
					case REQUEST_DENIED:
					case UNKNOWN_ERROR: 
					default:			
						logger.info("Google Geocoding API returned" + geocoderResponse.getStatus());
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();	
				}
		}
		catch (Exception e){
			logger.error("Exception processing location request", e);
			// Return 500 Internal Server Error
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();		
		}
	}		
}
