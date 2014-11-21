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
import uk.ac.manchester.cs.openphacts.ims.loader.handler.ImsHandler;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.PreviewHandler;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsMapper;

public class OldLoader 
{
    protected final ImsMapper imsMapper;
            
    public static final Map<URI, URI> LINKSET_MUST = new HashMap<URI, URI>();
    public static final Map<URI, URI> LINKSET_SHOULD = new HashMap<URI, URI>();
    public static final Map<URI, URI> LINKSET_MAY = new HashMap<URI, URI>();
    public static final Map<URI, URI> DATASET_MUST = new HashMap<URI, URI>();
    public static final Map<URI, URI> DATASET_SHOULD = new HashMap<URI, URI>();
    public static final Map<URI, URI> DATASET_MAY = new HashMap<URI, URI>();
    public static final Map<URI, URI> DISTRIBUTION_MUST = new HashMap<URI, URI>();
    public static final Map<URI, URI> DISTRIBUTION_SHOULD = new HashMap<URI, URI>();
    public static final Map<URI, URI> DISTRIBUTION_MAY = new HashMap<URI, URI>();

    public static final Set<URI> DATASET_PREDICATES = new HashSet<URI>();
    public static final Set<URI> DISTRIBUTION_PREDICATES = new HashSet<URI>();
    public static final Set<URI> MULTIPLE_PREDICATES = new HashSet<URI>();

    private final HashMap<URI, Set<Statement>> savedStatements = new HashMap<URI, Set<Statement>>();    
    protected PreviewHandler finder;

    private final URI context;
    
    private URI linksetPredicate;
    private boolean isSymetric;
    
