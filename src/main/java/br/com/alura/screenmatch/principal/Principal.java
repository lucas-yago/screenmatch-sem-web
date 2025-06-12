package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.service.ApiKeysProvider;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private final Scanner scanner = new Scanner(System.in);
    private final ConsumoApi consumoApi = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=" + ApiKeysProvider.getApiKey("api.omdb.key");
    private final List<DadosSerie> dadosSeries = new ArrayList<>();

    public void exibeMenu() {
        var menu = """
                
                1 - Buscar séries
                2 - Buscar episódios
                3 - Listar séries buscadas
                
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
                    listarEpisodiosBuscados();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void listarEpisodiosBuscados() {
        List<Serie> series = dadosSeries.stream()
                .map(d -> new Serie(d))
                .collect(Collectors.toList());

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscaSerieWeb() {
        DadosSerie dados = getDadosSerie();
        dadosSeries.add(dados);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para a busca");
        var nomeSerie = scanner.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return conversor.obterDados(json, DadosSerie.class);
    }

    private void buscarEpisodioPorSerie() {
        DadosSerie dadosSerie = getDadosSerie();
        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            var jsonTemporada = consumoApi.obterDados(ENDERECO + dadosSerie.titulo()
                    .replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);
    }
}
