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
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.IOException;
import org.bridgedb.IDMapperException;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.utils.Reporter;
import org.bridgedb.utils.StoreType;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *  This is a hack as it depends on the files being in the actact locations
 * @author Christian
 */
public class RunLoader {

    private static URI GENERATE_PREDICATE = null;
    private static URI USE_EXISTING_LICENSES = null;
    private static URI NO_DERIVED_BY = null;
    private static final boolean LOAD = true;
    
    Loader loader;
    
    public RunLoader() throws BridgeDBException {
        SQLUriMapper.factory(true, StoreType.LOAD);
        loader = new Loader(StoreType.LOAD);
    }
    
    private int load(String uri) throws BridgeDBException, VoidValidatorException{
        Reporter.println("Loading " + uri);
        return loader.load(uri, null);
    }
       
    public static void main(String[] args) throws IDMapperException, RDFHandlerException, IOException, BridgeDBException, VoidValidatorException  {
        ConfigReader.logToConsole();

        RunLoader runLoader = new RunLoader();

        //runLoader.load("https://www.dropbox.com/sh/6dov4e3drd2nvs7/0BCh1lgh5Y/ChemblOldMolecule-ChemblOldId.ttl");
        //runLoader.load("https://www.dropbox.com/sh/6dov4e3drd2nvs7/o0uW19eXTP/ChemblOldTargets-Enzyme.ttl");
        //runLoader.load("https://www.dropbox.com/sh/6dov4e3drd2nvs7/XUg5S95NCN/ChemblOldTargets-Swissprot.ttl"); 
        //runLoader.load("https://www.dropbox.com/sh/6dov4e3drd2nvs7/rIGy-VEUZz/ChembOldTargets-ChemblOldId.ttl"); 
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-ChemSpider.ttl"); 
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/Chembl13Id-ChemSpider.ttl");
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-DrugbankTargets.ttl");
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-GO.ttl");
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-MSH.ttl");
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-NCIM.ttl");
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-Pdb.ttl");
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-Swissprot.ttl");
        runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ChemSpider-DrugBankDrugs.ttl"); 
//        transativeFinder.UpdateTransative();

        //CS -> Chebi
 //       runLoader.load("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/LINKSET_EXACTMATCH_CHEBI20121023.ttl");
//        transativeFinder.UpdateTransative();
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/ChEBI102VoID.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_functional_parentChEBI102Linkset.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_parent_hydrideChEBI102Linkset.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_partChEBI102Linkset.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_roleChEBI102Linkset.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_conjugate_acid_ofChEBI102Linkset.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_conjugate_base_ofChEBI102Linkset.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_enantiomer_ofChEBI102Linkset.ttl");
//        runLoader.load("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_tautomer_ofChEBI102Linkset.ttl");
//        transativeFinder.UpdateTransative();

    }

}
