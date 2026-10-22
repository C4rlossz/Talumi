# Imagens, ícones e tipografia

Talumi 1.3.0 — 22/10/2026. Inspeção de arquivos app/src/main, referências no código e dependências. Busca local e inventário com SHA-256; não é busca reversa global nem laudo de autoria humana.

| Arquivo | Tipo e procedência | Licenciamento / condição | SHA-256 |
|---|---|---|---|
| `app/src/main/res/drawable/chip.xml` | Vetor de chip ou composição adaptativa do projeto, desenvolvido nesta conversa com assistência de IA | Contribuições originais sob MIT; não prova exclusividade do conceito | `81569bad69f04cc9bc24cddde8073f72fca078e0dfb8ef1df80754837ad03355` |
| `app/src/main/res/drawable/chip_mono.xml` | Vetor de chip ou composição adaptativa do projeto, desenvolvido nesta conversa com assistência de IA | Contribuições originais sob MIT; não prova exclusividade do conceito | `84495d52802ef853a9ca170e7874e35afff85e62480c0358744b15f9fc7c9685` |
| `app/src/main/res/mipmap-anydpi/ic_launcher.xml` | Vetor de chip ou composição adaptativa do projeto, desenvolvido nesta conversa com assistência de IA | Contribuições originais sob MIT; não prova exclusividade do conceito | `23656dc5c8e2e4c0992036d7b5a31741f4f9e2ea8710ef4691d30d4f296a4d2a` |

- `ChipMark`, MainActivity.kt: desenho procedural do símbolo do Talumi, sem raster externo; segue a mesma ressalva de procedência dos vetores.
- `InvestmentPie`, InvestmentUi.kt: gráfico procedural a partir dos valores locais, sem imagem externa.
- Ícones `Icons.*`: Material Icons/AndroidX, direitos de terceiros, Apache-2.0. As bibliotecas resolvidas estão no inventário de dependências. Não são desenhos exclusivos de Carlos Eduardo.
- Tipografia: fonte do sistema Android. Nenhum TTF/OTF ou download de fontes foi encontrado no aplicativo.
- Não foram encontrados PNG, JPG, GIF, WebP, retratos, fotografias ou logos de bancos dentro de app/src/main. O APK pode conter recursos internos de bibliotecas AndroidX; eles permanecem de terceiros e não são certificados como desenhos originais do projeto.
- A captura de referência enviada pelo usuário está fora do aplicativo; licença não comprovada. Excluída do pacote GitHub.
- O arquivo externo talumi-branding-conceito-01.png é uma exploração de branding com IA, conforme o histórico disponível. Não foi incluído no app nem no pacote público auditado; não se assume exclusividade ou liberação universal.
- Nome Talumi e conceito de chip não tiveram anterioridade de marca concluída no INPI. Antes de uso comercial, pesquisar conflitos e avaliar registro; não inserir símbolo ® sem registro.

Decisão: publicar somente o código, os vetores do projeto com a ressalva de procedência e os componentes licenciados com atribuição. Não publicar a imagem de referência nem a exploração raster como se fossem direitos exclusivos comprovados.

## Capa da documentação do GitHub

`docs/assets/talumi-cover.svg`: ilustração vetorial criada nesta etapa com assistência de IA, usando a identidade e o símbolo do Talumi, sem fotografias ou recursos remotos. É uma capa decorativa, não uma captura do aplicativo. Fontes do sistema são apenas referenciadas; nenhum arquivo de fonte é redistribuído. Contribuições originais sob MIT, com as mesmas ressalvas de procedência e exclusividade deste relatório. Não integra os três XML do aplicativo inventariados na auditoria 1.3.0.

SHA-256: `606c7efd76737874b8619d5c7e0b9cf5bf42c4b42a9b7ee4f36da53f0f223433`.
