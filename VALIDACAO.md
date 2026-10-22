# Validação do Talumi 1.4.0

Verificações realizadas em 22/10/2026.

- APK debug compilado; pacote com.talumi.financas, versão 1.4.0 (5), Android 8/API 26 ou superior.
- 69 testes aprovados em 10 suítes, sem falhas ou ignorados.
- Android Lint: nenhum erro; 6 avisos sobre versões mais novas de dependências. Isso não é uma análise completa de vulnerabilidades.
- Assinatura V2 verificada, igual à versão 1.3.0; alinhamento conferido com zipalign.
- Documentos locais do APK comparados byte a byte com a fonte.
- Seis pares adicionais de contraste de relatório aprovados nos dois temas; ver docs/auditoria/RELATORIOS-1.4.md.
- Auditoria 1.3.0 preservada como histórico (47 cenários e 76 componentes). Não houve nova resolução de dependências para esse inventário; a atualização não altera declarações de dependências.
- ZIP público sem chave de assinatura, banco, backups, APK/AAB, caches ou configuração local do SDK.

## Testes executados

- AppFlowTest: 1.
- FinanceDbTest: 20.
- InvestmentDbTest: 9.
- InvestmentMathTest: 6.
- LedgerTest: 17.
- LocalRequestTest: 3.
- MarketRatesTest: 2.
- MonthlyReportTest: 8.
- PrivacyDbTest: 2.
- SalaryWorkerTest: 1.

MonthlyReportTest verifica precisão em centavos, negativos, entradas ou saídas isoladas, estado vazio, datas de corte, meses anteriores e futuros, transferências internas, aportes e resgates, exclusão de rendimentos da carteira, categorias e valores altos. O fluxo Compose exercita a aba Relatórios, mudança de mês, gráfico com descrição acessível, acesso aos movimentos, informações do aplicativo, URLs dos perfis, temas claro e escuro e estado vazio após exclusão. Os botões externos são verificados sem abrir serviços no teste.

Os testes anteriores continuam cobrindo contas, salário recorrente, carteira, backups, migrações, privacidade e exclusão local. Não houve instalação de atualização em aparelho físico nem emulador Android nesta sessão. Compatibilidade de atualização foi verificada pela identificação do pacote, incremento de versão, assinatura preservada e manutenção do esquema SQLite 3. Não houve teste de TalkBack real ou de todos os tamanhos de fonte, auditoria jurídica nova nem certificação de segurança.

## Artefato

APK: talumi-1.4.0-debug.apk
SHA-256: 39f3f8d8a2d5c06e59149156870a3faca6700af6e318b2cd5f0ae3b773419b5f

A chave de desenvolvimento continua somente no ambiente local para compatibilidade das versões de teste. Não faz parte do ZIP público. Faça backup antes de atualizar e instale sobre a versão anterior, sem desinstalar.
