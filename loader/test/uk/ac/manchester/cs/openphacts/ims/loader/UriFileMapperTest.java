/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.Reporter;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author Christian
 */
@Ignore //See comment on each test why these should be ignored
public class UriFileMapperTest {
    
    private static final String URI_BASE = "http://www.example.com/UriFileMapperTest#";
    private static final String FILE_BASE = "a:\\UriFileMapperTest\\";

    public UriFileMapperTest() throws BridgeDBException {
        UriFileMapper.init();
        UriFileMapper.addMapping(URI_BASE,FILE_BASE);
    }
    
     /**
     * Test of getUri method, of class UriFileMapper.
     */ //URis and paths are systems sensitive so this will fail on a different platform
    @Test
    public void testGetUri() throws BridgeDBException {
        Reporter.println("getUri");
        String ending = "abc";
        File file = new File (FILE_BASE+ ending);
        URI expResult = new URIImpl(URI_BASE + ending);
        URI result = UriFileMapper.getUri(file);
        assertEquals(expResult, result);
    }

    /**
     * Test of toFile method, of class UriFileMapper.
     */ //Does not work unless expResult exists!
    @Test
    public void testToFile() throws BridgeDBException {
        Reporter.println("toFile");
        String ending = "abc/123";
        String uri = URI_BASE + ending;
        File expResult = new File (FILE_BASE+ ending);
        File result = UriFileMapper.toFile(uri);
        assertEquals(expResult, result);
    }
}
