package FrontEnd;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // System.err.println("You can see the print.");
        List<String> fileArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String str = args[i];
            if (str.equals("-h")) {
                print();
            } else {
                fileArgs.add(str);
            }
        }

        if (fileArgs.size() != 1) {
            System.err.println("ERROR: Command Incorrect or invalid path.");
            return;
        }

        String fileName = fileArgs.get(0);
        // System.err.println(fileName);

        BufferedReader reader;

        try {
            //reader = new BufferedReader(new FileReader("/Users/chenchenzikang/Downloads/COMP 412/test_input/report1.i"));
            //reader = new BufferedReader(new FileReader("/Users/chenchenzikang/Downloads/COMP 412/Lab1/test_input/report4.i"));
            reader = new BufferedReader(new FileReader(fileName));
        } catch (Exception e) {
            System.err.println("ERROR: Unable to open the file.");
            return;
        }

       Scanner scanner = new Scanner(reader);
       Parser parser = new Parser(scanner);
       parser.parseIR();
       if (scanner.error == 0 && parser.error == 0) {
           Renamer renamer = new Renamer(parser);
           renamer.rename();
           Scheduler scheduler = new Scheduler(renamer);
           scheduler.schedule();
       }

//         if (options.contains("-h")) {
//             print();
//         } else if (k != -1) {
//             if (k < 3 || k > 64) {
//                 System.err.println("ERROR: Invalid K.");
//             } else {
//                 Scanner scanner = new Scanner(reader);
//                 Parser parser = new Parser(scanner);
//                 parser.parseIR();
//                 if (scanner.error == 0 && parser.error == 0) {
//                     Allocator allocator = new Allocator(parser, k);
//                     allocator.allocate();
//
//                     IR ir = parser.irList.getHead();
//                     while (ir != null) {
//                         allocator.print(ir);
//                         ir = ir.getNext();
//                     }
//                 }
//             }
//         } else if (options.contains("-x")) {
//             Scanner scanner = new Scanner(reader);
//             Parser parser = new Parser(scanner);
//             parser.parseIR();
//             if (scanner.error == 0 && parser.error == 0) {
//                 // start renaming.
//                 Renamer renamer = new Renamer(parser);
//                 renamer.rename();
//             }
//         }
    }

    private static void print() {
        String msg = "Required arguments:\n" +
                "\t filename  is the pathname (absolute or relative) to the input file\n" +
                "\n" +
                "Optional flags:\n" +
                "\t -h \t prints this message" +
                "At most one of the following three flags:\n" +
                "\t -x k";
        System.out.println(msg);
    }
}
