/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.manchester.cs.openphacts.ims.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.bridgedb.mysql.MySQLAccess;
import org.bridgedb.sql.SQLAccess;
import static org.bridgedb.sql.SQLListener.ID_COLUMN_NAME;
import static org.bridgedb.sql.SQLListener.MAPPING_SET_TABLE_NAME;
import org.bridgedb.sql.SqlFactory;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author christian
 */
public class UniportReader {
    
    private static void readFile(String fileName) throws IOException, BridgeDBException, SQLException{
        SQLAccess sqlAccess = SqlFactory.createASQLAccess("temp");
        Connection possibleOpenConnection = sqlAccess.getConnection();
        FileReader reader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String insert = "INSERT INTO mapping (uniprot, ensembl) VALUES(?, ?);";
        PreparedStatement insertStatement = possibleOpenConnection.prepareStatement(insert);
        while (bufferedReader.ready()){
            String line = bufferedReader.readLine();
            String[] parts = line.split("\t");
            if (parts.length >= 20 && !parts[19].isEmpty()){
                String[] ensembls = parts[19].split("; ");
                for (String ensembl:ensembls){
                    insertStatement.setString(1, parts[0]);
                    insertStatement.setString(2, ensembl);
                    insertStatement.execute();
                }
            }
        }
        insertStatement.close();
    }
    
    public static void main(String[] args) throws Exception {
        readFile("C:\\OpenPhacts\\uniport\\idmapping_selected.tab");
    }

}
