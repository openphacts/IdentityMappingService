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
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import uk.ac.manchester.cs.openphacts.ims.loader.transative.TransativeFinderIMS;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 * @author Christian
 */
public class RunLoader {

    private static URI GENERATE_PREDICATE = null;
    private static URI USE_EXISTING_LICENSES = null;
    private static URI NO_DERIVED_BY = null;
    private static final boolean LOAD = true;
    
    private final Loader loader;
    private final RdfReader reader;

    public RunLoader() throws BridgeDBException, VoidValidatorException {
        SQLUriMapper.factory(true, StoreType.LOAD);
        reader = RdfFactoryIMS.getReader(StoreType.LOAD);
        reader.clear();
        loader = new Loader(StoreType.LOAD);
    }
    
    private int loadLinkset(String uri) throws BridgeDBException, VoidValidatorException{
        Reporter.println("Loading " + uri);
        return loader.load(uri, null);
    }
       
    private Resource loadVoid(String uri) throws BridgeDBException, VoidValidatorException{
        Reporter.println("Loading " + uri);
        return reader.loadURI(uri);
    }
    
    public static void main(String[] args) throws IDMapperException, RDFHandlerException, IOException, BridgeDBException, VoidValidatorException  {
        ConfigReader.logToConsole();

        RunLoader runLoader = new RunLoader();
        TransativeFinderIMS transativeFinder = new TransativeFinderIMS(StoreType.LOAD);

/*        //Version 1.1
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/Chembl13Molecule-Chembl13Id_nov12.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/Chembl13Targets-Enzyme.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/Chembl13Targets-Swissprot.ttl"); 
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-ChemSpider.ttl"); 
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/Chembl13Id-ChemSpider.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-DrugbankTargets.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-GO.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-MSH.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-NCIM.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-Pdb.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ConceptWiki-Swissprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/ChemSpider-DrugBankDrugs.ttl"); 

        //version 1.2 additional CS -> Chebi
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.1/LINKSET_EXACTMATCH_CHEBI20121023.ttl");
        runLoader.loadVoid("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/ChEBI102VoID.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_functional_parentChEBI102Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_parent_hydrideChEBI102Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_partChEBI102Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/has_roleChEBI102Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_conjugate_acid_ofChEBI102Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_conjugate_base_ofChEBI102Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_enantiomer_ofChEBI102Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi102/is_tautomer_ofChEBI102Linkset.ttl");
        transativeFinder.UpdateTransative();
*/
        //Version 1.3.alpha1 There be dragons
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/CW_ChemSpider.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/CW_DBTarget.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/CW_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Ensemble_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Flybase_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/LINKSET_EXACT_CHEBI20130408.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/LINKSET_EXACT_CHEMBL20130408.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/LINKSET_EXACT_DRUGBANK20130408.ttl");        
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/MGD_Uniport.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Ncbigene_uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/RGD_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/UniProt_Unigene.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/UniProt_mgi.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/UniProt_rgd.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_Ensembl.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_GeneID.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_ipi.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_ncbigene.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_pdb.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_refseq.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_sgd.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Uniprot_zfin.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/WPHMDBMetabolites_ChemSpider.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/Wormbase_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/ZDB-GENE_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/aers_drugbank_linkset.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/chembl_16_target_chembl_16_targetcmpt_ls.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/chembl_16_targetcmpt_uniprot_ls.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/refseq_uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/sgd_Uniprot.ttl");
        runLoader.loadLinkset("http://openphacts.cs.man.ac.uk/ims/linkset/version1.3.alpha1/uniprot_omim.ttl");
        runLoader.loadVoid("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/ChEBI104VoID.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/has_functional_parentChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/has_parent_hydrideChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/has_partChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/has_roleChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/is_conjugate_acid_ofChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/is_enantiomer_ofChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/is_enantiomer_ofChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/is_substituent_group_fromChEBI104Linkset.ttl");
        runLoader.loadLinkset("https://github.com/openphacts/ops-platform-setup/blob/master/void/chebi/chebi104/is_tautomer_ofChEBI104Linkset.ttl");
        
        transativeFinder.UpdateTransative();

    }

}
