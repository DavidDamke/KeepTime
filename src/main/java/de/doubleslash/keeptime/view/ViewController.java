// Copyright 2019 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.common.ColorHelper;
import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.FontProvider;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.exceptions.FXMLLoaderException;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.view.time.Interval;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Component
public class ViewController {

   private static final int PREF_HEIGHT_PROJECT_LIST_VIEW = 142;

   private class Delta {
      double x;
      double y;
   }

   private static final Logger LOG = LoggerFactory.getLogger(ViewController.class);

   private static final String TIME_ZERO = "00:00:00";

   private final Delta dragDelta = new Delta();

   private final Canvas taskbarCanvas = new Canvas(32, 32);

   public static final ObjectProperty<Color> fontColorProperty = new SimpleObjectProperty<>();

   private final BooleanProperty mouseHoveringProperty = new SimpleBooleanProperty(false);

   public static final LongProperty activeWorkSecondsProperty = new SimpleLongProperty(0);

   @FXML
   private Pane pane;
   @FXML
   private BorderPane borderPane;

   @FXML
   private ListView<Project> availableProjectsListView;

   @FXML
   private VBox projectsVBox;

   @FXML
   private Label bigTimeLabel;
   @FXML
   private Label allTimeLabel;
   @FXML
   private Label todayAllSeconds;

   @FXML
   private Button minimizeButton;
   @FXML
   private Button closeButton;

   @FXML
   private Button addNewProjectButton;
   @FXML
   private TextField searchTextField;
   @FXML
   private Button settingsButton;
   @FXML
   private Button calendarButton;

   @FXML
   private Canvas canvas;

   @FXML
   private Label currentProjectLabel;

   @FXML
   private TextArea textArea;

   private ColorTimeLine mainColorTimeLine;

   private Stage mainStage;
   private Controller controller;
   private Model model;

   public static String changeStyleAttribute(final String style, final String attribute, final String newValue) {
      String newStyle = "";
      final String newStyleAttribute = "-" + attribute + ": " + newValue + "; ";
      if (style.contains(attribute)) {
         newStyle = style.replaceAll("-" + attribute + ": " + "[^;]+;", newStyleAttribute);
      } else {
         newStyle = style + newStyleAttribute;
      }

      return newStyle;
   }

   public void setController(final Controller controller, final Model model) {
      this.controller = controller;
      this.model = model;

      controller.changeProject(model.getIdleProject(), 0);

      updateProjectView();
   }

   public void setStage(final Stage primaryStage) {
      this.mainStage = primaryStage;
   }

   private Stage reportStage;
   private ReportController reportController;

   private Stage settingsStage;
   private SettingsController settingsController;

   private ProjectsListViewController projectsListViewController;

