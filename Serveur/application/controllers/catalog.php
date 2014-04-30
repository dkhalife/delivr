<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

/**
 * Cette classe traite toutes les requetes concernant le truck
 **/
class Catalog extends CI_Controller {
	/**
	 * Constructeur par defaut
	 **/
	public function __construct(){
		parent::__construct();
		
		header('Content-Type: text/json');
	}
	
	/**
	 * Methode d'acces aux proprietes du truck
	 **/
	public function index(){
		$this->db->select("*")
				 ->from("catalog");
		$q = $this->db->get();
		
		$ret = $q->num_rows() == 0 ? [] : $q->result();

		echo json_encode([
			'success'	=>	true,
			'items'		=>	$ret
		]);
	}
}