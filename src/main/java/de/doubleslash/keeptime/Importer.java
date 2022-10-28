package de.doubleslash.keeptime;

import javafx.scene.control.Alert;

import org.h2.tools.RunScript;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.SQLException;


public class Importer {

    public static void main(String[] args) throws SQLException {

        String url=args[0];
        String username=args[1];
        String password=args[2];
        String file=args[3];
        RunScript.execute(url, username, password, file, Charset.defaultCharset(), true);




        Alert informationDialog = new Alert(Alert.AlertType.INFORMATION);
        informationDialog.setTitle("Import done");
        informationDialog.setHeaderText("The data was imported.");
        informationDialog.setContentText("KeepTime will now be CLOSED! You have to RESTART it again to see the changes");
        informationDialog.showAndWait();

    }

}
