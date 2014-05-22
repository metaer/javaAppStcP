package com.company;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException, AgentPlusParseException {
        String fileName = args[0];
        File file = new File(fileName);
        FileInputStream is = new FileInputStream(file);

        byte[] data = new byte[(int)file.length()];
        is.read(data);
        is.close();

        String text =  new String(data, "CP1251");

        AgentPlusServerParser agPlus = new AgentPlusServerParser(text);
        agPlus.parse();

        for (Table t : agPlus.tables) {
            System.out.println(t.header.name + " " + t.rows.size());
            if (t.sub != null)
                System.out.println("\t" + t.sub.name);
        }
    }
}
