# Verificação complementar — Relatórios 1.4.0

22/10/2026. Luminância relativa sRGB, método matemático. Complementa o registro histórico 1.3.0, sem substituir seu escopo. As cores dos arcos também são usadas na legenda textual. Exigência adotada: 4,5:1 para texto e pelo menos 3:1 para gráficos.

| Tema | Elemento | Cor | Superfície | Razão |
|---|---|---|---|---:|
| Claro | Entrada / anel | #0F765E | #FFFFFF | 5.5654:1 |
| Claro | Saída / anel | #BA3550 | #FFFFFF | 5.6560:1 |
| Claro | Categoria / progresso | #7047EB | #FFFFFF | 5.5411:1 |
| Escuro | Entrada / anel | #62D7B0 | #1E192A | 9.6657:1 |
| Escuro | Saída / anel | #FF8FA3 | #1E192A | 7.9090:1 |
| Escuro | Categoria / progresso | #B69AFF | #1E192A | 7.3759:1 |

Arcos com separação de superfície, valores textuais, indicação textual de resultado negativo e descrição acessível com entradas, saídas e resultado. Trilha neutra é decorativa; o estado vazio tem texto. Animação finita de 850 ms usa os mecanismos do Compose, sem temporizador infinito. A preferência de duração de animação do sistema é delegada ao Compose.

Inspeção de código e testes Compose em Android simulado, sem validação visual em aparelho físico, TalkBack real ou fonte ampliada. Não certifica acessibilidade integral. Sem novas imagens raster, dependências ou serviços de análise.
