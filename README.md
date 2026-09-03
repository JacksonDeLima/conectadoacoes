# ConectaDoações 🤝📦
### Plataforma Mobile de Logística Reversa, Triagem Inteligente e Gestão Comunitária de Doações

> **Projeto de Extensão Universitária**  
> **Instituição:** Universidade do Vale do Rio dos Sinos – **UNISINOS**  
> **Curso:** ADS - Análise e Desenvolvimento de Sistemas - Campus São Leopoldo  
> **Área Temática:** Tecnologia, Assistência Social e Sustentabilidade  

---

##  1. Contexto Social e Diagnóstico Comunitário

O terceiro setor e as entidades de acolhimento social (casas de passagem, bancos de alimentos, lares de idosos e brechós comunitários) no Brasil enfrentam desafios logísticos e operacionais que comprometem sua eficiência assistencial:

1. **Gargalo Operacional na Triagem Informal**: O atendimento por aplicativos de mensagens (WhatsApp) gera sobrecarga de mensagens não estruturadas, fotos dispersas e perda de tempo da equipe avaliando itens inadequados (roupas rasgadas, alimentos vencidos, móveis quebrados).
2. **Desperdício de Recursos em Coletas Frustradas**: Voluntários mobilizam veículos, combustível e tempo para buscar doações sem dados prévios de volume, peso ou condições reais de conservação, gerando custos operacionais inviáveis.
3. **Frustração do Doador e Ruptura da Confiança**: A ausência de resposta estruturada deixa o cidadão no escuro sobre o destino de sua doação ou gera rejeições abruptas que desmotivam futuras ações solidárias.
4. **Desalinhamento entre Oferta e Demanda**: Entidades recebem excesso de itens que já possuem em estoque enquanto enfrentam escassez crítica de itens essenciais (leite em pó, agasalhos infantis, fraldas geriátricas).

O **ConectaDoações** foi concebido como uma resposta tecnológica a esses gargalos, promovendo a ponte direta entre a cidadania ativa e a gestão de suprimentos do terceiro setor.

---

##  2. Alinhamento com os Objetivos de Desenvolvimento Sustentável (ODS – ONU)

O projeto está formalmente vinculado à **Agenda 2030 das Nações Unidas**, atuando diretamente nos seguintes Objetivos de Desenvolvimento Sustentável:

| ODS | Meta Relacionada | Aplicação no ConectaDoações |
| :--- | :--- | :--- |
| **ODS 2: Fome Zero** | Meta 2.1 e 2.2 | Apoio direto a Bancos de Alimentos Comunitários com triagem ágil de cestas básicas e alimentos não perecíveis, acelerando a redistribuição antes do vencimento. |
| **ODS 10: Redução das Desigualdades** | Meta 10.2 | Democratização do acesso a vestuário, móveis e itens de higiene de primeira necessidade para famílias em vulnerabilidade social. |
| **ODS 11: Cidades Sustentáveis** | Meta 11.6 | Fortalecimento das redes comunitárias locais e redução do impacto logístico do transporte urbano no descarte e coleta de bens. |
| **ODS 12: Consumo Responsável** | Meta 12.5 | Fomento à **Logística Reversa e Economia Circular**, garantindo o prolongamento da vida útil de bens duráveis e evitando descarte prematuro em aterros sanitários. |

---

##  3. Diretrizes da Extensão Universitária (MEC / CNE nº 7/2018)

O desenvolvimento deste projeto atende integralmente aos cinco princípios fundamentais que regem a extensão na educação superior brasileira:

1. **Interação Dialógica**: O software não é construído "para" a comunidade, mas "com" a comunidade. Ele modela a rotina real de triadores voluntários e doadores locais, estabelecendo canais humanizados de comunicação.
2. **Interdisciplinaridade e Interprofissionalidade**: Articulação entre **Engenharia de Software** (arquitetura reativa, Room Database), **Interação Humano-Computador** (design de acessibilidade e heurísticas de usabilidade), **Logística Operacional** e **Serviço Social**.
3. **Indissociabilidade Ensino-Pesquisa-Extensão**: Aplicação direta das teorias aprendidas em sala de aula (padrões de projeto, persistência de dados local, linguagens modernas) para resolver um problema territorial palpável.
4. **Impacto na Formação do Estudante**: Oportunidade para o discente vivenciar o impacto social de sua profissão, desenvolvendo sensibilidade ética, cidadã e capacidade de resposta a carências comunitárias.
5. **Impacto e Transformação Social**: Potencial de otimização de dezenas de horas de trabalho voluntário por semana, transformando esforço braçal e repetitivo em acolhimento humano qualificado.

---

##  4. Matriz de Atores e Proposta de Valor

