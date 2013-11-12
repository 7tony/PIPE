package pipe.controllers;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import pipe.actions.ActionEnum;
import pipe.actions.GuiAction;
import pipe.gui.ApplicationSettings;
import pipe.gui.CopyPasteManager;
import pipe.gui.PetriNetTab;
import pipe.handlers.MouseHandler;
import pipe.handlers.mouse.SwingMouseUtilities;
import pipe.models.PetriNet;
import pipe.models.PipeApplicationModel;
import pipe.models.Token;
import pipe.petrinet.*;
import pipe.utilities.transformers.PNMLTransformer;
import pipe.utilities.transformers.TNTransformer;
import pipe.views.PetriNetView;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class PipeApplicationController
{
    private PetriNetController petriNetController;
    private final CopyPasteManager copyPasteManager;
    private final PipeApplicationModel applicationModel;

    public PipeApplicationController(PipeApplicationModel applicationModel, PetriNetController petriNetController,
            CopyPasteManager copyPasteManager)
    {
        this.petriNetController = petriNetController;
        this.applicationModel = applicationModel;
        this.copyPasteManager = copyPasteManager;
        ApplicationSettings.register(this);
    }

    private PetriNet loadPetriNetFromFile(File file, boolean isTN)
    {

        try {
            // BK 10/02/07: Changed loading of PNML to accomodate new
            // PNMLTransformer class
            Document document;
            if (isTN) {
                TNTransformer transformer = new TNTransformer();
                document = transformer.transformTN(file.getPath());
            } else {
                // ProgressBar pb = new ProgressBar("test");
                PNMLTransformer transformer = new PNMLTransformer();
                document = transformer.transformPNML(file.getPath());
                //petriNetTab.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
            CreatorStruct struct = new CreatorStruct(new PlaceCreator(), new TransitionCreator(), new ArcCreator(),
                    new AnnotationCreator(), new RateParameterCreator(), new TokenCreator(), new StateGroupCreator());
            PetriNetReader reader = new PetriNetReader(struct);
            PetriNet net = reader.createFromFile(document);
            net.setPnmlName(file.getAbsolutePath());
            return net;

        } catch (Exception e) {
//                JOptionPane.showMessageDialog(this, "Error loading file:\n" + file.getName() + "\n" + e.toString(),
//                        "File load error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Creates an empty petrinet with a default token
     */
    public void createEmptyPetriNet() {
        PetriNet model = new PetriNet();
        Token defaultToken = createDefaultToken();
        model.addToken(defaultToken);
        createNewTab(model);
    }

    private Token createDefaultToken() {
        Token token = new Token("Default", false, 0, new Color(0, 0, 0));
        return token;
    }

    public void createNewTabFromFile(File file, boolean isTN) {

        if (isPasteInProgress()) {
            cancelPaste();
        }

        PetriNet netModel = loadPetriNetFromFile(file, isTN);
        createNewTab(netModel);

    }

    private void createNewTab(PetriNet net) {
        PetriNetView view = petriNetController.addPetriNet(net);
        PetriNetTab petriNetTab = new PetriNetTab(view, petriNetController);
        MouseHandler handler = new MouseHandler(new SwingMouseUtilities(), petriNetController, net, petriNetTab, view);
        petriNetTab.addMouseListener(handler);
        petriNetTab.addMouseMotionListener(handler);
        petriNetTab.addMouseWheelListener(handler);

        net.registerObserver(view);
//        TODO: WHY? also why should I add a the pipe application view as an obsever?
        view.addObserver(petriNetTab);
        //view.addObserver(ApplicationSettings.getApplicationView());



//        int freeSpace = addEmptyPetriNetTo(petriNetTabs);
//
//        String name = "";


        //TODO: This assumes a 1:1 relationship. Store in map?
//        PetriNetView petriNetView = getPetriNetView(petriNetTabs.size() - 1);
//        PetriNetTab petriNetTab = getTab(petriNetTabs.size() - 1);

//        petriNetView.addObserver(petriNetTab); // Add the view as Observer
//        petriNetView.addObserver(this); // Add the app window as
        // observer

        String name;
        if (net.getPnmlName().isEmpty()) {
            name = "Petri net " + (applicationModel.newPetriNetNumber());
        } else {
            name = FilenameUtils.getBaseName(net.getPnmlName());
        }

        petriNetTab.setNetChanged(false); // Status is unchanged

        ApplicationSettings.getApplicationView().addNewTab(name, petriNetTab);

//        frameForPetriNetTabs.addTab(name, null, scroller, null);
//        frameForPetriNetTabs.setSelectedIndex(freeSpace);

        petriNetTab.updatePreferredSize();


//        refreshTokenClassChoices(); // Steve Doubleday: ensure combo box reflects tokens that were loaded
//        setTitle(name);// Change the program caption
//        frameForPetriNetTabs.setTitleAt(freeSpace, name);
        //applicationModel.selectAction.actionPerformed(null);
        net.notifyObservers();
    }

    public GuiAction getAction(ActionEnum actionType) {
        return applicationModel.getAction(actionType);
    }

    public CopyPasteManager getCopyPasteManager()
    {
        return copyPasteManager;
    }

    public boolean isPasteEnabled()
    {
        return copyPasteManager.pasteEnabled();
    }

    public boolean isPasteInProgress()
    {
        return copyPasteManager.pasteInProgress();
    }

    public void cancelPaste()
    {
        copyPasteManager.cancelPaste();
    }

    public void copy(ArrayList selection, PetriNetTab appView)
    {
        copyPasteManager.doCopy(selection, appView);
    }

    public void showPasteRectangle(PetriNetTab appView)
    {
        copyPasteManager.showPasteRectangle(appView);
    }

    public void createNewPetriNet() {
        PetriNet net = new PetriNet();
        petriNetController.addPetriNet(net);
    }


    public int addEmptyPetriNetTo(ArrayList<PetriNetTab> petriNetTabs)
    {
        PetriNetView petriNetView = petriNetController.addEmptyPetriNet();
        PetriNetTab petriNetTab = new PetriNetTab(petriNetView, petriNetController);
        petriNetTabs.add(petriNetTab);
        return petriNetTabs.size() - 1;
    }

    public PetriNetController getPetriNetController()
    {
        return petriNetController;
    }
}
