/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfFactory;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class RdfFactoryIMS {

    public static RdfReader getReader() throws BridgeDBException {
        try {
            if (ConfigReader.inTestMode()){
                return RdfFactory.getTestFilebase();
            } else {
                return RdfFactory.getImsFilebase();
            }
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException("Unable to get RDFReader.", ex);
        }
    }

}
