![Banner Corporatiu Nothingan Pri S.A.](banner.png.jpeg)

# 🚗 Nothingan Pri S.A. — Sistema de Gestió de Lloguer de Vehicles

Aplicació web corporativa de tipus **ERP i CRM** desenvolupada per a **Nothingan Pri S.A.**, dissenyada per centralitzar i digitalitzar la gestió integral de clients, flota de vehicles, reserves i facturació històrica.

---

## 🏗️ Arquitectura i Tecnologies

El sistema segueix el patró arquitectònic **MVC (Model-View-Controller)** sobre l'ecosistema **Spring Boot**, combinant una base de dades relacional per a la operativa diària i una base de dades NoSQL per a l'auditoria i l'analítica històrica:

- **Llenguatge i Framework:** Java 17, Spring Boot, Spring Data JPA, Spring Security.
- **Base de Dades Relacional (Transaccional):** **MySQL** (gestió d'usuaris, rols, clients, agents, vehicles, reserves actives i lliuraments).
- **Base de Dades NoSQL (Històric i Analítica):** **MongoDB** (registre immutable d'esdeveniments i generació d'informes de client - **RF91**).
- **Frontend:** Thymeleaf, HTML5, CSS3 i JavaScript.

---

## 👥 Rols i Control d'Accés (Spring Security)

L'aplicació implementa autenticació segura amb contrasenyes xifrades mitjançant **BCrypt** i control d'accés basat en tres perfils d'usuari:

1. **Client:** Es registra a la plataforma, consulta el catàleg de vehicles disponibles per dates, realitza reserves amb càlcul automàtic de tarifes i obté el seu tiquet de confirmació.
2. **Agent:** Gestiona l'operativa diària de flota, tramita els lliuraments de vehicles (registrant quilometratge i estat inicial) i valida les devolucions per alliberar o retenir la fiança.
3. **Administrador:** Té el control total del sistema; valida les altes de nous clients, gestiona els comptes dels agents, supervisa el cicle de vida de les reserves i consulta els informes històrics de facturació.

---

## ⚙️ Funcionalitats Principals (Sprint 4)

- **Catàleg i Disponibilitat Dinàmica:** Filtratge de vehicles lliures en temps real segons el rang de dates seleccionat, bloquejant automàticament les unitats reservades.
- **Cicle de Vida Complet de la Reserva:**
  - *Càlcul de Pressupost:* Càlcul dinàmic de dies $\times$ preu/dia més la fiança estàndard.
  - *Confirmació i Anul·lació:* Generació de codi de reserva únic i possibilitat d'anul·lació (`ANUL_LADA`) amb alliberament immediat del vehicle.
  - *Lliurament i Devolució:* Control d'estat (`EN_CURS`, `FINALITZADA`) amb registre d'observacions i quilometratge.
- **Informes Històrics de Client amb MongoDB (RF91):**
  - Sincronització d'esdeveniments de reserva (creacions i anul·lacions) cap a la col·lecció `historic_reserves` de MongoDB.
  - Panell d'indicadors (**KPIs**) per client: recompte total de reserves registrades i volum econòmic total facturat.
  - Taula de traçabilitat amb el detall cronològic de cada operació.
