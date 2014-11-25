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
package uk.ac.manchester.cs.openphacts.ims.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.CalendarLiteralImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.RdfFactoryIMS;

/**
 *
 * @author Christian
 */
public class PreviewHandlerOld extends RDFHandlerBase{
    
    private final Collection<URI> predicatesToStoreMultiple;
    
    private final HashMap<URI, Set<Statement>> savedStatements = new HashMap<URI, Set<Statement>>();    
    private final HashMap<URI, Statement> singlePredicate = new HashMap<URI, Statement>();
    private final HashMap<URI, Integer> predicateCount = new HashMap<URI, Integer>();
    
    private final RdfReader reader;

    public PreviewHandlerOld(Collection<URI> predicates) throws BridgeDBException{
        predicatesToStoreMultiple = predicates;
        this.reader = RdfFactoryIMS.getReader();
    }
    
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        //ystem.out.println("Handle " + st);
        URI predicate = st.getPredicate();
        if (predicatesToStoreMultiple.contains(predicate)){
            Set<Statement> statements = savedStatements.get(predicate);
            if (statements == null){
                statements = new HashSet<Statement>();
            }
            statements.add(st);
            //ystem.out.println ("Added to " + statements);
            savedStatements.put(predicate, statements);
        } else {
            Integer count = predicateCount.get(st);
            if (count != null){
                count++;
                //ystem.out.println ("Count " + count);
                predicateCount.put(predicate, count);
            } else {
                Statement previous = singlePredicate.get(predicate);
                if (previous == null){
                    singlePredicate.put(predicate, st);
                    //ystem.out.println ("Single " + st);
                } else if (previous.equals(st)){
                    //duplicate do nothing
                } else {
                    count = 2;
                    //ystem.out.println ("Count " + count);
                    predicateCount.put(predicate, count);                
                }
            }
        }
    }

    public Statement getRandomPredicateStatements(URI predicate) throws BridgeDBException {
        if (predicatesToStoreMultiple.contains(predicate)){
            Set<Statement> statements = savedStatements.get(predicate);
            if (statements.size() >= 1){
                return statements.iterator().next();
            }
            return null;
        } else {
            return singlePredicate.get(predicate);
        }
    }
    
    public Statement getSinglePredicateStatements(URI predicate) throws BridgeDBException {
        if (predicatesToStoreMultiple.contains(predicate)){
            Set<Statement> statements = savedStatements.get(predicate);
            if (statements.size() == 1){
                return statements.iterator().next();
            }
            if (statements.isEmpty()){
                return null;
            }
            throw new  BridgeDBException ("Found " + statements.size() + " statements with predicate " + predicate 
                    + " while previewer only expected 1");
        } else {
            Integer count = predicateCount.get(predicate);
            if (count == null){
                return singlePredicate.get(predicate);
            } else {
                throw new  BridgeDBException ("Found " + count + " statements with predicate " + predicate
                        + " while previewer only expected 1");
            }
        }
    }
    
    public final Set<Statement> getStatementList(Resource subject, URI predicate) throws BridgeDBException{
        if (subject == null){
            return getStatementList(predicate);
        }
        Set<Statement> results = new HashSet<Statement>();
        if (predicatesToStoreMultiple.contains(predicate)){
            Set<Statement> statements = savedStatements.get(predicate);
            if (statements == null){
                return results;
            }
            for (Statement statement:statements){
                if (statement.getSubject().equals(subject)){
                    results.add(statement);
                }
            }
        } else {
            Statement statement = getSinglePredicateStatements(predicate);
            if (statement != null && statement.getSubject().equals(subject)){
                results.add(statement);
            }      
        }
        return results;
    }
    
    public final Set<Statement> getStatementList(URI predicate) throws BridgeDBException{
        if (predicatesToStoreMultiple.contains(predicate)){
            System.out.println(predicatesToStoreMultiple);
            Set<Statement> statements = savedStatements.get(predicate);
            if (statements == null){
                return new HashSet<Statement>();
            } else {
                return new HashSet<Statement>(statements);
            }
        } else {
            System.out.println(predicate);
            HashSet<Statement> results = new HashSet<Statement>();
            Statement statement = getSinglePredicateStatements(predicate);
            System.out.println(statement);
            if (statement != null){
                results.add(statement);
            }
            System.out.println(results);
            return results;
        }
    }
    
    //public HashMap<URI, Integer> getPredicateCount(){
    //     return predicateCount;
    // }
    
    public Integer getPredicateCount(URI predicate){
        if (predicatesToStoreMultiple.contains(predicate)){
            Set<Statement> statements = savedStatements.get(predicate);
            if (statements == null){
                return 0;
            } else {
                return statements.size();
            }
        } else {
            Integer count = predicateCount.get(predicate);
            if (count != null){
                return count;
            } else {
                Statement statement = singlePredicate.get(predicate);
                if (statement == null){
                    return 0;
                } else {
                    return 1;
                }
            }
        }
    }
    
    private Collection<Statement> getStatements(Resource subject, URI... predicates) 
            throws BridgeDBException, VoidValidatorException{
        for (URI predicate:predicates){
            Collection<Statement> statements = getStatementList(subject, predicate);
            if (!statements.isEmpty()){
                return statements;
            }
        }
        for (URI predicate:predicates){
            Collection<Statement> statements = reader.getStatementList(subject, predicate, null);
            if (!statements.isEmpty()){
                return statements;
            }
        }
        return new HashSet<Statement>();
    }
    
    public final Value getSingleValue(Resource subject, URI... predicates) throws BridgeDBException, VoidValidatorException{
        Collection<Statement> statements = getStatements(subject, predicates);
        if (statements == null || statements.isEmpty()){
            throw new BridgeDBException("No statements found " + withInfo(subject, predicates));
        }
        if (statements.size() == 1){
            return statements.iterator().next().getObject();
        }
        Statement first = null;
        for (Statement statement:statements){
            if (first == null){
                first = statement;
            } else {
                if (first.equals(statement)){
                    //ignore duplicates
                } else {
                    throw new BridgeDBException(statements.size() + " statements found " + withInfo(subject, predicates));
                }
            }
        }
        //All Statements the same so retirn 1;
        return first.getObject();
    }
    
    public final Value getPossibleValue(Resource subject, URI... predicates) 
            throws VoidValidatorException, BridgeDBException {
        Collection<Statement> statements = getStatements(subject, predicates);
        if (statements == null || statements.isEmpty()){
            return null;
        }
        return statements.iterator().next().getObject();
    }

    public final URI getSingleURI(Resource subject, URI... predicates) 
            throws VoidValidatorException, BridgeDBException {
        Value value = getSingleValue(subject, predicates);
        if (value instanceof URI){
            return (URI)value;
        }
        throw new BridgeDBException("Founnd none URI " + value + " object " + withInfo(subject, predicates));
    }

    public final URI getPossibleURI(Resource subject, URI... predicates) 
            throws VoidValidatorException, BridgeDBException {
        Value value = getPossibleValue(subject, predicates);
        if (value == null){
            return null;
        }
        if (value instanceof URI){
            return (URI)value;
        }
        throw new BridgeDBException("Founnd none URI " + value + " object " + withInfo(subject, predicates));
    }

    public final CalendarLiteralImpl getPossibleCalendar(Resource subject, URI... predicates) 
            throws VoidValidatorException, BridgeDBException {
        Value value = getPossibleValue(subject, predicates);
        if (value == null){
            return null;
        }
        if (value instanceof CalendarLiteralImpl){
            return (CalendarLiteralImpl)value;
        }
        throw new BridgeDBException("Founnd none CalendarLiteralImpl " + value + " object " + withInfo(subject, predicates));
    }
    
    public final String getPossibleString(Resource subject, URI predicate) 
            throws VoidValidatorException, BridgeDBException {
        Value value = getPossibleValue(subject, predicate);
        if (value == null){
            return null;
        }
        return value.stringValue();
    }

    public String withInfo(Resource subject, URI[] predicates) {
        if (predicates.length == 1){
            return "with Subject " + subject + " and predicate " + predicates[0].stringValue();
        } else {
            String message = "with Subject " + subject + " and predicates ";
            for (int i = 0; i < predicates.length - 1; i++){
                message = message + predicates[i] + ", ";
            }
            message = message + predicates[predicates.length - 1];            
            return message;
        }
    }

}
