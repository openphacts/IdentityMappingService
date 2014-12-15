// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009  BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  OpenPhacts
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package uk.ac.manchester.cs.openphacts.ims.ws.server;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.api.UriMapper;
import org.bridgedb.uri.tools.DirectStatementMaker;
import org.bridgedb.uri.tools.StatementMaker;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.uri.WSUriServer;
import org.openrdf.rio.RDFFormat;
import uk.ac.manchester.cs.datadesc.validator.Validator;
import uk.ac.manchester.cs.datadesc.validator.ValidatorExampleConstants;
import uk.ac.manchester.cs.datadesc.validator.ValidatorImpl;
import uk.ac.manchester.cs.datadesc.validator.bean.StatementBean;
import uk.ac.manchester.cs.datadesc.validator.bean.URIBean;
import uk.ac.manchester.cs.datadesc.validator.metadata.MetaDataSpecification;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfInterface;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.datadesc.validator.server.FrameInterface;
import uk.ac.manchester.cs.datadesc.validator.server.ValidatorWSInterface;
import uk.ac.manchester.cs.datadesc.validator.server.WsValidatorServer;
import uk.ac.manchester.cs.datadesc.validator.ws.WsValidationConstants;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsStatementMaker;
import uk.ac.manchester.cs.openphacts.ims.rdf.RdfFactoryIMS;

/**
 *
 * @author Christian
 */
public class WsImsServer extends WSUriServer implements FrameInterface, ValidatorWSInterface{
    
    private final WsValidatorServer wsValidatorServer;

    static final Logger logger = Logger.getLogger(WsImsServer.class);
    
    public WsImsServer()  throws BridgeDBException   {
        this(SQLUriMapper.getExisting(), new ImsStatementMaker());
    }
               
    public WsImsServer(UriMapper uriMapper, StatementMaker statementMaker)  throws BridgeDBException   {
        super(uriMapper, statementMaker);
        wsValidatorServer = new WsValidatorServer();
        try {
            RdfInterface rdfInterface = (RdfInterface) RdfFactoryIMS.getReader();
            Validator validator = new ValidatorImpl(rdfInterface);
            wsValidatorServer.setUp(rdfInterface, validator, this);
            MetaDataSpecification.LoadSpecification(ValidatorExampleConstants.SIMPLE_FILE, 
                    ValidatorExampleConstants.SIMPLE_NAME, ValidatorExampleConstants.SIMPLE_DESCRIPTION);
            logger.info("WsImsServer setup");    
        } catch (VoidValidatorException ex) {
            logger.error("Initisation of WsImsServer Service failed!", ex);
        }
    }

    /**
     * Welcome page for the Service.
     * 
     * Expected to be overridden by the QueryExpander
     * 
     * @param httpServletRequest
     * @return
     * @throws BridgeDBException
     * @throws UnsupportedEncodingException 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Override
    public Response welcomeMessage(@Context HttpServletRequest httpServletRequest) throws BridgeDBException, UnsupportedEncodingException {
        if (logger.isDebugEnabled()){
            logger.debug("welcomeMessage called!");
        }
        return bridgeDbHome(httpServletRequest);
    }

    @Override
    protected String serviceName(){
        return "OpenPhacts IMS ";
    }

    /**
     * Allows Super classes to add to the side bar
     */
    @Override
    public void addSideBarMiddle(StringBuilder sb, HttpServletRequest httpServletRequest) {
        super.addSideBarMiddle(sb, httpServletRequest);
        wsValidatorServer.addValidatorSideBar(sb, httpServletRequest);
    }
   
    protected void addValidatorSideBar(StringBuilder sb, HttpServletRequest httpServletRequest) {
        sb.append("<div class=\"menugroup\">OPS Validation Service</div>");
        addSideBarItem(sb, WsValidationConstants.VALIDATE_HOME, "Home", httpServletRequest);
        addSideBarItem(sb, WsValidationConstants.VALIDATE,WsValidationConstants.VALIDATE, httpServletRequest);
        addSideBarItem(sb, WsValidationConstants.STATEMENT_LIST, WsValidationConstants.STATEMENT_LIST,  httpServletRequest);
        addSideBarItem(sb, WsValidationConstants.BY_RESOURCE, WsValidationConstants.BY_RESOURCE,  httpServletRequest);
        addSideBarItem(sb, WsValidationConstants.SPARQL, WsValidationConstants.SPARQL, httpServletRequest);
        addSideBarItem(sb, WsValidationConstants.LOAD_URI, WsValidationConstants.LOAD_URI, httpServletRequest);
    }

    @Override
    public void footerAndEnd(StringBuilder sb) {
        sb.append("</div>\n<div id=\"footer\">");
        sb.append("This site is run by <a href=\"https://wiki.openphacts.org/index.php/User:Christian\">Christian Brenninkmeijer</a>.");
        sb.append("\n<div></body></html>");
    }
    
