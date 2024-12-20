import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;

public class TGCom24Scraper {

    public ArrayList<Notizia> Notizie;

    public TGCom24Scraper(){
        Notizie = new ArrayList<>();
    }

    public void scrape() {
        try {
            String url = "https://www.tgcom24.mediaset.it";
            Document document = Jsoup.connect(url).get();
            Elements newsElements = document.select("div.cursor-default");
            HashSet<String> links = new java.util.HashSet<String>();
            for (Element newsElement : newsElements) {
                Element linkElement = newsElement.selectFirst("a");
                String link = linkElement != null ? linkElement.attr("href") : "";
                links.add(link);
            }
            url = "https://www.tgcom24.mediaset.it/ultimissima/oraxora.shtml";
            document = Jsoup.connect(url).get();
            Element newsElement = document.selectFirst("div.bd-area-main");
            Elements linkelements = newsElement.select("a");
            for (Element linkElement : linkelements) {
                String link = linkElement != null ? linkElement.attr("href") : "";
                links.add(link);
            }
            for (String link : links) {
                try {
                    document = Jsoup.connect(link).get();
                    Element titleElement = document.selectFirst("h1");
                    String title = titleElement != null ? titleElement.text() : "Nessun titolo";
                    Elements jsonLdScripts = document.select("script[type=application/ld+json]");
                    String Titolo = "";
                    String data_pubblicazione = "";
                    String Immagine = "";
                    String Argomento = "";
                    String Sintesi = "";
                    for (Element script : jsonLdScripts) {
                        String jsonContent = script.html();
                        JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
                        if (jsonObject.has("@type") && "NewsArticle".equals(jsonObject.get("@type").getAsString())) {
                            Titolo = jsonObject.get("headline").getAsString();
                            data_pubblicazione = jsonObject.get("datePublished").getAsString().substring(0, 10);
                            JsonArray immagini = jsonObject.getAsJsonArray("image");
                            Immagine = immagini.get(0).getAsString();
                            String argomentoUrl = jsonObject.get("genre").getAsString();
                            if (argomentoUrl.endsWith("/")) {
                                argomentoUrl = argomentoUrl.substring(0, argomentoUrl.length() - 1);
                            }
                            Sintesi = jsonObject.get("description").getAsString();
                            Argomento = argomentoUrl.substring(argomentoUrl.lastIndexOf('/') + 1);
                            Notizie.add(new Notizia(Titolo, Date.valueOf(data_pubblicazione), Immagine, Argomento, link, Sintesi));
                            break;
                        }
                    }
                } catch (ValidationException | MalformedURLException e) {
                    System.out.println("Link non valido: " + link);
                }
            }
        } catch (IOException e) {
            System.out.println("Link non valido o errore nella connessione: " + e.getMessage());
        }
    }
}
