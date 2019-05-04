package Compiler.Symbol;

import Compiler.Config;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public Map<String, FunctionSymbol> functionTable;
    public Map<String, VariableSymbol> variableTable;
    public SymbolTable parent;

    Map<String, Integer> offsets;
    Integer curOffset;

    public SymbolTable(SymbolTable parent) {
        functionTable = new HashMap<>();
        variableTable = new HashMap<>();
        this.parent = parent;
        offsets = new HashMap<>();
        curOffset = 0;
    }

    public void addFunction(String name, FunctionSymbol functionSymbol) {
        functionTable.put(name, functionSymbol);
    }

    public void addVariable(String name, VariableSymbol variableSymbol) {
        variableTable.put(name, variableSymbol);
        offsets.put(name, curOffset);
        curOffset += Config.REGISTER_WIDTH;
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

    public int getOffset(String name) {
        return offsets.get(name);
    }
}
