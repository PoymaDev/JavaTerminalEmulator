package org.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class Main {
    private static String vfsPath = "";
    private static String script = "";
    private static ArrayList<String> vfsFiles = new ArrayList();
    private static String currentPath = "/";


    public static JFrame createWindow() {
        String user = System.getProperty("user.name");
        String host = "localhost";

        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception var4) {
        }

        String title = "Эмулятор - [" + user + "@" + host + "]";
        JFrame window = new JFrame(title);
        window.setSize(500, 600);
        window.setDefaultCloseOperation(3);
        window.setLayout(new BorderLayout());
        return window;
    }

    public static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setEditable(false);
        textArea.append("Эмулятор запущен..");
        return textArea;
    }

    public static JTextField createTextField() {
        JTextField inputField = new JTextField();
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.WHITE);
        return inputField;
    }

    public static void exit(JTextArea textArea) {
        textArea.append("Выходим из эмулятора");
        System.exit(0);
    }

    public static void ls(ArrayList<String> tokens, JTextArea textArea) {
        textArea.append("Выполнена команда ls. Аргументы " + tokens.toString() + "\n");
    }

    public static void cd(ArrayList<String> tokens, JTextArea textArea) {
        textArea.append("Выполнена команда cd. Аргументы " + tokens.toString() + "\n");
    }

    public ArrayList<String> cdParser(String input) {
        ArrayList<String> tokens = new ArrayList();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);

        while(matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                tokens.add(matcher.group(2));
            }
        }

        return tokens;
    }


    private static void executeCommand(String input, JTextArea textArea) {
        ArrayList<String> tokens = (new Main()).cdParser(input);
        if (tokens.isEmpty()) {
            textArea.append("$ \n");
        } else {
            String command = (String)tokens.get(0);
            tokens.remove(0);
            textArea.append("$ " + input + "\n");
            switch (command) {
                case "exit":
                    exit(textArea);
                    break;
                case "ls":
                    ls(tokens, textArea);
                    break;
                case "cd":
                    cd(tokens, textArea);
                    break;
                case "conf-dump":
                    textArea.append("vfs-path = " + vfsPath + "\n");
                    textArea.append("script = " + script + "\n");
                    break;
                default:
                    textArea.append(" Такой команды не существует\n");
            }

        }
    }

    private static void executeStartScript(String script, JTextArea textArea) {
        if (!script.isEmpty()) {
            if (script.endsWith(".txt")) {
                try {
                    File file = new File(script);
                    Scanner fileScanner = new Scanner(file);

                    while(fileScanner.hasNextLine()) {
                        String cmd = fileScanner.nextLine().trim();
                        if (!cmd.isEmpty() && !cmd.startsWith("//")) {
                            executeCommand(cmd, textArea);
                        }
                    }

                    fileScanner.close();
                } catch (Exception var7) {
                }
            } else {
                String[] commands = script.split(";");

                for(String cmd : commands) {
                    cmd = cmd.trim();
                    if (!cmd.isEmpty() && !cmd.startsWith("//")) {
                        executeCommand(cmd, textArea);
                    }
                }
            }
        }

    }


    public static void main(String[] args) {
        if (args.length > 0) {
            vfsPath = args[0];
        }

        if (args.length > 1) {
            script = args[1];
        }

        JFrame window = createWindow();
        JTextArea textArea = createTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        JTextField inputField = createTextField();
        window.add(scrollPane, "Center");
        window.add(inputField, "North");
        window.setVisible(true);
        inputField.addActionListener((e) -> {
            String vvod = inputField.getText().trim();
            if (!vvod.isEmpty()) {
                executeCommand(vvod, textArea);
            }
            inputField.setText("");
        });


        executeStartScript(script, textArea);
    }
}
