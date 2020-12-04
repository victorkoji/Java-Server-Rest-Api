package controllers;

import java.io.IOException;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Actor;
import model.ActorModel;


public class ActorController {

	public ActorController() {
		// TODO Auto-generated constructor stub
	}
	
//	public void efetuarCadastro(HttpServletRequest request, HttpServletResponse response) throws IOException{
//		String nome = request.getParameter("nome");
//		String birthdate = request.getParameter("birthdate");
//		Actor usuario = new Actor();
//		ActorModel usuarioDAO = new ActorModel();
//		
//
//		usuario.setName(nome);
//		usuario.setBirthdate(birthdate);
//		usuarioDAO.cadastrar(usuario);
//	
//		response.getWriter().print("Salvo");
//	}
	
	public JSONArray getListActors(String param) throws Exception{

		int id = Integer.parseInt(param);
		
		ActorModel usuarioDAO = new ActorModel();
		JSONArray result =  usuarioDAO.getList(id);

		return result;

	}
	
	public JSONArray getListActors() throws Exception{

		ActorModel usuarioDAO = new ActorModel();
		JSONArray result =  usuarioDAO.getList();

		return result;

	}
}
