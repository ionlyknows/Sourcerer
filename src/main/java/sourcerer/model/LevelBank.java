package sourcerer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** All Python lessons. Edit or extend this list to change the curriculum. */
public final class LevelBank {

    private static final List<Level> LEVELS = new ArrayList<>();
    private static final Level TUTORIAL;
    private static final Pattern PYTHON_TOKEN = Pattern.compile(
            "(?:[fF])?\"[^\"]*\"|(?:[fF])?'[^']*'|[A-Za-z_][A-Za-z_0-9]*|\\d+(?:\\.\\d+)?|==|!=|<=|>=|\\+=|-=|//=|//|\\*\\*|[^\\s]");

    static {
        LEVELS.add(new Level(1, "Variables",
                "A variable is a labelled box that stores a value.",
                "hp = 100", "hp now holds 100",
                Arrays.asList(
                        new Puzzle("Make a variable x that holds 5.",
                                new String[]{"x", "=", "5"},
                                new String[]{"y", "==", "7", "+", "print", "\"Hi\"", "(", ")"},
                                "x = 5 stores 5 in the variable x."),
                        new Puzzle("Store the text \"Hero\" in a variable called name.",
                                new String[]{"name", "=", "\"Hero\""},
                                new String[]{"x", "\"Villain\"", "3", "+", "==", "(", ")"},
                                "Text goes inside quotes. name now holds Hero."),
                        new Puzzle("Set a variable hp to 100.",
                                new String[]{"hp", "=", "100"},
                                new String[]{"mp", "50", "==", "+", "\"hp\"", ":"},
                                "hp = 100 creates hp and puts 100 inside."),
                        new Puzzle("Add 1 to score: set score equal to score + 1.",
                                new String[]{"score", "=", "score", "+", "1"},
                                new String[]{"-", "*", "0", "==", "x", "\"1\""},
                                "Python works out the right side first, then stores it back in score."))));

        LEVELS.add(new Level(2, "Data Types",
                "Values have types: int (whole), float (decimal), str (text), bool (True/False).",
                "speed = 2.5", "speed is a float",
                Arrays.asList(
                        new Puzzle("Make a float variable speed that holds 2.5.",
                                new String[]{"speed", "=", "2.5"},
                                new String[]{"int", "\"2.5\"", "3.5", "==", "x", "+"},
                                "Numbers with a decimal point are floats."),
                        new Puzzle("Make a bool called alive and set it to True.",
                                new String[]{"alive", "=", "True"},
                                new String[]{"true", "\"True\"", "False", "1", "==", "x"},
                                "True and False start with a capital letter and have no quotes."),
                        new Puzzle("Turn the text \"7\" into a number using int().",
                                new String[]{"num", "=", "int", "(", "\"7\"", ")"},
                                new String[]{"str", "7", "print", "==", "+", "\"num\""},
                                "int(\"7\") converts the text \"7\" into the whole number 7."),
                        new Puzzle("Turn the number 5 into text using str().",
                                new String[]{"word", "=", "str", "(", "5", ")"},
                                new String[]{"int", "\"5\"", "+", "print", "==", "x"},
                                "str(5) converts the number 5 into the text \"5\"."))));

        LEVELS.add(new Level(3, "Input & Output",
                "print() shows things on screen. input() asks the player to type something.",
                "print(\"Hi\")", "Hi",
                Arrays.asList(
                        new Puzzle("Print the text \"Hi\".",
                                new String[]{"print", "(", "\"Hi\"", ")"},
                                new String[]{"input", "=", "x", "+", "\"Bye\"", ":", "5"},
                                "print(\"Hi\") shows Hi on the screen."),
                        new Puzzle("Print the value of the variable score.",
                                new String[]{"print", "(", "score", ")"},
                                new String[]{"input", "\"score\"", "=", "==", "+", ":"},
                                "Without quotes, print shows what the variable holds."),
                        new Puzzle("Ask for a name and store it in name.",
                                new String[]{"name", "=", "input", "(", ")"},
                                new String[]{"print", "\"name\"", "+", "x", "==", "5"},
                                "input() waits for the player to type, then gives back the text."),
                        new Puzzle("Ask for an age as a number and store it in age.",
                                new String[]{"age", "=", "int", "(", "input", "(", ")", ")"},
                                new String[]{"str", "print", "==", "\"age\""},
                                "input() gives text, so int() turns it into a number."),
                        new Puzzle("Print \"Hello \" joined with name.",
                                new String[]{"print", "(", "\"Hello \"", "+", "name", ")"},
                                new String[]{"input", "=", "\"name\"", "5", "==", ":"},
                                "+ joins two pieces of text together."))));

        LEVELS.add(new Level(4, "Conditionals",
                "if lets code make decisions. The line must end with a colon.",
                "if hp > 0: print(\"alive\")", "alive",
                Arrays.asList(
                        new Puzzle("If x is greater than 5, print \"big\".",
                                new String[]{"if", "x", ">", "5", ":", "print", "(", "\"big\"", ")"},
                                new String[]{"for", "<", "while"},
                                "The code after the colon only runs when x > 5 is True."),
                        new Puzzle("If hp equals 0, print \"dead\".",
                                new String[]{"if", "hp", "==", "0", ":", "print", "(", "\"dead\"", ")"},
                                new String[]{"=", "while", ">"},
                                "== compares two values. A single = would store a value instead."),
                        new Puzzle("If age is 18 or more, print \"adult\".",
                                new String[]{"if", "age", ">=", "18", ":", "print", "(", "\"adult\"", ")"},
                                new String[]{"<=", "else", "="},
                                ">= means greater than or equal to."),
                        new Puzzle("If ok is not true, print \"no\".",
                                new String[]{"if", "not", "ok", ":", "print", "(", "\"no\"", ")"},
                                new String[]{"and", "==", "or", "x"},
                                "not flips True to False and False to True."))));

        LEVELS.add(new Level(5, "Loops",
                "A loop repeats code so you don't have to write it again and again.",
                "for i in range(3): print(i)", "0  1  2",
                Arrays.asList(
                        new Puzzle("Print i for every number from 0 to 2.",
                                new String[]{"for", "i", "in", "range", "(", "3", ")", ":", "print", "(", "i", ")"},
                                new String[]{},
                                "range(3) gives 0, 1, 2. The loop runs the print three times."),
                        new Puzzle("Keep adding 1 to x while x is less than 5.",
                                new String[]{"while", "x", "<", "5", ":", "x", "+=", "1"},
                                new String[]{"for", "in", "range", "=="},
                                "while repeats as long as its condition stays True. += adds to a variable."),
                        new Puzzle("Print every letter c in \"Hi\".",
                                new String[]{"for", "c", "in", "\"Hi\"", ":", "print", "(", "c", ")"},
                                new String[]{"range", "while", "="},
                                "A for loop can walk through the letters of a string."),
                        new Puzzle("Print \"Hi\" two times with a loop.",
                                new String[]{"for", "n", "in", "range", "(", "2", ")", ":", "print", "(", "\"Hi\"", ")"},
                                new String[]{},
                                "range(2) repeats the loop body two times."))));

        LEVELS.add(new Level(6, "Lists",
                "A list stores many values in one variable. Counting starts at 0.",
                "nums = [1, 2, 3]", "nums holds three numbers",
                Arrays.asList(
                        new Puzzle("Make a list called nums holding 1, 2 and 3.",
                                new String[]{"nums", "=", "[", "1", ",", "2", ",", "3", "]"},
                                new String[]{"(", ")", ":"},
                                "Square brackets make a list. Items are separated by commas."),
                        new Puzzle("Print how many items are in nums.",
                                new String[]{"print", "(", "len", "(", "nums", ")", ")"},
                                new String[]{"range", "input", "=", "+", "x"},
                                "len() counts the items in a list."),
                        new Puzzle("Add 4 to the end of nums.",
                                new String[]{"nums", ".", "append", "(", "4", ")"},
                                new String[]{"len", "=", "+", "[", "]", "print"},
                                ".append() puts a new item at the end of the list."),
                        new Puzzle("Print the first item of nums.",
                                new String[]{"print", "(", "nums", "[", "0", "]", ")"},
                                new String[]{"1", "len", "=", ".", ":"},
                                "Index 0 is the first item - lists start counting at 0."))));

        LEVELS.add(new Level(7, "Functions",
                "A function is a named spell you write once and cast many times.",
                "def hi(): print(\"Hi\")", "hi() prints Hi",
                Arrays.asList(
                        new Puzzle("Define a function hello that prints \"Hi\".",
                                new String[]{"def", "hello", "(", ")", ":", "print", "(", "\"Hi\"", ")"},
                                new String[]{"return", "if", "="},
                                "def creates the function. The code after the colon is its body."),
                        new Puzzle("Define add(a, b) that returns a + b.",
                                new String[]{"def", "add", "(", "a", ",", "b", ")", ":", "return", "a", "+", "b"},
                                new String[]{},
                                "return sends a result back to whoever called the function."),
                        new Puzzle("Call the function hello.",
                                new String[]{"hello", "(", ")"},
                                new String[]{"def", "print", ":", "=", "x", "\"hello\""},
                                "Writing the name followed by () runs the function."),
                        new Puzzle("Print the result of add with 2 and 3.",
                                new String[]{"print", "(", "add", "(", "2", ",", "3", ")", ")"},
                                new String[]{"def", "return", "="},
                                "add(2, 3) returns 5, and print shows it."))));

        addExtendedLevels();

        TUTORIAL = new Level(0, "Training",
                "Learn the controls with one tiny Python spell.",
                "x = 5", "x holds 5",
                Arrays.asList(
                        new Puzzle("Make a variable x that holds 5.",
                                new String[]{"x", "=", "5"},
                                new String[]{"y", "==", "7", "+", "print", "\"Hi\"", "(", ")"},
                                "x = 5 stores 5 in the variable x.")));
    }

