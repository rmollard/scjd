package suncertify;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import suncertify.client.ClientController;
import suncertify.server.ServerController;
import suncertify.utils.Localization;

/**
 * Main class for launching the URLyBird 1.3.2 application.
 *
 * @author Robert Mollard
 */
public final class URLyBirdLauncher {

    /**
     * The possible modes in which the program can be started.
     */
    private enum StartupMode {

        /**
         * Network client mode.
         */
        CLIENT,

        /**
         * Network server mode.
         */
        SERVER,

        /**
         * Standalone mode with no networking.
         */
        STANDALONE;
    }

    /**
     * The default startup mode to use.
     */
    private static final StartupMode DEFAULT_MODE = StartupMode.CLIENT;

    /**
     * The port to use when in server or client mode.
     */
    private static final int NETWORK_PORT = Registry.REGISTRY_PORT;

    /**
     * The modes that the program supports. This is a mapping
     * between the mode name and the mode.
     */
    private static final Map<String, StartupMode> MODES =
        new HashMap<String, StartupMode>();

    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger("suncertify");

    /**
     * The name of the log file for the program.
     */
    private static final String LOG_FILE_NAME = "URLyBird_Log.xml";

    /**
     * Controller, used to display warnings on startup.
     */
    private final Controller controller;

    /**
     * Start up the program in the given mode.
     *
     * @param startupMode the startup mode to use
     */
    private URLyBirdLauncher(final StartupMode startupMode) {

        LOGGER.fine("Program started in " + startupMode + " mode");

        final ClientController client;
        final ServerController server;

        switch (startupMode) {
        case SERVER:
            server = new ServerController(NETWORK_PORT);
            controller = server;
            break;

        case STANDALONE:
            server = new ServerController();
            //Connect the client directly to the server
            client = new ClientController(server.getConnection());
            controller = client;
            break;

        case CLIENT:
        default:
            client = new ClientController(NETWORK_PORT);
            controller = client;
            break;
        }
    }

    /**
     * We are tolerant of incorrect command line arguments and always launch a
     * GUI. If there are incorrect command line arguments, we display a warning
     * message.
     *
     * @param args startup arguments,
     *        we expect "server" or "alone" or no argument.
     *        If any invalid arguments are given, or if several arguments
     *        are given, we launch the GUI and display an appropriate
     *        warning message.
     *        If no arguments are given, the default mode is Client.
     *        If more than one argument is given, we use the first
     *        argument.
     */
    public static void main(final String[] args) {

        LOGGER.finer("URLyBird program starting");

        try {
            //Set up logging file
            final Handler handler = new FileHandler(LOG_FILE_NAME);
            Logger.getLogger("suncertify").addHandler(handler);
        } catch (SecurityException e) {
            LOGGER.severe("Security exception when trying to create log file:"
                    + e);
        } catch (IOException e) {
            LOGGER.severe("I/O exception when trying to create log file:" + e);
        }

        StartupMode startupMode = DEFAULT_MODE;

        //The problems encountered while parsing command line arguments
        List<StartupProblem> startupWarnings = new ArrayList<StartupProblem>();

        //Initialize the modes
        MODES.put(Localization.getString("arguments.client"),
                StartupMode.CLIENT);
        MODES.put(Localization.getString("arguments.server"),
                StartupMode.SERVER);
        MODES.put(Localization.getString("arguments.standalone"),
                StartupMode.STANDALONE);

        //Try to parse the first command line argument (if given)
        if (args.length > 0) {

            startupMode = MODES.get(args[0]);
            if (startupMode == null) {
                //Use default mode if there is an error
                startupMode = DEFAULT_MODE;
                startupWarnings.add(
                        StartupProblem.createUnknownArgumentError(args[0]));
            }
        }
        //Expected only one argument at most
        if (args.length > 1) {
            startupWarnings.add(StartupProblem.createTooManyArgumentsError());
        }

        URLyBirdLauncher program = new URLyBirdLauncher(startupMode);

        StringBuilder buffer = new StringBuilder();
        //Consolidate warning messages into one string
        for (StartupProblem i : startupWarnings) {
            buffer.append(i.getMessage());
            buffer.append("\n");
        }
        //Show warning message if needed
        if (startupWarnings.size() > 0) {
            if (program.controller != null) {
                program.controller.showWarning(buffer.toString());
            }
        }
    }

}
