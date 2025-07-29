package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ApiKeysProvider;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private final Scanner scanner = new Scanner(System.in);
    private final ConsumoApi consumoApi = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=" + ApiKeysProvider.getApiKey("api.omdb.key");
    private final List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private  Optional<Serie> serieBusca;


    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var menu = """
                
                1 - Buscar séries
                2 - Buscar episódios
                3 - Listar séries buscadas
                4 - Busca séries por titulo
                5 - Busca séries por Ator
                6 - Top 5 séries
                7 - Busca por gênero
                8 - Busca séries por temporadas
                9 - Busca Episódio por trecho
                10 - Top 5 epiódios por série
                11 - Busca episódios apartir de uma data
                
                0 - Sair
                """;
        var opcao = -1;

        while (opcao != 0) {
            System.out.println(menu);
            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    buscaSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorTemporada();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTop5EpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void listarSeriesBuscadas() {
//        List<Serie> series = dadosSeries.stream()
//                .map(d -> new Serie(d))
//                .collect(Collectors.toList());
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscaSerieWeb() {
        DadosSerie dados = getDadosSerie();
//        dadosSeries.add(dados);
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para a busca");
        var nomeSerie = scanner.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return conversor.obterDados(json, DadosSerie.class);
    }

    private void buscarEpisodioPorSerie() {
//        DadosSerie dadosSerie = getDadosSerie();
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = scanner.nextLine();

//        Optional<Serie> serie = series.stream()
//                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
//                .findFirst();
        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var jsonTemporada = consumoApi.obterDados(ENDERECO + serieEncontrada.getTitulo()
                        .replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = scanner.nextLine();

         serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da serie: " + serieBusca.get());
        } else {
            System.out.println("Serie não encontrada");
        }
    }


    private void buscarSeriePorAtor() {
        System.out.println("Qual o nome para busca?");
        String nomeAtor = scanner.nextLine();
        System.out.println("Avaliacões apartir de qual valor?");
        Double avaliacao = scanner.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s -> System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Qual a categoria para busca?");
        var nomeGenero = scanner.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesBuscadas = repositorio.findByGenero(categoria);
        System.out.println("Series do genero " + nomeGenero);
        seriesBuscadas.forEach(System.out::println);
    }

    private void buscarSeriesPorTemporada() {
        System.out.println("Qual o numero de temporadas maximo para busca?");
        int totalTemporadas = scanner.nextInt();
        System.out.println("Avaliacões apartir de qual valor?");
        double avaliacao = scanner.nextDouble();
//        List<Serie> seriesBuscadas = repositorio.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas, avaliacao);
        List<Serie> seriesBuscadas = repositorio.buscaPorTemporadasEAvaliacao(totalTemporadas, avaliacao);
        seriesBuscadas.forEach(s -> System.out.println(
               "titulo: " + s.getTitulo() + " temporadas: " + s.getTotalTemporadas() + " avaliação: " + s.getAvaliacao()
        ));
    }

    private void buscarEpisodioPorTrecho(){
        System.out.println("Digite o trecho do episódio para busca:");
        String trechoEpisodio = scanner.nextLine();
        List<Episodio> episodio = repositorio.buscaPorTrechoEpisodio(trechoEpisodio);
        episodio.forEach(e->
                System.out.printf("Série: %s  Temporada: %s  Episódio: %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo()));
    }

    private void buscarTop5EpisodiosPorSerie(){
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()){
            List<Episodio> top5Episodios = repositorio.buscaPorTop5EpisodiosDaSerie(serieBusca.get().getTitulo());
            top5Episodios.forEach(e->
                    System.out.printf("Série: %s  Temporada: %s  Episódio: %s - %s  Avaliacao: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosDepoisDeUmaData(){
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()){
            System.out.println("Digite o ano limite de lançamento: ");
            var anoLancamento = scanner.nextInt();
            scanner.nextLine();

            List<Episodio> episodiosApartirDe = repositorio.buscaEpisodioApartirDaData(serieBusca.get().getTitulo(), anoLancamento);
            episodiosApartirDe.forEach(e->
                    System.out.printf("Série: %s  Temporada: %s  Episódio: %s - %s  Lancamento: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo(), e.getDataLancamento()));
        }
    }

}
