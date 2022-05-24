/*
    Class: MainStage
    Contains main method and the primary stage for the application.
    TODO
    make things look pretty
*/

package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.util.converter.DoubleStringConverter;

public class Main extends Application {

    // main layout container for the main stage
    private BorderPane borderPane;

    // control box in the right side for shape transformations inside the subscene.
    // Shape transformations include: Rotate, Translate, Scale, and Change Colour.
    private VBox controlBox;
    private Label controlBoxLabel, rotateXLabel, rotateYLabel, rotateZLabel, translateXLabel, translateYLabel, translateZLabel, scaleLabel, changeBgColorLabel, changeColorLabel;
    private Slider rotateSliderX, rotateSliderY, rotateSliderZ;
    private TextField translateXTextField, translateYTextField, translateZTextField;
    private Button translateButton; // once this is clicked, it gets the values of the text fields
    private Slider scaleSlider;
    final ColorPicker colorPicker = new ColorPicker();
    final ColorPicker bgColorPicker = new ColorPicker();

    // SubScene that has the shapes inside in 3D
    private Pane pane;
    private SubScene subScene;
    private double subSceneWidth;
    private double subSceneHeight;
    private Group shapesGroup;
    private PerspectiveCamera camera;

    // Menu bar that contains "file" menu
    private MenuBar menuBar;

    // "file" menu that contains menu items, "save" and "open"
    private Menu fileMenu;

    private MenuItem saveMenuItem;
    private MenuItem openMenuItem;

    // Button to add the shape into the sub-scene
    private Button addShapeButton;

    // Shape attributes
    private double width, height, depth, radius;
    private Shape3D selectedShape; // this is the selected shape
    private Material selectedShapeMaterial; // this is the selected shape's material

    private Shape3D[] shapeList;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        /***************** Defining the variables *****************/

        // layout that is the root of everything else
        borderPane = new BorderPane();

        // ShapeEditor and its labels
        controlBox = new VBox(20);
        controlBoxLabel = new Label("Делатель фигурок 2000");
        rotateXLabel = new Label("Двигать по оси X: ");
        rotateYLabel = new Label("Двигать по оси Y: ");
        rotateZLabel = new Label("Двигать по оси Z: ");
        translateXLabel = new Label("Translate X: ");
        translateYLabel = new Label("Translate Y: ");
        translateZLabel = new Label("Translate Z: ");
        scaleLabel = new Label("Изменить размер: ");
        changeColorLabel = new Label("Изменить цвет фигуры: ");
        changeBgColorLabel = new Label("Изменить цвет фона: ");

        // Sliders
        rotateSliderX = new Slider(0, 360, 180);
        rotateSliderY = new Slider(0, 360, 180);
        rotateSliderZ = new Slider(0, 360, 180);
        scaleSlider = new Slider(0.5, 2.0, 1.0);

        rotateSliderX.setShowTickMarks(true);
        rotateSliderY.setShowTickMarks(true);
        rotateSliderZ.setShowTickMarks(true);
        scaleSlider.setShowTickMarks(true);

        // Translate TextFields and Button
        translateXTextField = new TextField();
        translateYTextField = new TextField();
        translateZTextField = new TextField();
        translateButton = new Button("Переместить");

        controlBox.setStyle("-fx-background-color: lightsteelblue");
        controlBox.setPadding(new Insets(35, 25, 20, 25));

        disableControls(true);

        // sub-scene that contains the shapes (original: 800, 600)
        pane = new Pane();
        shapesGroup = new Group();
        subScene = new SubScene(pane, subSceneWidth = 700, subSceneHeight = 500);
        camera = new PerspectiveCamera();
        camera.getTransforms().add(new Translate(0, 0, 0));

        pane.getChildren().add(shapesGroup);
        subScene.setCamera(camera);

        /********* Set the input detection for the translate text fields **********/
        setTextFieldDoubleOnly(translateXTextField);
        setTextFieldDoubleOnly(translateYTextField);
        setTextFieldDoubleOnly(translateZTextField);


        /********* Set the Style and the style changer for the pane/background of subscene *********/
        pane.setStyle("-fx-border-style: solid; -fx-border-width: 2px; -fx-border-color: lightgray; -fx-background-color: white");

        bgColorPicker.setOnAction(e -> {
            pane.setStyle("-fx-border-style: solid; -fx-border-width: 2px; -fx-border-color: lightgray; -fx-background-color: #" + colorToHex(bgColorPicker.getValue()));
            System.out.println("Foo");
        });

