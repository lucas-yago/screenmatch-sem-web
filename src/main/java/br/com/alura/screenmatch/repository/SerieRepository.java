package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

     Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

     List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeAtor, Double avaliacao);

     List<Serie> findTop5ByOrderByAvaliacaoDesc();

     List<Serie> findByGenero(Categoria categoria);

//     List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(Integer NumeoroTemporadas, Double avaliacao);

     @Query("select s from Serie s where s.totalTemporadas <= :temporadas and s.avaliacao >= :avaliacao")
     List<Serie> buscaPorTemporadasEAvaliacao(int temporadas, double avaliacao);

     @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:trechoEpisodio% ")
     List<Episodio> buscaPorTrechoEpisodio(String trechoEpisodio);

     @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s.titulo = :serie ORDER BY e.avaliacao DESC LIMIT 5 ")
     List<Episodio> buscaPorTop5EpisodiosDaSerie(String serie);

     @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s.titulo = :titulo AND YEAR(e.dataLancamento) >= :ano")
     List<Episodio> buscaEpisodioApartirDaData(String titulo, int ano);
}
