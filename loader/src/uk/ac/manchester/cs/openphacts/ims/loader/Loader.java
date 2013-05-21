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
import java.util.List;
import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.LinksetListener;
import org.bridgedb.rdf.BridgeDBRdfHandler;
import org.bridgedb.rdf.constants.VoidConstants;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.uri.UriListener;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfFactory;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.validator.Validator;
import uk.ac.manchester.cs.openphacts.validator.ValidatorImpl;

public class Loader 
{
    private final Validator validator;
    private final LinksetListener linksetListener;
    private final RdfReader reader;
            
    public Loader(StoreType storeType) throws BridgeDBException {
        validator = new ValidatorImpl();
        UriListener uriListener = SQLUriMapper.factory(false, storeType);
        linksetListener = new LinksetListenerPlus(uriListener);
        try {
            if (storeType == StoreType.TEST){
                reader = RdfFactory.getTestFilebase();
            } else {
                reader = RdfFactory.getImsFilebase();
            }
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException("Unable to get RDFReader.", ex);
        }
        BridgeDBRdfHandler.init();
    }
    
    private PredicateFinder getPredicateFinder(String uri) throws BridgeDBException{
        PredicateFinder finder = new PredicateFinder();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(uri);
        return finder;
    }
    
    private PredicateFinder getPredicateFinder(File file) throws BridgeDBException{
        PredicateFinder finder = new PredicateFinder();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(file);
        return finder;
    }

    private URI getObject(PredicateFinder finder, URI predicate) throws BridgeDBException{
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
    
    private Resource getLinksetId(PredicateFinder finder) throws BridgeDBException{
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.LINK_PREDICATE);
        if (statement != null){
            return statement.getSubject();
        }
        statement =  finder.getSinglePredicateStatements(DulConstants.EXPRESSES);
        if (statement != null){
            return statement.getSubject();
        }
        throw new BridgeDBException("Unable to get LinksetrId");
    }
    
    public LoaderResult load(String uri, String formatName) throws VoidValidatorException, BridgeDBException{
        PredicateFinder finder = getPredicateFinder(uri);
        URI linkPredicate = getObject(finder, VoidConstants.LINK_PREDICATE);
        String justification = getObject(finder, DulConstants.EXPRESSES).stringValue();
        Resource linksetId = getLinksetId(finder);
        Resource context = reader.loadURI(uri);
        int mappingSetId =  linksetListener.parse(uri, linksetId.stringValue(), linkPredicate, justification);
        return new LoaderResult(mappingSetId, linksetId, context);
    }

    public LoaderResult load(File file, String formatName) throws VoidValidatorException, BridgeDBException{
        PredicateFinder finder = getPredicateFinder(file);
        URI linkPredicate = getObject(finder, VoidConstants.LINK_PREDICATE);
        String justification = getObject(finder, DulConstants.EXPRESSES).stringValue();
        Resource linksetId = getLinksetId(finder);
        System.out.println("linksetId " + linksetId);
        Resource context = reader.loadFile(file);
        List<Statement> 
                //c
        statements = reader.getStatementList(linksetId);
        for (Statement statement:statements){
            System.out.println(statement);
        }
        int mappingSetId = linksetListener.parse(file, linksetId.stringValue(), linkPredicate, justification);
        return new LoaderResult(mappingSetId, linksetId, context);
    }
}
