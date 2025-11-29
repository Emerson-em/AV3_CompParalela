// src/AnalisadorDesempenho.java - VERSÃO CORRIGIDA
import java.util.*;
import java.io.*;

public class AnalisadorDesempenho {
    private BuscadorPalavras buscador;

    public AnalisadorDesempenho() {
        this.buscador = new BuscadorPalavras();
    }

    public List<ResultadoBusca> executarTestesCompletos() {
        List<ResultadoBusca> todosResultados = new ArrayList<>();

        String[] arquivos = {
                "Amostras/DonQuixote-388208.txt",
                "Amostras/Dracula-165307.txt",
                "Amostras/MobyDick-217452.txt"
        };

        String[] palavras = {"the", "and", "of", "to", "in"};

        System.out.println("🚀 INICIANDO TESTES DE DESEMPENHO");
        System.out.println("=================================");

        for (int amostra = 1; amostra <= 3; amostra++) {
            System.out.println("\n📊 AMOSTRA " + amostra + ":");
            System.out.println("-------------------");

            for (String arquivo : arquivos) {
                if (!new File(arquivo).exists()) {
                    System.out.println("❌ Arquivo não encontrado: " + arquivo);
                    continue;
                }

                for (String palavra : palavras) {
                    System.out.println("🔍 Testando: " + new File(arquivo).getName() + " - '" + palavra + "'");

                    // Executar todos os métodos
                    ResultadoBusca serial = buscador.buscarSerialCPU(arquivo, palavra);
                    ResultadoBusca parallelCPU = buscador.buscarParallelCPU(arquivo, palavra);
                    ResultadoBusca parallelGPU = buscador.buscarParallelGPU(arquivo, palavra);

                    todosResultados.add(serial);
                    todosResultados.add(parallelCPU);
                    todosResultados.add(parallelGPU);

                    // VERIFICAÇÃO DE CONSISTÊNCIA EM TEMPO REAL
                    boolean consistente = verificarConsistenciaLocal(serial, parallelCPU, parallelGPU);

                    System.out.println("   " + serial);
                    System.out.println("   " + parallelCPU);
                    System.out.println("   " + parallelGPU);

                    if (consistente) {
                        System.out.println("   ✅ CONSISTENTE - Todos os métodos: " + serial.getOcorrencias() + " ocorrências");
                    } else {
                        System.out.println("   ❌ INCONSISTENTE - Verifique implementação!");
                    }
                    System.out.println();
                }
            }
        }

        // Análise final de consistência
        verificarConsistenciaGlobal(todosResultados);

        buscador.limparRecursos();
        return todosResultados;
    }

    private boolean verificarConsistenciaLocal(ResultadoBusca serial, ResultadoBusca parallelCPU, ResultadoBusca parallelGPU) {
        return serial.getOcorrencias() == parallelCPU.getOcorrencias() &&
                serial.getOcorrencias() == parallelGPU.getOcorrencias();
    }

    private void verificarConsistenciaGlobal(List<ResultadoBusca> resultados) {
        System.out.println("\n🔍 VERIFICAÇÃO GLOBAL DE CONSISTÊNCIA");
        System.out.println("====================================");

        Map<String, List<ResultadoBusca>> grupos = new HashMap<>();

        // Agrupar por arquivo-palavra
        for (ResultadoBusca resultado : resultados) {
            String chave = resultado.getArquivo() + "|" + resultado.getPalavra();
            grupos.putIfAbsent(chave, new ArrayList<>());
            grupos.get(chave).add(resultado);
        }

        int totalGrupos = 0;
        int gruposConsistentes = 0;

        for (String chave : grupos.keySet()) {
            List<ResultadoBusca> grupo = grupos.get(chave);
            totalGrupos++;

            Set<Integer> contagens = new HashSet<>();
            for (ResultadoBusca r : grupo) {
                contagens.add(r.getOcorrencias());
            }

            if (contagens.size() == 1) {
                gruposConsistentes++;
                System.out.println("✅ " + chave + " - CONSISTENTE (" + contagens.iterator().next() + " ocorrências)");
            } else {
                System.out.println("❌ " + chave + " - INCONSISTENTE:");
                Map<String, Integer> contagensPorMetodo = new HashMap<>();
                for (ResultadoBusca r : grupo) {
                    contagensPorMetodo.put(r.getMetodo(), r.getOcorrencias());
                    System.out.println("   • " + r.getMetodo() + ": " + r.getOcorrencias() + " ocorrências");
                }
            }
        }

        System.out.println("\n📊 RESUMO FINAL DE CONSISTÊNCIA:");
        System.out.println("   • Total de grupos: " + totalGrupos);
        System.out.println("   • Grupos consistentes: " + gruposConsistentes);
        System.out.println("   • Taxa de consistência: " +
                String.format("%.1f%%", (gruposConsistentes * 100.0 / totalGrupos)));

        if (gruposConsistentes == totalGrupos) {
            System.out.println("🎉 TODOS OS RESULTADOS SÃO CONSISTENTES!");
        } else {
            System.out.println("⚠️  Algumas inconsistências detectadas - verifique implementações paralelas");
        }
    }

