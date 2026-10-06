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
    private static ArrayList<String> vfsFiles = new ArrayList<>();
    private static String currentPath = "/";


    public static JFrame createWindow() {
        String user = System.getProperty("user.name");
        String host = "localhost";

        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            host = "unknown";
        }

        String title = "Эмулятор - [" + user + "@" + host + "]";
        JFrame window = new JFrame(title);
        window.setSize(500, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        String lookupPath = currentPath;
        if (!tokens.isEmpty()) {
            String target = (String)tokens.get(0);
            if (target.startsWith("/")) {
                lookupPath = target;
            } else {
                lookupPath = currentPath.equals("/") ? "/" + target : currentPath + "/" + target;
            }
        }

        textArea.append("Содержимое папки " + lookupPath + ":\n");
        boolean hasFiles = false;
        String prefix = lookupPath.equals("/") ? "/" : lookupPath + "/";

        for(String path : vfsFiles) {
            if (path.startsWith(prefix)) {
                String remainder = path.substring(prefix.length());
                if (!remainder.contains("/")) {
                    textArea.append("  " + remainder + "\n");
                    hasFiles = true;
                }
            }
        }

        if (!hasFiles) {
            textArea.append("  [Папка пуста]\n");
        }
    }

    public static void cd(ArrayList<String> tokens, JTextArea textArea) {
        if (tokens.isEmpty()) {
            currentPath = "/";
            textArea.append("Совершен переход в корневую директорию" + "\n");
        } else {
            String target = (String)tokens.get(0);
            if (target.equals("/")) {
                currentPath = "/";
            } else {
                String destinationPath;
                if (target.startsWith("/")) {
                    destinationPath = target;
                } else {
                    destinationPath = currentPath.equals("/") ? "/" + target : currentPath + "/" + target;
                }

                if (vfsFiles.contains(destinationPath)) {
                    currentPath = destinationPath;
                    textArea.append("Вы перешли в папку " + destinationPath + "\n");
                } else {
                    textArea.append("Ошибка: папка '" + target + "' не найдена в VFS.\n");
                }

            }
        }

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
                } catch (Exception exception) {
                    textArea.append("[Ошибка VFS] Не удалось прочитать скрипт:\n");
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

    private static void scanDirectory(File root, String virtualPrefix) {
        File[] list = root.listFiles();
        if (list != null) {
            for(File f : list) {
                String vPath = virtualPrefix + (virtualPrefix.equals("/") ? "" : "/") + f.getName();
                vfsFiles.add(vPath);
                if (f.isDirectory()) {
                    scanDirectory(f, vPath);
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {vfsPath = args[0];}
        if (args.length > 1) {script = args[1];}

        File rootDir;
        if (!vfsPath.isEmpty()) {
            rootDir = new File(vfsPath);
        } else {
            String currentPathStr = new File(".").getAbsolutePath();
            String rootPath = currentPathStr.substring(0, currentPathStr.indexOf("JavaTerminalEmulator") + "JavaTerminalEmulator".length());
            rootDir = new File(rootPath);
        }
        vfsFiles.add("/");
        scanDirectory(rootDir, "");

        JFrame window = createWindow();
        JTextArea textArea = createTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        JTextField inputField = createTextField();
        window.add(scrollPane, "Center");
        window.add(inputField, "North");
        window.setVisible(true);
        inputField.addActionListener((e) -> {
            String input = inputField.getText().trim();
            if (!input.isEmpty()) {
                executeCommand(input, textArea);
            }
            inputField.setText("");
        });

        executeStartScript(script, textArea);
    }
}
