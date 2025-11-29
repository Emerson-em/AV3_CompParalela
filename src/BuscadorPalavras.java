// src/BuscadorPalavras.java - VERSÃO CORRIGIDA
import org.jocl.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.*;
import java.util.regex.Pattern;

import static org.jocl.CL.*;

public class BuscadorPalavras {
    private cl_context context;
    private cl_command_queue commandQueue;
    private cl_program program;
    private boolean openCLDisponivel = false;

    public BuscadorPalavras() {
        this.openCLDisponivel = inicializarOpenCL();
    }

    // ========== MÉTODO SERIAL CPU ==========
    public ResultadoBusca buscarSerialCPU(String arquivoPath, String palavraAlvo) {
        long startTime = System.currentTimeMillis();
        int ocorrencias = 0;
        int tamanhoTexto = 0;

        try {
            String conteudo = new String(Files.readAllBytes(new File(arquivoPath).toPath()),
                    StandardCharsets.UTF_8);
            tamanhoTexto = conteudo.length();

            // LÓGICA CONSISTENTE: split por não-palavras
            String conteudoLower = conteudo.toLowerCase();
            String palavraLower = palavraAlvo.toLowerCase();

            String[] palavras = conteudoLower.split("\\W+");
            for (String palavra : palavras) {
                if (palavra.equals(palavraLower)) {
                    ocorrencias++;
                }
            }

        } catch (Exception e) {
            System.err.println("Erro no método serial: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        String nomeArquivo = new File(arquivoPath).getName();

        return new ResultadoBusca("SerialCPU", nomeArquivo, palavraAlvo,
                ocorrencias, (endTime - startTime), tamanhoTexto);
    }

    // ========== METODO PARALELO CPU ========== (**CORRIGIDO**)
    public ResultadoBusca buscarParallelCPU(String arquivoPath, String palavraAlvo) {
        long startTime = System.currentTimeMillis();
        int ocorrencias = 0;
        int tamanhoTexto = 0;

        try {
            String conteudo = new String(Files.readAllBytes(new File(arquivoPath).toPath()),
                    StandardCharsets.UTF_8);
            tamanhoTexto = conteudo.length();

            String conteudoLower = conteudo.toLowerCase();
            String palavraLower = palavraAlvo.toLowerCase();

            int numThreads = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<Future<Integer>> futures = new ArrayList<>();

            // DIVISÃO CONSISTENTE: processar palavras, não chunks de texto
            String[] todasPalavras = conteudoLower.split("\\W+");
            int palavrasPorThread = Math.max(1, todasPalavras.length / numThreads);

            for (int i = 0; i < numThreads; i++) {
                final int start = i * palavrasPorThread;
                final int end = (i == numThreads - 1) ? todasPalavras.length : (i + 1) * palavrasPorThread;

                Callable<Integer> task = () -> {
                    int count = 0;
                    for (int j = start; j < end; j++) {
                        if (todasPalavras[j].equals(palavraLower)) {
                            count++;
                        }
                    }
                    return count;
                };
                futures.add(executor.submit(task));
            }

            // Coletar resultados
            for (Future<Integer> future : futures) {
                ocorrencias += future.get();
            }

            executor.shutdown();

        } catch (Exception e) {
            System.err.println("Erro no método paralelo CPU: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        String nomeArquivo = new File(arquivoPath).getName();

        return new ResultadoBusca("ParallelCPU", nomeArquivo, palavraAlvo,
                ocorrencias, (endTime - startTime), tamanhoTexto);
    }

    // ========== METODO PARALELO GPU ========== (**CORRIGIDO**)
    public ResultadoBusca buscarParallelGPU(String arquivoPath, String palavraAlvo) {
        if (!openCLDisponivel) {
            System.out.println("⚠️  OpenCL não disponível. Usando fallback para CPU.");
            return buscarSerialCPU(arquivoPath, palavraAlvo); // Fallback
        }

        long startTime = System.currentTimeMillis();
        int ocorrencias = 0;
        int tamanhoTexto = 0;

        try {
            String conteudo = new String(Files.readAllBytes(new File(arquivoPath).toPath()),
                    StandardCharsets.UTF_8);
            tamanhoTexto = conteudo.length();

            // Preparar dados para OpenCL
            byte[] textoBytes = conteudo.toLowerCase().getBytes(StandardCharsets.UTF_8);
            byte[] palavraBytes = palavraAlvo.toLowerCase().getBytes(StandardCharsets.UTF_8);

            // Chamar kernel OpenCL corrigido
            ocorrencias = executarBuscaOpenCL(textoBytes, palavraBytes);

        } catch (Exception e) {
            System.err.println("Erro no método GPU: " + e.getMessage());
            // Fallback para garantir resultado
            ocorrencias = buscarSerialCPU(arquivoPath, palavraAlvo).getOcorrencias();
        }

        long endTime = System.currentTimeMillis();
        String nomeArquivo = new File(arquivoPath).getName();

        return new ResultadoBusca("ParallelGPU", nomeArquivo, palavraAlvo,
                ocorrencias, (endTime - startTime), tamanhoTexto);
    }

    private boolean inicializarOpenCL() {
        try {
            // Obter plataforma
            int[] numPlatforms = new int[1];
            clGetPlatformIDs(0, null, numPlatforms);

            if (numPlatforms[0] == 0) {
                System.out.println("Nenhuma plataforma OpenCL encontrada.");
                return false;
            }

            cl_platform_id[] platforms = new cl_platform_id[numPlatforms[0]];
            clGetPlatformIDs(platforms.length, platforms, null);

            // Obter dispositivo
            int[] numDevices = new int[1];
            clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_GPU, 0, null, numDevices);

            if (numDevices[0] == 0) {
                clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_CPU, 0, null, numDevices);
            }

            if (numDevices[0] == 0) {
                System.out.println("Nenhum dispositivo OpenCL encontrado.");
                return false;
            }

            cl_device_id[] devices = new cl_device_id[numDevices[0]];
            clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_ALL, devices.length, devices, null);

            // Criar contexto e fila
            context = clCreateContext(null, 1, new cl_device_id[]{devices[0]}, null, null, null);
            commandQueue = clCreateCommandQueue(context, devices[0], 0, null);

            // Compilar programa
            String kernelSource = carregarKernelBuscaConsistente();
            program = clCreateProgramWithSource(context, 1, new String[]{kernelSource}, null, null);
            clBuildProgram(program, 0, null, null, null, null);

            System.out.println("✅ OpenCL inicializado para busca paralela na GPU");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Falha na inicialização OpenCL: " + e.getMessage());
            return false;
        }
    }

    private String carregarKernelBuscaConsistente() {
        return """
            // Função para verificar caractere de palavra (CONSISTENTE com \\W+)
            int isWordChar(uchar c) {
                return (c >= 'a' && c <= 'z') || 
                       (c >= 'A' && c <= 'Z') || 
                       (c >= '0' && c <= '9') || 
                       (c == '_');
            }
            
            __kernel void buscarPalavraConsistente(__global const uchar* texto,
                                                 __global const uchar* palavraAlvo,
                                                 __global int* resultado,
                                                 const int tamanhoTexto,
                                                 const int tamanhoPalavra) {
                int id = get_global_id(0);
                int ocorrenciasLocal = 0;
                
                // Cada thread processa uma parte do texto
                int workSize = get_global_size(0);
                int chunkSize = (tamanhoTexto + workSize - 1) / workSize;
                int start = id * chunkSize;
                int end = min(start + chunkSize, tamanhoTexto);
                
                // Ajustar start para não quebrar palavra no início
                if (start > 0) {
                    while (start < tamanhoTexto && isWordChar(texto[start])) {
                        start++;
                    }
                }
                
                // Ajustar end para não quebrar palavra no final
                if (end < tamanhoTexto) {
                    while (end > start && isWordChar(texto[end - 1])) {
                        end--;
                    }
                }
                
                // Buscar palavra no chunk ajustado
                for (int i = start; i < end; i++) {
                    if (i + tamanhoPalavra <= tamanhoTexto) {
                        // Verificar match exato
                        bool match = true;
                        for (int j = 0; j < tamanhoPalavra; j++) {
                            if (texto[i + j] != palavraAlvo[j]) {
                                match = false;
                                break;
                            }
                        }
                        
                        // VERIFICAÇÃO CONSISTENTE com split("\\W+")
                        if (match) {
                            // Antes: deve ser não-word-char OU início
                            bool antesOk = (i == 0) || !isWordChar(texto[i - 1]);
                            
                            // Depois: deve ser não-word-char OU fim
                            bool depoisOk = (i + tamanhoPalavra >= tamanhoTexto) || 
                                          !isWordChar(texto[i + tamanhoPalavra]);
                            
                            if (antesOk && depoisOk) {
                                ocorrenciasLocal++;
                            }
                        }
                    }
                }
                
                resultado[id] = ocorrenciasLocal;
            }
            """;
    }

    private int executarBuscaOpenCL(byte[] texto, byte[] palavraAlvo) {
        try {
            int workItems = Math.min(256, texto.length / 1000); // Ajustar dinamicamente
            workItems = Math.max(1, workItems);

            int[] resultados = new int[workItems];
            Arrays.fill(resultados, 0);

            // Criar buffers
            cl_mem textoBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_uchar * texto.length, Pointer.to(texto), null);
            cl_mem palavraBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_uchar * palavraAlvo.length, Pointer.to(palavraAlvo), null);
            cl_mem resultadoBuffer = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
                    Sizeof.cl_int * workItems, null, null);

