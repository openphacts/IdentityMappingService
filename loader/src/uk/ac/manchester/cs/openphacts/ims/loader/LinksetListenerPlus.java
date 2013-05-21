/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.RdfParser;
import org.bridgedb.uri.UriListener;
import org.openrdf.rio.RDFHandler;

/**
 *
 * @author Christian
 */
public class LinksetListenerPlus extends LinksetListener{
    
    public LinksetListenerPlus(UriListener uriListener){
        super(uriListener);
    }
    
    protected RdfParser getParser(RDFHandler handler){
       return new RdfParserPlus(handler);
    }

}
