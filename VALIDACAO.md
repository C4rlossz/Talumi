# Validação do Talumi 1.3.0

Verificações realizadas em 22/10/2026.

- APK debug compilado; pacote com.talumi.financas, versão 1.3.0 (4), Android 8/API 26 ou superior.
- 61 testes aprovados, sem falhas ou ignorados.
- Android Lint: nenhum erro; 6 avisos de dependências com versões mais novas, sem triagem CVE completa.
- Assinatura V2 verificada, igual à das versões anteriores; alinhamento conferido com zipalign.
- Documentos locais do APK comparados byte a byte com a fonte.
- 47 cenários/pares de contraste explícitos aprovados pela medição matemática registrada.
- 76 dependências do runtime debug inventariadas, com hashes e avisos de licença.
- Pacote público separado, sem keystore, banco, backups, APK/AAB, caches ou configuração local do SDK.

## Testes executados

- AppFlowTest: 1.
- FinanceDbTest: 20.
- InvestmentDbTest: 9.
- InvestmentMathTest: 6.
- LedgerTest: 17.
- LocalRequestTest: 3.
- MarketRatesTest: 2.
- PrivacyDbTest: 2.
- SalaryWorkerTest: 1.

O fluxo Compose em Android simulado pelo Robolectric exercita contas, entradas, salário recorrente, temas, autoria, carteira, aportes, rendimento, resgate, leitura da política, abertura do formulário de acessibilidade, desativação de consultas e confirmação de exclusão local. Testes de banco verificam migrações, backups e ausência de recriação após apagar registros. O texto de solicitação é testado sem rede e sem leitura do banco financeiro.

Não foi testado em aparelho físico nem emulador Android nesta sessão. Não foram executados TalkBack real, Accessibility Scanner, envio de formulário para a caixa de e-mail, teste de invasão, avaliação contratual de provedores ou certificação jurídica. Ver docs/auditoria/RELATORIO.md e VALIDACAO-MANUAL.md para riscos e pendências. O teste automatizado não certifica conformidade integral ou ausência de erros.

## Artefato

APK: talumi-1.3.0-debug.apk
SHA-256: 770c8ac86fed3df6129abb62f4150ec7b824e3624e55c6cac673fe2d94395adc

A chave é de desenvolvimento, não de produção. Foi mantida apenas localmente para compatibilidade de testes e excluída do ZIP público. Não use ZIPs antigos que incluíam a chave como fonte de publicação.
