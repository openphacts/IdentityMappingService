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
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfInterface;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsListener;

/**
 *
 * @author Christian
 */
public class ImsHandler extends LinksetHandler{
    
    private final RdfInterface rdfInterface;
    private final Resource context;

    public ImsHandler(RdfInterface rdfInterface, Resource context, ImsListener imsListener, URI linkPredicate, String justification, Resource mappingResource, 
            Resource mappingSource, boolean symetric){
        super(imsListener, linkPredicate, justification, mappingResource, mappingSource, symetric);
        this.rdfInterface = rdfInterface;
        this.context = context;
    }
    
    public ImsHandler(RdfInterface rdfInterface, Resource context, ImsListener imsListener, URI linkPredicate, String forwardJustification, 
            String backwardJustification, Resource mappingResource, Resource mappingSource){
        super(imsListener, linkPredicate, forwardJustification, backwardJustification, mappingResource, mappingSource);
        this.rdfInterface = rdfInterface;
        this.context = context;
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if (st.getPredicate().equals(linkPredicate)){
            super.handleStatement(st);
        } else {
            try {
                rdfInterface.add(st, context);
            } catch (VoidValidatorException ex) {
                throw new RDFHandlerException("unable to load statement " + st, ex);
            }
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        super.endRDF();
        try {
            rdfInterface.commit();
        } catch (VoidValidatorException ex) {
            throw new RDFHandlerException("Unable to commit", ex);
        }
        
    }
}
