# Segurança e dados pessoais

Relate vulnerabilidades de forma privada a Carlosz.eduardo1402@gmail.com. Informe versão, passos mínimos para reproduzir e impacto, usando dados fictícios. Não publique senhas, backups, informações de pessoas ou detalhes exploráveis em issues públicas. Não há programa de recompensa ou prazo de resposta garantido.

Escopo revisado: Talumi Android 1.3.0. O APK distribuído é de desenvolvimento, não uma versão de produção endurecida. O banco não tem criptografia independente nem autenticação biométrica própria. Backups JSON e solicitações são texto sem criptografia do app.

A chave de desenvolvimento usada em versões anteriores já acompanhou pacotes privados de código-fonte. Ela deve ser considerada inadequada para produção e não está no pacote público preparado nesta auditoria. Use chave de produção própria e privada para futuras distribuições de produção; trocar a assinatura pode impedir atualização direta da instalação de teste. Nunca publique keystore, senhas de assinatura ou dados reais.

Não foi executado teste de invasão, avaliação forense, análise completa de fornecedores nem certificação de ausência de CVEs. Alertas de versões de dependências precisam de triagem antes de distribuição de produção. Veja docs/auditoria/RELATORIO.md para os limites e riscos restantes.
