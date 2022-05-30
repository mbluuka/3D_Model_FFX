/*
    Добро пожаловать в код приложения по построению 3D фигур, а также последующая работа с ними
    TODO Фигуры
    Тор, эллипс, плоскость, отрезок

    TODO Выбор нескольких фигур и функционал для них, как для соло фигуры
*/

package sample;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.geometry.Insets;
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
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.util.converter.DoubleStringConverter;

public class Main extends Application {

    // Основной слой
    private BorderPane borderPane;

    // Боковая панель, имеющая функционал по редактированию параметров фигуры
    private VBox controlBox;
    private Label controlBoxLabel,
            rotateXLabel, rotateYLabel, rotateZLabel,
            translateXLabel, translateYLabel, translateZLabel,
            scaleLabel, changeBgColorLabel, changeColorLabel;

    private Slider rotateSliderX, rotateSliderY, rotateSliderZ;
    private TextField translateXTextField, translateYTextField, translateZTextField;
    private Button translateButton, deleteButton;
    private Slider scaleSlider;
    final ColorPicker colorPicker = new ColorPicker();
    final ColorPicker bgColorPicker = new ColorPicker();
    private TextField xcoordsTF, ycoordsTF, zcoordsTF;

    // Подсцена, в которую мы будем добавлять фигуры
    private Pane pane;
    private SubScene subScene;
    private double subSceneWidth;
    private double subSceneHeight;
    private Group shapesGroup;
    private PerspectiveCamera camera;

    // Сама кнопка меню
    private MenuBar menuBar;

    // Вызов меню с параметрами
    private Menu fileMenu;

    private MenuItem saveMenuItem;
    private MenuItem openMenuItem;

    // Кнопка для добавления фигуры
    private Button addShapeButton;

    // Переменные фигур
    private double width, height, depth, radius;
    private Shape3D selectedShape; // Метка выбранной фигуры
    private Material selectedShapeMaterial; // Цвет выбранной фигуры

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        /***************** Определение переменных *****************/

        // Макет/слой, который является фундаментом всего что тут только есть
        borderPane = new BorderPane();

        // ShapeEditor and its labels
        controlBox = new VBox(15);
        controlBoxLabel = new Label("Делатель фигурок 2000"); // Название

        // Метка "вращателей"
        rotateXLabel = new Label("Вращать по оси X: ");
        rotateYLabel = new Label("Вращать по оси Y: ");
        rotateZLabel = new Label("Вращать по оси Z: ");

        // Метка "перемещателей"
        translateXLabel = new Label("Переместить по X: ");
        translateYLabel = new Label("Переместить по Y: ");
        translateZLabel = new Label("Переместить по Z: ");

        scaleLabel = new Label("Изменить размер: "); // Метка изменения размера
        changeColorLabel = new Label("Изменить цвет фигуры: ");
        changeBgColorLabel = new Label("Изменить цвет фона: ");

        // Ползунки
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

        xcoordsTF = new TextField();
        xcoordsTF.setEditable(false);

        ycoordsTF = new TextField();
        ycoordsTF.setEditable(false);

        zcoordsTF = new TextField();
        zcoordsTF.setEditable(false);

        translateButton = new Button("Переместить");
        deleteButton = new Button("Удалить");

        // Устанавливаем задний фон боковой панели
        controlBox.setStyle("-fx-background-color: lightsteelblue");
        controlBox.setPadding(new Insets(30, 20, 15, 20));

        disableControls(true);

        // Подсцена, содержащая фигуры (Оригинальный масштаб подсценки: 800, 600)
        pane = new Pane();
        shapesGroup = new Group();
        subScene = new SubScene(pane, subSceneWidth = 750, subSceneHeight = 550);
        camera = new PerspectiveCamera();
        camera.getTransforms().add(new Translate(0, 0, 0));

        pane.getChildren().add(shapesGroup);
        subScene.setCamera(camera);

        /********* Устанавливаем определение ввода для полей с перемещалкой **********/
        setTextFieldDoubleOnly(translateXTextField);
        setTextFieldDoubleOnly(translateYTextField);
        setTextFieldDoubleOnly(translateZTextField);


