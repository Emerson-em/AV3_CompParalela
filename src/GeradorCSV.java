// src/GeradorCSV.java
import java.io.*;
import java.util.List;

public class GeradorCSV {

    public static void gerarArquivoCSV(List<ResultadoBusca> resultados, String nomeArquivo) {
        File pastaResultados = new File("resultados");
        if (!pastaResultados.exists()) {
            pastaResultados.mkdir();
        }

        String caminhoCompleto = "resultados/" + nomeArquivo;

        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoCompleto))) {
            // Escrever cabeçalho
            writer.println(ResultadoBusca.getCSVHeader());

            // Escrever dados
            for (ResultadoBusca resultado : resultados) {
                writer.println(resultado.toCSV());
            }

            System.out.println("✅ Arquivo CSV gerado: " + caminhoCompleto);

        } catch (IOException e) {
            System.err.println("❌ Erro ao gerar CSV: " + e.getMessage());
        }
    }

    public static void gerarResumoCSV(List<ResultadoBusca> resultados) {
        // Agrupar por metodo e arquivo para análise comparativa
        try (PrintWriter writer = new PrintWriter(new FileWriter("resultados/resumo_analise.csv"))) {
            writer.println("Metodo,Arquivo,TempoMedio,TempoMin,TempoMax,NumTestes");

            // Agrupar resultados
            java.util.Map<String, java.util.List<ResultadoBusca>> agrupados = new java.util.HashMap<>();

            for (ResultadoBusca resultado : resultados) {
                String chave = resultado.getMetodo() + "," + resultado.getArquivo();
                agrupados.putIfAbsent(chave, new java.util.ArrayList<>());
                agrupados.get(chave).add(resultado);
            }

            // Calcular estatísticas por grupo
            for (String chave : agrupados.keySet()) {
                java.util.List<ResultadoBusca> grupo = agrupados.get(chave);
                double tempoMedio = grupo.stream().mapToLong(ResultadoBusca::getTempoExecucao).average().orElse(0);
                long tempoMin = grupo.stream().mapToLong(ResultadoBusca::getTempoExecucao).min().orElse(0);
                long tempoMax = grupo.stream().mapToLong(ResultadoBusca::getTempoExecucao).max().orElse(0);

                writer.printf("%s,%.2f,%d,%d,%d%n", chave, tempoMedio, tempoMin, tempoMax, grupo.size());
            }

            System.out.println("✅ Arquivo de resumo gerado: resultados/resumo_analise.csv");

        } catch (IOException e) {
            System.err.println("❌ Erro ao gerar resumo CSV: " + e.getMessage());
        }
    }
}