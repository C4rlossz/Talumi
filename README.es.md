<p align="center"><img src="docs/assets/talumi-cover.svg" alt="Talumi: finanzas e inversiones. Proyecto Android de Carlos Eduardo." width="100%" /></p>
<p align="center"><strong><a href="README.md">Português</a> · <a href="README.en.md">English</a> · <a href="README.es.md">Español</a></strong></p>
<h1 align="center">Talumi</h1>
<p align="center"><strong>Tu dinero, con claridad.</strong><br/>Finanzas personales e inversiones en una aplicación Android con datos locales.</p>
<p align="center"><strong>Android 8+</strong> · <strong>Kotlin</strong> · <strong>Jetpack Compose</strong> · <strong>SQLite</strong> · <strong>MIT</strong></p>
<p align="center"><a href="#features">Funciones</a> · <a href="#guide">Guía de uso</a> · <a href="#development">Compilar</a> · <a href="#audit">Auditoría</a> · <a href="#license">Licencia y créditos</a></p>

## Novedades de la versión 1.4.0

- **Relatórios**: anillo animado, resultado mensual en el centro, selección de mes y gastos por categoría. Calcula ingresos menos gastos confirmados hasta hoy; transferencias e inversiones se mantienen separadas.
- **Información** junto a configuración: tecnologías, datos locales y enlaces al GitHub y LinkedIn de Carlos Eduardo.
- [Guía de actualización e instalación (portugués)](docs/ATUALIZACAO-1.4.0.md). La auditoría 1.3.0 que sigue es histórica; consulta [la validación de esta versión](VALIDACAO.md).

## El proyecto