            // Criar kernel
            cl_kernel kernel = clCreateKernel(program, "buscarPalavraConsistente", null);

            // Definir argumentos
            clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(textoBuffer));
            clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(palavraBuffer));
            clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(resultadoBuffer));
            clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{texto.length}));
            clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{palavraAlvo.length}));

            // Executar
            long[] globalWorkSize = new long[]{workItems};
            clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, globalWorkSize, null, 0, null, null);

            // Ler resultados
            clEnqueueReadBuffer(commandQueue, resultadoBuffer, CL_TRUE, 0,
                    Sizeof.cl_int * resultados.length, Pointer.to(resultados), 0, null, null);

            // Somar resultados
            int total = 0;
            for (int count : resultados) {
                total += count;
            }

            // Liberar recursos
            clReleaseMemObject(textoBuffer);
            clReleaseMemObject(palavraBuffer);
            clReleaseMemObject(resultadoBuffer);
            clReleaseKernel(kernel);

            return total;

        } catch (Exception e) {
            System.err.println("Erro na execução OpenCL: " + e.getMessage());
            return -1; // Indicador de erro
        }
    }

    public void limparRecursos() {
        try {
            if (program != null) clReleaseProgram(program);
            if (commandQueue != null) clReleaseCommandQueue(commandQueue);
            if (context != null) clReleaseContext(context);
        } catch (Exception e) {
            System.err.println("Erro ao limpar recursos OpenCL: " + e.getMessage());
        }
    }
}