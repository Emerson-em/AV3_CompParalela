Como rodar no IntelliJ:
File -> Project Structure -> Modules -> Dependencies -> Clique no "+" -> JARs ou Directories -> clique no arquivo "jocl-2.0.4.jar" -> Apply e OK
Por fim, basta rodar o "Main"


ANÁLISE DE DESEMPENHO DE CONTAGEM DE PALAVRAS EM AMBIENTES SERIAIS E PARALELOS UTILIZANDO CPU E GPU.

Autor 1: Emerson Evangelista Mesquita.

Autor 2: Ana Luiza Craveiro Veras.


Palavras-chave: processamento paralelo. CPU. GPU. OpenCL. desempenho.
 

RESUMO
 

Este trabalho apresenta uma análise comparativa entre três métodos de contagem de palavras implementados em Java: uma versão sequencial executada na CPU, uma versão paralela estruturada por meio de múltiplas threads e uma versão paralela executada na GPU utilizando OpenCL. O objetivo da pesquisa foi mensurar o comportamento dessas abordagens quando submetidas a diferentes arquivos de texto e a múltiplas execuções, permitindo identificar padrões de performance entre arquiteturas seriais e paralelas. Para isso, desenvolveu-se um framework capaz de realizar testes repetidos, coletar tempos de execução e registrar automaticamente os resultados em arquivos CSV. A análise estatística e gráfica evidenciou como cada abordagem respondeu a diferentes condições de carga, revelando que o paralelismo em CPU demonstrou o melhor desempenho geral, enquanto a GPU exibiu maior custo inicial, beneficiando-se apenas em cenários de volumetria muito elevada. As conclusões obtidas reforçam a importância de compreender a relação entre arquitetura de processamento, tamanho do conjunto de dados e eficiência computacional.

 
INTRODUÇÃO
 

A necessidade crescente de manipular grandes quantidades de dados textuais tem impulsionado o estudo de técnicas de processamento paralelo. Embora a tarefa de contar palavras seja simples sob o ponto de vista algorítmico, sua execução em larga escala permite analisar, de maneira didática e prática, como diferentes arquiteturas influenciam a performance de algoritmos aplicados sobre grandes massas de dados. Neste contexto, o presente trabalho propõe a implementação e comparação de três abordagens distintas: a primeira, denominada SerialCPU, consiste em um processamento sequencial tradicional; a segunda, ParallelCPU, faz uso de múltiplas threads para dividir a tarefa entre vários núcleos; a terceira, ParallelGPU, explora o paralelismo massivo provido por aceleradores gráficos por meio da tecnologia OpenCL.

As três abordagens recebem como entrada um arquivo de texto e uma palavra a ser procurada, produzindo como saída o número de ocorrências e o tempo de processamento. A comparação sistemática entre elas permite observar como diferentes arquiteturas respondem às mesmas tarefas sob condições variáveis e revela insights importantes sobre eficiência computacional.	

 
METODOLOGIA
 

A metodologia deste trabalho envolveu inicialmente a implementação das três abordagens de contagem de palavras, todas escritas em Java e estruturadas de forma a manter consistência entre os métodos. A versão sequencial percorre o texto utilizando laços tradicionais, enquanto a versão paralela para CPU divide o texto em partes processadas por múltiplas threads, configuradas de acordo com o número de núcleos disponíveis no ambiente de teste. Já a versão para GPU foi construída utilizando a biblioteca JOCL, permitindo a execução de kernels OpenCL que realizam a contagem de palavras em paralelo diretamente na placa gráfica.

Para que os resultados fossem reprodutíveis, desenvolveu-se um framework de testes responsável por executar automaticamente cada método diversas vezes, variando arquivos de tamanhos diferentes e ajustando o número de núcleos durante as execuções paralelas da CPU. Em seguida, os tempos coletados foram armazenados em arquivos CSV, possibilitando a organização automática das informações e facilitando a produção de gráficos. A análise estatística dos dados resultantes consistiu na comparação direta dos tempos médios e no estudo das variações observadas entre os métodos em cenários de carga distintos. Essa abordagem permitiu estudar o comportamento dos algoritmos sob diferentes condições e de forma sistemática.

RESULTADOS E DISCUSSÃO
 

Os resultados obtidos demonstraram diferenças marcantes entre as três abordagens implementadas. A versão sequencial apresentou desempenho previsível, com tempos de execução proporcionais ao tamanho do texto analisado. Sua eficiência foi notável em arquivos pequenos, nos quais o custo de criação de threads ou inicialização da GPU não se justificava, o que evidencia que métodos simples ainda são bastante adequados quando o volume de dados não é significativo.

A versão paralela em CPU apresentou ganhos relevantes, principalmente em textos médios e grandes. Os tempos foram reduzidos consistentemente quando comparados ao método sequencial, e observou-se que o melhor desempenho ocorreu quando se utilizou um número de threads equivalente ou próximo ao número de núcleos físicos da máquina de teste. Em situações onde esse limite foi ultrapassado, os ganhos diminuíram e o excesso de threads passou a gerar mais sobrecarga do que benefício, demonstrando o comportamento clássico de saturação de recursos computacionais.

