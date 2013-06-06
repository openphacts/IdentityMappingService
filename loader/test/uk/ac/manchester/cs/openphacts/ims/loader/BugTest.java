/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import org.bridgedb.utils.Reporter;
import org.junit.Test;
import uk.ac.manchester.cs.openphacts.ims.loader.transative.TransativeTestBase;

/**
 *
 * @author Christian
 */
public class BugTest extends TransativeTestBase{
    
    @Test
    public void testBug1() throws Exception {
        Reporter.println("LoadBug1");
        //Validator.
        loadFile("test-data/buglinkset1.ttl");

    }
 
}
