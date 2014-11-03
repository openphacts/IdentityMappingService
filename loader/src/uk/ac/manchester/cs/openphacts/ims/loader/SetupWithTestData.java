/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.openrdf.rio.RDFHandlerException;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class SetupWithTestData {
    
    public static void main(String[] args) throws BridgeDBException, VoidValidatorException, RDFHandlerException, IOException  {
        ConfigReader.logToConsole();
        File file = new File("test-data/load.xml");
        URI uri = file.toURI();
        String[] loadArgs = new String[1];
        loadArgs[0] = uri.toString();
        RunLoader.main(loadArgs);
    }

 }
