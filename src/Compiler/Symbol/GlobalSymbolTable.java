package Compiler.Symbol;

import Compiler.AST.TokenLocation;

import java.util.HashMap;
import java.util.Map;

public class GlobalSymbolTable extends SymbolTable {

    public Map<String, ClassSymbol> classTable;
    public Map<String, PrimitiveSymbol> primitiveTable;

    public PrimitiveSymbol intType;
    public PrimitiveSymbol boolType;
    public PrimitiveSymbol voidType;
    public ClassSymbol stringType;
    public ClassSymbol nullType;

    public GlobalSymbolTable() {
        super(null);
        classTable = new HashMap<>();
        primitiveTable = new HashMap<>();
        init();
    }

    public boolean containClass(String name) {
        return classTable.containsKey(name);
    }

    public void addClass(String name, ClassSymbol classSymbol) {
        classTable.put(name, classSymbol);
    }

    public ClassSymbol getClass(String name) {
        return classTable.get(name);
    }

    public void addPrimitive(String name) {
        primitiveTable.put(name, new PrimitiveSymbol(name));
    }

    public PrimitiveSymbol getPrimitive(String name) {
        return primitiveTable.get(name);
    }

    void init() {
//        int, bool, void symbols
        addPrimitive("int");
        addPrimitive("bool");
        addPrimitive("void");

//        null symbol
        ClassSymbol nullSymbol = new ClassSymbol("null") {
            @Override
            public boolean match(Type type) {
                if (type instanceof ArrayType) return true;
                if (type instanceof ClassSymbol) {
                    if (((ClassSymbol) type).name.equals("string")) return false;
                    else return true;
                }
                return false;
            }
        };
        nullSymbol.location = new TokenLocation(0, 0);
        nullSymbol.symbolTable = new SymbolTable(this);
        addClass("null", nullSymbol);

//        string symbol
        ClassSymbol stringSymbol = new ClassSymbol("string") {
            @Override
            public boolean match(Type type) {
                if (type instanceof ClassSymbol) return this.name.equals(((ClassSymbol) type).name);
                else return false;
            }
        };
        stringSymbol.location = new TokenLocation(0, 0);
        stringSymbol.symbolTable = new SymbolTable(this);
        addClass("string", stringSymbol);

        intType = getPrimitive("int");
        boolType = getPrimitive("bool");
        voidType = getPrimitive("void");
        stringType = getClass("string");
        nullType = getClass("null");

        stringSymbol.symbolTable.addFunction("length", string_Length());
        stringSymbol.symbolTable.addFunction("substring", string_Substring());
        stringSymbol.symbolTable.addFunction("parseInt", string_ParseInt());
        stringSymbol.symbolTable.addFunction("ord", string_Ord());


//        builtin functions
        addFunction("print", global_Print());
        addFunction("println", global_Println());
        addFunction("getString", global_GetString());
        addFunction("getInt", global_GetInt());
        addFunction("toString", global_ToString());

    }

    private FunctionSymbol string_Length() {
        FunctionSymbol ret = new FunctionSymbol("string.length");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType;
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType);
        ret.parameterNameList.add("this");
        return ret;
    }

    private FunctionSymbol string_Substring() {
        FunctionSymbol ret = new FunctionSymbol("string.substring");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = stringType;
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType);
        ret.parameterNameList.add("this");
        ret.parameterTypeList.add(intType);
        ret.parameterNameList.add("left");
        ret.parameterTypeList.add(intType);
        ret.parameterNameList.add("right");
        return ret;
    }

    private FunctionSymbol string_ParseInt() {
        FunctionSymbol ret = new FunctionSymbol("string.parseInt");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType;
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType);
        ret.parameterNameList.add("this");
        return ret;
    }

    private FunctionSymbol string_Ord() {
        FunctionSymbol ret = new FunctionSymbol("string.ord");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType;
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType);
        ret.parameterNameList.add("this");
        ret.parameterTypeList.add(intType);
        ret.parameterNameList.add("pos");
        return ret;
    }

    private FunctionSymbol global_Print() {
        FunctionSymbol ret = new FunctionSymbol("print");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = voidType;
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType);
        ret.parameterNameList.add("str");
        ret.withSideEffect = true;
        return ret;
    }

    private FunctionSymbol global_Println() {
        FunctionSymbol ret = new FunctionSymbol("println");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = voidType;
        ret.isGlobal = true;
        ret.parameterTypeList.add(stringType);
        ret.parameterNameList.add("str");
        ret.withSideEffect = true;
        return ret;
    }

    private FunctionSymbol global_GetString() {
        FunctionSymbol ret = new FunctionSymbol("getString");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = stringType;
        ret.isGlobal = true;
        ret.withSideEffect = true;
        return ret;
    }

    private FunctionSymbol global_GetInt() {
        FunctionSymbol ret = new FunctionSymbol("getInt");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = intType;
        ret.isGlobal = true;
        ret.withSideEffect = true;
        return ret;
    }

    private FunctionSymbol global_ToString() {
        FunctionSymbol ret = new FunctionSymbol("toString");
        ret.location = new TokenLocation(0, 0);
        ret.returnType = stringType;
        ret.isGlobal = true;
        ret.parameterTypeList.add(intType);
        ret.parameterNameList.add("i");
        return ret;
    }
}
