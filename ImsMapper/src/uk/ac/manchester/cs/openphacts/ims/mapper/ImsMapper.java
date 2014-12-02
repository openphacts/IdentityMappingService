// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009  BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
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
package uk.ac.manchester.cs.openphacts.ims.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.bridgedb.DataSource;
import org.bridgedb.pairs.CodeMapper;
import org.bridgedb.rdf.pairs.RdfBasedCodeMapper;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.sql.SqlFactory;
import org.bridgedb.uri.tools.RegexUriPattern;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;

/**
 *
 * @author christian
 */
public class ImsMapper extends SQLUriMapper implements ImsListener{
    
    private static ImsMapper mapper = null;


    private String registerMappingQuery = null;
    
    private static final Logger logger = Logger.getLogger(ImsMapper.class);
    
    public static ImsMapper getExisting() throws BridgeDBException {
        if (mapper == null) {
            CodeMapper codeMapper = new RdfBasedCodeMapper();
            mapper = new ImsMapper(false, codeMapper);
        }
        return mapper;
    }

    public static ImsMapper createNew() throws BridgeDBException {
        CodeMapper codeMapper = new RdfBasedCodeMapper();
        mapper = new ImsMapper(true, codeMapper);
        return mapper;
    }

    /**
     * Creates a new UriMapper including BridgeDB implementation based on a
     * connection to the SQL Database.
     *
     * @param dropTables Flag to determine if any existing tables should be
     * dropped and new empty tables created.
     */
    private ImsMapper(boolean dropTables, CodeMapper codeMapper) throws BridgeDBException {
        super(dropTables, codeMapper);
    }

    protected void createMappingSetTable() throws BridgeDBException {
        //"IF NOT EXISTS " is not supported
        String query = "";
        Statement sh = null;
        try { 
            sh = createStatement();
            query = "CREATE TABLE " + MAPPING_SET_TABLE_NAME 
                    + " (" + ID_COLUMN_NAME + " INT " + autoIncrement + " PRIMARY KEY, "
                    + SOURCE_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + ") NOT NULL, "
                    + PREDICATE_COLUMN_NAME + " VARCHAR(" + PREDICATE_LENGTH + "), "
                    + JUSTIFICATION_COLUMN_NAME + " VARCHAR(" + JUSTIFICATION_LENGTH + "), "
                    + TARGET_DATASOURCE_COLUMN_NAME + " VARCHAR(" + SYSCODE_LENGTH + "), "
                    + MAPPING_RESOURCE_COLUMN_NAME + " VARCHAR(" + MAPPING_URI_LENGTH + "), "
                    + MAPPING_SOURCE_COLUMN_NAME + " VARCHAR(" + MAPPING_URI_LENGTH + "), "
                    + SYMMETRIC_COLUMN_NAME + " INT, "
                    + MAPPING_LINK_COUNT_COLUMN_NAME + " INT, "
                    + MAPPING_SOURCE_COUNT_COLUMN_NAME + " INT, "
                    + MAPPING_TARGET_COUNT_COLUMN_NAME + " INT"
                    + " ) " + SqlFactory.engineSetting();
            sh.execute(query);
        } catch (SQLException e) {
            throw new BridgeDBException("Error creating the MappingSet table using " + query, e);
        } finally {
            close(sh, null);
        }
    }

    @Override
    public int registerMappingSet(RegexUriPattern sourceUriPattern, String predicate, String justification,
            RegexUriPattern targetUriPattern, Resource mappingResource, Resource mappingSource, boolean symetric) throws BridgeDBException {
        checkUriPattern(sourceUriPattern);
        checkUriPattern(targetUriPattern);
        DataSource source = DataSource.getExistingBySystemCode(sourceUriPattern.getSysCode());
        DataSource target = DataSource.getExistingBySystemCode(targetUriPattern.getSysCode());
        int mappingSetId = registerMappingSet(source, target, predicate, justification, mappingResource, mappingSource, 0);
        if (symetric) {
            int symetricId = registerMappingSet(target, source, predicate, justification, mappingResource, mappingSource, mappingSetId);

            setSymmetric(mappingSetId, symetricId);
        }
        subjectUriPatterns.put(mappingSetId, sourceUriPattern);
        targetUriPatterns.put(mappingSetId, targetUriPattern);
        return mappingSetId;
    }

