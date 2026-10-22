<p align="center"><img src="docs/assets/talumi-cover.svg" alt="Talumi: personal finance and investments. An Android project by Carlos Eduardo." width="100%" /></p>
<p align="center"><strong><a href="README.md">Português</a> · <a href="README.en.md">English</a> · <a href="README.es.md">Español</a></strong></p>
<h1 align="center">Talumi</h1>
<p align="center"><strong>Your money, with clarity.</strong><br/>Personal finance and investments in an Android app with local data storage.</p>
<p align="center"><strong>Android 8+</strong> · <strong>Kotlin</strong> · <strong>Jetpack Compose</strong> · <strong>SQLite</strong> · <strong>MIT</strong></p>
<p align="center"><a href="#features">Features</a> · <a href="#guide">User guide</a> · <a href="#development">Build and run</a> · <a href="#audit">Audit</a> · <a href="#license">License and credits</a></p>

## What is new in 1.4.0

- **Relatórios**: animated ring, monthly income minus expenses in the center, month selection and expense categories. Only confirmed finance transactions through today count; transfers and investments remain separate.
- **Information** beside settings: technologies, local data and Carlos Eduardo’s GitHub and LinkedIn links.
- [Update and installation guide (Portuguese)](docs/ATUALIZACAO-1.4.0.md). The 1.3.0 audit below is historical; see [current build validation](VALIDACAO.md).

## The project

