// src/Main.java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("🎯 ANÁLISE COMPARATIVA: ALGORITMOS SERIAL vs PARALELO");
        System.out.println("=====================================================");

        AnalisadorDesempenho analisador = new AnalisadorDesempenho();

        List<ResultadoBusca> resultados = analisador.executarTestesCompletos();

        GeradorCSV.gerarArquivoCSV(resultados, "dados_completos.csv");
        GeradorCSV.gerarResumoCSV(resultados);

        analisador.analisarResultados(resultados);

        System.out.println("🎉 PROCESSAMENTO CONCLUÍDO!");
        System.out.println("📁 Resultados salvos em: resultados/");
    }
}