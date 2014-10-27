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

import java.io.File;
import java.util.Set;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.sql.justification.OpsJustificationMaker;
import org.bridgedb.uri.loader.LinksetHandler;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import uk.ac.manchester.cs.datadesc.validator.Validator;
import uk.ac.manchester.cs.datadesc.validator.ValidatorImpl;
import uk.ac.manchester.cs.datadesc.validator.constants.VoidConstants;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.PredicateFinderHandler;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.RdfInterfacteHandler;

public class Loader 
{
    private final Validator validator;
    protected final RdfReader reader;
    protected final SQLUriMapper uriListener;
            
    public Loader() throws BridgeDBException {
        validator = new ValidatorImpl();
        uriListener = SQLUriMapper.getExisting();
        reader = RdfFactoryIMS.getReader();
        UriPattern.refreshUriPatterns();
    }
    
    protected final PredicateFinderHandler getPredicateFinderHandler(String uri, String rdfFormatName) throws BridgeDBException{
        PredicateFinderHandler finder = new PredicateFinderHandler();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(uri, rdfFormatName);
        return finder;
    }
    
    private PredicateFinderHandler getPredicateFinderHandler(String baseURI, File file, String rdfFormatName) throws BridgeDBException{
        PredicateFinderHandler finder = new PredicateFinderHandler();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(baseURI, file, rdfFormatName);
        return finder;
    }

    protected final  URI getObject(PredicateFinderHandler finder, URI predicate) throws BridgeDBException{
        Statement statement =  finder.getSinglePredicateStatements(predicate);
        if (statement != null){
            Value object = statement.getObject();
            if (object instanceof URI){
                return (URI)object;
            }
            throw new BridgeDBException ("Unexpected Object in " + statement);
        }
        Integer count = finder.getPredicateCount(predicate);
        if (count == 0){
            throw new BridgeDBException("No statement found with predicate "+ predicate);
        }
        throw new BridgeDBException("Found " + count + " statements with predicate "+ predicate);
    }
    
    protected final  URI getObject(PredicateFinderHandler finder, URI predicateMain, URI predicateBackup) throws BridgeDBException{
       Statement statement =  finder.getSinglePredicateStatements(predicateMain);
        if (statement != null){
            Value object = statement.getObject();
            if (object instanceof URI){
                return (URI)object;
            }
            throw new BridgeDBException ("Unexpected Object in " + statement);
        }
        statement =  finder.getSinglePredicateStatements(predicateBackup);
        if (statement != null){
            Value object = statement.getObject();
            if (object instanceof URI){
                return (URI)object;
            }
            throw new BridgeDBException ("Unexpected Object in " + statement);
        }
        Integer count = finder.getPredicateCount(predicateMain);
        if (count == 0){
            throw new BridgeDBException("No statement found with predicate "+ predicateMain);
        }
        throw new BridgeDBException("Found " + count + " statements with predicate "+ predicateMain);
    }

    protected final Value getPossibleValue(PredicateFinderHandler finder, URI predicate) throws BridgeDBException{
        Statement statement =  finder.getSinglePredicateStatements(predicate);
        if (statement != null){
            return statement.getObject();
        }
        return null;
    }

    protected final Value getPossibleValue(Resource subject, URI predicate) throws VoidValidatorException, BridgeDBException {
        Value result = null;
        for (Statement statement:reader.getStatementList(subject, predicate, null)){
            if (result == null){
                result = statement.getObject();
            } else if (result.stringValue().equals(statement.getObject().stringValue())){
                //ignore dublicate
            } else {
                throw new BridgeDBException ("Found two (or more) objects for subject " + subject 
                    + " and predicate " + predicate + ". Found " + result + " and " + statement.getObject());
            }
        }
        return result;
    }

    protected final URI getObject(Resource subject, URI predicate) throws VoidValidatorException, BridgeDBException {
        Value value = getPossibleValue(subject, predicate);
        if (value == null){
            throw new BridgeDBException ("No statements found for subject " + subject + " and predicate " + predicate);
        } else {
            return getUri(value);
        }
    }

    protected final URI getObject(Resource subject, URI predicateMain, URI predicateBackup) throws VoidValidatorException, BridgeDBException {
        Value value = getPossibleValue(subject, predicateMain);
        if (value == null){
            value = getPossibleValue(subject, predicateBackup);
        }
        if (value == null){
            throw new BridgeDBException ("No statements found for subject " + subject + " and predicate " + predicateMain);
        }
        return getUri(value);
    }

    protected final Resource getLinksetId(PredicateFinderHandler finder) throws BridgeDBException{
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.LINK_PREDICATE);
        if (statement != null){
            return statement.getSubject();
        }
        statement =  finder.getSinglePredicateStatements(DulConstants.EXPRESSES);
        if (statement != null){
            return statement.getSubject();
        }
        statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
        if (statement != null){
            throw new BridgeDBException("Found an void:inDataset to " + statement.getObject() +
                    " but that did not lead to find a statement with either " + VoidConstants.LINK_PREDICATE + " or " 
                    + DulConstants.EXPRESSES);
        }
        
