package sourcerer.model;

import java.util.Arrays;
import java.util.List;

/** Python Syntax Grimoire content. Add your own DictionaryEntry objects here. */
public final class SyntaxDictionary {

    private static final List<DictionaryEntry> ENTRIES = Arrays.asList(
            new DictionaryEntry("Variables", "Variable (=)", "name = value",
                    "Stores a value in a named box. Python works out the type by itself.",
                    "x = 5   # x holds 5",
                    new String[]{"x", "=", "5"}, "x holds 5"),
            new DictionaryEntry("Variables", "Text (str)", "name = \"text\"",
                    "Text goes between quotes. Single or double quotes both work.",
                    "name = \"Hero\"",
                    new String[]{"name", "=", "\"Hero\""}, "name holds Hero"),
            new DictionaryEntry("Data Types", "Float", "name = 2.5",
                    "A number with a decimal point.",
                    "speed = 2.5",
                    new String[]{"speed", "=", "2.5"}, "speed holds 2.5"),
            new DictionaryEntry("Data Types", "Bool", "name = True",
                    "True or False, always with a capital first letter.",
                    "alive = True",
                    new String[]{"alive", "=", "True"}, "alive holds True"),
            new DictionaryEntry("Data Types", "int() and str()", "int(text)  str(number)",
                    "Convert between text and numbers.",
                    "num = int(\"7\")   # num = 7",
                    new String[]{"num", "=", "int", "(", "\"7\"", ")"}, "7"),
            new DictionaryEntry("Arithmetic", "Addition (+)", "a + b",
                    "Adds numbers. With text it joins the pieces together.",
                    "total = 8 + 12   # total = 20",
                    new String[]{"total", "=", "8", "+", "12"}, "20"),
            new DictionaryEntry("Arithmetic", "Floor Division (//)", "a // b",
                    "Divides and drops the decimals. A single / keeps them.",
                    "q = 17 // 5   # q = 3",
                    new String[]{"q", "=", "17", "//", "5"}, "3"),
            new DictionaryEntry("Arithmetic", "Modulo (%)", "a % b",
                    "The remainder after dividing.",
                    "r = 17 % 5   # r = 2",
                    new String[]{"r", "=", "17", "%", "5"}, "2"),
            new DictionaryEntry("Arithmetic", "Power (**)", "a ** b",
                    "Raises a number to a power.",
                    "p = 2 ** 3   # p = 8",
                    new String[]{"p", "=", "2", "**", "3"}, "8"),
            new DictionaryEntry("Input & Output", "print()", "print(value)",
                    "Shows a value on the screen and moves to the next line.",
                    "print(\"Hi\")",
                    new String[]{"print", "(", "\"Hi\"", ")"}, "Hi"),
            new DictionaryEntry("Input & Output", "input()", "name = input()",
                    "Waits for the player to type. The answer always arrives as text.",
                    "name = input()",
                    new String[]{"name", "=", "input", "(", ")"}, "name holds what was typed"),
            new DictionaryEntry("Conditionals", "if", "if condition: action",
                    "Runs the action only when the condition is True. Don't forget the colon.",
                    "if hp > 0: print(\"alive\")",
                    new String[]{"if", "hp", ">", "0", ":", "print", "(", "\"alive\"", ")"}, "alive"),
            new DictionaryEntry("Conditionals", "Comparisons", "==  !=  <  >  <=  >=",
                    "Compare two values. == asks 'equal?', while = stores a value.",
                    "age >= 18",
                    new String[]{"age", ">=", "18"}, "True when age is 18 or more"),
            new DictionaryEntry("Loops", "for ... in range()", "for i in range(n):",
                    "Repeats code n times. i counts 0, 1, 2 ...",
                    "for i in range(3): print(i)",
                    new String[]{"for", "i", "in", "range", "(", "3", ")", ":", "print", "(", "i", ")"}, "0  1  2"),
            new DictionaryEntry("Loops", "while", "while condition:",
                    "Repeats while the condition stays True.",
                    "while x < 5: x += 1",
                    new String[]{"while", "x", "<", "5", ":", "x", "+=", "1"}, "x ends at 5"),
            new DictionaryEntry("Lists", "List ([ ])", "name = [a, b, c]",
                    "Stores many values in one variable. The first item is at index 0.",
                    "nums = [1, 2, 3]",
                    new String[]{"nums", "=", "[", "1", ",", "2", ",", "3", "]"}, "three items"),
            new DictionaryEntry("Lists", "len() and append()", "len(list)  list.append(x)",
                    "len counts the items. append adds one to the end.",
                    "nums.append(4)",
                    new String[]{"nums", ".", "append", "(", "4", ")"}, "nums gets a 4 at the end"),
            new DictionaryEntry("Functions", "def", "def name(params): body",
                    "Defines a reusable function. return sends a result back.",
                    "def add(a, b): return a + b",
                    new String[]{"def", "add", "(", "a", ",", "b", ")", ":", "return", "a", "+", "b"}, "add(2, 3) gives 5")
    );

    private SyntaxDictionary() {
    }

    public static List<DictionaryEntry> entries() {
        return ENTRIES;
    }
}
