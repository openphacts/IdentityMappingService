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
import java.util.List;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DCTermsConstants;
import org.bridgedb.rdf.constants.DCatConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.rdf.constants.PavConstants;
import org.bridgedb.rdf.constants.XMLSchemaConstants;
import org.bridgedb.sql.justification.JustificationMaker;
import org.bridgedb.sql.justification.OpsJustificationMaker;
import org.bridgedb.uri.tools.RegexUriPattern;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import uk.ac.manchester.cs.datadesc.validator.constants.VoidConstants;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.ImsHandler1;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.PreviewHandler1;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsMapper;

public class LinksetLoader 
{
    private final ImsMapper imsMapper;
    private final RdfReader reader;        
    private PreviewHandler1 finder;
    
    private final URI context;   
    private URI linksetId;
    
    private static final Value ANY_OBJECT = null;
    
    public static int load(String uri) throws VoidValidatorException, BridgeDBException{
        return load(uri, null);
    }
    
    public static int load(File file) throws VoidValidatorException, BridgeDBException{
        URI context = UriFileMapper.getUri(file);
        return load(file, context);
    }
    
    public static int load(File file, URI context) throws VoidValidatorException, BridgeDBException{
        return load (file, context, null);
    }
    
    public static int load(File file, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
        URI context = UriFileMapper.getUri(file);
        return load(file, context, rdfFormatName);
    }
    
    public static int load(String uri, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
        URI context = new URIImpl(uri);
        LinksetLoader loader = new LinksetLoader(context);
        loader.getPreviewHandler(uri, rdfFormatName);
        RdfParserIMS1 parser = loader.getParser();
        parser.parse(uri, rdfFormatName);
        return parser.getMappingsetId();       
    }

    public static int load(File file, URI context, String rdfFormatName) 
            throws VoidValidatorException, BridgeDBException{
        LinksetLoader loader = new LinksetLoader(context);
        loader.getPreviewHandler(context.stringValue(), file, rdfFormatName);
        RdfParserIMS1 parser = loader.getParser();
        parser.parse(context.stringValue(), file, rdfFormatName);
        return parser.getMappingsetId();       
    }

    protected LinksetLoader(URI context) throws BridgeDBException {
        imsMapper = ImsMapper.getExisting();
        reader = RdfFactoryIMS.getReader();
        UriPattern.refreshUriPatterns();
        this.context = context;
    }
    
    protected final void getPreviewHandler(String uri, String rdfFormatName) throws BridgeDBException{
        finder = new PreviewHandler1();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(uri, rdfFormatName);
    }
    
    private void getPreviewHandler(String baseURI, File file, String rdfFormatName) throws BridgeDBException{
        finder = new PreviewHandler1();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(baseURI, file, rdfFormatName);
    }

    private RdfParserIMS1 getParser() throws VoidValidatorException, BridgeDBException{
        URI linksetPredicate;
      
        String forwardJustification;
        String backwardJustification;  
        boolean isSymetric;

        Statement statement =  finder.getInVoidStatement();
        if (statement != null){
            linksetId = getObjectURI(statement);
            List<Statement> statementList = reader.getStatementList(linksetId, VoidConstants.LINK_PREDICATE, ANY_OBJECT);
            linksetPredicate = getObjectURI(statementList);
            statementList = reader.getStatementList(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, ANY_OBJECT);
            if (statementList.isEmpty()){
                statementList = reader.getStatementList(linksetId, DulConstants.EXPRESSES, ANY_OBJECT);
            }
            forwardJustification = getObjectURI(statementList).stringValue();
        } else {
            statement =  finder.getLinkPredicateStatement();
            linksetId = getSubjectURI(statement);
            linksetPredicate = getObjectURI(statement);
            statement =  finder.getJustificationStatement();
            forwardJustification = getObjectURI(statement).stringValue();
        }
        
        ImsHandler1 handler = new ImsHandler1(imsMapper, linksetPredicate, forwardJustification, context);
        return new RdfParserIMS1(handler);
    }

    private URI getSubjectURI(Statement statement) throws BridgeDBException{
        if (statement.getSubject() instanceof URI){
            return (URI)statement.getSubject();
        } else {
            throw new BridgeDBException("Found statement " + statement + " but subject is not a URI.");
        }
    }

    private URI getObjectURI(Statement statement) throws BridgeDBException{
        if (statement.getObject() instanceof URI){
            return (URI)statement.getObject();
        } else {
            throw new BridgeDBException("Found statement " + statement + " but object is not a URI.");
        }
    }

    private URI getObjectURI(Collection<Statement> statements) throws BridgeDBException{
        if (statements.size() == 1){
            return getObjectURI(statements.iterator().next());
        } else {
            throw new BridgeDBException ("Found " + statements.size() + " where 1 expected");
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

    static void recover() throws BridgeDBException {
       LinksetLoader loader = new LinksetLoader(null);
       loader.imsMapper.recover();
    }
    
    void closeInput() throws BridgeDBException {
        imsMapper.closeInput();
    }

}
