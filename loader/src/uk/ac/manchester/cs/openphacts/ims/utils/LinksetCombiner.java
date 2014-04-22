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
import java.net.URLEncoder;
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
import uk.ac.manchester.cs.openphacts.ims.loader.Loader;
import uk.ac.manchester.cs.openphacts.ims.loader.RdfFactoryIMS;
import uk.ac.manchester.cs.openphacts.ims.loader.RdfParserPlus;
import uk.ac.manchester.cs.openphacts.ims.loader.UriFileMapper;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.PredicateFinderHandler;

/**
 *
 * @author Christian
 */
public class LinksetCombiner extends Loader {
    
   public static String DEFAULT_BASE_URI = "http://no/BaseURI/Set/";
   private URI linkPredicate;
   private URI justification;
   private RepositoryConnection repositoryConnection;
   private ArrayList<URI> originals;
   private DataSource sourceDataSource;
   private DataSource targetDataSource;
   
   private LinksetCombiner() throws BridgeDBException{
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
    private static PredicateFinderHandler getPredicateFinderHandler(String baseURI, File file, String rdfFormatName) throws BridgeDBException{
        PredicateFinderHandler finder = new PredicateFinderHandler();
        RdfParserPlus parser = new RdfParserPlus(finder);
        parser.parse(baseURI, file, rdfFormatName);
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
       return finder;
    }*/

    private void checkInfo (String uri) throws BridgeDBException, VoidValidatorException{
        RdfReader reader = RdfFactoryIMS.getReader();
        PredicateFinderHandler finder = getPredicateFinderHandler(uri, null);
        Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
        Resource linksetId;
        URI linkPredicate;
        URI justification;
        Value isSymetric;
        if (statement != null){
            linksetId  = getObject(statement);
            linkPredicate = getObject(linksetId, VoidConstants.LINK_PREDICATE);
            justification = getObject(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES);  
            isSymetric = getPossibleValue(linksetId, BridgeDBConstants.IS_SYMETRIC);
        } else {
            linksetId = getLinksetId(finder);
            linkPredicate = getObject(finder, VoidConstants.LINK_PREDICATE);
            justification = getObject(finder, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES);    
            isSymetric = getPossibleValue(finder, BridgeDBConstants.IS_SYMETRIC);
        }
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
                xref = uriListener.toXref(subject.stringValue());
            } catch (BridgeDBException e){
                System.err.println(e.getMessage());
                return false;
            }
            if (xref == null){
                System.err.println("No xref for  " + subject);
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
                xref = uriListener.toXref(object.stringValue());
            } catch (BridgeDBException e){
                System.err.println(e.getMessage());
                return false;
            }
            if (xref == null){
                System.err.println("No xref for  " + object);
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

    private void writeRDF(RDFWriter goodWriter, RDFWriter badWriter, URI linksetId) 
            throws IOException, RDFHandlerException, RepositoryException, BridgeDBException{ 
        int goodCount = 0;
        int badCount = 0;
        //rdfWriter.handleNamespace("", DEFAULT_BASE_URI);
        goodWriter.startRDF();
        badWriter.startRDF();
        for (URI original:originals){
            Statement statement = new StatementImpl(linksetId, PavConstants.DERIVED_FROM, original);
            goodWriter.handleStatement(statement);
        }
        goodWriter.handleStatement(new StatementImpl(linksetId, VoidConstants.LINK_PREDICATE, linkPredicate));
        goodWriter.handleStatement(new StatementImpl(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, justification));
         
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
        goodWriter.endRDF();
        badWriter.endRDF();
        Reporter.println("Wrote " + goodCount + " Ignored " + badCount);
    }
       
    public void writeFile(String fileMiddle) throws VoidValidatorException, RepositoryException, BridgeDBException {
        Reporter.println("Writing to " + fileMiddle);
        File goodFile = new File("C:/Dropbox/ims/linkset/versions1.4.1/" + fileMiddle + ".ttl");
        File badFile = new File("C:/Dropbox/ims/linkset/versions1.4.1/" + fileMiddle + "_bad.ttl");
        URI linksetId = new URIImpl("http://openphacts.cs.man.ac.uk/ims/linkset/version1.4.1/" + fileMiddle + ".ttl");
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

    public static void CHARGE_UNSENSITIVE() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_CLOSE_PARENT_CHILD_CHARGE_UNSENSITIVE_PARENT_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_CLOSE_PARENT_CHILD_CHARGE_UNSENSITIVE_PARENT_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_CLOSE_PARENT_CHILD_CHARGE_UNSENSITIVE_PARENT_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_CLOSE_PARENT_CHILD_CHARGE_UNSENSITIVE_PARENT_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_CLOSE_PARENT_CHILD_CHARGE_UNSENSITIVE_PARENT_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_CLOSE_PARENT_CHILD_CHARGE_UNSENSITIVE_PARENT_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_CLOSE_PARENT_CHILD_CHARGE_UNSENSITIVE_PARENT");
    }

    public static void FRAGMENT_UNSENSITIVE() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_CLOSE_PARENT_CHILD_FRAGMENT_UNSENSITIVE_PARENT_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_CLOSE_PARENT_CHILD_FRAGMENT_UNSENSITIVE_PARENT_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_CLOSE_PARENT_CHILD_FRAGMENT_UNSENSITIVE_PARENT_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_CLOSE_PARENT_CHILD_FRAGMENT_UNSENSITIVE_PARENT_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_CLOSE_PARENT_CHILD_FRAGMENT_UNSENSITIVE_PARENT_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_CLOSE_PARENT_CHILD_FRAGMENT_UNSENSITIVE_PARENT_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_CLOSE_PARENT_CHILD_FRAGMENT_UNSENSITIVE_PARENT");
    }

    public static void ISOTOPE_UNSENSITIVE() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_CLOSE_PARENT_CHILD_ISOTOPE_UNSENSITIVE_PARENT_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_CLOSE_PARENT_CHILD_ISOTOPE_UNSENSITIVE_PARENT_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_CLOSE_PARENT_CHILD_ISOTOPE_UNSENSITIVE_PARENT_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_CLOSE_PARENT_CHILD_ISOTOPE_UNSENSITIVE_PARENT_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_CLOSE_PARENT_CHILD_ISOTOPE_UNSENSITIVE_PARENT_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_CLOSE_PARENT_CHILD_ISOTOPE_UNSENSITIVE_PARENT_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_CLOSE_PARENT_CHILD_ISOTOPE_UNSENSITIVE_PARENT");
    }
    
