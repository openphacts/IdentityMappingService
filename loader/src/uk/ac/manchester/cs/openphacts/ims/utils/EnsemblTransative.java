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
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
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
import uk.ac.manchester.cs.datadesc.validator.rdftools.Reporter;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.loader.Loader;

/**
 *
 * @author Christian
 */
public class EnsemblTransative extends Loader {
    
   public static String DEFAULT_BASE_URI = "http://no/BaseURI/Set/";
   private RepositoryConnection repositoryConnection;
   
   private static final URIImpl linkSetENSGENST = new URIImpl("http://bridgedb.org/linkSetENSGENST");
   private static final URIImpl linkSetENSTENSP = new URIImpl("http://bridgedb.org/linkSetENSTENSP");
   private static final URIImpl ensembl = new URIImpl("http://bridgedb.org/ensembl");
   private static final URIImpl uniport = new URIImpl("http://bridgedb.org/uniport");

   private int ENSGfoundEnsembl = 0;
   private int ENSGfoundUniport = 0;
   private int ENSGnotFound = 0;
   private int ENSTfound = 0;
   private int ENSTnotFound = 0;
   private int other = 0;
   
    private EnsemblTransative() throws BridgeDBException{
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

    public void addFile(File inputFile, Resource graph) throws VoidValidatorException, RepositoryException, BridgeDBException {
        Reporter.println("Parsing " + inputFile.getAbsolutePath());
        try {
            repositoryConnection.add(inputFile, DEFAULT_BASE_URI, getFormat(inputFile.getName()), graph);
        } catch (Exception ex) {
            repositoryConnection.close();
            throw new VoidValidatorException ("Error parsing RDf file ", ex);
        }
    }
    
    private boolean findUniprot(URI object)throws Exception{
        URIImpl original = new URIImpl("http://purl.uniprot.org/ensembl/" + object.getLocalName());
        String queryString = "SELECT (COUNT(?s) AS ?count) WHERE { GRAPH <" + uniport + "> { ?s ?p <" + original + ">.} }"; 
        TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult results = query.evaluate();
        if (results.hasNext()) {
            BindingSet bindingSet = results.next();
            Literal newCount = (Literal)bindingSet.getValue("count");
            System.out.println(" -> " +  newCount.intValue());
            return (newCount.intValue() > 0);
        } else {
            System.out.println(" not found");            
            return false;
        }
    }

    private void replaceENSG(URI subject, int count)throws Exception{
        URIImpl original = new URIImpl("http://identifiers.org/ensembl/" + subject.getLocalName());
        String queryString = "SELECT ?o WHERE { GRAPH <" + linkSetENSGENST + "> { <" + original + "> ?p ?o .} }"; 
        TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult results = query.evaluate();
        if (results.hasNext()) {
//            boolean found = false;
//            while (results.hasNext()) {
//                BindingSet bindingSet = results.next();
//                URI theObject = (URI)bindingSet.getValue("o");
//                System.out.print(original + " " + theObject );
//                if (findUniprot(theObject)){
//                    found = true;
//                }
//            }
//            if (found){
//                ENSGfoundUniport++;
//            } else {
//                ENSGfoundEnsembl++;
//            }
        } else {
            System.out.println(subject);
//            ENSGnotFound++;
        }
    }

    private void replaceENST(URI subject, int count)throws Exception{
        URIImpl original = new URIImpl("http://identifiers.org/ensembl/" + subject.getLocalName());
        String queryString = "SELECT (COUNT(?o) AS ?count) WHERE { GRAPH <" + linkSetENSTENSP + "> { <" + original + "> ?p ?o .} }"; 
        TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult results = query.evaluate();
        if (results.hasNext()) {
            BindingSet bindingSet = results.next();
            Literal newCount = (Literal)bindingSet.getValue("count");
            System.out.println(original + " " + newCount.intValue() + "  " + count);
            if (newCount.intValue() > 0){
                ENSTfound++;
            } else {
                ENSTnotFound++;
            }
        } else {
            System.out.println(original + " not found");            
        }
    }

    private void replaceObjects(URI subject, int count)throws Exception{
        if (subject.getLocalName().startsWith("ENSG")){
            this.replaceENSG(subject, count);
        } else if (subject.getLocalName().startsWith("ENST")){
            this.replaceENST(subject, count);
        } else {
            other++;
        }
    }
    
    private void check() throws Exception{ 
//        File file = new File(fileName);
//        FileWriter writer = new FileWriter(file);
//        BufferedWriter buffered = new BufferedWriter(writer);
        String queryString = "SELECT ?s (COUNT(?o) AS ?count) WHERE { GRAPH <" + ensembl + "> { ?s ?p ?o .} } GROUP BY ?s"; 
        System.out.println(queryString);
        TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult results = query.evaluate();
        while (results.hasNext()) {
            BindingSet bindingSet = results.next();
            URI subject = (URI)bindingSet.getValue("s");
            Literal count = (Literal)bindingSet.getValue("count");
            if (count.intValue() > 1000){
                replaceObjects(subject, count.intValue());
            }
//            buffered.newLine();
        }
//        buffered.close();
    }

    private void merge() throws Exception{ 
        File file = new File("C:/Dropbox/ims/dev/temp/ENSG_ENST_ENSP_Uniport.ttl");
        FileWriter writer = new FileWriter(file);
        BufferedWriter buffered = new BufferedWriter(writer);
        String queryString = "SELECT ?ENSG ?uniport WHERE { "
                + "GRAPH <" + linkSetENSGENST + "> { ?ENSG ?p1 ?ENST .} "
                + "GRAPH <" + linkSetENSTENSP + "> { ?ENST ?p1 ?ENSP .} " 
                + "GRAPH <" + uniport + "> { ?uniport <http://www.w3.org/2000/01/rdf-schema#seeAlso> ?ENSP .} "
                + "} order by ?ENSG "; 
        System.out.println(queryString);
        TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult results = query.evaluate();
        while (results.hasNext()) {
            BindingSet bindingSet = results.next();
            URI ensg = (URI)bindingSet.getValue("ENSG");
            URI uniport = (URI)bindingSet.getValue("uniport");
            URI enst = (URI)bindingSet.getValue("ENST");
            buffered.append("<" + ensg + "> rdfs:seeAlso <" + uniport + ">.");
            buffered.newLine();
        }
        buffered.close();
    }
    
    public static void main(String[] args) throws Exception {
        ConfigReader.useTest();
        EnsemblTransative checker = new EnsemblTransative() ;
        File file = new File("C:/Dropbox/ims/dev/temp/linkSetENSGENST.ttl");
        checker.addFile(file, linkSetENSGENST);
        file = new File("C:/Dropbox/ims/dev/temp/linkSetENSTENSP.ttl");
        checker.addFile(file, linkSetENSTENSP);
        //file = new File("C:\\Dropbox\\ims\\originals\\ensembl_2013-07-22\\homo_sapiens_core_71_37_ensembl_uniprot.ttl");
        //checker.addFile(file, ensembl);
        file = new File("C:/Dropbox/ims/dev/temp/uniprot_ensembl.ttl");
        checker.addFile(file, uniport);
        System.out.println("aaded finished");
        checker.merge();
        
        //System.out.println("ENSG found uniport " + checker.ENSGfoundUniport + " found ensembl " + checker.ENSGfoundEnsembl + " not found "+ checker.ENSGnotFound);
        //System.out.println("ENST found " + checker.ENSTfound + " not found " + checker.ENSTnotFound);
        //System.out.println("Other " + checker.other);
    }

}
