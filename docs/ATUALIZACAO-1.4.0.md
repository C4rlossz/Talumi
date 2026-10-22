# Talumi 1.4.0

## Relatório do mês

Em **Finanças → Relatórios**, selecione o mês. O anel animado apresenta a proporção entre entradas e saídas confirmadas; o valor no centro é o **resultado do mês = entradas − saídas**. Os valores também aparecem em texto e na descrição acessível do gráfico.

O relatório considera somente registros de contas financeiras, confirmados e com data até hoje, dentro do mês escolhido. Salários recorrentes entram depois do processamento da ocorrência. Agendamentos ainda não confirmados, movimentos futuros, transferências e rendimentos de investimentos não são contados como receita ou despesa.

O cartão **Saldo das contas** é diferente do resultado mensal: inclui saldos iniciais e todos os movimentos confirmados até o fim do mês selecionado, limitado à data atual. Soma bancos, carteira, reservas e cofrinhos; exclui aplicações. Transferir dinheiro para investimentos reduz esse saldo, mas não é uma despesa. O saldo inicial informado não tem data histórica própria: relatórios anteriores também partem dele. Para manter os números coerentes, evite cadastrar lançamentos já incorporados ao saldo inicial.

As saídas são agrupadas por categoria, com total e participação nas despesas. Não são limites de orçamento. O botão **Ver movimentos do mês** abre os registros do mês selecionado.

## Informações do aplicativo

O botão **ⓘ** ao lado das configurações abre tecnologias, armazenamento local, funcionamento offline, consulta opcional de CDI/Selic, cuidados com backups e créditos de Carlos Eduardo. Os links abrem o navegador ou aplicativo externo apenas após um toque:

- GitHub: https://github.com/C4rlossz
- LinkedIn: https://www.linkedin.com/in/carlos-eduardo-a8bb50268/
- Projeto: https://github.com/C4rlossz/Talumi

O LinkedIn foi conferido no campo `profile.linkedin` do repositório público do portfólio do autor. Nenhum perfil foi inventado.

## Atualizar sem perder os registros

Exporte um backup pelas configurações. Instale o APK 1.4.0 sobre a versão anterior, sem desinstalar. Foram preservados o pacote `com.talumi.financas` e a chave local de desenvolvimento; o código de versão passa a 5. O esquema SQLite permanece na versão 3, sem migração destrutiva nesta atualização.

Uma instalação compilada por outra pessoa pode ter assinatura diferente. Nesse caso, não desinstale antes de exportar seu backup; uma nova instalação exigirá a restauração manual do JSON.

## Escopo das verificações

Consulte `VALIDACAO.md` para os resultados desta compilação. A auditoria documental de 1.3.0 permanece como registro histórico; esta atualização não representa certificação jurídica, de segurança ou de acessibilidade. Não acrescenta coleta, rastreamento, dependências ou serviço de nuvem. Gráficos são desenhados pelo código, sem incorporar a imagem de referência.
