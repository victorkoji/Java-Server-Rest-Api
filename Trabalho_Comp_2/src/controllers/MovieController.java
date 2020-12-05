package controllers;

import java.io.IOException;
import java.sql.ResultSet;

import org.json.JSONArray;

import model.ActorModel;
import model.MovieModel;

public class MovieController {

	public MovieController() {
		// TODO Auto-generated constructor stub
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
