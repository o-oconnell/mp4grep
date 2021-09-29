package Main;

import Main.ArgumentParsing.ControllerFactory;

// Responsibility: uses command line arguments to execute the controller.
public class MP4grep {
    public static void main(String[] args) {
        ControllerFactory controllerFactory = new ControllerFactory();
        Controller control = controllerFactory.getControllerForArgs(args);
        control.execute();
    }
}
