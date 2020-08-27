package cn.hyg.client.view;

import cn.hyg.client.config.Constants;
import cn.hyg.client.job.JobInstance;
import cn.hyg.client.job.JobThreadPool;
import cn.hyg.client.model.Task;
import cn.hyg.client.repository.ConfigRepo;
import cn.hyg.client.repository.TaskRepo;
import cn.hyg.client.util.DlgUtil;
import cn.hyg.client.util.FileUtil;
import cn.hyg.client.util.ShellUtil;
import cn.hyg.client.util.StringHelper;
import cn.hyg.client.value.TaskType;
import cn.hyg.client.model.FileInfo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


public class TaskView extends BorderPane {

    private Stage prmStg;
    private VBox listView = new VBox();

    private int pageCount;
    private int pageIdx;
    private int pageSize;
    private int state;
    private TaskType tt;
    private String outputDir;
    private String fastaDir;
    private String recentInputDir;
    private List<Task> dataList;
    private HashSet<Task> deleteList = new HashSet<Task>();
    private ObservableList<Task> Tasks;
    private boolean isSelectedAll;

    public TaskView(TaskType tt, Stage primaryStage) {
        this.tt = tt;
        this.prmStg = primaryStage;
        this.state = -1;
        // 初始化视图
        //System.out.println(tt);

        initView();
    }

    private void initView() {
        this.setMinWidth(900);
        this.setMinHeight(800);
        this.setPadding(new Insets(10, 10, 5, 10));

        ConfigRepo cr = new ConfigRepo();
        outputDir = cr.getTargetDir("output_dir");
        if (StringUtils.isBlank(outputDir)) {
            outputDir = System.getProperty("user.home");
        }
        //fastaDir = outputDir + File.separator + "fasta" + File.separator + ;

        //fastaDir = outputDir + File.separator + "fasta" + File.separator + StringHelper.getDate();
        fastaDir = StringHelper.makeFastaDir(fastaDir,outputDir);
        //System.out.println(fastaDir);
        recentInputDir = cr.getTargetDir("input_dir");
        if (StringUtils.isBlank(recentInputDir)) {
            recentInputDir = System.getProperty("user.home");
        }
        BorderPane topView = initTop();
        BorderPane bottomView = initBottom();
        initList();
        this.setTop(topView);
        this.setCenter(listView);
        this.setBottom(bottomView);
    }

