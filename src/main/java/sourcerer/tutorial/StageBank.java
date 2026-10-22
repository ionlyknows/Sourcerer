package sourcerer.tutorial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/** All six tutorials. Each round is a small puzzle; edit or add rounds here. */
final class StageBank {

    private static final List<Stage> STAGES = new ArrayList<>();

    @SafeVarargs
    private static List<Function<Board.Ctx, Board>> rounds(Function<Board.Ctx, Board>... fs) {
        return Arrays.asList(fs);
    }

    private static MatchBoard.Row row(String left, String answer, String clue) {
        return new MatchBoard.Row(left, answer, clue);
    }

    static {
        // 1 ---------------------------------------------------------------- VARIABLES
        STAGES.add(new Stage("Variables", "Match the boxes",
                "A variable is a labelled box. Click a value, then click the box it belongs in!",
                "A variable is a labelled box: name = value. The name goes on the left, the value on the right.",
                rounds(
                        ctx -> new MatchBoard(ctx, "Which value belongs in each variable box?",
                                Arrays.asList(
                                        row("score =", "120", "points the hero earned"),
                                        row("hero =", "\"Ana\"", "the hero's name (text)"),
                                        row("lives =", "3", "tries left")),
                                "True"),
                        ctx -> new MatchBoard(ctx, "What does each variable hold after the code runs?",
                                Arrays.asList(
                                        row("x = 5 ; x = x + 1   ->  x", "6", ""),
                                        row("y = 2 ; y = y * 10  ->  y", "20", ""),
                                        row("s = \"Hi\" ; s = s + \"!\"  ->  s", "\"Hi!\"", "")),
                                "7", "\"Hi\""),
                        ctx -> new ChoiceBoard(ctx, "Which line really creates a variable x that holds 5?",
                                "# pick the correct line",
                                new String[]{"5 = x", "x = 5", "x == 5", "\"x\" = 5"}, 1,
                                "The name goes on the LEFT of = and the value on the RIGHT: x = 5."))));

        // 2 ---------------------------------------------------------------- DATA TYPES
        STAGES.add(new Stage("Data Types", "Sort the values",
                "Every value has a type. Click a tile, then click the box where it belongs!",
                "int = whole numbers, float = decimals, str = text in quotes, bool = True or False.",
                rounds(
                        ctx -> new SortBoard(ctx, "Sort every tile into its data type box.", new String[][]{
                                {"42", "int"}, {"7", "int"}, {"3.14", "float"},
                                {"\"Hello\"", "str"}, {"True", "bool"}, {"\"Hi\"", "str"}}),
                        ctx -> new SortBoard(ctx, "Trickier ones! Look closely at quotes and dots.", new String[][]{
                                {"2.5", "float"}, {"\"7\"", "str"}, {"False", "bool"},
                                {"-3", "int"}, {"0.5", "float"}, {"'x'", "str"}}),
                        ctx -> new ChoiceBoard(ctx, "Which one turns the text \"7\" into a whole number?",
                                "num = ???",
                                new String[]{"int(\"7\")", "str(\"7\")", "float(7)", "\"7\""}, 0,
                                "int() converts text into a whole number."))));

        // 3 ---------------------------------------------------------------- INPUT & OUTPUT
        STAGES.add(new Stage("Input & Output", "Connect the blocks",
                "Code runs from top to bottom. Click blocks in running order, then press RUN!",
                "input() brings a value in, print() sends a value out. Blocks run top to bottom.",
                rounds(
                        ctx -> new SequenceBoard(ctx, "Connect the INPUT to the OUTPUT.", "Ana", "Hello Ana",
                                "name = input()", "print(\"Hello \" + name)"),
                        ctx -> new SequenceBoard(ctx, "input() gives text, so int() makes it a number.", "5", "6",
                                "age = int(input())", "age = age + 1", "print(age)"),
                        ctx -> new SequenceBoard(ctx, "Build the machine that doubles the input.", "4", "8",
                                "n = int(input())", "double = n * 2", "print(double)"))));

        // 4 ---------------------------------------------------------------- CONDITIONALS
        STAGES.add(new Stage("Conditionals", "Pick the path",
                "The condition decides which path runs. Is it True or False? Pick the path!",
                "if runs its code only when the condition is True. Otherwise else runs.",
                rounds(
                        ctx -> new ChoiceBoard(ctx, "Which path does the program take?",
                                "x = 7\nif x > 5:\n    print(\"big\")\nelse:\n    print(\"small\")",
                                new String[]{"big", "small", "(nothing)"}, 0,
                                "7 > 5 is True, so the if-path prints big."),
                        ctx -> new ChoiceBoard(ctx, "== asks \"are they equal?\". Which path runs?",
                                "hp = 0\nif hp == 0:\n    print(\"dead\")\nelse:\n    print(\"alive\")",
                                new String[]{"alive", "dead", "(nothing)"}, 1,
                                "0 == 0 is True, so it prints dead."),
                        ctx -> new ChoiceBoard(ctx, "Careful with >= (greater than or equal).",
                                "age = 16\nif age >= 18:\n    print(\"adult\")\nelse:\n    print(\"kid\")",
                                new String[]{"adult", "kid", "(nothing)"}, 1,
                                "16 >= 18 is False, so the else-path prints kid."),
                        ctx -> new ChoiceBoard(ctx, "Pick the condition that makes \"adult\" print.",
                                "age = 20\nif ???:\n    print(\"adult\")",
                                new String[]{"age < 18", "age >= 18", "age == 5"}, 1,
                                "20 >= 18 is True, so \"adult\" prints."),
                        ctx -> new ChoiceBoard(ctx, "not flips True and False. What prints?",
                                "ok = False\nif not ok:\n    print(\"no\")",
                                new String[]{"no", "(nothing)"}, 0,
                                "not False is True, so it prints no."))));

        // 5 ---------------------------------------------------------------- LOOPS
        STAGES.add(new Stage("Loops", "Complete the loop",
                "A loop repeats code. Fill the blanks so the code prints the goal!",
                "A loop repeats code: for i in range(n) runs n times, while repeats as long as its condition is True.",
                rounds(
                        ctx -> new ChoiceBoard(ctx, "Spot the repeating action! Which loop does the same job?",
                                "print(\"Hi\")\nprint(\"Hi\")\nprint(\"Hi\")\nprint(\"Hi\")",
                                new String[]{"for i in range(4): print(\"Hi\")", "for i in range(3): print(\"Hi\")",
                                        "print(\"Hi\" * 3)"}, 0,
                                "range(4) repeats the print four times."),
                        ctx -> new BlankBoard(ctx, "Repeat the print three times.", "Go!  Go!  Go!",
                                new String[]{"for i in range(___):", "    print(___)"},
                                new String[]{"3", "\"Go!\""}, "4", "\"Stop\"", "i"),
                        ctx -> new BlankBoard(ctx, "range(n) counts 0, 1, 2 ... up to n-1.", "0  1  2  3",
                                new String[]{"for i in range(___):", "    print(___)"},
                                new String[]{"4", "i"}, "3", "\"i\"", "5"),
                        ctx -> new BlankBoard(ctx, "A while loop repeats while its condition stays True.", "1  2  3",
                                new String[]{"n = 1", "while n ___ 4:", "    print(n)", "    n ___ 1"},
                                new String[]{"<", "+="}, ">", "=", "=="),
                        ctx -> new BlankBoard(ctx, "A for loop can walk through letters.", "H  i",
                                new String[]{"for c in ___:", "    print(___)"},
                                new String[]{"\"Hi\"", "c"}, "range", "\"c\""))));

        // 6 ---------------------------------------------------------------- FUNCTIONS
        STAGES.add(new Stage("Functions", "Build the machine",
                "A function takes input, does its job and gives output. Build the machine!",
                "def makes a reusable function. It takes inputs (parameters) and return sends a result back.",
                rounds(
                        ctx -> new SequenceBoard(ctx, "Define the function first, then call it.", "4", "8",
                                "def double(n):", "    return n * 2", "print(double(4))"),
                        ctx -> new ChoiceBoard(ctx, "What comes out of the function machine?",
                                "def add(a, b):\n    return a + b\n\nprint(add(2, 3))",
                                new String[]{"5", "23", "2 3"}, 0,
                                "add gives back a + b, and 2 + 3 is 5."),
                        ctx -> new SequenceBoard(ctx, "Define greet, then cast it.", "\"Ana\"", "Hi Ana",
                                "def greet(name):", "    print(\"Hi \" + name)", "greet(\"Ana\")"),
                        ctx -> new BlankBoard(ctx, "Finish the machine: it multiplies n by itself.", "9",
                                new String[]{"def square(n):", "    return ___ * ___", "print(square(3))"},
                                new String[]{"n", "n"}, "2", "\"n\"", "+"))));
    }

    private StageBank() {
    }

    static List<Stage> all() {
        return STAGES;
    }

    static int count() {
        return STAGES.size();
    }
}
