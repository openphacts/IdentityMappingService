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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DCTermsConstants;
import org.bridgedb.rdf.constants.DCatConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.rdf.constants.PavConstants;
import org.bridgedb.sql.justification.OpsJustificationMaker;
import org.bridgedb.uri.tools.RegexUriPattern;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.CalendarLiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import uk.ac.manchester.cs.datadesc.validator.constants.VoidConstants;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.ImsHandler;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.PreviewHandler;
import uk.ac.manchester.cs.openphacts.ims.mapper.ImsMapper;

public class Loader 
{
    protected final RdfReader reader;
    protected final ImsMapper imsMapper;
            
    public static final Set<URI> LINKSET_PREDICATES = new HashSet<URI>();
    public static final Set<URI> DATASET_PREDICATES = new HashSet<URI>();
    public static final Set<URI> DISTRIBUTION_PREDICATES = new HashSet<URI>();
    public static final Set<URI> MULTIPLE_PREDICATES = new HashSet<URI>();

    private final HashMap<URI, Set<Statement>> savedStatements = new HashMap<URI, Set<Statement>>();    
    protected PreviewHandler finder;

    private final URI context;
    
    private Resource linksetId;
    private String linksetTitle;
    private String linksetDescription;
    private URI linksetPublisher;
    private URI linksetLicense;
    private CalendarLiteralImpl linksetIssued;
    private URI linksetDataDump;
    private URI linksetSubjectsTarget;
    private URI linksetSubjectsDatatype;
    private URI linksetObjectsTarget;
    private URI linksetObjectsDatatype;
    private URI linksetPredicate;
    private URI linksetJustification;
    private URI linksetAssertionMethod;
    private Value linksetIsSymetric;
    private boolean isSymetric;
    
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
        DATASET_PREDICATES.add(DCTermsConstants.LICENSE_URI);
        DATASET_PREDICATES.add(PavConstants.VERSION);
        DATASET_PREDICATES.add(DCatConstants.DISTRIBUTION_URI);
        DATASET_PREDICATES.add(VoidConstants.URI_SPACE_URI);
        DATASET_PREDICATES.add(VoidConstants.URI_REGEX_PATTERN);
        DISTRIBUTION_PREDICATES.add(PavConstants.VERSION);
        DISTRIBUTION_PREDICATES.add(DCatConstants.BYTE_SIZE_URI);
        MULTIPLE_PREDICATES.addAll(DATASET_PREDICATES);
        MULTIPLE_PREDICATES.addAll(DISTRIBUTION_PREDICATES);
    }
    
    public static int load(String uri) throws VoidValidatorException, BridgeDBException{
        return load(uri, null);
    }
    
    public static int load(String uri, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
        URI context = new URIImpl(uri);
        Loader loader = new Loader(context);
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
        Loader loader = new Loader(context);
        loader.getPreviewHandler(context.stringValue(), file, rdfFormatName);
        RdfParserIMS parser = loader.getParser(context);
        parser.parse(context.stringValue(), file, rdfFormatName);
        return parser.getMappingsetId();       
    }


    protected Loader() throws BridgeDBException {
        this(null);
    }
    
    protected Loader(URI context) throws BridgeDBException {
        imsMapper = ImsMapper.getExisting();
        reader = RdfFactoryIMS.getReader();
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

    protected final Collection<Statement> getStatements(Resource subject, URI... predicates) 
            throws BridgeDBException, VoidValidatorException{
        for (URI predicate:predicates){
            Collection<Statement> statements = finder.getStatementList(subject, predicate);
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
    
    protected final Value getSingleValue(Resource subject, URI... predicates) throws BridgeDBException, VoidValidatorException{
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
    
    protected final Value getPossibleValue(Resource subject, URI... predicates) 
            throws VoidValidatorException, BridgeDBException {
        Collection<Statement> statements = getStatements(subject, predicates);
        if (statements == null || statements.isEmpty()){
            return null;
        }
        return statements.iterator().next().getObject();
    }

    protected final URI getSingleURI(Resource subject, URI... predicates) 
            throws VoidValidatorException, BridgeDBException {
        Value value = getSingleValue(subject, predicates);
        if (value instanceof URI){
            return (URI)value;
        }
        throw new BridgeDBException("Founnd none URI " + value + " object " + withInfo(subject, predicates));
    }

    protected final URI getPossibleURI(Resource subject, URI... predicates) 
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

    protected final CalendarLiteralImpl getPossibleCalendar(Resource subject, URI... predicates) 
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
    
    protected final String getPossibleString(Resource subject, URI predicate) 
            throws VoidValidatorException, BridgeDBException {
        Value value = getPossibleValue(subject, predicate);
        if (value == null){
            return null;
        }
        return value.stringValue();
    }

 /*   protected final URI getPossibleObject(Resource subject, URI predicateMain, URI predicateBackup) 
            throws VoidValidatorException, BridgeDBException {
        URI uri = getPossibleObject(subject, predicateMain);
        if (uri != null){
            return uri;
        }
        return getPossibleObject(subject, predicateBackup);
    }

    protected final URI getObject(Resource subject, URI predicateMain, URI predicateBackup) 
            throws VoidValidatorException, BridgeDBException {
        URI uri = getPossibleObject(subject, predicateMain, predicateBackup);
        if (uri == null){
            throw new BridgeDBException ("No statements found for subject " + subject + 
                    " and predicate " + predicateMain + " or " + predicateBackup);
        }  
        return uri;
    }
   */ 
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
    
    private void loadLinksetData() throws BridgeDBException, VoidValidatorException{
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
        if (statement != null){
            linksetId  = getObject(statement);
        } else {
            linksetId = getLinksetId();
        }
        linksetIsSymetric = getPossibleValue(linksetId, BridgeDBConstants.IS_SYMETRIC);
        if (linksetIsSymetric == null){
            isSymetric = true;
        } else if (linksetIsSymetric instanceof Literal){
                Literal literal = (Literal)linksetIsSymetric;
                isSymetric = literal.booleanValue();
        } else {
            throw new BridgeDBException ("Reading " + context + " unexpected object " + linksetIsSymetric  
                    + " found with predicate " + BridgeDBConstants.IS_SYMETRIC);
        }
        linksetTitle = getPossibleString(linksetId, DCTermsConstants.TITLE_URI);
        linksetDescription = getPossibleString(linksetId, DCTermsConstants.DESCRIPTION_URI);
        linksetPublisher = getPossibleURI(linksetId,  DCTermsConstants.PUBLISHER_URI);
        linksetLicense = getPossibleURI(linksetId, DCTermsConstants.LICENSE_URI);
        linksetIssued = this.getPossibleCalendar(linksetId, DCTermsConstants.ISSUED_URI);
        linksetDataDump = getPossibleURI(linksetId, VoidConstants.DATA_DUMP);
        linksetSubjectsTarget = getSingleURI(linksetId, VoidConstants.SUBJECTSTARGET);
        //private URI linksetSubjectsDatatype;
        linksetObjectsTarget = getSingleURI(linksetId, VoidConstants.OBJECTSTARGET);
        //private URI linksetObjectsDatatype;
        linksetPredicate = getSingleURI(linksetId, VoidConstants.LINK_PREDICATE);
        linksetJustification = getSingleURI(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES);
        //private URI linksetAssertionMethod;
    }
    
    private int registerLinkset() throws BridgeDBException{
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

    public RdfParserIMS getParser(URI context) throws VoidValidatorException, BridgeDBException{
        loadLinksetData();
        int mappingSetId = registerLinkset();
        ImsHandler handler = new ImsHandler(imsMapper, linksetPredicate, isSymetric, mappingSetId, reader, context); 
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
       Loader loader = new Loader(null);
       loader.imsMapper.recover();
    }
    
    void closeInput() throws BridgeDBException {
        imsMapper.closeInput();
    }

    private String withInfo(Resource subject, URI[] predicates) {
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
