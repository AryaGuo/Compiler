package Compiler.Symbol;

import java.util.LinkedList;
import java.util.List;

public class FunctionSymbol extends Symbol {
    public Type returnType;
    public List<Type> parameterTypeList;
    public List<String> parameterNameList;
    public SymbolTable symbolTable;

    public boolean isGlobal;

    public FunctionSymbol(String name) {
        this.name = name;
        parameterTypeList = new LinkedList<>();
        parameterNameList = new LinkedList<>();
    }
}
