package application.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

	protected String CPF;
	protected String nome;
	protected String email;
	protected String senha;
	protected String nomeUsuario;
	protected List<Telefone> telefones = new ArrayList<Telefone>();

	public Usuario() {
	}
	
	public Usuario(String CPF, String nome, String email, String senha, String nomeUsuario, Integer tipoUsuario,
			List<Telefone> telefones) {
		super();
		this.CPF = CPF;
		this.nome = nome;
		this.email = email;
		this.senha = senha;
		this.nomeUsuario = nomeUsuario;
		this.telefones = telefones;
	}

	public String getCPF() {
		return CPF;
	}

	public void setCPF(String CPF) {
		this.CPF = CPF;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public List<Telefone> getTelefones() {
		return telefones;
	}

	public void setTelefones(List<Telefone> telefones) {
		this.telefones = telefones;
	}
	
	public void addTelefone(Telefone telefone) {
		this.telefones.add(telefone);
	}

}