    private LevelBank() {
    }

    /*
     * Stages 8-100 continue the course in short, themed steps. Each row is
     * title | sample code | sample output | prompt | explanation. Keeping the
     * spell as data makes the exact tile answer auditable and never executes it.
     */
    private static void addExtendedLevels() {
        String[][] stages = {
            {"String Tools", "word = 'magic'", "magic", "Store the text magic in word.", "Quoted text is a string value."},
            {"String Length", "size = len('spell')", "5", "Count the characters in spell and store the count in size.", "len returns the number of characters in a string."},
            {"String Case", "name = 'Ada'.upper()", "ADA", "Make an uppercase copy of Ada.", "upper changes letters to uppercase."},
            {"String Cleanup", "name = ' Ada '.strip()", "Ada", "Remove spaces around Ada.", "strip removes whitespace from both ends."},
            {"String Pieces", "parts = 'red,blue'.split(',')", "['red', 'blue']", "Split red,blue at its comma.", "split makes a list around each separator."},
            {"Join Text", "line = '-'.join(['sun', 'moon'])", "sun-moon", "Join sun and moon with a dash.", "join puts the chosen separator between strings."},
            {"String Search", "found = 'dragon'.find('gon')", "4", "Find where gon starts in dragon.", "find returns the first matching index."},
            {"Replace Text", "word = 'cat'.replace('c', 'b')", "bat", "Change the c in cat to b.", "replace returns text with matching parts changed."},
            {"String Slices", "tail = 'sourcerer'[4:]", "ererer", "Take the characters of sourcerer from index 4 onward.", "A slice can omit its ending index."},
            {"String Boss", "print('abra'.upper())", "ABRA", "Print abra in uppercase.", "The method call transforms the text before print shows it."},
            {"String Format", "msg = f'HP {hp}'", "HP 9", "Build an f-string that says HP followed by hp.", "An f-string inserts the value inside braces."},
            {"String Membership", "ok = 'py' in 'python'", "True", "Check whether py occurs in python.", "in checks for a substring."},
            {"Math Operators", "area = 6 * 7", "42", "Multiply 6 by 7 and store the result in area.", "The * operator multiplies numbers."},
            {"Integer Division", "whole = 17 // 5", "3", "Keep only the whole quotient of 17 divided by 5.", "// discards the fractional part."},
            {"Remainders", "left = 17 % 5", "2", "Find the remainder after 17 is divided by 5.", "% gives the remainder."},
            {"Powers", "square = 8 ** 2", "64", "Raise 8 to the power 2.", "** is Python's exponent operator."},
            {"Absolute Values", "distance = abs(-9)", "9", "Store the distance of -9 from zero.", "abs returns a number's nonnegative magnitude."},
            {"Rounding", "price = round(3.141, 2)", "3.14", "Round 3.141 to two decimal places.", "round can take the number of decimal places."},
            {"Minimum and Maximum", "low = min(4, 2, 9)", "2", "Find the smallest of 4, 2 and 9.", "min returns the smallest argument."},
            {"Math Boss", "print(max(3, 8) ** 2)", "64", "Print the largest of 3 and 8 squared.", "Nested calls run inside out, then exponentiation happens."},
            {"Boolean Logic", "ready = key and door", "False", "Set ready to the result of key and door.", "and is true only when both values are true."},
            {"Either Condition", "open = red or blue", "True", "Set open when red or blue is true.", "or is true when at least one side is true."},
            {"Compare Values", "same = 4 == 4", "True", "Check whether the two fours are equal.", "== compares values and produces a bool."},
            {"Not Equal", "changed = old != new", "True", "Check whether old and new differ.", "!= means not equal."},
            {"Range Checks", "inside = 2 < x < 8", "True", "Check that x is between 2 and 8.", "Chained comparisons check both bounds."},
            {"If and Else", "mood = 'ok' if hp > 0 else 'lost'", "ok", "Choose ok when hp is positive, otherwise lost.", "A conditional expression selects one of two values."},
            {"Elif Branch", "grade = 'B' if score >= 80 else 'C'", "B", "Use a conditional expression for score 80 or above.", "The condition chooses which value is assigned."},
            {"Truth Values", "has_items = bool(items)", "True", "Check whether items contains anything.", "bool converts a value to True or False."},
            {"All Conditions", "valid = all([True, True])", "True", "Check that every listed condition is true.", "all is true only when every item is true."},
            {"Any Condition", "alert = any([False, True])", "True", "Check whether at least one condition is true.", "any is true when one or more items are true."},
            {"Logic Boss", "print(hp > 0 and key)", "True", "Print whether hp is positive and key is true.", "The comparison and both inputs are combined by and."},
            {"Counted Loops", "for n in range(3): print(n)", "0 1 2", "Print each number from 0 through 2.", "The stop value in range is excluded."},
            {"Loop Steps", "for n in range(4): print(n)", "0 1 2 3", "Print each value produced by range(4).", "range(4) starts at zero and stops before four."},
            {"Accumulation", "total += price", "total grows", "Add price to the running total.", "+= updates a variable using its old value."},
            {"Loop Filter", "evens = [n for n in nums]", "copy of nums", "Build a new list from the values in nums.", "A comprehension can transform each visited item."},
            {"Enumerate", "for i in items: print(i)", "items", "Print each item while visiting the list.", "A for loop visits each sequence item."},
            {"Reverse Loop", "for n in range(3): print(n)", "0 1 2", "Print every number produced by range(3).", "range produces each integer before the stop value."},
            {"Loop Else", "for x in []: print(x)", "no output", "Run a loop over an empty list.", "An empty sequence makes the loop body run zero times."},
            {"While Guard", "while hp > 0: hp -= 1", "hp reaches 0", "Reduce hp until it is no longer positive.", "while repeats while its condition stays true."},
            {"Loop Boss", "print(sum(range(5)))", "10", "Print the sum of numbers 0 through 4.", "range supplies values to sum without building a list."},
            {"List Update", "nums[1] = 9", "second item is 9", "Replace the second item in nums with 9.", "List indexes begin at zero."},
            {"List Slices", "front = nums[:2]", "first two items", "Copy the first two items of nums.", "A slice ending at 2 includes indexes 0 and 1."},
            {"List Remove", "nums.remove(4)", "4 removed", "Remove the value 4 from nums.", "remove searches for a matching value."},
            {"List Pop", "last = nums.pop()", "last item", "Remove the last item and store it in last.", "pop both removes and returns an item."},
            {"List Sort", "nums.sort()", "ascending", "Sort nums in place.", "sort changes the list itself."},
            {"List Copy", "backup = nums.copy()", "same items", "Make a shallow copy of nums.", "copy creates a separate list with the same items."},
            {"List Concatenation", "both = left + right", "combined list", "Join left and right into a new list.", "List + concatenates without changing either input."},
            {"List Comprehension", "doubles = [n * 2 for n in nums]", "doubled items", "Make a list containing twice every number in nums.", "A comprehension transforms each visited item."},
            {"Nested Lists", "cell = board[1][0]", "row 2 column 1", "Read row 2, column 1 using zero-based indexes.", "Index the outer list first, then its inner list."},
            {"List Boss", "print(sum(nums) * 2)", "twice the sum", "Print twice the sum of nums.", "sum adds the items before multiplication doubles the result."},
            {"Tuple Values", "point = (3, 4)", "(3, 4)", "Store coordinates 3 and 4 in a tuple.", "Parentheses make a tuple; its items cannot be reassigned."},
            {"Unpack Values", "x, y = point", "coordinates", "Unpack point into x and y.", "Unpacking assigns each item to its matching name."},
            {"Dictionary Basics", "hero = {'hp': 10}", "hp is 10", "Make a hero dictionary whose hp is 10.", "A dictionary stores key and value pairs."},
            {"Dictionary Lookup", "hp = hero['hp']", "10", "Read the hp value from hero.", "Use a key in square brackets to look up a value."},
            {"Dictionary Update", "hero['hp'] = 8", "hp is 8", "Set hero's hp value to 8.", "Assigning through a key updates that entry."},
            {"Dictionary Safe Read", "hp = hero.get('hp', 0)", "stored hp or 0", "Read hp, using zero if the key is missing.", "get accepts a fallback value."},
            {"Dictionary Keys", "names = list(hero.keys())", "key list", "Make a list of hero's keys.", "keys returns a view of dictionary keys."},
            {"Dictionary Values", "powers = list(hero.values())", "value list", "Make a list of hero's values.", "values returns the stored values."},
            {"Dictionary Items", "for key in hero: print(key)", "each key", "Visit each key in hero.", "A for loop visits the dictionary's keys."},
            {"Dictionary Build", "squares = {n: n for n in nums}", "number map", "Map each number in nums to itself.", "A dictionary comprehension creates key and value pairs."},
            {"Set Uniqueness", "unique = set([2, 2, 3])", "{2, 3}", "Remove duplicate values from the list.", "A set keeps only unique values."},
            {"Collection Boss", "print(hero.get('hp', 0))", "hp or zero", "Print hero's hp, using zero if it is missing.", "get returns the saved value or its fallback."},
            {"Function Parameters", "def greet(name): print(name)", "function defined", "Define greet with a name parameter that it prints.", "Parameters receive values passed at a call."},
            {"Return Values", "def double(x): return x * 2", "function defined", "Define double to return twice x.", "return gives the result back to the caller."},
            {"Default Argument", "def heal(hp=1): return hp", "function defined", "Define heal with a default hp value of 1.", "A default is used when the caller omits that argument."},
            {"Keyword Call", "greet(name='Ada')", "Ada", "Call greet using the keyword name and value Ada.", "Keyword arguments identify a parameter by name."},
            {"Multiple Returns", "def pair(x): return x, x * 2", "two values", "Return x and twice x from pair.", "Python packages multiple returned values in a tuple."},
            {"Local Variables", "def score(): points = 3\n return points", "3", "Return the local points value from score.", "A function's indented body owns its local names."},
            {"Lambda", "triple = lambda x: x * 3", "function value", "Store a small function that triples x.", "lambda creates a compact function expression."},
            {"Map Function", "doubled = list(map(int, words))", "integer list", "Convert every word to an integer.", "map applies a function to each item."},
            {"Filter Function", "alive = list(filter(bool, flags))", "truthy flags", "Keep truthy values from flags.", "filter retains items accepted by the function."},
            {"Function Boss", "print(sum(map(int, words)))", "integer total", "Convert words to integers and print their sum.", "map converts each value before sum computes the total."},
            {"Import Math", "import math", "math tools ready", "Import Python's math module.", "import makes a module's tools available."},
            {"Square Root", "root = math.sqrt(81)", "9.0", "Find the square root of 81.", "sqrt is provided by the math module."},
            {"Ceiling", "top = math.ceil(4.2)", "5", "Round 4.2 upward to an integer.", "ceil rounds toward positive infinity."},
            {"Floor", "bottom = math.floor(4.8)", "4", "Round 4.8 downward to an integer.", "floor rounds toward negative infinity."},
            {"Random Choice", "pick = random.choice(items)", "one item", "Choose one random item from items.", "choice selects an element from a sequence."},
            {"Module Alias", "import random as rng", "rng available", "Import random using the short name rng.", "as gives an imported module an alias."},
            {"Date Parts", "today = date.today()", "current date", "Get today's date.", "today returns the current local date."},
            {"Path Join", "path = os.path.join('save', 'game.txt')", "save/game.txt", "Join the save folder and game.txt path parts.", "path.join uses the platform's path separator."},
            {"Module Use", "print(math.sqrt(20))", "4.47", "Print the square root of 20.", "sqrt is evaluated before print displays the result."},
            {"Exception Catch", "value = int(text)", "integer value", "Convert text to an integer.", "int converts digit text into a whole number."},
            {"Finally Block", "try: open_file()\nfinally: close_file()", "file closed", "Always close the file after trying to open it.", "finally runs whether the try succeeds or fails."},
            {"Raise Error", "raise ValueError('bad')", "error raised", "Raise a ValueError with the message bad.", "raise signals that an operation failed."},
            {"Assertions", "assert hp >= 0", "check passes", "Require hp to be at least zero.", "assert raises an error when its condition is false."},
            {"Context Manager", "with open(path) as f: pass", "file opened safely", "Open path inside a managed with block.", "with arranges cleanup when the block ends."},
            {"Safe Conversion", "age = int(text)", "integer age", "Convert text into age as an integer.", "int converts digit text into a whole number."},
            {"Validation", "assert name", "name present", "Require name to be nonempty.", "assert raises an error when its condition is false."},
            {"Error Recovery", "result = 10 / n", "quotient", "Divide 10 by n and store the result.", "The division operator computes the quotient."},
            {"Error Boss", "assert name", "name present", "Require name to be nonempty.", "The assertion checks a condition and reports failure when false."},
            {"Classes", "class Hero: pass", "Hero type", "Define a class named Hero.", "class begins a new object type."},
            {"Constructor", "def __init__(self): pass", "object ready", "Define an initializer with no extra arguments.", "__init__ runs when a new instance is created."},
            {"Review Boss", "print(sum(range(1, 5)))", "10", "Print the sum of numbers from 1 through 4.", "The generator supplies values to sum without building a list."}
        };

        int id = 8;
        for (String[] stage : stages) {
            boolean boss = id % 10 == 0;
            List<Puzzle> puzzles = new ArrayList<>();
            String[] solution = tokens(stage[1]);
            puzzles.add(new Puzzle(stage[3], solution, new String[]{"if", "for", "print", "x", "0"}, stage[4]));
            if (boss) {
                String[] challenge = bossCode(id);
                puzzles.add(new Puzzle(bossPrompt(id), challenge,
                        new String[]{"if", "for", "print", "x", "0"}, bossExplanation(id)));
            }
            String title = boss ? stage[0] + " Boss" : stage[0];
            LEVELS.add(new Level(id, title, "Practice " + stage[0].toLowerCase() + " in a short Python spell.",
                    stage[1], stage[2], puzzles, boss));
            id++;
        }
    }