    public void analisarResultados(List<ResultadoBusca> resultados) {
        System.out.println("\n📈 ANÁLISE ESTATÍSTICA DE DESEMPENHO");
        System.out.println("===================================");

        Map<String, List<Long>> temposPorMetodo = new HashMap<>();

        for (ResultadoBusca resultado : resultados) {
            String metodo = resultado.getMetodo();
            temposPorMetodo.putIfAbsent(metodo, new ArrayList<>());
            temposPorMetodo.get(metodo).add(resultado.getTempoExecucao());
        }

        for (String metodo : temposPorMetodo.keySet()) {
            List<Long> tempos = temposPorMetodo.get(metodo);

            double tempoMedio = tempos.stream().mapToLong(Long::longValue).average().orElse(0);
            long tempoMin = tempos.stream().mapToLong(Long::longValue).min().orElse(0);
            long tempoMax = tempos.stream().mapToLong(Long::longValue).max().orElse(0);
            double desvioPadrao = calcularDesvioPadrao(tempos);

            System.out.println("📊 " + metodo + ":");
            System.out.println("   • Tempo médio: " + String.format("%.2f", tempoMedio) + " ms");
            System.out.println("   • Tempo mínimo: " + tempoMin + " ms");
            System.out.println("   • Tempo máximo: " + tempoMax + " ms");
            System.out.println("   • Desvio padrão: " + String.format("%.2f", desvioPadrao) + " ms");
            System.out.println("   • Número de testes: " + tempos.size());
            System.out.println();
        }

        // Análise comparativa
        System.out.println("⚡ ANÁLISE COMPARATIVA:");
        if (temposPorMetodo.containsKey("SerialCPU") && temposPorMetodo.containsKey("ParallelCPU")) {
            double tempoSerial = temposPorMetodo.get("SerialCPU").stream().mapToLong(Long::longValue).average().orElse(0);
            double tempoParallelCPU = temposPorMetodo.get("ParallelCPU").stream().mapToLong(Long::longValue).average().orElse(0);
            double speedupCPU = tempoSerial / tempoParallelCPU;

            System.out.println("   • Speedup CPU Paralela: " + String.format("%.2fx", speedupCPU));
        }

        if (temposPorMetodo.containsKey("SerialCPU") && temposPorMetodo.containsKey("ParallelGPU")) {
            double tempoSerial = temposPorMetodo.get("SerialCPU").stream().mapToLong(Long::longValue).average().orElse(0);
            double tempoParallelGPU = temposPorMetodo.get("ParallelGPU").stream().mapToLong(Long::longValue).average().orElse(0);
            double speedupGPU = tempoSerial / tempoParallelGPU;

            System.out.println("   • Speedup GPU: " + String.format("%.2fx", speedupGPU));
        }
    }

    private double calcularDesvioPadrao(List<Long> valores) {
        if (valores.size() <= 1) return 0;

        double media = valores.stream().mapToLong(Long::longValue).average().orElse(0);
        double somaQuadrados = 0;

        for (long valor : valores) {
            somaQuadrados += Math.pow(valor - media, 2);
        }

        return Math.sqrt(somaQuadrados / (valores.size() - 1));
    }
}