package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import webserver.ConexaoMySQL;

public class MovieModel {
	private Connection conn = ConexaoMySQL.getConexao();
	public MovieModel() {
		// TODO Auto-generated constructor stub
	}
	
	public JSONArray cadastrar(String title, String synopsis) {
		title = title.replaceAll("'","");
		synopsis = synopsis.replaceAll("'","");
		String query = String.format("INSERT INTO movies(title, synopsis) VALUES('%s', '%s')", title, synopsis);
		
		try(PreparedStatement preparestatement = (PreparedStatement) conn.prepareStatement(query)) {
			
		      // execute the query, and get a java resultset
		      int rs = preparestatement.executeUpdate(query);
		      JSONArray jsonArray = new JSONArray();
		      JSONObject obj = new JSONObject();
		      String response = "O registro foi inserido com sucesso!";
		      
		      if(rs == 0)
		    	  response = "Nao foi possível cadastrar esse registro!";
		      
		      jsonArray.put(obj.put("response", response));
		      
		      preparestatement.close();
				      
		      return jsonArray;
		      
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public JSONArray atualizar(int id, String title, String synopsis) {
		title = title.replaceAll("'","");
		synopsis = synopsis.replaceAll("'","");
		String query = String.format("UPDATE movies "
				+ "SET title = '%s', synopsis = '%s'"
				+ "WHERE id = %d", title, synopsis, id);
		
		try(PreparedStatement preparestatement = (PreparedStatement) conn.prepareStatement(query)) {
			
		      // execute the query, and get a java resultset
		      int rs = preparestatement.executeUpdate(query);
		      JSONArray jsonArray = new JSONArray();
		      JSONObject obj = new JSONObject();
		      String response = "O registro foi inserido com sucesso!";
		      
		      if(rs == 0)
		    	  response = "Nao foi possível cadastrar esse registro!";
		      
		      jsonArray.put(obj.put("response", response));
		      
		      preparestatement.close();
				      
		      return jsonArray;
		      
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/** Busca o movie pelo id **/
	public JSONArray getList(int id) throws Exception {
		String query = "SELECT * FROM movies WHERE id ="+ id;
		
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
	
	/** Busca a lista toda de movies **/
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
	
	public JSONArray delete(int id) throws Exception {
		String query = "DELETE FROM movies WHERE id=" + id;
		try(PreparedStatement preparestatement = (PreparedStatement) conn.prepareStatement(query)) {
		      // execute the query, and get a java resultset
		      int rs = preparestatement.executeUpdate(query);
		      JSONArray jsonArray = new JSONArray();
		      JSONObject obj = new JSONObject();
		      String response = "O registro com id "+id+" foi deletado com sucesso.";
		      
		      if(rs == 0)
		    	  response = "Nao foi encontrado esse registro!";
		      
		      jsonArray.put(obj.put("response", response));
		      
		      preparestatement.close();
				      
		      return jsonArray;
		      
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/** Busca um actor pelo id **/
	public JSONArray getListActorsByMovies(int id) throws Exception {
		String query = ""
				+ "SELECT a.id, a.name, a.birthdate FROM movie_actors AS ma "
				+ "INNER JOIN actors AS a ON a.id = ma.actorid "
				+ "INNER JOIN movies AS m ON m.id = ma.movieid "
				+ "WHERE movieid =" + id;
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
	            obj.put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), 
	            		resultSet.getObject(i + 1));

	        }

	        jsonArray.put(obj);
	    }
	    return jsonArray;
    }

}
