# Talumi

Criado por **Carlos Eduardo** — projeto autoral de portfólio. Versão 1.3.0.

Organizador financeiro Android com Kotlin, Jetpack Compose e SQLite local. Identidade própria baseada no conceito visual Talumi: violeta, lavanda, símbolo de chip e temas claro/escuro.

## Funcionalidades

- Finanças com contas bancárias manuais, carteira, reservas e cofrinhos.
- Carteira de investimentos separada: CDB, Tesouro Selic, LCI, LCA, fundos e outras aplicações.
- Gráfico de pizza por banco, aportes, resgates e registro de rendimentos/perdas.
- Previsão bruta mensal com CDI e Selic online do Banco Central e cache local.
- Saldo inicial positivo ou negativo e saldo atual calculado pelos lançamentos confirmados.
- Entradas, saídas e transferências entre contas próprias.
- Categorias e observações editáveis, busca e histórico por mês.
- Agenda de pagamentos e recebimentos, com pendências vencidas.
- Recorrências mensais para salário e despesas fixas, com vencimento ajustado em meses curtos.
- Salário automático por dia fixo ou quinto dia útil, com sábados opcionais e exceções locais de feriados/folgas.
- Valores a pagar e receber por pessoa, vencimento e acertos parciais ou totais vinculados à conta escolhida.
- Edição e exclusão de lançamentos com recálculo de saldos.
- Temas claro, escuro e do sistema.
- Exportação e restauração de backup JSON pelo seletor de arquivos do Android.

Não há conexão bancária, Firebase, cadastro ou anúncios. A permissão de internet serve apenas para consultar taxas públicas do Banco Central, sem enviar a carteira. O aplicativo inicia vazio; não inclui saldos ou pessoas fictícias.

## Requisitos e instalação

Android 8.0 (API 26) ou superior. O APK de desenvolvimento fornecido pode ser instalado diretamente no aparelho, permitindo a instalação pela fonte usada para abri-lo quando o Android solicitar.

O APK é uma versão de teste assinada com chave de desenvolvimento. Para publicar na Play Store, configure uma chave de produção própria e gere um AAB de release. A chave de produção deve ser guardada pelo proprietário do projeto. Não altere a assinatura entre atualizações que devem manter os dados instalados.

## Primeiros passos

1. Em **Contas**, adicione cada banco, carteira, reserva ou cofrinho e o saldo inicial. Use um valor como `1250,50`, sem pontos de milhar.
2. Registre entradas e saídas escolhendo a conta. O saldo é o inicial mais os movimentos confirmados. Evite registrar novamente movimentos já incluídos no saldo inicial.
3. Para mover dinheiro entre contas próprias, use **Transferência**. Guardar dinheiro em um cofrinho não é um gasto.
4. Para cadastrar salário automático, abra **Agenda → Cadastrar salário recorrente**. Informe o valor líquido, a conta e a regra: dia fixo ou quinto dia útil. Os recebimentos passam a entrar no saldo local na data definida. As recorrências criadas pelo formulário comum continuam com confirmação manual.
5. Na **Agenda**, abra o compromisso para confirmar, editar ou excluir. Se recebeu antes do previsto, ajuste a data para o dia real e confirme.
6. Em **Pessoas**, registre quem deve a você ou o que você deve. Use **Registrar acerto** para informar pagamento ou recebimento parcial/total e a conta envolvida.
7. Nas configurações, escolha o tema e exporte backups regularmente.

## Como os valores funcionam

