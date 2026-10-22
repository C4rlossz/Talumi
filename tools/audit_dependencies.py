from pathlib import Path
import xml.etree.ElementTree as ET,hashlib,json,zipfile,io,urllib.request,os
root=Path(__file__).resolve().parents[1];out=root/'docs/auditoria';out.mkdir(exist_ok=True,parents=True)
licenses=root/'LICENSES';licenses.mkdir(exist_ok=True)
cache=Path(os.environ.get('GRADLE_USER_HOME',str(Path.home()/'.gradle')))/'caches/modules-2/files-2.1'
# Full upstream license text, included under its own redistribution terms.
with urllib.request.urlopen('https://www.apache.org/licenses/LICENSE-2.0.txt',timeout=20) as response: apache=response.read()
assert b'Version 2.0, January 2004' in apache
(licenses/'Apache-2.0.txt').write_bytes(apache)
notices={};components=[];rows=[]
ns={'m':'http://maven.apache.org/POM/4.0.0'}
def declared(g,a,v,seen=None):
 seen=set() if seen is None else seen
 if (g,a,v) in seen:return [],None
 seen.add((g,a,v));poms=list((cache/g/a/v).rglob('*.pom'))
 if not poms:return [],None
 p=poms[0];tree=ET.parse(p).getroot()
 found=[{'name':x.findtext('m:name','',ns),'url':x.findtext('m:url','',ns)} for x in tree.findall('m:licenses/m:license',ns)]
 if found:return found,str(p)
 parent=tree.find('m:parent',ns)
 if parent is not None:
  inherited,_=declared(parent.findtext('m:groupId','',ns),parent.findtext('m:artifactId','',ns),parent.findtext('m:version','',ns),seen)
  if inherited:return inherited,str(p)
 return [],str(p)
def embedded(data,coord):
 with zipfile.ZipFile(io.BytesIO(data)) as z:
  for n in z.namelist():
   if any(term in Path(n).name.upper() for term in ('LICENSE','NOTICE','COPYING')) and not n.endswith('/') and z.getinfo(n).file_size<150000:
    raw=z.read(n)
    if b'\x00' in raw:continue
    digest=hashlib.sha256(raw).hexdigest();entry=notices.setdefault(digest,{'content':raw,'origins':[]});entry['origins'].append(coord+' / '+n)
  if 'classes.jar' in z.namelist():embedded(z.read('classes.jar'),coord+' / classes.jar')
for line in (root/'build/audit/runtime-resolved.tsv').read_text(encoding='utf-8-sig').splitlines():
 if line.startswith('coordinate') or not line.strip():continue
 coord,artifact=line.split('\t');g,a,v=coord.split(':');path=Path(artifact);found,pom=declared(g,a,v)
 provenance='POM da versão resolvida (ou licença herdada do POM pai)'
 if not found and coord=='com.google.guava:listenablefuture:1.0':
  found=[{'name':'Apache License 2.0','url':'https://raw.githubusercontent.com/google/guava/v26.0/guava/src/com/google/common/util/concurrent/ListenableFuture.java'}];provenance='Cabeçalho Apache-2.0 do arquivo oficial ListenableFuture, tag v26.0; POM herda guava-parent 26.0-android'
 assert found and all('apache' in (x['name']+' '+x['url']).lower() for x in found), (coord,found)
 digest=hashlib.sha256(path.read_bytes()).hexdigest()
 components.append({'type':'library','group':g,'name':a,'version':v,'purl':f'pkg:maven/{g}/{a}@{v}','hashes':[{'alg':'SHA-256','content':digest}],'licenses':[{'license':{'id':'Apache-2.0'}}],'properties':[{'name':'talumi:scope','value':'debugRuntimeClasspath'},{'name':'talumi:license-evidence','value':provenance}],'externalReferences':[{'type':'license','url':found[0]['url']}]})
 rows.append(f'| `{coord}` | Apache-2.0 | `{digest}` |')
 embedded(path.read_bytes(),coord)
