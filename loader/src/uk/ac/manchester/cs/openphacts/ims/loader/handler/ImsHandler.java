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
import org.bridgedb.uri.tools.RegexUriPattern;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfInterface;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.RdfFactoryIMS;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsListener;

/**
 *
 * @author Christian
 */
public class ImsHandler extends LinksetHandler{
    
    private final RdfInterface rdfInterface;
    private final ImsListener imsListener;
    private final URI mappingResource; 
   
    public ImsHandler(ImsListener imsListener, URI linkPredicate, String justification,
            URI mappingResource, URI mappingSource) 
            throws BridgeDBException{
        super(imsListener, linkPredicate, justification, mappingSource);
        this.imsListener = imsListener;
        this.rdfInterface = RdfFactoryIMS.getReader();
        this.mappingResource = mappingResource;
    }
    
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if (st.getPredicate().equals(linkPredicate)){
            super.handleStatement(st);
        } else {
            try {
                rdfInterface.add(st, mappingSource);
            } catch (VoidValidatorException ex) {
                throw new RDFHandlerException("unable to load statement " + st, ex);
            }
        }
    }

    protected void registerMappingSet(RegexUriPattern sourcePattern, RegexUriPattern targetPattern ) 
            throws BridgeDBException{
        if (backwardJustification == null){
            mappingSet =  imsListener.registerMappingSet(sourcePattern, linkPredicate.stringValue(), 
                    justification, targetPattern, mappingResource, mappingSource, false);
            this.setSymetric(false);
        } else {
            mappingSet = imsListener.registerMappingSet(sourcePattern, linkPredicate.stringValue(), 
                    justification, backwardJustification, targetPattern, mappingSource);
        }
    }


    @Override
    public void endRDF() throws RDFHandlerException {
        super.endRDF();
        try {
            rdfInterface.commit();
            imsListener.closeInput();
        } catch (VoidValidatorException ex) {
            throw new RDFHandlerException("Unable to commit", ex);
        } catch (BridgeDBException ex) {
            throw new RDFHandlerException("Unable to close", ex);
        }
    }
}