        // menu bar and its items
        menuBar = new MenuBar();
        fileMenu = new Menu("File");
        saveMenuItem = new MenuItem("Save");
        openMenuItem = new MenuItem("Open");

        saveMenuItem.setOnAction(event->{
        	save(primaryStage);
        });

        openMenuItem.setOnAction(event->{
            load(primaryStage);
        });

        /***************** Add the button that adds shapes into the sub-scene *****************/
        addShapeButton = new Button("Добавить фигуру");
        addShapeButton.setStyle("-fx-pref-width: 500; -fx-pref-height: 30; -fx-font-size: 20; -fx-border-radius: 5");

        addShapeButton.setOnAction(e -> {
            showForm();
        });
        HBox buttonBox = new HBox(addShapeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets( 50, 0, 50, 0));

        /***************** Adding Controls to control box *****************/
        controlBox.getChildren().add(controlBoxLabel);
        controlBox.getChildren().addAll(new HBox(40, rotateXLabel, rotateSliderX),
                new HBox(40, rotateYLabel, rotateSliderY),
                new HBox(40, rotateZLabel, rotateSliderZ),
                new HBox(25, translateXLabel, translateXTextField),
                new HBox(25, translateYLabel, translateYTextField),
                new HBox(25, translateZLabel, translateZTextField),
                new HBox(25, translateButton),
                new HBox(10, scaleLabel, scaleSlider),
                new HBox(5, changeColorLabel, colorPicker),
                new HBox(5, changeBgColorLabel, bgColorPicker));

        controlBox.setMargin(controlBoxLabel, new Insets(0, 0, 15, 0));
        controlBox.setSpacing(15);
        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBoxLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");

        /***************** Adding menu items where they belong *****************/
        menuBar.getMenus().add(fileMenu);
        fileMenu.getItems().addAll(saveMenuItem,openMenuItem);

        /***************** Adding sub-scene, button, and control box to the grid-pane *****************/
        borderPane.setCenter(subScene);
        borderPane.setBottom(buttonBox);
        borderPane.setRight(controlBox);
        borderPane.setTop(menuBar);

        borderPane.setMargin(borderPane.getTop(), new Insets(0, 0, 40, 0));
        borderPane.setMargin(borderPane.getCenter(), new Insets(0, 15, 10, 40));
        borderPane.setMargin(borderPane.getRight(), new Insets(0, 40, 10, 15));
        borderPane.setStyle("-fx-background-color: whitesmoke");

        /********** Controls for the Selected Shape Transformations **********/

