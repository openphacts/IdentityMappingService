// OpenPHACTS RDF Validator,
// A tool for validating and storing RDF.
//
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  University of Manchester
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
package uk.ac.manchester.cs.openphacts.ims.loader.handler;

import org.bridgedb.uri.loader.LinksetHandler;
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
