// OpenPHACTS RDF Validator,
// A tool for validating and storing RDF.
//
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  University of Manchester
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
package uk.ac.manchester.cs.openphacts.ims.loader.transative;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.bridgedb.loader.transative.TransativeCreator;
import org.bridgedb.rdf.constants.VoidConstants;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.CalendarLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import uk.ac.manchester.cs.openphacts.ims.constants.DctermsConstants;
import uk.ac.manchester.cs.openphacts.ims.constants.PavConstants;
import uk.ac.manchester.cs.openphacts.ims.loader.RdfFactoryIMS;
import uk.ac.manchester.cs.openphacts.valdator.constants.RdfConstants;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
class TransativeCreatorIMS extends TransativeCreator{
 
    private final RdfReader reader;
    private static final Value ANY_OBJECT = null;
    private static final String VERSION = "1.2";

    protected TransativeCreatorIMS(MappingSetInfo left, MappingSetInfo right, StoreType storeType) 
            throws BridgeDBException, IOException{
        super(left, right, storeType);
        reader = RdfFactoryIMS.getReader(storeType);
    }

    @Override
    protected void writeHeader(RDFWriter writer) throws BridgeDBException, RDFHandlerException {
        URI leftId = new URIImpl(leftInfo.getMappingSource());
        URI rightId = new URIImpl(rightInfo.getMappingSource());
        String baseUri = RdfFactoryIMS.getBaseURI();

        URI newId = new URIImpl(baseUri + VERSION + "_" + leftInfo.getStringId() + "_" + rightInfo.getStringId());
        writer.handleStatement(new StatementImpl(newId, RdfConstants.TYPE_URI, VoidConstants.LINKSET));
            
        String title = "Transative linkset from " + leftInfo.getStringId() + " to " + rightInfo.getStringId();
        Value titleValue = new LiteralImpl(title);
        writer.handleStatement(new StatementImpl(newId, DctermsConstants.TITLE, titleValue));
           
        writer.handleStatement(new StatementImpl(newId, DctermsConstants.DESCRIPTION, titleValue));

        Value subject = getObject(leftId, VoidConstants.SUBJECTSTARGET);
        writer.handleStatement(new StatementImpl(newId, VoidConstants.SUBJECTSTARGET, subject));
        Value object = getObject(rightId, VoidConstants.OBJECTSTARGET);
        writer.handleStatement(new StatementImpl(newId, VoidConstants.OBJECTSTARGET, object));

        try {
            List<Statement> liscenceStatements = reader.getStatementList(leftId, DctermsConstants.LICENSE, ANY_OBJECT);
            liscenceStatements.addAll(reader.getStatementList(rightId, DctermsConstants.LICENSE, ANY_OBJECT));
            for (Statement liscenceStatement:liscenceStatements){
                writer.handleStatement(new StatementImpl(newId, DctermsConstants.LICENSE, liscenceStatement.getObject()));
            }
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException ("Error getting statements for " + DctermsConstants.LICENSE, ex);
        }
            
        writer.handleStatement(new StatementImpl(newId, VoidConstants.LINK_PREDICATE, this.predicate));

        writer.handleStatement(new StatementImpl(newId, PavConstants.VERSION, new LiteralImpl(VERSION)));

        writer.handleStatement(new StatementImpl(newId, PavConstants.CREATED_WITH, 
                new URIImpl("https://github.com/openphacts/IdentityMappingService")));
                
        writer.handleStatement(new StatementImpl(newId, PavConstants.CREATED_BY, 
                new URIImpl("https://github.com/openphacts/IdentityMappingService")));

        try {
       		//Calendar cal = Calendar.getInstance();
        	//Date now = cal.getTime();
            GregorianCalendar c = new GregorianCalendar();
            //c.setTime(now);
            XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            Value nowValue = new CalendarLiteralImpl(date); 
            writer.handleStatement(new StatementImpl(newId, PavConstants.CREATED_ON, nowValue));
        } catch (DatatypeConfigurationException ex) {
            throw new BridgeDBException ("Unable to create a Date", ex);
		}
    }

    private Value getObject(Resource subject, URI predicate) throws BridgeDBException{
        try {
            List<Statement> statements = reader.getStatementList(subject, predicate, ANY_OBJECT);
            if (statements.isEmpty()){
                throw new BridgeDBException ("No Statement found for " + subject + " and " + predicate);            
            }
            if (statements.size() > 1){
                String error = "Multiple Statement found for " + subject + " and " + predicate;
                for (Statement statement:statements){
                    error += "/n " + statement;
                }
                throw new BridgeDBException (error);            
            }
            return statements.get(0).getObject();
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException ("Error getting object for " + subject + " and " + predicate, ex);
        }
    }

    private void writeStatement(Resource subject, URI predicate, Value object) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
 
}