**Talumi** is a personal finance organizer conceived and directed by **[Carlos Eduardo](https://github.com/C4rlossz)** as a portfolio project. It brings together income, expenses, salaries, debts and investments, keeping spendable money separate from invested assets.

Its visual identity combines violet, lavender and a card-chip-inspired symbol, with light and dark themes. The cover above is an illustration, not an app screenshot. The current app interface is in Brazilian Portuguese; documentation is also available in English and Spanish.

<a id="features"></a>
## ✨ What you can manage

| Area | Features |
|---|---|
| **Finances** | Manually entered bank accounts and balances, cash, savings, money jars, income, expenses and transfers between your own accounts. |
| **Calendar and salary** | Scheduled payments and income; automatic salary entries on a fixed day or the fifth business day; configurable Saturdays and holidays. |
| **People** | Money owed to you, your debts, due dates and partial or full settlements linked to an account. |
| **Investments** | Separate portfolio, allocation pie chart by bank, CDBs, Tesouro Selic, LCI/LCA, funds and other holdings; contributions, withdrawals, gains and losses. |
| **Data and privacy** | JSON backup and restore, confirmed deletion, offline notices and local privacy-rights/accessibility forms. |

### Where your data stays

Financial records are stored in the app's **private SQLite database on the device**. This version has no user account, ads, analytics, bank connection or configured Firebase backend. Users enter balances themselves; the app does not move real money.

Optional **CDI and Selic** updates use the Brazilian Central Bank's public API. Opening Investments requests rates when there is no saved lookup from that day; **Atualizar CDI e Selic** requests another update. If it fails, the previous rate and reference date remain available. Requests use public series parameters without portfolio data; the service can receive the IP address and normal connection metadata. New online lookups can be disabled in privacy settings.

<a id="guide"></a>
## 🧭 User guide

Control names below remain in Portuguese so you can match them to the current interface.

1. **Add accounts.** Under **Finanças → Contas**, enter a bank, cash wallet, savings account or jar with an opening balance. Use `1250,50` without thousands separators. Negative opening balances are supported.
2. **Record transactions.** Select the account receiving or spending the money. Transfers between your own accounts are not income or expenses. Avoid duplicating transactions already included in the opening balance.
3. **Set up your salary.** Under **Agenda → Cadastrar salário recorrente**, choose the amount, account, start date and payment rule. The start date determines which months are included; do not add salaries already reflected in the opening balance. Saturdays and holidays are configured manually.
4. **Track commitments.** Use Agenda for scheduled entries and **Pessoas → Registrar acerto** for debt-related payments or receipts. Creating a debt record alone does not change an account balance.
5. **Separate investments.** Choose **Investimentos** at the top. Use **Valor já investido** for an existing holding. To invest from a finance account, create the holding with zero and select **Aportar**. **Resgatar** transfers money back and respects the recorded maturity restriction.
6. **Check returns.** Enter the product and contractual rate. Forecasts are **gross estimates** with constant balances and rates, excluding taxes, fees and market-price changes. They are not automatically credited. Record actual gains from your statement.
7. **Make backups.** In settings, choose **Exportar backup local** and select a location for the JSON file. To import, use **Restaurar um backup**. Restoring replaces current records. The app does not encrypt exported files.

Updates using the same application identity and signing key preserve local data. Uninstalling the app or clearing its storage deletes that data, so export a copy first. Exported backups are manual snapshots and do not update themselves. Cloud destinations depend on your chosen file provider.

Android may delay background salary processing; overdue occurrences are reconciled when the app opens. Investment projections start after the holding's balance-reference date, so the current month may cover only the remaining period. The detailed [Portuguese guide](docs/GUIA-COMPLETO.md) explains calendar and calculation rules.

## 🛠 Technology

| Technology | Role in Talumi |
|---|---|
| **Kotlin 2.1.20** | Android implementation, models and financial rules. |
| **Jetpack Compose / Material 3** | Declarative UI, themes, forms and Canvas charts. |
| **SQLite / SQLiteOpenHelper** | Local financial persistence, transactions and migrations. |
| **ViewModel, StateFlow and coroutines** | UI state and disk operations outside the main thread. |
| **WorkManager** | Periodic local processing of recurring salary entries. |
| **HTTPS / Central Bank SGS API** | Public CDI and Selic reference rates with a local cache. |
| **JUnit, Robolectric and Compose tests** | Calculation rules, persistence and UI flows. |
| **Android Lint / Gradle Kotlin DSL** | Static analysis and build configuration. |

The financial database uses SQLiteOpenHelper. Room is a transitive WorkManager dependency. No cloud credentials are required to use the project.

```text
app/src/main/java/com/talumi/financas/
├── MainActivity.kt       # Navigation and visual identity
├── Forms.kt              # Forms and confirmations
├── Models.kt             # Money, ledger and calendar
├── FinanceDb.kt          # SQLite, migrations and backups
├── FinanceViewModel.kt   # State and serialized operations
├── Investments.kt       # Portfolio and estimates
├── InvestmentUi.kt      # Portfolio UI and chart
├── MonthlyReport.kt     # Monthly report calculations
├── ReportUi.kt          # Animated monthly report
├── AboutUi.kt           # App and author information
├── MarketRates.kt        # Public CDI / Selic lookups
├── SalaryWorker.kt       # Local recurring entries
└── LegalUi.kt            # Privacy and local forms
```

<a id="development"></a>
## 🚀 Build and run

**Prerequisites:** Android Studio, JDK 17 and Android SDK 35. Minimum Android version: 8.0/API 26; target API 35. Gradle Wrapper 8.11.1. The first sync downloads dependencies.

```sh
git clone https://github.com/C4rlossz/Talumi.git
cd Talumi
```

Open the folder in Android Studio, let Gradle sync and run on a device or emulator. To build a test APK:

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

Output: `app/build/outputs/apk/debug/app-debug.apk`.

The private development key is not published. A different environment may generate a different debug signature and cannot necessarily update an existing installation; see the [signing notes](development/README.md). Production distribution requires its own configuration and key. Keep keystores, databases, backups and `local.properties` out of Git.

<a id="audit"></a>
## 🔎 Audit and documentation

Version **1.3.0** received a technical and documentary review on **October 22, 2026**, commissioned by Carlos Eduardo and performed with AI assistance. These figures describe that recorded validation, not a live CI run.

| Recorded evidence | Result |
|---|---|
| Automated tests | **61 passed**, with no failures or skipped tests. |
| Android Lint | **0 errors**; 6 dependency-update warnings. |
| Contrast | **47 explicit color pairs/scenarios measured and passed**, with palette corrections. |
| Debug APK runtime | **76 artifacts** inventoried with versions, licenses and hashes. |
| App-owned visual resources | **3 icon/composition XML files** inventoried, plus documented procedural drawings. |

The review covers privacy, terms, cookies/storage, accessibility, authorship, images, third-party notices and publication risks. **It is not full LGPD/WCAG certification, legal advice or a guarantee that there are no vulnerabilities.** Physical-device assistive-technology checks, contact-mailbox procedures and production readiness remain documented follow-up items.

| Document | Content |
|---|---|
| [Audit report](docs/auditoria/RELATORIO.md) | Scope, corrections, evidence, risks and limitations. |
| [Recorded validation](VALIDACAO.md) | Tests, build, signing and package checks. |
| [Privacy](docs/PRIVACIDADE.md) · [Terms](docs/TERMOS.md) · [Cookies](docs/COOKIES.md) | Local processing, public connections, files and use conditions. |
| [Accessibility](docs/ACESSIBILIDADE.md) · [Contrast](docs/auditoria/CONTRASTE.md) | Measures, calculations and pending manual checks. |
| [Images](docs/auditoria/IMAGENS.md) · [Authorship](docs/AUTORIA.md) | Provenance, attribution and exclusivity limits. |
| [Dependencies](docs/auditoria/DEPENDENCIAS.md) · [SBOM](docs/auditoria/sbom-debug.cdx.json) | Debug runtime components and evidence. |
| [Audit tools](tools/README.md) | Reproducing the available checks. |

Detailed audit records and in-app notices are currently in Portuguese. In the app, open **Configurações → Privacidade, acessibilidade e licenças** for offline documents, local forms, lookup controls and confirmed deletion. Forms create TXT files for you to review and send if you choose; they do not send messages automatically.

## 🤝 Contributing

Suggestions, fixes and improvements are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md), open an issue to discuss a change and submit a pull request describing the implementation and validation. Use fictional data in examples and screenshots. Report vulnerabilities or personal-data concerns through the private channel in [SECURITY.md](SECURITY.md).

<a id="license"></a>
## 📜 License and credits

**Copyright © 2026 Carlos Eduardo.** Original Talumi contributions are released under the **[MIT License](LICENSE)**.

You may use, study, modify, distribute and commercially use the project, **provided that Carlos Eduardo's copyright notice and a copy of the MIT License are retained** in copies or substantial portions of the software. Keep applicable third-party notices as well. The license does not require you to publish your changes.

AndroidX, Kotlin, Material Icons and other components retain their own authors and licenses; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and [LICENSES](LICENSES). The project was developed with AI assistance, as documented in the authorship records.

**Project owner and lead:** [Carlos Eduardo — C4rlossz](https://github.com/C4rlossz)  
**Public contact:** [Carlosz.eduardo1402@gmail.com](mailto:Carlosz.eduardo1402@gmail.com)

<p align="center"><strong>Talumi · Your money, with clarity.</strong></p>
