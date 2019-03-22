package Compiler.Symbol;

import Compiler.AST.TokenLocation;

import java.util.HashMap;
import java.util.Map;

public class GlobalSymolTable extends SymbolTable {

    public Map<String, ClassSymbol> classTable;
    public Map<String, PrimitiveSymbol> primitiveTable;

    public GlobalSymolTable() {
        super(null);
        classTable = new HashMap<>();
        primitiveTable = new HashMap<>();
        init();
    }

    public void addClass(String name, ClassSymbol classSymbol) {
        classTable.put(name, classSymbol);
    }
    public ClassSymbol getClass(String name) {
        return classTable.get(name);
    }
    public void addPrimitive(PrimitiveSymbol primitiveSymbol) {
        primitiveTable.put(primitiveSymbol.name, primitiveSymbol);
    }
    public PrimitiveSymbol getPrimitive(String name) {
        return primitiveTable.get(name);
    }

    void init() {
//        int, bool, void symbols
        addPrimitive(new PrimitiveSymbol("int"));
        addPrimitive(new PrimitiveSymbol("bool"));
        addPrimitive(new PrimitiveSymbol("void"));

//        null symbol
        ClassSymbol nullSymbol = new ClassSymbol("null");
        nullSymbol.location = new TokenLocation(0,0);
        nullSymbol.symbolTable = new SymbolTable(this);
        addClass("null", nullSymbol);

//        string symbol
        ClassSymbol stringSymbol = new ClassSymbol("string");
        stringSymbol.location = new TokenLocation(0, 0);
        stringSymbol.symbolTable = new SymbolTable(this);
        stringSymbol.symbolTable.addFunction("length", _stringLength());
        stringSymbol.symbolTable.addFunction("substring", _stringSubstring());
        stringSymbol.symbolTable.addFunction("parseInt", _stringParseInt());
        stringSymbol.symbolTable.addFunction("ord", _stringOrd());
        addClass("string", stringSymbol);

//        builtin functions
        addFunction("print", _globalPrint());
        addFunction("println", _globalPrintln());
        addFunction("getString", _globalGetString());
        addFunction("getInt", _globalGetInt());
        addFunction("toString", _globalToString());
    }

    private Type voidType() {
        return new PrimitiveType("void", getPrimitive("void"));
    }
    private Type intType() {
        return new PrimitiveType("int", getPrimitive("int"));
    }
    private Type stringType() {
        return new ClassType("string", getClass("string"));
    }

    private FunctionSymbol _stringLength() {
        FunctionSymbol ret = new FunctionSymbol("string.length");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType();
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType());
        ret.parameterNameList.add("this");
        return ret;
    }

    private FunctionSymbol _stringSubstring() {
        FunctionSymbol ret = new FunctionSymbol("string.substring");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = stringType();
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType());
        ret.parameterNameList.add("this");
        ret.parameterTypeList.add(intType());
        ret.parameterNameList.add("left");
        ret.parameterTypeList.add(intType());
        ret.parameterNameList.add("right");
        return ret;
    }

    private FunctionSymbol _stringParseInt() {
        FunctionSymbol ret = new FunctionSymbol("string.parseInt");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType();
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType());
        ret.parameterNameList.add("this");
        return ret;
    }

    private FunctionSymbol _stringOrd() {
        FunctionSymbol ret = new FunctionSymbol("string.ord");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType();
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType());
        ret.parameterNameList.add("this");
        ret.parameterTypeList.add(intType());
        ret.parameterNameList.add("pos");
        return ret;
    }

    private FunctionSymbol _globalPrint() {
        FunctionSymbol ret = new FunctionSymbol("print");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = voidType();
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType());
        ret.parameterNameList.add("str");
        return ret;
    }

    private FunctionSymbol _globalPrintln() {
        FunctionSymbol ret = new FunctionSymbol("println");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = voidType();
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType());
        ret.parameterNameList.add("str");
        return ret;
    }

    private FunctionSymbol _globalGetString() {
        FunctionSymbol ret = new FunctionSymbol("getString");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = stringType();
        ret.isGlobal = true;
        return ret;
    }

    private FunctionSymbol _globalGetInt() {
        FunctionSymbol ret = new FunctionSymbol("getInt");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType();
        ret.isGlobal = true;
        return ret;
    }

    private FunctionSymbol _globalToString() {
        FunctionSymbol ret = new FunctionSymbol("toString");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = stringType();
        ret.isGlobal = true;
        ret.parameterTypeList.add(intType());
        ret.parameterNameList.add("i");
        return ret;
    }
}
