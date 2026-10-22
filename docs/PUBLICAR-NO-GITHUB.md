# Publicação do Talumi no GitHub

Autor: Carlos Eduardo. Licença MIT. Contato público: Carlosz.eduardo1402@gmail.com.

Use a pasta Talumi extraída de talumi-1.3.0-github.zip. Repositório do projeto: https://github.com/C4rlossz/Talumi . A publicação do código é uma etapa posterior à auditoria original; os resultados daquela revisão continuam identificados por versão. Não publique a pasta de trabalho inteira nem ZIPs antigos: eles podem conter chave de teste, arquivos de máquina, imagens de referência e versões de desenvolvimento.

O pacote público contém fonte, testes, Gradle Wrapper, LICENSE, atribuições, documentos de privacidade/acessibilidade, segurança e relatórios. Exclui keystores, senhas de assinatura, APK/AAB, local.properties, banco, backup, cache, configuração da IDE e imagens de referência. A chave local foi mantida fora desse pacote apenas para gerar a atualização de teste com assinatura compatível.

Antes do primeiro envio, revise os arquivos extraídos e os resultados de VALIDACAO.md e docs/auditoria/RELATORIO.md. Configure JDK 17 e SDK Android 35 em seu ambiente. Execute os testes e o Lint. Sem sua chave de teste privada configurada, outro ambiente usará uma assinatura debug diferente; isso não prejudica a publicação do fonte, mas pode impedir atualização direta da instalação antiga.

Na página do repositório, apresente o produto e a contribuição de Carlos Eduardo, o uso de Kotlin/Compose/SQLite, os objetivos de portfólio e a assistência de IA registrada em AUTORIA.md. Não afirme que Material Icons/AndroidX sejam obras exclusivas suas, que o nome tenha registro INPI ou que exista certificação LGPD/WCAG. A MIT permite uso comercial e modificações por terceiros com preservação dos avisos; isso foi escolhido pelo responsável.

Para capturas e demonstrações, use dados fictícios. Se gerar um vídeo ou imagem nova, revise a ausência de saldos reais, nomes de terceiros, e-mails de usuários, notificações, CPF e dados de conta. Preserve LICENSES e THIRD_PARTY_NOTICES ao distribuir o app ou fonte. Atualize o inventário de dependências e os avisos ao mudar o produto.

Pendências para produção/loja são distintas da publicação do fonte: assinatura de produção, segurança de dados reais, testes físicos de acessibilidade, operação de atendimento e revisão jurídica no contexto de distribuição. Não trate o APK debug como release de produção.
