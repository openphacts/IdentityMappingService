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
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.util.HashMap;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 *
 * @author Christian
 */
public class PredicateFinder extends RDFHandlerBase{
    
    private final HashMap<URI, Statement> singlePredicate = new HashMap<URI, Statement>();
    private final HashMap<URI, Integer> predicateCount = new HashMap<URI, Integer>();
        
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        URI predicate = st.getPredicate();
        if (predicateCount.containsKey(predicate)){
            Integer count = predicateCount.get(predicate);
            count++;
            predicateCount.put(predicate, count);
        } else if (singlePredicate.containsKey(predicate)){
            singlePredicate.put(predicate, null);
            Integer count = 2;
            predicateCount.put(predicate, count);            
        } else {
            singlePredicate.put(predicate, st);            
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        super.endRDF();
        Integer count = 1;
        for (URI uri:singlePredicate.keySet()){
            predicateCount.put(uri, count);
        }
    }

    public Statement getSinglePredicateStatements(URI predicate){
        return singlePredicate.get(predicate);
    }
    
    //public HashMap<URI, Integer> getPredicateCount(){
    //     return predicateCount;
    // }
    
    public Integer getPredicateCount(URI predicate){
        if (predicateCount.containsKey(predicate)){
            return predicateCount.get(predicate);
        } else {
            return 0;
        }
    } 
}
