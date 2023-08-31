package Project2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Serverd extends Application {
    private Test test;
    private boolean isOrderShuffled;
    private ArrayList<String> nicknames = new ArrayList<>();
    private double width = 100, height = 300;
    private static final double W = 1000, H = 650;
    Media media = new Media(new File("C:\\Users\\PC\\Desktop\\CSS 108 SPRING\\project\\№3\\src\\Project2\\music.mp3").toURI().toString());
    private Stage stage;
    private ArrayList<Question> questions = new ArrayList<>();
    private int id;
    private int sec = 10;
    private int[] timers = {sec};
    private boolean resultBoolean = false;
    private final Map<String, Integer> states = new HashMap<String, Integer>();
    private Map<Integer, String> resultat = new TreeMap<Integer, String>();

    private static int genPin() {
        return 12345;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        StackPane root = new StackPane();
        Image image = new Image(new FileInputStream("src\\project2\\kahoottitle.jpg"));
        ImageView imageView = new ImageView(image);

        root.getChildren().add(imageView);

        root.setStyle("-fx-background-color: #3e147f");

        BorderPane borderPane = new BorderPane();

        Label lbl = new Label("Game PIN:\n" + genPin());
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));

        lbl.setAlignment(Pos.CENTER);
        lbl.setMinWidth(600);
        borderPane.setTop(lbl);
        borderPane.setBottom(go());
        root.getChildren().addAll(borderPane);

        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(2022);
                int clientNo = 1;


                while (true) {
                    try {
                        System.out.println("Waiting for incomes");
                        Socket socket = server.accept();
                        System.out.println(clientNo + " Client is Connected!");
                        new Thread(() -> {
                            try {
                                DataInputStream fromClient = new DataInputStream(socket.getInputStream());
                                DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
                                while (true) {
                                    int clientPin = fromClient.readInt();
//                            System.out.println(clientPin);
                                    if (clientPin != genPin()) {
                                        toClient.writeUTF("Wrong PIN!");
                                    } else {
                                        toClient.writeUTF("Success!");
                                    }
                                    String nickname = fromClient.readUTF();
                                    System.out.println(nickname);
                                    nicknames.add(nickname);
                                    Label nLbl = new Label(nickname);
                                    nLbl.setTextFill(Color.WHITE);
                                    nLbl.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));


                                    Platform.runLater(() -> {
                                                borderPane.setCenter(nLbl);
                                            }
                                    );
                                    while (true){
                                        if(id == questions.size() -2){
                                            resultBoolean = true;
                                        }

                                            String clientNick = fromClient.readUTF();
                                            int clientChoice = fromClient.readInt();
                                            System.out.println(clientNick);
                                            System.out.println(clientChoice);
                                            String clientChek = test.getOptionAt(clientChoice);
                                            if(checkTest(clientChek, test)){
                                                toClient.writeInt(1);
                                            }else {
                                                toClient.writeInt(0);
                                            }

                                            toClient.writeBoolean(resultBoolean);
                                            System.out.println(resultBoolean);
                                            if(resultBoolean){
                                                String nick = fromClient.readUTF();
                                                int point = fromClient.readInt();
                                                System.out.println(nick);
                                                System.out.println(point);
                                                resultat.put(point, nick);
                                            }



                                    }



                                }


                            } catch (IOException ignored) {

                            }


                        }).start();
                        clientNo++;