- Dinheiro é armazenado em centavos inteiros (`Long`), sem arredondamento de ponto flutuante.
- O saldo disponível das Finanças exclui contas do tipo Investimento. Reservas e cofrinhos continuam nas Finanças. O total investido aparece somente na carteira de investimentos.
- Entradas e saídas mensais consideram movimentos confirmados, incluindo acertos de dívidas. Transferências próprias não entram nesses totais.
- A previsão até o fim do mês soma também os agendamentos até essa data, inclusive pendências anteriores. Ela não inclui dívidas ainda sem lançamento agendado.
- A dívida cadastrada é um compromisso separado e não muda o saldo da conta. Se emprestou dinheiro agora, registre a saída correspondente além do compromisso a receber.
- Um acerto atualiza o saldo da conta e o restante da dívida na mesma transação SQLite. Excluir um acerto reabre o valor correspondente.
- Recorrências preparam os recebimentos até 12 meses à frente; esse horizonte é estendido automaticamente. Uma ocorrência excluída não é recriada. Salários que começam depois do dia de pagamento pulam para o próximo mês.
- A data mensal é ancorada no dia original: um evento de dia 31 ocorre em 28/29 em fevereiro e retorna ao dia 31 em março.
- Encerrar uma recorrência remove somente ocorrências futuras ainda não confirmadas. Preserva o histórico e as pendências até hoje.
- Rendimentos reais de investimentos são informados conforme o extrato. Previsões não são creditadas automaticamente e não representam pagamentos mensais.

## Salário automático e autoria — versão 1.1

O salário pode ser registrado todo mês no dia escolhido ou no quinto dia útil. Para dias úteis, a regra padrão conta segunda a sexta; sábados podem ser incluídos. Domingos são excluídos. Feriados e folgas são datas locais informadas pelo usuário, uma por linha, no formato `dd/MM/aaaa`; não há consulta de feriados pela internet.

Escolha **Começar a partir de** para controlar a primeira ocorrência. Uma data passada contabiliza os recebimentos vencidos desde essa data; hoje ou uma data futura evita somar novamente salários já incluídos no saldo inicial. Confira a prévia do primeiro recebimento antes de salvar.

O processamento ocorre ao abrir/retomar o app, a cada minuto enquanto a interface está em primeiro plano e por trabalho periódico local em segundo plano. O Android pode adiar a execução em segundo plano por economia de bateria; não há promessa de horário exato. Ao abrir o app, as ocorrências vencidas são reconciliadas sem duplicação. Isso registra valores no Talumi e não consulta nem movimenta seu banco.

Se já existe uma entrada mensal cadastrada, use **Agenda → Gerenciar recorrências mensais → Ativar recebimento automático**. Isso reaproveita a recorrência e seus lançamentos, sem duplicá-los, e só ativa ocorrências a partir de hoje. Pendências anteriores permanecem manuais. Para trocar a regra de datas ou o valor mensal, encerre a recorrência antiga e cadastre outra com início adequado.

Editar uma ocorrência automática torna a confirmação daquela ocorrência manual, mantendo as demais automáticas. Excluir uma ocorrência impede que ela seja recriada. Encerrar a série interrompe os recebimentos futuros.

Em **Configurações → Sobre o Talumi**, o aplicativo exibe **Criado por Carlos Eduardo** e **Projeto autoral de portfólio**. O link do GitHub ainda não foi informado; o botão só aparece quando o recurso `creator_github_url`, em `res/values/strings.xml`, receber a URL final.

A atualização usa o mesmo pacote e a mesma assinatura de desenvolvimento da versão 1.0.0, com código de versão superior. O SQLite migra da versão 1 para a 2 adicionando colunas; contas, dívidas, histórico e recorrências existentes são preservados. Recorrências antigas continuam manuais até ativação explícita. Backups da versão anterior continuam compatíveis; novos backups usam o formato 2.

## Carteira de investimentos — versão 1.2

Use o seletor **Finanças / Investimentos** no topo. Os investimentos antigos são movidos automaticamente para a carteira, conservando os mesmos registros, saldos iniciais e movimentos. Eles chegam como **Outro / a classificar**, taxa zero: edite o banco, produto e taxa do contrato. Não há duplicação de valores nem exclusão de histórico. Só contas que já eram do tipo Investimento são reclassificadas; contas comuns não são inferidas pelo nome.

