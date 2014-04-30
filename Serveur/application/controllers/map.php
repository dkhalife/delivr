<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

/**
 * Cette classe traite toutes les requetes concernant la carte
 **/
class Map extends CI_Controller {
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
	 * Methode qui permet d'avoir la liste de tous les pointeurs sur la carte
	 **/
	public function index(){
		if(!$this->isAdmin()){
			echo json_encode([
				'success'	=>	false,
				'message'	=>	'Vous ne disposez pas des privileges necessaires pour executer cette operation'
			]);
			return;
		}
		
		$this->db->select('*')
				 ->from('map')
				 ->where('visited', false);
		$q = $this->db->get();
		
		if($q->num_rows() > 0){
			echo json_encode([
				'success'	=>	true,
				'markers'	=>	$q->result()
			]);
		}
		else {
			echo json_encode([
				'success'	=>	true,
				'markers'	=>	[]
			]);
		}
	}
	
	/**
	 * Methode qui permet de marquer un point comme visite
	 **/
	public function visit($marker_id){
		if(!$this->isAdmin()){
			echo json_encode([
				'success'	=>	false,
				'message'	=>	'Vous ne disposez pas des privileges necessaires pour executer cette operation'
			]);
			return;
		}
		
		$this->db->where('marker_id', $marker_id)
				 ->update('map', [
					'visited'	=>	true
				 ]);
				 
		echo json_encode([
			'success'	=>	$this->db->affected_rows() == 1
		]);
	}
	
	/**
	 * Methode d'acces a l'etat d'une commande
	 **/
	public function track(){
		$this->db->select('visited')
				 ->from('map')
				 ->where('marker_id', $this->input->post('order_id'))
				 ->limit(1);
		$query = $this->db->get();
		
		if($query->num_rows() == 0){
			echo json_encode([
				'success'	=>	false,
				'message'	=>	'L\'identifiant de la commande est invalide'
			]);
		}
		else {
			echo json_encode([
				'success'	=>	true,
				'delivered'	=>	$query->row()->visited
			]);
		}
	}
	
	/**
	 * Methode d'ajout d'un pointeur sur la carte
	 **/
	public function add(){
		$this->load->library('upload', [
			'upload_path'	=>	'./uploads/',
			'allowed_types'	=>	'gif|jpg|png',
			'max_size'		=>	'10240',
			'encrypt_name'	=>	TRUE
		]);
		
		if($this->upload->do_upload('image')){
			$image_path = '/uploads/'.$this->upload->data()['file_name'];
		
			$this->db->set('name', $this->input->post('name'))
					 ->set('phone', $this->input->post('phone'))
					 ->set('battery', $this->input->post('battery'))
					 ->set('longitude', $this->input->post('longitude'))
					 ->set('latitude', $this->input->post('latitude'))
					 ->set('order', $this->input->post('order'))
					 ->set('image', $image_path);
			if($this->db->insert("map")){
				$marker_id = $this->db->insert_id();
				echo json_encode([
					'success'	=>	true,
					'marker_id'	=>	$marker_id
				]);
			}
			else {
				echo json_encode([
					'success'	=>	false,
					'message'	=>	'Veuillez rentrer toutes les informations requises.'
				]);
			}
		}
		else {
			echo json_encode([
				'success'	=>	false,
				'message'	=>	'Erreur lors du traitement de l\'image.'
			]);
		}
	}
}