# Auditoria do Talumi — autoria, privacidade, acessibilidade e publicação

Data de referência: 22/10/2026. Versão revisada: 1.3.0, pacote com.talumi.financas, SQLite 3. Responsável pelo projeto: Carlos Eduardo. Contato público informado e autorizado pelo responsável: Carlosz.eduardo1402@gmail.com. Licença escolhida: MIT.

## Conclusão

O projeto recebeu revisão técnica e documental assistida por IA, sob solicitação de Carlos Eduardo. Foram preparados documentos, controles locais de privacidade, formulários, atribuições, inventários e um pacote de fonte para GitHub. Esta revisão não é auditoria independente assinada por Carlos Eduardo, parecer jurídico, certificação LGPD/WCAG, busca mundial de originalidade ou garantia de ausência de erros. A ciência e assinatura pessoal do autor não são presumidas.

A atribuição ao autor foi preservada sem apropriação dos direitos de bibliotecas ou ícones. A licença MIT permite reutilização com manutenção do aviso de copyright e licença. Não permite afirmar que todos os componentes sejam de autoria exclusiva de Carlos Eduardo. A documentação registra assistência de IA e distingue os componentes externos.

## Escopo e evidências

Inspeção do código Kotlin, manifesto, recursos XML, configurações de build e backup; resolução de dependências debug pelo Gradle; POMs e avisos dos artefatos; inventário e hashes de recursos; cálculos de contraste; testes automatizados de lógica, SQLite e interface Compose; verificação de assinatura e conteúdo dos pacotes no fechamento da entrega. Os resultados executados ficam em VALIDACAO.md.

Não houve inspeção de aparelho físico, teste de invasão, avaliação de root/malware, perícia de memória flash, teste do e-mail público, revisão contratual do Gmail, pesquisa conclusiva no INPI, busca reversa global de imagens ou consulta completa de CVEs. Logs e código não comprovam comportamentos de todos os fabricantes Android ou serviços externos. O escopo é esta versão e estas dependências, não versões futuras.

## Resultado por pedido

| Área | Situação e evidência |
|---|---|
| Política de privacidade | Criada e disponível offline no aplicativo e em docs/PRIVACIDADE.md. Explica dados locais, IP técnico na consulta pública, arquivos exportados, pedidos e limites. |
| Termos de uso | Criados em docs/TERMOS.md, sem atribuir condição de banco, retorno garantido ou renúncia geral a direitos obrigatórios. A MIT permanece aplicável ao código. |
| Aviso de cookies | Ausência de WebView, cookies de navegador e rastreadores identificados no código próprio. Criado aviso de não utilização; sem banner de consentimento fictício. Site futuro exige avaliação nova. |
| Formulário LGPD | Gera texto local sem buscar dados financeiros ou exigir CPF, documento, justificativa ou consentimento genérico. O usuário decide se envia. Exportação, correção e exclusão local são acessíveis. |
| Formulário de acessibilidade | Criado no app, sem pedir diagnóstico, deficiência ou dados financeiros. Contato público configurado. |
| Contraste | 47 pares/cenários explícitos aprovados pelo método registrado em CONTRASTE.md. Âmbar da pizza e cores de ganho/erro sobre superfícies claras foram ajustados. Não certifica todos os estados ou telas. |
| Imagens e copyright | Três XML de ícone/estrutura adaptativa inventariados, desenhos procedurais identificados e Material Icons atribuídos. Referência de terceiros e raster de branding fora do pacote público. Veja IMAGENS.md. |
| Bibliotecas | 76 artefatos do runtime debug inventariados por coordenada, licença declarada e hash. Textos de licença e avisos preservados em LICENSES e no app. |
| Dados do responsável | Carlos Eduardo e e-mail informado. Não foram inventados empresa, CPF, endereço ou CNPJ. |
| Autoria e licença | LICENSE MIT, AUTORIA.md, THIRD_PARTY_NOTICES.md e créditos no app. Copyright sobre contribuições originais na extensão dos direitos do autor; terceiros preservados. |
| GitHub | Pacote de fonte público separado de artefatos privados, com exclusão de keystore, credenciais, banco, backup, caminhos do SDK, caches e capturas de referência. Nenhum repositório foi publicado nesta etapa. |
| LGPD e riscos jurídicos | Avaliação preliminar e registro de tratamentos/riscos documentados; não há declaração de conformidade integral. Operação real do atendimento e revisão profissional continuam necessárias. |

## Achados corrigidos

- Ausência de políticas e acesso aos avisos dentro do app: documentos locais e central de privacidade implementados.
- Ausência de canal visível: e-mail fornecido pelo autor incluído no app e documentação, sem teste de envio ou propriedade da caixa.
- Ausência de exclusão coletiva: remoção confirmada dos registros financeiros, recorrências e cache de taxas. Tarefas não recriam salários apagados; consultas iniciadas antes da exclusão têm seu resultado invalidado para evitar recompor o cache após a ação. Preferências e arquivos externos têm tratamento explicado.
- Consultas públicas automáticas sem controle de preferência: opção para desativar novas consultas, mantendo a carteira e o cache offline. Não se promete cancelar bytes já enviados.
- Fatia âmbar com 2,13:1 sobre branco: cor e superfície corrigidas, divisórias e descrição textual dinâmica do gráfico acrescentadas. Verde e erro claros foram escurecidos para cobrir também a superfície variante.
- Controle de ocultar valores com 40 dp: aumentado para 48 dp. Ícones de navegação não repetem o rótulo textual.
- Falta de licença autoral explícita: MIT escolhida pelo autor e atribuída a Carlos Eduardo; licenças de terceiros mantidas.
- Chave debug incluída em entregas anteriores: permanece apenas no ambiente local privado para compatibilidade de teste; foi retirada da exceção do .gitignore e excluída do pacote GitHub. Não deve ser chave de produção.

