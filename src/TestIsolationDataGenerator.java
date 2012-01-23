import java.io.IOException;

import common.DataExtractor;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

public class TestIsolationDataGenerator {

    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean help = false;

    private static Options plumeOptions;

    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataGenerator [options]";

    /**
     * @param args
     *            [0] : full path to the repository directory
     * @param args
     *            [1] : full path to the output file
     * @param args
     *            [2] : starting commit id
     * @param args
     *            [3] : ending commit id
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        plumeOptions = new Options(TestIsolationDataGenerator.usage_string);
        @SuppressWarnings("unused")
        String[] cmdLineArgs = plumeOptions.parse_or_usage(args);

        // Display help just for the 'publicized' option groups
        if (TestIsolationDataGenerator.help) {
            plumeOptions.print_usage();
            return;
        }

        DataExtractor.extractData(args[0], args[1], args[2], args[3]);
    }

}