    private BorderPane initTop() {
        BorderPane topView = new BorderPane();
        HBox rightBox = new HBox();
        TaskRepo taskRepo = new TaskRepo();
        Button deleteButton = new Button("删除数据");
        deleteButton.setId("DeleteButton");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(deleteList.size() == 0){
                    final Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    BorderPane borderPaneLayout = new BorderPane();
                    // 宽度和高度
                    borderPaneLayout.setMinSize(300, 150);
                    Button confirm = new Button("确定");
                    confirm.setId("ConButton");
                    confirm.setOnAction(event1 -> stage.close());
                    HBox hBox = new HBox();
                    hBox.setSpacing(20);
                    hBox.getChildren().addAll(confirm);
                    hBox.setAlignment(Pos.CENTER);
                    VBox vBox = new VBox();
                    Text text = new Text("请选择要删除的文件");
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
                    stage.setScene(scene);
                    stage.show();
                }
                else {
                    final Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    BorderPane borderPaneLayout = new BorderPane();
                    // 宽度和高度
                    borderPaneLayout.setMinSize(300, 150);
                    Button confirm = new Button("是");
                    confirm.setId("ConfirmButton");
                    Button cancel = new Button("否");
                    cancel.setId("CancelButton");
                    confirm.setOnAction(event12 -> {
                        for (Task task : deleteList) {
                            Tasks.remove(task);
                            taskRepo.setDelete(task.getSerial(), 1);
                        }
                        deleteList = new HashSet<>();
                        isSelectedAll = false;
                        initList();
                        TaskView.super.setTop(initTop());
                        stage.close();
                    });
                    cancel.setOnAction(event13 -> stage.close());
                    HBox hBox = new HBox();
                    hBox.setSpacing(20);
                    hBox.getChildren().addAll(confirm, cancel);
                    hBox.setAlignment(Pos.CENTER);

                    VBox vBox = new VBox();
                    Text text = new Text("是否要删除文件?");
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
                    stage.setScene(scene);
                    stage.show();
                }
            }
        });

        if(this.tt == TaskType.ASM){
            Button buttonFasta = new Button("查看拼接结果");
            buttonFasta.setId("FastaButton");
            // 点击
            buttonFasta.setOnAction(event -> {

                if (StringUtils.isBlank(fastaDir)) {
                    return;
                }
                fastaDir = StringHelper.makeFastaDir(fastaDir,outputDir);
                File f = new File(fastaDir);
                if (!f.exists() || !f.isDirectory()) {
                    if (!f.mkdir()) {
                        return;
                    }
                }
                FileUtil.openDir(fastaDir);
            });
            rightBox.getChildren().add(buttonFasta);
        }

        Button buttonSelect = new Button("选择文件");
        buttonSelect.setId("SelectButton");
        // 点击
        buttonSelect.setOnAction(event -> {
            FileChooser fc = new FileChooser();
            fc.setTitle(Constants.DLG_UPLOAD_TITLE);
            fc.setInitialDirectory(new File(recentInputDir));
            fc.setInitialFileName("");
            // 数据格式可以为.fq 或者.fq.gz
            switch (tt) {
                case QC:
                case ASM:
                    fc.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("fq文件", "*.fq", "*.fq.gz", "*fq.tar.gz","*.fastq.gz","*fastq.tar.gz")
                    );
                    break;
                case PRD:
                case ANT:
                    fc.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("fasta文件", "*.fa", "*.fas", "*.fasta")
                    );
                    break;
            }

            List<File> fileList = fc.showOpenMultipleDialog(prmStg);
            if (fileList == null || fileList.size() == 0) {
                return;
            }

            List<String> filenameList = justifyInput(fileList);
            if (filenameList.size() == 0) {
                return;
            }

            String inputDir = fileList.get(0).getParent() + File.separator;

            final TableView table = new TableView();
            final Stage stage = new Stage();
            final Scene scene = new Scene(new Group());
            stage.setTitle("选中的文件");
            stage.setWidth(500);
            stage.setHeight(520);
            // 自定义样式
            URL url = getClass().getClassLoader().getResource("styles/self.css");
            if (url != null) {
                String urlStr = url.toExternalForm();
                scene.getStylesheets().add(urlStr);
            }
            table.setId("FileChooseTable");
            final Label label = new Label("文件列表");
            label.setFont(new Font("Arial", 16));
            table.setEditable(false);
            TableColumn fileNameCol = new TableColumn("文件名");
            fileNameCol.setMinWidth(450);
            fileNameCol.setCellValueFactory(
                    new PropertyValueFactory<FileInfo, String>("name")
            );
            table.getColumns().addAll(fileNameCol);
            ObservableList<FileInfo> data = FXCollections.observableArrayList();

            table.setItems(data);
            for (String file : filenameList) {
                FileInfo fileInfo = new FileInfo();
                String name = file;
                if (file.contains(",")) {
                    name = file.split(",")[0] + "\n" + file.split(",")[1];
                }
                fileInfo.setName(name);
                data.add(fileInfo);
            }
            Button confirmButton = new Button("确定");
            confirmButton.setId("ConfirmButton");
            Button cancelButton = new Button("取消");
            cancelButton.setId("CancelButton");
            confirmButton.setOnAction((ActionEvent e) -> {
                // 提交任务
                for (String filenames : filenameList) {
                    JobInstance ji = new JobInstance(filenames, inputDir, outputDir, tt);
                    if(tt.equals(TaskType.ASM)){
                        fastaDir = StringHelper.makeFastaDir(fastaDir,outputDir);
                        ji.setFastaDir(fastaDir);
                    }
                    ji.init();
                    JobThreadPool.getInstance().addTask(ji, this);
                    initList();
                }
                initList();
                stage.close();
            });
            cancelButton.setOnAction((ActionEvent e) -> stage.close());

            HBox hb = new HBox();
            hb.getChildren().addAll(confirmButton, cancelButton);
            hb.setSpacing(3);
            final VBox vbox = new VBox();
            vbox.setSpacing(5);
            vbox.setPadding(new Insets(10, 0, 0, 10));
            vbox.getChildren().addAll(label, table, hb);
            ((Group) scene.getRoot()).getChildren().addAll(vbox);
            stage.setScene(scene);
            stage.show();
        });
        rightBox.getChildren().add(buttonSelect);
        rightBox.getChildren().add(deleteButton);

        RadioButton buttonAll = new RadioButton("全部");
        buttonAll.setUserData(-1);
        RadioButton buttonCompleted = new RadioButton("已完成");
        buttonCompleted.setUserData(60);
        RadioButton buttonUncompleted = new RadioButton("正在运行");
        buttonUncompleted.setUserData(11);
        RadioButton buttonException = new RadioButton("异常");
        buttonException.setUserData(404);
        RadioButton buttonQCException = null;
        if(tt.equals(TaskType.ASM)){
            buttonQCException = new RadioButton("质控未通过");
            buttonQCException.setUserData(61);
        }

        ToggleGroup group = new ToggleGroup();
        group.getToggles().add(buttonAll);
        group.getToggles().add(buttonCompleted);
        group.getToggles().add(buttonUncompleted);
        group.getToggles().add(buttonException);
        if(tt.equals(TaskType.ASM)){
            group.getToggles().add(buttonQCException);
        }
        group.selectToggle(buttonAll);

        group.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> onToggleChange(new_toggle));

        HBox leftBox = new HBox();
        leftBox.getChildren().add(buttonAll);
        leftBox.getChildren().add(buttonCompleted);
        leftBox.getChildren().add(buttonUncompleted);
        leftBox.getChildren().add(buttonException);
        if(tt.equals(TaskType.ASM)){
            leftBox.getChildren().add(buttonQCException);
        }
        leftBox.setSpacing(5);
        leftBox.setAlignment(Pos.CENTER);



        HBox selectBox = new HBox();
        CheckBox checkBox = new CheckBox("全选");
        checkBox.setId("SelectAllBox");
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!checkBox.isSelected()){
                    isSelectedAll = false;
                    deleteList.clear();
                }
                else{
                    isSelectedAll = true;
                    deleteList.addAll(dataList);
                }
                initList();
            }
        });
        selectBox.getChildren().addAll(checkBox);


        topView.setRight(rightBox);
        topView.setLeft(leftBox);
        topView.setBottom(selectBox);

        return topView;
    }

    private BorderPane initBottom() {

        String str = "当前输出目录：" + outputDir;
        Label label = new Label(str);

        BorderPane bottomView = new BorderPane();
        bottomView.setLeft(label);

        Button buttonSet = new Button("设置");
        buttonSet.setId("SetButton");
        // 点击
        buttonSet.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File f = directoryChooser.showDialog(prmStg);
            if (f == null) {
                return;
            }
            outputDir = f.getAbsolutePath();
            //fastaDir = outputDir + File.separator + "fasta" + File.separator + StringHelper.getDate();
            fastaDir = StringHelper.makeFastaDir(fastaDir,outputDir);
            ConfigRepo cr = new ConfigRepo();
            cr.setTargetDir(outputDir, "output_dir");
            Label newLabel = new Label("当前输出目录：" + outputDir);
            bottomView.setLeft(newLabel);
        });
        bottomView.setRight(buttonSet);

        return bottomView;
    }

    Label tipLabel = new Label("");
    HBox tipHbox = new HBox();
    public void initList() {
        tipHbox.getChildren().remove(tipLabel);
        listView.getChildren().remove(tipHbox);
        this.pageIdx = 0;
        this.pageSize = 10;
        int totalCount = refreshData(pageIdx, pageSize, state);
        //TableView<Task> listTable = refreshTable(dataList);
        BorderPane tableWithPagination = initTableWithPagination(pageCount, pageIdx);
        if (listView.getChildren().size() > 0) {
            listView.getChildren().remove(0);
        }
        String tip;
        if(totalCount <= 10){
            tip = "共有" + totalCount + "条数据,无分页";
        }
        else{
            tip = "共有" + totalCount + "条数据,每页" + pageSize + "条";
        }
        tipLabel.setText(tip);
        tipHbox.setAlignment(Pos.CENTER);


        tipHbox.getChildren().addAll(tipLabel);
        listView.getChildren().addAll(tableWithPagination,tipHbox);
        VBox.setVgrow(tableWithPagination, Priority.ALWAYS);
        //VBox.setVgrow(label,Priority.ALWAYS);
    }

    private int refreshData(int pageIdx, int pageSize, int state) {
        TaskRepo tr = new TaskRepo();

        int totalCount = tr.count(state, tt.name());
        if (totalCount % pageSize > 0) {
            this.pageCount = totalCount / pageSize + 1;
        } else {
            this.pageCount = totalCount / pageSize;
        }
        this.dataList = tr.queryList(pageIdx, pageSize, state, tt.name());
        return totalCount;
    }

    private BorderPane initTableWithPagination(int pageCount, int pageIdx) {

        /*Button btnFirst = new Button("首页");
        Button btnLast = new Button("尾页");*/
        if (pageCount == 0) {
            pageCount = 1;
            pageIdx = 1;
        }
        Pagination page = new Pagination(pageCount, pageIdx);
        page.setPageFactory(this::onPageChange);
        page.setMaxPageIndicatorCount(5);

        HBox pageBox = new HBox();
        pageBox.setPadding(new Insets(10, 5, 5, 5));
        pageBox.getChildren().add(page);

        /*HBox pageView = new HBox();
        pageView.setAlignment(Pos.CENTER);
        pageView.setSpacing(10);
        //pageView.getChildren().addAll(btnFirst, pageBox, btnLast);
        pageView.getChildren().addAll(pageBox);*/
        HBox.setHgrow(page, Priority.ALWAYS);

        /*HBox pageIllustrationView = new HBox();
        pageIllustrationView.getChildren().add(selection);
        pageIllustrationView.setAlignment(Pos.CENTER);*/

        BorderPane pagination = new BorderPane();
        //pagination.setLeft(pageIllustrationView);
        pagination.setCenter(page);
        return pagination;
    }

    private TableView<Task> refreshTable(List<Task> dataList) {

        TableView<Task> table = new TableView<>();
        table.setId("MainTable");
        //TableColumn<Task, String> serial = new TableColumn<>("序号");
        TableColumn<Task,String> selected = new TableColumn<>("复选");
        TableColumn<Task, String> filenames = new TableColumn<>("文件");
        TableColumn<Task, String> status = new TableColumn<>("状态");
        TableColumn<Task, String> submitTime = new TableColumn<>("提交时间");
        TableColumn<Task, String> finishTime = new TableColumn<>("完成时间");
        TableColumn<Task,String> opertaion = new TableColumn<>("操作选项");

        selected.setMaxWidth(50);
        //serial.setMinWidth(200);
        filenames.setMinWidth(370);
        status.setMinWidth(155);
        submitTime.setMinWidth(150);
        finishTime.setMinWidth(150);
        opertaion.setMinWidth(130);

        selected.setCellFactory(col -> {
            TableCell<Task,String> cell = new TableCell<Task,String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    Task task = (Task) this.getTableRow().getItem();
                    if(task == null){
                        return;
                    }
                    int state = task.getState();
                    if(state == 11){
                        return;
                    }

                    CheckBox checkBox = new CheckBox();

                    if(isSelectedAll && !checkBox.isSelected()){
                        checkBox.setSelected(true);
                    }
                    checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if(!checkBox.isSelected()){
                                //System.out.println(task);
                                //System.out.println(deleteList.contains(task));
                                if(isSelectedAll){
                                    String serial = task.getSerial();
                                    deleteList.removeIf(task1 -> task1.getSerial().equals(serial));
                                    if (deleteList.size() == 0) {
                                        TaskView.super.setTop(initTop());
                                    }
                                }
                                else{
                                    deleteList.remove(task);
                                }
                            }
                            else{
                                //System.out.println(task);
                                //System.out.println("加了");
                                deleteList.add(task);
                                //System.out.println(deleteList);
                            }
                            //System.out.println(deleteList);
                        }
                    });
                    if(isSelectedAll && !checkBox.isSelected()){
                        checkBox.setSelected(true);
                    }
                    this.setGraphic(checkBox);
                }
            };
            return cell;
        });

        //serial.setCellValueFactory(p -> p.getValue().getSerialProperty());
        filenames.setCellValueFactory(p -> p.getValue().getFilesProperty());
        //status.setCellValueFactory(p -> p.getValue().getStatusProperty());
        status.setCellFactory(col -> {
            TableCell<Task,String> cell = new TableCell<Task,String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    Task task = (Task) this.getTableRow().getItem();
                    if(task == null){
                        return;
                    }
                    int state = task.getState();
                    if(state == 61 || state == 404){
                        this.setStyle("-fx-text-fill: red");
                    }else{
                        this.setStyle("-fx-text-fill: black");
                    }
                    this.setText(task.getStateStr());
                }
            };
            return cell;
        });
        submitTime.setCellValueFactory(p -> p.getValue().getSubmitTimeProperty());
        finishTime.setCellValueFactory(p -> p.getValue().getFinishTimeProperty());
        opertaion.setCellFactory(col -> {
            TableCell<Task,String> cell = new TableCell<Task,String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    //initList();
                    super.updateItem(item, empty);
                    Task task = (Task) this.getTableRow().getItem();
                    if(task == null){
                        return;
                    }
                    int state = task.getState();
                    if(state == 11 || state == 403){
                        return;
                    }
                    Button button = new Button("操作");
                    button.setStyle("-fx-text-fill: #FFFFFF");
                    button.setOnAction(event -> {
                        //initList();
                        System.out.println(task);
                        TaskRepo tr = new TaskRepo();
                        String serialNum = task.getSerial();
                        if (state == 10) {
                            CancelTaskMenu.getInstance(serialNum,TaskView.this).show(this, Side.BOTTOM, 0, 0);
                        } else {
                            String resultDir = tr.getResultDir(serialNum);
                            TaskMenu.setTargetDir(resultDir);
                            TaskMenu.setState(state);
                            TaskMenu.setTt(tt);
                            if (tt.equals(TaskType.QC) || tt.equals(TaskType.ASM)) {
                                TaskMenu.setFiles(task.getFiles());
                                TaskMenu.setQcDir(task.getResultDir() + "QC" + File.separator);
                            }

                            System.out.println("[结果目录]: " + TaskMenu.getTargetDir());
                            TaskMenu.getInstance().show(this, Side.BOTTOM, 0, 0);
                        }
                    });
                    this.setGraphic(button);
                }
            };
            return cell;
        });
