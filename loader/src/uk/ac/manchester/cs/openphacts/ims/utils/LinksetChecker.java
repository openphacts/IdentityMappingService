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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.bridgedb.rdf.constants.BridgeDBConstants;
import org.bridgedb.rdf.constants.DulConstants;
import org.bridgedb.rdf.constants.VoidConstants;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.sail.memory.MemoryStore;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.Reporter;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.Loader;
import uk.ac.manchester.cs.openphacts.ims.loader.RdfFactoryIMS;
import uk.ac.manchester.cs.openphacts.ims.loader.UriFileMapper;
import uk.ac.manchester.cs.openphacts.ims.loader.handler.PreviewHandler;

/**
 *
 * @author Christian
 */
public class LinksetChecker extends Loader {
    
   public static String DEFAULT_BASE_URI = "http://no/BaseURI/Set/";
   private URI linkPredicate;
   private URI justification;
   private RepositoryConnection repositoryConnection;
   //private ArrayList<URI> originals;
   //private DataSource sourceDataSource;
   //private DataSource targetDataSource;
   //private boolean convert = false;
   
    private LinksetChecker() throws BridgeDBException{
        linkPredicate = null;
        justification = null;
      // sourceDataSource = null;
      // targetDataSource = null;
      // originals = new ArrayList<URI>();
      // repositoryConnection = null;
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

    public void addFile(File inputFile) throws VoidValidatorException, RepositoryException, BridgeDBException {
        Reporter.println("Parsing " + inputFile.getAbsolutePath());
        try {
            repositoryConnection.add(inputFile, DEFAULT_BASE_URI, getFormat(inputFile.getName()));
        } catch (Exception ex) {
            repositoryConnection.close();
            throw new VoidValidatorException ("Error parsing RDf file ", ex);
        }
    }

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
        linkPredicate = getSingleURI(linksetId, VoidConstants.LINK_PREDICATE);
        justification = getSingleURI(linksetId, BridgeDBConstants.LINKSET_JUSTIFICATION, DulConstants.EXPRESSES);  
        isSymetric = getPossibleValue(linksetId, BridgeDBConstants.IS_SYMETRIC);
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
        
    public void addUri(String uri) throws BridgeDBException, VoidValidatorException, RepositoryException {
        Reporter.println("Reading " + uri);
        checkInfo(uri);
        File file = UriFileMapper.toFile(uri);
        addFile (file);
    }
    
    private void writeSubjectCount(String fileName) throws Exception{ 
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        BufferedWriter buffered = new BufferedWriter(writer);
        String queryString = "SELECT ?s (COUNT(?o) AS ?count) WHERE { ?s ?p ?o .} GROUP BY ?s"; 
        TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult results = query.evaluate();
        while (results.hasNext()) {
            BindingSet bindingSet = results.next();
            URI subject = (URI)bindingSet.getValue("s");
            Literal count = (Literal)bindingSet.getValue("count");
            buffered.append(subject.getLocalName() + " , " + count.integerValue());
            buffered.newLine();
        }
        buffered.close();
    }

    private void writeObjectCount(String fileName) throws Exception{ 
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        BufferedWriter buffered = new BufferedWriter(writer);
        String queryString = "SELECT ?o (COUNT(?s) AS ?count) WHERE { ?s ?p ?o .} GROUP BY ?o"; 
        TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult results = query.evaluate();
        while (results.hasNext()) {
            BindingSet bindingSet = results.next();
            Value object = bindingSet.getValue("o");
            Literal count = (Literal)bindingSet.getValue("count");
            if (object instanceof URI){
                buffered.append(((URI)object).getLocalName() + " , " + count.integerValue());
                buffered.newLine();
            } else{
                System.out.println(object);
            }
        }
        buffered.close();
    }

    public static void main(String[] args) throws Exception {
        ConfigReader.useTest();
        LinksetChecker checker = new LinksetChecker() ;
        //checker.addUri("http://openphacts.cs.man.ac.uk/ims//linkset/version1.4.1/ensembl/ENS_LRG_gene.ttl");
        //checker.addUri("http://openphacts.cs.man.ac.uk/ims//linkset/version1.4.1/ensembl/uniprot.ttl");
       // System.out.println("aaded finished");
        //checker.writeSubjectCount("C:/Dropbox/OPS/linkCounts/enembleBased.csv");
        //checker.addUri("http://openphacts.cs.man.ac.uk/ims//linkset/version1.4.1/uniprot/uniprot_ensembl.ttl");
        File file = new File("C:\\Dropbox\\ims\\dev\\version1.4.2.alpha1\\RSC\\LINKSET_SUPER_CHILD_CHILD.ttl");
        checker.addFile(file);
        System.out.println("aaded finished");
        checker.writeSubjectCount("C:/Dropbox/ims/dev/temp/linkSetENSGENST_subjects.csv");
        checker.writeObjectCount("C:/Dropbox/ims/dev/temp/linkSetENSGENST_objects.csv");
        
    }

}
