from pathlib import Path
import json,hashlib,re
root=Path(__file__).resolve().parents[1];out=root/'docs/auditoria';out.mkdir(exist_ok=True,parents=True)
def rgb(h):return tuple(int(h[i:i+2],16)/255 for i in (0,2,4))
def lum(c):return sum(w*(v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4) for w,v in zip((.2126,.7152,.0722),c))
def ratio(f,b):x,y=sorted((lum(f),lum(b)));return (y+.05)/(x+.05)
main=(root/'app/src/main/java/com/talumi/financas/MainActivity.kt').read_text(encoding='utf-8');investment=(root/'app/src/main/java/com/talumi/financas/InvestmentUi.kt').read_text(encoding='utf-8')
pairs=[]
def add(label,fg,bg,minimum):
 r=ratio(rgb(fg),rgb(bg));pairs.append({'par':label,'texto_ou_grafico':fg,'fundo':bg,'razao':round(r,6),'minimo':minimum,'resultado':'APROVADO' if r>=minimum else 'FALHA'});assert r>=minimum,(label,r)
for theme,colors in [('Claro',{'texto':'171322','muted':'686176','fundo':'F8F7FC','superficie':'FFFFFF','variante':'F0ECF8','primary':'7047EB','onPrimary':'FFFFFF','container':'EDE7FF','onContainer':'3F248D','positivo':'0F765E','erro':'BA3550','outline':'827A91'}),('Escuro',{'texto':'F5F2FA','muted':'BAB1CD','fundo':'121019','superficie':'1E192A','variante':'2A2338','primary':'B69AFF','onPrimary':'24104E','container':'34224F','onContainer':'EDE7FF','positivo':'62D7B0','erro':'FF8FA3','outline':'8E839F'})]:
 for bg in ['fundo','superficie','variante']:
  for fg in ['texto','muted','positivo','erro']:add(f'{theme}: {fg}/{bg}',colors[fg],colors[bg],4.5)
 for key in ['texto','muted','fundo','superficie','variante','primary','onPrimary','container','onContainer','positivo','erro','outline']:
  token=colors[key]
  assert ('0xFF'+token in main) or (token=='FFFFFF' and 'Color.White' in main), ('Cor ausente no código',token)
 add(theme+': botão principal',colors['onPrimary'],colors['primary'],4.5)
 add(theme+': contêiner primário',colors['onContainer'],colors['container'],4.5)
 add(theme+': ícone primário em contêiner',colors['primary'],colors['container'],3)
 add(theme+': limite de campo',colors['outline'],colors['superficie'],3)
chart=re.search(r'private val chartColors=listOf\((.+)\)',investment).group(1)
for i,h in enumerate(re.findall(r'0xFF([A-Fa-f0-9]{6})',chart),1):
 for theme,bg in [('claro','FFFFFF'),('escuro','1E192A')]:add(f'Gráfico: fatia {i} / {theme}',h,bg,3)
# Sample the explicitly coded gradient and composite alpha text on every sample.
g1=rgb('6738E8');g2=rgb('8050D8')
for a in [1,.95,.9]:
 values=[]
 for i in range(1001):
  t=i/1000;b=tuple(x*(1-t)+y*t for x,y in zip(g1,g2));f=tuple(a+(1-a)*v for v in b);values.append(ratio(f,b))
 minimum=min(values);assert minimum>=4.5
 pairs.append({'par':f'Texto branco alfa {a} / gradiente (1001 amostras)','texto_ou_grafico':f'FFFFFF alfa {a}','fundo':'6738E8 → 8050D8','razao':round(minimum,6),'minimo':4.5,'resultado':'APROVADO'})
