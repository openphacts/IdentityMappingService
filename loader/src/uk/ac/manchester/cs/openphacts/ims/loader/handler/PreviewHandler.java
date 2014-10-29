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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DCTermsConstants;
import org.bridgedb.rdf.constants.DCatConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.rdf.constants.PavConstants;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import uk.ac.manchester.cs.datadesc.validator.constants.VoidConstants;

/**
 *
 * @author Christian
 */
public class PreviewHandler extends RDFHandlerBase{
    
    private static final Set<URI> predicateToStore = new HashSet<URI>();
    public static final Set<URI> LINKSET_PREDICATES = new HashSet<URI>();
    public static final Set<URI> DATASET_PREDICATES = new HashSet<URI>();
    public static final Set<URI> DISTRIBUTION_PREDICATES = new HashSet<URI>();
    
    private final HashMap<URI, Set<Statement>> savedStatements = new HashMap<URI, Set<Statement>>();    
    
    static {
        LINKSET_PREDICATES.add(DCTermsConstants.TITLE_URI);
        LINKSET_PREDICATES.add(DCTermsConstants.DESCRIPTION_URI);
        LINKSET_PREDICATES.add(VoidConstants.SUBJECTSTARGET);
        LINKSET_PREDICATES.add(VoidConstants.OBJECTSTARGET);
        LINKSET_PREDICATES.add(VoidConstants.LINK_PREDICATE);
        LINKSET_PREDICATES.add(BridgeDBConstants.SUBJECTS_DATATYPE);//For future use
        LINKSET_PREDICATES.add(BridgeDBConstants.OBJECTS_DATATYPE);//For future use
        LINKSET_PREDICATES.add(BridgeDBConstants.SUBJECTS_SPECIES);//For future use
        LINKSET_PREDICATES.add(BridgeDBConstants.OBJECTS_SPECIES);//For future use
        LINKSET_PREDICATES.add(BridgeDBConstants.IS_SYMETRIC);//?
        LINKSET_PREDICATES.add(BridgeDBConstants.LINKSET_JUSTIFICATION);
        LINKSET_PREDICATES.add(DulConstants.EXPRESSES);
        DATASET_PREDICATES.add(DCTermsConstants.TITLE_URI);
        DATASET_PREDICATES.add(DCTermsConstants.DESCRIPTION_URI);
        DATASET_PREDICATES.add(PavConstants.VERSION);
        DATASET_PREDICATES.add(DCatConstants.DISTRIBUTION_URI);
        DISTRIBUTION_PREDICATES.add(PavConstants.VERSION);
        DISTRIBUTION_PREDICATES.add(DCatConstants.BYTE_SIZE_URI);
        predicateToStore.add(VoidConstants.IN_DATASET);
        predicateToStore.addAll(LINKSET_PREDICATES);
        predicateToStore.addAll(DATASET_PREDICATES);
        predicateToStore.addAll(DISTRIBUTION_PREDICATES);
    }
    
    public PreviewHandler(){
        for (URI uri:predicateToStore){
            HashSet<Statement> statements = new HashSet<Statement>();
            savedStatements.put(uri, statements);
        }
    }
    
     
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        URI predicate = st.getPredicate();
        if (predicateToStore.contains(predicate)){
            Set<Statement> statements = savedStatements.get(predicate);
            statements.add(st);
            savedStatements.put(predicate, statements);
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        super.endRDF();
    }

    public Statement getSinglePredicateStatements(URI predicate) throws BridgeDBException {
        Set<Statement> statements = savedStatements.get(predicate);
        if (statements.size() == 1){
            return statements.iterator().next();
        }
        if (statements.isEmpty()){
            return null;
        }
        throw new  BridgeDBException ("Found " + statements.size() + " statements with predicate " + predicate);
    }
    
    public final Set<Statement> getStatementList(Resource subject, URI predicate){
        if (subject == null){
            return getStatementList(predicate);
        }
        Set<Statement> statements = savedStatements.get(predicate);
        Set<Statement> results = new HashSet<Statement>();
        if (statements == null){
            return results;
        }
        for (Statement statement:statements){
            if (statement.getSubject().equals(subject)){
                results.add(statement);
            }
        }
        return results;
    }
    
    public final Set<Statement> getStatementList(URI predicate){
        Set<Statement> statements = savedStatements.get(predicate);
        if (statements == null){
            new HashSet<Statement>();
        }
        return new HashSet<Statement>(statements);
    }
    
    //public HashMap<URI, Integer> getPredicateCount(){
    //     return predicateCount;
    // }
    
    public Integer getPredicateCount(URI predicate){
        Set<Statement> statements = savedStatements.get(predicate);
        return statements.size();
    }
}
