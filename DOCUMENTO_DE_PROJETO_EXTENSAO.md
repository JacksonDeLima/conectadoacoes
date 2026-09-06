# UNIVERSIDADE DO VALE DO RIO DOS SINOS - UNISINOS
## CURSO DE ANÁLISE E DESENVOLVIMENTO DE SISTEMAS - CAMPUS SÃO LEOPOLDO
### ATIVIDADE ACADÊMICA EXTENSIONISTA — DESAFIO DE DESENVOLVIMENTO MOBILE

---

# PROJETO CONECTADOAÇÕES
## Plataforma Móvel para Gestão de Cadeia de Custódia, Triagem Colaborativa e Prestação de Contas para Entidades do Terceiro Setor

* **Discente / Equipe de Desenvolvimento:** Jackson Luis S. de Lima (e Equipe ADS UNISINOS)
* **Semestre Letivo:** 2026/1
* **Instituição de Ensino:** UNISINOS (São Leopoldo / RS)
* **Entidade Parceira Externa:** Banco Comunitário de Alimentos do Vale do Sinos / Associações Beneficentes da Região
* **Repositório Oficial:** [https://github.com/JacksonDeLima/conectadoacoes](https://github.com/JacksonDeLima/conectadoacoes)

---

## 1. DADOS DA ENTIDADE PARCEIRA E CONTEXTO SOCIAL

### 1.1 Identificação da Entidade
* **Nome Institucional:** Banco Comunitário de Alimentos do Vale do Sinos
* **Município de Atuação:** São Leopoldo, Novo Hamburgo, Sapucaia do Sul e região do Vale do Rio dos Sinos (RS).
* **Público Beneficiário:** Famílias em situação de vulnerabilidade socioeconômica extrema, indivíduos em insegurança alimentar e pessoas desabrigadas atendidas pela rede assistencial.

### 1.2 O Diagnóstico da Dor Social
Historicamente, as entidades assistenciais do Vale do Sinos operam a recepção e coleta de doações de forma precária e manual, principalmente por meio de aplicativos de mensagens instantâneas (WhatsApp) e ligações telefônicas. Esse modelo informal acarreta severas falhas operacionais:
1. **Sobrecarga dos Voluntários:** Dezenas de mensagens dispersas com fotos de itens que muitas vezes não atendem aos critérios de higiene, validade ou estado de conservação da entidade.
2. **Coletas Frustradas e Custo de Combustível:** Deslocamento inútil de vans e veículos da entidade para buscar doações anunciadas que já haviam sido doadas para terceiros ou que eram materiais imprestáveis (taxa de frustração superior a 35%).
3. **Ausência de Transparência e Rastreabilidade (Gargalo do MROSC):** Dificuldade crônica em prestar contas formais para os Conselhos Municipais de Assistência Social e para o Ministério Público, conforme preconizado pela **Lei Federal nº 13.019/2014 (Marco Regulatório das Organizações da Sociedade Civil - MROSC)**.

### 1.3 Alinhamento aos Objetivos de Desenvolvimento Sustentável (ONU 2030)
* **ODS 2 — Fome Zero e Agricultura Sustentável:** Garantia de fluxo contínuo de alimentos não perecíveis e de qualidade para as cozinhas comunitárias.
* **ODS 10 — Redução das Desigualdades:** Destinação direcionada de bens essenciais (roupas térmicas, fraldas, colchões) às famílias periféricas.
* **ODS 11 — Cidades e Comunidades Sustentáveis:** Logística urbana circular e resiliente no Vale do Sinos.
* **ODS 12 — Consumo e Produção Responsáveis:** Reutilização de itens domésticos em bom estado, evitando o descarte inadequado em lixões e aterros.

---

## 2. ARQUITETURA DA SOLUÇÃO E PROJETO COMPLETO

O **ConectaDoações** foi concebido como um ecossistema móvel de colaboração física e digital (*phygital*) que conecta duas pontas essenciais da sociedade: o **Cidadão Doador** e os **Voluntários e Gestores das ONGs**.

### 2.1 Visão Global do Produto (Macrofluxo)
```
[ Cidadão Doador ]
       │
       ▼ (1. Cadastro com Foto + Consulta à Demanda Invertida)
[ Plataforma ConectaDoações (Room SQLite v5) ]
       │
       ▼ (2. Triagem Técnica: Aprovação com Acordo Logístico OU Recusa Empática)
[ Voluntário Plantonista da ONG ]
       │
       ├───────────────────────────────────────────┐
       ▼ (3. Rota no Maps para Coleta)             ▼ (4. Entrega no Ponto)
[ Van da Entidade (Logística) ]           [ Doador leva à sede ]
       │                                           │
       └─────────────────────┬─────────────────────┘
                             ▼
               [ 5. Entrada no Estoque ] (Conferência com QR Code 2D)
                             │
                             ▼
            [ 6. Destinação ao Beneficiário ] (Família acolhida)
                             │
                             ▼
          [ 7. Prestação de Contas & Balancete ] (PDF A4 Oficial MROSC)
```

---

## 3. ESPECIFICAÇÃO DE REQUISITOS (MATRIZ FORMAL DE ENGENHARIA DE SOFTWARE)

Para atender ao critério de prever a **solução completa**, a matriz abaixo detalha o escopo integral do projeto dividido pela metodologia **MoSCoW**, demarcando com precisão os requisitos implementados no aplicativo e os do roadmap de expansão:

### 3.1 Requisitos Funcionais Implementados (Escopo Entregue no App)

| Identificador | Requisito Funcional | Descrição Detalhada | Critério de Aceite |
| :--- | :--- | :--- | :--- |
| **RF01** | **Cadastro de Doações com Foto** | O doador tira ou seleciona foto da galeria, preenche título, descrição, categoria, nome, bairro e WhatsApp. | Imagem é duplicada e persistida localmente no armazenamento privado do app. |
| **RF02** | **Vitrine de Demanda Invertida** | Carrossel dinâmico que exibe as carências urgentes cadastradas pela ONG selecionada no dropdown. | Ao tocar em um chip de urgência, a categoria e a descrição sugerida são pré-preenchidas. |
| **RF03** | **Triagem Visual da Entidade** | Painel da ONG com listagem de ofertas pendentes, foto ampliada, dados do doador e atalho WhatsApp direto. | Voluntário visualiza o estado do bem antes de autorizar a entrada ou coleta. |
| **RF04** | **Acordo Logístico na Aprovação** | Modal que obriga a definição da logística: *Entrega no Ponto* (doador leva) ou *Coleta em Domicílio* (ONG busca). | Registro de instruções claras de recebimento e identificação do triador responsável. |
| **RF05** | **Recusa Construtiva e Empática** | Diálogo que obriga a seleção de um motivo respeitoso e educativo caso o item seja incompatível. | Previne desperdício de transporte e educa o cidadão sobre as reais necessidades da ONG. |
| **RF06** | **Cadeia de Custódia Auditável** | O item progride por 5 estados formais: *Pendente ➔ Aprovado ➔ Recebido no Estoque ➔ Entregue ao Beneficiário* (ou *Recusado*). | Cada transição carimba timestamp e nome do responsável físico pela operação. |
| **RF07** | **Recibo Digital com QR Code 2D** | Gera documento digital com código de auditoria `#CD-xxxx` e matriz visual 2D para conferência de estoque. | Renderização de Bitmap nítido no celular do doador para conferência no galpão. |
| **RF08** | **Balancete Social e Indicadores** | Dashboard analítico com Total de Doações, Itens em Estoque, Famílias Beneficiadas e Taxa de Eficiência. | Cálculo dinâmico em tempo real sobre a base local do SQLite Room. |
| **RF09** | **Exportação de PDF Formal (MROSC)** | Geração nativa de documento PDF em formato A4 oficial com padrão sóbrio (sem emojis) e linhas de assinatura. | Criação física do arquivo via `PdfDocument` e abertura através de `FileProvider`. |
| **RF10** | **Roteamento com Google Maps / Waze** | Agrupa bairros de coletas agendadas e traça rota multiparadas (*waypoints*) até a sede da entidade. | Disparo de `Intent` nativa do Android para aplicativos de GPS sem custo de API. |
| **RF11** | **Gestão Dinâmica de Demandas** | Modal no painel da ONG permitindo cadastrar novas carências, nível de criticidade ou remover carências atendidas. | Atualização reativa imediata na vitrine visualizada pelos doadores. |

### 3.2 Requisitos Funcionais do Roadmap Futuro (Solução Global Completa)

| Identificador | Requisito Funcional Futuro | Descrição |
| :--- | :--- | :--- |
| **RF12** | **Autenticação RBAC com Gov.br** | Login formal de administradores, voluntários de campo e cidadãos com verificação de identidade. |
| **RF13** | **Sincronização em Nuvem Híbrida** | Replicação dos dados locais com serviço em nuvem (ex: Supabase/PostgreSQL) para rede estadual de ONGs. |
| **RF14** | **Notificações Push (FCM)** | Alertas no celular do doador informando em tempo real: *"Sua doação foi entregue à Família Santos!"*. |
| **RF15** | **Integração com Cadastro Único / CRAS** | Validação automática da família acolhida com o número do NIS/CadÚnico municipal. |

### 3.3 Requisitos Não-Funcionais (RNFs)

* **RNF01 — Arquitetura e Linguagem:** Construído 100% em **Kotlin 2.0** utilizando o framework declarativo **Jetpack Compose** e arquitetura reativa baseada em fluxos (**StateFlow**).
* **RNF02 — Persistência Offline-First:** Banco de dados relacional embarcado **SQLite** gerenciado pelo **Android Room v5**, garantindo funcionamento integral em galpões ou regiões sem sinal de telefonia.
* **RNF03 — Conformidade com o MROSC e LGPD:** Tratamento ético de dados cadastrais, registro de custódia transparente e conformidade com a Lei Federal nº 13.019/2014.
* **RNF04 — Usabilidade e Heurísticas de Nielsen:**
  - *Visibilidade do Status do Sistema:* Badges coloridos e rastreamento `#CD-xxxx` constante.
  - *Prevenção de Erros:* Modais de confirmação, campos obrigatórios validados e opções pré-configuradas.
  - *Design Universal:* Contraste acessível, áreas de toque generosas (mínimo 48dp) e suporte a redimensionamento de fontes.

---

## 4. DEFESA DOS CRITÉRIOS DE AVALIAÇÃO DA BANCA ACADÊMICA

### 🎯 Critério 1: Complexidade dos Requisitos Escolhidos (30% da Nota)
O projeto **supera com folga um CRUD básico acadêmico** ao incorporar regras de negócio complexas de logística e auditoria pública:
1. **Máquina de Estados de Custódia Física:** Não é apenas salvar dados; o sistema gerencia um ciclo de vida rigoroso de 5 estados com conferência presencial no galpão e destinação a famílias acolhidas.
2. **Geração Documental em PDF Nativo (Low-Level Canvas):** O sistema constrói programaticamente um documento PDF em padrão A4 oficial (`android.graphics.pdf.PdfDocument`), desenhando títulos, tabelas, bordas e linhas de assinatura em coordenadas cartesianas sem dependências de frameworks externos pesados.
3. **Gerador Algorítmico de Matriz 2D / QR Code:** Renderização de matriz booleana com marcadores de posicionamento (*Position Finder Patterns*) e modulação de dados para conferência rápida de estoque.
4. **Logística Geográfica com Waypoints:** Algoritmo que agrupa bairros de coletas pendentes e constrói URI parametrizada para disparar rotas de coleta otimizadas no Google Maps.

### 🎯 Critério 2: Funcionalidade, Completude e Correto Funcionamento (40% da Nota)
O aplicativo encontra-se **100% implementado, testado e validado**, executando perfeitamente tanto no emulador Android Studio quanto em smartphones físicos:
* Persistência de fotos via armazenamento local privado (`File(context.filesDir, ...)`).
* Três abas de navegação integradas no `Scaffold`: **Quero Doar**, **Triagem ONG** e **Prestação de Contas**.
* Validação de integridade: nenhuma ação fecha o app inesperadamente (*zero crashes*), transições suaves com Material 3 e feedback visual instantâneo via `Snackbar` e `Toast`.

### 🎯 Critério 3: Correto Uso das Tecnologias Estudadas (10% da Nota)
* **Kotlin 2.0 + KSP (Kotlin Symbol Processing):** Compilação moderna com anotações tipadas para Room.
* **Jetpack Compose + Material Design 3:** Interface declarativa sem arquivos XML legados, utilizando `TopAppBar`, `NavigationBar`, `ElevatedCard`, `FilterChip` e `ExposedDropdownMenuBox`.
* **Room Database v5:** 4 entidades relacionais (`Donation`, `Ngo`, `Volunteer`, `UrgentNeedEntity`) com DAO reativo baseado em `kotlinx.coroutines.flow.Flow`.
* **Android Jetpack FileProvider:** Compartilhamento seguro de URIs de PDFs gerados sem violação de permissões de armazenamento (`FLAG_GRANT_READ_URI_PERMISSION`).

### 🎯 Critério 4: Qualidade do Código, Apresentação e IHC (20% da Nota)
* **Organização Modular de Pacotes:**
  - `data/`: Entidades, DAOs e migração do banco Room.
  - `ui/`: Telas desacopladas (`DonationFormScreen`, `DonationListScreen`, `AccountabilityScreen`).
  - `util/`: Utilitários dedicados (`PdfReportGenerator`, `QrCodeGenerator`, `LogisticsRouteHelper`).
* **Qualidade Visual e Identidade:** Tema sóbrio em verde esmeralda e azul marinho, transmitindo credibilidade institucional e acolhimento social.

---

## 5. DOCUMENTAÇÃO VISUAL E PROTÓTIPO DE TELAS

O projeto adota navegação intuitiva dividida nas três principais jornadas da assistência social:

1. **Tela 1 — Quero Doar (Jornada do Cidadão):**
   - Seletor da instituição parceira destinatária com exibição de endereço e horários.
   - Vitrine horizontal com chips de demandas urgentes em tempo real.
   - Captura e preview de foto com câmera/galeria.
   - Botão de envio ergonômico com subtítulo inteligente e prevenção de truncamento.
2. **Tela 2 — Triagem ONG & Cadeia de Custódia (Jornada do Voluntário):**
   - Barra de filtros por entidade e seleção do triador plantonista da equipe.
   - Atalhos de um toque para: Rota no Google Maps, Bipe de Código de Auditoria e Gestão de Demandas.
   - Cards detalhados com ações de triagem (Aprovar com Acordo Logístico vs. Recusar com Feedback Construtivo).
   - Ações de custódia: "Confirmar Entrada no Estoque" e "Registrar Entrega a Família Acolhida".
3. **Tela 3 — Prestação de Contas & Balancete Social (Jornada da Gestão e Comunidade):**
   - Seletor de balancete consolidado da rede ou por entidade individual.
   - Cards de métricas analíticas (Total, Estoque, Entregas, Taxa de Eficiência).
   - Inventário com barras de progresso percentuais por categoria de bens.
   - Livro de auditoria transparente com códigos de rastreamento `#CD-xxxx`.
   - Botão para **Gerar Relatório Oficial em PDF** formatado e sóbrio (sem emojis) para auditoria e conselhos.

---

## 6. TERMO DE VALIDAÇÃO E COMPROVAÇÃO DA ATIVIDADE EXTENSIONISTA

```text
================================================================================
          UNIVERSIDADE DO VALE DO RIO DOS SINOS - UNISINOS
       TERMO DE VALIDAÇÃO DE PROJETO DE EXTENSÃO UNIVERSITÁRIA
================================================================================

Declaramos para os devidos fins de comprovação acadêmica junto à UNISINOS,
referente ao Desafio da Atividade Acadêmica Extensionista do Curso de Análise
e Desenvolvimento de Sistemas (ADS), que os acadêmicos responsáveis pelo
projeto "ConectaDoações" mantiveram contato e apresentaram a proposta de solução
tecnológica desenvolvida para esta entidade parceira.

A solução atende satisfatoriamente às necessidades identificadas na organização
prévia de ofertas de doação, redução de perdas logísticas de coleta comunitária
e estruturação transparente de prestação de contas sociais nos parâmetros do
Marco Regulatório das Organizações da Sociedade Civil (Lei 13.019/2014).

Nome da Entidade: Banco Comunitário de Alimentos do Vale do Sinos
Município / UF: São Leopoldo / RS
Representante / Cargo: ______________________________________________________
Assinatura do Responsável: __________________________________________________
Data de Validação: ____ / ____ / 2026
================================================================================
```

---
*Documento elaborado e formatado de acordo com as diretrizes de avaliação extensionista da UNISINOS.*
