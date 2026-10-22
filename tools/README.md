# Verificações reproduzíveis

Python 3, JDK 17 e o SDK/Gradle configurados são pré-requisitos. Execute a partir da pasta Talumi.

- `python tools/audit_visuals.py`: recalcula os 47 cenários de cor e o inventário de recursos, checando tokens da fonte. Revisa documentos em docs/auditoria. Não substitui inspeção física ou captura de tela.
- `./gradlew -I tools/runtime-inventory.init.gradle :app:auditResolvedRuntime`: resolve o runtime debug e escreve coordenadas/caminhos privados em build/audit/runtime-resolved.tsv. No Windows use gradlew.bat. O diretório build é ignorado e não deve ser publicado.
- `python tools/audit_dependencies.py`: lê esse inventário e POMs no cache Gradle, preserva avisos dos AAR/JAR, baixa o texto Apache do site oficial e atualiza inventário, SBOM e avisos. Exige rede para o texto de licença. Arquivos do Gradle Wrapper já preservados em LICENSES não são reobtidos por esse script.

A revisão de licenças não é varredura de CVEs. Alterações de versão, escopo, tipos de licença ou artefatos novos exigem revisão humana; o script interrompe se encontrar uma licença não contemplada. A exceção Guava ListenableFuture está documentada com a tag de origem.

Execute ainda `./gradlew :app:testDebugUnitTest :app:lintDebug`. A declaração de conformidade não deve ser ampliada por apenas repetir esses comandos.
