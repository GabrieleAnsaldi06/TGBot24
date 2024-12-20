import java.util.HashMap;

public class Utente {
    private long username;
    private String password;
    private String nome;
    private int punti = 0;
    HashMap<String, Integer> preferiti = new HashMap<String, Integer>();

    public Utente(long username, String password, String nome, int punti) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.punti = punti;
    }

    public Utente(long username, String password, String nome) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.punti = 0;
    }

    public void addPreferito(String argomento) {
        preferiti.put(argomento, 0);
    }

    private void tostring() {
        System.out.println("Utente {Username=" + username + ", Password=" + password + ", Nome=" + nome + "}");
    }

    public long getId() {
        return username;
    }

    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }

    public int getPunti() {
        return punti;
    }

    public void gainPoints() {
        this.punti++;
    }

    public HashMap<String, Integer> getPreferiti() {
        return preferiti;
    }
}