- Para uma aplicação que já existe, cadastre **Valor já investido**. Isso representa patrimônio existente e não desconta outra conta. Não inclua o mesmo valor como saldo disponível do banco.
- Para investir a partir de uma conta das Finanças, crie uma aplicação com zero e use **Aportar**. A conta diminui e o investimento aumenta na mesma transação.
- **Resgatar** faz o caminho inverso. Exige saldo investido e respeita o bloqueio até o vencimento informado.
- **Registrar rendimento ou perda** atualiza somente o investimento. Use o valor efetivo do extrato; para tornar o dinheiro disponível nas Finanças, registre depois o resgate.
- A pizza agrupa saldos positivos por instituição. Saldos negativos legados são preservados, sinalizados e excluídos das fatias.

**Taxas e previsão:** CDI é um índice, enquanto CDB e Tesouro Selic são produtos. Escolha percentual do CDI (ex.: 110%), Selic acrescida de taxa anual, prefixado anual ou taxa mensal. CDI e Selic anuais efetivos vêm das séries SGS 4389 e 1178 do Banco Central. A consulta ocorre ao abrir a carteira quando não há cache consultado no dia, ou ao tocar em Atualizar. Falhas mantêm a última taxa salva e sua data; sem taxa, a previsão correspondente fica indisponível. Referências com mais de sete dias exibem aviso. Nenhum dado de conta é enviado.

A estimativa parte do dia seguinte à **Data do saldo de referência**, até o fim do mês escolhido ou vencimento. Após qualquer movimento confirmado, a data passa a hoje, pois o saldo registrado foi atualizado; a previsão do mês corrente passa a cobrir somente o período restante. Para uma projeção de mês completo, selecione um mês futuro. Não existe capitalização automática entre meses projetados: cada mês usa o saldo registrado atual como base.

Taxa mensal usa dias corridos proporcionalmente à duração do mês. Taxa anual usa capitalização em base 252 dias úteis. Percentual de CDI incide sobre a taxa diária equivalente, capitalizada nos dias úteis do período. Selic mais taxa usa o produto dos fatores anuais. Dias úteis excluem sábados, domingos e os feriados que você informar por aplicação. O app não busca calendário de feriados. O resultado em centavos é arredondado uma vez, ao final.

A previsão é **bruta**, com saldo e taxas constantes, sem impostos, taxas, futuros aportes ou marcação a mercado. Não é garantia de retorno, cotação, saldo creditado ou cupom mensal. O valor efetivo do Tesouro Selic em venda antecipada pode diferir. Confira o extrato para registrar os valores realizados.

