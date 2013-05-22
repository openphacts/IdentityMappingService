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

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.ws.WsUriConstants;
import org.bridgedb.ws.uri.WSUriServer;

/**
 *
 * @author Christian
 */
public class WsImsServer extends WSUriServer{
    
    static final Logger logger = Logger.getLogger(WsImsServer.class);

    public WsImsServer()  throws BridgeDBException   {
        super();
        logger.info("WsImsServer setup");        
    }
               
    /**
     * Welcome page for the Serivce.
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

    @Override
    protected void top(StringBuilder sb, String title) throws BridgeDBException{
        sb.append("<div id=\"container\">");
        sb.append("<div id=\"top\">");
        sb.append("<a href=\"http://www.cs.manchester.ac.uk/\">" +
        		"<img style=\"float: left; border: none; padding: 0px; margin: 0px;\" " +
        		"src=\"http://assets.manchester.ac.uk/logos/university-1.png\" " +
        		"alt=\"The University of Manchester\" height=\"50\"></img></a>");
        sb.append("<a href=\"http://www.openphacts.org/\">" +
        		"<img style=\"float: right; border: none; padding: 0px; margin: 0px;\" " +
        		"src=\"http://www.openphacts.org/images/stories/banner.jpg\" " +
        		"alt=\"Open PHACTS\" height=\"50\"></img></a>");
        sb.append("<h1>");
        sb.append(title);
        sb.append("</h1>");
        sb.append("</div>");   
    }

    @Override
    protected void footerAndEnd(StringBuilder sb) throws BridgeDBException{
        sb.append("</div>\n<div id=\"footer\">");
        sb.append("This site is run by <a href=\"https://wiki.openphacts.org/index.php/User:Christian\">Christian Brenninkmeijer</a>.");
        sb.append("\n<div></body></html>");
    }
    

}