        /********* Стиль для основного окна и под окна *********/
        pane.setStyle("-fx-border-style: solid; -fx-border-width: 2px;" +
                " -fx-border-color: lightgray; -fx-background-color: white");

        bgColorPicker.setOnAction(e -> {
            pane.setStyle("-fx-border-style: solid; -fx-border-width: 2px;" +
                    " -fx-border-color: lightgray; -fx-background-color: #" + colorToHex(bgColorPicker.getValue()));
            System.out.println("Background is change");
        });

        // Менюшка и её кнопочки
        menuBar = new MenuBar();
        fileMenu = new Menu("Файл");
        saveMenuItem = new MenuItem("Сохранить");
        openMenuItem = new MenuItem("Открыть");

        saveMenuItem.setOnAction(event->{
        	save();
        });

        openMenuItem.setOnAction(event->{
            load();
        });

        /***************** Добавляем кнопку, которая будет вызывать функцию для построения фигуры *****************/
        addShapeButton = new Button("Добавить фигуру");
        addShapeButton.setStyle("-fx-pref-width: 500; -fx-pref-height: 30; -fx-font-size: 20; -fx-border-radius: 5");

        addShapeButton.setOnAction(e -> {
            showForm();
        });

        HBox buttonBox = new HBox(addShapeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets( 50, 0, 50, 0));

        /***************** Добавление блока с визуализацией данными *****************/
        controlBox.getChildren().add(controlBoxLabel);
        controlBox.getChildren().addAll(
                new HBox(30, rotateXLabel, rotateSliderX),
                new HBox(30, rotateYLabel, rotateSliderY),
                new HBox(30, rotateZLabel, rotateSliderZ),
                new HBox(25, translateXLabel, translateXTextField),
                new HBox(25, translateYLabel, translateYTextField),
                new HBox(25, translateZLabel, translateZTextField),
                new HBox(20, translateButton, deleteButton),
                new HBox(20, scaleLabel, scaleSlider),
                new HBox(10, changeColorLabel, colorPicker),
                new HBox(10, changeBgColorLabel, bgColorPicker)
                );

        controlBox.setMargin(controlBoxLabel, new Insets(0, 0, 10, 0));
        controlBox.setSpacing(10);
        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBoxLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");

        /***************** Добавление меню *****************/
        menuBar.getMenus().add(fileMenu);
        fileMenu.getItems().addAll(saveMenuItem,openMenuItem);

        /***************** Добавление подсцены, кнопок и боковой панели *****************/
        borderPane.setCenter(subScene); // что по центру
        borderPane.setBottom(buttonBox); // что внизу
        borderPane.setRight(controlBox); // что справа
        borderPane.setTop(menuBar); // что сверху

        borderPane.setMargin(borderPane.getTop(), new Insets(0, 0, 40, 0));
        borderPane.setMargin(borderPane.getCenter(), new Insets(0, 15, 10, 40));
        borderPane.setMargin(borderPane.getRight(), new Insets(0, 40, 10, 15));
        borderPane.setStyle("-fx-background-color: whitesmoke");

        /********** Контроллеры для изменения выбранной фигуры **********/

        // Слайдеры для изменения позиции фигуры
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