   @FXML
   private void initialize() {

      availableProjectsListView.setFixedCellSize(13);

      setUpTime();

      setUpTextArea();

      // reposition window if projects are hidden (as anchor is top left)
      mouseHoveringProperty.addListener((a, b, c) -> {
         if (!model.hideProjectsOnMouseExit.get()) {
            setProjectListVisible(true);
            return;
         }

         setProjectListVisible(c);
      });

      minimizeButton.setOnAction(ae -> mainStage.setIconified(true));
      minimizeButton.textFillProperty().bind(fontColorProperty);
      closeButton.setOnAction(ae -> mainStage.close());
      closeButton.textFillProperty().bind(fontColorProperty);

      addNewProjectButton.textFillProperty().bind(fontColorProperty);

      // Add a light to colorize buttons
      // TODO is there a nicer way for this? (see #12)
      final Lighting lighting = new Lighting();
      lighting.lightProperty().bind(Bindings.createObjectBinding(() -> {
         final Color color = fontColorProperty.get();
         return new Light.Distant(45, 45, color);
      }, fontColorProperty));

      settingsButton.setOnAction(ae -> settingsClicked());
      settingsButton.setEffect(lighting);

      calendarButton.setOnAction(ae -> calendarClicked());
      calendarButton.setEffect(lighting);

      final Runnable updateMainBackgroundColor = this::runUpdateMainBackgroundColor;

      mouseHoveringProperty.addListener((a, b, c) -> updateMainBackgroundColor.run());

      Platform.runLater(() -> {
         loadSubStages();
         fontColorProperty.set(model.defaultFontColor.get());
         fontColorProperty.bind(Bindings.createObjectBinding(() -> {
            if (mouseHoveringProperty.get()) {
               return model.hoverFontColor.get();
            } else {
               return model.defaultFontColor.get();
            }
         }, mouseHoveringProperty, model.defaultFontColor, model.hoverFontColor));

         bigTimeLabel.textFillProperty().bind(fontColorProperty);
         allTimeLabel.textFillProperty().bind(fontColorProperty);
         todayAllSeconds.textFillProperty().bind(fontColorProperty);
         currentProjectLabel.textFillProperty().bind(fontColorProperty);

         final Runnable displayProjectRightRunnable = () -> {
            if (model.displayProjectsRight.get()) {
               borderPane.setLeft(null);
               borderPane.setRight(projectsVBox);
            } else {
               borderPane.setRight(null);
               borderPane.setLeft(projectsVBox);
            }
         };
         model.displayProjectsRight.addListener((a, oldValue, newValue) -> displayProjectRightRunnable.run());
         displayProjectRightRunnable.run();

         // Setup textarea font color binding
         final Runnable textAreaColorRunnable = () -> {
            final String textAreaStyle = changeStyleAttribute(textArea.getStyle(), "fx-text-fill",
                  "rgba(" + ColorHelper.colorToCssRgba(fontColorProperty.get()) + ")");
            textArea.setStyle(textAreaStyle);
         };
         fontColorProperty.addListener((a, b, c) -> textAreaColorRunnable.run());
         textAreaColorRunnable.run();

         model.activeWorkItem.addListener((a, b, c) -> {
            updateProjectView();
            textArea.setText("");
         });

         model.defaultBackgroundColor.addListener((a, b, c) -> updateMainBackgroundColor.run());
         model.hoverBackgroundColor.addListener((a, b, c) -> updateMainBackgroundColor.run());
         updateMainBackgroundColor.run();
      });

      pane.setOnMouseEntered(a -> mouseHoveringProperty.set(true));

      pane.setOnMouseExited(a -> mouseHoveringProperty.set(false));

      // Drag stage
      pane.setOnMousePressed(mouseEvent -> {

         // record a delta distance for the drag and drop operation.
         dragDelta.x = mainStage.getX() - mouseEvent.getScreenX();
         dragDelta.y = mainStage.getY() - mouseEvent.getScreenY();
      });

      pane.setOnMouseDragged(mouseEvent -> {
         mainStage.setX(mouseEvent.getScreenX() + dragDelta.x);
         mainStage.setY(mouseEvent.getScreenY() + dragDelta.y);
      });

      bigTimeLabel.textProperty().bind(Bindings.createStringBinding(
            () -> DateFormatter.secondsToHHMMSS(activeWorkSecondsProperty.get()), activeWorkSecondsProperty));

      // update ui each second
      Interval.registerCallBack(() -> {
         final LocalDateTime now = LocalDateTime.now();
         model.activeWorkItem.get().setEndTime(now); // FIXME not good to change model

         final long currentWorkSeconds = Duration
               .between(model.activeWorkItem.get().getStartTime(), model.activeWorkItem.get().getEndTime())
               .getSeconds();
         activeWorkSecondsProperty.set(currentWorkSeconds);
         final long todayWorkingSeconds = controller.calcTodaysWorkSeconds();
         final long todaySeconds = controller.calcTodaysSeconds();

         // update all ui labels
         allTimeLabel.setText(DateFormatter.secondsToHHMMSS(todayWorkingSeconds));
         todayAllSeconds.setText(DateFormatter.secondsToHHMMSS(todaySeconds));

         projectsListViewController.tick();

         mainColorTimeLine.update(model.getPastWorkItems(), controller.calcTodaysSeconds());
         updateTaskbarIcon(currentWorkSeconds);
      });

      mainColorTimeLine = new ColorTimeLine(canvas);
   }

   private void updateProjectView() {
      final Project project = model.activeWorkItem.get().getProject();
      currentProjectLabel.setText(project.getName());
      currentProjectLabel.setUnderline(project.isWork());
      final Circle circle = new Circle(4);
      circle.setFill(project.getColor());
      currentProjectLabel.setGraphic(circle);
   }

   private void settingsClicked() {
      LOG.info("Settings clicked");
      this.mainStage.setAlwaysOnTop(false);
      settingsController.update();
      settingsStage.show();
   }

   private void calendarClicked() {
      LOG.info("Calendar clicked");
      this.mainStage.setAlwaysOnTop(false);
      reportController.update();
      reportStage.show();
   }

