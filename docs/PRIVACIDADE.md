# Política de privacidade do Talumi

Versão documental 1.1 — 22/10/2026 — aplicativo 1.4.0.

## Responsável e contato

Carlos Eduardo é o autor e responsável pelo projeto pessoal de portfólio Talumi. Não há empresa ou CNPJ informado. Contato público para privacidade e acessibilidade: Carlosz.eduardo1402@gmail.com. Este aviso descreve a versão Android local; não abrange cópias modificadas por terceiros nem um futuro site.

## Dados no aparelho

O aplicativo permite registrar nomes de contas e instituições, saldos, transações, categorias, observações, datas, salários, aplicações, rendimentos e nomes de pessoas ligadas a dívidas ou recebimentos. O processamento e a persistência desses registros ocorrem no SQLite privado do aplicativo. Tema e preferência de consultas online ficam em preferências locais. O WorkManager também mantém metadados técnicos de tarefas, como identificadores e horários de execução, em armazenamento privado; não recebe registros financeiros como entrada da tarefa. As taxas públicas de CDI e Selic são armazenadas localmente com suas datas.

O autor não recebe automaticamente esses registros, não tem acesso remoto ao banco e não consegue recuperá-los após perda do aparelho ou apagar dados remotamente. O usuário determina os registros que insere. Evite CPF, senhas bancárias, dados de saúde e informações desnecessárias sobre outras pessoas. Não existe cadastro, login, conexão com banco, anúncios, analytics ou Firebase nesta versão.

## Consulta pública de CDI e Selic

Com a opção de consultas online ativada, abrir a carteira ou atualizar as taxas faz requisições HTTPS às séries públicas 4389 e 1178 em api.bcb.gov.br. A consulta usa o identificador da série e quantidade de resultados; não inclui contas, saldos, nomes ou investimentos do usuário. O serviço e os intermediários de rede podem receber informações técnicas normais da conexão, como IP, horário e dados de protocolo. Eles têm suas próprias práticas de tratamento. Não prometemos ausência de logs no servidor do Banco Central.

As consultas estão habilitadas inicialmente para manter a função de taxas online solicitada para o aplicativo. É possível desligá-las em Configurações → Privacidade, acessibilidade e licenças. Isso afeta as próximas consultas; uma requisição já iniciada pode terminar. A carteira e as taxas já salvas continuam disponíveis offline. Esse controle de função não é uma autorização genérica para compartilhar dados financeiros.

## Arquivos de backup e formulários

Backup JSON e solicitações em texto são gravados somente no destino que você escolhe no seletor de arquivos do Android. O provedor de arquivos pode ser local ou nuvem, conforme sua escolha. Os arquivos não são criptografados pelo Talumi. Quem acessar um backup poderá ler seus registros. Backup automático do Android está desabilitado na configuração do aplicativo, sujeito aos comportamentos do sistema e fabricante.

Os formulários de LGPD e acessibilidade não consultam o banco financeiro e não são enviados por servidor ou e-mail automaticamente. Nome e e-mail de resposta são opcionais. O texto inclui somente os campos preenchidos, a data e a versão do aplicativo. Você decide se encaminha o arquivo ao contato público. Não anexe backup financeiro a uma solicitação comum.

## Relatórios e links do autor

Os relatórios mensais são calculados no aparelho a partir dos registros locais, sem serviço externo de gráficos. Os botões de GitHub e LinkedIn abrem o navegador ou aplicativo externo somente após um toque. Nenhum saldo ou registro financeiro é incluído nesses links. A visita ao serviço externo fica sujeita às práticas de privacidade desse serviço.

## Se você enviar contato ao autor

Ao enviar voluntariamente um e-mail, o autor poderá receber endereço de resposta, nome, conteúdo e anexos que você decidir enviar. A finalidade é responder ao pedido, tratar o problema relatado e cumprir obrigações aplicáveis. O autor deve tratar pedidos de direitos para cumprimento de obrigação legal, quando aplicável (LGPD, art. 7º, II); pedidos de suporte relacionados ao uso podem envolver os procedimentos solicitados pelo titular (art. 7º, V). Finalidades adicionais, dados sensíveis ou outros contextos exigem avaliação própria, sem extensão automática dessas hipóteses.

O atendimento usa a caixa Gmail informada; o provedor de e-mail pode envolver armazenamento e processamento fora do Brasil. O autor deve verificar as condições e salvaguardas aplicáveis antes de encaminhar ou armazenar dados recebidos em outros serviços. A auditoria do código não verificou a configuração dessa caixa. Não informe diagnóstico médico nem outros dados sensíveis no formulário de acessibilidade.

Não existe prazo técnico de retenção imposto pelo aplicativo à caixa de e-mail. O procedimento de atendimento determina revisão e eliminação ou anonimização após conclusão, salvo necessidade justificada de preservação legal ou defesa de direitos. O titular pode solicitar informações sobre finalidade, fundamento, compartilhamento e retenção de um atendimento concreto. Não alegamos que mensagens já enviadas sejam apagadas automaticamente pelo app.

## Acesso, correção, cópia e exclusão

Consulte e edite os registros no próprio aplicativo; obtenha uma cópia em Exportar backup local. Excluir todos os registros locais remove contas, transações, dívidas, recorrências, aplicações e taxas salvas, mediante confirmação. As preferências de tema e rede permanecem. Para remover também as preferências, use Limpar armazenamento nas configurações Android ou desinstale. Faça cópia antes se quiser preservar algo.

A exclusão não apaga backups ou solicitações exportados, cópias em nuvem ou mensagens enviadas. Você precisa gerir essas cópias no serviço correspondente. O SQLite usa exclusão segura lógica, mas não há garantia de remoção forense de memória flash, snapshots, aparelho comprometido ou cópias do fabricante.

Para dados efetivamente recebidos pelo autor, use Carlosz.eduardo1402@gmail.com ou gere o formulário LGPD no app. Não é exigido CPF nem uma justificativa para exercer um direito. Qualquer verificação de identidade deve ser proporcional e estritamente necessária, sem pedir o banco financeiro completo. Prazos e limites legais serão observados conforme a natureza do pedido; não há promessa de resposta instantânea. Permanecem os direitos de reclamação à ANPD e de acesso às vias legais.

## Segurança, retenção e alterações

Os registros locais permanecem até sua edição, exclusão, limpeza ou desinstalação. O isolamento do Android e HTTPS reduzem riscos, mas não eliminam acesso em aparelho desbloqueado, root, malware ou backups expostos. O banco não tem criptografia independente nem bloqueio biométrico próprio. O APK fornecido é de desenvolvimento e não deve ser tratado como versão de produção endurecida.

Este aviso deve ser revisto antes de introduzir conta online, publicidade, analytics, sincronização, novos destinatários ou publicação em loja. A licença MIT do código não substitui os direitos sobre dados pessoais.

Fontes: LGPD — https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm ; ANPD — https://www.gov.br/anpd/pt-br .
