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

import info.aduna.lang.FileFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.rdf.constants.PavConstants;
import org.bridgedb.rdf.constants.VoidConstants;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.Reporter;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.UriFileMapper;
import uk.ac.manchester.cs.openphacts.ims.rdf.RdfFactoryIMS;

/**
 *
 * @author Christian
 */
public class ParentChildCheck extends UtilBase {
    
   public static String DEFAULT_BASE_URI = "http://no/BaseURI/Set/";
   private URI linkPredicate;
   private URI justification;
   private RepositoryConnection repositoryConnection;
   private ArrayList<URI> originals;
   private DataSource sourceDataSource;
   private DataSource targetDataSource;
   private boolean convert = false;
   
   private ParentChildCheck() throws BridgeDBException{
       linkPredicate = null;
       justification = null;
       sourceDataSource = null;
       targetDataSource = null;
       originals = new ArrayList<URI>();
       repositoryConnection = null;
       try {
            Repository repository = new SailRepository(new MemoryStore());
            repository.initialize();
            repositoryConnection = repository.getConnection();
       } catch (Exception ex) {
           throw new BridgeDBException ("Error parsing RDf file ", ex);
       }
   }
   
   private static RDFFormat getFormat(String fileName) throws VoidValidatorException{
        if (fileName.endsWith(".n3")){
            fileName = "try.ttl";
        }
        RDFParserRegistry reg = RDFParserRegistry.getInstance();
        FileFormat fileFormat = reg.getFileFormatForFileName(fileName);
        if (fileFormat == null || !(fileFormat instanceof RDFFormat)){
            //added bridgeDB/OPS specific extension here if required.  
            throw new VoidValidatorException("failed");
        } else {
            return (RDFFormat)fileFormat;
        }
    }

    /*
    private static PredicateFinderHandler getPreviewHandler(String baseURI, File file, String rdfFormatName) throws BridgeDBException{
        PredicateFinderHandler finder = new PredicateFinderHandler();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(baseURI, file, rdfFormatName);
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
       return finder;
    }*/

    private void checkInfo (String uri) throws BridgeDBException, VoidValidatorException{
        RdfReader reader = RdfFactoryIMS.getReader();
        getPreviewHandler(uri, null);
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
        Resource linksetId;
        URI linkPredicate;
        URI justification;
        Value isSymetric;
        if (statement != null){
            linksetId  = getObject(statement);
        } else {
            linksetId = getLinksetId();
        }
        linkPredicate = finder.getSingleURI(linksetId, VoidConstants.LINK_PREDICATE);
        justification = finder.getSingleURI(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES);    
        isSymetric = finder.getPossibleValue(linksetId, BridgeDBConstants.IS_SYMETRIC);
        System.out.println("Found " + linksetId);
        if (this.linkPredicate == null) {
            this.linkPredicate = linkPredicate;
        } else if (this.linkPredicate.equals(linkPredicate)){
        } else {
            throw new BridgeDBException ("Diffrent predicates. Was " + this.linkPredicate + " but found " + linkPredicate);
        }
        if (this.justification == null) {
            this.justification = justification;
        } else if (this.justification.equals(justification)){
        } else {
            throw new BridgeDBException ("Diffrent justification. Was " + this.justification + " but found " + justification);
        }
    }
    
    public void addFile(File inputFile) throws VoidValidatorException, RepositoryException, BridgeDBException {
        Reporter.println("Parsing " + inputFile.getAbsolutePath());
        try {
            repositoryConnection.add(inputFile, DEFAULT_BASE_URI, getFormat(inputFile.getName()));
        } catch (Exception ex) {
            repositoryConnection.close();
            throw new VoidValidatorException ("Error parsing RDf file ", ex);
        }
    }

    public void addUri(String uri) throws BridgeDBException, VoidValidatorException, RepositoryException {
        Reporter.println("Reading " + uri);
        checkInfo (uri);
        URI original = new URIImpl(uri);
        originals.add(original);
        File file = UriFileMapper.toFile(uri);
        addFile (file);
    }
    
    private boolean checkStatement(Statement statement) throws BridgeDBException {
        Xref xref;
        Resource subject = statement.getSubject();
        if (subject instanceof URI){
            try{
                xref = imsMapper.toXref(subject.stringValue());
            } catch (BridgeDBException e){
                System.err.println(e.getMessage());
                return false;
            }
            if (xref == null){
                System.err.println("No xref for subject <" + subject + ">");
                return false;
            } else if (sourceDataSource == null){
                sourceDataSource = xref.getDataSource();
            } else if (sourceDataSource != xref.getDataSource()){
                throw new BridgeDBException("found two dataSources was " + sourceDataSource + " now " + xref.getDataSource());
            }
        } else {
            System.err.println("None URI subject " + subject);
            return false;
        }
        Value object = statement.getObject();
        if (object instanceof URI){
            try{
                xref = imsMapper.toXref(object.stringValue());
            } catch (BridgeDBException e){
                System.err.println(e.getMessage());
                return false;
            }
            if (xref == null){
                System.err.println("No xref for object <" + object + ">");
                return false;
            } else if (targetDataSource == null){
                targetDataSource = xref.getDataSource();
            } else if (targetDataSource != xref.getDataSource()){
                throw new BridgeDBException("found two dataSources was " + targetDataSource + " now " + xref.getDataSource());
            }
        } else {
            System.err.println("None URI object " + object);
            return false;
        }
        return true;
    }