# Confirm source tokens exist, preventing a report disconnected from this revision.
assert '0xFF9A6500' in investment and 'containerColor=MaterialTheme.colorScheme.surface' in investment
assert 'modifier=Modifier.size(48.dp)) {Icon(if(visible)' in main
(out/'contraste.json').write_text(json.dumps({'data':'2026-10-22','metodo':'Luminância relativa sRGB WCAG; cores explícitas e 1001 amostras do gradiente; sem captura de tela','pares':pairs},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
md=['# Contraste — Talumi 1.3.0','', 'Medição matemática de 22/10/2026. Referência: WCAG 2.2, 1.4.3 e 1.4.11; aplicação a software nativo orientada por WCAG2ICT. Texto comum: 4,5:1; gráficos, ícones e limites de controles: 3:1. Não se arredonda um valor abaixo do mínimo para aprovar.','', 'Correção: âmbar do gráfico `#E7A53E` sobre branco = 2,132110:1, insuficiente para a referência de gráficos. Agora `#9A6500`, com fundo explícito de superfície nos dois temas. As demais fatias têm contraste ≥3:1 com esses fundos; divisórias de superfície separam cores adjacentes. Legenda e descrição contêm os valores sem depender somente das cores.','', '| Par | Cor | Fundo | Razão mínima | Referência | Resultado |','|---|---|---|---:|---:|---|']
md += [f"| {p['par']} | {p['texto_ou_grafico']} | {p['fundo']} | {p['razao']:.4f}:1 | {p['minimo']}:1 | {p['resultado']} |" for p in pairs]
md+=['','Escopo: pares explicitamente mapeados no código, estados ativos e gradiente amostrado. Não inclui todos os estados de foco/pressionamento/desabilitado, cores internas Material, transparências de toda a árvore, customizações do sistema ou renderização em aparelho. Não é certificação WCAG. Os elementos decorativos e o símbolo da marca não são usados como texto ou controle sem alternativa.','', 'Evidência de fonte: MainActivity.kt e InvestmentUi.kt. O relatório JSON preserva precisão adicional. Exige revisão após mudança de paleta.']
(out/'CONTRASTE.md').write_text('\n'.join(md)+'\n',encoding='utf-8')
# Resource inventory — exact files in this source tree.
files=[]
for path in sorted((root/'app/src/main').rglob('*')):
 if not path.is_file():continue
 if path.suffix.lower() in {'.png','.jpg','.jpeg','.webp','.gif','.svg','.ttf','.otf'} or (path.suffix=='.xml' and any(p.startswith(('drawable','mipmap')) for p in path.parts)):
  files.append({'arquivo':path.relative_to(root).as_posix(),'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'tipo':path.suffix})
(out/'imagens.json').write_text(json.dumps(files,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
rows=['# Imagens, ícones e tipografia','', 'Talumi 1.3.0 — 22/10/2026. Inspeção de arquivos app/src/main, referências no código e dependências. Busca local e inventário com SHA-256; não é busca reversa global nem laudo de autoria humana.','', '| Arquivo | Tipo e procedência | Licenciamento / condição | SHA-256 |','|---|---|---|---|']
for f in files:
 rows.append(f"| `{f['arquivo']}` | Vetor de chip ou composição adaptativa do projeto, desenvolvido nesta conversa com assistência de IA | Contribuições originais sob MIT; não prova exclusividade do conceito | `{f['sha256']}` |")
rows+=['','- `ChipMark`, MainActivity.kt: desenho procedural do símbolo do Talumi, sem raster externo; segue a mesma ressalva de procedência dos vetores.','- `InvestmentPie`, InvestmentUi.kt: gráfico procedural a partir dos valores locais, sem imagem externa.','- Ícones `Icons.*`: Material Icons/AndroidX, direitos de terceiros, Apache-2.0. As bibliotecas resolvidas estão no inventário de dependências. Não são desenhos exclusivos de Carlos Eduardo.','- Tipografia: fonte do sistema Android. Nenhum TTF/OTF ou download de fontes foi encontrado no aplicativo.','- Não foram encontrados PNG, JPG, GIF, WebP, retratos, fotografias ou logos de bancos dentro de app/src/main. O APK pode conter recursos internos de bibliotecas AndroidX; eles permanecem de terceiros e não são certificados como desenhos originais do projeto.','- A captura de referência enviada pelo usuário está fora do aplicativo; licença não comprovada. Excluída do pacote GitHub.','- O arquivo externo talumi-branding-conceito-01.png é uma exploração de branding com IA, conforme o histórico disponível. Não foi incluído no app nem no pacote público auditado; não se assume exclusividade ou liberação universal.','- Nome Talumi e conceito de chip não tiveram anterioridade de marca concluída no INPI. Antes de uso comercial, pesquisar conflitos e avaliar registro; não inserir símbolo ® sem registro.','', 'Decisão: publicar somente o código, os vetores do projeto com a ressalva de procedência e os componentes licenciados com atribuição. Não publicar a imagem de referência nem a exploração raster como se fossem direitos exclusivos comprovados.']
(out/'IMAGENS.md').write_text('\n'.join(rows)+'\n',encoding='utf-8')
print(len(pairs),'pares/cenários de contraste aprovados;',len(files),'recursos de imagem XML inventariados')
