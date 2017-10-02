package testframework;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shell {
    static Logger logger = LoggerFactory.getLogger(Shell.class);

    public static void main(String[] args) {

        Options options = new Options();

        Option testDirectory = new Option("i", "input", true, "Directory with tests");
        Option resultFile = new Option("o", "output", true, "File with tests result");

        testDirectory.setRequired(true);

        options.addOption(testDirectory);
        options.addOption(resultFile);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("test_runner", options);
            System.exit(1);
            return;
        }
        String outputFile = "./result.txt";
        if (cmd.hasOption("output")) {
            outputFile = cmd.getOptionValue("output");
        }
        String inputDirectory = cmd.getOptionValue("input");

        TestRunner runner = new TestRunner();

        runner.runAllTests(inputDirectory, outputFile);
    }
}
