package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Movie;
import webserver.ConexaoMySQL;

public class MovieModel {
	private Connection conn = ConexaoMySQL.getConexao();
	public MovieModel() {
		// TODO Auto-generated constructor stub
	}
	
	public void cadastrar(Movie movie) {
		
	}
	
	public JSONArray getList() throws Exception {
		String query = "SELECT * FROM movies";
		try(PreparedStatement preparestatement = (PreparedStatement) conn.prepareStatement(query)) {
		      // execute the query, and get a java resultset
		      ResultSet rs = preparestatement.executeQuery(query);
		      
		      JSONArray json = convertToJSONArray(rs);
		      
		      preparestatement.close();
		      return json;
		      
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static JSONArray convertToJSONArray(ResultSet resultSet) throws Exception {
	    JSONArray jsonArray = new JSONArray();
	    while (resultSet.next()) {
	    	JSONObject obj = new JSONObject();
	        int total_rows = resultSet.getMetaData().getColumnCount();
	        for (int i = 0; i < total_rows; i++) {
	            obj.put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1));

	        }
	        System.out.println(obj);
	        jsonArray.put(obj);
	    }
	    return jsonArray;
    }

}
