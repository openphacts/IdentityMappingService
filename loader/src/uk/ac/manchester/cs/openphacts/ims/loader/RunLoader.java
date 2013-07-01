/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.utils.Reporter;
import org.bridgedb.utils.StoreType;
import org.openrdf.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.manchester.cs.openphacts.ims.loader.transative.TransativeFinderIMS;
import uk.ac.manchester.cs.openphacts.valdator.metadata.MetaDataSpecification;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.validator.Validator;
import uk.ac.manchester.cs.openphacts.validator.ValidatorImpl;

/**
 *
 * @author Christian
 */
public class RunLoader {
    
    private static final String CLEAR_ALL = "clearAll";
    private static final String LINKSET = "linkset";
    private static final String VOID = "void";
    private static final String DO_TRANSITIVE = "doTransitive";      
    
    private final Loader loader;
    private final RdfReader reader;

    public RunLoader(boolean clear) throws BridgeDBException, VoidValidatorException {
        reader = RdfFactoryIMS.getReader(StoreType.LOAD);
        if (clear){
            SQLUriMapper.factory(true, StoreType.LOAD);
            reader.clear();
        }
        loader = new Loader(StoreType.LOAD);
    }
    

    public static void clean(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int n = childNodes.getLength() - 1; n >= 0; n--) {
            Node child = childNodes.item(n);
            short nodeType = child.getNodeType();
            if (nodeType == Node.ELEMENT_NODE){
                clean(child);
            } else if (nodeType == Node.TEXT_NODE) {
                String trimmedNodeVal = child.getNodeValue().trim();
                if (trimmedNodeVal.length() == 0) {
                    node.removeChild(child);
                } else {
                    child.setNodeValue(trimmedNodeVal);
                }
            } else if (nodeType == Node.COMMENT_NODE) {
                node.removeChild(child);
            }
        }
    }

    private void loadLinkset(String uri) throws BridgeDBException, VoidValidatorException{
        Reporter.println("Loading " + uri);
        //Validator validator = new ValidatorImpl();
        //String result = validator.validateUri(uri, null, "opsVoid", Boolean.TRUE);
        //System.out.println(result);
        loader.load(uri, null);
    }
       
    private void loadVoid(String uri) throws BridgeDBException, VoidValidatorException{
        Reporter.println("Loading " + uri);
        reader.loadURI(uri);
    }
    
    public static void main(String argv[]) throws BridgeDBException {           
        RunLoader runLoader = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;
            if (argv.length == 0){
            	File fXmlFile = new File("test-data/load.xml");
                doc = dBuilder.parse(fXmlFile);
            } else {
                URL url = new URL(argv[0]);
                InputStream stream = url.openStream();
                doc = dBuilder.parse(stream);
            }    
            Element root = doc.getDocumentElement();
            clean(root);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            root.normalize();
 
            NodeList nList = root.getChildNodes();
 
        	for (int temp = 0; temp < nList.getLength(); temp++) {
         		Node nNode = nList.item(temp);
                if (nNode instanceof Element){
                    String name = nNode.getNodeName();
                    String uri = nNode.getTextContent();
                    if (name.equals(CLEAR_ALL)){
                        runLoader = new RunLoader(true);
                    } else if (name.equals(LINKSET)){
                        if (runLoader == null){
                            runLoader = new RunLoader(false);
                        }
                        runLoader.loadLinkset(uri);
                    } else if (name.equals(VOID)){
                        runLoader.loadVoid(uri);
                    } else if (name.equals(DO_TRANSITIVE)){
                        TransativeFinderIMS transativeFinder = new TransativeFinderIMS(StoreType.LOAD);
                        transativeFinder.UpdateTransative();
                    } else {
                        Reporter.error("Unexpected element " + name);
                    }
                } else {
                    Reporter.error("Unexpected node " + nNode + " type " + nNode.getClass());            
                }
            }
        } catch (Exception e) {
            throw new BridgeDBException("Error loading ", e);
        }
    }
 
}