package controllers;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import model.MovieModel;

public class MovieController {

	public MovieController() {
		
	}
	
	public JSONArray cadastrarMovie(JSONObject request) throws IOException{
		String nome = request.getString("title");
		String synopsis = request.getString("synopsis");

		MovieModel movieDAO = new MovieModel();

		JSONArray result = movieDAO.cadastrar(nome, synopsis);
	
		return result;
	}
	
	public JSONArray atualizarMovie(JSONObject request) throws IOException{
		int id = request.getInt("id");
		String nome = request.getString("title");
		String synopsis = request.getString("synopsis");

		MovieModel movieDAO = new MovieModel();

		JSONArray result = movieDAO.atualizar(id, nome, synopsis);
	
		return result;
	}
	
	public JSONArray getListMovies(String param) throws Exception{
		MovieModel movie = new MovieModel();
		
		int id = Integer.parseInt(param);
		
		JSONArray result = movie.getList(id);
		
		return result;
	}
	
	public JSONArray getListMovies() throws Exception{
		MovieModel movie = new MovieModel();
		JSONArray result = movie.getList();
		
		return result;
	}
	
	public JSONArray getListActorsByMovies(String param) throws Exception{

		int id = Integer.parseInt(param);
		
		MovieModel movieDAO = new MovieModel();
		JSONArray result =  movieDAO.getListActorsByMovies(id);

		return result;

	}
	
	public JSONArray deleteMovie(String param) throws Exception{
		MovieModel movie = new MovieModel();

		int id = Integer.parseInt(param);
		
		JSONArray result = movie.delete(id);
		
		return result;
	}
}
