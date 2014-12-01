/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.rdf;

import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfFactory;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;

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
                return RdfFactory.getFilebase();
            }
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException("Unable to get RDFReader.", ex);
        }
    }

}
