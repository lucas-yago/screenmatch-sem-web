package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
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
    private final String API_KEY = "&apikey=15859606";


    public void exibeMenu() {
        System.out.println("Digite o nome da série para a busca");
        var nomeSerie = scanner.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
//        System.out.println(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dadosSerie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            var jsonTemporada = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> {
            System.out.printf("--------------------- Temporada %s ---------------------%n", t.numero());
            t.episodios().forEach(e -> System.out.println(e.numero() + " - " + e.titulo()));
            System.out.println(" ");
        });

//        List<String> nomes = Arrays.asList("lucas","adriel","viego","wagner","mario");
//
//        nomes.stream()
//                .sorted()
//                .limit(3)
//                .filter(n -> n.startsWith("l"))
//                .map(n-> n.toUpperCase())
//                .forEach(System.out::println);

        System.out.println("---------- Top 10 episodios melhor avaliados ----------");
        List<DadosEpisodio> dadosEpisodio = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

//        dadosEpisodio.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
////                .peek(e -> System.out.println("filtro: " + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
////                .peek(e -> System.out.println("sort: " + e))
//                .limit(10)
////                .peek(e -> System.out.println("limit: " + e))
//                .map( e-> e.titulo().toUpperCase())
////                .peek(e -> System.out.println("map: " + e))
//                .forEach(System.out::println);


        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(e -> new Episodio(t.numero(), e)))
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);

//        System.out.println("Digite um trecho do título do episódio");
//
//        var trechoTitulo = scanner.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//                .findFirst();
//        if (episodioBuscado.isPresent()) {
//            System.out.println("Episódio encontrado!");
//            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
//        } else {
//            System.out.println("Episódio não encontrado!");
//        }

//
//        System.out.println("A partir de que ano você deseja ver os episodios");
//        var ano = scanner.nextInt();
//        scanner.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyy");
//
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                                " Temporada: " + e.getTemporada() +
//                                " Episodio: " + e.getTitulo() +
//                                " Data de lançamento: " + e.getDataLancamento().format(formatador)
//                        )
//                );

        System.out.println("avaliaçao media por temporada");
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getNumero,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacoesPorTemporada);


        DoubleSummaryStatistics est =episodios.stream()
                .filter(e -> e.getAvaliacao() >0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println(est);

        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade: " + est.getCount());


    }


}
