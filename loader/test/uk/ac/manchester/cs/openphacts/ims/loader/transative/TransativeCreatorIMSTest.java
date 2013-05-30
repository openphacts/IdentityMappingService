// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009  BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
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
package uk.ac.manchester.cs.openphacts.ims.loader.transative;

import java.io.File;
import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.transative.TransativeCreatorTest;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.DirectoriesConfig;
import org.bridgedb.utils.Reporter;
import org.bridgedb.utils.StoreType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import uk.ac.manchester.cs.openphacts.ims.loader.Loader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfFactory;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class TransativeCreatorIMSTest  {
    
    protected static final String MAIN_JUSTIFCATION = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
    static final String LENS_JUSTIFCATION = "http://www.bridgedb.org/test#testJustification";
    static final URI linkPredicate = new URIImpl("http://www.w3.org/2004/02/skos/core#exactMatch");
    static SQLUriMapper uriListener;
    static RdfReader reader;
    static Loader instance;

    @BeforeClass
    public static void setUpClass() throws BridgeDBException, VoidValidatorException {
        instance = new Loader(StoreType.TEST);
        DirectoriesConfig.useTestDirectory();
        uriListener = SQLUriMapper.factory(true, StoreType.TEST);
        reader = RdfFactory.getTestFilebase();
    }
    
    protected void loadFile(String fileName, String justification) throws BridgeDBException, VoidValidatorException{
        File file = new File(fileName);
        loadFile(file, justification);
    }
    
    protected void loadFile(File file, String justification) throws BridgeDBException, VoidValidatorException{
        Reporter.println("parsing " + file.getAbsolutePath());
        int mappingSetId = instance.load(file, null);
        MappingSetInfo mapping = uriListener.getMappingSetInfo(mappingSetId);
        int numberOfLinks = mapping.getNumberOfLinks();
        assertThat(numberOfLinks, greaterThanOrEqualTo(3));      
    }
    
    /**
     * Test of parse method, of class LinksetListener.
     */
    @Test
    public void testLoadTestData() throws Exception {
        Reporter.println("LoadTestData");
        //Validator.
        loadFile("test-data/cw-cs.ttl", MAIN_JUSTIFCATION);
        loadFile("test-data/cs-cm.ttl", MAIN_JUSTIFCATION);
        File transative = TransativeCreatorIMS.doTransativeIfPossible(1, 3, StoreType.TEST);
        System.out.println(transative.getAbsolutePath());
        loadFile(transative, MAIN_JUSTIFCATION);
    }

 }
