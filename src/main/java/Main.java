import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;
import java.io.*;
import java.net.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) throws IOException {

        String s = "https://www.nalog.ru/opendata/7707329152-kgn/";
        getZipArchive(s);
    }
    public static void getZipArchive(String uri) {
        try (
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(new HttpGet(uri))
        ) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String data = IOUtils.toString(entity.getContent(), "UTF-8");

                if (data.contains("Сведения об участии в консолидированной группе налогоплательщиков")) {
                    String zipPath =null;
                    Pattern p =Pattern.compile("((https?:)[a-z0-9\\s_@\\-^!#$%&+={}.\\/]+)+\\.zip"); //регулярное выражение для нахождения ссылки на zip-архив
                    Matcher m = p.matcher(data);
                    if(m.find()){
                        zipPath=m.group();
                    }
                    readDataFromZip(zipPath);

                } else {
                    System.out.println(data);
                }
            }
        } catch (Throwable cause) {
            cause.printStackTrace();
        }
    }

    private static void readDataFromZip(String path) throws IOException {
        URL url = new URL(path);
        ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(url.openStream(), "UTF-8");
        byte[] buf = new byte[1024];
        while (zipInputStream.getNextEntry() != null) {
            zipInputStream.read(buf);
            Document document = Jsoup.parse(new String(buf));
            Elements elements =  document.select("СведНП");
            for (Element element : elements) {
                System.out.println(element.attr("ИННЮЛ"));
            }
        }
    }
}
