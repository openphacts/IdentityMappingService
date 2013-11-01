/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.ws.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.ws.server.MappingSetTableMaker;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.impl.URIImpl;
import uk.ac.manchester.cs.datadesc.validator.ws.WsValidationConstants;

/**
 *
 * @author Christian
 */
public class IMSMappingSetTableMaker extends MappingSetTableMaker {
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(IMSMappingSetTableMaker.class);
   
    public IMSMappingSetTableMaker(List<MappingSetInfo> mappingSetInfos, HttpServletRequest httpServletRequest){
        super(mappingSetInfos, httpServletRequest);
    }
 
    @Override 
    protected void addResourceLinkCell(StringBuilder sb, String resource) throws BridgeDBException{
        sb.append("\t\t<td>");
        if (resource == null){  
            sb.append("&nbsp");
        } else {
            sb.append("<a href=\"");
            sb.append(httpServletRequest.getContextPath());
            sb.append("/");
            sb.append(WsValidationConstants.BY_RESOURCE);
            sb.append("?");
            sb.append(WsValidationConstants.RESOURCE);        
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(resource, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                logger.error(ex);
                sb.append(resource);
            }
            sb.append("\">");
            URIImpl impl = new URIImpl(resource); 
            sb.append(impl.getLocalName());
            sb.append("</td>\n");
        }
        sb.append("</td>\n");
   }

}
