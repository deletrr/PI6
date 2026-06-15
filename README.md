PontoLivre — Sistema de Estacionamento Inteligente

O **PontoLivre** é uma solução de **Estacionamento Inteligente baseada em IoT** projetada para otimizar a gestão de vagas urbanas. Através da integração de sensores ultrassônicos (ESP32), comunicação via protocolo MQTT e um ecossistema multiplataforma (Mobile e Web), o sistema permite o monitoramento em tempo real, pagamentos digitais automáticos e fiscalização eficiente, transformando a experiência de estacionar em cidades conectadas.

## Sumário

- [Integrantes](#integrantes)
- [Ferramentas e Bibliotecas Utilizadas](#ferramentas-e-bibliotecas-utilizadas)
- [Funcionalidades Principais](#funcionalidades-principais)
- [Desafios e Limitações Atuais](#desafios-e-limitações-atuais)
- [Planejamento e Soluções Futuras](#planejamento-e-soluções-futuras)
- [Guia de Execução Passo a Passo](#guia-de-execução-passo-a-passo)
- [Simulando Sem Hardware](#simulando-sem-hardware)
- [Usuários para Teste](#usuários-para-teste)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Entidades e CRUDs](#entidades-e-cruds)
- [Endpoints da API](#endpoints)

## Github do projeto:

https://github.com/deletrr/PI6

## PI 4 Semestre (ideia base)

https://github.com/deletrr/PI4iot

O estacionamento em áreas urbanas é um desafio constante que impacta motoristas, gestores públicos e a fluidez do tráfego. A dificuldade em encontrar vagas disponíveis, a fiscalização ineficiente e a ausência de dados em tempo real geram congestionamentos, desperdício de tempo e aumento da emissão de poluentes.
O projeto IoT Cidade Conectada propõe uma solução baseada em Internet das Coisas (IoT), transformando vagas comuns em pontos inteligentes de monitoramento, permitindo controle em tempo real, maior eficiência urbana e melhor experiência para os usuários.

## Videos:
https://www.youtube.com/shorts/J7qhVD1H8Ag
https://www.youtube.com/watch?v=lOSfP_FoQU4

Este projeto integra hardware (ESP32), comunicação em tempo real (MQTT), um backend (Spring Boot) e 
interfaces multiplataforma (Kotlin Multiplatform).

## Integrantes

| Nome | RA | CRUDs (Responsabilidade) |
| :--- | :--- | :--- |
| **Amanda Ianes da Fonseca** | 2571392322023 | Usuários (User), Suporte (SupportTicket) |
| **Bianca Soares Bomfim** | 2571392322013 | Parquímetros (ParkingMeter), Logs (MqttLog) |
| **Daniel Teixeira da Silva** | 2571392312027 | Veículos (Vehicle), Multas (Fine) |
| **Danilo da Silva Paulino** | 2571392322037 | Sessões (ParkingSession), Transações (WalletTransaction) |

## Ferramentas e Bibliotecas Utilizadas

### Backend (Spring Boot)
O núcleo do sistema foi desenvolvido utilizando o ecossistema Kotlin com **Spring Boot**, iniciado via **Spring Initializr**.
- **Spring Boot 3.2.5**: Framework base para a aplicação.
- **Spring Security & JJWT**: Implementação de autenticação e autorização via JSON Web Token (JWT).
- **Spring Data JPA & Hibernate**: Camada de persistência para interação com o banco de dados.
- **Flyway**: Gerenciamento de migrações do banco de dados PostgreSQL.
- **Eclipse Paho**: Cliente MQTT de alta performance para comunicação com o hardware.
- **ZXing**: Geração e leitura de QR Codes para integração com o aplicativo móvel.
- **Spring Boot Validation**: Validação rigorosa de dados de entrada nos DTOs.

### Frontend (Kotlin Multiplatform & Compose)
A interface foi construída com **Compose Multiplatform**, permitindo o compartilhamento de lógica e UI entre Android e Web.
- **Jetpack Compose / Compose Multiplatform 1.6.2**: Framework declarativo para construção de interfaces modernas.
- **Ktor Client**: Biblioteca multiplatforma para consumo de APIs REST.
- **Kotlinx Serialization**: Serialização e desserialização eficiente de JSON.
- **Kotlinx Coroutines**: Gerenciamento de fluxos assíncronos e concorrência.
- **Mapas e Geolocalização**: OpenStreetMap e Leaflet
- **Material 3**: Sistema de design do Google para uma experiência de usuário consistente.

### IoT (ESP32)
O firmware dos parquímetros utiliza bibliotecas leves e eficientes para garantir tempo de resposta em tempo real.
- **PlatformIO**: Ecossistema de desenvolvimento profissional para sistemas embarcados.
- **Arduino Framework**: Base para desenvolvimento rápido e compatibilidade de hardware.
- **PubSubClient**: Cliente MQTT leve para comunicação bidirecional com o broker.
- **Adafruit SSD1306**: Driver para controle do display OLED.

### Hardware (ESP32)
- **Detecção Ultrassônica**: Sensor HC-SR04 com filtros de ruído para detecção precisa.
- **Display OLED Dinâmico**: Alternância automática de telas mostrando status, tempo restante e dados do veículo.
- **Geração de Código Único**: Cada nova ocupação gera um código de segurança diferente.

### Infraestrutura e Ferramentas
- **IntelliJ IDEA / Android Studio**: IDEs principais para desenvolvimento Backend e Mobile.
- **VS Code + PlatformIO**: Ambiente otimizado para o desenvolvimento do firmware ESP32.
- **Docker & Docker Compose**: Orquestração do ambiente de desenvolvimento (PostgreSQL e Mosquitto).
- **PostgreSQL**: Banco de dados relacional robusto para armazenamento de sessões e usuários.
- **Mosquitto (MQTT Broker)**: Servidor de mensagens leve para a infraestrutura IoT.

## Funcionalidades Principais

### Motorista (App Android)
- **Fluxo de Sessão por Código**: Início de estacionamento via código de 6 dígitos gerado na hora pelo parquímetro.
- **Gestão de Veículos**: Cadastro de múltiplos veículos com Modelo, Placa e Cor.
- **Pagamento Incremental**: Pague de 1 em 1 hora (R$ 6,50) diretamente pelo app.
- **Tolerância sem Custo**: Primeiros 15 minutos de estacionamento são gratuitos.
- **Estorno Automático**: Reembolso integral automático se o veículo sair da vaga dentro do período de tolerância.
- **Notificações em Tempo Real**: Alertas de vencimento de tempo pago, fim de tolerância e limite de 2h.
- **Carteira Digital**: Recarga unificada via Pix ou Cartão com aprovação imediata.

### Administrador (Painel Web & Android)
- **Dashboard Moderno (Web)**: Visão geral de usuários, ocupação, receita e alertas.
- **Fiscalização de Obstruções**: Identificação de veículos estacionados sem sessão ativa.
- **Acionamento de Fiscal**: Botão para simular o envio de um fiscal para uma vaga específica.
- **Gestão de Dados**: Controle total sobre usuários (saldos e níveis) e parquímetros.
- **Transparência**: Histórico completo de sessões e logs MQTT brutos para diagnóstico.
---

## Desafios e Limitações Atuais

Durante o desenvolvimento, foram identificados pontos de atenção que impactam a escalabilidade do sistema em ambiente real:

1.  **Conectividade Urbana**: A dependência de redes Wi-Fi em vias públicas é um limitador crítico para a estabilidade da comunicação IoT.
2.  **Confiabilidade de Sensores**: Sensores ultrassônicos podem sofrer interferências ambientais (sujeira, chuva) ou serem obstruídos intencionalmente, gerando falsos positivos de ocupação.
3.  **Complexidade de Integração**: A orquestração entre dispositivos de borda (ESP32), brokers de mensagens (MQTT) e servidores de aplicação exige alta robustez para evitar perda de eventos.
4.  **Custos de Geolocalização**: O uso de APIs proprietárias para mapas e localização gera custos, dificultando a viabilidade de projetos de baixo orçamento.

---

## Planejamento e Soluções Futuras

Para mitigar os desafios citados, o roteiro de evolução do projeto prevê:

- **Migração de Protocolo de Rede**: Substituição do Wi-Fi por tecnologias LPWAN (como LoRaWAN) ou módulos NB-IoT (GSM), que oferecem maior alcance e menor consumo de energia em áreas abertas.
- **Fusão de Sensores**: Implementação de redundância no hardware utilizando magnetômetros (para detectar massa metálica) ou sensores infravermelhos em conjunto com o ultrassônico.
- **Arquitetura de Resiliência**: Implementação de lógica de "Edge Computing" no firmware para permitir que o parquímetro opere offline e sincronize os eventos quando a conexão for restabelecida.
- **Otimização de Processos**: Modularização do backend em microserviços e implementação de pipelines de CI/CD otimizados para reduzir o tempo de compilação e suporte.

---

## Guia de Execução Passo a Passo

### 1. Infraestrutura (Banco de Dados e Broker MQTT)
A maneira mais fácil de iniciar a infraestrutura é usando o Docker. Isso subirá o banco de dados PostgreSQL e o Broker Mosquitto com todas as configurações necessárias.

```bash
cd infra
docker compose up -d
```
> **O que isso faz?**
> - Inicia o **PostgreSQL** e já executa o script `init.sql` (cria tabelas e insere dados e simulações iniciais).
> - Inicia o **Mosquitto MQTT**, permitindo que o ESP32 e o Backend se comuniquem.

---

###  Limpeza do Ambiente (Reset Total CUIDADO!!!)
Caso precise reiniciar o banco do zero ou limpar volumes antigos:
```bash
cd infra
docker compose down -v
```


### 2. Backend (Spring Boot)
O backend gerencia a lógica de cobrança, usuários e sessões.

1.  **Configuração:** Verifique o arquivo `backend/src/main/resources/application.yml`. As credenciais padrão já coincidem com as do Docker.
2.  **Execução:** Na raiz do projeto, execute:
    ```bash
    ./gradlew :backend:bootRun
    ```
    O servidor estará disponível em `http://localhost:8080`.

---

### 3. Firmware (ESP32)
O firmware detecta a presença de veículos e envia via MQTT.

1.  Abra a pasta `esp32_firmware` no VS Code com a extensão **PlatformIO**.
2.  Edite `src/config.h` com suas credenciais:
    ```cpp
    #define WIFI_SSID       "NOME_DO_SEU_WIFI"
    #define WIFI_PASSWORD   "SUA_SENHA"
    #define MQTT_BROKER     "IP_DO_SEU_PC" // O IP da sua máquina na rede local
    ```
3.  Conecte o ESP32 e clique em **Upload** no PlatformIO.

#### Conexões de Hardware (Esquema)
| ESP32 Pin | Componente | Pino do Componente |
| :--- | :--- | :--- |
| GPIO 5 | Sensor HC-SR04 | TRIG |
| GPIO 18 | Sensor HC-SR04 | ECHO |
| GPIO 21 | Display OLED | SDA |
| GPIO 22 | Display OLED | SCL |
| 3.3V / GND | Todos | VCC / GND |

---

### 4. Frontend (Android e Web)
O projeto usa Kotlin Multiplatform para compartilhar lógica entre plataformas.

-   **Android:**
    1. Abra o projeto no Android Studio.
    2. **Mapa:** O projeto utiliza o **MapLibre** (Open Source), que é 100% gratuito e não exige chave de API do Google.
    3. Execute o módulo ``.
    > *Nota: Se usar o emulador, o IP do backend é `http://10.0.2.2:8080`.*
     ```bash
    ./gradlew :frontend_kmp:androidApp
    ```

-   **Web:**
    1. Execute o comando abaixo no terminal:
    ```bash
    ./gradlew clean
    
    ./gradlew :frontend_kmp:webApp:jsBrowserDevelopmentRun
      

    ```    

    O site abrirá em `http://localhost:3000`.

### Backend e Painel Web (Simultâneo)
```bash
./gradlew --parallel :backend:bootRun :frontend_kmp:webApp:jsBrowserDevelopmentRun
```

---

## Simulando Sem Hardware
Você pode testar o sistema completo mesmo sem um ESP32 físico, simulando mensagens MQTT.

1.  **Ocupar Vaga (PKM-001):**
    ```bash
    docker exec -it pontolivre_mosquitto mosquitto_pub -t "parquimetro/PKM-001/status" -m "Ocupado"
    ```
2.  **Liberar Vaga (Fim da Sessão):**
    ```bash
    docker exec -it pontolivre_mosquitto mosquitto_pub -t "parquimetro/PKM-001/status" -m "Livre"
    ```

---

## Usuários para Teste

| Email | Senha | Perfil |
| :--- | :--- | :--- |
| `admin@pontolivre.com` | `Admin@123` | Administrador |
| `joao@email.com` | `User@123` | Usuário Comum |


## Estrutura de Pastas

```
pontolivrePI/
├── backend/
│   └── src/main/kotlin/com/pontolivre/
│       ├── config/
│       │   ├── GlobalExceptionHandler.kt
│       │   ├── MqttConfig.kt
│       │   ├── MqttProperties.kt
│       │   ├── ParkingRulesConfig.kt
│       │   ├── SecurityConfig.kt
│       │   └── WebSocketConfig.kt
│       ├── controller/
│       │   ├── AuthController.kt
│       │   ├── FineController.kt
│       │   ├── MqttLogController.kt
│       │   ├── ParkingMeterController.kt
│       │   ├── ParkingSessionController.kt
│       │   ├── SupportController.kt       
│       │   ├── UserController.kt
│       │   ├── VehicleController.kt
│       │   └── WalletController.kt         
│       ├── dto/
│       │   └── Dtos.kt
│       ├── entity/
│       │   ├── Fine.kt
│       │   ├── MqttLog.kt
│       │   ├── ParkingMeter.kt
│       │   ├── ParkingSession.kt
│       │   ├── SupportTicket.kt
│       │   ├── User.kt
│       │   ├── Vehicle.kt
│       │   └── WalletTransaction.kt
│       ├── mqtt/
│       │   ├── MqttSubscriber.kt
│       │   └── SessionScheduler.kt
│       ├── repository/
│       │   ├── FineRepository.kt
│       │   ├── MqttLogRepository.kt
│       │   ├── ParkingMeterRepository.kt
│       │   ├── ParkingSessionRepository.kt
│       │   ├── SupportTicketRepository.kt
│       │   ├── UserRepository.kt
│       │   ├── VehicleRepository.kt
│       │   └── WalletTransactionRepository.kt
│       ├── security/
│       │   ├── CustomUserDetailsService.kt
│       │   ├── JwtAuthenticationFilter.kt
│       │   └── JwtService.kt
│       └── service/
│           ├── AuthService.kt
│           ├── BillingService.kt
│           ├── FineService.kt
│           ├── ParkingMeterService.kt
│           ├── ParkingSessionService.kt
│           ├── SupportService.kt
│           ├── UserService.kt
│           ├── VehicleService.kt
│           └── WalletService.kt
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-test.yml
│       └── db/migration/
│           ├── V1__initial_schema.sql
│           ├── V2__add_vehicles_and_pending_sessions.sql
│           └── V3__add_refund_transaction_type.sql
│
├── frontend_kmp/
│   ├── androidApp/
│   │   └── src/main/kotlin/com/pontolivre/android/
│   │       └── MainActivity.kt
│   ├── shared/
│   │   └── src/
│   │       ├── commonMain/kotlin/com/pontolivre/shared/
│   │       │   ├── api/
│   │       │   │   ├── ApiClient.kt
│   │       │   │   └── ApiServices.kt
│   │       │   ├── model/
│   │       │   │   └── Models.kt
│   │       │   ├── repository/
│   │       │   │   ├── AppSession.kt
│   │       │   │   └── VehicleRepository.kt
│   │       │   ├── ui/
│   │       │   │   ├── components/Components.kt
│   │       │   │   ├── navigation/Screen.kt
│   │       │   │   ├── screens/
│   │       │   │   │   ├── admin/
│   │       │   │   │   │   ├── AdminLogsAndExtractScreens.kt
│   │       │   │   │   │   └── AdminScreens.kt
│   │       │   │   │   └── user/
│   │       │   │   │       ├── ActiveSessionScreen.kt
│   │       │   │   │       ├── AuthScreens.kt
│   │       │   │   │       ├── HomeScreen.kt
│   │       │   │   │       ├── ManualCodeScreen.kt
│   │       │   │   │       ├── MeterDetailScreen.kt
│   │       │   │   │       ├── MetersScreen.kt
│   │       │   │   │       ├── UserScreens.kt
│   │       │   │   │       ├── VehicleSelectionScreen.kt
│   │       │   │   │       ├── VehiclesScreen.kt
│   │       │   │   │       └── WalletScreens.kt
│   │       │   │   └── theme/Theme.kt
│   │       │   ├── util/
│   │       │   │   ├── Extensions.kt
│   │       │   │   └── Geocoding.kt
│   │       │   └── viewmodel/
│   │       │       ├── AdminViewModels.kt
│   │       │       ├── AuthViewModel.kt
│   │       │       └── UserViewModels.kt
│   │       ├── androidMain/kotlin/com/pontolivre/shared/
│   │       │   ├── api/TokenStorage.android.kt
│   │       │   └── ui/
│   │       │       ├── components/MiniMapView.android.kt
│   │       │       └── screens/user/ParkingMapView.android.kt
│   │       └── jsMain/kotlin/com/pontolivre/shared/
│   │           ├── api/TokenStorage.js.kt
│   │           └── ui/
│   │               ├── components/MiniMapView.js.kt
│   │               └── screens/user/ParkingMapView.js.kt
│   └── webApp/
│       └── src/jsMain/kotlin/com/pontolivre/web/
│           ├── AdminWeb.kt
│           ├── AuthWeb.kt
│           └── Main.kt
│
└── infra/
    ├── docker-compose.yml
    ├── init.sql
    └── mosquitto.conf
```

---

## Entidades e CRUDs

### Usuario/User
| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | PK |
| name | String | |
| email | String | único |
| passwordHash | String | |
| cpf | String | único |
| phone | String? | |
| role | UserRole | USER \| ADMIN |
| balance | BigDecimal | saldo da carteira |
| active | Boolean | |
| createdAt / updatedAt | LocalDateTime | |

### Veiculo/Vehicle
| Campo | Tipo |
|---|---|
| id | UUID |
| user | User (FK) |
| model | String |
| plate | String |
| color | String |

### Parquimetro/ParkingMeter
| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | |
| code | String | único |
| description | String? | |
| latitude / longitude | Double? | |
| status | ParkingStatus | FREE \| OCCUPIED \| RESERVED \| MAINTENANCE |
| mqttTopic | String | único |
| lastSeen | LocalDateTime? | |
| orphan | Boolean | sem sessão vinculada |
| active | Boolean | |

### Sesssão/ParkingSession
| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | |
| user | User? (FK) | |
| parkingMeter | ParkingMeter (FK) | |
| vehicle | Vehicle? (FK) | |
| vehiclePlate | String? | |
| startTime | LocalDateTime | |
| endTime | LocalDateTime? | |
| freeUntil | LocalDateTime | prazo de tolerância |
| chargedHours | Int | |
| amountCharged | BigDecimal | |
| status | SessionStatus | ACTIVE \| CLOSED \| OVERTIME \| PENDING \| EXPIRED |
| overtime | Boolean | |
| sessionCode | String? | código de 6 dígitos do parquímetro |
| codeExpiresAt | LocalDateTime? | |

### Fine   (multas)
| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | |
| user | User (FK) | |
| session | ParkingSession (FK) | |
| amount | BigDecimal | |
| reason | String | |
| status | FineStatus | PENDING \| PAID \| DISPUTED |
| paidAt | LocalDateTime? | |

### Transações/WalletTransaction
| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | |
| user | User (FK) | |
| session | ParkingSession? (FK) | |
| type | TransactionType | CREDIT_PIX \| CREDIT_CARD \| DEBIT_SESSION \| DEBIT_FINE \| CREDIT_REFUND |
| amount | BigDecimal | |
| balanceBefore / balanceAfter | BigDecimal | |
| description | String | |
| paymentMethod | PaymentMethod? | PIX \| CREDIT_CARD |
| referenceCode | String? | |

### SupportTicket
| Campo | Tipo |
|---|---|
| id | UUID |
| user | User (FK) |
| subject | String |
| message | String |
| response | String? |
| resolved | Boolean |

### MqttLog
| Campo | Tipo |
|---|---|
| id | UUID |
| topic | String |
| payload | String |
| createdAt | LocalDateTime |

---

## Endpoints

### Auth — `/api/auth`
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Cadastro de novo usuário |
| POST | `/api/auth/login` | Público | Login — retorna JWT |

---

### Users — `/api/users` e `/api/admin/users`
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/users/me` | Autenticado | Retorna dados do usuário logado |
| PUT | `/api/users/me` | Autenticado | Atualiza dados do usuário logado |
| GET | `/api/admin/users` | ADMIN | Lista usuários com paginação e busca (`?search=&page=&size=`) |
| GET | `/api/admin/users/{id}` | ADMIN | Busca usuário por ID |
| PUT | `/api/admin/users/{id}` | ADMIN | Atualiza usuário (saldo, role, active) |
| DELETE | `/api/admin/users/{id}` | ADMIN | Remove usuário |

---

### Vehicles — `/api/vehicles`
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/vehicles` | Autenticado | Lista veículos do usuário logado |
| POST | `/api/vehicles` | Autenticado | Cadastra novo veículo |
| DELETE | `/api/vehicles/{id}` | Autenticado | Remove veículo do usuário |

---

### Parking Meters — `/api/parking-meters`
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/parking-meters/map` | Público | Lista parquímetros para exibição no mapa |
| GET | `/api/parking-meters/{code}/by-code` | Público | Busca parquímetro pelo código |
| GET | `/api/parking-meters/{id}` | Público | Busca parquímetro por ID |
| GET | `/api/parking-meters` | ADMIN | Lista todos com paginação e busca (`?search=&page=&size=`) |
| GET | `/api/parking-meters/orphans` | ADMIN | Lista parquímetros sem sessão vinculada |
| POST | `/api/parking-meters` | ADMIN | Cria novo parquímetro |
| PUT | `/api/parking-meters/{id}` | ADMIN | Atualiza parquímetro |
| DELETE | `/api/parking-meters/{id}` | ADMIN | Remove parquímetro |

---

### Parking Sessions — `/api/sessions` (sessão)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/sessions/start` | Autenticado | Inicia sessão de estacionamento |
| POST | `/api/sessions/claim` | Autenticado | Vincula sessão pendente via código do parquímetro |
| POST | `/api/sessions/{id}/pay-hours` | Autenticado | Paga hora(s) da sessão ativa |
| POST | `/api/sessions/{id}/end` | Autenticado | Encerra sessão |
| GET | `/api/sessions/active` | Autenticado | Retorna sessão ativa do usuário (204 se nenhuma) |
| GET | `/api/sessions/history` | Autenticado | Histórico paginado de sessões (`?page=&size=`) |
| GET | `/api/sessions/{id}` | Autenticado | Busca sessão por ID |
| GET | `/api/sessions` | ADMIN | Lista todas as sessões com paginação |

---

### Fines — `/api/fines`  (multas)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/fines/mine` | Autenticado | Lista multas do usuário logado (`?page=&size=`) |
| GET | `/api/fines` | ADMIN | Lista todas as multas (`?page=&size=`) |
| GET | `/api/fines/{id}` | ADMIN | Busca multa por ID |
| PUT | `/api/fines/{id}` | ADMIN | Atualiza status da multa |
| DELETE | `/api/fines/{id}` | ADMIN | Remove multa |

---

### Wallet — `/api/wallet` e `/api/admin/wallet` (carteira)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/wallet/balance` | Autenticado | Retorna saldo atual |
| POST | `/api/wallet/recharge` | Autenticado | Recarga via PIX ou cartão |
| GET | `/api/wallet/extract` | Autenticado | Extrato paginado do usuário (`?page=&size=`) |
| GET | `/api/admin/wallet/extract` | ADMIN | Extrato geral de todas as transações (`?page=&size=`) |

---

### Support — `/api/support`
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/support` | Autenticado | Abre chamado de suporte |
| GET | `/api/support/mine` | Autenticado | Lista chamados do usuário logado |
| GET | `/api/support` | ADMIN | Lista todos os chamados (`?resolved=&page=&size=`) |
| POST | `/api/support/{id}/respond` | ADMIN | Responde e resolve chamado |
| DELETE | `/api/support/{id}` | ADMIN | Remove chamado |

---

### Dashboard — `/api/admin/dashboard`
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/admin/dashboard` | ADMIN | Métricas gerais da operação |

---

### MQTT Logs — `/api/admin/mqtt-logs`
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/admin/mqtt-logs` | ADMIN | Retorna os 100 logs MQTT mais recentes |

---

Rotas públicas: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/parking-meters/map`, `GET /api/parking-meters/{code}/by-code`, `GET /api/parking-meters/{id}`.