           //linksetPublisher = finder.getPossibleURI(linksetId,  DCTermsConstants.PUBLISHER_URI);
        //linksetLicense = finder.getPossibleURI(linksetId, DCTermsConstants.LICENSE_URI);
        //linksetIssued = finder.getPossibleCalendar(linksetId, DCTermsConstants.ISSUED_URI);
        //linksetDataDump = finder.getPossibleURI(linksetId, VoidConstants.DATA_DUMP);
        //private URI linksetAssertionMethod;

    
    static {
        //These are ones where there may be more than one values so not stored directly
        LINKSET_MUST.put(DCTermsConstants.PUBLISHER_URI, XMLSchemaConstants.ANY_URI);
        LINKSET_MUST.put(DCTermsConstants.LICENSE_URI, XMLSchemaConstants.ANY_URI);
        LINKSET_MUST.put(DCTermsConstants.ISSUED_URI, XMLSchemaConstants.DATE_TIME);
        LINKSET_MUST.put(VoidConstants.DATA_DUMP, XMLSchemaConstants.ANY_URI);
        LINKSET_MUST.put(BridgeDBConstants.ASSERTION_METHOD, XMLSchemaConstants.ANY_URI);
        
        //Currently only should values are subset which is used to get other values not stored 
        //And species which are stored directly
        
        LINKSET_MAY.put(PavConstants.AUTHORED_BY, XMLSchemaConstants.ANY_URI);
        LINKSET_MAY.put(PavConstants.AUTHORED_ON, XMLSchemaConstants.DATE_TIME);
        LINKSET_MAY.put(PavConstants.CREATED_BY, XMLSchemaConstants.ANY_URI);
        LINKSET_MAY.put(PavConstants.CREATED_ON, XMLSchemaConstants.DATE_TIME);
        LINKSET_MAY.put(PavConstants.CREATED_WITH, XMLSchemaConstants.ANY_URI);

        DATASET_PREDICATES.add(DCTermsConstants.TITLE_URI);
        DATASET_PREDICATES.add(DCTermsConstants.DESCRIPTION_URI);
        DATASET_PREDICATES.add(PavConstants.VERSION);
        DATASET_PREDICATES.add(DCatConstants.DISTRIBUTION_URI);
        
        DATASET_MUST.put(DCTermsConstants.PUBLISHER_URI, XMLSchemaConstants.ANY_URI);
        DATASET_MUST.put(DCatConstants.LANDING_PAGE_URI, XMLSchemaConstants.ANY_URI);
        DATASET_MUST.put(DCTermsConstants.LICENSE_URI, XMLSchemaConstants.ANY_URI);
        DATASET_MUST.put(DCTermsConstants.ISSUED_URI, XMLSchemaConstants.DATE_TIME);
        //While listed in the MUST section it is only must if RDF Data
        DATASET_MAY.put(VoidConstants.DATA_DUMP, XMLSchemaConstants.DATE_TIME);
        
        DATASET_SHOULD.put(PavConstants.PREVIOUS_VERSION, XMLSchemaConstants.ANY_URI);
        DATASET_SHOULD.put(DCTermsConstants.ACCRUAL_PERIODICITY_URI, XMLSchemaConstants.ANY_URI);
        //While listed in the SHOULD section it is only must if RDF conversions
            DATASET_MAY.put(PavConstants.IMPORTED_FROM, XMLSchemaConstants.ANY_URI);
            DATASET_MAY.put(PavConstants.IMPORTED_BY, XMLSchemaConstants.ANY_URI);
            DATASET_MAY.put(PavConstants.LAST_REFERSHED_ON, XMLSchemaConstants.DATE_TIME);
            DATASET_MAY.put(PavConstants.CREATED_WITH, XMLSchemaConstants.ANY_URI);
            DATASET_MAY.put(PavConstants.DERIVED_FROM, XMLSchemaConstants.ANY_URI);
        DATASET_SHOULD.put(VoidConstants.EXAMPLE_RESOURCE, XMLSchemaConstants.ANY_URI);
        
        DATASET_MAY.put(VoidConstants.SPARQL_ENDPOINT, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(VoidConstants.URI_SPACE_URI, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(VoidConstants.URI_REGEX_PATTERN, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(VoidConstants.VOCABULARY, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(PavConstants.AUTHORED_BY, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(PavConstants.AUTHORED_ON, XMLSchemaConstants.DATE_TIME);
        DATASET_MAY.put(PavConstants.CREATED_BY, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(PavConstants.CREATED_ON, XMLSchemaConstants.DATE_TIME);
        DATASET_MAY.put(DCatConstants.THEME_URI, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(CitoConstants.CITE_AS_AUTHORITY_URI, XMLSchemaConstants.ANY_URI);
        DATASET_MAY.put(FoafConstants.PAGE_URI, XMLSchemaConstants.ANY_URI);
        
        DISTRIBUTION_PREDICATES.add(PavConstants.VERSION);
        DISTRIBUTION_PREDICATES.add(DCatConstants.BYTE_SIZE_URI);
        
        DISTRIBUTION_MUST.put(DCatConstants.MEDIA_TYPE_URI, XMLSchemaConstants.ANY_URI);
        DISTRIBUTION_MUST.put(DCatConstants.DOWNLOAD_URI, XMLSchemaConstants.ANY_URI);

        DISTRIBUTION_SHOULD.put(DCTermsConstants.ISSUED_URI, XMLSchemaConstants.DATE_TIME);
        DISTRIBUTION_SHOULD.put(PavConstants.PREVIOUS_VERSION, XMLSchemaConstants.ANY_URI);
              
        MULTIPLE_PREDICATES.addAll(LINKSET_MUST.keySet());
        MULTIPLE_PREDICATES.addAll(LINKSET_SHOULD.keySet());
        MULTIPLE_PREDICATES.addAll(LINKSET_MAY.keySet());
        MULTIPLE_PREDICATES.addAll(DATASET_PREDICATES);
        MULTIPLE_PREDICATES.addAll(DATASET_MUST.keySet());
        MULTIPLE_PREDICATES.addAll(DATASET_SHOULD.keySet());
        MULTIPLE_PREDICATES.addAll(DATASET_MAY.keySet());
        MULTIPLE_PREDICATES.addAll(DISTRIBUTION_PREDICATES);
        MULTIPLE_PREDICATES.addAll(DISTRIBUTION_MUST.keySet());
        MULTIPLE_PREDICATES.addAll(DISTRIBUTION_SHOULD.keySet());
        MULTIPLE_PREDICATES.addAll(DISTRIBUTION_MAY.keySet());
    }
    
    public static int load(String uri) throws VoidValidatorException, BridgeDBException{
        return load(uri, null);
    }
    
    public static int load(String uri, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
        URI context = new URIImpl(uri);
        OldLoader loader = new OldLoader(context);
        loader.getPreviewHandler(uri, rdfFormatName);
        RdfParserIMS parser = loader.getParser(context);
        parser.parse(uri, rdfFormatName);
        return parser.getMappingsetId();       
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
    
    public static int load(File file, URI context, String rdfFormatName) 
            throws VoidValidatorException, BridgeDBException{
        OldLoader loader = new OldLoader(context);
        loader.getPreviewHandler(context.stringValue(), file, rdfFormatName);
        RdfParserIMS parser = loader.getParser(context);
        parser.parse(context.stringValue(), file, rdfFormatName);
        return parser.getMappingsetId();       
    }


    protected OldLoader() throws BridgeDBException {
        this(null);
    }
    
    protected OldLoader(URI context) throws BridgeDBException {
        imsMapper = ImsMapper.getExisting();
        UriPattern.refreshUriPatterns();
        this.context = context;
    }
    
    protected final void getPreviewHandler(String uri, String rdfFormatName) throws BridgeDBException{
        finder = new PreviewHandler(MULTIPLE_PREDICATES);
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(uri, rdfFormatName);
    }
    
    private void getPreviewHandler(String baseURI, File file, String rdfFormatName) throws BridgeDBException{
        finder = new PreviewHandler(MULTIPLE_PREDICATES);
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
    
    private int loadLinksetData() throws BridgeDBException, VoidValidatorException{
        //Get the LinksetID
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
        URI linksetId;
        if (statement != null){
            linksetId  = getObject(statement);
        } else {
            statement =  finder.getSinglePredicateStatements(VoidConstants.LINK_PREDICATE);
            if (statement == null){
                throw new BridgeDBException ("No statement found with either " + VoidConstants.IN_DATASET +
                        " or " + VoidConstants.LINK_PREDICATE);
            } else { 
                linksetId = (URI)statement.getSubject();
            }
        }
       
        //Get the data needed to register the linkset
        linksetPredicate = finder.getSingleURI(linksetId, VoidConstants.LINK_PREDICATE);
        URI linksetJustification = finder.getSingleURI(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES);
        Value linksetIsSymetric = finder.getPossibleValue(linksetId, BridgeDBConstants.IS_SYMETRIC);
        if (linksetIsSymetric == null){
            isSymetric = true;
        } else if (linksetIsSymetric instanceof Literal){
                Literal literal = (Literal)linksetIsSymetric;
                isSymetric = literal.booleanValue();
        } else {
            throw new BridgeDBException ("Reading " + context + " unexpected object " + linksetIsSymetric  
                    + " found with predicate " + BridgeDBConstants.IS_SYMETRIC);
        }
        //When used void:uriSpace or void:uriRegexPattern should be obtained here
        int mappingSetId = registerLinkset(linksetPredicate, linksetJustification, isSymetric);

        //Get the data specifically stroed with the linkset
        //Either because there can be only one or because it is a String
        String linksetTitle = finder.getPossibleString(linksetId, DCTermsConstants.TITLE_URI);
        String linksetDescription = finder.getPossibleString(linksetId, DCTermsConstants.DESCRIPTION_URI);
        URI linksetSubjectsTarget = finder.getSingleURI(linksetId, VoidConstants.SUBJECTSTARGET);
        URI linksetSubjectsType = finder.getPossibleURI(linksetId, BridgeDBConstants.SUBJECTS_DATATYPE);
        URI linksetObjectsTarget = finder.getSingleURI(linksetId, VoidConstants.OBJECTSTARGET);
        URI linksetObjectsType = finder.getPossibleURI(linksetId, BridgeDBConstants.OBJECTS_DATATYPE);
        URI linksetSubjectSpecies = finder.getPossibleURI(linksetId, BridgeDBConstants.SUBJECTS_SPECIES);
        URI linksetObjectsSpecies = finder.getPossibleURI(linksetId, BridgeDBConstants.OBJECTS_SPECIES);
        proecessLinksetVoid(mappingSetId, linksetId, linksetTitle, linksetDescription, linksetSubjectsTarget, 
                linksetSubjectsType, linksetObjectsTarget, linksetObjectsType, linksetSubjectSpecies, 
                linksetObjectsSpecies, isSymetric);
        
        for (URI predicate:LINKSET_MUST.keySet()){
            Set<Statement> statements = finder.getStatementList(linksetId, predicate);
            for (Statement aStatement:statements){
                processLinkSetMust(linksetId, predicate, aStatement.getObject(), LINKSET_MUST.get(predicate));
            }
        }

        for (URI predicate:LINKSET_SHOULD.keySet()){
            Set<Statement> statements = finder.getStatementList(linksetId, predicate);
            for (Statement aStatement:statements){
                processLinkSetShould(linksetId, predicate, aStatement.getObject(), LINKSET_SHOULD.get(predicate));
            }
        }

        for (URI predicate:LINKSET_MAY.keySet()){
            Set<Statement> statements = finder.getStatementList(linksetId, predicate);
            for (Statement aStatement:statements){
                System.out.println(aStatement);
                processLinkSetMay(linksetId, predicate, aStatement.getObject(), LINKSET_MAY.get(predicate));
            }
        }

        return mappingSetId;
    }
    
    private void loadDatasetData(URI dataSetId) throws BridgeDBException, VoidValidatorException{
         
        //Get the data specifically stroed with the linkset
        //Either because there can be only one or because it is a String
        String datasetTitle = finder.getPossibleString(dataSetId, DCTermsConstants.TITLE_URI);
        String datasetDescription = finder.getPossibleString(dataSetId, DCTermsConstants.DESCRIPTION_URI);
        String datasetVersion = finder.getPossibleString(dataSetId, PavConstants.VERSION);
        URI datasetDistribution = finder.getSingleURI(dataSetId, DCatConstants.DESCRIPTION_URI);
        processDatasetVoid(dataSetId, datasetTitle, datasetDescription, datasetVersion, datasetDistribution);  
        
        for (URI predicate:DATASET_MUST.keySet()){
            Set<Statement> statements = finder.getStatementList(dataSetId, predicate);
            for (Statement aStatement:statements){
                processDataSetMust(dataSetId, predicate, aStatement.getObject(), DATASET_MUST.get(predicate));
            }
        }

        for (URI predicate:DATASET_SHOULD.keySet()){
            Set<Statement> statements = finder.getStatementList(dataSetId, predicate);
            for (Statement aStatement:statements){
                processDataSetShould(dataSetId, predicate, aStatement.getObject(), DATASET_SHOULD.get(predicate));
            }
        }

        for (URI predicate:DATASET_MAY.keySet()){
            Set<Statement> statements = finder.getStatementList(dataSetId, predicate);
            for (Statement aStatement:statements){
                System.out.println(aStatement);
                processDataSetMay(dataSetId, predicate, aStatement.getObject(), DATASET_MAY.get(predicate));
            }
        }
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
    
    /**
     * Pass through method to handle the basic linkset information.
     * 
     * This version just send the info to the mapper for loading.
     * 
     * Could be overwritten by a validator ski
     * @param mappingSetId
     * @param linksetId
     * @param linksetTitle
     * @param linksetDescription
     * @param linksetSubjectsTarget
     * @param linksetSubjectsType
     * @param linksetObjectsTarget
     * @param linksetObjectsType
     * @param linksetSubjectSpecies
     * @param linksetObjectsSpecies 
     */
    protected void proecessLinksetVoid(int mappingSetId, URI linksetId, String linksetTitle, String linksetDescription, 
            URI linksetSubjectsTarget, URI linksetSubjectsType, URI linksetObjectsTarget, URI linksetObjectsType, 
            URI linksetSubjectSpecies, URI linksetObjectsSpecies, boolean isSymetric) throws BridgeDBException{
        imsMapper.addLinksetVoid(mappingSetId, linksetId, linksetTitle, linksetDescription, 
                linksetSubjectsTarget, linksetSubjectsType, linksetObjectsTarget, linksetObjectsType, 
                linksetSubjectSpecies, linksetObjectsSpecies);
        if (isSymetric){
            imsMapper.addLinksetVoid(mappingSetId + 1, linksetId, linksetTitle, linksetDescription, 
                    linksetSubjectsTarget, linksetSubjectsType, linksetObjectsTarget, linksetObjectsType, 
                    linksetSubjectSpecies, linksetObjectsSpecies);
        }
    }
    
    private void processDatasetVoid(URI dataSetId, String datasetTitle, String datasetDescription, String datasetVersion, 
            URI datasetDistribution) throws BridgeDBException {
        imsMapper.addDatasetVoid(dataSetId, datasetTitle, datasetDescription, datasetVersion, datasetDistribution);
    }

    protected void processLinkSetMust(URI linksetId, URI predicate, Value object, URI type) throws BridgeDBException {
        loadStatement(linksetId, predicate, object, type);
    }

    protected void processLinkSetShould(URI linksetId, URI predicate, Value object, URI type) throws BridgeDBException {
        loadStatement(linksetId, predicate, object, type);
    }

    protected void processLinkSetMay(URI linksetId, URI predicate, Value object, URI type) throws BridgeDBException {
        loadStatement(linksetId, predicate, object, type);
    }

    protected void processDataSetMust(URI linksetId, URI predicate, Value object, URI type) throws BridgeDBException {
        loadStatement(linksetId, predicate, object, type);
    }

    protected void processDataSetShould(URI linksetId, URI predicate, Value object, URI type) throws BridgeDBException {
        loadStatement(linksetId, predicate, object, type);
    }

    protected void processDataSetMay(URI linksetId, URI predicate, Value object, URI type) throws BridgeDBException {
        loadStatement(linksetId, predicate, object, type);
    }

    private void loadStatement(URI linksetId, URI predicate, Value object, URI type) throws BridgeDBException {
        if (type.equals(XMLSchemaConstants.ANY_URI)){
            if (object instanceof URI){
                URI uri = (URI)object;
                imsMapper.loadStatement(linksetId, predicate, uri);
            } else {
                throw new BridgeDBException("None URI type found for " + linksetId + " " + predicate + " " 
                    + object + ". Found " + object.getClass());            
            }
        } else if (type.equals(XMLSchemaConstants.DATE_TIME)){
            if (object instanceof Literal){
                Literal literal = (Literal)object;
                XMLGregorianCalendar dataTime = literal.calendarValue();
                if (dataTime != null){
                    imsMapper.loadStatement(linksetId, predicate, dataTime);
                } else {
                    throw new BridgeDBException("No DateTime value obtained form " + linksetId + " " + predicate + " " 
                        + object + ". Found " + object.getClass());            
                }
            } else {
                throw new BridgeDBException("None Literal type found for " + linksetId + " " + predicate + " " 
                    + object + ". Found " + object.getClass());            
            }
        } else {
            throw new BridgeDBException("Incorrect type found for " + linksetId + " " + predicate + " " 
                    + object + ". Expected type " + type + " found " + object.getClass());
        }
    }
    
    public RdfParserIMS getParser(URI context) throws VoidValidatorException, BridgeDBException{
        int mappingSetId = loadLinksetData();
        ImsHandler handler = new ImsHandler(imsMapper, linksetPredicate, isSymetric, mappingSetId, context); 
        return new RdfParserIMS(handler);
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

    static void recover() throws BridgeDBException {
       OldLoader loader = new OldLoader(null);
       loader.imsMapper.recover();
    }
    
    void closeInput() throws BridgeDBException {
        imsMapper.closeInput();
    }


}