**Talumi** es un organizador de finanzas personales ideado y dirigido por **[Carlos Eduardo](https://github.com/C4rlossz)** como proyecto de portafolio. Reúne ingresos, gastos, salarios, compromisos e inversiones, manteniendo separado el dinero disponible del patrimonio invertido.

Su identidad combina violeta, lavanda y un símbolo inspirado en el chip de una tarjeta, con temas claro y oscuro. La portada es una ilustración, no una captura de la aplicación. La interfaz actual está en portugués brasileño; la documentación también está disponible en inglés y español.

<a id="features"></a>
## ✨ Qué puedes organizar

| Área | Funciones |
|---|---|
| **Finanzas** | Cuentas y saldos bancarios manuales, efectivo, reservas, huchas, ingresos, gastos y transferencias entre cuentas propias. |
| **Agenda y salario** | Cobros y pagos programados; salario automático en un día fijo o el quinto día hábil; sábados y festivos configurables. |
| **Personas** | Dinero que te deben, deudas propias, vencimientos y liquidaciones parciales o totales vinculadas a una cuenta. |
| **Inversiones** | Cartera independiente, gráfico circular por banco, CDB, Tesouro Selic, LCI/LCA, fondos y otras inversiones; aportaciones, rescates, rendimientos y pérdidas. |
| **Datos y privacidad** | Copias y restauración JSON, eliminación confirmada, documentos sin conexión y formularios locales de privacidad y accesibilidad. |

### Dónde se guardan los datos

Los registros financieros se guardan en el **SQLite privado de la aplicación**, en el dispositivo. Esta versión no incluye cuenta de usuario, anuncios, analítica, conexión bancaria ni un backend Firebase configurado. El usuario introduce los saldos; la aplicación no mueve dinero real.

La consulta opcional de **CDI y Selic** utiliza la API pública del Banco Central de Brasil. Al abrir Inversiones, solicita tasas si no hay una consulta guardada de ese día; **Atualizar CDI e Selic** permite repetirla. Si falla, conserva la última tasa y su fecha. Solicita parámetros públicos de las series sin enviar tu cartera; el servicio puede recibir la IP y metadatos habituales de conexión. Puedes desactivar nuevas consultas en privacidad.

<a id="guide"></a>
## 🧭 Guía de uso

Los nombres de los controles se conservan en portugués para que puedas encontrarlos en la interfaz actual.

1. **Añade tus cuentas.** En **Finanças → Contas**, introduce banco, cartera de efectivo, reserva o hucha y su saldo inicial. Ejemplo: `1250,50`, sin separadores de miles. Se permiten saldos iniciales negativos.
2. **Registra movimientos.** Selecciona la cuenta del ingreso o gasto. Las transferencias entre cuentas propias no son ingresos ni gastos. Evita duplicar movimientos incluidos en el saldo inicial.
3. **Configura tu salario.** En **Agenda → Cadastrar salário recorrente**, elige importe, cuenta, fecha de inicio y regla de cobro. La fecha determina los meses incluidos; no contabilices de nuevo salarios ya presentes en el saldo inicial. Sábados y festivos se configuran manualmente.
4. **Organiza compromisos.** Usa Agenda para movimientos programados y **Pessoas → Registrar acerto** para pagos o cobros relacionados con deudas. Registrar una deuda, por sí solo, no modifica el saldo.
5. **Separa las inversiones.** Elige **Investimentos** arriba. Para una inversión existente, utiliza **Valor já investido**. Para invertir desde Finanzas, crea una inversión con cero y selecciona **Aportar**. **Resgatar** devuelve dinero a una cuenta y respeta el bloqueo de vencimiento indicado.
6. **Comprueba rendimientos.** Introduce producto y tasa del contrato. Las previsiones son **brutas**, con saldo y tasas constantes, sin impuestos, costes ni variaciones del precio de mercado. No se abonan automáticamente. Registra los rendimientos reales según tu extracto.
7. **Haz copias.** En ajustes, usa **Exportar backup local** y selecciona dónde guardar el JSON. Para importarlo, utiliza **Restaurar um backup**. La restauración sustituye los registros actuales. La aplicación no cifra el archivo exportado.

Las actualizaciones con la misma identificación y clave de firma conservan la base local. Desinstalar o borrar el almacenamiento elimina los datos: exporta una copia antes. Las copias exportadas son manuales y no se actualizan solas. Los destinos en la nube dependen del proveedor seleccionado.

Android puede retrasar las tareas de salario en segundo plano; las ocurrencias vencidas se reconcilian al abrir la aplicación. Las previsiones de inversión parten de la fecha de referencia del saldo, por lo que el mes actual puede cubrir solo el período restante. La [guía completa en portugués](docs/GUIA-COMPLETO.md) explica calendario y cálculos.

## 🛠 Tecnologías

| Tecnología | Función en Talumi |
|---|---|
| **Kotlin 2.1.20** | Implementación Android, modelos y reglas financieras. |
| **Jetpack Compose / Material 3** | Interfaz declarativa, temas, formularios y gráficos en Canvas. |
| **SQLite / SQLiteOpenHelper** | Persistencia financiera local, transacciones y migraciones. |
| **ViewModel, StateFlow y coroutines** | Estado de la interfaz y operaciones de disco fuera del hilo principal. |
| **WorkManager** | Procesamiento periódico local de salarios recurrentes. |
| **HTTPS / API SGS del Banco Central** | Tasas públicas de CDI y Selic con caché local. |
| **JUnit, Robolectric y pruebas Compose** | Reglas de cálculo, persistencia y flujos de interfaz. |
| **Android Lint / Gradle Kotlin DSL** | Análisis estático y configuración de compilación. |

La base financiera utiliza SQLiteOpenHelper. Room es una dependencia transitiva de WorkManager. El proyecto no necesita credenciales de nube para funcionar.

```text
app/src/main/java/com/talumi/financas/
├── MainActivity.kt       # Navegación e identidad visual
├── Forms.kt              # Formularios y confirmaciones
├── Models.kt             # Importes, saldos y calendario
├── FinanceDb.kt          # SQLite, migraciones y copias
├── FinanceViewModel.kt   # Estado y operaciones serializadas
├── Investments.kt       # Cartera y estimaciones
├── InvestmentUi.kt      # Interfaz y gráfico de cartera
├── MonthlyReport.kt     # Monthly report calculations
├── ReportUi.kt          # Animated monthly report
├── AboutUi.kt           # App and author information
├── MarketRates.kt        # Consulta pública de CDI / Selic
├── SalaryWorker.kt       # Recurrencias locales
└── LegalUi.kt            # Privacidad y formularios locales
```

<a id="development"></a>
## 🚀 Compilar y ejecutar

**Requisitos:** Android Studio, JDK 17 y SDK Android 35. Versión mínima: Android 8.0/API 26; target API 35. Gradle Wrapper 8.11.1. La primera sincronización descarga dependencias.

```sh
git clone https://github.com/C4rlossz/Talumi.git
cd Talumi
```

Abre la carpeta en Android Studio, espera la sincronización y ejecuta en un dispositivo o emulador. Para generar un APK de prueba:

**Windows — PowerShell**

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
```

**Linux / macOS**

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
```

Salida: `app/build/outputs/apk/debug/app-debug.apk`.

La clave privada de prueba no se publica. Otro entorno puede generar una firma debug diferente y no podrá necesariamente actualizar una instalación anterior; consulta las [notas de firma](development/README.md). Producción requiere configuración y clave propias. Mantén fuera de Git las claves, bases, copias y `local.properties`.

<a id="audit"></a>
## 🔎 Auditoría y documentación

La versión **1.3.0** recibió una revisión técnica y documental el **22 de octubre de 2026**, solicitada por Carlos Eduardo y realizada con asistencia de IA. Estas cifras corresponden a esa validación registrada, no a una ejecución continua de CI.

| Evidencia registrada | Resultado |
|---|---|
| Pruebas automatizadas | **61 aprobadas**, sin fallos ni pruebas omitidas. |
| Android Lint | **0 errores**; 6 avisos de versiones más recientes de dependencias. |
| Contraste | **47 pares/escenarios explícitos medidos y aprobados**, con correcciones en la paleta. |
| Runtime del APK debug | **76 artefactos** inventariados con versiones, licencias y hashes. |
| Recursos visuales propios de la aplicación | **3 XML de icono/composición** inventariados, además de dibujos procedurales documentados. |

La revisión abarca privacidad, términos, cookies/almacenamiento, accesibilidad, autoría, imágenes, terceros y riesgos de publicación. **No es certificación integral LGPD/WCAG, dictamen jurídico ni garantía de ausencia de vulnerabilidades.** Las pruebas físicas con tecnologías de asistencia, los procedimientos del correo de contacto y la preparación para producción siguen como pendientes documentados.

| Documento | Contenido |
|---|---|
| [Informe de auditoría](docs/auditoria/RELATORIO.md) | Alcance, correcciones, evidencias, riesgos y límites. |
| [Validación ejecutada](VALIDACAO.md) | Pruebas, compilación, firma y controles del paquete. |
| [Privacidad](docs/PRIVACIDADE.md) · [Términos](docs/TERMOS.md) · [Cookies](docs/COOKIES.md) | Datos locales, conexiones públicas, archivos y condiciones de uso. |
| [Accesibilidad](docs/ACESSIBILIDADE.md) · [Contraste](docs/auditoria/CONTRASTE.md) | Medidas, cálculos y comprobaciones manuales pendientes. |
| [Imágenes](docs/auditoria/IMAGENS.md) · [Autoría](docs/AUTORIA.md) | Procedencia, atribución y límites de exclusividad. |
| [Dependencias](docs/auditoria/DEPENDENCIAS.md) · [SBOM](docs/auditoria/sbom-debug.cdx.json) | Componentes del runtime debug y evidencias. |
| [Herramientas de auditoría](tools/README.md) | Cómo reproducir las comprobaciones disponibles. |

Los documentos detallados y avisos de la aplicación están actualmente en portugués. En la aplicación, abre **Configurações → Privacidade, acessibilidade e licenças** para leerlos sin conexión, generar formularios locales, controlar consultas y confirmar la eliminación. Los formularios crean un TXT que tú decides revisar y enviar; no transmiten mensajes automáticamente.

## 🤝 Contribuir

Las sugerencias, correcciones y mejoras son bienvenidas. Lee [CONTRIBUTING.md](CONTRIBUTING.md), abre una issue para conversar sobre un cambio y envía un pull request con descripción y validación. Usa datos ficticios en ejemplos y capturas. Para vulnerabilidades o datos personales, utiliza el canal privado de [SECURITY.md](SECURITY.md).

<a id="license"></a>
## 📜 Licencia y créditos

**Copyright © 2026 Carlos Eduardo.** Las contribuciones originales de Talumi se distribuyen bajo la **[licencia MIT](LICENSE)**.

Puedes usar, estudiar, modificar, distribuir y utilizar comercialmente el proyecto, **conservando el aviso de copyright de Carlos Eduardo y una copia de la licencia MIT** en las copias o partes sustanciales del software. Conserva también los avisos aplicables a terceros. La licencia no exige publicar tus modificaciones.

AndroidX, Kotlin, Material Icons y otros componentes conservan sus respectivos titulares y licencias; consulta [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) y [LICENSES](LICENSES). El desarrollo contó con asistencia de IA, según la documentación de autoría.

**Responsable y director del proyecto:** [Carlos Eduardo — C4rlossz](https://github.com/C4rlossz)  
**Contacto público:** [Carlosz.eduardo1402@gmail.com](mailto:Carlosz.eduardo1402@gmail.com)

<p align="center"><strong>Talumi · Tu dinero, con claridad.</strong></p>
