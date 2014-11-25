/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.bridgedb.DataSource;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.UriPatternType;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.uri.lens.Lens;
import org.bridgedb.uri.lens.LensTools;
import org.bridgedb.uri.tools.RegexUriPattern;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.utils.Reporter;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.ims.loader.transative.TransativeTestBase;

/**
 *
 * @author Christian
 */
public class BugTest extends TransativeTestBase{
    
    @Before
    public void testLoader() throws BridgeDBException, IOException, OpenRDFException, FileNotFoundException {
        //Check database is running and settup correctly or kill the test. 
        ConfigReader.useTest();
//        linksetLoader = new LinksetLoader();
//        linksetLoader.clearExistingData( StoreType.TEST);  
        setupPattern("TransativeTestA", "http://www.example.com/DS_A/$id");
        setupPattern("TransativeTestB", "http://www.example.com/DS_B/$id");
        SQLUriMapper mapper = SQLUriMapper.createNew();
 	}

    private void setupPattern (String name, String pattern) throws BridgeDBException{
        DataSource dataSource = DataSource.register(name, name).urlPattern(pattern).asDataSource();
        Lens testLens = LensTools.byId(Lens.TEST_LENS_NAME);
        testLens.addAllowedMiddleSource(dataSource);
        UriPattern uriPattern = UriPattern.register(pattern, name, UriPatternType.mainUrlPattern);
        System.out.println(pattern);
    }
    
    @Test
    @Ignore //rather slow and old data anyway
    public void testBug1() throws Exception {
        Reporter.println("LoadBug1");
        //Validator.
        loadFile("test-data/buglinkset1.ttl");
    }
 
    //Doible loaded void but all info the same
    @Test
    public void DoubleBugA() throws Exception {
        Reporter.println("DoubleBugA");
        RdfReader reader = RdfFactoryIMS.getReader();
        List<Statement> statements = reader.getStatementList(null, null, null);
        for (Statement statement:statements){
            System.out.println(statement);
        }
        File file = new File("test-data/void1A.ttl");
        reader.loadFile(file, file.toURI().toString());
        statements = reader.getStatementList(null, null, null);
        for (Statement statement:statements){
            System.out.println(statement);
        }
        file = new File("test-data/void2A.ttl");
        reader.loadFile(file, file.toURI().toString());
        //Validator.
        statements = reader.getStatementList(null, null, null);
        for (Statement statement:statements){
            System.out.println(statement);
        }
        loadFile("test-data/doubleA.ttl");
    }

    //Double loaded void but all justification changed
    @Test (expected =  BridgeDBException.class)
    public void DoubleBugB() throws Exception {
        Reporter.println("DoubleBugB");
        RdfReader reader = RdfFactoryIMS.getReader();
        File file = new File("test-data/void1B.ttl");
        reader.loadFile(file, file.toURI().toString());
        file = new File("test-data/void2B.ttl");
        reader.loadFile(file, file.toURI().toString());
        //Validator.
        loadFile("test-data/doubleB.ttl");
    }

    @Test
    public void testBugDrugbank() throws Exception {
        Reporter.println("BugDrugbank");
        SQLUriMapper sqlUriMapper = SQLUriMapper.createNew();
        RegexUriPattern pattern = sqlUriMapper.toUriPattern("http://drugbank.ca/drugs/DB01269");
        System.out.println(pattern);
        assertNotNull(pattern);
    }
    
}