        throw new BridgeDBException("Unable to find a statement with either " + VoidConstants.LINK_PREDICATE + " or " 
                + DulConstants.EXPRESSES);
    }
    
    public int load(String uri) throws VoidValidatorException, BridgeDBException{
        return load(uri, null);
    }
    
    public int load(String uri, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
        Resource context = new URIImpl(uri);
        PredicateFinderHandler finder = getPredicateFinderHandler(uri, rdfFormatName);
        RdfParserIMS parser = getParser(context, finder, null);
        parser.parse(uri, rdfFormatName);
        return parser.getMappingsetId();       
    }

    public int load(File file) throws VoidValidatorException, BridgeDBException{
        Resource context = UriFileMapper.getUri(file);
        return load(file, context);
    }
    
    public int load(File file, Resource context) throws VoidValidatorException, BridgeDBException{
        return load (file, context, null, null);
    }
    
    public int load(File file, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
        Resource context = UriFileMapper.getUri(file);
        return load(file, context, rdfFormatName, null);
    }
    
    public int load(File file, Resource context, String rdfFormatName, Boolean symmetric) 
            throws VoidValidatorException, BridgeDBException{
        PredicateFinderHandler finder = getPredicateFinderHandler(context.stringValue(), file, rdfFormatName);
        RdfParserIMS parser = getParser(context , finder, symmetric);
        parser.parse(context.stringValue(), file, rdfFormatName);
        return parser.getMappingsetId();       
    }

    public RdfParserIMS getParser(Resource context, PredicateFinderHandler finder, Boolean symmetric) throws VoidValidatorException, BridgeDBException{
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
        Resource linksetId;
        URI linkPredicate;
        String rawJustification;
        Value isSymetric;
        if (statement != null){
            linksetId  = getObject(statement);
            linkPredicate = getObject(linksetId, VoidConstants.LINK_PREDICATE);
            rawJustification = getObject(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES).stringValue();  
            isSymetric = getPossibleValue(linksetId, BridgeDBConstants.IS_SYMETRIC);
        } else {
            linksetId = getLinksetId(finder);
            linkPredicate = getObject(finder, VoidConstants.LINK_PREDICATE);
            rawJustification = getObject(finder, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES).stringValue();    
            isSymetric = getPossibleValue(finder, BridgeDBConstants.IS_SYMETRIC);
        }
        Boolean mergedSymetric = mergeSymetric(context, symmetric, isSymetric);
        LinksetHandler linksetHandler;
        if (mergedSymetric == null){
            OpsJustificationMaker opsJustificationMaker = OpsJustificationMaker.getInstance();
            String forwardJustification = opsJustificationMaker.getForward(rawJustification); //getInverseJustification(justification);  
            String backwardJustification = opsJustificationMaker.getInverse(rawJustification); //getInverseJustification(justification);  
            if (forwardJustification.equals(backwardJustification)){
                linksetHandler = new LinksetHandler(uriListener, linkPredicate, rawJustification, 
                        linksetId, context, true);
            } else {
                linksetHandler = new LinksetHandler(uriListener, linkPredicate, forwardJustification, backwardJustification, linksetId, context);
            }
        } else {
            linksetHandler = new LinksetHandler(uriListener, linkPredicate, rawJustification, 
                linksetId, context, mergedSymetric.booleanValue());
        }
        RdfInterfacteHandler readerHandler = new RdfInterfacteHandler(reader, context);
        //ImsRdfHandler combinedHandler = 
        //        new ImsRdfHandler(linksetHandler, readerHandler, linkPredicate);
        return new RdfParserIMS(linksetHandler, readerHandler, linkPredicate);
    }

    private Boolean mergeSymetric (Resource context, Boolean given, Value read) throws BridgeDBException{
        if (read == null){
            return given;
        }
        if (read instanceof Literal){
            Literal literal = (Literal)read;
            boolean saysSymetric = literal.booleanValue();
            if (given != null && given != saysSymetric){
                throw new BridgeDBException ("Request to load " + context + " with symetric = " + given 
                            + " but found " + read);
            }
            return saysSymetric;
        } 
        throw new BridgeDBException ("Reading " + context + " unexpected object " + read 
                + " found with predicate " + BridgeDBConstants.IS_SYMETRIC);
    }
    
    protected final URI getObject(Statement statement) throws BridgeDBException{
        if (statement.getObject() instanceof URI){
            return (URI)statement.getObject();
        } else {
            throw new BridgeDBException("Found statement " + statement + " but object is not a URI.");
        }
    }

   private URI getUri(Value value) throws BridgeDBException{
        if (value instanceof URI){
            return (URI)value;
        } else {
            throw new BridgeDBException("Found " + value + " but it is not a URI.");
        }
    }

   void recover() throws BridgeDBException {
        uriListener.recover();
    }
    
   void closeInput() throws BridgeDBException {
        uriListener.closeInput();
    }
}
