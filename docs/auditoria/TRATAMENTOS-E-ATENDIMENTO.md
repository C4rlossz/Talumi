# Registro simplificado e procedimento de atendimento

Carlos Eduardo — Talumi 1.3.0 — 22/10/2026. Documento operacional preliminar; não é RIPD formal exigido ou aprovado pela ANPD. O responsável deve revisar conforme a operação efetiva.

## Inventário de tratamentos

| Fluxo | Dados e local | Destinatário | Finalidade e retenção |
|---|---|---|---|
| Livro financeiro local | Nomes, contas, saldos, transações, datas, salários, dívidas, pessoas e investimentos; SQLite privado | Sem envio automático ao autor | Organização determinada pelo usuário; até editar, excluir, limpar armazenamento ou desinstalar. Uso pessoal exclusivamente particular pode ter regime distinto; não se presume exceção para todos os contextos. |
| Preferências | Tema e habilitação de consulta online; preferências privadas | Sem envio automático | Funcionamento solicitado; mantidas após apagar o livro financeiro, removidas ao limpar armazenamento/desinstalar. |
| WorkManager | Identificador e estado técnico de tarefas, horários; banco privado AndroidX | Sem dados financeiros como entrada da tarefa | Processar recorrências locais; gerido pelo sistema/WorkManager. Limpar armazenamento remove também esses metadados. |
| Consulta de taxas | Série 4389/1178, protocolo, IP e horário de conexão no destino; cache local de taxa/data | API do Banco Central e infraestrutura de rede | Obter referência pública; carteira não enviada. Novas consultas podem ser desligadas. Não se conhece a retenção de logs do terceiro. |
| Exportação | Backup completo JSON ou formulário TXT | Provedor de arquivo escolhido pelo usuário, inclusive nuvem se selecionada | Cópia solicitada; retenção controlada pelo usuário/provedor. Não criptografado pelo app. |
| Contato voluntário | E-mail, nome, texto e anexos realmente enviados pelo usuário | Carlos Eduardo e provedor Gmail da caixa indicada | Responder solicitação e obrigações aplicáveis; bases legais e período devem ser avaliados no atendimento concreto. Não é preenchimento automático do app. |
| GitHub | Código e dados fictícios publicados pelo mantenedor; eventuais contribuições | GitHub e público do repositório | Portfólio e colaboração MIT; políticas da plataforma se aplicam. Proibido publicar bancos e dados reais em issues. |

O autor não é automaticamente receptor dos registros do aparelho. Dados financeiros têm elevado impacto de confidencialidade, embora nem todo dado financeiro seja, por essa categoria isolada, dado pessoal sensível no rol legal. Texto livre pode incluir dados sensíveis indevidamente; a minimização e as advertências procuram evitar isso.

## Atendimento de direitos e acessibilidade

1. Ler pedidos recebidos no contato público; registrar data, assunto e encaminhamento com o mínimo de informações. Não exigir CPF ou documento automaticamente.
2. Informar quando os dados existem apenas no aparelho, orientar consulta/correção/exportação/exclusão local sem pedir um backup completo. Não alegar capacidade de acesso remoto.
3. Para mensagens ou dados efetivamente recebidos pelo autor, identificar finalidade, destinatários, fundamento e retenção; confirmar identidade somente quando necessário e de forma proporcional. Solicitar apenas o indispensável.
4. Verificar os prazos e condições legais aplicáveis a cada pedido. Acesso e demais direitos não têm todos o mesmo prazo; o formulário não substitui a obrigação de responder. Explicar eventuais limites legais, oferecendo as vias cabíveis.
5. Em relato de acessibilidade, pedir apenas contexto técnico útil e voluntário. Não solicitar diagnóstico. Priorizar impossibilidade de executar funções essenciais.
6. Concluir o atendimento com resposta compreensível; revisar a necessidade de conservar dados. Eliminar ou anonimizar o que deixou de ser necessário; justificar retenção legal ou de defesa de direitos. O app não executa essa revisão na caixa de e-mail.

## Segurança e incidentes

Manter autenticação forte e acesso restrito na caixa de contato. Não adicionar analytics, nuvem, gravação de tela ou telemetria sem nova revisão. Em suspeita de incidente, conter o acesso indevido, preservar evidências necessárias de modo restrito, avaliar alcance e risco, buscar orientação especializada e verificar os requisitos e prazos vigentes de comunicação à ANPD e titulares. Não publicar dados expostos em issues. Este procedimento não afirma que um plano de resposta já foi exercitado.

## Reavaliação obrigatória de escopo

Reavaliar agentes, bases legais, destinatários, transferências internacionais, segurança e políticas antes de sincronização, cadastro, anúncios, tratamento voltado a crianças, serviço pago ou uso organizacional. Não transferir a análise deste app local para um futuro serviço em servidor.
