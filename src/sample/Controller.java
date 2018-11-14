package sample;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private Stage mainStage = Main.stage;
    private File classpath;
    private List<String> urls;
    private String defaultLocation;
    private String saveDirectory;

    @FXML
    public Button startDownload;
    @FXML
    public Button chooseLocation;
    @FXML
    public TextField saveLocation;
    @FXML
    public TextField url;
    @FXML
    public TextField issueNumber;
    @FXML
    public ProgressBar progress;

    @FXML
    public void initialize() throws IOException {
        saveLocation.setDisable(true);
        startDownload.setDisable(true);
        System.out.println("before load");
        loadDefaultLocation();
        System.out.println("after load");
        saveLocation.setText(defaultLocation);
    }

    @FXML
    public void chooseLocation() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        classpath = directoryChooser.showDialog(mainStage);
        if (classpath != null) {
            startDownload.setDisable(false);
            saveLocation.setText(classpath.getAbsolutePath());
            defaultLocation = classpath.toString();
            System.out.println("before save");
            saveDefaultLocation();
            System.out.println("after save");
        }
    }

    public void saveDefaultLocation() throws IOException {
        try (FileWriter writer = new FileWriter("defaultLocation.txt")) {
            writer.write(defaultLocation);
        }
    }

    public void loadDefaultLocation() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("defaultLocation.txt"))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            defaultLocation = builder.toString();
        }
    }

    @FXML
    public void download() throws IOException {
        String url = this.url.getText();
        urls = new ArrayList<>();

        System.out.println("on download");
        Document doc = Jsoup.connect(url).get();
        Elements media = doc.select("[src]");

        for (int i = 0; i < media.size(); i++) {
            Element src = media.get(i);
            if (src.tagName().equals("img")) {
                if (!src.absUrl("abs:src").equals("https://readcomicbooksonline.org/reader/themes/new2/logos1.png") &&
                        !src.absUrl("abs:src").equals("https://readcomicbooksonline.org/reader/themes/new2/no-previous.png") &&
                        !src.absUrl("abs:src").equals("https://readcomicbooksonline.org/reader/themes/new2/next.png") &&
                        !src.absUrl("abs:src").equals("https://readcomicbooksonline.org/reader/themes/new2/no-previous.png") &&
                        !src.absUrl("abs:src").equals("https://readcomicbooksonline.org/reader/themes/new2/next.png"))
                    urls.add(src.absUrl("abs:src"));
            }
        }

        for (String s : urls) {
            System.out.println(s);
        }

        DownloadTask task = new DownloadTask();

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            progress.progressProperty().unbind();
            progress.setProgress(100);
        });

        progress.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    public void saveImage(String imageUrl, String destinationFile) throws IOException {
        saveDirectory = defaultLocation + "\\" + issueNumber.getText();
        new File(saveDirectory).mkdirs();
        String USER_AGENT = "Mozilla/5.0", inputLine;
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        InputStream is = connection.getInputStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    public class DownloadTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            for (int i = 0; i < urls.size(); i++) {
                saveImage(urls.get(i), saveDirectory + "\\" + (i + 1) + ".jpg");
                System.out.println("Downloaded to: " + saveDirectory + "\\" + (i + 1) + ".jpg");
            }
            return null;
        }
    }
}