A versão paralela em GPU apresentou comportamento distinto. Embora a GPU seja capaz de oferecer paralelismo massivo, o custo inicial envolvido na transferência dos dados para o dispositivo e na preparação do kernel influenciou fortemente o resultado. Isso fez com que, para arquivos pequenos e médios, a GPU apresentasse tempos superiores aos obtidos tanto pela versão sequencial quanto pela paralela da CPU. Apenas nos maiores textos foi possível observar uma melhora relativa, embora ainda inferior à obtida pelo paralelismo em CPU. Esse comportamento está alinhado com a literatura sobre computação paralela, que indica que operações com overhead significativo tendem a compensar apenas quando o volume de dados é suficientemente alto.

Os gráficos construídos a partir dos arquivos CSV reforçaram essas conclusões. As curvas da execução sequencial apresentaram comportamento praticamente linear, enquanto as curvas da versão paralela em CPU mostraram quedas acentuadas de tempo à medida que mais núcleos eram utilizados, até o ponto em que a saturação se manifestou. No caso da GPU, os gráficos evidenciaram maior variabilidade nos resultados, com tempos mais longos para arquivos menores e um comportamento mais estável conforme o tamanho dos arquivos aumentava. A interpretação conjunta dos resultados confirma que o paralelismo em CPU é, no cenário apresentado, a abordagem mais eficiente e equilibrada entre custo e desempenho.	
	

CONCLUSÃO
 

A análise desenvolvida ao longo deste trabalho permitiu compreender, de maneira aprofundada, como diferentes arquiteturas e estratégias de processamento influenciam o desempenho de um mesmo algoritmo quando submetido a condições variadas de carga computacional. A partir da implementação de versões serial, paralela em CPU e paralela em GPU do algoritmo de contagem de palavras, foi possível observar o impacto direto do paralelismo, do número de núcleos e também do overhead de inicialização característico de cada abordagem.

Os resultados demonstraram que a versão sequencial, embora simples, permanece competitiva em cenários de baixa demanda, pois não sofre com custos adicionais de criação de threads ou transferência de dados. Essa constatação reafirma que, para tarefas pequenas e moderadas, soluções diretas continuam sendo frequentemente as mais adequadas. No entanto, à medida que o tamanho dos arquivos aumenta, a limitação inerente ao processamento sequencial torna-se evidente, revelando tempos de execução proporcionalmente maiores.

A versão paralela em CPU destacou-se como a estratégia mais eficiente na maioria dos experimentos realizados. O paralelismo por múltiplas threads aproveita diretamente a arquitetura multicore sem exigir custos elevados de inicialização ou transferência de dados. A análise estatística confirmou que, quando o número de threads corresponde ao número de núcleos disponíveis, o desempenho alcança seu melhor equilíbrio entre divisão de carga e comunicação entre threads. Quando esse limite é ultrapassado, a sobrecarga na coordenação entre as threads impede que melhorias adicionais sejam alcançadas, demonstrando que o desempenho paralelo depende não apenas da quantidade de threads utilizadas, mas principalmente de um controle cuidadoso sobre a granularidade da tarefa e da estrutura física da máquina.

Por outro lado, a versão paralela executada na GPU apresentou comportamento mais complexo e menos previsível. Embora a GPU seja, em teoria, a arquitetura mais poderosa quando se trata de paralelismo massivo, sua utilização prática para tarefas textuais relativamente simples demonstrou-se limitada devido ao alto custo de preparação. O tempo gasto na transferência do arquivo para a placa gráfica e na inicialização do kernel OpenCL tornou-se significativo, superando em muitos casos o tempo total das abordagens em CPU. Os resultados mostraram que esse método só começa a se tornar competitivo quando o volume de dados ultrapassa um ponto crítico no qual o paralelismo massivo da GPU consegue compensar os custos iniciais. Entretanto, mesmo nesses cenários, o método ParallelCPU manteve desempenho superior, evidenciando que o tipo de problema analisado — essencialmente baseado em operações textuais e não numéricas — favorece arquiteturas com menor latência e maior eficiência em manipulação de memória tradicional.

A partir dessa análise, torna-se possível formular uma compreensão mais ampla sobre a escolha da arquitetura de processamento. O trabalho evidencia que a GPU não é universalmente superior e que seu uso deve ser considerado apenas quando a natureza da tarefa exige operações altamente paralelizáveis e com blocos de dados suficientemente grandes para diluir o overhead. O paralelismo em CPU, por sua vez, mostrou-se uma solução equilibrada, versátil e eficiente para a grande maioria dos casos presentes neste estudo. Além disso, o framework de testes desenvolvido, juntamente com a coleta de dados via arquivos CSV e a análise estatística realizada, demonstrou ser fundamental para compreender o comportamento real das implementações.A utilização de diferentes tamanhos de arquivos e a repetição dos testes contribuíram para a confiabilidade dos resultados e permitiram observar tendências estáveis de desempenho.

Em síntese, o estudo evidenciou que a escolha entre processamento serial, paralelismo em CPU ou paralelismo em GPU deve ser guiada não apenas pelas capacidades teóricas das arquiteturas, mas principalmente pelo tipo de tarefa, pelo tamanho do conjunto de dados e pela relação entre o overhead de inicialização e o trabalho efetivo executado. Os resultados obtidos fornecem uma base sólida para trabalhos futuros que explorem algoritmos mais complexos ou novos padrões de paralelismo, além de contribuírem para o entendimento prático das vantagens e limitações das arquiteturas analisadas.


