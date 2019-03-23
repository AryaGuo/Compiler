package Compiler.Symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public Map<String, FunctionSymbol> functionTable;
    public Map<String, VariableSymbol> variableTable;
    public SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        functionTable = new HashMap<>();
        variableTable = new HashMap<>();
        this.parent = parent;
    }

    public void addFunction(String name, FunctionSymbol functionSymbol) {
        functionTable.put(name, functionSymbol);
    }

    public void addVariable(String name, VariableSymbol variableSymbol) {
        variableTable.put(name, variableSymbol);
    }

    public FunctionSymbol getFunction(String name) {
        return functionTable.get(name);
    }

    public VariableSymbol getVariable(String name) {
        return variableTable.get(name);
    }

    public boolean containFunction(String name) {
        return functionTable.containsKey(name);
    }

    public boolean containVariable(String name) {
        return variableTable.containsKey(name);
    }
}