//        }
                    } catch (IOException ignored) {

                    }
                }
            } catch (IOException ignored) {

            }

        }).start();

        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.setTitle("Server");
        primaryStage.show();

    }

    public StackPane go() {
        Button btnStart = new Button("Start");
        btnStart.setTextFill(Color.WHITE);
        btnStart.setMinWidth(600);
        btnStart.setMinHeight(50);
        btnStart.setStyle("-fx-background-color: red");
        btnStart.setOnAction(event -> {
            if (questions.get(0) instanceof Test) {
                try {
                    stage.setScene(new Scene(currentTest(0), W, H));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                stage.setScene(new Scene(currentFillIn(0), W, H));
            }
        });
        StackPane stackPane = new StackPane();
        FileChooser fc = new FileChooser();
        Button btn = new Button("Choose a file");
        btn.setTextFill(Color.WHITE);
        btn.setMinWidth(600);
        btn.setMinHeight(50);
        btn.setStyle("-fx-background-color: blue");
        btn.setOnAction(e -> {
            File selectedFile = fc.showOpenDialog(stage);
            Quiz quiz = new Quiz();
            questions = (ArrayList<Question>) quiz.loadFromFile(selectedFile.getPath());
            if (isOrderShuffled)
                Collections.shuffle(questions);
            quiz.setName(selectedFile.getName().replace(".txt", ""));
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        });
        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(btnStart, btn);
        stackPane.getChildren().addAll(vBox);

        return stackPane;
    }


    public BorderPane currentTest(int ind) throws IOException {

        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setOffsetX(3.0);
        ds.setColor(Color.GRAY);
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #46178f");
        Label lblTimer = new Label();
        test = (Test) questions.get(ind);
        Label txtLabel = new Label((ind + 1) + ") " + test.getDescription());
        Font font = Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 30);
        txtLabel.setFont(font);
        txtLabel.setTextFill(Color.WHITE);
        txtLabel.setWrapText(true);
        txtLabel.setMinWidth(W);
        txtLabel.setAlignment(Pos.TOP_CENTER);
        txtLabel.setMinWidth(W);




        ToggleGroup tg = new ToggleGroup();
        Button redBtn = kahootButton(test.getOptionAt(0), "red");
        Button orangeBtn = kahootButton(test.getOptionAt(1), "orange");
        Button blueBtn = kahootButton(test.getOptionAt(2), "blue");
        Button greenBtn = kahootButton(test.getOptionAt(3), "green");

        redBtn.setEffect(ds);
        orangeBtn.setEffect(ds);
        blueBtn.setEffect(ds);
        greenBtn.setEffect(ds);


        VBox vBox1 = new VBox();
        vBox1.getChildren().addAll(redBtn, orangeBtn);
        VBox vBox2 = new VBox();
        vBox2.getChildren().addAll(blueBtn, greenBtn);

        setMargin(redBtn, orangeBtn, blueBtn, greenBtn);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(vBox1, vBox2);
        hBox.setMaxHeight(200);
        hBox.setMaxWidth(200);


//        Image img = new Image("Project2/img/logo.png");
//        ImageView iv = new ImageView(img);
//        iv.setFitWidth(450.);
//        iv.setFitHeight(300.);
        Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {

                        redBtn.setEffect(ds);
                        orangeBtn.setEffect(ds);
                        blueBtn.setEffect(ds);
                        greenBtn.setEffect(ds);
                        String temp = String.valueOf(timers[0]--);
                        lblTimer.setText(temp);
                        Font font = Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 35);
                        lblTimer.setTextFill(Color.WHITE);
                        lblTimer.setFont(font);
                        lblTimer.setWrapText(true);
                        lblTimer.setMinWidth(W);
                        lblTimer.setAlignment(Pos.CENTER);
                        if(timers[0] == -1){
                            timer.cancel();
                            lblTimer.setVisible(false);
                            Button btnNext = controlButton("NEXT");

                            if (ind + 1 < questions.size()) {
                                btnNext.setOnAction(e -> {
                                    if (questions.get(ind + 1) instanceof FillIn) {
                                        stage.setScene(new Scene(currentFillIn(++id), W, H));
                                    } else {
                                        try {
                                            stage.setScene(new Scene(currentTest(++id), W, H));
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }

                                });
                            }
                            else {
                                if (ind == questions.size() - 1){
                                    btnNext.setText("✔");
                                }


                                btnNext.setOnAction(e -> {
                                    try {
                                        stage.setScene(new Scene(result(), W, H));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            }
                            StackPane next = new StackPane(btnNext);
                            next.setMinWidth(80);
                            Bloom bloom = new Bloom();
                            bloom.setThreshold(0.1);
                            MotionBlur bb = new MotionBlur();
                            bb.setRadius(5.0f);
                            bb.setAngle(15.0f);
                            if(!test.getAnswer().equals(test.getOptionAt(0))){
                                redBtn.setEffect(bb);
                            }else redBtn.setEffect(bloom);

                            if(!test.getAnswer().equals(test.getOptionAt(1))){
                                orangeBtn.setEffect(bb);
                            }else orangeBtn.setEffect(bloom);

                            if(!test.getAnswer().equals(test.getOptionAt(2))){
                                blueBtn.setEffect(bb);
                            }else blueBtn.setEffect(bloom);

                            if(!test.getAnswer().equals(test.getOptionAt(3))){
                                greenBtn.setEffect(bb);
                            }else greenBtn.setEffect(bloom);

                            borderPane.setRight(next);
                            timers[0] = sec;

                        }
                    }
                });
            }
        }, 0, 1000);

        VBox holder = new VBox(10);
        holder.setAlignment(Pos.CENTER);
        holder.setMaxWidth(500);
        holder.setMaxHeight(500);
        lblTimer.setFont(Font.font("Times New Roman", FontWeight.MEDIUM, FontPosture.ITALIC, 30));
        holder.getChildren().addAll(lblTimer);
        borderPane.setTop(txtLabel);
        borderPane.setCenter(holder);
        borderPane.setBottom(hBox);
        borderPane.setRight(lblTimer);
        return borderPane;
    }

    private boolean checkTest(String userChoice, Test test) {
        if (userChoice.equals(test.getAnswer())) {
                return true;
        } else {
                return false;
        }
    }

    public BorderPane currentFillIn(int ind) {
        BorderPane borderPane = new BorderPane();
        Label lblTimer = new Label();
        FillIn fillIn = (FillIn) questions.get(ind);
        //ImageView btnIv = new ImageView(new Image("Project2/img/k.png"));
        //btnIv.setFitWidth(50);
        //btnIv.setFitHeight(30);
        Label txtLabel = new Label((ind + 1) + ") " + fillIn.toString());
        Font font = Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 16);
        txtLabel.setFont(font);
        txtLabel.setWrapText(true);
        txtLabel.setMinWidth(W);
        txtLabel.setAlignment(Pos.CENTER);

        Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        String temp = String.valueOf(timers[0]--);
                        lblTimer.setText(temp);
                        Font font = Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 30);
                        lblTimer.setFont(font);
                        lblTimer.setWrapText(true);
                        lblTimer.setMinWidth(W);
                        lblTimer.setAlignment(Pos.BOTTOM_RIGHT);
                        if(timers[0] == -1){
                            timer.cancel();
                            lblTimer.setVisible(false);
                            Button btnNext = controlButton("NEXT");

                            if (ind + 1 < questions.size()) {
                                btnNext.setOnAction(e -> {
                                    if (questions.get(ind + 1) instanceof FillIn) {
                                        stage.setScene(new Scene(currentFillIn(++id), W, H));
                                    } else {
                                        try {
                                            stage.setScene(new Scene(currentTest(++id), W, H));
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }

                                });
                            }
                            else {
                                if (ind == questions.size() - 1) btnNext.setText("✔");
                                btnNext.setOnAction(e -> {
                                    //  stage.setScene(new Scene(resultPane(), W, H));
                                });
                            }
                            StackPane next = new StackPane(btnNext);
                            next.setMinWidth(80);
                            BoxBlur bb = new BoxBlur();

                            borderPane.setRight(next);
                            timers[0] = sec;
                        }
                    }
                });
            }
        }, 0, 1000);


        Button redBtn = kahootButton("red", "red");
        Button orangeBtn = kahootButton("orange", "orange");
        Button blueBtn = kahootButton("blue", "blue");
        Button greenBtn = kahootButton("green", "green");
        redBtn.setVisible(false);
        orangeBtn.setVisible(false);
        blueBtn.setVisible(false);
        greenBtn.setVisible(false);

        VBox vBox1 = new VBox();
        vBox1.getChildren().addAll(redBtn, orangeBtn);
        VBox vBox2 = new VBox();
        vBox2.getChildren().addAll(blueBtn, greenBtn);

        setMargin(redBtn, orangeBtn, blueBtn, greenBtn);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(vBox1, vBox2);
        hBox.setMaxHeight(200);
        hBox.setMaxWidth(200);
        VBox ansLayout = new VBox(10);
