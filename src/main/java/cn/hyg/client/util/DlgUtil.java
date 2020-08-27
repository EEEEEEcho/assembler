package cn.hyg.client.util;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class DlgUtil {

    public static void showAlert(Stage prmStg, String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(prmStg);
        alert.titleProperty().set(title);
        alert.headerTextProperty().set(msg);
        alert.showAndWait();
    }

    /*public void showInfo(Stage prmStg) {

        Stage dialogStage = new Stage();

        dialogStage.initOwner(prmStg);

        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setWidth(width);
        dialogStage.setHeight(height);
        dialogStage.setX(windowsPosition.getX() + width/2);
        dialogStage.setY(windowsPosition.getY() + height/2);
        dialogStage.setResizable(false);

        BorderPane borderPaneLayout = new BorderPane();
        borderPaneLayout.getStyleClass().add("root");

        Button confirm = myStyleButton.getShadowButton("是(Y)", ImageUtil.getImageView("image/ok.png"));
        Button cancel = myStyleButton.getShadowButton("否(N)", ImageUtil.getImageView("image/close.png"));

        confirm.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                dialogStage.close();
                primaryStage.close();
            }
        });

        cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                dialogStage.close();
            }
        });

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.getChildren().addAll(confirm,cancel);
        hBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox();
        Text text = new Text(Constant.CLOSE_PAGE_TITLE);
        vBox.setSpacing(40);
        vBox.getChildren().addAll(text,hBox);
        vBox.setAlignment(Pos.CENTER);

        borderPaneLayout.setCenter(vBox);

        Scene scene = new Scene(borderPaneLayout);
        dialogStage.setTitle(Constant.SOFTWARE_TITLE);
        dialogStage.setScene(scene);

        logger.info("WindowsClosePage UI");
    }*/

}