    private void writeSimple(RDFWriter goodWriter, RDFWriter badWriter) 
            throws IOException, RDFHandlerException, RepositoryException, BridgeDBException{ 
        int goodCount = 0;
        int badCount = 0;
        //derivedFrom
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, linkPredicate, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next(); 
            if (checkStatement(statement)){
                goodWriter.handleStatement(statement);
                goodCount++;
            } else {
                badWriter.handleStatement(statement);                
                badCount++;
            }
            if (goodCount % 100000 == 0){
                Reporter.println("Writing " + goodCount + " ignoring " + badCount);
            }
        }
        Reporter.println("Wrote " + goodCount + " Ignored " + badCount);
    }

    private URI convertSubject(Resource subject) throws BridgeDBException {
        Xref xref;
        if (subject instanceof URI){
            try{
                 xref = imsMapper.toXref(subject.stringValue());
            } catch (BridgeDBException e){
                System.err.println(e.getMessage());
                return null;
            }
            if (xref == null){
                System.err.println("No xref for subject <" + subject + ">");
                return null;
            } else if (sourceDataSource == null){
                sourceDataSource = xref.getDataSource();
            } else if (sourceDataSource != xref.getDataSource()){
                throw new BridgeDBException("found two dataSources was " + sourceDataSource + " now " + xref.getDataSource());
            }
            return new URIImpl(xref.getKnownUrl());
        } else {
            System.err.println("None URI object " + subject);
            return null;
        }        
    }

    private URI convertObject(Value object) throws BridgeDBException {
        Xref xref;
        if (object instanceof URI){
            try{
                 xref = imsMapper.toXref(object.stringValue());
            } catch (BridgeDBException e){
                System.err.println(e.getMessage());
                return null;
            }
            if (xref == null){
                System.err.println("No xref for object <" + object + ">");
                return null;
            } else if (targetDataSource == null){
                targetDataSource = xref.getDataSource();
            } else if (targetDataSource != xref.getDataSource()){
                throw new BridgeDBException("found two dataSources was " + targetDataSource + " now " + xref.getDataSource());
            }
            return new URIImpl(xref.getKnownUrl());
        } else {
            System.err.println("None URI object " + object);
            return null;
        }        
    }


    private void writeConverting(RDFWriter goodWriter, RDFWriter badWriter) 
            throws IOException, RDFHandlerException, RepositoryException, BridgeDBException{ 
        int goodCount = 0;
        int badCount = 0;
        //derivedFrom
        RepositoryResult<Statement> statements = 
                repositoryConnection.getStatements(null, linkPredicate, null, true);
        while (statements.hasNext()) {
            Statement statement = statements.next(); 
            URI subject = convertSubject(statement.getSubject());
            URI object = convertObject(statement.getObject());
            if (subject == null || object == null){
                badWriter.handleStatement(statement);                
                badCount++;
                if (badCount % 100 == 0){
                    Reporter.println("Writing " + goodCount + " ignoring " + badCount);
                }
            } else {   
                Statement newStatement = new StatementImpl(subject, statement.getPredicate(), object);
                goodWriter.handleStatement(newStatement);
                goodCount++;
                if (goodCount % 100000 == 0){
                    Reporter.println("Writing " + goodCount + " ignoring " + badCount);
                }
            }
        }
        Reporter.println("Wrote " + goodCount + " Ignored " + badCount);
    }

    private void writeRDF(RDFWriter goodWriter, RDFWriter badWriter, URI linksetId) 
            throws IOException, RDFHandlerException, RepositoryException, BridgeDBException{ 
         //rdfWriter.handleNamespace("", DEFAULT_BASE_URI);
        goodWriter.startRDF();
        badWriter.startRDF();
        for (URI original:originals){
            Statement statement = new StatementImpl(linksetId, PavConstants.DERIVED_FROM, original);
            goodWriter.handleStatement(statement);
        }
        goodWriter.handleStatement(new StatementImpl(linksetId, VoidConstants.LINK_PREDICATE, linkPredicate));
        goodWriter.handleStatement(new StatementImpl(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, justification));
 
        if (convert){
            writeConverting(goodWriter, badWriter);            
        } else {
            writeSimple(goodWriter, badWriter);
        }
        goodWriter.endRDF();
        badWriter.endRDF();
    }
       
    public void writeFile(String fileMiddle) throws VoidValidatorException, RepositoryException, BridgeDBException {
        Reporter.println("Writing to " + fileMiddle);
        File goodFile = new File("C:\\Dropbox\\ims\\dev\\version1.4.2.alpha1\\good_" + fileMiddle + ".ttl");
        File badFile = new File("C:\\Dropbox\\ims\\dev\\version1.4.2.alpha1\\bad_" + fileMiddle + ".ttl");
        URI linksetId = new URIImpl("http://openphacts.cs.man.ac.uk/ims/linkset/version1.4.2.alpha1/" + fileMiddle + ".ttl");
        try {
            Writer goodWriter = new FileWriter (goodFile);
            TurtleWriter goodTurtleWriter = new TurtleWriter(goodWriter);
            Writer badWriter = new FileWriter (badFile);
            TurtleWriter badTurtleWriter = new TurtleWriter(badWriter);
            writeRDF(goodTurtleWriter, badTurtleWriter, linksetId);
            goodWriter.close();
            badWriter.close();
        } catch (Exception ex) {
            throw new VoidValidatorException ("Error parsing RDf file ", ex);
        } finally {
            repositoryConnection.close();
        }
    }

    public static void main(String[] args) throws Exception {
        ParentChildCheck combiner = new ParentChildCheck();
        combiner.convert = true;
        combiner.addUri("file:///C:/Dropbox/ims/dev/version1.4.2.alpha1/LINKSET_TAUTOMER_CHILD_CHILD.ttl");
        System.out.println(combiner.linkPredicate);
        combiner.writeFile("LINKSET_TAUTOMER_CHILD_CHILD");
    }

}