Fontes oficiais: [Selic anualizada base 252 — Banco Central](https://dadosabertos.bcb.gov.br/dataset/1178-taxa-de-juros---selic-anualizada-base-252), [API SGS CDI](https://api.bcb.gov.br/dados/serie/bcdata.sgs.4389/dados/ultimos/1?formato=json), [regras do Tesouro Direto](https://www.tesourodireto.com.br/sobre-o-tesouro/regras-e-regulamento).

Esta versão mantém pacote e assinatura das versões anteriores e migra o SQLite para a versão 3. Backups 1 e 2 continuam aceitos; novos backups usam formato 3 e incluem aplicações e taxas salvas.

## Dados e backup

O SQLite fica no armazenamento privado do aplicativo. O backup automático do Android está desativado; desinstalar ou limpar os dados do aplicativo remove os registros locais. Faça uma exportação antes dessas ações.

O backup JSON não é criptografado. A restauração substitui os registros atuais, exige confirmação e valida estrutura, referências e regras financeiras dentro de uma transação. Um arquivo inválido desfaz a tentativa de substituição.

Esta versão não envia notificações no horário dos vencimentos. Os agendamentos e atrasos são consultados na agenda.

## Desenvolvimento

Abra esta pasta no Android Studio, instale o SDK Android 35 e use JDK 17. O Gradle Wrapper usa Gradle 8.11.1.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
```

No macOS/Linux:

```sh
sh gradlew :app:assembleDebug
sh gradlew :app:testDebugUnitTest :app:lintDebug
```

O SDK é configurado pelo Android Studio em `local.properties`, que não deve ser versionado. A chave de teste não acompanha o pacote público. O ambiente local privado pode conter a chave antiga para compatibilidade de atualizações; ela não deve ser usada para produção ou publicada. A variável opcional `TALUMI_DEBUG_KEYSTORE` pode apontar para outra chave de desenvolvimento; sem arquivo configurado, usa-se a chave de debug padrão do ambiente Android.

### Estrutura

- `Models.kt`: modelos, valores monetários, datas e cálculos financeiros puros.
- `FinanceDb.kt`: esquema SQLite, validações, transações, recorrências e backup.
- `FinanceViewModel.kt`: estado da interface, operações de disco fora da thread principal e preferências de tema.
- `MainActivity.kt`: navegação, dashboard, contas, agenda, histórico e identidade visual.
- `Forms.kt`: formulários, seletores, detalhes e confirmações.
- `Investments.kt`: produtos, índices, carteira e estimativas.
- `InvestmentUi.kt`: carteira, gráfico por banco e operações.
- `MarketRates.kt`: consulta HTTPS restrita às séries públicas do Banco Central, sem dados financeiros pessoais.
- `SalaryWorker.kt`: aplicação, conexão compartilhada com o SQLite e processamento periódico de salários com WorkManager.
- `app/src/test/`: testes de cálculo, persistência SQLite e fluxo Compose usando JUnit e Robolectric.

As operações de escrita da interface são serializadas. Saldos e valores restantes são derivados do histórico, evitando contadores que possam divergir dos lançamentos.

## Identidade e dependências

O símbolo de chip é desenhado em Canvas e nos recursos vetoriais do projeto. A interface usa tipografia do sistema Android e ícones Material. Kotlin, AndroidX, Material e demais bibliotecas mantêm suas licenças próprias. O projeto não depende de templates de interface de terceiros.

O nome Talumi e a identidade visual ainda não passaram por pesquisa de disponibilidade de marca ou domínio. Antes de distribuir comercialmente, finalize esses pontos e a assinatura de produção.

## Privacidade, acessibilidade e licença — 1.3.0

Copyright 2026 Carlos Eduardo. Contribuições originais sob [MIT](../LICENSE), com assistência de IA registrada em [Autoria](AUTORIA.md). Componentes externos mantêm seus titulares e licenças em [Créditos de terceiros](../THIRD_PARTY_NOTICES.md) e LICENSES.

No aplicativo: Configurações → Privacidade, acessibilidade e licenças. Documentos offline, contato público, formulários que geram texto local, controle das consultas públicas de CDI/Selic e exclusão confirmada do livro financeiro. Nenhum formulário envia seu banco ou e-mail automaticamente.

- [Política de privacidade](PRIVACIDADE.md), [Termos](TERMOS.md) e [Cookies/armazenamento](COOKIES.md).
- [Acessibilidade e suas limitações](ACESSIBILIDADE.md), [Formulários](FORMULARIOS.md).
- [Relatório da auditoria](auditoria/RELATORIO.md), [Contraste](auditoria/CONTRASTE.md), [Imagens](auditoria/IMAGENS.md), [Dependências](auditoria/DEPENDENCIAS.md).
- [Publicar no GitHub](PUBLICAR-NO-GITHUB.md) e [Segurança](../SECURITY.md).

Contato do responsável: Carlosz.eduardo1402@gmail.com. Projeto pessoal de portfólio; não há CNPJ informado. A revisão documenta correções e riscos pendentes, sem prometer conformidade jurídica integral, ausência de erros ou certificação de acessibilidade. Consulte VALIDACAO.md para os testes realmente executados.
