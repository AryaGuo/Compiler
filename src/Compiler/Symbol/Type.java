package Compiler.Symbol;

public interface Type {
    boolean match(Type type);

    int getBytes();
}
