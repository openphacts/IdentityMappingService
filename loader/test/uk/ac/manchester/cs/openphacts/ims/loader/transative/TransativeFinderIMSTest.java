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

import java.io.FileNotFoundException;
import java.io.IOException;
import org.bridgedb.DataSource;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.rdf.UriPatternType;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.OverallStatistics;
import org.bridgedb.uri.Lens;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.utils.Reporter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFHandlerException;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class TransativeFinderIMSTest extends TransativeTestBase{
 
    SQLUriMapper mapper;
   
    @Before
    public void testLoader() throws BridgeDBException, IOException, OpenRDFException, FileNotFoundException {
        //Check database is running and settup correctly or kill the test. 
        ConfigReader.useTest();
        mapper = SQLUriMapper.createNew();
//        linksetLoader = new LinksetLoader();
//        linksetLoader.clearExistingData( StoreType.TEST);  
        setupPattern("TransativeTestA", "http://www.example.com/DS_A/$id");
        setupPattern("TransativeTestB", "http://www.example.com/DS_B/$id");
        setupPattern("TransativeTestC", "http://www.example.com/DS_C/$id");
        setupPattern("TransativeTestD", "http://www.example.com/DS_D/$id");
        setupPattern("TransativeTestE", "http://www.example.com/DS_E/$id");
        setupPattern("TransativeTestF", "http://www.example.com/DS_F/$id");
 	}

    private void setupPattern (String name, String pattern) throws BridgeDBException{
        DataSource dataSource = DataSource.register(name, name).asDataSource();
        TransativeFinderIMS.addAcceptableVai(dataSource);
        UriPattern uriPattern = UriPattern.register(pattern, name, UriPatternType.mainUrlPattern);
    }
    
    @Test
	public void testFinder1() throws BridgeDBException, RDFHandlerException, IOException, VoidValidatorException {	
        Reporter.println("testFinder1");
        loadFile("test-data/sampleAToB.ttl");
        loadFile("test-data/sampleEToD.ttl");
        loadFile("test-data/sampleAToC.ttl");
        loadFile("test-data/sampleAToD.ttl");
        TransativeFinderIMS transativeFinder = new TransativeFinderIMS();
        transativeFinder.UpdateTransative();
        OverallStatistics results = mapper.getOverallStatistics(Lens.getAllLens());
        assertEquals(20, results.getNumberOfMappingSets());
        Reporter.println("testFinder Done");
	}
	
}