noticeIndex=[]
for i,(digest,entry) in enumerate(sorted(notices.items()),1):
 name=f'UPSTREAM-{i:02d}-{digest[:12]}.txt';(licenses/name).write_bytes(entry['content']);noticeIndex.append('## '+name+'\n\n'+'\n'.join('- '+origin for origin in entry['origins']))
(licenses/'ORIGENS.md').write_text('# Textos preservados dos artefatos\n\n'+'\n\n'.join(noticeIndex)+'\n',encoding='utf-8')
(root/'docs/auditoria/DEPENDENCIAS.md').write_text('# Inventário resolvido de dependências\n\nTalumi 1.3.0 — 22/10/2026. Configuração `debugRuntimeClasspath`, '+str(len(components))+' artefatos diretos e transitivos, incluindo ferramentas de inspeção do APK debug. Evidência: resolução Gradle, POMs das versões resolvidas e exceção de procedência documentada para ListenableFuture. Não é uma análise de CVEs nem uma lista de todas as dependências de build/teste.\n\nOs textos completos estão em LICENSES e avisos extraídos dos artefatos foram preservados. Hashes identificam os AAR/JAR resolvidos, não garantem ausência de falhas. Refaça a resolução e a revisão ao alterar dependências ou produzir variante release.\n\n| Coordenada | Licença declarada | SHA-256 do artefato |\n|---|---|---|\n'+'\n'.join(rows)+'\n',encoding='utf-8')
(out/'sbom-debug.cdx.json').write_text(json.dumps({'bomFormat':'CycloneDX','specVersion':'1.5','version':1,'metadata':{'component':{'type':'application','name':'Talumi','version':'1.3.0'},'properties':[{'name':'scope','value':'Gradle debugRuntimeClasspath; sem grafo detalhado, sem build/teste'}]},'components':components},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
summary='''# Licenças e créditos de terceiros

Talumi 1.3.0 — 22/10/2026.

As contribuições originais de Carlos Eduardo usam MIT. Essa licença não altera as licenças nem atribui ao autor a propriedade de componentes de terceiros.

O APK debug resolve 76 artefatos de runtime: famílias AndroidX/Jetpack Compose, Material, Activity, Lifecycle, WorkManager, Room/SQLite e utilitários associados, Kotlin stdlib/coroutines, JetBrains annotations e Guava ListenableFuture. As licenças declaradas verificadas são Apache-2.0. O inventário completo por versão e SHA-256 está em docs/auditoria/DEPENDENCIAS.md e sbom-debug.cdx.json. Room é transitivo do WorkManager; o banco financeiro próprio usa SQLiteOpenHelper.

Material Icons são de terceiros (Google/colaboradores), disponibilizados sob Apache-2.0: https://developers.google.com/fonts/docs/material_icons . Kotlin: https://github.com/JetBrains/kotlin . AndroidX: https://android.googlesource.com/platform/frameworks/support/ . Guava: https://github.com/google/guava .

Os textos integrais de licença e avisos preservados acompanham o repositório em LICENSES. O app inclui cópias desses textos nos documentos locais de licenças. Gradle Wrapper e arquivos de bootstrap são componentes de terceiros; os avisos do Gradle 8.11.1 estão separados e não representam a autoria de Carlos Eduardo. SDK Android, JDK e distribuição completa do Gradle não são redistribuídos no pacote de fonte.

A fonte tipográfica é fornecida pelo sistema Android. Não há arquivo de fonte, fotografia ou logo de banco próprio de terceiros encontrado em app/src/main. A auditoria não é uma certificação mundial de originalidade ou de compatibilidade de qualquer futura dependência.
'''
summary=summary.replace('76 artefatos',str(len(components))+' artefatos')
(root/'THIRD_PARTY_NOTICES.md').write_text(summary,encoding='utf-8')
assets=root/'app/src/main/assets/legal'
# Self-contained on-device notice file, without external HTML or WebView.
full=summary+'\n\n'+ '\n\n'.join('## '+p.name+'\n\n'+p.read_text(encoding='utf-8',errors='replace') for p in sorted(licenses.glob('*.txt')))
(assets/'THIRD_PARTY_NOTICES.md').write_text(full,encoding='utf-8')
print(len(components),'artefatos verificados;',len(notices),'textos de licença/aviso preservados')