```
                     ┌─────────────────────────────────────────┐
                     │            ConectaDoações               │
                     └─────────────────────────────────────────┘
                                ▲                   ▲
                                │                   │
                    ┌───────────┴────────┐ ┌────────┴───────────┐
                    │   Cidadão Doador   │ │  Entidade Social   │
                    └────────────────────┘ └────────────────────┘
                    • Escolha da ONG        • Triagem visual ágil
                    • Vitrine de carências  • Acordo logístico claro
                    • Feedback respeitoso   • Gestão de voluntários
```

| Ator | Papel no Ecossistema | Valor Entregue pelo Sistema |
| :--- | :--- | :--- |
| **Cidadão Doador** | Agente de solidariedade que disponibiliza itens próprios para a comunidade. | Visualização das carências urgentes, facilidade de envio com 1 foto e retorno claro sobre logística ou motivo de recusa. |
| **Voluntário / Triador** | Membro da instituição responsável pela análise de viabilidade dos itens. | Painel centralizado com filtros, botões de decisão em 1 toque e eliminação da sobrecarga em canais pessoais de mensagens. |
| **Coordenação da ONG** | Liderança que planeja frotas, campanhas e capacidade de armazenamento. | Cadastro da instituição, definição de horários, frentes prioritárias e rastreabilidade da equipe atuante. |
| **Comunidade Beneficiária** | Famílias acolhidas que recebem os donativos. | Recebimento de itens com dignidade, limpos, em perfeito estado de uso e com garantia de prazos de validade. |

---

##  5. Princípios de Interação Humano-Computador (IHC) & Usabilidade

O sistema foi desenhado sob a ótica das **10 Heurísticas de Usabilidade de Jakob Nielsen**:

* **Demanda Invertida (Prevenção de Erros – Nielsen #5)**: Em vez de receber doações aleatórias, o aplicativo expõe no topo do formulário uma vitrine com o que a entidade mais precisa (*Urgente*, *Necessário*, *Estoque Cheio*). Ao tocar na necessidade, o campo de categoria é pré-selecionado, evitando ofertas incompatíveis na origem.
* **Recusa Construtiva (Diagnóstico e Recuperação de Falhas – Nielsen #9)**: Rejeições secas geram atrito e afastam o doador. O sistema disponibiliza motivos pré-formatados empáticos (ex: capacidade técnica de reparo excedida, validade de alimentos expirada) acompanhados de orientações sobre onde direcionar o item.
* **Acordo Logístico Operacional (Correspondência com o Mundo Real – Nielsen #2)**: Elimina o vácuo operacional pós-aprovação. Define formalmente se o doador entregará o item na sede da entidade (com endereço e horário auto-preenchidos) ou se a ONG agendará uma rota de coleta em domicílio no bairro informado.
* **Transparência e Governança (Visibilidade do Status – Nielsen #1)**: Toda doação aprovada ou recusada é carimbada com o nome e função do voluntário responsável e a data/hora da avaliação.

---

##  6. Arquitetura de Software & Modelagem de Dados

O aplicativo segue o padrão **Offline-First**, garantindo funcionamento fluido mesmo em áreas com instabilidade de conexão móvel através de banco de dados SQLite embarcado com a biblioteca oficial **Android Jetpack Room v3**.

### Stack Tecnológica
* **Linguagem:** Kotlin 2.0.21 (Moderna, expressiva e padrão oficial Android)
* **Interface:** Jetpack Compose com Material Design 3 (Declarativo, reativo e acessível)
* **Banco Local:** SQLite nativo gerenciado via Room Database v3
* **Concorrência e Reatividade:** Kotlin Coroutines e `StateFlow` (Unidirectional Data Flow)
* **Mídia:** Android Photo Picker (`PickVisualMedia`) com armazenamento privado durável
* **Renderização de Imagens:** Coil Compose

### Diagrama Entidade-Relacionamento (Room v3)
```
┌────────────────────────────────────────┐
│                  Ngo                   │
├────────────────────────────────────────┤
│ id: Long (PK, AutoGenerate)            │
│ name: String                           │
│ categoryFocus: String                  │
│ address: String                        │
│ phone: String                          │
│ operatingHours: String                 │
│ isPartnerVerified: Boolean             │
└────────────────────────────────────────┘
          │ 1                         │ 1
          │                           │
          │ N                         │ N
┌─────────▼──────────────────────────────┤  ┌─────────────────────────────────────┐
│               Donation                 │  │              Volunteer              │
├────────────────────────────────────────┤  ├─────────────────────────────────────┤
│ id: Long (PK, AutoGenerate)            │  │ id: Long (PK, AutoGenerate)         │
│ ngoId: Long (FK)                       │  │ ngoId: Long (FK)                    │
│ ngoName: String                        │  │ name: String                        │
│ title: String                          │  │ role: String                        │
│ category: String                       │  │ phone: String                       │
│ description: String                    │  └─────────────────────────────────────┘
│ imageUri: String?                      │
│ status: String [Pendente|Aprovado|Rec.]│
│ createdAt: Long                        │
│ donorName: String                      │
│ donorNeighborhood: String              │
│ donorPhone: String                     │
│ reviewedBy: String?                    │
│ reviewedAt: Long?                      │
│ rejectionReason: String?               │
│ logisticsType: String?                 │
│ logisticsDetails: String?              │
└────────────────────────────────────────┘
```

---

##  7. Funcionalidades e Fluxos das Telas

### Tela 1: Visão do Doador (`DonationFormScreen.kt`)
1. **Seletor Dinâmico de Entidade**: O cidadão escolhe para qual instituição do município deseja doar e visualiza sede e horário de atendimento.
2. **Cadastro Instantâneo de Novas Entidades**: Botão `+ Nova ONG` para inclusão imediata de novas instituições parceiras.
3. **Vitrine de Necessidades Atuais**: Carrossel com chips coloridos de urgência que pré-preenchem o item.
4. **Identificação Cidadã**: Nome, Bairro/Região (para viabilidade de rota de coleta) e WhatsApp.
5. **Captura Visual com Cópia Segura**: O doador anexa foto da galeria que é persistida no armazenamento privado do app (`context.filesDir`), prevenindo perda de URI pós-reboot.

### Tela 2: Central de Triagem da ONG (`DonationListScreen.kt`)
1. **Filtros por Entidade e Status**: Permite alternar entre visualizar doações de uma única ONG ou de toda a rede comunitária.
2. **Gestão de Equipe e Plantão**: Seletor do voluntário ativo no turno e botão `+ Novo Voluntário` para cadastro de novos triadores em 1 toque.
3. **Diálogo de Aprovação com Acordo Logístico**: Escolha entre entrega no ponto de coleta ou coleta em domicílio com janela de turno.
4. **Diálogo de Recusa com Feedback Construtivo**: Escolha de justificativa padronizada e campo de nota orientativa.
5. **Integração com WhatsApp**: Botão de contato direto com o doador para alinhamento rápido.

---

##  8. Como Clonar, Compilar e Executar

### Pré-requisitos
* **Android Studio:** Ladybug (2024.2.1+) ou versão equivalente com suporte a AGP 8.x/9.x.
* **JDK:** Java 17 ou Java 21 (incluso no Android Studio JBR).
* **Android SDK:** SDK Platform 35 (Android 15) instalado via SDK Manager.
* **Dispositivo de Teste:** Emulador Android (API 26+) ou smartphone físico com depuração USB ativa.

### Passo a Passo de Execução

1. **Clonar o Repositório:**
   ```bash
   git clone https://github.com/JacksonDeLima/conectadoacoes.git
   ```

2. **Abrir no Android Studio:**
   - Abra o **Android Studio**.
   - Selecione **File > Open** e navegue até a pasta clonada `conectadoacoes`.
   - Aguarde o término da sincronização do Gradle (*Gradle Sync*).

3. **Executar a Aplicação:**
   - Selecione o seu emulador ou dispositivo no seletor de dispositivos superior.
   - Clique no botão **Run ▶** ou pressione **Shift + F10**.

---

##  9. Indicadores de Impacto Social Estimado (KPIs)

Para validação contínua da eficácia extensionista junto às ONGs parceiras, o projeto estabelece as seguintes métricas:

| Indicador | Situação Anterior (WhatsApp / Informal) | Meta com o ConectaDoações |
| :--- | :--- | :--- |
| **Tempo Médio de Resposta da Triagem** | 24 a 72 horas por conversa de chat | **Menos de 2 minutos** por laudo em 1 toque |
| **Índice de Coletas Frustradas** | ~35% dos deslocamentos de veículos | **Redução para menos de 5%** com triagem prévia |
| **Aproveitamento de Doações Recebidas** | ~50% dos itens doados sem condição de uso | **Mais de 85% de itens prontos para uso** com demanda invertida |
| **Retenção e Recorrência de Doadores** | Baixa (devido à falta de feedback) | **Engajamento contínuo** com transparência nas decisões |

---

## 📄 10. Licença e Autoria

Projeto desenvolvido por **Jackson Luis** para fins acadêmicos e de extensão comunitária no âmbito da **Universidade do Vale do Rio dos Sinos (UNISINOS)**.

Distribuído sob a licença **MIT**, permitindo que outras entidades sociais, municípios e instituições de ensino adaptem e ampliem a plataforma livremente para fins comunitários.