    public static void STEREO_UNSENSITIVE() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_CLOSE_PARENT_CHILD_STEREO_UNSENSITIVE_PARENT_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_CLOSE_PARENT_CHILD_STEREO_UNSENSITIVE_PARENT_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_CLOSE_PARENT_CHILD_STEREO_UNSENSITIVE_PARENT_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_CLOSE_PARENT_CHILD_STEREO_UNSENSITIVE_PARENT_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_CLOSE_PARENT_CHILD_STEREO_UNSENSITIVE_PARENT_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_CLOSE_PARENT_CHILD_STEREO_UNSENSITIVE_PARENT_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_CLOSE_PARENT_CHILD_STEREO_UNSENSITIVE_PARENT"); 
    }

    public static void SUPER_UNSENSITIVE() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_CLOSE_PARENT_CHILD_SUPER_UNSENSITIVE_PARENT_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_CLOSE_PARENT_CHILD_SUPER_UNSENSITIVE_PARENT_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_CLOSE_PARENT_CHILD_SUPER_UNSENSITIVE_PARENT_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_CLOSE_PARENT_CHILD_SUPER_UNSENSITIVE_PARENT_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_CLOSE_PARENT_CHILD_SUPER_UNSENSITIVE_PARENT_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_CLOSE_PARENT_CHILD_SUPER_UNSENSITIVE_PARENT_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_CLOSE_PARENT_CHILD_SUPER_UNSENSITIVE_PARENT");
    }
    
    public static void TAUTOMER_UNSENSITIVEAT_7_4_PH() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_AT_7_4_PH_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_AT_7_4_PH_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_AT_7_4_PH_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_AT_7_4_PH_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_AT_7_4_PH_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_AT_7_4_PH_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVEAT_7_4_PH_PARENT"); 
    }

    public static void TAUTOMER_UNSENSITIVE() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_CLOSE_PARENT_CHILD_TAUTOMER_UNSENSITIVE_PARENT"); 
    }


    public static void OPS_CHEMSPIDER() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_EXACT_OPS_CHEMSPIDER_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_EXACT_OPS_CHEMSPIDER_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_EXACT_OPS_CHEMSPIDER_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_EXACT_OPS_CHEMSPIDER_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_EXACT_OPS_CHEMSPIDER_MESH20131111.ttl");
        //Different justification combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_EXACT_OPS_CHEMSPIDER_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_EXACT_OPS_CHEMSPIDER"); 
    }

        
    public static void FRAGMENT() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-11.ttl");
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/CRS/void_2013-11-12.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEBI/LINKSET_RELATED_PARENT_CHILD_FRAGMENT_CHEBI20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/CHEMBL/LINKSET_RELATED_PARENT_CHILD_FRAGMENT_CHEMBL20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/DRUGBANK/LINKSET_RELATED_PARENT_CHILD_FRAGMENT_DRUGBANK20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/HMDB/LINKSET_RELATED_PARENT_CHILD_FRAGMENT_HMDB20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/MESH/LINKSET_RELATED_PARENT_CHILD_FRAGMENT_MESH20131111.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/RSC_2013-11-11/PDB/LINKSET_RELATED_PARENT_CHILD_FRAGMENT_PDB20131111.ttl");
        combiner.writeFile("RSC/LINKSET_RELATED_PARENT_CHILD_FRAGMENT_PARENT");
    }
       
    public static void ArrayExpress() throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims/dev/version1.3.1.alpha1/ensembl/Ensembl_71.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/bos_taurus_core_71_31_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/caenorhabditis_elegans_core_71_235_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/danio_rerio_core_71_9_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/drosophila_melanogaster_core_71_546_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/equus_caballus_core_71_2_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/gallus_gallus_core_71_4_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/homo_sapiens_core_71_37_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/mus_musculus_core_71_38_ensembl_ArrayExpressLinkSets.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/rattus_norvegicus_core_71_5_ensembl_ArrayExpressLinkSets.ttl");
        combiner.writeFile("ensembl/ArrayExpress");
    }
    /*
    public static void () throws Exception {
        LinksetCombiner combiner = new LinksetCombiner();
        combiner.reader.loadURI("http://openphacts.cs.man.ac.uk/ims//dev/version1.3.1.alpha1/ensembl/Ensembl_71.ttl");
        combiner.addUri("http://openphacts.cs.man.ac.uk/ims/originals/ensembl_2013-07-22/");
        combiner.writeFile("ensembl/");
    }
    
drosophila_melanogaster_core_71_546_ensembl_BioGRIDLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_EC_NUMBERLinkSets.ttl
bos_taurus_core_71_31_ensembl_EMBLLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_EMBLLinkSets.ttl
canis_familiaris_core_71_31_ensembl_EMBLLinkSets.ttl
danio_rerio_core_71_9_ensembl_EMBLLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_EMBLLinkSets.ttl
equus_caballus_core_71_2_ensembl_EMBLLinkSets.ttl
gallus_gallus_core_71_4_ensembl_EMBLLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_EMBLLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_EMBLLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_EMBLLinkSets.ttl
homo_sapiens_core_71_37_ensembl_ENS_LRG_geneLinkSets.ttl
bos_taurus_core_71_31_ensembl_EntrezGeneLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_EntrezGeneLinkSets.ttl
canis_familiaris_core_71_31_ensembl_EntrezGeneLinkSets.ttl
danio_rerio_core_71_9_ensembl_EntrezGeneLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_EntrezGeneLinkSets.ttl
equus_caballus_core_71_2_ensembl_EntrezGeneLinkSets.ttl
gallus_gallus_core_71_4_ensembl_EntrezGeneLinkSets.ttl
homo_sapiens_core_71_37_ensembl_EntrezGeneLinkSets.ttl
mus_musculus_core_71_38_ensembl_EntrezGeneLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_EntrezGeneLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_EntrezGeneLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_EntrezGeneLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_flybase_annotation_idLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_flybase_gene_idLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_flybase_transcript_idLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_flybase_translation_idLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_GO_to_geneLinkSets.ttl
bos_taurus_core_71_31_ensembl_GOLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_GOLinkSets.ttl
canis_familiaris_core_71_31_ensembl_GOLinkSets.ttl
danio_rerio_core_71_9_ensembl_GOLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_GOLinkSets.ttl
equus_caballus_core_71_2_ensembl_GOLinkSets.ttl
gallus_gallus_core_71_4_ensembl_GOLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_GOLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_GOLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_GOLinkSets.ttl
bos_taurus_core_71_31_ensembl_goslim_goaLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_goslim_goaLinkSets.ttl
canis_familiaris_core_71_31_ensembl_goslim_goaLinkSets.ttl
danio_rerio_core_71_9_ensembl_goslim_goaLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_goslim_goaLinkSets.ttl
equus_caballus_core_71_2_ensembl_goslim_goaLinkSets.ttl
gallus_gallus_core_71_4_ensembl_goslim_goaLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_goslim_goaLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_goslim_goaLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_goslim_goaLinkSets.ttl
bos_taurus_core_71_31_ensembl_HGNCLinkSets.ttl
canis_familiaris_core_71_31_ensembl_HGNCLinkSets.ttl
danio_rerio_core_71_9_ensembl_HGNCLinkSets.ttl
equus_caballus_core_71_2_ensembl_HGNCLinkSets.ttl
gallus_gallus_core_71_4_ensembl_HGNCLinkSets.ttl
homo_sapiens_core_71_37_ensembl_HGNCLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_HGNCLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_InterproLinkSets.ttl
bos_taurus_core_71_31_ensembl_IPILinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_IPILinkSets.ttl
homo_sapiens_core_71_37_ensembl_LRGLinkSets.ttl
bos_taurus_core_71_31_ensembl_MEROPSLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_MEROPSLinkSets.ttl
canis_familiaris_core_71_31_ensembl_MEROPSLinkSets.ttl
danio_rerio_core_71_9_ensembl_MEROPSLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_MEROPSLinkSets.ttl
equus_caballus_core_71_2_ensembl_MEROPSLinkSets.ttl
gallus_gallus_core_71_4_ensembl_MEROPSLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_MEROPSLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_MEROPSLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_MEROPSLinkSets.ttl
mus_musculus_core_71_38_ensembl_MGILinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_MGILinkSets.ttl
homo_sapiens_core_71_37_ensembl_MIM_GENELinkSets.ttl
homo_sapiens_core_71_37_ensembl_MIM_MORBIDLinkSets.ttl
bos_taurus_core_71_31_ensembl_miRBaseLinkSets.ttl
canis_familiaris_core_71_31_ensembl_miRBaseLinkSets.ttl
danio_rerio_core_71_9_ensembl_miRBaseLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_miRBaseLinkSets.ttl
equus_caballus_core_71_2_ensembl_miRBaseLinkSets.ttl
gallus_gallus_core_71_4_ensembl_miRBaseLinkSets.ttl
homo_sapiens_core_71_37_ensembl_miRBaseLinkSets.ttl
mus_musculus_core_71_38_ensembl_miRBaseLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_miRBaseLinkSets.ttl
homo_sapiens_core_71_37_ensembl_OrphanetLinkSets.ttl
danio_rerio_core_71_9_ensembl_OTTGLinkSets.ttl
homo_sapiens_core_71_37_ensembl_OTTGLinkSets.ttl
mus_musculus_core_71_38_ensembl_OTTGLinkSets.ttl
bos_taurus_core_71_31_ensembl_PDBLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_PDBLinkSets.ttl
canis_familiaris_core_71_31_ensembl_PDBLinkSets.ttl
danio_rerio_core_71_9_ensembl_PDBLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_PDBLinkSets.ttl
equus_caballus_core_71_2_ensembl_PDBLinkSets.ttl
gallus_gallus_core_71_4_ensembl_PDBLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_PDBLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_PDBLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_PDBLinkSets.ttl
bos_taurus_core_71_31_ensembl_protein_idLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_protein_idLinkSets.ttl
canis_familiaris_core_71_31_ensembl_protein_idLinkSets.ttl
danio_rerio_core_71_9_ensembl_protein_idLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_protein_idLinkSets.ttl
equus_caballus_core_71_2_ensembl_protein_idLinkSets.ttl
gallus_gallus_core_71_4_ensembl_protein_idLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_protein_idLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_protein_idLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_protein_idLinkSets.ttl
bos_taurus_core_71_31_ensembl_RefSeq_mRNA_predictedLinkSets.ttl
canis_familiaris_core_71_31_ensembl_RefSeq_mRNA_predictedLinkSets.ttl
equus_caballus_core_71_2_ensembl_RefSeq_mRNA_predictedLinkSets.ttl
gallus_gallus_core_71_4_ensembl_RefSeq_mRNA_predictedLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_RefSeq_mRNA_predictedLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RefSeq_mRNA_predictedLinkSets.ttl
bos_taurus_core_71_31_ensembl_RefSeq_mRNALinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_RefSeq_mRNALinkSets.ttl
canis_familiaris_core_71_31_ensembl_RefSeq_mRNALinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_RefSeq_mRNALinkSets.ttl
equus_caballus_core_71_2_ensembl_RefSeq_mRNALinkSets.ttl
gallus_gallus_core_71_4_ensembl_RefSeq_mRNALinkSets.ttl
pan_troglodytes_core_71_214_ensembl_RefSeq_mRNALinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RefSeq_mRNALinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_RefSeq_mRNALinkSets.ttl
bos_taurus_core_71_31_ensembl_RefSeq_ncRNA_predictedLinkSets.ttl
canis_familiaris_core_71_31_ensembl_RefSeq_ncRNA_predictedLinkSets.ttl
equus_caballus_core_71_2_ensembl_RefSeq_ncRNA_predictedLinkSets.ttl
gallus_gallus_core_71_4_ensembl_RefSeq_ncRNA_predictedLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_RefSeq_ncRNA_predictedLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RefSeq_ncRNA_predictedLinkSets.ttl
bos_taurus_core_71_31_ensembl_RefSeq_ncRNALinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_RefSeq_ncRNALinkSets.ttl
canis_familiaris_core_71_31_ensembl_RefSeq_ncRNALinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_RefSeq_ncRNALinkSets.ttl
equus_caballus_core_71_2_ensembl_RefSeq_ncRNALinkSets.ttl
gallus_gallus_core_71_4_ensembl_RefSeq_ncRNALinkSets.ttl
pan_troglodytes_core_71_214_ensembl_RefSeq_ncRNALinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RefSeq_ncRNALinkSets.ttl
bos_taurus_core_71_31_ensembl_RefSeq_peptide_predictedLinkSets.ttl
canis_familiaris_core_71_31_ensembl_RefSeq_peptide_predictedLinkSets.ttl
danio_rerio_core_71_9_ensembl_RefSeq_peptide_predictedLinkSets.ttl
equus_caballus_core_71_2_ensembl_RefSeq_peptide_predictedLinkSets.ttl
gallus_gallus_core_71_4_ensembl_RefSeq_peptide_predictedLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_RefSeq_peptide_predictedLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RefSeq_peptide_predictedLinkSets.ttl
bos_taurus_core_71_31_ensembl_RefSeq_peptideLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_RefSeq_peptideLinkSets.ttl
canis_familiaris_core_71_31_ensembl_RefSeq_peptideLinkSets.ttl
danio_rerio_core_71_9_ensembl_RefSeq_peptideLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_RefSeq_peptideLinkSets.ttl
equus_caballus_core_71_2_ensembl_RefSeq_peptideLinkSets.ttl
gallus_gallus_core_71_4_ensembl_RefSeq_peptideLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_RefSeq_peptideLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RefSeq_peptideLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_RefSeq_peptideLinkSets.ttl
bos_taurus_core_71_31_ensembl_RFAMLinkSets.ttl
canis_familiaris_core_71_31_ensembl_RFAMLinkSets.ttl
danio_rerio_core_71_9_ensembl_RFAMLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_RFAMLinkSets.ttl
equus_caballus_core_71_2_ensembl_RFAMLinkSets.ttl
gallus_gallus_core_71_4_ensembl_RFAMLinkSets.ttl
homo_sapiens_core_71_37_ensembl_RFAMLinkSets.ttl
mus_musculus_core_71_38_ensembl_RFAMLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_RFAMLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RFAMLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_RGDLinkSets.ttl
bos_taurus_core_71_31_ensembl_UniGeneLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_UniGeneLinkSets.ttl
canis_familiaris_core_71_31_ensembl_UniGeneLinkSets.ttl
danio_rerio_core_71_9_ensembl_UniGeneLinkSets.ttl
equus_caballus_core_71_2_ensembl_UniGeneLinkSets.ttl
gallus_gallus_core_71_4_ensembl_UniGeneLinkSets.ttl
homo_sapiens_core_71_37_ensembl_UniGeneLinkSets.ttl
mus_musculus_core_71_38_ensembl_UniGeneLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_UniGeneLinkSets.ttl
bos_taurus_core_71_31_ensembl_UniParcLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_UniParcLinkSets.ttl
danio_rerio_core_71_9_ensembl_UniParcLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_UniParcLinkSets.ttl
equus_caballus_core_71_2_ensembl_UniParcLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_UniParcLinkSets.ttl
bos_taurus_core_71_31_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
canis_familiaris_core_71_31_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
danio_rerio_core_71_9_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
equus_caballus_core_71_2_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
gallus_gallus_core_71_4_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_Uniprot%2FSPTREMBLLinkSets.ttl
bos_taurus_core_71_31_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
caenorhabditis_elegans_core_71_235_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
canis_familiaris_core_71_31_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
danio_rerio_core_71_9_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
drosophila_melanogaster_core_71_546_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
equus_caballus_core_71_2_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
gallus_gallus_core_71_4_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
pan_troglodytes_core_71_214_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
rattus_norvegicus_core_71_5_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
saccharomyces_cerevisiae_core_71_4_ensembl_Uniprot%2FSWISSPROTLinkSets.ttl
homo_sapiens_core_71_37_ensembl_uniprot.ttl
danio_rerio_core_71_9_ensembl_ZFIN_IDLinkSets.ttl
*/
    
    public static void main(String[] args) throws Exception {
     //   CHARGE_UNSENSITIVE();
     //   FRAGMENT_UNSENSITIVE();
     //   ISOTOPE_UNSENSITIVE();
     //   STEREO_UNSENSITIVE();
     //   SUPER_UNSENSITIVE();
     //   TAUTOMER_UNSENSITIVEAT_7_4_PH();
     //   TAUTOMER_UNSENSITIVE();
     //   OPS_CHEMSPIDER();
     //   FRAGMENT();
        
     ArrayExpress();
    }
}
