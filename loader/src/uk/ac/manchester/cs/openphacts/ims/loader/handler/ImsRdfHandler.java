/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader.handler;

import org.bridgedb.loader.LinksetHandler;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author Christian
 */
public class ImsRdfHandler implements RDFHandler{
    
    private final LinksetHandler linksetHandler;
    private final RdfInterfacteHandler rdfInterfacteHandler;
    private final URI predicate;
    
    public ImsRdfHandler(LinksetHandler linksetHandle, RdfInterfacteHandler rdfInterfacteHandler, URI predicate){
        this.linksetHandler = linksetHandle;
        this.rdfInterfacteHandler = rdfInterfacteHandler;
        this.predicate = predicate;
    }
    
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if (st.getPredicate().equals(predicate)){
            linksetHandler.handleStatement(st);
        } else {
            rdfInterfacteHandler.handleStatement(st);
        }
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        linksetHandler.startRDF();
        rdfInterfacteHandler.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        linksetHandler.endRDF();
        rdfInterfacteHandler.endRDF();
    }

    @Override
    public void handleNamespace(String prefix, String uri)  throws RDFHandlerException {
        linksetHandler.handleNamespace(prefix, uri);
        rdfInterfacteHandler.handleNamespace(prefix, uri);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        linksetHandler.handleComment(comment);
        rdfInterfacteHandler.handleComment(comment);
    }

    
}
