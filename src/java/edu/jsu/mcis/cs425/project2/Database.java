package edu.jsu.mcis.cs425.project2;

import java.util.HashMap;
import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Database 
{
    private Connection _connection;
    
    private Connection getConnection() {        
        if (_connection == null)
        {
            try 
            {            
                Context envContext = new InitialContext();            
                Context initContext  = (Context)envContext.lookup("java:/comp/env");            
                DataSource ds = (DataSource)initContext.lookup("jdbc/db_pool");            
                _connection = ds.getConnection();
            }                
            catch (Exception e) 
            { 
                e.printStackTrace(); 
            }
        }
        
        return _connection;    
    }
    
    protected Connection getDBConnection() {
        return this.getConnection();
    }
    
    protected HashMap getUserInfo(String username)
    {
        HashMap<String, String> hmap = new HashMap<String, String>();
        
        try {
            Connection conn = getDBConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM user WHERE username = ?");
            pstmt.setString(1, username);
            boolean hasresults = pstmt.execute(); 
            
            if (hasresults)
            {
                ResultSet resultset = pstmt.getResultSet();
                if (resultset.next()) 
                {
                    hmap.put("id", resultset.getString("id"));
                    hmap.put("username", resultset.getString("username"));
                    hmap.put("displayname", resultset.getString("displayname"));
                } 
            }
            
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return hmap;
    }
    
    protected String getSkillsListAsHTML(int userId) {
        String result = "<div id=\"checkboxes\"><ul>";
        
        try {            
            Connection conn = getDBConnection();
            String query = "SELECT id, description, IF ((SELECT COUNT(*) > 0 FROM applicants_to_skills" +
                    " WHERE skillsid = skills.id AND userid = '" + userId +  "'), 'checked', '')" +
                    " as status FROM skills;";
            PreparedStatement statement = conn.prepareStatement(query);
            
            ResultSet results = statement.executeQuery();
                        
            while(results.next()){
                result += "<li><input type=\"checkbox\" name=\"skills\" id=\"skills_" + 
                        results.getString("id") + "\" value=\"" + results.getString("id") +
                        "\" " + results.getString("status") + "><label for=\"skills_" +
                        results.getString("id") + "\">" + results.getString("description") +
                        "</label></li>";
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        result += "</ul></div>";
        
        return result;
    }
    
    protected void setSkillsList(int userId, String[] skills) {
        try {
            Connection conn = getDBConnection();
            String query = "DELETE FROM cs425_p2.applicants_to_skills WHERE userId = '" + userId + "';";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.execute(query);
            
            String values = "";
            for (int i = 0; i < skills.length; i++)
            {
                values += "(" + userId + ", " + Integer.parseInt(skills[i]) + ")";
                if ((i + 1) < skills.length)
                {
                    values += ", ";
                }
                else {
                    values += ";";
                }
            }
            
            query = "INSERT INTO cs425_p2.applicants_to_skills (userId, skillsid) VALUES " + values;
            PreparedStatement insertStatement = conn.prepareStatement(query);
            insertStatement.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected String getJobsListAsHTML(int userId) {
        String result = "<div id=\"checkboxes\"><ul>";
        
        try {            
            Connection conn = getDBConnection();
            String query = "SELECT DISTINCT jobs.id, jobs.name, " +
                "IF ((SELECT COUNT(*) > 0 FROM applicants_to_jobs " +
                "WHERE jobsid = jobs.id AND userid = '1'), 'checked', '') " +
                "AS status " +
                "FROM jobs " +
                "JOIN skills_to_jobs " +
                "ON jobs.id = skills_to_jobs.jobsid " +
                "WHERE skills_to_jobs.skillsid IN " +
                "(SELECT skillsid FROM applicants_to_skills " +
                "WHERE userid = '1');";
            PreparedStatement statement = conn.prepareStatement(query);
            
            ResultSet results = statement.executeQuery();
                        
            while(results.next()){
                result += "<li><input type=\"checkbox\" name=\"jobs\" id=\"jobs_" + 
                        results.getString("id") + "\" value=\"" + results.getString("id") +
                        "\" " + results.getString("status") + "><label for=\"jobs_" +
                        results.getString("id") + "\">" + results.getString("name") +
                        "</label></li>";
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        result += "</ul></div>";
        
        return result;
    }
    
    protected void setJobsList(int userId, String[] jobs) {
        try {
            Connection conn = getDBConnection();
            String query = "DELETE FROM cs425_p2.applicants_to_jobs WHERE userId = '" + userId + "';";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.execute(query);
            
            String values = "";
            for (int i = 0; i < jobs.length; i++)
            {
                values += "(" + userId + ", " + Integer.parseInt(jobs[i]) + ")";
                if ((i + 1) < jobs.length)
                {
                    values += ", ";
                }
                else {
                    values += ";";
                }
            }
            
            query = "INSERT INTO cs425_p2.applicants_to_jobs (userId, jobsid) VALUES " + values;
            PreparedStatement insertStatement = conn.prepareStatement(query);
            insertStatement.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}