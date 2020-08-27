package cn.hyg.client;

import cn.hyg.client.config.Constants;
import cn.hyg.client.job.JobInstance;
import cn.hyg.client.job.JobThreadPool;
import cn.hyg.client.model.Task;
import cn.hyg.client.repository.TaskRepo;
import cn.hyg.client.value.MenuEnum;
import cn.hyg.client.value.TaskType;
import cn.hyg.client.view.TaskView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


public class AppMain extends Application {

    private static final int SINGLE_INSTANCE_LISTENER_PORT = 61923;
    private static final String SINGLE_INSTANCE_FOCUS_MESSAGE = "focus";
    // We define a pause before focusing on an existing instance
    // because sometimes the command line or window launching the instance
    // might take focus back after the second instance execution complete
    // so we introduce a slight delay before focusing on the original window
    // so that the original window can retain focus.
    private static final int FOCUS_REQUEST_PAUSE_MILLIS = 500;

    private static final String instanceId = UUID.randomUUID().toString();

    private Stage primaryStage;
    private TabPane tabPane;
    private VBox tabView;
    private TaskView taskView;
    /*private SplitPane mainSplitPane;
    // 左侧面板
    private Parent leftView;
    // 右侧面板
    private VBox rightView = new VBox();*/

    public void start(Stage primaryStage) {

        recoverTask();

        Platform.setImplicitExit(false);

        Button minimize = new Button("Minimize");
        minimize.setOnAction(event -> primaryStage.setIconified(true));

        Button hide = new Button("Hide");
        hide.setOnAction(event -> primaryStage.hide());

        Button exit = new Button("Exit");
        exit.setOnAction(event -> Platform.exit());

        primaryStage.setTitle(Constants.PRJ_TITLE);
        tabPane = initTabView(primaryStage);
        //initSplitView(primaryStage);

        //Scene scene = new Scene(mainSplitPane, 900, 600);
        Scene scene = new Scene(tabPane, 900, 860);

        // jMetro样式
        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        // 自定义样式
        URL url = getClass().getClassLoader().getResource("styles/self.css");
        if (url != null) {
            String urlStr = url.toExternalForm();
            scene.getStylesheets().add(urlStr);
        }

        //Application.setUserAgentStylesheet(STYLESHEET_MODENA);

        primaryStage.setOnCloseRequest(new WindowCloseEvent(primaryStage));

        primaryStage.setScene(scene);
//        primaryStage.setMinWidth(900);
//        primaryStage.setMinHeight(550);
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(680);
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("images/logo.jpg").toString()));
        primaryStage.show();
        this.primaryStage = primaryStage;

    }

    public void init() {

        CountDownLatch instanceCheckLatch = new CountDownLatch(1);

        Thread instanceListener = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(SINGLE_INSTANCE_LISTENER_PORT, 10)) {
                instanceCheckLatch.countDown();

                while (true) {
                    try (
                            Socket clientSocket = serverSocket.accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                    ) {
                        String input = in.readLine();
                        System.out.println("Received single instance listener message: " + input);
                        if (input.startsWith(SINGLE_INSTANCE_FOCUS_MESSAGE) && primaryStage != null) {
                            Thread.sleep(FOCUS_REQUEST_PAUSE_MILLIS);
                            Platform.runLater(() -> {
                                System.out.println("To front: " + instanceId);
                                primaryStage.setIconified(false);
                                primaryStage.show();
                                primaryStage.toFront();
                            });
                        }
                    } catch (IOException e) {
                        System.out.println("Single instance listener unable to process focus message from client");
                        e.printStackTrace();
                    }
                }
            } catch (java.net.BindException b) {
                System.out.println("SingleInstanceApp already running");

                try (
                        Socket clientSocket = new Socket(InetAddress.getLocalHost(), SINGLE_INSTANCE_LISTENER_PORT);
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
                ) {
                    System.out.println("Requesting existing app to focus");
                    out.println(SINGLE_INSTANCE_FOCUS_MESSAGE + " requested by " + instanceId);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Aborting execution for instance " + instanceId);
                Platform.exit();
            } catch (Exception e) {
                System.out.println(e.toString());
            } finally {
                instanceCheckLatch.countDown();
            }
        }, "instance-listener");
        instanceListener.setDaemon(true);
        instanceListener.start();

        try {
            instanceCheckLatch.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public void stop() {
        System.out.println("Exiting instance " + instanceId);
    }

    private TabPane initTabView(Stage primaryStage) {

        List<String> tabs = new ArrayList<>();
        for (TaskType tt : TaskType.values()) {
            tabs.add(tt.getValue());
        }

        TabPane tp = new TabPane();

        for (String t : tabs) {
            Tab tab = new Tab(t);
            tab.setClosable(false);
            VBox tabView = new VBox();
            taskView = new TaskView(TaskType.getByValue(t), primaryStage);
            tabView.getChildren().add(taskView);
            //rightView.getChildren().add(node);
            tab.setContent(tabView);
            tp.getTabs().add(tab);
        }

        return tp;

    }

    /*private void initSplitView(Stage primaryStage) {
        initLeftView(primaryStage);
        initRightView();
        mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);
        mainSplitPane.getItems().add(leftView);
        mainSplitPane.getItems().add(rightView);
        mainSplitPane.setDividerPositions(0.2f, 0.8f);
    }

    // 初始化左侧的视图
    private void initLeftView(Stage primaryStage) {

        ListView<String> menu = initMenuView(primaryStage);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(5, 0, 5, 0));
        vBox.setSpacing(5);
        vBox.getChildren().add(menu);

        String str = "当前版本：" + Constants.VERSION;
        Label label = new Label(str);
        //Text text = new Text(str);
        vBox.getChildren().add(label);
        VBox.setVgrow(menu, Priority.ALWAYS);

        leftView = vBox;

    }

    private ListView<String> initMenuView(Stage primaryStage) {

        ListView<String> menu = new ListView<>();

        ObservableList<String> data = FXCollections.observableArrayList();

        // 菜单列表
        for (int i = 0; i < MenuEnum.values().length; i++) {
            String m = MenuEnum.valueOf(i).value;
            data.add(m);
        }

        // 鼠标点击事件监听
        menu.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val,
                 String new_val) -> onLeftMenuClick(MenuEnum.getByValue(new_val), primaryStage));

        menu.getItems().addAll(data);
        menu.setMinWidth(200);
        menu.setPrefWidth(200);

        if (menu.getItems().size() > 0) {
            menu.getSelectionModel().select(0);
        }

        return menu;
    }

    private void onLeftMenuClick(MenuEnum menuName, Stage primaryStage) {
        //int index = data.indexOf(menuName);
        if (rightView.getChildren().size() > 0) {
            rightView.getChildren().remove(0);
        }
        Node node = updateDataView(menuName, primaryStage);
        if (node != null) {
            rightView.getChildren().add(node);
            VBox.setVgrow(node, Priority.ALWAYS);
        }
    }

    private Node updateDataView(MenuEnum menuName, Stage primaryStage) {

        switch (menuName) {
            case TASK:
                return new TaskView(TaskType.QC, primaryStage);
            default:
                return null;
        }
    }

    // 初始化右侧的视图
    private void initRightView() {
        rightView.setAlignment(Pos.CENTER);
    }*/

    private void onTabChange(TaskType tt, Stage primaryStage) {
        //int index = data.indexOf(menuName);
        if (tabView.getChildren().size() > 0) {
            tabView.getChildren().remove(0);
        }
        Node node = new TaskView(tt, primaryStage);
        if (node != null) {
            tabView.getChildren().add(node);
            VBox.setVgrow(node, Priority.ALWAYS);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void recoverTask() {
        TaskRepo tr = new TaskRepo();

        // 获取所有正在运行的程序的列表
        List<Task> tl = tr.listAllByState(11, "");
        if (tl != null && tl.size() > 0) {
            for (Task t : tl) {
                // 标记作业异常结束
                tr.finishTask(t.getSerial(), "", 404);
            }
        }

        // 获取所有正在排队的程序的列表
        tl = tr.listAllByState(10, "");
        if (tl != null && tl.size() > 0) {
            // 重新提交并排队
            for (Task t : tl) {
                if (StringUtils.isBlank(t.getFiles()) || StringUtils.isBlank(t.getResultDir())) {
                    // 标记作业异常结束
                    System.out.println("标记作业异常结束：" + t.getId());
                    tr.finishTask(t.getSerial(), "", 404);
                    continue;
                }
                System.out.println("恢复作业：" + t.getId());
                JobInstance ji = new JobInstance(t.getFiles(), t.getPath(), t.getResultDir(), TaskType.valueOf(t.getType()));
                ji.setTask(t);
                ji.setRerun(true);
                ji.init();
                JobThreadPool.getInstance().addTask(ji, taskView);
            }
        }
    }
}

class WindowCloseEvent implements EventHandler<WindowEvent> {

    private Stage stage;

    public WindowCloseEvent(Stage stage) {
        this.stage = stage;
    }

    public void handle(WindowEvent event) {

        event.consume();

        WindowsCloseStage WindowsCloseStage = new WindowsCloseStage(stage);
        WindowsCloseStage.activateDlg();
    }
}

class WindowsCloseStage {

    //private static final Logger logger = Logger.getLogger(WindowsCloseStage.class);

    private Stage dialogStage;
    private Stage primaryStage;

    public WindowsCloseStage(Stage stage) {

        /*MyStyleButton myStyleButton = MyStyleButton.getInstance();

        WindowsPositionObject windowsPosition = WindowsPositionObject.getInstance();
        double width = windowsPosition.getLength()/2;
        double height = windowsPosition.getWidth()/2;*/

        dialogStage = new Stage();
        primaryStage = stage;

        dialogStage.initOwner(primaryStage);

        dialogStage.initModality(Modality.WINDOW_MODAL);
        /*dialogStage.setWidth(width);
        dialogStage.setHeight(height);
        dialogStage.setX(windowsPosition.getX() + width/2);
        dialogStage.setY(windowsPosition.getY() + height/2);*/
        dialogStage.setResizable(false);

        BorderPane borderPaneLayout = new BorderPane();
        // 宽度和高度
        borderPaneLayout.setMinSize(300, 150);
        borderPaneLayout.getStyleClass().add("root");

        /*Button confirm = myStyleButton.getShadowButton("是(Y)", ImageUtil.getImageView("image/ok.png"));
        Button cancel = myStyleButton.getShadowButton("否(N)", ImageUtil.getImageView("image/close.png"));*/

        Button confirm = new Button("是(Y)");
        confirm.setId("ConfirmButton");
        Button cancel = new Button("否(N)");
        cancel.setId("CancelButton");
        confirm.setOnMouseClicked(event -> {
            dialogStage.close();
            primaryStage.hide();
        });

        cancel.setOnMouseClicked(event -> dialogStage.close());

        HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.getChildren().addAll(confirm, cancel);
        hBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox();
        Text text = new Text(Constants.DLG_CLOSE_TEXT);
        vBox.setSpacing(40);
        vBox.getChildren().addAll(text, hBox);
        vBox.setAlignment(Pos.CENTER);

        borderPaneLayout.setCenter(vBox);

        Scene scene = new Scene(borderPaneLayout);
        URL url = getClass().getClassLoader().getResource("styles/self.css");
        if (url != null) {
            String urlStr = url.toExternalForm();
            scene.getStylesheets().add(urlStr);
        }

        dialogStage.setTitle(Constants.DLG_CLOSE_WINDOW);
        dialogStage.setScene(scene);


        //logger.info("WindowsCloseStage UI");
    }

    void activateDlg() {
        dialogStage.show();
    }
}