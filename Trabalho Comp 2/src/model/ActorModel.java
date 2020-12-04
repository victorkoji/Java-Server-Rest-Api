package model;

import webserver.ConexaoMySQL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Actor;
import java.sql.PreparedStatement;

public class ActorModel {
	private Connection conn = ConexaoMySQL.getConexao();
	
	public ActorModel() {
		// TODO Auto-generated constructor stub
	}
	
	public void cadastrar(Actor actor) {
		String query = "insert into actors(name, birthdate)values(?,?)";
		try(PreparedStatement preparestatement = (PreparedStatement) conn.prepareStatement(query)) {
			
			preparestatement.setString(1, actor.getName()); //substitui o ? pelo dado do usuario
			preparestatement.setString(2, actor.getBirthdate());
			
			//executando comando sql
			preparestatement.execute();
			preparestatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public JSONArray getList(int id) throws Exception {
		String query = "SELECT * FROM actors WHERE id =" + id;
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
	
	public JSONArray getList() throws Exception {
		String query = "SELECT * FROM actors";
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
	            obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
	                    .toLowerCase(), resultSet.getObject(i + 1));

	        }
	        jsonArray.put(obj);
	    }
	    return jsonArray;
    }
}