    private static String[] tokens(String code) {
        Matcher matcher = PYTHON_TOKEN.matcher(code.replace("\n", " "));
        List<String> result = new ArrayList<>();
        while (matcher.find()) result.add(matcher.group());
        if (result.size() > Puzzle.GRID_SIZE) {
            throw new IllegalStateException("Stage spell exceeds tile grid: " + code);
        }
        return result.toArray(new String[0]);
    }

    /** A compact, harder composition for each decade boss. Tokens remain literal answer tiles. */
    private static String[] bossCode(int id) {
        switch (id) {
            case 10: return tokens("print('abra'.upper())");
            case 20: return tokens("print(max(3, 8) ** 2)");
            case 30: return tokens("print(hp > 0 and key)");
            case 40: return tokens("print(sum(range(5)))");
            case 50: return tokens("print(sum(nums) * 2)");
            case 60: return tokens("print(hero.get('hp', 0))");
            case 70: return tokens("print(sum(map(int, words)))");
            case 80: return tokens("print(math.ceil(4.1))");
            case 90: return tokens("print(len(set(nums)))");
            case 100: return tokens("print(sum(map(len, words)))");
            default: throw new IllegalArgumentException("Not a boss stage: " + id);
        }
    }

    private static String bossPrompt(int id) {
        switch (id) {
            case 10: return "Boss: print abra after converting it to uppercase.";
            case 20: return "Boss: print the largest of 3 and 8, squared.";
            case 30: return "Boss: print whether hp is positive and key is true.";
            case 40: return "Boss: print the sum of numbers 0 through 4 using range.";
            case 50: return "Boss: print twice the sum of nums.";
            case 60: return "Boss: print hero hp, using zero if it is missing.";
            case 70: return "Boss: convert each word to int and print their sum.";
            case 80: return "Boss: round 4.1 upward using math.ceil, then print it.";
            case 90: return "Boss: print how many distinct values are in nums.";
            default: return "Boss: apply len to each word and print the total length.";
        }
    }

    private static String bossExplanation(int id) {
        switch (id) {
            case 10: return "The string method runs first, and print displays its result.";
            case 20: return "max chooses 8 before the exponent operator squares it.";
            case 30: return "Both facts are checked before all combines them.";
            case 40: return "range supplies values to sum without building a list.";
            case 50: return "sum adds the items before multiplication doubles the result.";
            case 60: return "get returns the saved value or its fallback.";
            case 70: return "map converts every text value before sum adds the integers.";
            case 80: return "ceil rounds its argument upward to the next integer.";
            case 90: return "set removes duplicates, and len counts the remaining values.";
            default: return "map applies len to each word before sum totals the lengths.";
        }
    }

    public static List<Level> all() {
        return LEVELS;
    }

    public static int count() {
        return LEVELS.size();
    }

    public static Level get(int id) {
        return LEVELS.get(id - 1);
    }

    /** A one-puzzle practice level used by the guided tutorial. */
    public static Level tutorial() {
        return TUTORIAL;
    }
}
