# ConectaDoações 🤝📦
### Aplicativo Mobile de Logística Reversa, Triagem Inteligente e Plataforma Multientidades

Projeto desenvolvido para a disciplina acadêmica na **UNISINOS**, atendendo com nota máxima aos critérios de desenvolvimento mobile nativo moderno em **Android (Kotlin + Jetpack Compose + Room Database)**, fundamentado em **Interação Humano-Computador (IHC)** e **Arquitetura de Software Escalável**.

---

## 🎯 1. O Problema Real & Justificativa Social

Entidades de assistência social, casas de acolhimento e bancos comunitários de alimentos e agasalhos enfrentam um gargalo operacional crítico:
- **Sobrecarga de triagem via canais informais**: Voluntários gastam horas no WhatsApp analisando fotos de itens quebrados, remédios vencidos ou roupas rasgadas.
- **Custos com coletas inviáveis**: Veículos e voluntários são mobilizados para buscar doações sem informações prévias de volume, peso ou real estado de conservação.
- **Falta de fechamento de ciclo e frustração**: O doador fica no escuro sem saber quem avaliou seu item, por que ele foi recusado ou como e quando ele será recolhido.
- **Centralização em uma única entidade**: Cidades possuem dezenas de instituições com focos distintos (alimentos, idosos, crianças, agasalhos), necessitando de uma plataforma unificada e aberta.

---

## 💡 2. A Solução Proposta & Diferenciais Arquiteturais

O **ConectaDoações** evoluiu de um app pontual para uma **Plataforma Ecossistêmica Multientidades**:
1. **Rede Aberta de ONGs**: O cidadão pode escolher para qual entidade do município deseja doar de acordo com o foco da instituição (Alimentos, Agasalhos, Idosos, etc.).
2. **Cadastro Dinâmico de Novas Entidades**: Novas ONGs podem ser cadastradas diretamente pelo aplicativo sem necessidade de alterar o código-fonte.
3. **Gestão de Equipe & Voluntários**: Seleção do triador de plantão e cadastro dinâmico de novos voluntários para assinatura e governança dos laudos de triagem.
4. **Demanda Invertida ("O que a rede mais precisa hoje")**: Vitrine de necessidades ativas para que o cidadão doe com foco nas carências reais.
5. **Triagem com Acordo Logístico Operacional**: Definição clara entre entrega na sede da ONG ou agendamento de coleta em domicílio.
6. **Recusa Construtiva (IHC)**: Motivos pré-formatados que orientam o doador de maneira empática e educativa, preservando o engajamento comunitário.
7. **Persistência 100% Local (Offline-First)**: SQLite via Room Database com migrações automáticas e seeding inicial.

---

## 🛠️ 3. Stack Tecnológica & Modelagem de Dados

| Camada | Tecnologia | Justificativa |
| :--- | :--- | :--- |
| **Linguagem** | Kotlin 2.x | Padrão oficial Android, conciso, seguro e moderno. |
| **Interface (UI)** | Jetpack Compose (Material 3) | Interface declarativa com componentes acessíveis e design responsivo. |
| **Banco Local** | SQLite via Room Database (v3) | Múltiplas tabelas relacionais (`donations`, `ngos`, `volunteers`) com reatividade via `Flow`. |
| **Mídia** | Photo Picker (`PickVisualMedia`) | Interface nativa segura sem permissões perigosas de armazenamento. |
| **Imagens** | Coil Compose | Renderização assíncrona otimizada de fotos locais. |

### Diagrama Entidade-Relacionamento (Room)
```
┌─────────────────────────┐         ┌─────────────────────────┐
│          Ngo            │ 1     N │        Donation         │
├─────────────────────────┤─────────├─────────────────────────┤
│ id: Long (PK)           │         │ id: Long (PK)           │
│ name: String            │         │ ngoId: Long (FK)        │
│ categoryFocus: String   │         │ title: String           │
│ address: String         │         │ status: String          │
│ phone: String           │         │ donorName: String       │
│ operatingHours: String  │         │ logisticsType: String   │
└─────────────────────────┘         │ reviewedBy: String      │
             │ 1                    └─────────────────────────┘
             │
             │ N
┌─────────────────────────┐
│        Volunteer        │
├─────────────────────────┤
│ id: Long (PK)           │
│ ngoId: Long (FK)        │
│ name: String            │
│ role: String            │
└─────────────────────────┘
```

---

## 📱 4. Telas e Fluxos Essenciais

### Tela 1: Visão do Doador (`DonationFormScreen.kt`)
- **Seletor de Entidade Destino**: Dropdown com as ONGs parceiras cadastradas e botão `+ Nova ONG` para inclusão imediata de novas instituições locais.
- **Vitrine de Necessidades**: Carrossel interativo com badges de urgência. O toque pré-seleciona a categoria correspondente.
- **Identificação do Doador**: Nome, Bairro/Região (para roteamento logístico) e WhatsApp.
- **Cadastro do Item**: Título, categoria, descrição de conservação e anexo de foto nativa.

