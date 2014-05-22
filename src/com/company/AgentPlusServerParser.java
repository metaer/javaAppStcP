package com.company;

import java.util.*;

class Header {
    public String name;
    public String[] columnNames;

    public Header(String name) {
        this.name = name;
    }
}

class Table {
    public Table(String name) {
        header = new Header(name);
    }

    public Table(Header header) {
        this.header = header;
    }

    public Header header;
    public Header sub;
    public List<Row> rows = new ArrayList<Row>();
}

class Row {
    public Row(Header header) {
        this.header = header;
    }

    public Header header;
    public String[] values;
    public Table sub;
}

class AgentPlusParseException extends Throwable {
    public AgentPlusParseException(String s) {
        super(s);
    }
}

public class AgentPlusServerParser {
    private final String text;
    private Table currentTable;
    private Stack<Table> tablesStack = new Stack<Table>();

    public List<Table> tables = new ArrayList<Table>();

    public AgentPlusServerParser(String fromServer) {
        this.text = fromServer;
    }

    public void parse() throws AgentPlusParseException {
        Scanner scanner = new Scanner(text);
        String line;

        // skip first line
        line = scanner.nextLine();
        assert (line.startsWith("agentp_data\tto_ppc"));

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.startsWith("//")) // comment
                continue;

            parseLine(line);
        }
    }

    private void parseLine(String line) throws AgentPlusParseException {
        String[] params = line.split("\t");
        if (params.length == 0) // skip line
            return;

        String tag = params[0];
        if (tag.equals("<Begin>"))
            parseBegin(params);
        else if (tag.equals("<End>"))
            parseEnd(params);
        else if (tag.equals("<Sub>"))
            parseSub(params);
        else
            parseData(params);
    }

    private void parseBegin(String[] params) throws AgentPlusParseException {
        String name = params[1];
        if (currentTable == null) {
            currentTable = new Table(parseHeader(params));
            tables.add(currentTable);
        } else if (name.equals(currentTable.sub.name)) { // maybe sub begins
            tablesStack.push(currentTable);
            // create sub rows
            Row row = currentTable.rows.get(currentTable.rows.size() - 1);
            row.sub = new Table(currentTable.sub);
            currentTable = row.sub;
        } else {
            throw new AgentPlusParseException("Start tag for " + name + " is not for current table" + currentTable.header.name);
        }
    }

    private void parseEnd(String[] params) throws AgentPlusParseException {
        assert (currentTable != null);

        String name = params[1];
        if (name.equals(currentTable.header.name)) {// close table
            currentTable = !tablesStack.isEmpty() ? tablesStack.pop() : null;
        } else {
            throw new AgentPlusParseException("End tag for " + name + " is not for current table" + currentTable.header.name);
        }
    }

    private void parseSub(String[] params) {
        assert (currentTable != null);

        currentTable.sub = parseHeader(params);
    }

    private void parseData(String[] params) {
        assert (currentTable != null);
//        assert (params.length == currentTable.header.columnNames.length);

        Row row = new Row(currentTable.header);
        row.values = params;

        currentTable.rows.add(row);
    }

    private static Header parseHeader(String[] params) {
        Header header = new Header(params[1]);
        header.columnNames = params[2].substring("Struct:".length()).split(","); // TODO
        return header;
    }
}
