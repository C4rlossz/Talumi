# Assinatura de desenvolvimento

A chave local de teste não faz parte do pacote público e está ignorada pelo Git. Sem chave privada configurada, o ambiente Android usa sua própria chave debug. Isso pode impedir atualizar APKs de teste assinados em outro ambiente.

Para usar uma chave de teste privada existente, configure a variável de ambiente TALUMI_DEBUG_KEYSTORE apontando para seu arquivo fora do repositório. O arquivo deve usar as credenciais debug Android esperadas por esta configuração. Nunca use essa configuração ou a chave previamente distribuída para assinar uma versão de produção. A publicação em loja requer configuração de release e chave própria mantida em sigilo.