   @FXML
   public void addNewProject(final ActionEvent ae) {
      LOG.info("Add new project clicked");
      // TODO somewhat duplicate dialog of create and edit
      final Dialog<Project> dialog = setUpDialogProject("Create new project", "Create a new project");

      final GridPane grid = setUpAddNewProjectGridPane("", Color.WHITE, true);

      // TODO disable OK button if no name is set
      dialog.getDialogPane().setContent(grid);

      dialogResultConverter(dialog, grid);
      mainStage.setAlwaysOnTop(false);
      final Optional<Project> result = dialog.showAndWait();
      mainStage.setAlwaysOnTop(true);

      result.ifPresent(project -> {
         controller.addNewProject(project.getName(), project.isWork(), project.getColor(), project.getIndex());
      });
   }

   private Dialog<Project> setUpDialogProject(final String title, final String headerText) {
      final Dialog<Project> dialog = new Dialog<>();
      dialog.setTitle(title);
      dialog.setHeaderText(headerText);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      return dialog;
   }

   private GridPane setUpAddNewProjectGridPane(final String projectName, final Color projectColor,
         final boolean isWork) {
      final GridPane grid = setUpGridPane(projectName, projectColor, isWork);

      final Spinner<Integer> indexSpinner = new Spinner<>();
      final int availableProjectAmount = model.getAvailableProjects().size();
      indexSpinner.setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount, availableProjectAmount));
      grid.add(indexSpinner, 1, 3);

      return grid;
   }

   private GridPane setUpGridPane(final String projectName, final Color projectColor, final boolean isWork) {
      final GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      final Label nameLabel = new Label("Name:");
      nameLabel.setFont(FontProvider.getDefaultFont());
      grid.add(nameLabel, 0, 0);

      final TextField projectNameTextField = new TextField(projectName);
      projectNameTextField.setFont(FontProvider.getDefaultFont());
      grid.add(projectNameTextField, 1, 0);

      final Label colorLabel = new Label("Color:");
      colorLabel.setFont(FontProvider.getDefaultFont());
      grid.add(colorLabel, 0, 1);

      final ColorPicker colorPicker = new ColorPicker(projectColor);
      grid.add(colorPicker, 1, 1);

      final Label isWorkLabel = new Label("IsWork:");
      isWorkLabel.setFont(FontProvider.getDefaultFont());
      grid.add(isWorkLabel, 0, 2);

      final CheckBox isWorkCheckBox = new CheckBox();
      isWorkCheckBox.setSelected(isWork);
      isWorkCheckBox.setFont(FontProvider.getDefaultFont());
      grid.add(isWorkCheckBox, 1, 2);

      final Label sortIndex = new Label("SortIndex:");
      sortIndex.setFont(FontProvider.getDefaultFont());
      grid.add(new Label("SortIndex:"), 0, 3);

      return grid;
   }

   private Dialog<Project> dialogResultConverter(final Dialog<Project> dialog, final GridPane grid) {
      dialog.setResultConverter(dialogButton -> {
         if (dialogButton == ButtonType.OK) {
            final ObservableList<Node> nodes = grid.getChildren();
            final TextField projectNameTextField = (TextField) nodes.get(1);
            final ColorPicker colorPicker = (ColorPicker) nodes.get(3);
            final CheckBox isWorkCheckBox = (CheckBox) nodes.get(5);
            final Spinner<Integer> indexSpinner = (Spinner<Integer>) nodes.get(7);
            return new Project(projectNameTextField.getText(), colorPicker.getValue(), isWorkCheckBox.isSelected(),
                  indexSpinner.getValue()); // temporary (misused) transfer object for project
         }
         return null;
      });
      return dialog;
   }

   private void runUpdateMainBackgroundColor() {
      Color color = model.defaultBackgroundColor.get();
      double opacity = 0;
      if (mouseHoveringProperty.get()) {
         color = model.hoverBackgroundColor.get();
         opacity = .3;
      }
      String style = changeStyleAttribute(pane.getStyle(), "fx-background-color",
            "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
      style = changeStyleAttribute(style, "fx-border-color",
            "rgba(" + ColorHelper.colorToCssRgb(color) + ", " + opacity + ")");
      pane.setStyle(style);
   }

   private void setUpTime() {
      bigTimeLabel.setText(TIME_ZERO);
      allTimeLabel.setText(TIME_ZERO);
      todayAllSeconds.setText(TIME_ZERO);
   }

   private void setUpTextArea() {
      textArea.setWrapText(true);
      textArea.setEditable(false);
      textArea.editableProperty().bind(mouseHoveringProperty);

      textArea.textProperty().addListener((a, b, c) -> controller.setComment(textArea.getText()));
   }

   private void setProjectListVisible(final boolean showProjectList) {
      projectsVBox.setManaged(showProjectList);
      final double beforeWidth = mainStage.getWidth();
      mainStage.sizeToScene();
      final double afterWidth = mainStage.getWidth();
      projectsVBox.setVisible(showProjectList);
      final double offset = afterWidth - beforeWidth;
      if (!model.displayProjectsRight.get()) {
         // we only need to move the stage if the node on the left is hidden
         // not sure how we can prevent the jumping
         mainStage.setX(mainStage.getX() - offset);
      }
   }

   private void loadSubStages() {
      try {
         // Report stage
         final FXMLLoader fxmlLoader = createFXMLLoader(RESOURCE.FXML_REPORT);
         final Parent sceneRoot = fxmlLoader.load();
         sceneRoot.setFocusTraversable(true);
         sceneRoot.requestFocus();
         reportController = fxmlLoader.getController();
         reportController.setModel(model);
         reportController.setController(controller);
         reportStage = new Stage();
         reportStage.initModality(Modality.APPLICATION_MODAL);

         final Scene reportScene = new Scene(sceneRoot);
         reportScene.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ESCAPE) {
               LOG.info("pressed ESCAPE");
               reportStage.close();
            }
         });

         reportStage.setScene(reportScene);
         reportStage.setTitle("Report");
         reportStage.setResizable(false);
         reportStage.setOnHiding(windowEvent -> this.mainStage.setAlwaysOnTop(true));

         // Settings stage
         final FXMLLoader fxmlLoader2 = createFXMLLoader(RESOURCE.FXML_SETTINGS);
         final Parent settingsRoot = fxmlLoader2.load();
         settingsController = fxmlLoader2.getController();
         settingsController.setControllerAndModel(controller, model);
         settingsStage = new Stage();
         settingsController.setStage(settingsStage);
         settingsStage.initModality(Modality.APPLICATION_MODAL);
         settingsStage.setTitle("Settings");
         settingsStage.setResizable(false);

         final Scene settingsScene = new Scene(settingsRoot);
         settingsScene.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ESCAPE) {
               LOG.info("pressed ESCAPE");
               settingsStage.close();
            }
         });

         settingsStage.setScene(settingsScene);
         settingsStage.setOnHiding(e -> this.mainStage.setAlwaysOnTop(true));
      } catch (final IOException e) {
         LOG.error("Error while loading sub stage");
         throw new FXMLLoaderException(e);
      }
   }

   private FXMLLoader createFXMLLoader(final RESOURCE fxmlLayout) {
      return new FXMLLoader(Resources.getResource(fxmlLayout));
   }

   private void updateTaskbarIcon(final long currentWorkSeconds) {
      final GraphicsContext gcIcon = taskbarCanvas.getGraphicsContext2D();

      gcIcon.clearRect(0, 0, taskbarCanvas.getWidth(), taskbarCanvas.getHeight());
      gcIcon.setFill(model.activeWorkItem.get().getProject().getColor());
      gcIcon.fillRect(1, 27, 31, 5);

      gcIcon.setStroke(model.taskBarColor.get());
      gcIcon.setTextAlign(TextAlignment.CENTER);
      gcIcon.strokeText(DateFormatter.secondsToHHMMSS(currentWorkSeconds).replaceFirst(":", ":\n"),
            Math.round(taskbarCanvas.getWidth() / 2), Math.round(taskbarCanvas.getHeight() / 2) - 5.0);

      final SnapshotParameters snapshotParameters = new SnapshotParameters();
      snapshotParameters.setFill(Color.TRANSPARENT);
      final WritableImage image = taskbarCanvas.snapshot(snapshotParameters, null);

      final StackPane layout = new StackPane();
      layout.getChildren().addAll(new ImageView(image));

      final BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
      final Image icon = SwingFXUtils.toFXImage(bi, null);

      final ObservableList<Image> icons = mainStage.getIcons();
      icons.addAll(icon);
      if (icons.size() > 1) {
         icons.remove(0);
      }
   }

   public void secondInitialize() {
      this.projectsListViewController = new ProjectsListViewController(model, controller, mainStage,
            availableProjectsListView, searchTextField, false);
   }

}