        // Ползунок для изменения масштаба фигуры
        scaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedShape == null)
                return;
            scale(selectedShape, ((Double) newValue) - ((Double) oldValue));
        });

        // Фунционал кнопки удаления фигуры
        deleteButton.setOnAction(actionEvent -> {
            if(selectedShape == null) {
                return;
            }
            selectedShape.setDisable(false);
            selectedShape.setVisible(false);
        });

        // перемещаем фигуру по значению координаты
        translateButton.setOnAction(actionEvent -> {

            if(selectedShape == null)
                return;

            double x, y, z;

            // Если с поле textfield ничего не введено, то заносим 0, иначе парсим то, что ввел пользователь
            if(translateXTextField.getText().equals(""))
                x = 0;
            else {
                x = Double.parseDouble(translateXTextField.getText());
            }
            if(translateYTextField.getText().equals(""))
                y = 0;
            else{
                y = Double.parseDouble(translateYTextField.getText());
            }
            if(translateZTextField.getText().equals(""))
                z = 0;
            else{
                z = Double.parseDouble(translateZTextField.getText());
            }
            translate(selectedShape, x, y, z);
        });

        // Выбираем цвет для выбранной фигуры
        colorPicker.setOnAction(actionEvent -> {
            if(selectedShape == null)
                return;
            selectedShapeMaterial = new PhongMaterial(colorPicker.getValue());
            selectedShape.setMaterial(selectedShapeMaterial);
        });



        /********** Создание сцены **********/
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Модельки 2000");
        primaryStage.show();

    }
    /********** Конец метода start() **********/


    /***************** Открытие нового окна для добавления фигуры *****************/
    private void showForm() {
        Stage stage = new Stage();

        // Метки и контроллеры
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

        // Для фигур типа "коробка"
        Label widthLabel = new Label("Ширина: ");
        Label heightLabel = new Label("Длина: ");

        TextField shapeWidth = new TextField();
        TextField shapeHeight = new TextField();

        // For boxes and cylinders only
        Label depthLabel = new Label("Высота: ");
        TextField shapeDepth = new TextField();

        // Для сферы и цилиндра
        Label radiusLabel = new Label("Радиус: ");
        TextField shapeRadius = new TextField();

        // Для эллипса

        TextField[] textFields = {xPosition, yPosition, zPosition, shapeWidth, shapeHeight, shapeDepth, shapeRadius};

        for(TextField t: textFields){
            setTextFieldDoubleOnly(t);
        }

        // Выбиралка с фигурами
        ChoiceBox<String> shapes = new ChoiceBox<>();
        shapes.getItems().addAll("Сфера", "Коробка", "Цилиндр", "Октаэдр", "Плоскость");

        VBox vbox = new VBox(20, label,
                new HBox(40, shapeLabel, shapes),
                new HBox(20, xLabel, xPosition),
                new HBox(20, yLabel, yPosition),
                new HBox(20, zLabel, zPosition),
                addButton);
        vbox.setPadding(new Insets(30));
        vbox.setAlignment(Pos.TOP_CENTER);


        // Тут вызываются разные сцены с полями по необходимой фигуре, которую мы выбрали ранее
        shapes.getSelectionModel().selectedIndexProperty().addListener((source, o, n) -> {
            vbox.getChildren().clear();
            if (n.equals(0)) {
                // сфера
                vbox.getChildren().addAll(label,
                        new HBox(40, shapeLabel, shapes),
                        new HBox(20, xLabel, xPosition),
                        new HBox(20, yLabel, yPosition),
                        new HBox(20, zLabel, zPosition),
                        new HBox(37, radiusLabel, shapeRadius),
                        addButton);
            }
            else if (n.equals(1)) {
                // коробка
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
                // Цилиндр
                vbox.getChildren().addAll(label,
                        new HBox(40, shapeLabel, shapes),
                        new HBox(20, xLabel, xPosition),
                        new HBox(20, yLabel, yPosition),
                        new HBox(20, zLabel, zPosition),
                        new HBox(37, radiusLabel, shapeRadius),
                        new HBox(37, heightLabel, shapeHeight),
                        addButton);
            }
            else if (n.equals(3)){
                // Октаэдр
                vbox.getChildren().addAll(label,
                        new HBox(40, shapeLabel, shapes),
                        new HBox(20, xLabel, xPosition),
                        new HBox(20, yLabel, yPosition),
                        new HBox(20, zLabel, zPosition),
                        new HBox(37, radiusLabel, shapeRadius),
                        addButton);
            }
            else if (n.equals(4)) {
                // Плоскость
                vbox.getChildren().addAll(label,
                        new HBox(40, shapeLabel, shapes),
                        new HBox(20, xLabel, xPosition),
                        new HBox(20, yLabel, yPosition),
                        new HBox(20, zLabel, zPosition),
                        new HBox(41, widthLabel, shapeWidth),
                        new HBox(41, depthLabel, shapeDepth),
                        addButton);
            }
            vbox.setPadding(new Insets(30));
            vbox.setAlignment(Pos.TOP_CENTER);
        }
        );

        // Подтверждение о добавлении выбранной фигуры
        addButton.setOnAction(e -> {
            int selectedShape = shapes.getSelectionModel().getSelectedIndex();

            double x,y,z;

            // Если поля пустые, то значению присваивается 0, иначе - передаются введенные координаты
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

            if (selectedShape == 0) {
                // Сфера
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

            }
            else if (selectedShape == 1) {
                // Коробка
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

            }
            else if (selectedShape == 2) {
                // Цилиндр
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

            }
            else if (selectedShape == 3){
                // Октаэдр
                if(shapeRadius.getText().equals("")) {
                    showFormNotCompleteAlert();
                    return;
                }
                radius = Double.parseDouble(shapeRadius.getText());

                if(radius == 0) {
                    showFormNotCompleteAlert();
                    return;
                }

                Sphere sphere = new Sphere(radius, 1);
                translate(sphere, x, y, z);
                pane.getChildren().add(sphere);
                changeProperties(sphere);
                stage.close();
            }
            else if (selectedShape == 4) {
                // Плоскость
                if (shapeWidth.getText().equals("") || shapeDepth.getText().equals("")) {
                    showFormNotCompleteAlert();
                    return;
                }
                width = Double.parseDouble(shapeWidth.getText());
                height = 0.1;
                depth = Double.parseDouble(shapeDepth.getText());
                if (width == 0 || height == 0 || depth == 0) {
                    showFormNotCompleteAlert();
                    return;
                }
                Box box = new Box(width, height, depth);
                translate(box, x, y, z);
                pane.getChildren().add(box);
                changeProperties(box);
                stage.close();
            }

            else {
                // Если не выбрана ни одна фигура
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ОШИБКА");
                alert.setHeaderText("Не выбрана ни одна фигура");
                alert.setContentText("Выберете одну из фигур");
                alert.show();
                return;
            }
            translateXTextField.setText("");
            translateYTextField.setText("");
            translateZTextField.setText("");
        });

        Scene scene = new Scene(vbox, 375, 500);
        stage.setScene(scene);
        stage.setTitle("Добавить фигуру");
        stage.show();
    }

    private void showFormNotCompleteAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ОЩЩЩИБКА");
        alert.setHeaderText("Введи данные");
        alert.setContentText("Заполните форму и убедитесь, что параметры фигуры не в 0.");
        alert.show();
    }

    private void changeProperties(Shape3D shape) {

        /********* Выбор фигуры в сцене *********/
        shape.setOnMousePressed(e -> {

            // Если эта фигура уже выбрана, то снять выделение
            if(shape == selectedShape) {
                shape.setMaterial(selectedShapeMaterial);

                selectedShape = null;
                selectedShapeMaterial = null;

                disableControls(true);
                return;
            }

            if(selectedShape != null)
                selectedShape.setMaterial(selectedShapeMaterial);

            selectedShape = shape;
            selectedShapeMaterial = shape.getMaterial();
            disableControls(false);
            shape.setMaterial(new PhongMaterial(Color.LIGHTBLUE));
        });
    }

    /*
    Данная функция отключает боковое меню
    Если фигура не выбрана, то меню не активно, иначе - активно
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
        deleteButton.setDisable(b);
        colorPicker.setDisable(b);
    }


    private void translate(Shape3D shape, double x, double y, double z) {

        // Если фигура выходит за границу, то он доходит до края сцены и прекращает перемещение
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

    // Функция для изменения масштаба фигуры,
    // где factor - значение ползунка насколько надо изменить размер фигуры
    private void scale(Shape3D shape, double factor) {
        shape.setScaleX(shape.getScaleX() + factor);
        shape.setScaleY(shape.getScaleY() + factor);
        shape.setScaleZ(shape.getScaleZ() + factor);
    }

    // Функция для сохранения файла - такого не будет
    public void save() {}

    // Функция для загрузки файла - это не работает)
    public void load() {}

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

    // Правила для заполнения поля для ввода данных
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