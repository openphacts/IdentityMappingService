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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.CitoConstants;
import org.bridgedb.rdf.constants.DCTermsConstants;
import org.bridgedb.rdf.constants.DCatConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.rdf.constants.FoafConstants;
import org.bridgedb.rdf.constants.PavConstants;
import org.bridgedb.rdf.constants.XMLSchemaConstants;
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
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.RdfParserPlus;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.ImsHandler;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsMapper;

public class UtilBase 
{
    protected final ImsMapper imsMapper;
            
    public static final Set<URI> MULTIPLE_PREDICATES = new HashSet<URI>();

    private final HashMap<URI, Set<Statement>> savedStatements = new HashMap<URI, Set<Statement>>();    
    protected PreviewHandlerOld finder;

    private final URI context;
    
    private URI linksetPredicate;
    private boolean isSymetric;
    
           //linksetPublisher = finder.getPossibleURI(linksetId,  DCTermsConstants.PUBLISHER_URI);
        //linksetLicense = finder.getPossibleURI(linksetId, DCTermsConstants.LICENSE_URI);
        //linksetIssued = finder.getPossibleCalendar(linksetId, DCTermsConstants.ISSUED_URI);
        //linksetDataDump = finder.getPossibleURI(linksetId, VoidConstants.DATA_DUMP);
        //private URI linksetAssertionMethod;

    protected UtilBase() throws BridgeDBException {
        this(null);
    }
    
    protected UtilBase(URI context) throws BridgeDBException {
        imsMapper = ImsMapper.getExisting();
        UriPattern.refreshUriPatterns();
        this.context = context;
    }
    
    protected final void getPreviewHandler(String uri, String rdfFormatName) throws BridgeDBException{
        finder = new PreviewHandlerOld(MULTIPLE_PREDICATES);
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(uri, rdfFormatName);
    }
    
    private void getPreviewHandler(String baseURI, File file, String rdfFormatName) throws BridgeDBException{
        finder = new PreviewHandlerOld(MULTIPLE_PREDICATES);
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(baseURI, file, rdfFormatName);
    }

    protected final Resource getLinksetId() throws BridgeDBException{
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
    
    private int registerLinkset(URI linksetPredicate, URI linksetJustification, boolean isSymetric) throws BridgeDBException{
        //When required get species and type here too
        //When used void:uriSpace or void:uriRegexPattern should be checked here
        Statement aMapping = finder.getRandomPredicateStatements(linksetPredicate);
        Resource subject = aMapping.getSubject();
        Value object = aMapping.getObject();
        if (!(subject instanceof URI)){
            throw new BridgeDBException ("None URI subject in " + aMapping);
        }
        if (!(object instanceof URI)){
            throw new BridgeDBException ("None URI object in " + aMapping);
        }
        RegexUriPattern sourcePattern = imsMapper.toUriPattern(subject.stringValue());
        if (sourcePattern == null){
            throw new BridgeDBException("Unable to get a pattern for " + subject.stringValue());
        }
        RegexUriPattern targetPattern = imsMapper.toUriPattern(object.stringValue());
        if (targetPattern == null){
            throw new BridgeDBException("Unable to get a pattern for " + object.stringValue());
        }
        String rawJustification = linksetJustification.stringValue();
        OpsJustificationMaker opsJustificationMaker = OpsJustificationMaker.getInstance();
        String forwardJustification = forwardJustification = opsJustificationMaker.getForward(rawJustification); //getInverseJustification(justification);  
        String backwardJustification = opsJustificationMaker.getInverse(rawJustification); //getInverseJustification(justification);  
        if (forwardJustification.equals(backwardJustification)){
            return imsMapper.registerMappingSet(sourcePattern, linksetPredicate.stringValue(), 
                    rawJustification, targetPattern, context, isSymetric);
        }
        return imsMapper.registerMappingSet(sourcePattern, linksetPredicate.stringValue(), 
                forwardJustification, backwardJustification, targetPattern, context);
    }
        
    //public RdfParserIMS getParser(URI context) throws VoidValidatorException, BridgeDBException{
    //    int mappingSetId = loadLinksetData();
    //    ImsHandler handler = new ImsHandler(imsMapper, linksetPredicate, isSymetric, mappingSetId, context); 
    //    return new RdfParserIMS(handler);
    // }
    
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

    static void recover() throws BridgeDBException {
       UtilBase loader = new UtilBase(null);
       loader.imsMapper.recover();
    }
    
    void closeInput() throws BridgeDBException {
        imsMapper.closeInput();
    }


}
