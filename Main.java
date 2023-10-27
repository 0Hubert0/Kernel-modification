package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class Main extends Application {

    static int[][] kernel = new int[3][3];
    static Canvas canvas = new Canvas();
    static GraphicsContext gr = canvas.getGraphicsContext2D();
    static boolean inverted=false;

    @Override
    public void start(Stage primaryStage) throws Exception{
        AnchorPane root = new AnchorPane();
        Image[] image = new Image[1];
        //BufferedImage img = ImageIO.read(new File("trzy.png"));

        //Image image = new Image(/*"https://www.vinylchapters.com/wp-content/uploads/2019/12/camila-cabello-750x420.jpg"*/"https://i.pinimg.com/750x/ce/20/f7/ce20f771f6d35970b19498af97e2bd07.jpg");
        //Image image = SwingFXUtils.toFXImage(img, null);

        Scene scene = new Scene(root, 900, 700);
        scene.setFill(Color.BLACK);

        canvas.setWidth(scene.getWidth());
        canvas.setHeight(scene.getHeight());
        
        canvas.setLayoutX(30);
        canvas.setLayoutY(30);

        Stage stage = new Stage();

        Text text = new Text("Drop a photo in the middle of the screen");
        text.setFill(Color.BEIGE);
        text.setTranslateX(250);
        text.setTranslateY(150);

        canvas.setOnDragOver(event -> {
            if(event.getGestureSource()!=canvas && event.getDragboard().hasFiles())
            {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        canvas.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if(db.hasFiles())
            {
                String location = db.getFiles().toString().substring(1, db.getFiles().toString().length()-1);
                image[0] = new Image(String.valueOf(new File("file:"+location)));
                gr.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.setWidth(image[0].getWidth());
                canvas.setHeight(image[0].getHeight());
                if(image[0].getWidth()<700)
                {
                    canvas.setLayoutX(100);
                }
                else if(image[0].getWidth()>900)
                {
                    canvas.setLayoutX(0);
                }
                else
                {
                    canvas.setLayoutX(scene.getWidth()- image[0].getWidth());
                }
                stage.setWidth(canvas.getWidth()+400);
                stage.setHeight(canvas.getHeight()+100);
                text.setVisible(false);
                gr.drawImage(image[0], 0, 0);
            }
        });

        Rectangle rec = new Rectangle(canvas.getWidth()+250,
                canvas.getHeight()+100, Color.BLACK);
        root.getChildren().addAll(rec, text, canvas);

        Button original = new Button("Original");
        original.setTranslateY(50);
        root.getChildren().add(original);

        original.setOnAction(event -> {
            kernel= new int[][]{{0, 0, 0}, {0, 1, 0}, {0, 0, 0}};
            Color[][] colors = convolution(getColours(image[0]),kernel);
            draw(colors);
        });

        Button blur = new Button("Blur");
        blur.setTranslateY(50);
        root.getChildren().add(blur);

        blur.setOnAction(event -> {
            kernel= new int[][]{{2, 2, 2}, {2, 2, 2}, {2, 2, 2}};
            Color[][] colors = convolution(getColours(image[0]),kernel);
            draw(colors);
        });

        Button sharpen = new Button("Sharpen");
        sharpen.setTranslateY(100);
        root.getChildren().add(sharpen);

        sharpen.setOnAction(event -> {
            kernel= new int[][]{{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};
            Color[][] colors = convolution(getColours(image[0]),kernel);
            draw(colors);
        });

        Button outline = new Button("Outline");
        outline.setTranslateY(100);
        root.getChildren().add(outline);

        outline.setOnAction(event -> {
            kernel= new int[][]{{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}};
            Color[][] colors = convolution(getColours(image[0]),kernel);
            draw(colors);
        });

        Button emboss = new Button("Emboss");
        emboss.setTranslateY(150);
        root.getChildren().add(emboss);

        emboss.setOnAction(event -> {
            kernel= new int[][]{{-2, -1, 0}, {-1, 1, 1}, {0, 1, 2}};
            Color[][] colors = convolution(getColours(image[0]),kernel);
            draw(colors);
        });

        Button invert= new Button("Invert");
        invert.setTranslateY(150);
        root.getChildren().add(invert);

        invert.setOnAction(event -> {
            inverted = true;
            kernel= new int[][]{{0, 0, 0}, {0, 1, 0}, {0, 0, 0}};
            Color[][] colors = convolution(getColours(image[0]),kernel);
            draw(colors);
            inverted =false;
        });

        Timeline synchronizeTimeline = new Timeline(new KeyFrame(Duration.millis(10), event->{
            rec.setWidth(scene.getWidth());
            rec.setHeight(scene.getHeight());
            original.setTranslateX(scene.getWidth()-200);
            blur.setTranslateX(scene.getWidth()-100);
            sharpen.setTranslateX(scene.getWidth()-200);
            outline.setTranslateX(scene.getWidth()-100);
            emboss.setTranslateX(scene.getWidth()-200);
            invert.setTranslateX(scene.getWidth()-100);
        }));

        synchronizeTimeline.setCycleCount(-1);
        synchronizeTimeline.play();

        stage.setScene(scene);
        stage.show();
    }

    public static Color[][] convolution(Color[][] img, int[][] kernel)
    {
        Color[][] colors = new Color[(int) canvas.getWidth()][(int)canvas.getHeight()];
        for (int i = 0; i < canvas.getWidth()-4; i++) {
            for (int j = 0; j < canvas.getHeight()-4; j++) {
                double sum1 = 0, sum2=0, sum3=0;
                int sumOfSums = 0;
                for (int k = -1; k < kernel.length-1; k++) {
                    for (int l = -1; l <kernel[0].length-1; l++) {
                        if(!(i+k<0||j+l<0) && !(i+k>=canvas.getWidth() || l+j>=canvas.getHeight()))
                        {
                            sum1+=img[i+k][j+l].getRed()*kernel[k+1][l+1];
                            sumOfSums+=kernel[k+1][l+1];
                            sum2+=img[i+k][j+l].getGreen()*kernel[k+1][l+1];
                            sum3+=img[i+k][j+l].getBlue()*kernel[k+1][l+1];
                        }
                    }
                }
                double red = sum1;
                double green = sum2;
                double blue = sum3;
                if(sumOfSums!=0) {
                    red /= sumOfSums;
                    green /= sumOfSums;
                    blue /= sumOfSums;
                }
                if(red>1){red=1;}
                if(red<0){red=0;}
                if(green>1){green=1;}
                if(green<0){green=0;}
                if(blue>1){blue=1;}
                if(blue<0){blue=0;}
                if(inverted)
                {
                    red=1-red;
                    green=1-green;
                    blue=1-blue;
                }
                colors[i][j] = new Color(red, green, blue, 1);

            }
        }
        return colors;
    }

    public static Color[][] getColours(Image image)
    {
        Color[][] colors = new Color[(int)image.getWidth()][(int)image.getHeight()];

        for (int i = 0; i < image.getWidth()-1; i++) {
            for (int j = 0; j < image.getHeight()-1; j++) {
                colors[i][j] = image.getPixelReader().getColor(i, j);
            }
        }
        return colors;
    }

    public static void draw(Color[][] colors)
    {
        gr.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i <canvas.getWidth()-4; i++) {
            for (int j = 0; j < canvas.getHeight()-4; j++) {
                gr.setStroke(colors[i][j]);
                gr.beginPath();
                gr.lineTo(i, j);
                gr.stroke();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