//        status.setCellFactory(col -> {
//            final TableCell<Task, String> cell = new TableCell<>();
//            cell.textProperty().bind(cell.itemProperty()); // in general might need to subclass TableCell and override updateItem(...) here
//            cell.setOnMouseClicked(event -> {
//                if (event.getButton() == MouseButton.SECONDARY) {
//                    TaskRepo tr = new TaskRepo();
//                    Task task = (Task) cell.getTableRow().getItem();
//                    if (task == null) {
//                        return;
//                    }
//                    String serialNum = task.getSerial();
//                    int state = task.getState();
//                    if (state == 403 || state == 11 || state == 404) {
//                        return;
//                    }
//                    if (state == 10) {
//                        CancelTaskMenu.getInstance(serialNum, this).show(cell, Side.BOTTOM, 0, 0);
//                    } else {
//                        String resultDir = tr.getResultDir(serialNum);
//                        TaskMenu.setTargetDir(resultDir);
//                        TaskMenu.setTt(tt);
//                        if (tt.equals(TaskType.QC)) {
//                            TaskMenu.setFiles(task.getFiles());
//                            TaskMenu.setQcDir(task.getResultDir() + "QC" + File.separator);
//                        }
//                        System.out.println("[结果目录]: " + TaskMenu.getTargetDir());
//                        TaskMenu.getInstance().show(cell, Side.BOTTOM, 0, 0);
//                    }
//                }
//            });
//            return cell;
//        });

        //table.getColumns().addAll(serial, filenames, status, submitTime, finishTime);

        table.getColumns().addAll(selected,filenames, status, submitTime, finishTime,opertaion);
