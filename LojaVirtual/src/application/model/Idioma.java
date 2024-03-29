package application.model;

public class Idioma {

	private Integer ID;
	private String nome;

	public Idioma() {
	}
	
	public Idioma(Integer ID, String nome) {
		super();
		this.ID = ID;
		this.setNome(nome);
	}

	public Integer getID() {
		return ID;
	}

	public void setID(Integer ID) {
		this.ID = ID;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Override
	public String toString() {
		return this.nome;
	}

}
