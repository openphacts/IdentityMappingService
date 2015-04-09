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

import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import uk.ac.manchester.cs.datadesc.validator.constants.VoidConstants;

/**
 * 
 * @author Christian
 */
public class PreviewHandler extends RDFHandlerBase{
    
    private Statement inVoidStatement = null;
    private Statement linkPredicateStatement = null;
    private Statement justificationStatement = null;

    public PreviewHandler() throws BridgeDBException{
    }
    
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        //ystem.out.println("Handle " + st);
        URI predicate = st.getPredicate();
        if (predicate.equals(VoidConstants.IN_DATASET)){
            if (inVoidStatement != null){
                throw new RDFHandlerException ("Found two statements with predicate " + VoidConstants.IN_DATASET + 
                        st + " and " + inVoidStatement);
            }
            if (linkPredicateStatement != null){
                throw new RDFHandlerException ("Found a statement with predicate " + VoidConstants.IN_DATASET + 
                        st + " and one with " + VoidConstants.LINK_PREDICATE + " " + linkPredicateStatement);
            }
            if (justificationStatement != null){
                throw new RDFHandlerException ("Found a statement with predicate " + VoidConstants.IN_DATASET + 
                        st + " and one with a justification" + justificationStatement);
            }
            inVoidStatement = st;
        } else if (predicate.equals(VoidConstants.LINK_PREDICATE)){
            if (linkPredicateStatement != null){
                throw new RDFHandlerException ("Found two statements with predicate " + VoidConstants.LINK_PREDICATE + 
                        st + " and " + linkPredicateStatement);
            }
            if (inVoidStatement != null){
                throw new RDFHandlerException ("Found a statement with predicate " + VoidConstants.IN_DATASET + 
                        inVoidStatement + " and one with " + VoidConstants.LINK_PREDICATE + " " + st);
            }
            linkPredicateStatement = st;
        } else if (predicate.equals(BridgeDBConstants.LINKSET_JUSTIFICATION) || predicate.equals(DulConstants.EXPRESSES)){
            if (justificationStatement != null){
                throw new RDFHandlerException ("Found two statements with justification " + 
                        st + " and " + justificationStatement);
            }
            if (inVoidStatement != null){
                throw new RDFHandlerException ("Found a statement with predicate " + VoidConstants.IN_DATASET + 
                        inVoidStatement + " and one with justification " + st);
            }
            justificationStatement = st;
        }
        //ignore all other statements
    }
    
       @Override
    public void endRDF() throws RDFHandlerException {
        super.endRDF();
        if (inVoidStatement == null){
            if (linkPredicateStatement == null){
                throw new RDFHandlerException ("No statements found with predicate " + VoidConstants.IN_DATASET + 
                        " or " + VoidConstants.LINK_PREDICATE);
            }
            if (justificationStatement == null){
                throw new RDFHandlerException ("No statements found with predicate " + VoidConstants.IN_DATASET + 
                        ", " + BridgeDBConstants.LINKSET_JUSTIFICATION + " or " + DulConstants.EXPRESSES);
            }
        }
    }

    /**
     * @return the inVoidStatement
     */
    public final Statement getInVoidStatement() {
        return inVoidStatement;
    }

    /**
     * @return the inVoidStatement
     */
    public final Statement getLinkPredicateStatement() {
        return linkPredicateStatement;
    }

    /**
     * @return the inVoidStatement
     */
    public final Statement getJustificationStatement() {
        return justificationStatement;
    }

}
