# Declaração de acessibilidade

Talumi 1.3.0 — avaliação técnica de 22/10/2026. Responsável: Carlos Eduardo — Carlosz.eduardo1402@gmail.com.

## Compromisso e escopo

O projeto busca uma experiência acessível no Android. A revisão usa WCAG 2.2 como referência, a orientação WCAG2ICT para software não web e as recomendações Android. Não é um certificado de conformidade integral WCAG, eMAG ou NBR, nem substitui testes com pessoas com deficiência.

## Medidas implementadas e verificáveis

Temas claro, escuro e do sistema; texto nativo escalável em sp; botões Material; descrição de ações sem rótulo textual; formulários com rótulos; estados de entrada, saída, pendência e valor negativo expressos por texto, não só por cor. O gráfico da carteira tem uma descrição com bancos, valores e percentuais e legenda textual equivalente.

Foi corrigida a fatia âmbar da pizza: a cor anterior tinha contraste de 2,13:1 sobre branco; a nova paleta e a superfície explícita são medidas no relatório de contraste. Divisórias separam as fatias. Verde de ganho e vermelho de erro claros também foram ajustados para preservar 4,5:1 sobre a superfície variante. O botão para ocultar valores agora tem 48 dp. Os interruptores receberam rótulos explícitos para leitor de tela e os erros de formulário são anunciados como atualização acessível. Ícones de navegação decorativos não repetem o rótulo já apresentado. Documentos e formulários locais são acessíveis sem uma página web externa.

A análise matemática cobre os pares de cores explicitamente mapeados no código e os gradientes amostrados. Para texto comum, a referência é 4,5:1; para elementos gráficos e controles identificáveis, 3:1. Não equivale a medir cada pixel de todos os estados do sistema ou componentes Material.

## Limitações conhecidas

Ainda faltam testes manuais com TalkBack, Switch Access, teclado externo e Accessibility Scanner em aparelho físico, diferentes versões Android e ampliação de fonte até 200%. Linhas de contas, valores longos e a navegação com cinco itens precisam ser conferidos nessas configurações. Diálogos e seletor de arquivo dependem também do sistema e do fabricante. A auditoria não declara que não existam barreiras.

## Relatar uma barreira

Use Configurações → Privacidade, acessibilidade e licenças → Formulário de acessibilidade, ou escreva para Carlosz.eduardo1402@gmail.com. Indique a tela, o comportamento esperado e observado, e opcionalmente versão Android, tamanho de fonte e tecnologia assistiva usada. Não é necessário informar deficiência, diagnóstico, CPF ou dados financeiros.

O formulário gera um texto local; não envia automaticamente. Você revisa e decide se encaminha. O autor deve responder conforme as informações disponíveis e priorizar barreiras que impeçam acesso às funções essenciais, sem prometer uma data antes da avaliação.

Referências: https://www.w3.org/TR/WCAG22/ ; https://www.w3.org/TR/wcag2ict-22/ ; https://developer.android.com/guide/topics/ui/accessibility/apps .
