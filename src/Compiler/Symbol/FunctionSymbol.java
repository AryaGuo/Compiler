package Compiler.Symbol;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FunctionSymbol extends Symbol {
    public Type returnType;
    public List<Type> parameterTypeList;
    public List<String> parameterNameList;
    public SymbolTable symbolTable;

    public boolean withSideEffect;
    public boolean isGlobal;
    public Set<VariableSymbol> usedGlobals;
    public Set<FunctionSymbol> callee;
    public Set<FunctionSymbol> visited;

    public FunctionSymbol(String name) {
        this.name = name;
        parameterTypeList = new LinkedList<>();
        parameterNameList = new LinkedList<>();
        usedGlobals = new HashSet<>();
        callee = new HashSet<>();
        visited = new HashSet<>();
    }

    public void finish() {
        visited.clear();
        dfsSideEffect(this);
    }

    private void dfsSideEffect(FunctionSymbol functionSymbol) {
        if (visited.contains(functionSymbol) || withSideEffect) {
            return;
        }
        visited.add(functionSymbol);
        for (FunctionSymbol fs : functionSymbol.callee) {
            if (fs.withSideEffect) {
                withSideEffect = true;
                break;
            }
        }
    }
}
