package controllers;

import org.json.JSONArray;

import model.ActorModel;

public class ActorController {

	public ActorController() {

	}
	
	public JSONArray getListActors(String param) throws Exception{

		int id = Integer.parseInt(param);
		
		ActorModel actorDAO = new ActorModel();
		JSONArray result =  actorDAO.getList(id);

		return result;

	}
	
	public JSONArray getListActors() throws Exception{

		ActorModel actorDAO = new ActorModel();
		JSONArray result =  actorDAO.getList();

		return result;
	}
}
