// Generated from /Users/guowenxuan/codelab/reCompiler/src/Compiler.Parser/Mx.g4 by ANTLR 4.7.2
package Compiler.Parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MxLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, BOOL_LITERAL=33, NULL_LITERAL=34, STRING=35, INT=36, BOOL=37, 
		VOID=38, CLASS=39, IF=40, WHILE=41, FOR=42, BREAK=43, CONTINUE=44, RETURN=45, 
		THIS=46, NEW=47, ELSE=48, IDENTIFIER=49, INTEGER_LITERAL=50, STRING_LITERAL=51, 
		LINECOMMENT=52, BLOCKCOMMENT=53, WHITESPACE=54;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
			"T__25", "T__26", "T__27", "T__28", "T__29", "T__30", "T__31", "BOOL_LITERAL", 
			"NULL_LITERAL", "STRING", "INT", "BOOL", "VOID", "CLASS", "IF", "WHILE", 
			"FOR", "BREAK", "CONTINUE", "RETURN", "THIS", "NEW", "ELSE", "IDENTIFIER", 
			"INTEGER_LITERAL", "STRING_LITERAL", "CHAR", "LINECOMMENT", "BLOCKCOMMENT", 
			"WHITESPACE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "','", "'='", "'('", "')'", "'{'", "'}'", "'['", "']'", 
			"'++'", "'--'", "'.'", "'+'", "'-'", "'!'", "'~'", "'*'", "'/'", "'%'", 
			"'<<'", "'>>'", "'<'", "'>'", "'<='", "'>='", "'=='", "'!='", "'&'", 
			"'^'", "'|'", "'&&'", "'||'", null, "'null'", "'string'", "'int'", "'bool'", 
			"'void'", "'class'", "'if'", "'while'", "'for'", "'break'", "'continue'", 
			"'return'", "'this'", "'new'", "'else'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, "BOOL_LITERAL", 
			"NULL_LITERAL", "STRING", "INT", "BOOL", "VOID", "CLASS", "IF", "WHILE", 
			"FOR", "BREAK", "CONTINUE", "RETURN", "THIS", "NEW", "ELSE", "IDENTIFIER", 
			"INTEGER_LITERAL", "STRING_LITERAL", "LINECOMMENT", "BLOCKCOMMENT", "WHITESPACE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public MxLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Mx.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\28\u0150\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3"+
		"\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f"+
		"\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3"+
		"\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3"+
		"\31\3\32\3\32\3\32\3\33\3\33\3\33\3\34\3\34\3\34\3\35\3\35\3\36\3\36\3"+
		"\37\3\37\3 \3 \3 \3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\5\"\u00c5"+
		"\n\"\3#\3#\3#\3#\3#\3$\3$\3$\3$\3$\3$\3$\3%\3%\3%\3%\3&\3&\3&\3&\3&\3"+
		"\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3(\3)\3)\3)\3*\3*\3*\3*\3*\3*\3+\3+"+
		"\3+\3+\3,\3,\3,\3,\3,\3,\3-\3-\3-\3-\3-\3-\3-\3-\3-\3.\3.\3.\3.\3.\3."+
		"\3.\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\61\3\61\3\61\3\61\3\61\3\62\3"+
		"\62\7\62\u011a\n\62\f\62\16\62\u011d\13\62\3\63\6\63\u0120\n\63\r\63\16"+
		"\63\u0121\3\64\3\64\3\64\7\64\u0127\n\64\f\64\16\64\u012a\13\64\3\64\3"+
		"\64\3\65\3\65\3\65\3\66\3\66\3\66\3\66\7\66\u0135\n\66\f\66\16\66\u0138"+
		"\13\66\3\66\3\66\3\67\3\67\3\67\3\67\7\67\u0140\n\67\f\67\16\67\u0143"+
		"\13\67\3\67\3\67\3\67\3\67\3\67\38\68\u014b\n8\r8\168\u014c\38\38\4\u0128"+
		"\u0141\29\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33"+
		"\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67"+
		"\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61a\62c\63e\64g\65"+
		"i\2k\66m\67o8\3\2\b\4\2C\\c|\6\2\62;C\\aac|\3\2\62;\b\2$$^^ddppttvv\4"+
		"\2\f\f\17\17\5\2\13\f\17\17\"\"\2\u0156\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35"+
		"\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)"+
		"\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2"+
		"\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2"+
		"A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3"+
		"\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2"+
		"\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2"+
		"g\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\3q\3\2\2\2\5s\3\2\2\2\7u\3"+
		"\2\2\2\tw\3\2\2\2\13y\3\2\2\2\r{\3\2\2\2\17}\3\2\2\2\21\177\3\2\2\2\23"+
		"\u0081\3\2\2\2\25\u0083\3\2\2\2\27\u0086\3\2\2\2\31\u0089\3\2\2\2\33\u008b"+
		"\3\2\2\2\35\u008d\3\2\2\2\37\u008f\3\2\2\2!\u0091\3\2\2\2#\u0093\3\2\2"+
		"\2%\u0095\3\2\2\2\'\u0097\3\2\2\2)\u0099\3\2\2\2+\u009c\3\2\2\2-\u009f"+
		"\3\2\2\2/\u00a1\3\2\2\2\61\u00a3\3\2\2\2\63\u00a6\3\2\2\2\65\u00a9\3\2"+
		"\2\2\67\u00ac\3\2\2\29\u00af\3\2\2\2;\u00b1\3\2\2\2=\u00b3\3\2\2\2?\u00b5"+
		"\3\2\2\2A\u00b8\3\2\2\2C\u00c4\3\2\2\2E\u00c6\3\2\2\2G\u00cb\3\2\2\2I"+
		"\u00d2\3\2\2\2K\u00d6\3\2\2\2M\u00db\3\2\2\2O\u00e0\3\2\2\2Q\u00e6\3\2"+
		"\2\2S\u00e9\3\2\2\2U\u00ef\3\2\2\2W\u00f3\3\2\2\2Y\u00f9\3\2\2\2[\u0102"+
		"\3\2\2\2]\u0109\3\2\2\2_\u010e\3\2\2\2a\u0112\3\2\2\2c\u0117\3\2\2\2e"+
		"\u011f\3\2\2\2g\u0123\3\2\2\2i\u012d\3\2\2\2k\u0130\3\2\2\2m\u013b\3\2"+
		"\2\2o\u014a\3\2\2\2qr\7=\2\2r\4\3\2\2\2st\7.\2\2t\6\3\2\2\2uv\7?\2\2v"+
		"\b\3\2\2\2wx\7*\2\2x\n\3\2\2\2yz\7+\2\2z\f\3\2\2\2{|\7}\2\2|\16\3\2\2"+
		"\2}~\7\177\2\2~\20\3\2\2\2\177\u0080\7]\2\2\u0080\22\3\2\2\2\u0081\u0082"+
		"\7_\2\2\u0082\24\3\2\2\2\u0083\u0084\7-\2\2\u0084\u0085\7-\2\2\u0085\26"+
		"\3\2\2\2\u0086\u0087\7/\2\2\u0087\u0088\7/\2\2\u0088\30\3\2\2\2\u0089"+
		"\u008a\7\60\2\2\u008a\32\3\2\2\2\u008b\u008c\7-\2\2\u008c\34\3\2\2\2\u008d"+
		"\u008e\7/\2\2\u008e\36\3\2\2\2\u008f\u0090\7#\2\2\u0090 \3\2\2\2\u0091"+
		"\u0092\7\u0080\2\2\u0092\"\3\2\2\2\u0093\u0094\7,\2\2\u0094$\3\2\2\2\u0095"+
		"\u0096\7\61\2\2\u0096&\3\2\2\2\u0097\u0098\7\'\2\2\u0098(\3\2\2\2\u0099"+
		"\u009a\7>\2\2\u009a\u009b\7>\2\2\u009b*\3\2\2\2\u009c\u009d\7@\2\2\u009d"+
		"\u009e\7@\2\2\u009e,\3\2\2\2\u009f\u00a0\7>\2\2\u00a0.\3\2\2\2\u00a1\u00a2"+
		"\7@\2\2\u00a2\60\3\2\2\2\u00a3\u00a4\7>\2\2\u00a4\u00a5\7?\2\2\u00a5\62"+
		"\3\2\2\2\u00a6\u00a7\7@\2\2\u00a7\u00a8\7?\2\2\u00a8\64\3\2\2\2\u00a9"+
		"\u00aa\7?\2\2\u00aa\u00ab\7?\2\2\u00ab\66\3\2\2\2\u00ac\u00ad\7#\2\2\u00ad"+
		"\u00ae\7?\2\2\u00ae8\3\2\2\2\u00af\u00b0\7(\2\2\u00b0:\3\2\2\2\u00b1\u00b2"+
		"\7`\2\2\u00b2<\3\2\2\2\u00b3\u00b4\7~\2\2\u00b4>\3\2\2\2\u00b5\u00b6\7"+
		"(\2\2\u00b6\u00b7\7(\2\2\u00b7@\3\2\2\2\u00b8\u00b9\7~\2\2\u00b9\u00ba"+
		"\7~\2\2\u00baB\3\2\2\2\u00bb\u00bc\7v\2\2\u00bc\u00bd\7t\2\2\u00bd\u00be"+
		"\7w\2\2\u00be\u00c5\7g\2\2\u00bf\u00c0\7h\2\2\u00c0\u00c1\7c\2\2\u00c1"+
		"\u00c2\7n\2\2\u00c2\u00c3\7u\2\2\u00c3\u00c5\7g\2\2\u00c4\u00bb\3\2\2"+
		"\2\u00c4\u00bf\3\2\2\2\u00c5D\3\2\2\2\u00c6\u00c7\7p\2\2\u00c7\u00c8\7"+
		"w\2\2\u00c8\u00c9\7n\2\2\u00c9\u00ca\7n\2\2\u00caF\3\2\2\2\u00cb\u00cc"+
		"\7u\2\2\u00cc\u00cd\7v\2\2\u00cd\u00ce\7t\2\2\u00ce\u00cf\7k\2\2\u00cf"+
		"\u00d0\7p\2\2\u00d0\u00d1\7i\2\2\u00d1H\3\2\2\2\u00d2\u00d3\7k\2\2\u00d3"+
		"\u00d4\7p\2\2\u00d4\u00d5\7v\2\2\u00d5J\3\2\2\2\u00d6\u00d7\7d\2\2\u00d7"+
		"\u00d8\7q\2\2\u00d8\u00d9\7q\2\2\u00d9\u00da\7n\2\2\u00daL\3\2\2\2\u00db"+
		"\u00dc\7x\2\2\u00dc\u00dd\7q\2\2\u00dd\u00de\7k\2\2\u00de\u00df\7f\2\2"+
		"\u00dfN\3\2\2\2\u00e0\u00e1\7e\2\2\u00e1\u00e2\7n\2\2\u00e2\u00e3\7c\2"+
		"\2\u00e3\u00e4\7u\2\2\u00e4\u00e5\7u\2\2\u00e5P\3\2\2\2\u00e6\u00e7\7"+
		"k\2\2\u00e7\u00e8\7h\2\2\u00e8R\3\2\2\2\u00e9\u00ea\7y\2\2\u00ea\u00eb"+
		"\7j\2\2\u00eb\u00ec\7k\2\2\u00ec\u00ed\7n\2\2\u00ed\u00ee\7g\2\2\u00ee"+
		"T\3\2\2\2\u00ef\u00f0\7h\2\2\u00f0\u00f1\7q\2\2\u00f1\u00f2\7t\2\2\u00f2"+
		"V\3\2\2\2\u00f3\u00f4\7d\2\2\u00f4\u00f5\7t\2\2\u00f5\u00f6\7g\2\2\u00f6"+
		"\u00f7\7c\2\2\u00f7\u00f8\7m\2\2\u00f8X\3\2\2\2\u00f9\u00fa\7e\2\2\u00fa"+
		"\u00fb\7q\2\2\u00fb\u00fc\7p\2\2\u00fc\u00fd\7v\2\2\u00fd\u00fe\7k\2\2"+
		"\u00fe\u00ff\7p\2\2\u00ff\u0100\7w\2\2\u0100\u0101\7g\2\2\u0101Z\3\2\2"+
		"\2\u0102\u0103\7t\2\2\u0103\u0104\7g\2\2\u0104\u0105\7v\2\2\u0105\u0106"+
		"\7w\2\2\u0106\u0107\7t\2\2\u0107\u0108\7p\2\2\u0108\\\3\2\2\2\u0109\u010a"+
		"\7v\2\2\u010a\u010b\7j\2\2\u010b\u010c\7k\2\2\u010c\u010d\7u\2\2\u010d"+
		"^\3\2\2\2\u010e\u010f\7p\2\2\u010f\u0110\7g\2\2\u0110\u0111\7y\2\2\u0111"+
		"`\3\2\2\2\u0112\u0113\7g\2\2\u0113\u0114\7n\2\2\u0114\u0115\7u\2\2\u0115"+
		"\u0116\7g\2\2\u0116b\3\2\2\2\u0117\u011b\t\2\2\2\u0118\u011a\t\3\2\2\u0119"+
		"\u0118\3\2\2\2\u011a\u011d\3\2\2\2\u011b\u0119\3\2\2\2\u011b\u011c\3\2"+
		"\2\2\u011cd\3\2\2\2\u011d\u011b\3\2\2\2\u011e\u0120\t\4\2\2\u011f\u011e"+
		"\3\2\2\2\u0120\u0121\3\2\2\2\u0121\u011f\3\2\2\2\u0121\u0122\3\2\2\2\u0122"+
		"f\3\2\2\2\u0123\u0128\7$\2\2\u0124\u0127\5i\65\2\u0125\u0127\13\2\2\2"+
		"\u0126\u0124\3\2\2\2\u0126\u0125\3\2\2\2\u0127\u012a\3\2\2\2\u0128\u0129"+
		"\3\2\2\2\u0128\u0126\3\2\2\2\u0129\u012b\3\2\2\2\u012a\u0128\3\2\2\2\u012b"+
		"\u012c\7$\2\2\u012ch\3\2\2\2\u012d\u012e\7^\2\2\u012e\u012f\t\5\2\2\u012f"+
		"j\3\2\2\2\u0130\u0131\7\61\2\2\u0131\u0132\7\61\2\2\u0132\u0136\3\2\2"+
		"\2\u0133\u0135\n\6\2\2\u0134\u0133\3\2\2\2\u0135\u0138\3\2\2\2\u0136\u0134"+
		"\3\2\2\2\u0136\u0137\3\2\2\2\u0137\u0139\3\2\2\2\u0138\u0136\3\2\2\2\u0139"+
		"\u013a\b\66\2\2\u013al\3\2\2\2\u013b\u013c\7\61\2\2\u013c\u013d\7,\2\2"+
		"\u013d\u0141\3\2\2\2\u013e\u0140\13\2\2\2\u013f\u013e\3\2\2\2\u0140\u0143"+
		"\3\2\2\2\u0141\u0142\3\2\2\2\u0141\u013f\3\2\2\2\u0142\u0144\3\2\2\2\u0143"+
		"\u0141\3\2\2\2\u0144\u0145\7,\2\2\u0145\u0146\7\61\2\2\u0146\u0147\3\2"+
		"\2\2\u0147\u0148\b\67\2\2\u0148n\3\2\2\2\u0149\u014b\t\7\2\2\u014a\u0149"+
		"\3\2\2\2\u014b\u014c\3\2\2\2\u014c\u014a\3\2\2\2\u014c\u014d\3\2\2\2\u014d"+
		"\u014e\3\2\2\2\u014e\u014f\b8\2\2\u014fp\3\2\2\2\13\2\u00c4\u011b\u0121"+
		"\u0126\u0128\u0136\u0141\u014c\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}