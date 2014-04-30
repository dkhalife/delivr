<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

/**
 * Cette classe traite toutes les requetes concernant le truck
 **/
class Truck extends CI_Controller {
	/**
	 * Constructeur par defaut
	 **/
	public function __construct(){
		parent::__construct();
		
		header('Content-Type: text/json');
	}
	
	/**
	 * Methode qui verifie si on est admin
	 **/
	private function isAdmin(){
		return md5($this->input->post('key')) == 'a41acc7effe601de1dc2099a4e2fdd7c';
	}
	
	/**
	 * Methode d'acces aux proprietes du truck
	 **/
	public function index(){
		$this->db->select("*")
				 ->from("truck")
				 ->limit(1);
		$q = $this->db->get();
		
		if($q->num_rows() == 1){
			$data = $q->row();
			$data->success = true;
			echo json_encode($data);
		}
		else 
			echo json_encode([
				'success'	=>	false,
				'message'	=>	'Aucun truck est actuellement enregistre!'
			]);
	}
	
	/**
	 * Cette methode permet de deplacer le truck
	 **/
	public function move(){
		if(!$this->isAdmin()){
			echo json_encode([
				'success'	=>	false,
				'message'	=>	'Vous ne disposez pas des privileges necessaires pour executer cette operation'
			]);
			return;
		}
		
		$this->db->update("truck", [
			'longitude'	=>	$this->input->post('longitude'),
			'latitude'	=>	$this->input->post('latitude')
		]);
	}
}