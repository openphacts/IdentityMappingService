/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.Reporter;
import org.bridgedb.utils.StoreType;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class SetupWithTestData {
    
    Loader loader;
    
    public SetupWithTestData() throws BridgeDBException {
        SQLUriMapper.factory(true, StoreType.LOAD);
        loader = new Loader(StoreType.LOAD);
    }
    
    private void loadUri(String uri) throws BridgeDBException, VoidValidatorException{
        Reporter.println("Loading " + uri);
        loader.load(uri, null);
     }
    /**
     * Test of parse method, of class LinksetListener.
     */
   public static void main(String[] args) throws BridgeDBException, VoidValidatorException {
        Reporter.println("LoadTestData");
        SetupWithTestData loader = new SetupWithTestData();
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cs-cm.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cs-cm.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cw-cm.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cw-ct.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cw-dd.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cw-dt.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cw-cs_test_lens.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cs-cm_test_lens.ttl");
        loader.loadUri("https://github.com/openphacts/BridgeDb/blob/Christian/org.bridgedb.uri.loader/test-data/cw-cm_test_lens.ttl");
    }

 }
