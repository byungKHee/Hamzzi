package hamzzi.uci.core;

import hamzzi.uci.factory.CommandFactory;

import java.util.Scanner;

public class UciServer {

    public void start() {
        EngineContext context = new EngineContext();
        CommandFactory commandFactory = new CommandFactory();
        Scanner scanner = new Scanner(System.in);

        while (!context.isQuitting() && scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) continue;

            commandFactory.getCommand(line).execute(context, line);
        }
    }
}