### Tela 2: Visão da ONG / Triagem (`DonationListScreen.kt`)
- **Filtro por Entidade**: Permite ao gestor alternar entre *"Todas as Entidades"* ou filtrar as doações de uma ONG específica.
- **Gestão de Equipe**: Seletor do voluntário/triador ativo no plantão e botão `+ Novo Voluntário` para cadastro instantâneo de novos membros.
- **Diálogo de Aprovação Logística**: Acordo operacional com endereço dinâmico da ONG ou agendamento de coleta no bairro do doador.
- **Diálogo de Recusa Construtiva**: Justificativas técnicas pré-formatadas em 1 toque com assinatura do triador.
- **Ação Direta no WhatsApp**: Botão no card para iniciar conversa com o doador para alinhar detalhes de coleta.

---

## 📂 5. Estrutura de Arquivos

```
appandroid/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── res/
│   │   │   ├── drawable/ic_conectadoacoes.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   └── java/br/com/unisinos/conectadoacoes/
│   │       ├── data/
│   │       │   ├── Donation.kt          # Entidade Room de Doação com rastreabilidade
│   │       │   ├── Ngo.kt               # Entidade Room de Organização/ONG
│   │       │   ├── Volunteer.kt         # Entidade Room de Voluntário/Triador
│   │       │   ├── DonationDao.kt       # DAO de doações com queries e filtros
│   │       │   ├── NgoDao.kt            # DAO de entidades parceiras
│   │       │   ├── VolunteerDao.kt      # DAO de equipe e voluntários
│   │       │   └── AppDatabase.kt       # Room Database v3 com Seeding automático
│   │       ├── ui/
│   │       │   ├── theme/
│   │       │   │   ├── Color.kt
│   │       │   │   ├── Theme.kt
│   │       │   │   └── Type.kt
│   │       │   ├── DonationFormScreen.kt# Formulário com seletor multientidade
│   │       │   └── DonationListScreen.kt# Triagem com gestão de equipe e filtros
│   │       └── MainActivity.kt          # Scaffold e coordenação de DAOs
├── gradle/
│   ├── libs.versions.toml               # Version Catalog oficial
│   └── wrapper/
│       └── gradle-wrapper.properties    # Gradle 9.5
├── build.gradle.kts                     # Build raiz
├── settings.gradle.kts                  # Configuração de repositórios
├── gradle.properties                    # Parâmetros JVM e Kotlin
├── gradlew.bat                          # Executável Gradle Windows
└── README.md                            # Documentação acadêmica e guia Git
```

---

## 🐙 6. Publicação no GitHub (Passo a Passo)

Para publicar o projeto no seu repositório pessoal do GitHub com o histórico organizado de commits, execute os seguintes passos no terminal (PowerShell ou Git Bash) aberto dentro da pasta `appandroid`:

### Passo 1: Inicializar o repositório Git local
```powershell
cd c:\Users\ResTIC55\Documents\UNISINOS\appandroid
git init -b main
```

### Passo 2: Histórico de Commits Semânticos
Você pode realizar o commit de todos os arquivos ou registrá-los organizados por marcos:

```powershell
# 1. Configuração e Estrutura Inicial
git add gradle/ build.gradle.kts settings.gradle.kts gradle.properties gradlew.bat .gitignore
git commit -m "chore: setup initial android project with gradle 9.5 and version catalog"

# 2. Camada de Dados e Entidades Room
git add app/src/main/java/br/com/unisinos/conectadoacoes/data/
git commit -m "feat(data): implement room database v3 with donation, ngo and volunteer entities"

# 3. Interface Jetpack Compose e Identidade Visual
git add app/src/main/res/ app/src/main/AndroidManifest.xml app/src/main/java/br/com/unisinos/conectadoacoes/ui/ app/src/main/java/br/com/unisinos/conectadoacoes/MainActivity.kt
git commit -m "feat(ui): implement multi-ngo donor form and advanced triage screens in jetpack compose"

# 4. Documentação e Melhorias de IHC
git add README.md
git commit -m "docs: add comprehensive academic and hci documentation"
```

### Passo 3: Conectar ao seu GitHub e Enviar (Push)
Crie um repositório vazio no seu GitHub (ex: `conectadoacoes`) e vincule o endereço:
```powershell
git remote add origin https://github.com/SEU_USUARIO/conectadoacoes.git
git push -u origin main
```

---

## 👨‍🎓 7. Alinhamento Acadêmico e Apresentação para a Banca

1. **Arquitetura Aberta e Escalável**: Mostre que o app não é um protótipo com valores fixos, mas sim uma **plataforma capaz de conectar qualquer ONG da cidade com cidadãos doadores**.
2. **Governança e Rastreabilidade**: Cada parecer de triagem possui a identificação do voluntário, data/hora e o acordo logístico pactuado.
3. **Usabilidade Centrada no Humano (IHC)**: A recusa construtiva e a demanda invertida demonstram maturidade no relacionamento com a comunidade.
