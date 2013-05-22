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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfInterface;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class RdfInterfacteHandler extends RDFHandlerBase{
    private final RdfInterface rdfInterface;
    private final Resource context;
    
    public RdfInterfacteHandler(RdfInterface rdfInterface, Resource context){
        this.rdfInterface = rdfInterface;
        this.context = context;
    }
    
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        try {
            rdfInterface.add(st, context);
        } catch (VoidValidatorException ex) {
            throw new RDFHandlerException("unable to load statement " + st, ex);
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