        // rotate sliders
        rotateSliderX.valueProperty().addListener((observable, oldValue, newValue) -> { // TODO reset the sliders
            if(selectedShape == null)
                return;
            selectedShape.getTransforms().addAll(new Rotate(((Double) newValue) - ((Double) oldValue), Rotate.X_AXIS));
        });
        rotateSliderY.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedShape == null)
                return;
            selectedShape.getTransforms().addAll(new Rotate(((Double) newValue) - ((Double) oldValue), Rotate.Y_AXIS));
        });
        rotateSliderZ.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedShape == null)
                return;
            selectedShape.getTransforms().addAll(new Rotate(((Double) newValue) - ((Double) oldValue), Rotate.Z_AXIS));
        });

        // Scale Slider
        scaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedShape == null)
                return;
            scale(selectedShape, ((Double) newValue) - ((Double) oldValue));
        });


        // Translate the XYZ position of the shape
        translateButton.setOnAction(actionEvent -> {

            if(selectedShape == null)
                return;

            double x, y, z;

            // if there is nothing in the textfield, just default to 0, otherwise parse the text field
            if(translateXTextField.getText().equals(""))
                x = 0;
            else
                x = Double.parseDouble(translateXTextField.getText());
            if(translateYTextField.getText().equals(""))
                y = 0;
            else
                y = Double.parseDouble(translateYTextField.getText());
            if(translateZTextField.getText().equals(""))
                z = 0;
            else
                z = Double.parseDouble(translateZTextField.getText());

            translate(selectedShape, x, y, z);
        });

        // Color picker to change the color of the selected shape
        colorPicker.setOnAction(actionEvent -> {
            if(selectedShape == null)
                return;
            selectedShapeMaterial = new PhongMaterial(colorPicker.getValue());
            selectedShape.setMaterial(selectedShapeMaterial);
        });

        /********** primary stage set scene **********/
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Shape Editor");
        primaryStage.show();

    } /********** End of start() Method **********/


    /***************** Opens new window with the add shape form *****************/
    private void showForm() {
        Stage stage = new Stage();

        // Labels & controls
        Label label = new Label("Добавить фигуру");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");

        Button addButton = new Button("Добавить фигуру");
        addButton.setPadding(new Insets(5, 50, 5, 50));

        Label shapeLabel = new Label("Фигура: ");
        Label xLabel = new Label("Позиция по оси X: ");
        Label yLabel = new Label("Позиция по оси Y: ");
        Label zLabel = new Label("Позиция по оси Z: ");

        TextField xPosition = new TextField();
        TextField yPosition = new TextField();
        TextField zPosition = new TextField();

        // For boxes only
        Label widthLabel = new Label("Ширина: ");
        Label heightLabel = new Label("Высота: ");

        TextField shapeWidth = new TextField();
        TextField shapeHeight = new TextField();

        // For boxes and cylinders only
        Label depthLabel = new Label("Глубина: ");
        TextField shapeDepth = new TextField();

        // For spheres and cylinders only
        Label radiusLabel = new Label("Радиус: ");
        TextField shapeRadius = new TextField();

        TextField[] textFields = {xPosition, yPosition, zPosition, shapeWidth, shapeHeight, shapeDepth, shapeRadius};

        for(TextField t: textFields){
            setTextFieldDoubleOnly(t);
        }

        // ChoiceBox with list of shapes
        ChoiceBox<String> shapes = new ChoiceBox<>();
        shapes.getItems().addAll("Сфера", "Коробка", "Цилиндр");

        VBox vbox = new VBox(20, label,
                new HBox(40, shapeLabel, shapes),
                new HBox(20, xLabel, xPosition),
                new HBox(20, yLabel, yPosition),
                new HBox(20, zLabel, zPosition),
                addButton);
        vbox.setPadding(new Insets(30));
        vbox.setAlignment(Pos.TOP_CENTER);


        // Shows specific scenes depending on item selected in shape ChoiceBox
        shapes.getSelectionModel().selectedIndexProperty().addListener((source, o, n) -> {
            vbox.getChildren().clear();
            if (n.equals(0)) {
                vbox.getChildren().addAll(label,
                        new HBox(40, shapeLabel, shapes),
                        new HBox(20, xLabel, xPosition),
                        new HBox(20, yLabel, yPosition),
                        new HBox(20, zLabel, zPosition),
                        new HBox(37, radiusLabel, shapeRadius),
                        addButton);
            }
            else if (n.equals(1)) {
                vbox.getChildren().addAll(label,
                        new HBox(40, shapeLabel, shapes),
                        new HBox(20, xLabel, xPosition),
                        new HBox(20, yLabel, yPosition),
                        new HBox(20, zLabel, zPosition),
                        new HBox(41, widthLabel, shapeWidth),
                        new HBox(37, heightLabel, shapeHeight),
                        new HBox(41, depthLabel, shapeDepth),
                        addButton);
            }
            else if (n.equals(2)) {
                vbox.getChildren().addAll(label,
                        new HBox(40, shapeLabel, shapes),
                        new HBox(20, xLabel, xPosition),
                        new HBox(20, yLabel, yPosition),
                        new HBox(20, zLabel, zPosition),
                        new HBox(37, radiusLabel, shapeRadius),
                        new HBox(37, heightLabel, shapeHeight),
                        addButton);
            }
            vbox.setPadding(new Insets(30));
            vbox.setAlignment(Pos.TOP_CENTER);
        });

        // Submit shape details
        addButton.setOnAction(e -> {
            int selectedShape = shapes.getSelectionModel().getSelectedIndex();

            double x,y,z;

            // error handling for empty textfields
            if(xPosition.getText().equals(""))
                x = 0;
            else
                x = Double.parseDouble(xPosition.getText());
            if(yPosition.getText().equals(""))
                y = 0;
            else
                y = Double.parseDouble(yPosition.getText());
            if(zPosition.getText().equals(""))
                z = 0;
            else
                z = Double.parseDouble(zPosition.getText());

            if (selectedShape == 0) { // Sphere
                if(shapeRadius.getText().equals("")) {
                    showFormNotCompleteAlert();
                    return;
                }

                radius = Double.parseDouble(shapeRadius.getText());

                if(radius == 0) {
                    showFormNotCompleteAlert();
                    return;
                }

                Sphere sphere = new Sphere(radius);
                translate(sphere, x, y, z);
                pane.getChildren().add(sphere);
                changeProperties(sphere);
                stage.close();

            } else if (selectedShape == 1) { // Box
                if(shapeWidth.getText().equals("") || shapeHeight.getText().equals("") || shapeDepth.getText().equals("")) {
                    showFormNotCompleteAlert();
                    return;
                }
                width = Double.parseDouble(shapeWidth.getText());
                height = Double.parseDouble(shapeHeight.getText());
                depth = Double.parseDouble(shapeDepth.getText());
                if(width == 0 || height == 0 ||  depth == 0) {
                    showFormNotCompleteAlert();
                    return;
                }
                Box box = new Box(width, height, depth);
                translate(box, x, y, z);
                pane.getChildren().add(box);
                changeProperties(box);
                stage.close();

            } else if (selectedShape == 2) { // Cylinder
                if(shapeRadius.getText().equals("") || shapeHeight.getText().equals("")) {
                    showFormNotCompleteAlert();
                    return;
                }
                radius = Double.parseDouble(shapeRadius.getText());
                height = Double.parseDouble(shapeHeight.getText());

                if(radius == 0 || height == 0) {
                    showFormNotCompleteAlert();
                    return;
                }
                Cylinder cylinder = new Cylinder(radius, height);
                translate(cylinder, x, y, z);
                pane.getChildren().add(cylinder);
                changeProperties(cylinder);
                stage.close();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ОШИБКА");
                alert.setHeaderText("Не выбрана ни одна фигура");
                alert.setContentText("ВЫБЕРИ БЛЯТЬ ФИГУРУ!!!!");
                alert.show();
                return;
            }

            // set the translate textfields to empty, should the initial x, y, z, values be over or under the bounds
            translateXTextField.setText("");
            translateYTextField.setText("");
            translateZTextField.setText("");
        });

        Scene scene = new Scene(vbox, 375, 500);
        stage.setScene(scene);
        stage.setTitle("Add a Shape");
        stage.show();
    }

    private void showFormNotCompleteAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ОЩЩЩИБКА");
        alert.setHeaderText("Введи данные");
        alert.setContentText("Please complete the form and make sure shape properties are not left at 0.");
        alert.show();
    }

    private void changeProperties(Shape3D shape) {

        /********* Select the topmost shape in the pane inside the subscene *********/
        shape.setOnMousePressed(e -> {

            // if this was selected already, unselect it
            if(shape == selectedShape) {
                shape.setMaterial(selectedShapeMaterial);

                selectedShape = null;
                selectedShapeMaterial = null;

                disableControls(true);
                return;
            }

            // the previous selected shape should be changed back, should it exist
            if(selectedShape != null)
                selectedShape.setMaterial(selectedShapeMaterial);

            selectedShape = shape;
            selectedShapeMaterial = shape.getMaterial();
            disableControls(false);
            shape.setMaterial(new PhongMaterial(Color.LIGHTBLUE));
        });
    }

    /*
    Disable the selected shape controls of the main scene
    Controls should be disable when no shape is selected and
    enabled when a shape is selected.
     */
    private void disableControls(boolean b) {
        rotateSliderX.setDisable(b);
        rotateSliderY.setDisable(b);
        rotateSliderZ.setDisable(b);
        scaleSlider.setDisable(b);
        translateXTextField.setDisable(b);
        translateYTextField.setDisable(b);
        translateZTextField.setDisable(b);
        translateButton.setDisable(b);
        colorPicker.setDisable(b);

    }

    /*
    Moves the shape according to the corresponding x, y, z coordinates.
    Won't move the shape outside of the bounds, the subscene width and height,
    according to the center of the shape.
    of the shape. TODO find how to make it stay inside the edge according to opposite edge of the shape
                  TODO moving by z makes it visible beyond width and height
     */
    private void translate(Shape3D shape, double x, double y, double z) {

        // if moving the shape would make it go out of bounds, then don't move in that direction,
        // or only move until at the edge
        if((shape.getTranslateX() + x) > subSceneWidth) {
            x = subSceneWidth - shape.getTranslateX();
            translateXTextField.setText(Double.toString(x));
        } else if ((shape.getTranslateX() + x) < 0 ) {
            x = -(shape.getTranslateX());
            translateXTextField.setText(Double.toString(x));
        }
        if((shape.getTranslateY() + y) > subSceneHeight) {
            y = subSceneHeight - shape.getTranslateY();
            translateYTextField.setText(Double.toString(y));
        } else if((shape.getTranslateY() + y) < 0) {
            y = -(shape.getTranslateY());
            translateYTextField.setText(Double.toString(y));
        }
        if((shape.getTranslateZ() + z) < 0) {
            z = -(shape.getTranslateZ());
            translateZTextField.setText(Double.toString(z));
        }

        shape.setTranslateX(shape.getTranslateX() + x);
        shape.setTranslateY(shape.getTranslateY() + y);
        shape.setTranslateZ(shape.getTranslateZ() + z);
    }

    private void scale(Shape3D shape, double factor) {
        shape.setScaleX(shape.getScaleX() + factor);
        shape.setScaleY(shape.getScaleY() + factor);
        shape.setScaleZ(shape.getScaleZ() + factor);
    }

    public void save(Stage stage)
    {
        FileChooser fc = new FileChooser();
        File myFile = fc.showSaveDialog(stage);

        try {
            PrintWriter printWriter = new PrintWriter(myFile);
            for(int i=0; i<shapeList.length; ++i) {
                printWriter.append(printShapeData(shapeList[i]) + "\n");
            }
            printWriter.close();
        }
        catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error Creating File!");
            alert.setContentText("Please fix the error.");
            alert.show();
        }
    }

    public void load(Stage stage)
    {
        FileChooser FC = null;
        try
        {
            FC = new FileChooser();
            File saveFile = FC.showOpenDialog(stage);

            FC.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            Scanner reader = new Scanner(saveFile);
            String line;
            while(reader.hasNext())
            {
                line = reader.nextLine();
                System.out.println(line);
            }

        }
        catch(NullPointerException NPE)
        {
            System.out.println("Load File Aborted");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File Not Found");
        }
    }

    public String printShapeData(Shape3D shape)
    {
        String deliverables = "";
        deliverables = deliverables.concat(Double.toString(shape.getTranslateX()) +" "+ Double.toString(shape.getTranslateY()) +" "+ Double.toString(shape.getTranslateZ()) +" "+
                Double.toString(shape.getScaleX()) +" "+ Double.toString(shape.getScaleY()) +" "+ Double.toString(shape.getScaleZ())+" ");
        if(shape instanceof Box)
        {
            Box B = (Box)shape;
            deliverables = deliverables.concat(Double.toString(B.getWidth()) +" "+ Double.toString(B.getHeight()) +" "+ Double.toString(B.getDepth()));
            deliverables = "B " + deliverables;
        }
        else if(shape instanceof Sphere)
        {
            Sphere S = (Sphere)shape;
            deliverables = deliverables.concat(Double.toString(S.getRadius()));
            deliverables = "S " + deliverables;
        }
        else if(shape instanceof Cylinder)
        {
            Cylinder C = (Cylinder)shape;
            deliverables = deliverables.concat(Double.toString(C.getRadius()) +" "+ Double.toString(C.getHeight()));
            deliverables = "C " +deliverables;
        }
        return deliverables;
    }

    public static String colorToHex(Color color)
    {
        String hex1;
        String hex2;

        hex1 = Integer.toHexString(color.hashCode()).toUpperCase();

        switch (hex1.length()) {
            case 2:
                hex2 = "000000";
                break;
            case 3:
                hex2 = String.format("00000%s", hex1.substring(0,1));
                break;
            case 4:
                hex2 = String.format("0000%s", hex1.substring(0,2));
                break;
            case 5:
                hex2 = String.format("000%s", hex1.substring(0,3));
                break;
            case 6:
                hex2 = String.format("00%s", hex1.substring(0,4));
                break;
            case 7:
                hex2 = String.format("0%s", hex1.substring(0,5));
                break;
            default:
                hex2 = hex1.substring(0, 6);
                break;
        }
        return hex2;
    }

    /*
    Set the passed in text field to only contain a double as x.y
    where x is a 4 digit number, and y is a 2 digit number.
    With regards to setting the x, y, z position, the translate
    will be forced to be within the bounds of the subscene.
    See method:
        translate(Shape3D shape, double x, double y, double z)
    for more information.
     */
    private void setTextFieldDoubleOnly(TextField textField){
        final Pattern validDoubleText = Pattern.compile("\\-?\\d{0,3}([\\.]\\d{0,2})?");
        textField.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                }));
        textField.setText("");
    }
}