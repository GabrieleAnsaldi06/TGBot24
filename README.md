# Analisi TGBOT24

![Logo TGBot24](images/logo.png)

## Introduzione

Questo documento definisce un'analisi dettagliata per lo sviluppo di un bot di Telegram denominato **TGBot24**, progettato per fornire notizie sintetizzate agli utenti in modo semplice e accessibile. Il bot utilizza un sistema integrato di Web Scraping, basato su Jsoup, per recuperare notizie dal sito **TGCOM24**. I contenuti vengono elaborati tramite l'API di **ChatGPT**, che sintetizza le informazioni, mentre un database MySQL gestisce l'archiviazione delle notizie e la registrazione delle interazioni con gli utenti.

## Contesto

Il bot **TGBot24** è rivolto principalmente a studenti, professionisti, e appassionati di attualità che desiderano restare aggiornati sulle principali notizie in modo semplice, veloce ed interattivo. Grazie alla capacità di sintetizzare le informazioni, il bot mira a rendere l'accesso alle notizie più diretto e comprensibile, anche per chi ha poco tempo a disposizione.

In particolare, **TGBot24** si propone come uno strumento utile per chi vuole ricevere aggiornamenti quotidiani senza dover navigare attraverso più fonti, offrendo un'esperienza informativa arricchita da sintesi accurate e immagini rappresentative, il tutto direttamente all'interno dell'app Telegram.

## Obiettivi del Software

- Fornire un accesso rapido e facile alle notizie più recenti e rilevanti, rendendole immediatamente disponibili agli utenti.
- Automatizzare il processo di raccolta, sintesi e invio delle notizie tramite Web Scraping e intelligenza artificiale, riducendo al minimo l'intervento manuale.
- Offrire una piattaforma interattiva e user-friendly per la consultazione delle notizie, rendendo l’esperienza informativa più coinvolgente e accessibile.
- Consentire agli utenti di ricevere aggiornamenti personalizzati e sintetici, ottimizzando il tempo dedicato alla lettura delle notizie quotidiane.

## **Requisiti del Sistema**

### **Requisiti non funzionali (qualità)**

- Risposta rapida di pochi secondi per ogni richiesta.
- Il sistema deve essere **scalabile**, in modo da gestire un elevato numero di richieste simultanee senza compromettere le prestazioni.
- Le notizie estratte devono essere **accurate, affidabili e aggiornate**, mantenendo l'integrità delle informazioni fornite.
- L’interfaccia deve essere intuitiva e user-friendly per garantire un’ottima esperienza utente.

## **Funzionalità**

- Il bot invia **notizie sintetizzate** agli utenti, combinando testo e immagini, dopo aver elaborato le informazioni estratte dal sito **TGCOM24**.
- Gli utenti possono ricevere aggiornamenti automatici o personalizzati, selezionando le **categorie di notizie** di loro interesse.
- Gli utenti possono effettuare una ricerca basata su **parole chiave**, ricevendo un elenco di notizie correlate.
- Il sistema offre una cronologia delle notizie inviate, tracciando i progressi di utilizzo degli utenti nel database.

## **Architettura del Sistema**

1. **Telegram Bot**
    - Il bot è il principale punto di interazione con gli utenti. Riceve richieste tramite Telegram e restituisce notizie sintetizzate, corredate da immagini.
2. **API Telegram**
    - Questa componente collega il bot con l'infrastruttura di Telegram, facilitando l'invio e la ricezione di messaggi tramite il protocollo Long Polling o Webhook.
3. **Web Scraper**
    - Implementato con Jsoup, estrae periodicamente i dati dal sito **TGCOM24**: titoli, testi e immagini delle notizie. Successivamente, i dati vengono elaborati e preparati per il modulo di sintesi.
4. **Modulo AI**
    - Utilizzando l'API di **ChatGPT**, sintetizza i contenuti testuali estratti dal Web Scraper per fornire agli utenti una versione compatta e comprensibile delle notizie.
5. **Database**
    - Il database MySQL memorizza:
        - Le notizie estratte, incluse sintesi e URL di origine.
        - Informazioni sugli utenti iscritti al bot.
        - Log delle interazioni tra utenti e bot (ad esempio, cronologia delle notizie inviate).
6. **Utenti**
    - Gli utenti interagiscono con il bot tramite l'app Telegram, richiedendo notizie o ricevendo aggiornamenti giornalieri automatizzati.

## **Tecnologie**

**Linguaggi di Programmazione:**

- **Java**: Utilizzato per sviluppare la logica del bot, gestire le interazioni con gli utenti e implementare il web scraping per estrarre le notizie dai siti web.

**Database Management System (DBMS):**

- **MySQL**: Sistema di gestione del database utilizzato per archiviare le notizie, i dati degli utenti, le preferenze e i log delle interazioni tra bot e utenti.

**API Telegram:**

- L'**API Telegram** viene utilizzata per la comunicazione tra il bot e gli utenti. Consente al bot di ricevere e inviare messaggi in tempo reale, nonché di gestire comandi specifici degli utenti.

**Librerie:**

- **TelegramBots (Java)**: Libreria open-source che facilita l'interazione con l'API di Telegram, rendendo semplice l'invio e la ricezione di messaggi, l'elaborazione dei comandi, e l'interazione con le funzionalità avanzate di Telegram.
- **Jsoup (Java)**: Libreria utilizzata per il **web scraping**. Jsoup permette di connettersi a siti web, estrarre e manipolare facilmente il contenuto HTML per raccogliere notizie, titoli, testi e immagini da fonti come TGCOM24.

## Schema ER del database

![Schema ER del database](images/schema-er.png)

## Schema Logico del database

| **Utenti** | `ID` (PK), `Username`, `Password`, `Nome` |
| --- | --- |
| **Notizie** | `ID` (PK), `Titolo`, `Sintesi`, `URL_Immagine`, `DataPubblicazione`, `Argomento` |

## Interfaccia utente e casi d’uso

![Interfaccia utente e casi d’uso](images/use-case.png)