## Riscos e pendências reais

| ID | Nível | Risco residual | Ação antes da situação indicada |
|---|---|---|---|
| R01 | Alto para produção | APK debug, inspeção por ferramentas de desenvolvimento e chave de teste previamente distribuída com fonte privado. | Para produção: chave própria privada, configuração release, avaliação de migração da instalação debug e testes do artefato final. A exclusão do ZIP público não torna a chave antiga secreta novamente. |
| R02 | Alto em aparelho ou arquivo exposto | Banco sem criptografia independente, sem bloqueio biométrico próprio, backup JSON e solicitação em texto. | Proteger aparelho e destino dos arquivos; usar dados fictícios em demonstrações. Avaliar criptografia e autenticação antes de oferecer proteção forte para dados financeiros reais. |
| R03 | Médio | Configuração e retenção do atendimento por Gmail não auditadas; possível tratamento internacional pelo provedor. | Autor deve verificar segurança da caixa, permissões, condições do provedor, salvaguardas aplicáveis e rotina de retenção antes de ampliar atendimento ou receber anexos sensíveis. |
| R04 | Médio | Acessibilidade manual incompleta em tecnologias assistivas, fabricantes e fonte 200%. | Testar TalkBack, Switch Access, fonte, teclado e Scanner; registrar achados e corrigir antes de declarar conformidade completa. |
| R05 | Médio | Exclusividade de nome e novidade dos desenhos não certificadas. | Pesquisar anterioridade no INPI e sinais semelhantes antes de explorar comercialmente a marca; não redistribuir referência sem licença. |
| R06 | Médio | Dependências têm avisos de versões mais novas; ausência de varredura CVE completa. | Triar alertas e aplicar atualizações com testes antes de release. Licença compatível não significa dependência livre de vulnerabilidades. |
| R07 | Médio | Atendimento de direitos, incidentes e retenção dependem de atos humanos, não só do app. | Adotar o procedimento deste diretório, verificar a caixa, responder proporcionalmente, documentar exceções e atualizar os avisos. |
| R08 | Médio conforme uso | Usuário pode inserir nomes e dívidas de terceiros ou escolher backup em nuvem. | Minimizar dados, evitar dados sensíveis e dar destinação legítima aos arquivos. Não presumir que todo uso empresarial ou compartilhamento tenha a exceção doméstica da LGPD. |
| R09 | Médio | Classificação de agentes, bases legais, obrigações e relação de consumo dependem do uso e operação concretos. | Revisão jurídica antes de serviço público com coleta em servidor, monetização, público infantil, uso empresarial ou distribuição comercial. Não declarar dispensa geral da LGPD por ser pessoa física. |
| R10 | Médio antes de loja | Publicação em Google Play não foi executada nem suas declarações preenchidas. | Reavaliar política de dados, SDKs, versão release e formulários da loja no contexto da publicação. |

## Por que não há um selo de 100%

A LGPD alcança pessoas naturais quando cabível; a natureza pessoal do projeto não elimina automaticamente obrigações. A exceção de atividade exclusivamente particular e não econômica depende dos fatos, e não deve ser ampliada para qualquer distribuição do app, suporte ou uso profissional. Dados que o autor recebe em suporte exigem análise própria; o autor não recebe automaticamente os registros financeiros apenas por manter o código. O relatório registra essa distinção, sem presumir todos os papéis jurídicos.

Agentes de pequeno porte podem ter regras diferenciadas e eventual dispensa de encarregado nos termos da Resolução CD/ANPD nº 2; isso não é dispensa de proteção, segurança ou canal ao titular. O projeto informa um contato do responsável, sem inventar nomeação formal de encarregado ou certificação.

## Revisão do responsável

Responsável pelo projeto: Carlos Eduardo. Execução técnica desta revisão: assistida por IA. Contato e MIT foram definidos expressamente pelo responsável. Revisão jurídica profissional, testes físicos e declaração pessoal de ciência dos riscos continuam pendentes; este arquivo não contém assinatura nem alegação de aprovação independente.

## Fontes oficiais consultadas em 22/10/2026

- LGPD consolidada: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm
- Regras de pequeno porte: https://www.gov.br/anpd/pt-br/acesso-a-informacao/institucional/atos-normativos/regulamentacoes_anpd/resolucao-cd-anpd-no-2-de-27-de-janeiro-de-2022
- Cookies: https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/guia_orientativo_cookies_e_protecao_de_dados_pessoais
- Direitos sobre software e obras: https://www.planalto.gov.br/ccivil_03/leis/l9609.htm ; https://www.planalto.gov.br/ccivil_03/leis/l9610.htm
- INPI, marcas: https://www.gov.br/inpi/pt-br/servicos/marcas
- WCAG 2.2: https://www.w3.org/TR/WCAG22/ ; orientação para software: https://www.w3.org/TR/wcag2ict-22/
- Android acessível: https://developer.android.com/guide/topics/ui/accessibility/apps
- Material Icons: https://developers.google.com/fonts/docs/material_icons
- MIT: https://opensource.org/license/mit ; Apache 2.0: https://www.apache.org/licenses/LICENSE-2.0.txt
- Licenças no GitHub: https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/licensing-a-repository
