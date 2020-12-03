package controllers;

import java.io.IOException;
import java.sql.ResultSet;

import org.json.JSONArray;

import entity.Actor;
import model.ActorModel;
import model.MovieModel;

public class MovieController {

	public MovieController() {
		// TODO Auto-generated constructor stub
	}
	
	public JSONArray getListMovies() throws Exception{
		MovieModel movie = new MovieModel();
		JSONArray result = movie.getList();
		
		return result;

	}
}
