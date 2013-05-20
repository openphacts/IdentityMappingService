package uk.ac.manchester.cs.loader;

import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.LinksetListenerImpl;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.uri.UriListener;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import uk.ac.manchester.cs.openphacts.validator.Validator;
import uk.ac.manchester.cs.openphacts.validator.ValidatorImpl;

/**
 * Hello world!
 *
 */
public class Load 
{
    public static void main( String[] args ) throws BridgeDBException
    {
        Validator validator = new ValidatorImpl();
        UriListener uriListener = SQLUriMapper.factory(true, StoreType.LOAD);
        LinksetListener linksetListener = new LinksetListenerImpl(uriListener);
    }
}
