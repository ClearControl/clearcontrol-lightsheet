package clearcontrol.instructions.gui;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.instructions.ExecutableInstructionList;
import clearcontrol.instructions.HasInstructions;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.gui.halcyon.MicroscopeNodeType;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.instructions.io.ScheduleReader;
import clearcontrol.instructions.io.ScheduleWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * InstructionListBuilderGUI
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2018
 */
public class InstructionListBuilderGUI<M extends HasInstructions> extends CustomGridPane implements LoggingFeature {

    ScrollPane mPropertiesScrollPane;
    ListView<InstructionInterface> mCurrentProgramScheduleListView;
    private File mProgramTemplateDirectory =
            MachineConfiguration.get()
                    .getFolder("ProgramTemplates");

    ExecutableInstructionList<M> managedProgram;

    public InstructionListBuilderGUI(ExecutableInstructionList<M> instructionList) {
        CustomGridPane lSchedulerChecklistGridPane = new CustomGridPane();

        managedProgram = instructionList;

    TitledPane lTitledPane =
            new TitledPane("Schedule",
                    lSchedulerChecklistGridPane);
      lTitledPane.setAnimated(false);
      lTitledPane.setExpanded(true);
      GridPane.setColumnSpan(lTitledPane, 4);
    add(lTitledPane, 0, mRow);
    mRow++;

    int lRow = 0;
    {
        Label lLabel = new Label("Current program");
        lSchedulerChecklistGridPane.add(lLabel, 0, lRow);
        lRow++;
    }

    mCurrentProgramScheduleListView =
            new ListView<InstructionInterface>();
      mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
    refreshPropertiesScrollPane();
      mCurrentProgramScheduleListView.setMinHeight(300);
      mCurrentProgramScheduleListView.setMinWidth(450);

      mCurrentProgramScheduleListView.setOnMouseClicked(new EventHandler<MouseEvent>()
    {
        @Override
        public void handle(MouseEvent mouseEvent)
        {
            if (mouseEvent.getClickCount() > 0)
            {
                refreshPropertiesScrollPane();
            }
        }
    });

      lSchedulerChecklistGridPane.add(mCurrentProgramScheduleListView,
            0,
    lRow,
            1,
            9);

    {
        Button lMoveUpButton = new Button("^");
        lMoveUpButton.setTooltip(new Tooltip("Move up"));
        lMoveUpButton.setMinWidth(35);
        lMoveUpButton.setMinHeight(35);
        lMoveUpButton.setOnAction((e) -> {
            int i = mCurrentProgramScheduleListView.getSelectionModel()
                    .getSelectedIndex();
            if (i > 0)
            {
                InstructionInterface lInstructionInterface =
                        instructionList.get(i);
                instructionList.remove(i);
                instructionList.add(i - 1, lInstructionInterface);
                mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
                refreshPropertiesScrollPane();
            }
        });
        lSchedulerChecklistGridPane.add(lMoveUpButton, 1, lRow);
        lRow++;
    }

    {
        Button lMoveDownButton = new Button("v");
        lMoveDownButton.setTooltip(new Tooltip("Move down"));
        lMoveDownButton.setMinWidth(35);
        lMoveDownButton.setMinHeight(35);
        lMoveDownButton.setOnAction((e) -> {
            int count = 0;
            int i = mCurrentProgramScheduleListView.getSelectionModel()
                    .getSelectedIndex();
            if (i >= 0 && i < instructionList.size() - 1)
            {
                InstructionInterface lInstructionInterface =
                        instructionList.get(i);
                instructionList.remove(i);
                instructionList.add(i + 1, lInstructionInterface);
                mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
                refreshPropertiesScrollPane();
            }
        });
        lSchedulerChecklistGridPane.add(lMoveDownButton, 1, lRow);
        lRow++;
    }

    {
        Button lMinusButton = new Button("-");
        lMinusButton.setTooltip(new Tooltip("Remove"));
        lMinusButton.setMinWidth(35);
        lMinusButton.setMinHeight(35);
        lMinusButton.setOnAction((e) -> {
            int count = 0;
            int lSelectedIndex =
                    mCurrentProgramScheduleListView.getSelectionModel()
                            .getSelectedIndex();
            for (int i : mCurrentProgramScheduleListView.getSelectionModel()
                    .getSelectedIndices()
                    .sorted())
            {
                instructionList.remove(i - count);
                count++;
            }
            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
            mCurrentProgramScheduleListView.getSelectionModel()
                    .select(lSelectedIndex);
            refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lMinusButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lMinusButton, 1, lRow);
        lRow++;
    }

    {
        Button lUnselectButton = new Button("[]");
        lUnselectButton.setTooltip(new Tooltip("Unselect"));
        lUnselectButton.setMinWidth(35);
        lUnselectButton.setMinHeight(35);
        lUnselectButton.setOnAction((e) -> {
            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
            mCurrentProgramScheduleListView.getSelectionModel()
                    .select(-1);
            refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lUnselectButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lUnselectButton, 1, lRow);
        lRow++;
    }

    {
        Button lCloneButton = new Button("++");
        lCloneButton.setTooltip(new Tooltip("Clone"));
        lCloneButton.setMinWidth(35);
        lCloneButton.setMinHeight(35);
        lCloneButton.setOnAction((e) -> {
            int lSelectedIndex =
                    mCurrentProgramScheduleListView.getSelectionModel()
                            .getSelectedIndex();
            if (lSelectedIndex > -1)
            {
                instructionList.add(lSelectedIndex,
                        instructionList.get(lSelectedIndex)
                                .copy());
            }
            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
            refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lCloneButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lCloneButton, 1, lRow);
        lRow++;
    }

    lRow = 10;
    {
        ComboBox lExistingScheduleTemplates;
        {
            // load
            lExistingScheduleTemplates =
                    new ComboBox(listExistingSchedulerTemplateFiles());
            lSchedulerChecklistGridPane.add(lExistingScheduleTemplates,
                    0,
                    lRow);

            Button lLoadScheduleTemplateBytton = new Button("Load");
            lLoadScheduleTemplateBytton.setMaxWidth(Double.MAX_VALUE);
            lLoadScheduleTemplateBytton.setOnAction((e) -> {
                try
                {
                    managedProgram.clear();
                    new ScheduleReader<M>(managedProgram,
                            managedProgram.getInstructionSource(),
                            getFile(lExistingScheduleTemplates.getValue()
                                    .toString())).read();
                    mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
                    refreshPropertiesScrollPane();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            });

            lSchedulerChecklistGridPane.add(lLoadScheduleTemplateBytton,
                    1,
                    lRow,
                    2,
                    1);
            lRow++;

        }

        {
            // save
            Variable<String> lFileNameVariable =
                    new Variable<String>("filename",
                            "Program");

            TextField lFileNameTextField =
                    new TextField(lFileNameVariable.get());
            lFileNameTextField.setMaxWidth(Double.MAX_VALUE);
            lFileNameTextField.textProperty()
                    .addListener((obs, o, n) -> {
                        String lName = n.trim();
                        if (!lName.isEmpty())
                            lFileNameVariable.set(lName);
                    });
            lSchedulerChecklistGridPane.add(lFileNameTextField,
                    0,
                    lRow);

            Button lSaveProgramButton = new Button("Save");
            lSaveProgramButton.setAlignment(Pos.CENTER);
            lSaveProgramButton.setMaxWidth(Double.MAX_VALUE);
            lSaveProgramButton.setOnAction((e) -> {
                try
                {
                    new ScheduleWriter(managedProgram,
                            getFile(lFileNameVariable.get())).write();
                    lExistingScheduleTemplates.setItems(listExistingSchedulerTemplateFiles());
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            });
            GridPane.setColumnSpan(lSaveProgramButton, 1);
            lSchedulerChecklistGridPane.add(lSaveProgramButton,
                    1,
                    lRow,
                    2,
                    1);
            lRow++;
        }

    }

    String[] lFilters =
            { "Acquisition:",
                    "Adaptation:",
                    "Adaptive optics:",
                    "Filter wheel:",
                    "Fusion:",

                    "IO:",
                    "Laser:",
                    "Memory:",
                    "Post-processing:",
                    "Remote:",

                    "Smart:",
                    "Timing:",
                    "Visualisation:" };

    Node[] lIcons =
            { MicroscopeNodeType.Acquisition.getIcon(),
                    MicroscopeNodeType.AdaptiveOptics.getIcon(),
                    MicroscopeNodeType.AdaptiveOptics.getIcon(),
                    MicroscopeNodeType.FilterWheel.getIcon(),
                    MicroscopeNodeType.Scripting.getIcon(),

                    MicroscopeNodeType.Laser.getIcon(),
                    MicroscopeNodeType.Scripting.getIcon(),
                    MicroscopeNodeType.Scripting.getIcon(),
                    MicroscopeNodeType.Scripting.getIcon(),
                    MicroscopeNodeType.Scripting.getIcon(),

                    MicroscopeNodeType.Scripting.getIcon(),
                    MicroscopeNodeType.Scripting.getIcon(),
                    MicroscopeNodeType.StackDisplay3D.getIcon() };

    lRow = 0;
    // properties panel
    {

        Label lLabel = new Label("Properties");
        lSchedulerChecklistGridPane.add(lLabel, 2, lRow, 2, 1);
        lRow++;

        mPropertiesScrollPane = new ScrollPane();
        mPropertiesScrollPane.setMinHeight(150);
        mPropertiesScrollPane.setMaxHeight(150);
        mPropertiesScrollPane.setMaxHeight(450);
        lSchedulerChecklistGridPane.add(mPropertiesScrollPane,
                2,
                lRow,
                2,
                5);
        lRow++;

    }

    lRow = 7;
        {
            Label lLabel = new Label("Add instruction");
            lSchedulerChecklistGridPane.add(lLabel, 2, lRow, 2, 1);
            lRow++;

            TreeItem<String> rootItem =
                    buildInstructionTree(lFilters,
                            "",
                            lIcons);
            TreeView<String> tree = new TreeView<String>(rootItem);

            Label lSearchLabel = new Label("Search");
            lSchedulerChecklistGridPane.add(lSearchLabel, 2, lRow);

            TextField lSearchField = new TextField();
            lSchedulerChecklistGridPane.add(lSearchField, 3, lRow);
            lSchedulerChecklistGridPane.setOnKeyReleased((e) -> {
                info("keyreleased");
                tree.setRoot(buildInstructionTree(lFilters,
                        lSearchField.getText(),
                        lIcons));
            });
            lRow++;

            tree.setMinHeight(150);
            tree.setMinWidth(450);

            tree.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TreeItem<String> item = tree.getSelectionModel()
                                .getSelectedItem();
                        System.out.println("Selected Text : "
                                + item.getValue());
                        if (item.getParent() != null
                                && item.getParent()
                                .getValue()
                                .compareTo("Instructions") != 0) {
                            int lSelectedIndexInMainList =
                                    mCurrentProgramScheduleListView.getSelectionModel()
                                            .getSelectedIndex();
                            if (lSelectedIndexInMainList < 0)
                                lSelectedIndexInMainList = instructionList.size();
                            instructionList.add(lSelectedIndexInMainList,
                                    managedProgram.getInstructionSource().getInstructions(item.getParent()
                                            .getValue()
                                            + ":"
                                            + item.getValue())
                                            .get(0)
                                            .copy());
                            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(instructionList));
                            if (mCurrentProgramScheduleListView.getSelectionModel()
                                    .getSelectedIndex() > -1) {
                                mCurrentProgramScheduleListView.getSelectionModel()
                                        .select(mCurrentProgramScheduleListView.getSelectionModel()
                                                .getSelectedIndex()
                                                - 1);
                            }
                            refreshPropertiesScrollPane();
                        }
                    }
                }
            });

            StackPane lStackPane = new StackPane();
            lStackPane.getChildren().add(tree);

            lSchedulerChecklistGridPane.add(lStackPane, 2, lRow, 2, 1);
            // lRow+=5;
        }
    }


