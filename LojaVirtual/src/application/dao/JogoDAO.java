package application.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;

import application.db.connection.factory.SQLServerConnectionFactory;
import application.model.Categoria;
import application.model.Idioma;
import application.model.Jogo;
import application.model.Plataforma;
import application.model.Requisito;

public class JogoDAO implements IJogoDAO {

	private Connection con;
	private SQLServerConnectionFactory factory;
	private IRequisitoDAO requisitoDAO = new RequisitoDAO();
	private JogoIdiomaDAO jogoIdiomaDAO = new JogoIdiomaDAO();
	private JogoCategoriaDAO jogoCategoriaDAO = new JogoCategoriaDAO();
	private JogoPlataformaDAO jogoPlataformaDAO = new JogoPlataformaDAO();
	
	public JogoDAO() {
		try {
			factory = new SQLServerConnectionFactory();
			con = factory.getConnection();
		} catch (SQLException | IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void insert(Jogo jogo) throws SQLException {		
		String sql = "INSERT INTO jogos VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setString(1, jogo.getNome());
		ps.setFloat(2, jogo.getPreco());
		ps.setInt(3, jogo.getQtdJogo());
		ps.setDate(4, java.sql.Date.valueOf(jogo.getDataLancamento()));
		ps.setString(5, jogo.getDesenvolvedora());
		ps.setString(6, jogo.getDistribuidora());
		ps.setString(7, jogo.getNomeImg());
		ps.setString(8, jogo.getDescricao());
		ps.execute();
		ps.close();
		
		sql = "SELECT IDENT_CURRENT('jogos') AS codigo";
		ps = con.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
			jogo.setID(rs.getInt("codigo"));
			requisitoDAO.insert(jogo);

			List<Idioma> idiomas = new ArrayList<Idioma>();
			idiomas.addAll(jogo.getIdiomas());
			for (Idioma i : idiomas) {
				jogoIdiomaDAO.insert(jogo, i);
			}
			
			List<Plataforma> plataformas = new ArrayList<Plataforma>();
			plataformas.addAll(jogo.getPlataforma());
			for (Plataforma p : plataformas) {
				jogoPlataformaDAO.insert(jogo, p);

			}
			
			List<Categoria> categorias = new ArrayList<Categoria>();
			categorias.addAll(jogo.getCategoria());
			for (Categoria c : categorias) {
				jogoCategoriaDAO.insert(jogo, c);;
			}
		}
		
		ps.close();
	}

	@Override
	public void update(Jogo jogo) throws SQLException {
		String sql = "UPDATE jogos SET nome=?, preco=?, qtdJogo=?, dataLancamento=?, desenvolvedora=?, distribuidora=?, imagem=?, descricao=? " + 
				"WHERE codigo = ?";
		PreparedStatement ps = con.prepareStatement(sql);	
		
		ps.setString(1, jogo.getNome());
		ps.setFloat(2, jogo.getPreco());
		ps.setInt(3, jogo.getQtdJogo());
		ps.setDate(4, java.sql.Date.valueOf(jogo.getDataLancamento()));
		ps.setString(5, jogo.getDesenvolvedora());
		ps.setString(6, jogo.getDistribuidora());
		ps.setString(7, jogo.getNomeImg());
		ps.setString(8, jogo.getDescricao());
		ps.setInt(9, jogo.getID());
		jogoIdiomaDAO.update(jogo);
		jogoCategoriaDAO.update(jogo);
		jogoPlataformaDAO.update(jogo);
		requisitoDAO.update(jogo);
		
		ps.execute();
		ps.close();
	}

	@Override
	public void delete(Jogo jogo) throws SQLException {
		requisitoDAO.delete(jogo);
		jogoIdiomaDAO.delete(jogo);
		jogoCategoriaDAO.delete(jogo);
		jogoPlataformaDAO.delete(jogo);
		
		PedidoJogoDAO pedidoJogoDAO = new PedidoJogoDAO();
		pedidoJogoDAO.delete(jogo);
		
		String sql = "DELETE FROM jogos WHERE codigo = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, jogo.getID());
		
		ps.execute();
		ps.close();
	}

	@Override
	public Jogo select(Jogo jogo) throws SQLException {
		String sql = "SELECT codigo, nome, preco, qtdJogo, CONVERT(varchar, dataLancamento ,103) AS dataLancamento, desenvolvedora, distribuidora, " + 
				"imagem, descricao " + 
				"FROM jogos " + 
				"WHERE codigo = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, jogo.getID());
		
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
			jogo.setID(rs.getInt("codigo"));
			jogo.setNome(rs.getString("nome"));
			jogo.setPreco(rs.getFloat("preco"));
			jogo.setQtdJogo(rs.getInt("qtdJogo"));
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			LocalDate dt = LocalDate.parse(rs.getString("dataLancamento"), dtf);
			jogo.setDataLancamento(dt);
			
			jogo.setDesenvolvedora(rs.getString("desenvolvedora"));
			jogo.setDistribuidora(rs.getString("distribuidora"));
			jogo.setNomeImg(rs.getString("imagem"));
			jogo.setDescricao(rs.getString("descricao"));
			
			jogo.setIdiomas(jogoIdiomaDAO.selectAll(jogo));
			jogo.setCategoria(jogoCategoriaDAO.selectAll(jogo));
			jogo.setPlataforma(jogoPlataformaDAO.selectAll(jogo));
			
			RequisitoDAO requisitoDAO = new RequisitoDAO();
			jogo.setRequisito(requisitoDAO.select(jogo));
			
			rs.close();
			ps.close();
			return jogo;
			
		}else {
			rs.close();
			ps.close();
			return new Jogo();
		}
	}