    @Override
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path(WsValidationConstants.VALIDATE_HOME)
    public Response validateHome(@Context HttpServletRequest httpServletRequest) throws VoidValidatorException {
        return wsValidatorServer.validateHome(httpServletRequest);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path(WsValidationConstants.STATEMENT_LIST)
    @Override
    public Response getStatementList(@QueryParam(WsValidationConstants.SUBJECT) String subjectString, 
            @QueryParam(WsValidationConstants.PREDICATE) String predicateString, 
            @QueryParam(WsValidationConstants.OBJECT) String objectString, 
            @QueryParam(WsValidationConstants.CONTEXT) List<String> contextStrings,
            @Context HttpServletRequest httpServletRequest) throws VoidValidatorException {
       return wsValidatorServer.getStatementList(subjectString, predicateString, objectString, contextStrings, httpServletRequest);
    }

    @GET
    @Produces({MediaType.TEXT_PLAIN})
    @Path(WsValidationConstants.RDF_DUMP)
    @Override
    public String getRdfDump() throws VoidValidatorException {
        return wsValidatorServer.getRdfDump();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path(WsValidationConstants.BY_RESOURCE)
    @Override
    public Response getByResource(@QueryParam(WsValidationConstants.RESOURCE) String resourceString,
            @Context HttpServletRequest httpServletRequest) throws VoidValidatorException{        
        return wsValidatorServer.getByResource(resourceString, httpServletRequest);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path(WsValidationConstants.LOAD_URI)
    @Override
    public Response loadURI(@QueryParam(WsValidationConstants.URI) String address, 
            @QueryParam(WsValidationConstants.RDF_FORMAT) String formatName,
            @Context HttpServletRequest httpServletRequest)throws VoidValidatorException {
        return wsValidatorServer.loadURI(address, formatName, httpServletRequest);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path(WsValidationConstants.SPARQL)
    @Override
    public Response runSparqlQuery(@QueryParam(WsValidationConstants.QUERY)String query, 
            @QueryParam(WsValidationConstants.FORMAT)String formatName,
            @Context HttpServletRequest httpServletRequest)throws VoidValidatorException {
        return wsValidatorServer.runSparqlQuery(query, formatName, httpServletRequest);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path(WsValidationConstants.VALIDATE)
    @Override
    public Response validate(@QueryParam(WsValidationConstants.TEXT) String text, 
            @QueryParam(WsValidationConstants.URI) String uri, 
            @QueryParam(WsValidationConstants.RDF_FORMAT) String rdfFormat,
            @QueryParam(WsValidationConstants.SPECIFICATION) String specification,
            @QueryParam(WsValidationConstants.INCLUDE_WARNINGS) Boolean includeWarning,
            @Context HttpServletRequest httpServletRequest) throws VoidValidatorException {        
        return wsValidatorServer.validate(text, uri, rdfFormat, specification, includeWarning, httpServletRequest);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(WsValidationConstants.STATEMENT_LIST)
    @Override
    public List<StatementBean> getStatementList(@QueryParam(WsValidationConstants.SUBJECT) String subjectString, 
            @QueryParam(WsValidationConstants.PREDICATE) String predicateString, 
            @QueryParam(WsValidationConstants.OBJECT) String objectString, 
            @QueryParam(WsValidationConstants.CONTEXT) List<String> contextStrings) throws VoidValidatorException {
        return wsValidatorServer.getStatementList(subjectString, predicateString, objectString, contextStrings);
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(WsValidationConstants.BY_RESOURCE)
    public List<StatementBean> getByResource(@QueryParam(WsValidationConstants.RESOURCE) String resourceString) 
            throws VoidValidatorException{
        return wsValidatorServer.getByResource(resourceString);
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(WsValidationConstants.LOAD_URI)
    public URIBean loadURI(@QueryParam(WsValidationConstants.URI) String address, 
        @QueryParam(WsValidationConstants.RDF_FORMAT) String formatName)throws VoidValidatorException {
        return wsValidatorServer.loadURI(address, formatName);
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(WsValidationConstants.SPARQL)
    public String runSparqlQuery(@QueryParam(WsValidationConstants.QUERY)String query, 
            @QueryParam(WsValidationConstants.FORMAT)String formatName) throws VoidValidatorException{
        return wsValidatorServer.runSparqlQuery(query, formatName);
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(WsValidationConstants.VALIDATE)
    public String validate(@QueryParam(WsValidationConstants.TEXT) String text, 
            @QueryParam(WsValidationConstants.URI) String uri, 
            @QueryParam(WsValidationConstants.RDF_FORMAT) String rdfFormat,
            @QueryParam(WsValidationConstants.SPECIFICATION) String specification,
            @QueryParam(WsValidationConstants.INCLUDE_WARNINGS) Boolean includeWarning) throws VoidValidatorException {
        return wsValidatorServer.validate(text, uri, rdfFormat, specification, includeWarning);
    }

    @Override
    @POST
	@Path(WsValidationConstants.VALIDATE + WsValidationConstants.TURTLE)
    @Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String validateTurtle(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws VoidValidatorException {  
                //ToDo work out while this paramters is always null
		return wsValidatorServer.validate(uploadedInputStream, INFO, MIME_TYPE, noConentOnEmpty);
	}  
     
    @Override
    @POST
	@Path(WsValidationConstants.VALIDATE + WsValidationConstants.TURTLE)
    @Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response validateTurtleHtml(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, //ToDo work out while this paramters is always null
            @Context HttpServletRequest httpServletRequest) throws VoidValidatorException {  
        return wsValidatorServer.validateTurtleHtml(uploadedInputStream, fileDetail, httpServletRequest);
 	}  
 
    @GET
	@Path(WsValidationConstants.VALIDATE + WsValidationConstants.TURTLE)
    @Produces(MediaType.TEXT_HTML)
	public Response validateTurtleHtml(@Context HttpServletRequest httpServletRequest) throws VoidValidatorException {  
        return wsValidatorServer.validateTurtleHtml(httpServletRequest);
 	}  
 
}


