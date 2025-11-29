// src/ResultadoBusca.java
public class ResultadoBusca {
    private String metodo;
    private String arquivo;
    private String palavra;
    private int ocorrencias;
    private long tempoExecucao;
    private int tamanhoTexto;

    public ResultadoBusca(String metodo, String arquivo, String palavra,
                          int ocorrencias, long tempoExecucao, int tamanhoTexto) {
        this.metodo = metodo;
        this.arquivo = arquivo;
        this.palavra = palavra;
        this.ocorrencias = ocorrencias;
        this.tempoExecucao = tempoExecucao;
        this.tamanhoTexto = tamanhoTexto;
    }

    // Getters
    public String getMetodo() { return metodo; }
    public String getArquivo() { return arquivo; }
    public String getPalavra() { return palavra; }
    public int getOcorrencias() { return ocorrencias; }
    public long getTempoExecucao() { return tempoExecucao; }
    public int getTamanhoTexto() { return tamanhoTexto; }

    @Override
    public String toString() {
        return String.format("%s: %d ocorrências em %d ms (Arquivo: %s, Palavra: '%s')",
                metodo, ocorrencias, tempoExecucao, arquivo, palavra);
    }

    public String toCSV() {
        return String.format("%s,%s,%s,%d,%d,%d",
                metodo, arquivo, palavra, ocorrencias, tempoExecucao, tamanhoTexto);
    }

    public static String getCSVHeader() {
        return "Metodo,Arquivo,Palavra,Ocorrencias,TempoMs,TamanhoTexto";
    }
}