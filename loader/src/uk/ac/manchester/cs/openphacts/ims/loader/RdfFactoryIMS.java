/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfFactory;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class RdfFactoryIMS {

    public static RdfReader getReader(StoreType storeType) throws BridgeDBException {
        try {
            if (storeType == StoreType.TEST){
                return RdfFactory.getTestFilebase();
            } else {
                return RdfFactory.getImsFilebase();
            }
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException("Unable to get RDFReader.", ex);
        }
    }

    public static String getBaseURI() {
        return "http://no/BaseURI/Set/";
    }
    
}
