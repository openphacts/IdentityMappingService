/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bridgedb.rdf.constants.PavConstants;
import org.bridgedb.rdf.constants.VoidConstants;
import org.bridgedb.sql.transative.DirectMapping;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.api.Mapping;
import org.bridgedb.uri.tools.DirectStatementMaker;
import org.bridgedb.uri.tools.StatementMaker;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfMinimalInterface;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.ims.rdf.RdfFactoryIMS;

/**
 *
 * @author Christian
 */
public class ImsStatementMaker extends DirectStatementMaker implements StatementMaker{

    private final RdfMinimalInterface rdfInterface;
    
    public ImsStatementMaker() throws BridgeDBException{
        rdfInterface = RdfFactoryIMS.getReader();
    }
    
    @Override
    public Set<Statement> asRDF(MappingSetInfo info, String baseUri, String contextString) throws BridgeDBException{
        HashSet<Statement> results = new HashSet<Statement>();
        URI resourceUri = toURI(info.getMappingResource());
            
        URI source = toURI(info.getMappingSource());
        URI context = new URIImpl(contextString);
        
        //TODO should be added at readtime.
        results.add(new ContextStatementImpl(resourceUri, PavConstants.IMPORTED_FROM, source, context));
        
        addLinksetInfo(results,  resourceUri, context);
        
        return results;
    }

    private void  addLinksetInfo(Set<Statement> statements,  URI resourceUri, URI context) throws BridgeDBException{
        try {
            List<Statement> rdfStatements = rdfInterface.getStatementList(resourceUri);
            for (Statement st: rdfStatements){
                statements.add(new ContextStatementImpl(st.getSubject(), st.getPredicate(), st.getPredicate(), context));
                if (st.getPredicate().equals(VoidConstants.SUBJECTSTARGET) 
                        || st.getPredicate().equals(VoidConstants.OBJECTSTARGET)){
                    addMappingSetInfo(statements,  st.getPredicate(), context);
                }
            }
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException ("Error reading rdf for " + resourceUri.stringValue());
        }   
    }

    private void addMappingSetInfo(Set<Statement> results, Resource subject, URI context) throws BridgeDBException {
        try {
            List<Statement> rdfStatements = rdfInterface.getStatementList(subject);
            for (Statement st: rdfStatements){
                results.add(new ContextStatementImpl(st.getSubject(), st.getPredicate(), st.getPredicate(), context));
            }
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException ("Error reading rdf for " + subject.stringValue());
        }
    }
    
    @Override
    protected void addMappingVoid(Set<Statement> statements, DirectMapping directMapping, URI mappingSet) 
            throws BridgeDBException {
        URI resourceUri = toURI(directMapping.getMappingResource());
        statements.add(new ContextStatementImpl(mappingSet, PavConstants.DERIVED_FROM, resourceUri, mappingSet));
        addLinksetInfo(statements,  resourceUri, mappingSet);
    }
        
}
