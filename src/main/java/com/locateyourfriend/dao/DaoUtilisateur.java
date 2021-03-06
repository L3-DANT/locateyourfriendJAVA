package com.locateyourfriend.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bson.Document;

import com.locateyourfriend.daoInterface.DaoUtilisateurInterface;
import com.locateyourfriend.logger.MyLogger;
import com.locateyourfriend.model.Constantes;
import com.locateyourfriend.model.Utilisateur;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public class DaoUtilisateur extends DaoAbstract implements DaoUtilisateurInterface {

	/**
	 * L'initialisation du daoUtilisateurs permet la cr�ation des tables de la base de donn�es
	 * 
	 * TODO : externaliser ces fonction dans un sous-programme 
	 */
	public DaoUtilisateur(){
		super();
		createTables();
	}

	
	/**
	 * Permet de r�cup�rer un utilisateur en fonction de son adresse email qui sert d'identifiant
	 */
	@Override
	public Utilisateur getUtilisateur(String email) throws MongoException{
		logger.log(Level.INFO, "r�cup�ration d'un utilisateur : " + email);
		MongoCollection<Document> collection = mongoDatabase.getCollection(Constantes.TABLE_USER);
		FindIterable<Document> userDb = collection.find(new BasicDBObject(Constantes.COLONNE_EMAIL, email));
		if(userDb.first() == null){
			return null;
		}
		Document doc = userDb.first();
		Utilisateur user = new Utilisateur(doc.getString(Constantes.COLONNE_NOM), doc.getString(Constantes.COLONNE_PRENOM), doc.getString(Constantes.COLONNE_EMAIL), doc.getString(Constantes.COLONNE_MDP));
		return user;
	}

	/**
	 * Ajoute un utilisateur � la base de donn�es, il est retourn� � la fin du traitement.
	 */
	@Override
	public Utilisateur addUser(Utilisateur util) throws MongoException{
		logger.log(Level.INFO, "insertion d'un utilisateur : " + util.getEmail());
		MongoCollection<Document> collection = mongoDatabase.getCollection(Constantes.TABLE_USER);
		Document userDb = new Document();
		userDb.append(Constantes.COLONNE_EMAIL, util.getEmail());
		userDb.append(Constantes.COLONNE_NOM, util.getNom());
		userDb.append(Constantes.COLONNE_PRENOM, util.getPrenom());
		userDb.append(Constantes.COLONNE_MDP, util.getMotDePasse());
		collection.insertOne(userDb);
		return util;
	}
	
	/**
	 * Cr�e les tables
	 * @throws MongoException
	 */
	public void createTables() throws MongoException{
		MongoCollection<Document> collection = mongoDatabase.getCollection(Constantes.TABLE_USER);
		collection.createIndex(new BasicDBObject(Constantes.COLONNE_EMAIL, 1));
		collection.createIndex(new BasicDBObject(Constantes.COLONNE_NOM, 2));
		collection.createIndex(new BasicDBObject(Constantes.COLONNE_PRENOM ,3));
		collection.createIndex(new BasicDBObject(Constantes.COLONNE_MDP, 4));
		collection.createIndex(new BasicDBObject(Constantes.COLONNE_LOCALISATION, 5));
		collection = mongoDatabase.getCollection(Constantes.TABLE_JOINTURE_AMIS);
		collection.createIndex(new BasicDBObject(Constantes.COLONNE_AMI_ID, 1));
		collection.createIndex(new BasicDBObject(Constantes.COLONNE_AMI_CIBLE, 2));
	}

	/**
	 * Vide les tables
	 */
	public void emptyTable() throws MongoException{
		mongoDatabase.getCollection(Constantes.TABLE_USER).drop();
		mongoDatabase.getCollection(Constantes.TABLE_JOINTURE_AMIS).drop();
	}
	
	/**
	 * Permet de r�cup�rer tous les utilisateurs de la base de donn�es
	 */
	@Override
	public ArrayList<Utilisateur> getUtilisateurs()throws MongoException {
		logger.log(Level.INFO, "r�cup�ration des utilisateurs");
		MongoCollection<Document> collection = mongoDatabase.getCollection(Constantes.TABLE_USER);
		MongoCursor<Document> userDb = collection.find().iterator();
		ArrayList<Utilisateur> listeUser = new ArrayList<Utilisateur>();
		/**
		 * Chaque next() repr�sente un utilisateur stock� en base de donn�es
		 */
		while(userDb.hasNext()){
			Document doc = userDb.next();
			listeUser.add(new Utilisateur(doc.getString(Constantes.COLONNE_NOM), doc.getString(Constantes.COLONNE_PRENOM), doc.getString(Constantes.COLONNE_EMAIL), doc.getString(Constantes.COLONNE_MDP)));
		}
		return listeUser;
	}
	
	/**
	 * Main pour tests
	 */
	public static void main(String[] args){
		DaoUtilisateurInterface daoUtilisateur = new DaoUtilisateur();
		Utilisateur user = new Utilisateur("robin", "tritan", "tristan.robin1@gmail.com", "test");
		daoUtilisateur.addUser(user);
		Utilisateur userEnRetour = daoUtilisateur.getUtilisateur(user.getEmail());
		if(user.equals(userEnRetour)){
			MyLogger.getInstance().log(Level.INFO, "test r�ussit");
		}
		else{
			MyLogger.getInstance().log(Level.INFO, "test rat�");
		}
	}

	/**
	 * Permet de supprimer un utilisateur
	 * @param email
	 */
	public void remove(String email) {
		logger.log(Level.INFO, "suppression de l'utilisateur : " + email);
		MongoCollection<Document> collection = mongoDatabase.getCollection(Constantes.TABLE_USER);
		BasicDBObject query = new BasicDBObject();
		query.append(Constantes.COLONNE_EMAIL, email);
		collection.deleteOne(query);
	}
}
