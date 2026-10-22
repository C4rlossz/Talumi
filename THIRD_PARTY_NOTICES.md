# Licenças e créditos de terceiros

Talumi 1.3.0 — 22/10/2026.

As contribuições originais de Carlos Eduardo usam MIT. Essa licença não altera as licenças nem atribui ao autor a propriedade de componentes de terceiros.

O APK debug resolve 76 artefatos de runtime: famílias AndroidX/Jetpack Compose, Material, Activity, Lifecycle, WorkManager, Room/SQLite e utilitários associados, Kotlin stdlib/coroutines, JetBrains annotations e Guava ListenableFuture. As licenças declaradas verificadas são Apache-2.0. O inventário completo por versão e SHA-256 está em docs/auditoria/DEPENDENCIAS.md e sbom-debug.cdx.json. Room é transitivo do WorkManager; o banco financeiro próprio usa SQLiteOpenHelper.

Material Icons são de terceiros (Google/colaboradores), disponibilizados sob Apache-2.0: https://developers.google.com/fonts/docs/material_icons . Kotlin: https://github.com/JetBrains/kotlin . AndroidX: https://android.googlesource.com/platform/frameworks/support/ . Guava: https://github.com/google/guava .

Os textos integrais de licença e avisos preservados acompanham o repositório em LICENSES. O app inclui cópias desses textos nos documentos locais de licenças. Gradle Wrapper e arquivos de bootstrap são componentes de terceiros; os avisos do Gradle 8.11.1 estão separados e não representam a autoria de Carlos Eduardo. SDK Android, JDK e distribuição completa do Gradle não são redistribuídos no pacote de fonte.

A fonte tipográfica é fornecida pelo sistema Android. Não há arquivo de fonte, fotografia ou logo de banco próprio de terceiros encontrado em app/src/main. A auditoria não é uma certificação mundial de originalidade ou de compatibilidade de qualquer futura dependência.
