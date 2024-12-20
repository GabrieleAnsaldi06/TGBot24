import java.sql.*;

public class Notizia {
    String Titolo;
    Date data_pubblicazione;
    String Immagine;
    String Sintesi;
    String Argomento;
    String Link;

    public Notizia(String Titolo, Date data_pubblicazione, String Immagine, String Argomento, String Link, String Sintesi) {
        this.Titolo = Titolo;
        this.data_pubblicazione = data_pubblicazione;
        this.Immagine = Immagine;
        this.Argomento = Argomento;
        this.Link = Link;
        this.Sintesi = Sintesi;
    }

    public String ToString() {
        return "Notizia {Titolo=" + Titolo + ", data_pubblicazione=" + data_pubblicazione + ", Immagine=" + Immagine + ", Sintesi=" + Sintesi + ", Argomento=" + Argomento + "}";
    }

    public void InserisciInDatabase(Connection connection) {
        try {
            String query = "INSERT INTO notizie (Titolo, data_pubblicazione, Immagine, Sintesi, Argomento, Link) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, Titolo);
            statement.setDate(2, data_pubblicazione);
            statement.setString(3, Immagine);
            statement.setString(4, Sintesi);
            statement.setString(5, Argomento);
            statement.setString(6, Link);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Errore nell'inserimento della notizia nel database: " + e.getMessage());
        }
    }

}