//        Image img = new Image("Project2/img/fillin.png");
//        ImageView iv = new ImageView(img);
        VBox holder = new VBox();
        holder.setAlignment(Pos.CENTER);
        holder.setMaxWidth(500);
        holder.setMaxHeight(500);
        lblTimer.setFont(Font.font("Times New Roman", FontWeight.MEDIUM, FontPosture.ITALIC, 16));
        holder.getChildren().addAll(lblTimer);
        holder.setAlignment(Pos.CENTER);
        VBox.setMargin(lblTimer, new Insets(20, 0, 0, 0));

        //iv.setFitWidth(450.);
        //iv.setFitHeight(300.);
        Label hint = new Label("Type your answer here:");
        TextField tf = new TextField();

        tf.setText(fillIn.getTypedAns());
        tf.setMaxWidth(450);
        tf.setMaxHeight(500);
        tf.setPromptText("Type your answer here..");

//        tf1.setVisible(false);
        ansLayout.getChildren().addAll(holder, hint, tf);
        ansLayout.setAlignment(Pos.CENTER);
//        borderPane.setTop(iv);
        borderPane.setTop(txtLabel);
        borderPane.setCenter(ansLayout);

        borderPane.setBottom(hBox);


        BorderPane.setMargin(txtLabel, new Insets(10));

        borderPane.setRight(lblTimer);


        return borderPane;
    }

    public Button controlButton(String btnText) {
        Button btn = new Button(btnText);
        btn.setMinWidth(50);
        btn.setMinHeight(50);

        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 25);
        btn.setFont(font);
        return btn;
    }

    private void setMargin(Button red, Button orange, Button blue, Button green ) {
        VBox.setMargin(red, new Insets(3));
        VBox.setMargin(orange, new Insets(3));
        VBox.setMargin(blue, new Insets(3));
        VBox.setMargin(green, new Insets(3));
    }

    public Button kahootButton(String btnText, String btnColor) {
        Button btn = new Button(btnText);
        btn.setWrapText(true);
        btn.setMinWidth(W / 2. - 5);
        btn.setMinHeight(100);
        btn.setStyle("-fx-background-color: " + btnColor);

        btn.setTextFill(Color.WHITE);
        btn.setWrapText(true);
        btn.setPadding(new Insets(10));

        Font font = Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20);
        btn.setFont(font);
        return btn;
    }

    public BorderPane result() throws IOException {
        BorderPane borderPane = new BorderPane();
        Button place1 = new Button();
        Button place2 = new Button();
        Button place3 = new Button();

        place1.setTextFill(Color.BLACK);
        place1.setMinWidth(W);
        place1.setMinHeight(50);
        place1.setStyle("-fx-background-color: orange");

        place2.setTextFill(Color.WHITE);
        place2.setMinWidth(W);
        place2.setMinHeight(50);
        place2.setStyle("-fx-background-color: silver");

        place3.setTextFill(Color.WHITE);
        place3.setMinWidth(W);
        place3.setMinHeight(50);
        place3.setStyle("-fx-background-color: brown");

        VBox resul = new VBox(10);
        StackPane stackPane = new StackPane();
        new Thread(() ->{
            String[] nick = new String[resultat.size()];
            System.out.println(resultat.size());
            while (true){
                int idk = 0;
                for(int i = 20; i > 0; i--){
                    if(resultat.get(i) != null){
                        nick[idk++] = resultat.get(i);
                    }
                }
                if(nick.length == 1){
                    place1.setText("№1: " + nick[0]);
                    resul.getChildren().add(place1);
                    borderPane.setCenter(resul);
                }

                if(nick.length == 2){
                    place1.setText("№1: " + nick[0]);
                    place2.setText("№2: " + nick[1]);
                    resul.getChildren().addAll(place1, place2);
                    borderPane.setCenter(resul);
                }
                if (nick.length == 3){
                    place1.setText("№1: " + nick[0]);
                    place2.setText("№2: " + nick[1]);
                    place3.setText("№3: " + nick[2]);
                    resul.getChildren().addAll(place1,
                            place2,
                            place3);
                    borderPane.setCenter(resul);
                }
            }

        }).start();

        return borderPane;



    }
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                return map.get(k1).compareTo(map.get(k2));
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }
}