//        if(isSelectedAll){
//            for (TableColumn<Task, ?> column : table.getColumns()) {
//                System.out.println(column.getText());
//            }
//        }
        //System.out.println(dataList);
        Tasks = FXCollections.observableArrayList(dataList);
        table.setItems(Tasks);
        return table;
    }

    private void onToggleChange(Toggle newValue) {
        if (newValue != null) {
            //System.out.println(newValue.getUserData().toString());
            this.state = (Integer) newValue.getUserData();
            //refreshData(pageIdx, pageSize, state);
            initList();
        }
    }

    private Node onPageChange(int pageIdx) {
        this.pageIdx = pageIdx;
        refreshData(pageIdx, pageSize, state);
        return refreshTable(dataList);
    }

    private List<String> justifyInput(List<File> fileList) {

        List<String> result = new ArrayList<>();

        if (fileList == null) {
            // 点击了取消按钮
            return result;
        }

        if (fileList.size() < 1) {
            DlgUtil.showAlert(prmStg, "文件错误", "至少选择1个文件");
            return result;
        }

        // 更新浏览过的文件夹
        ConfigRepo cr = new ConfigRepo();
        recentInputDir = fileList.get(0).getParent();
        cr.setTargetDir(recentInputDir, "input_dir");

        switch (tt) {
            case QC:
            case ASM:
                // 识别所有的菌种名
                Set<String> names = new HashSet<>();
                List<String> nameList = new ArrayList<>();
                for (File f : fileList) {
                    String t = StringHelper.extractStrainName(f.getName());
                    if (StringUtils.isBlank(t)) {
                        continue;
                    }
                    names.add(t);
                    nameList.add(f.getName());
                }
                //System.out.println(names);
                //System.out.println(nameList);
                if (names.size() == 0) {
                    return result;
                }

                for (String n : names) {
                    String s1 = "";
                    String s2 = "";
                    for (String filename : nameList) {
                        String t = FileUtil.getFilenameWithSuffix(filename);

                        if (t.contains(n + ".")) {
                            s1 = filename;

                        }
                        String tmpName = FileUtil.getFilenameWithoutSuffix(filename);
                        //t是文件全名，如果t中有菌种名，而且，t包含了_ 且是以 1或2结尾的，那他们就可以配对为一组
                        if(tmpName.contains("_") && tmpName.contains(n) && (tmpName.endsWith("1") || tmpName.endsWith("2"))){
                            if (StringUtils.isNotBlank(s2)) {
                                s2 += ",";
                            }
                            s2 += filename;
                        }
                    }
                    if (StringUtils.isNotBlank(s1)) {
                        result.add(s1);
                    }

                    if (StringUtils.isNotBlank(s2)) {
                        result.add(s2);

                    }
                }
                break;
            case PRD:
            case ANT:
                for (File f : fileList) {
                    result.add(f.getName());
                }
                break;
            default:
                return result;
        }

        return result;
    }
}

