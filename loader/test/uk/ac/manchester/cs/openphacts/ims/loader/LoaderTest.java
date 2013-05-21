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
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfFactory;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.Reporter;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class LoaderTest {
    
    static Loader instance;
    static SQLUriMapper uriListener;
    static RdfReader reader;
             
    public LoaderTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() throws BridgeDBException, VoidValidatorException {
        instance = new Loader(StoreType.TEST);
        uriListener = SQLUriMapper.factory(true, StoreType.TEST);
        reader = RdfFactory.getTestFilebase();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of load method, of class Loader.
     */
    @Test
    public void testLoadFile() throws Exception {
        Reporter.println("loadFile");
        File file  = new File("test-data/cw-cs.ttl");
        String formatName = null;
        LoaderResult result = instance.load(file, formatName);
        MappingSetInfo mapping = uriListener.getMappingSetInfo(result.getMappingSetID());
        int numberOfLinks = mapping.getNumberOfLinks();
        assertThat(numberOfLinks, greaterThanOrEqualTo(3));
        assertEquals(mapping.getMappingSource(), result.getLinksetId().stringValue());
        List<Statement> statements = reader.getStatementList(null, null,  null, result.getLinksetContext());
        assertThat(statements.size(), greaterThanOrEqualTo(3));
        statements = reader.getStatementList(result.getLinksetId());
        assertThat(statements.size(), greaterThanOrEqualTo(3));
    }

   
}
