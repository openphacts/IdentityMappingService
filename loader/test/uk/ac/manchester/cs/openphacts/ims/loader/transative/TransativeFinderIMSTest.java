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
import org.bridgedb.uri.lens.Lens;
import org.bridgedb.uri.lens.LensTools;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.junit.Before;
import org.openrdf.OpenRDFException;

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
//        linksetLoader = new LinksetLoader();
//        linksetLoader.clearExistingData( StoreType.TEST);  
        setupPattern("TransativeTestA", "http://www.example.com/DS_A/$id");
        setupPattern("TransativeTestB", "http://www.example.com/DS_B/$id");
        setupPattern("TransativeTestC", "http://www.example.com/DS_C/$id");
        setupPattern("TransativeTestD", "http://www.example.com/DS_D/$id");
        setupPattern("TransativeTestE", "http://www.example.com/DS_E/$id");
        setupPattern("TransativeTestF", "http://www.example.com/DS_F/$id");
        mapper = SQLUriMapper.createNew();
 	}

    private void setupPattern (String name, String pattern) throws BridgeDBException{
        DataSource dataSource = DataSource.register(name, name).urlPattern(pattern).asDataSource();
        Lens testLens = LensTools.byId(Lens.TEST_LENS_NAME);
        testLens.addAllowedMiddleSource(dataSource);
        UriPattern uriPattern = UriPattern.register(pattern, name, UriPatternType.mainUrlPattern);
    }
    
}
