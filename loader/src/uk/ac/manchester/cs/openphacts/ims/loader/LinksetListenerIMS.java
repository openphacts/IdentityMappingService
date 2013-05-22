/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.bridgedb.loader.LinksetHandler;
import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.LinksetListenerSimple;
import org.bridgedb.loader.RdfParser;
import org.bridgedb.uri.UriListener;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;

/**
 *
 * @author Christian
 */
public class LinksetListenerIMS implements LinksetListener{

    private final UriListener uriListener;
    private boolean SYMETRIC = true; 
    
    public LinksetListenerIMS(UriListener uriListener){
        this.uriListener = uriListener;
    }
    
    static final Logger logger = Logger.getLogger(LinksetListenerSimple.class);
    
    @Override
    public int parse(File file, String mappingSource, URI linkPredicate, String justification) throws BridgeDBException{
        LinksetHandler handler = new LinksetHandler(uriListener, linkPredicate, justification, mappingSource, true);
        RdfParser parser = getParser(handler);
        parser.parse(file);
        return handler.getMappingsetId();
    }
    
    public int parse(String uri, String mappingSource, URI linkPredicate, String justification) throws BridgeDBException{
        LinksetHandler handler = new LinksetHandler(uriListener, linkPredicate, justification, mappingSource, true);
        RdfParser parser = getParser(handler);
        parser.parse(uri);
        return handler.getMappingsetId();
    }

     public int parse(InputStream stream, String mappingSource, URI linkPredicate, String justification) throws BridgeDBException{
        LinksetHandler handler = new LinksetHandler(uriListener, linkPredicate, justification, mappingSource, true);
        RdfParser parser = getParser(handler);
        parser.parse(stream, mappingSource);
        return handler.getMappingsetId();
    }

    protected RdfParser getParser(RDFHandler handler){
       return new RdfParserPlus(handler);
    }
}
