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
import java.util.Collection;
import java.util.Set;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.sql.justification.OpsJustificationMaker;
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
import uk.ac.manchester.cs.openphacts.ims.loader.handler.ImsHandler;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.PreviewHandler;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsMapper;

public class Loader 
{
    private final Validator validator;
    protected final RdfReader reader;
    protected final ImsMapper imsMapper;
            
    public Loader() throws BridgeDBException {
        validator = new ValidatorImpl();
        imsMapper = ImsMapper.getExisting();
        reader = RdfFactoryIMS.getReader();
        UriPattern.refreshUriPatterns();
    }
    
    protected final PreviewHandler getPreviewHandler(String uri, String rdfFormatName) throws BridgeDBException{
        PreviewHandler finder = new PreviewHandler();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(uri, rdfFormatName);
        return finder;
    }
    
    private PreviewHandler getPreviewHandler(String baseURI, File file, String rdfFormatName) throws BridgeDBException{
        PreviewHandler finder = new PreviewHandler();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(baseURI, file, rdfFormatName);
        return finder;
    }

    protected final Value getPossibleValue(PreviewHandler preview, Resource subject, URI predicate) 
            throws VoidValidatorException, BridgeDBException {
        Set<Statement> statements = preview.getStatementList(subject, predicate);
        if (statements.size() == 1){
            return statements.iterator().next().getObject();
        }
        if (statements.size() > 1){
            return getPossibleValue(statements);
        }        
        return getPossibleValue(reader.getStatementList(subject, predicate, null));
      }

    private Value getPossibleValue (Collection<Statement> statements) throws BridgeDBException{
        if (statements == null || statements.isEmpty()){
            return null;
        }
        Value result = null;
        for (Statement statement:statements){
            if (result == null){
                result = statement.getObject();
            } else if (result.stringValue().equals(statement.getObject().stringValue())){
                //ignore dublicate
            } else {
                throw new BridgeDBException ("Found two (or more) objects ");
            }
        }
        return result;
    }

    protected final URI getObject(PreviewHandler finder, URI predicate) throws BridgeDBException{
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
    
    /*protected final  URI getObject(PreviewHandler finder, URI predicateMain, URI predicateBackup) throws BridgeDBException{
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
    }*/

    protected final URI getObject(PreviewHandler finder, Resource subject, URI predicate) 
            throws VoidValidatorException, BridgeDBException {
        URI uri = getPossibleObject(finder, subject, predicate);
        if (uri == null){
            throw new BridgeDBException ("No statements found for subject " + subject + " and predicate " + predicate);
        } 
        return uri;
    }

    protected final URI getPossibleObject(PreviewHandler finder, Resource subject, URI predicate) 
            throws VoidValidatorException, BridgeDBException {
        Value value = getPossibleValue(finder, subject, predicate);
        return getUri(value);
    }

    protected final URI getPossibleObject(PreviewHandler finder, Resource subject, URI predicateMain, URI predicateBackup) 
            throws VoidValidatorException, BridgeDBException {
        URI uri = getPossibleObject(finder, subject, predicateMain);
        if (uri != null){
            return uri;
        }
        return getPossibleObject(finder, subject, predicateBackup);
    }

    protected final URI getObject(PreviewHandler finder, Resource subject, URI predicateMain, URI predicateBackup) 
            throws VoidValidatorException, BridgeDBException {
        URI uri = getPossibleObject(finder, subject, predicateMain, predicateBackup);
        if (uri == null){
            throw new BridgeDBException ("No statements found for subject " + subject + 
                    " and predicate " + predicateMain + " or " + predicateBackup);
        }  
        return uri;
    }
    
    protected final Resource getLinksetId(PreviewHandler finder) throws BridgeDBException{
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
        URI context = new URIImpl(uri);
        PreviewHandler finder = Loader.this.getPreviewHandler(uri, rdfFormatName);
        RdfParserIMS parser = getParser(context, finder, null);
        parser.parse(uri, rdfFormatName);
        return parser.getMappingsetId();       
    }

    public int load(File file) throws VoidValidatorException, BridgeDBException{
        URI context = UriFileMapper.getUri(file);
        return load(file, context);
    }
    
    public int load(File file, URI context) throws VoidValidatorException, BridgeDBException{
        return load (file, context, null, null);
    }
    
    public int load(File file, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
        URI context = UriFileMapper.getUri(file);
        return load(file, context, rdfFormatName, null);
    }
    
    public int load(File file, URI context, String rdfFormatName, Boolean symmetric) 
            throws VoidValidatorException, BridgeDBException{
        PreviewHandler finder = getPreviewHandler(context.stringValue(), file, rdfFormatName);
        RdfParserIMS parser = getParser(context , finder, symmetric);
        parser.parse(context.stringValue(), file, rdfFormatName);
        return parser.getMappingsetId();       
    }

    public RdfParserIMS getParser(URI context, PreviewHandler finder, Boolean symmetric) throws VoidValidatorException, BridgeDBException{
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
        Resource linksetId;
        URI linkPredicate;
        URI subjectTarget;
        URI objectTarget;
        String rawJustification;
        Value isSymetric;
        if (statement != null){
            linksetId  = getObject(statement);
        } else {
            linksetId = getLinksetId(finder);

        }
        linkPredicate = getObject(finder, linksetId, VoidConstants.LINK_PREDICATE);
        rawJustification = getObject(finder, linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES).stringValue();  
        isSymetric = getPossibleValue(finder, linksetId, BridgeDBConstants.IS_SYMETRIC);
        subjectTarget = getObject(finder, linksetId, VoidConstants.SUBJECTSTARGET);
        objectTarget = getObject(finder, linksetId, VoidConstants.OBJECTSTARGET);
        Boolean mergedSymetric = mergeSymetric(context, symmetric, isSymetric);
        ImsHandler handler;
        if (mergedSymetric == null){
            OpsJustificationMaker opsJustificationMaker = OpsJustificationMaker.getInstance();
            String forwardJustification = opsJustificationMaker.getForward(rawJustification); //getInverseJustification(justification);  
            String backwardJustification = opsJustificationMaker.getInverse(rawJustification); //getInverseJustification(justification);  
            if (forwardJustification.equals(backwardJustification)){
                handler = new ImsHandler(reader, context, imsMapper, linkPredicate, rawJustification, 
                        context, true);
            } else {
                handler = new ImsHandler(reader, context, imsMapper, linkPredicate, forwardJustification, 
                        backwardJustification, context);
            }
        } else {
            handler = new ImsHandler(reader, context, imsMapper, linkPredicate, rawJustification, 
                context, mergedSymetric.booleanValue());
        }
        //ImsRdfHandler combinedHandler = 
        //        new ImsRdfHandler(linksetHandler, readerHandler, linkPredicate);
        return new RdfParserIMS(handler);
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
        } else if (value == null){
            return null;
        } else {
            throw new BridgeDBException("Found " + value + " but it is not a URI.");
        }
    }

   void recover() throws BridgeDBException {
        imsMapper.recover();
    }
    
   void closeInput() throws BridgeDBException {
        imsMapper.closeInput();
    }
}
