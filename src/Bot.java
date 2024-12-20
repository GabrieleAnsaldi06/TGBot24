import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ArrayList<Utente> utenti = new ArrayList<>();
    Connection connection = null;
    private boolean searching = false;
    private int registering = 0;

    public Bot() {
        super();
        scheduleTasks();
    }

    private void resetConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tgbot24database", "root", "");
        } catch (SQLException e) {
            System.out.println("Errore nella connessione al database: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return "TGBot24";
    }

    @Override
    public String getBotToken() {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        if (token == null) {
            throw new IllegalStateException("Il token del bot non è stato trovato nella variabile di sistema TELEGRAM_BOT_TOKEN.");
        }
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            new Thread(() -> handleUpdates(update)).start();
        }
    }

    private void handleUpdates(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().startsWith("/register ")) {
                String dati = update.getMessage().getText().substring("/register ".length());
                long id = update.getMessage().getFrom().getId();
                String[] datiArray = dati.split(" ");
                if (datiArray.length == 3) {
                    String nome = datiArray[0];
                    String password = datiArray[1];
                    String password2 = datiArray[2];
                    if (password.equals(password2)) {
                        Utente utente = new Utente(id, password, nome);
                        utenti.add(utente);
                        sendTextMessage(update.getMessage().getChatId().toString(), "Benvenuto, " + utente.getNome() + "!");
                        sendHelpMessage(update.getMessage().getChatId().toString(), utente);
                        resetConnection();
                        try {
                            Statement statement = connection.createStatement();
                            statement.executeUpdate("INSERT INTO utenti (UserId, Password, Nome) VALUES ('" + id + "', '" + password + "', '" + nome + "')");
                        } catch (SQLException e) {
                            if (e instanceof SQLIntegrityConstraintViolationException) {
                                sendTextMessage(update.getMessage().getChatId().toString(), "Utente già registrato.");
                            }else {
                                System.out.println("Errore nell'inserimento dell'utente nel database: " + e.getMessage());
                            }
                        }
                    } else {
                        sendTextMessage(update.getMessage().getChatId().toString(), "Le password non corrispondono.");
                    }
                }
                else {
                    sendTextMessage(update.getMessage().getChatId().toString(), "Formato non valido. Usa /register <nome> <password> <password>");
                }
            } else if (update.getMessage().getText().startsWith("/login ")) {
                String password = update.getMessage().getText().substring("/login ".length());
                long id = update.getMessage().getFrom().getId();
                Utente utente = checkUserRegistered(id);
                if (utente != null) {
                    if (utente.getPassword().equals(password)) {
                        if (utenti.stream().noneMatch(u -> u.getId() == id)) {
                            utenti.add(utente);
                            sendTextMessage(update.getMessage().getChatId().toString(), "Benvenuto, " + utente.getNome() + "!");
                            sendHelpMessage(update.getMessage().getChatId().toString(), utente);
                        }
                        else {
                            sendTextMessage(update.getMessage().getChatId().toString(), "Sei già loggato, " + utente.getNome() + "!");
                        }
                    }
                    else {
                        sendTextMessage(update.getMessage().getChatId().toString(), "Password errata.");
                    }
                }else{
                    sendTextMessage(update.getMessage().getChatId().toString(), "Non sei registrato." +
                            "\nPer registrarti usa /register <nome> <password> <password>");
                }
            } else{
                long id = update.getMessage().getFrom().getId();
                Utente utente = checkUserLogged(id);
                if (utente != null) {
                    handleNews(update, utente);
                }else{
                    String chatId = update.getMessage().getChatId().toString();
                    sendTextMessage(chatId, "Non sei loggato.   :(\n\n" +
                            "Per loggarti usa /login <password>\n" +
                            "Per registrarti usa /register <nome> <password> <password>");
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            long id = update.getCallbackQuery().getFrom().getId();
            Utente utente = checkUserLogged(id);
            if (utente != null) {
                handleNews(update, utente);
            }else{
                String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                sendTextMessage(chatId, "Non sei loggato.   :(\n\n" +
                        "Per loggarti usa /login <password>\n" +
                        "Per registrarti usa /register <nome> <password> <password>");
            }
        }
    }

    private Utente checkUserRegistered(long id) {
        resetConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM utenti WHERE UserId = " + id);
            System.out.println(id);
            if (rs.next()) {
                return new Utente(rs.getLong("UserId"), rs.getString("Password"), rs.getString("Nome"), rs.getInt("points"));
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'elaborazione dei dati: " + e.getMessage());
        }
        return null;
    }

    private Utente checkUserLogged(long id) {
        return utenti.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    private void handleNews(Update update, Utente utente) {
        if (searching){
            if (update.hasMessage() && update.getMessage().hasText()) {
                String chatId = update.getMessage().getChatId().toString();
                String text = update.getMessage().getText();
                searchNews(chatId, text, utente);
            }
        }
        else {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();
                String chatId = message.getChatId().toString();
                String text = message.getText();

                if (text.equals("/start") || text.equals("/help")) {
                    sendHelpMessage(chatId, utente);
                }else if (text.equals("/userinfo")) {
                    sendUserInfo(chatId, utente);
                }else if (text.equals("/points")) {
                    sendPoints(chatId, utente);
                }else if (text.equals("/preferito")) {
                    sendPreferito(chatId, utente);
                }else {
                    sendUnknownCommandMessage(chatId);
                }
            } else if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

                if (callbackData.startsWith("argomento_")) {
                    String topic = callbackData.substring("argomento_".length());
                    sendTextMessage(chatId, "Ecco le notizie per l'argomento " + topic + ":");
                    sendNewsListByTopic(topic, chatId, utente);
                    return;
                }

                switch (callbackData) {
                    case "news_of_the_day":
                        sendTextMessage(chatId, "Ecco la notizia del giorno...");
                        sendDailyNews(chatId, utente);
                        break;
                    case "search_news":
                        sendSearchNewsText(chatId, utente);
                        break;
                    case "news_by_topic":
                        sendTopicButtons(chatId, utente);
                        break;
                    case "news_list_today":
                        sendTextMessage(chatId, "Ecco la lista delle notizie di oggi:");
                        sendNewsList(chatId, utente);
                        break;
                    default:
                        sendTextMessage(chatId, "Comando non riconosciuto.");
                        break;
                }
            }
        }
    }

    private void sendPreferito(String chatId, Utente utente) {
        if (utente.getPreferiti().isEmpty()) {
            sendTextMessage(chatId, "Non hai ancora inserito preferiti.");
        } else {
            sendTextMessage(chatId, "Ecco i tuoi preferiti:");
            for (String preferito : utente.getPreferiti().keySet()) {
                sendTextMessage(chatId, "Argomento: " + preferito);
            }
        }
    }

    private void sendPoints(String chatId, Utente utente) {
        sendTextMessage(chatId, "Punti: " + utente.getPunti());
        resetConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT points FROM Utenti");
            int posizione = 1;
            while (rs.next() && utente.getPunti() < rs.getInt(posizione)) {
                posizione++;
            }
            sendTextMessage(chatId, "Posizione nella classifica dei punti: " + posizione);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendUserInfo(String chatId, Utente utente) {
        sendTextMessage(chatId, "Nome: " + utente.getNome());
        sendTextMessage(chatId, "Punti: " + utente.getPunti());
    }

    private void searchNews(String chatId, String text, Utente utente) {
        resetConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM notizie WHERE Titolo LIKE '%" + text + "%'");
            boolean empty = true;
            while (rs.next()) {
                empty = false;
                Notizia notizia = new Notizia(
                        rs.getString("Titolo"),
                        rs.getDate("Data_Pubblicazione"),
                        rs.getString("Immagine"),
                        rs.getString("Argomento"),
                        rs.getString("Link"),
                        rs.getString("Sintesi")
                );
                sendNews(chatId, notizia, utente);
            }
            if (empty) {
                sendTextMessage(chatId, "Nessuna notizia trovata.");
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'elaborazione dei dati: " + e.getMessage());
        }
        searching = false;
    }

    private void sendSearchNewsText(String chatId, Utente utente) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Inserisci il titolo della notizia che vuoi cercare.");
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        searching = true;
    }

    private void sendDailyNews(String chatId, Utente utente) {
        resetConnection();
        Random random = new Random();
        int max = 0;
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM notizie");
            if (rs.next()) {
                max = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'elaborazione dei dati: " + e.getMessage());
        }
        int randomIndex = random.nextInt(0, max);
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM notizie ORDER BY Titolo DESC LIMIT 1 OFFSET " + randomIndex);
            if (rs.next()) {
                Notizia notizia = new Notizia(
                        rs.getString("Titolo"),
                        rs.getDate("Data_Pubblicazione"),
                        rs.getString("Immagine"),
                        rs.getString("Argomento"),
                        rs.getString("Link"),
                        rs.getString("Sintesi")
                );
                sendNews(chatId, notizia, utente);
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'elaborazione dei dati: " + e.getMessage());
        }
        sendChoices(chatId);
    }

    private void sendNewsListByTopic(String topic, String chatId, Utente utente) {
        resetConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM notizie WHERE Argomento = '" + topic + "'");
            while (rs.next()) {
                Notizia notizia = new Notizia(
                        rs.getString("Titolo"),
                        rs.getDate("Data_Pubblicazione"),
                        rs.getString("Immagine"),
                        rs.getString("Argomento"),
                        rs.getString("Link"),
                        rs.getString("Sintesi")
                );
                sendNews(chatId, notizia, utente);
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'elaborazione dei dati: " + e.getMessage());
        }
        sendChoices(chatId);
    }

    private void sendNews(String chatId, Notizia notizia, Utente utente) {
        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
        message.setCaption(
                notizia.Titolo + "\n" +
                "Argomento: " + notizia.Argomento + "\n" +
                "Sintesi: " + notizia.Sintesi + "\n" +
                "Data di pubblicazione: " + notizia.data_pubblicazione.toString() + "\n" +
                "Link: " + notizia.Link
        );
        message.setPhoto(new InputFile(notizia.Immagine));
        try {
            execute(message);
            utente.gainPoints();
            utente.addPreferito(notizia.Argomento);
            new Thread(() -> updatePoints(utente)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePoints(Utente utente) {
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                Statement statement = connection.createStatement();
                statement.execute("UPDATE utenti SET points = " + utente.getPunti() + " WHERE UserId = " + utente.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendChoices(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Cosa vuoi fare?");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                createButton("Notizia del giorno", "news_of_the_day"),
                createButton("Cerca Notizia", "search_news")
        ));
        rows.add(List.of(createButton("Notizie per argomento", "news_by_topic"),
                createButton("Lista di notizie di oggi", "news_list_today")));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHelpMessage(String chatId, Utente utente) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Ciao " + utente.getNome() + "! Ecco cosa puoi fare:\n\n" +
                "/start, /help - Mostra questa guida\n" +
                "/userinfo - Mostra le informazioni dell'utente\n" +
                "/points - Mostra i punti dell'utente\n" +
                "/preferito - Mostra l'argomento preferito dell' per oggi\n");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                createButton("Notizia del giorno", "news_of_the_day"),
                createButton("Cerca Notizia", "search_news")
        ));
        rows.add(List.of(createButton("Notizie per argomento", "news_by_topic"),
                createButton("Lista di notizie di oggi", "news_list_today")));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTopicButtons(String chatId, Utente utente) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Scegli un argomento:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        try {
            resetConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT DISTINCT Argomento FROM notizie");
            while (rs.next()) {
                String topic = rs.getString("Argomento");
                rows.add(List.of(createButton(topic, "argomento_" + topic)));
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'elaborazione dei dati: " + e.getMessage());
        }

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNewsList(String chatId, Utente utente) {
        resetConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM notizie");
            while (rs.next()) {
                Notizia notizia = new Notizia(
                        rs.getString("Titolo"),
                        rs.getDate("data_pubblicazione"),
                        rs.getString("Immagine"),
                        rs.getString("Argomento"),
                        rs.getString("Link"),
                        rs.getString("Sintesi")
                );
                sendNews(chatId, notizia, utente);
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'elaborazione dei dati: " + e.getMessage());
        }
        sendChoices(chatId);
    }

    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUnknownCommandMessage(String chatId) {
        sendTextMessage(chatId, "Comando non riconosciuto. Usa /help per vedere i comandi disponibili.");
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void scheduleTasks() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tgbot24database", "root", "");
                System.out.println("Connessione al database riuscita!");
            } catch (SQLException e) {
                System.out.println("Errore nella connessione al database: " + e.getMessage());
            }
            try {
                Statement statement = connection.createStatement();
                statement.execute("TRUNCATE TABLE notizie");
            } catch (SQLException e) {
                System.out.println("Errore nella creazione dello statement: " + e.getMessage());
            }
            TGCom24Scraper scraper = new TGCom24Scraper();
            scraper.scrape();
            for (Notizia notizia : scraper.Notizie) {
                System.out.println(notizia.ToString());
                notizia.InserisciInDatabase(connection);
            }
        }, 0, 24, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(() -> {
            utenti.clear();
        }, 0, 2, TimeUnit.HOURS);
    }
}
