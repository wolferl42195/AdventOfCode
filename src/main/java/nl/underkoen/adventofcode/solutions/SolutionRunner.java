package nl.underkoen.adventofcode.solutions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Command(name = "AdventOfCode",
        aliases = "aoc",
        version = "AoC-1.2",
        mixinStandardHelpOptions = true,
        synopsisHeading = "@|bold,underline Usage|@:%n",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        optionListHeading = "%n@|bold,underline Options|@:%n")
public class SolutionRunner implements Runnable {
    @Option(names = {"-p", "--package"}, description = "The package where the solutions are found.", showDefaultValue = Help.Visibility.ALWAYS)
    String pkg = Solution.class.getPackageName();

    @Option(names = {"-y", "--year"}, description = "Filter solutions on this year.")
    Integer year;

    @Option(names = {"-d", "--day"}, description = "Filter solutions on this day.")
    Integer day;

    @Option(names = {"-t", "--today"}, description = "Run's today's solution.", showDefaultValue = Help.Visibility.ALWAYS)
    boolean today = false;

    @Option(names = {"--output"}, description = "Shows the outputs of the solutions.", showDefaultValue = Help.Visibility.ALWAYS, negatable = true)
    boolean output = true;

    @Option(names = {"--show-times"}, description = "Shows the time it took to complete the solution.", showDefaultValue = Help.Visibility.ALWAYS, negatable = true)
    boolean times = true;

    @Option(names = {"--this-year"}, description = "Filter solutions to this year.", showDefaultValue = Help.Visibility.ALWAYS)
    boolean thisYear = false;

    @Option(names = {"-l", "--last"}, description = "Run's the last solution.", showDefaultValue = Help.Visibility.ALWAYS)
    boolean last = false;

    @Option(names = {"--no-verbose"}, description = "Enables console output.", showDefaultValue = Help.Visibility.ALWAYS, negatable = true)
    boolean verbose = true;

    @Option(names = {"--no-download"}, description = "Enables downloads of the input.", showDefaultValue = Help.Visibility.ALWAYS, negatable = true)
    boolean download = true;

    @Option(names = {"--inputs"}, description = "Custom location for the inputs.", showDefaultValue = Help.Visibility.ALWAYS)
    File inputs = new File(System.getProperty("user.dir") + "\\inputs");

    @Override
    public void run() {
        SolutionUtils.download = download;
        SolutionUtils.resources = inputs;
        System.out.println(inputs);

        Set<Map.Entry<Integer, List<Solution>>> yearSolutions = SolutionUtils.getAllSolutions(pkg).entrySet();

        LocalDateTime date = LocalDateTime.now();
        if (today || thisYear) year = date.getYear();
        if (today) day = date.getDayOfMonth();

        PrintStream out = System.out;
        if (!verbose) {
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }));
        }

        if (last && year == null) year = yearSolutions.stream()
                .mapToInt(Map.Entry::getKey)
                .max()
                .orElseThrow();

        if (year != null) {
            yearSolutions.removeIf(e -> !e.getKey().equals(year));
        }

        for (Map.Entry<Integer, List<Solution>> entry : yearSolutions) {
            out.printf("%n%n=== YEAR %d ===%n%n%n", entry.getKey());

            if (last && day == null) day = entry.getValue().stream()
                    .mapToInt(SolutionInfo::getDay)
                    .max()
                    .orElseThrow();

            if (day != null) {
                entry.getValue().removeIf(s -> s.getDay() != day);
            }

            entry.getValue().sort(Comparator.comparingInt(SolutionInfo::getDay));
            for (Solution solution : entry.getValue()) {
                long start = System.currentTimeMillis();
                try {
                    solution.execute(output);
                    long end = System.currentTimeMillis();
                    solution.test();
                    if (times) out.printf("Day %s succeeded and took %s ms%n", solution.getDay(), end - start);
                    else out.printf("Day %s succeeded", solution.getDay());
                } catch (Exception e) {
                    long end = System.currentTimeMillis();
                    if (times) System.err.printf("Day %s failed, after %s ms%n", solution.getDay(), end - start);
                    else System.err.printf("Day %s failed", solution.getDay());
                }
            }
        }
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new SolutionRunner());
        commandLine.setColorScheme(Help.defaultColorScheme(Help.Ansi.ON));
        System.exit(commandLine.execute(args));
    }
}