class CancelTaskMenu extends ContextMenu {
    private static CancelTaskMenu INSTANCE = null;

    private CancelTaskMenu() {

    }

    static CancelTaskMenu getInstance(String serial, TaskView taskView) {
        if (INSTANCE == null) {
            INSTANCE = new CancelTaskMenu();
        } else {
            INSTANCE.getItems().removeAll(INSTANCE.getItems());
        }
        MenuItem cancel = new MenuItem("取消");
        cancel.setOnAction(event -> {
            TaskRepo taskRepo = new TaskRepo();
            taskRepo.setState(serial, 403);
            taskView.initList();
        });
        INSTANCE.getItems().add(cancel);
        return INSTANCE;
    }

    private static void initList(TaskView taskView) {
        taskView.initList();
    }
}

class TaskMenu extends ContextMenu {
    /**
     * 单例
     */
    private static TaskMenu INSTANCE = null;
    private static String targetDir;
    private static String files;
    private static String qcDir;
    private static TaskType tt;
    private static int state;
    private static String serial;

    public static String getSerial() {
        return serial;
    }

    public static void setSerial(String serial) {
        TaskMenu.serial = serial;
    }

    /**
     * 私有构造函数
     */
    private TaskMenu() {
    }

    /**
     * 获取实例
     *
     * @return GlobalMenu
     */
    static TaskMenu getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskMenu();
        } else {
            INSTANCE.getItems().removeAll(INSTANCE.getItems());
        }

        MenuItem openResult = null;
        if(state == 404  || state== 61){
            openResult = new MenuItem("查看异常日志");
        }
        else{
            openResult = new MenuItem("打开结果目录");
        }


        openResult.setOnAction(event -> {
            if (StringUtils.isBlank(targetDir)) {
                return;
            }

            File f = new File(targetDir);
            if (!f.exists() || !f.isDirectory()) {
                return;
            }

            FileUtil.openDir(targetDir);

        });

        INSTANCE.getItems().add(openResult);

        if ((tt.equals(TaskType.ASM) && state != 60)  || (tt.equals(TaskType.QC) && state != 404) && StringUtils.isNotBlank(files) ) {

            System.out.println("files:" + files);

            if (files.contains(",")) {
                //System.out.println("走双端");
                // 双端
                //
                String qcFile1 = qcDir + FileUtil.getFilenameWithoutSuffix(files.split(",")[1]) + "_fastqc.html";
                String qcFile2 = qcDir + FileUtil.getFilenameWithoutSuffix(files.split(",")[0]) + "_fastqc.html";
                if(tt.equals(TaskType.ASM)){
                    // qcDir = home/biouser/Sabccssae123134413/QC/
                    // 改为home/biouser/Sabccssae123134413/assembly/QC/
                    String s1 = qcDir.split("QC")[0];
                    qcFile1 = s1 + "assembly" + File.separator +"QC" + File.separator + FileUtil.getFilenameWithoutSuffix(files.split(",")[1]) + "_fastqc.html";
                    qcFile2 =  s1 + "assembly" + File.separator + "QC" + File.separator + FileUtil.getFilenameWithoutSuffix(files.split(",")[0]) + "_fastqc.html";
                }
                System.out.println("qcFile1:" + qcFile1);
                System.out.println("qcFile2:" + qcFile2);
                MenuItem openQc1 = new MenuItem("查看质控1");
                String finalQcFile1 = qcFile1;
                openQc1.setOnAction(event -> {

                    if (StringUtils.isBlank(finalQcFile1)) {
                        System.out.println("qc是空的");
                        return;
                    }

                    File f = new File(finalQcFile1);
                    if (!f.exists() || !f.isFile()) {
                        System.out.println("f不存在或者f不是文件");
                        return;
                    }

                    String cmd[];
                    // 调用资源浏览器打开文件夹
                    if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
                        //cmd = "firefox " + qcFile1;
                        cmd = new String[]{"sh","-c", "nohup firefox " + finalQcFile1 + " > /dev/null 2>&1 &"};
                    } else {
                        cmd = new String[]{ "explorer " , finalQcFile1};
                    }
                    System.out.println(cmd);
                    ShellUtil.runCmd(cmd);
                });

                MenuItem openQc2 = new MenuItem("查看质控2");
                String finalQcFile2 = qcFile2;
                openQc2.setOnAction(event -> {

                    if (StringUtils.isBlank(finalQcFile2)) {
                        return;
                    }

                    File f = new File(finalQcFile2);
                    if (!f.exists() || !f.isFile()) {
                        return;
                    }

                    String cmd[];
                    // 调用资源浏览器打开文件夹
                    if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
                        cmd = new String[]{"sh","-c","nohup firefox " + finalQcFile2 +" > /dev/null 2>&1 &"};
                    } else {
                        cmd = new String[]{"explorer ", finalQcFile2};
                    }
                    //System.out.println(cmd);
                    ShellUtil.runCmd(cmd);
                });

                INSTANCE.getItems().add(openQc1);
                INSTANCE.getItems().add(openQc2);
            } else {
                //System.out.println("走单端");
                // 单端
                String qcFile = qcDir + FileUtil.getFilenameWithoutSuffix(files) + "_fastqc.html";
                if(tt.equals(TaskType.ASM)){
                    String s1 = qcDir.split("QC")[0];
                    qcFile = s1 + "assembly" + File.separator + "QC" + File.separator + FileUtil.getFilenameWithoutSuffix(files) + "_fastqc.html";
                }
                System.out.println("qcFile:" + qcFile);
                MenuItem openQc = new MenuItem("查看质控");
                String finalQcFile = qcFile;
                openQc.setOnAction(event -> {

                    if (StringUtils.isBlank(finalQcFile)) {
                        System.out.println("qc是空的");
                        return;
                    }

                    File f = new File(finalQcFile);
                    //System.out.println();
                    if (!f.exists() || !f.isFile()) {
                        System.out.println("f不存在或者f不是文件");
                        return;
                    }

                    String cmd[];
                    // 调用资源浏览器打开文件夹
                    if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
                        //cmd = "firefox " + qcFile;
                        cmd = new String[]{"sh","-c","nohup firefox " + finalQcFile +" > /dev/null 2>&1 &"};
                        //System.out.println(cmd);
                    } else {
                        cmd = new String[]{"explorer ", finalQcFile};
                        //System.out.println(cmd);
                    }
                    ShellUtil.runCmd(cmd);
                });
                INSTANCE.getItems().add(openQc);
            }
        }
        return INSTANCE;
    }

    static void setTargetDir(String targetDir) {
        TaskMenu.targetDir = targetDir;
    }

    static String getTargetDir() {
        return targetDir;
    }

    public static void setFiles(String files) {
        TaskMenu.files = files;
    }

    public static void setQcDir(String qcDir) {
        TaskMenu.qcDir = qcDir;
    }

    static void setTt(TaskType tt) {
        TaskMenu.tt = tt;
    }

    public static int getState() {
        return state;
    }

    public static void setState(int state) {
        TaskMenu.state = state;
    }
}