	@Override
	public List<Jogo> selectAll() throws SQLException {
		String sql = "SELECT * FROM jogos j " 
				+ "INNER JOIN requisitos r " 
				+ "ON j.requisitoCodigo = r.codigo ";
		PreparedStatement ps = con.prepareStatement(sql);
		
		ResultSet rs = ps.executeQuery();
		
		List<Jogo> listaJogos = new ArrayList<Jogo>();
		
		while (rs.next()) {
			Jogo jogo = new Jogo();
			
			jogo.setID(rs.getInt("codigo"));
			jogo.setNome(rs.getString("nome"));
			jogo.setPreco(rs.getFloat("preco"));
			jogo.setQtdJogo(rs.getInt("qtdJogo"));
			jogo.setDataLancamento(rs.getDate("dataLancamento").toLocalDate());
			jogo.setDesenvolvedora(rs.getString("desenvolvedora"));
			jogo.setDistribuidora(rs.getString("distribuidora"));
			jogo.setNomeImg(rs.getString("imagem"));
			jogo.setDescricao(rs.getString("descricao"));
			
			Requisito requisito = new Requisito();
			requisito.setID(rs.getInt("requisitoCodigo"));
			requisito.setSO(rs.getString("so"));
			requisito.setArmazenamento(rs.getString("armazenamento"));
			requisito.setProcessador(rs.getString("processador"));
			requisito.setMemoria(rs.getString("memoria"));
			requisito.setPlacaVideo(rs.getString("placaVideo"));
			requisito.setDirectX(rs.getString("directX"));
			
			jogo.setRequisito(requisito);
			
			listaJogos.add(jogo);
		}
		
		rs.close();
		ps.close();
		return listaJogos;
	}
	
	public List<Jogo> selectSimple(ModeloPesquisa modeloPesquisa) throws SQLException {
		String sql = "SELECT DISTINCT j.codigo, j.nome, j.preco, j.qtdJogo, j.imagem " + 
				"FROM jogos j " + 
				"INNER JOIN jogoIdioma ji " + 
				"ON ji.jogoCodigo = j.codigo " + 
				"INNER JOIN jogoCategoria jc " + 
				"ON jc.jogoCodigo = j.codigo " + 
				"INNER JOIN jogoPlataforma jp " + 
				"ON jp.jogoCodigo = j.codigo " + 
				"WHERE j.nome like ? ";
		if(modeloPesquisa.getIdioma().getID()!=0) sql+= "AND ji.idiomaCodigo = ? ";
		if(modeloPesquisa.getCategoria().getID()!=0) sql+= "AND jc.categoriaCodigo = ? ";
		if(modeloPesquisa.getPlataforma().getID()!=0) sql+= "AND jp.plataformaCodigo = ? ";
		
		PreparedStatement ps = con.prepareStatement(sql);
		int i = 1;
		ps.setString(i, "%" + modeloPesquisa.getNome() + "%");
		i++;
		if(modeloPesquisa.getIdioma().getID()!=0) {
			ps.setString(i, modeloPesquisa.getIdioma().getID()+"");
			i++;
		}
		if(modeloPesquisa.getCategoria().getID()!=0) {
			ps.setString(i, modeloPesquisa.getCategoria().getID()+"");
			i++;
		}
		if(modeloPesquisa.getPlataforma().getID()!=0) {
			ps.setString(i, modeloPesquisa.getPlataforma().getID()+"");
		}
		
		ResultSet rs = ps.executeQuery();
		
		List<Jogo> listaJogos = new ArrayList<Jogo>();
		
		while (rs.next()) {
			Jogo jogo = new Jogo();
			
			jogo.setID(rs.getInt("codigo"));
			jogo.setNome(rs.getString("nome"));
			jogo.setPreco(rs.getFloat("preco"));
			jogo.setQtdJogo(rs.getInt("qtdJogo"));
			jogo.setNomeImg(rs.getString("imagem"));
						
			listaJogos.add(jogo);
		}
		
		rs.close();
		ps.close();
		return listaJogos;
	}
	
	public List<Jogo> pesquisarPorNome(String nome) throws SQLException {
		String sql = "SELECT codigo, nome, preco, qtdJogo, CONVERT(varchar, dataLancamento ,103) AS dataLancamento, desenvolvedora, distribuidora," + 
				"imagem, descricao " + 
				"FROM jogos " + 
				"WHERE nome like ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setString(1, "%" + nome + "%");
		
		ResultSet rs = ps.executeQuery();
		
		List<Jogo> listaJogos = new ArrayList<Jogo>();
		
		while (rs.next()) {
			Jogo jogo = new Jogo();
			
			jogo.setID(rs.getInt("codigo"));
			jogo.setNome(rs.getString("nome"));
			jogo.setPreco(rs.getFloat("preco"));
			jogo.setQtdJogo(rs.getInt("qtdJogo"));
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			LocalDate dt = LocalDate.parse(rs.getString("dataLancamento"), dtf);
			jogo.setDataLancamento(dt);
			
			jogo.setDesenvolvedora(rs.getString("desenvolvedora"));
			jogo.setDistribuidora(rs.getString("distribuidora"));
			jogo.setNomeImg(rs.getString("imagem"));
			jogo.setDescricao(rs.getString("descricao"));
			
			jogo.setIdiomas(jogoIdiomaDAO.selectAll(jogo));
			jogo.setCategoria(jogoCategoriaDAO.selectAll(jogo));
			jogo.setPlataforma(jogoPlataformaDAO.selectAll(jogo));
			
			RequisitoDAO requisitoDAO = new RequisitoDAO();
			jogo.setRequisito(requisitoDAO.select(jogo));
			
			listaJogos.add(jogo);
		}
		
		rs.close();
		ps.close();
		return listaJogos;
	}

}