    private void refreshPropertiesScrollPane()
    {
        /**
         * Dirty hack: Use Java reflections to discover a matching panel TODO:
         * find a better way of doing this, without reflections
         */
        ArrayList<InstructionInterface> instructionList = managedProgram;
        if (mCurrentProgramScheduleListView.getSelectionModel()
                .getSelectedIndex() > -1)
        {
            InstructionInterface lInstruction =
                    instructionList.get(mCurrentProgramScheduleListView.getSelectionModel()
                            .getSelectedIndex());
            System.out.println("Selected: "
                    + instructionList.get(mCurrentProgramScheduleListView.getSelectionModel()
                    .getSelectedIndex()));
            try
            {
                Class<?> lInstructionClass = lInstruction.getClass();
                String lInstructionClassName =
                        lInstructionClass.getSimpleName();
                String lInstructionPanelClassName =
                        lInstructionClass.getPackage()
                                .getName()
                                + ".gui."
                                + lInstructionClassName
                                + "Panel";
                info("Searching for class %s as panel for calibration module %s \n",
                        lInstructionPanelClassName,
                        lInstructionClassName);
                Class<?> lClass = Class.forName(lInstructionPanelClassName);
                Constructor<?> lConstructor =
                        lClass.getConstructor(lInstruction.getClass());
                Node lPanel = (Node) lConstructor.newInstance(lInstruction);

                mPropertiesScrollPane.setContent(lPanel);
            }
            catch (ClassNotFoundException e)
            {
                warning("Cannot find panel for module %s \n",
                        lInstruction.getClass().getSimpleName());
                // e.printStackTrace();
                mPropertiesScrollPane.setContent(null);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    private TreeItem<String> buildInstructionTree(
                                                  String[] lFilters,
                                                  String pSearchFilter,
                                                  Node[] lIcons)
    {
        TreeItem<String> rootItem =
                new TreeItem<String>("Instructions",
                        MicroscopeNodeType.Other.getIcon());
        rootItem.setExpanded(true);
        for (int i = 0; i < lFilters.length; i++)
        {
            ArrayList<InstructionInterface> lAvailableSchedulersList =
                    managedProgram.getInstructionSource().getInstructions(lFilters[i],
                            pSearchFilter);
            if (lAvailableSchedulersList.size() > 0)
            {

                TreeItem<String> item = new TreeItem<String>(
                        lFilters[i].replace(":",
                                ""),
                        lIcons[i]);
                item.setExpanded(pSearchFilter.length() > 0);
                rootItem.getChildren().add(item);

                for (InstructionInterface lInstructionInterface : lAvailableSchedulersList)
                {
                    TreeItem<String> schedulerItem =
                            new TreeItem<String>(lInstructionInterface.getName()
                                    .replace(lFilters[i],
                                            ""));
                    item.getChildren().add(schedulerItem);
                }
            }
        }
        return rootItem;
    }

    private ObservableList<String> listExistingSchedulerTemplateFiles()
    {
        ArrayList<String> filenames = getScheduleTemplateNames();
        ObservableList<String> list =
                FXCollections.observableArrayList(filenames);
        return list;
    }

    private File getFile(String pName)
    {
        return new File(mProgramTemplateDirectory, pName + ".txt");
    }

    ArrayList<String> mExistingTemplateFileList =
            new ArrayList<String>();

    private ArrayList<String> getScheduleTemplateNames()
    {
        File folder = mProgramTemplateDirectory;

        mExistingTemplateFileList.clear();
        for (File file : folder.listFiles())
        {
            if (!file.isDirectory()
                    && file.getAbsolutePath().endsWith(".txt"))
            {
                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.length() - 4);

                mExistingTemplateFileList.add(fileName);
            }
        }

        return mExistingTemplateFileList;
    }

    public ListView<InstructionInterface> getCurrentProgramListView() {
        return mCurrentProgramScheduleListView;
    }
}