    @Override
    public int registerMappingSet(RegexUriPattern sourceUriPattern, String predicate, String forwardJustification, String backwardJustification,
            RegexUriPattern targetUriPattern, Resource mappingResource, Resource mappingSource) throws BridgeDBException {
        if (forwardJustification.equals(backwardJustification)){
            return registerMappingSet(sourceUriPattern, predicate, forwardJustification, targetUriPattern, mappingResource, mappingSource, true);
        } else {
            checkUriPattern(sourceUriPattern);
            checkUriPattern(targetUriPattern);
            DataSource source = DataSource.getExistingBySystemCode(sourceUriPattern.getSysCode());
            DataSource target = DataSource.getExistingBySystemCode(targetUriPattern.getSysCode());
            int mappingSetId = registerMappingSet(source, target, predicate, forwardJustification, mappingResource, mappingSource, 0);
            int symetricId = registerMappingSet(target, source, predicate, backwardJustification, mappingResource, mappingSource, 0);
            subjectUriPatterns.put(mappingSetId, sourceUriPattern);
            targetUriPatterns.put(mappingSetId, targetUriPattern);
            //Two linksets are NOT symmetric
            return mappingSetId;
        }
    }

    private int registerMappingSet(DataSource source, DataSource target, String predicate,
            String justification, Resource mappingResource, Resource mappingSource, int symmetric) throws BridgeDBException {
        PreparedStatement statement = null;
        try {
            if (registerMappingQuery == null){
                StringBuilder query = new StringBuilder("INSERT INTO ");
                query.append(MAPPING_SET_TABLE_NAME);
                query.append(" (");
                query.append(SOURCE_DATASOURCE_COLUMN_NAME); //1
                query.append(", ");
                query.append(PREDICATE_COLUMN_NAME); //2
                query.append(", ");
                query.append(JUSTIFICATION_COLUMN_NAME); //3
                query.append(", ");
                query.append(TARGET_DATASOURCE_COLUMN_NAME); //4
                query.append(", ");
                query.append(MAPPING_RESOURCE_COLUMN_NAME); //5
                query.append(", ");
                query.append(MAPPING_SOURCE_COLUMN_NAME); //6
                query.append(", ");
                query.append(SYMMETRIC_COLUMN_NAME); //7
                query.append(") VALUES ( ?, ?, ?, ? , ?, ?, ?)");
                registerMappingQuery = query.toString();
            }
            statement = createPreparedStatement(registerMappingQuery);
            statement.setString(1, getDataSourceKey(source));
            statement.setString(2, predicate);
            statement.setString(3, justification);
            statement.setString(4, getDataSourceKey(target));
            statement.setString(5, mappingResource.stringValue());
            statement.setString(6, mappingSource.stringValue());
            statement.setInt(7, symmetric);
            statement.executeUpdate();
            int autoinc = getAutoInc();
            logger.info("Registered new Mapping " + autoinc + " from " + getDataSourceKey(source) + " to " + getDataSourceKey(target));
            return autoinc;
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error registering mappingSet ", ex);
        } finally {
            close(statement, null);
        }
    }

    @Override
    public int registerMappingSet(RegexUriPattern sourceUriPattern, String predicate, String forwardJustification, 
            String backwardJustification, RegexUriPattern targetUriPattern, Resource mappingSource) 
            throws  BridgeDBException{
        throw new BridgeDBException ("Use method with mappingResource instead");
    }

    @Override
     public int registerMappingSet(RegexUriPattern sourceUriPattern, String predicate, String justification,
            RegexUriPattern targetUriPattern, Resource mappingSource, boolean symetric) throws BridgeDBException{
        throw new BridgeDBException ("Use method with mappingResource instead");
    }
}
