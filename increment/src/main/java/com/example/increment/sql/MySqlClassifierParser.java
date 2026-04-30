// Generated from com/example/increment/sql/MySqlClassifier.g4 by ANTLR 4.13.1
package com.example.increment.sql;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class MySqlClassifierParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEMI=1, COMMA=2, DOT=3, LPAREN=4, RPAREN=5, EQ=6, BLOCK_COMMENT=7, LINE_COMMENT=8, 
		STRING_LITERAL=9, BACKTICK_QUOTED=10, NUMBER_LITERAL=11, CREATE=12, OR=13, 
		REPLACE=14, TEMPORARY=15, TABLE=16, IF=17, NOT=18, EXISTS=19, ALTER=20, 
		DROP=21, TRUNCATE=22, RENAME=23, TO=24, UNIQUE=25, FULLTEXT=26, SPATIAL=27, 
		INDEX=28, KEY=29, ON=30, INSERT=31, UPDATE=32, DELETE=33, SELECT=34, BEGIN=35, 
		COMMIT=36, ROLLBACK=37, START=38, TRANSACTION=39, SAVEPOINT=40, SET=41, 
		WORK=42, AND=43, NO=44, CHAIN=45, RELEASE=46, USE=47, DATABASE=48, SCHEMA=49, 
		WITH=50, CONSISTENT=51, SNAPSHOT=52, READ=53, WRITE=54, ONLY=55, IN=56, 
		FROM=57, INTO=58, VALUES=59, WHERE=60, ADD=61, COLUMN=62, PRIMARY=63, 
		FOREIGN=64, REFERENCES=65, CONSTRAINT=66, DEFAULT=67, AUTO_INCREMENT=68, 
		NULL=69, NOT_NULL=70, INT=71, VARCHAR=72, CHAR=73, TEXT=74, BLOB=75, DECIMAL=76, 
		FLOAT=77, DOUBLE=78, DATE=79, DATETIME=80, TIMESTAMP=81, TIME=82, YEAR=83, 
		BOOLEAN=84, BIGINT=85, SMALLINT=86, TINYINT=87, MEDIUMINT=88, INTEGER=89, 
		ENUM=90, SET_KW=91, JSON=92, BIT=93, BINARY=94, VARBINARY=95, IDENTIFIER=96, 
		WS=97;
	public static final int
		RULE_statement = 0, RULE_ddlStatement = 1, RULE_dmlStatement = 2, RULE_transactionStatement = 3, 
		RULE_useStatement = 4, RULE_setStatement = 5, RULE_otherStatement = 6, 
		RULE_ifNotExists = 7, RULE_ifExists = 8, RULE_tableName = 9, RULE_schemaName = 10, 
		RULE_indexName = 11, RULE_identifier = 12, RULE_keywordAsId = 13, RULE_remainingSql = 14;
	private static String[] makeRuleNames() {
		return new String[] {
			"statement", "ddlStatement", "dmlStatement", "transactionStatement", 
			"useStatement", "setStatement", "otherStatement", "ifNotExists", "ifExists", 
			"tableName", "schemaName", "indexName", "identifier", "keywordAsId", 
			"remainingSql"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "','", "'.'", "'('", "')'", "'='", null, null, null, null, 
			null, "'CREATE'", "'OR'", "'REPLACE'", "'TEMPORARY'", "'TABLE'", "'IF'", 
			"'NOT'", "'EXISTS'", "'ALTER'", "'DROP'", "'TRUNCATE'", "'RENAME'", "'TO'", 
			"'UNIQUE'", "'FULLTEXT'", "'SPATIAL'", "'INDEX'", "'KEY'", "'ON'", "'INSERT'", 
			"'UPDATE'", "'DELETE'", "'SELECT'", "'BEGIN'", "'COMMIT'", "'ROLLBACK'", 
			"'START'", "'TRANSACTION'", "'SAVEPOINT'", null, "'WORK'", "'AND'", "'NO'", 
			"'CHAIN'", "'RELEASE'", "'USE'", "'DATABASE'", "'SCHEMA'", "'WITH'", 
			"'CONSISTENT'", "'SNAPSHOT'", "'READ'", "'WRITE'", "'ONLY'", "'IN'", 
			"'FROM'", "'INTO'", "'VALUES'", "'WHERE'", "'ADD'", "'COLUMN'", "'PRIMARY'", 
			"'FOREIGN'", "'REFERENCES'", "'CONSTRAINT'", "'DEFAULT'", "'AUTO_INCREMENT'", 
			"'NULL'", "'NOT NULL'", "'INT'", "'VARCHAR'", "'CHAR'", "'TEXT'", "'BLOB'", 
			"'DECIMAL'", "'FLOAT'", "'DOUBLE'", "'DATE'", "'DATETIME'", "'TIMESTAMP'", 
			"'TIME'", "'YEAR'", "'BOOLEAN'", "'BIGINT'", "'SMALLINT'", "'TINYINT'", 
			"'MEDIUMINT'", "'INTEGER'", "'ENUM'", null, "'JSON'", "'BIT'", "'BINARY'", 
			"'VARBINARY'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SEMI", "COMMA", "DOT", "LPAREN", "RPAREN", "EQ", "BLOCK_COMMENT", 
			"LINE_COMMENT", "STRING_LITERAL", "BACKTICK_QUOTED", "NUMBER_LITERAL", 
			"CREATE", "OR", "REPLACE", "TEMPORARY", "TABLE", "IF", "NOT", "EXISTS", 
			"ALTER", "DROP", "TRUNCATE", "RENAME", "TO", "UNIQUE", "FULLTEXT", "SPATIAL", 
			"INDEX", "KEY", "ON", "INSERT", "UPDATE", "DELETE", "SELECT", "BEGIN", 
			"COMMIT", "ROLLBACK", "START", "TRANSACTION", "SAVEPOINT", "SET", "WORK", 
			"AND", "NO", "CHAIN", "RELEASE", "USE", "DATABASE", "SCHEMA", "WITH", 
			"CONSISTENT", "SNAPSHOT", "READ", "WRITE", "ONLY", "IN", "FROM", "INTO", 
			"VALUES", "WHERE", "ADD", "COLUMN", "PRIMARY", "FOREIGN", "REFERENCES", 
			"CONSTRAINT", "DEFAULT", "AUTO_INCREMENT", "NULL", "NOT_NULL", "INT", 
			"VARCHAR", "CHAR", "TEXT", "BLOB", "DECIMAL", "FLOAT", "DOUBLE", "DATE", 
			"DATETIME", "TIMESTAMP", "TIME", "YEAR", "BOOLEAN", "BIGINT", "SMALLINT", 
			"TINYINT", "MEDIUMINT", "INTEGER", "ENUM", "SET_KW", "JSON", "BIT", "BINARY", 
			"VARBINARY", "IDENTIFIER", "WS"
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

	@Override
	public String getGrammarFileName() { return "MySqlClassifier.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MySqlClassifierParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public DdlStatementContext ddlStatement() {
			return getRuleContext(DdlStatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(MySqlClassifierParser.EOF, 0); }
		public DmlStatementContext dmlStatement() {
			return getRuleContext(DmlStatementContext.class,0);
		}
		public TransactionStatementContext transactionStatement() {
			return getRuleContext(TransactionStatementContext.class,0);
		}
		public UseStatementContext useStatement() {
			return getRuleContext(UseStatementContext.class,0);
		}
		public SetStatementContext setStatement() {
			return getRuleContext(SetStatementContext.class,0);
		}
		public OtherStatementContext otherStatement() {
			return getRuleContext(OtherStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statement);
		try {
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(30);
				ddlStatement();
				setState(31);
				match(EOF);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(33);
				dmlStatement();
				setState(34);
				match(EOF);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(36);
				transactionStatement();
				setState(37);
				match(EOF);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(39);
				useStatement();
				setState(40);
				match(EOF);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(42);
				setStatement();
				setState(43);
				match(EOF);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(45);
				otherStatement();
				setState(46);
				match(EOF);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DdlStatementContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(MySqlClassifierParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(MySqlClassifierParser.TABLE, 0); }
		public List<TableNameContext> tableName() {
			return getRuleContexts(TableNameContext.class);
		}
		public TableNameContext tableName(int i) {
			return getRuleContext(TableNameContext.class,i);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public TerminalNode OR() { return getToken(MySqlClassifierParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(MySqlClassifierParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(MySqlClassifierParser.TEMPORARY, 0); }
		public IfNotExistsContext ifNotExists() {
			return getRuleContext(IfNotExistsContext.class,0);
		}
		public TerminalNode ALTER() { return getToken(MySqlClassifierParser.ALTER, 0); }
		public TerminalNode DROP() { return getToken(MySqlClassifierParser.DROP, 0); }
		public IfExistsContext ifExists() {
			return getRuleContext(IfExistsContext.class,0);
		}
		public TerminalNode INDEX() { return getToken(MySqlClassifierParser.INDEX, 0); }
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public TerminalNode ON() { return getToken(MySqlClassifierParser.ON, 0); }
		public TerminalNode UNIQUE() { return getToken(MySqlClassifierParser.UNIQUE, 0); }
		public TerminalNode FULLTEXT() { return getToken(MySqlClassifierParser.FULLTEXT, 0); }
		public TerminalNode SPATIAL() { return getToken(MySqlClassifierParser.SPATIAL, 0); }
		public TerminalNode TRUNCATE() { return getToken(MySqlClassifierParser.TRUNCATE, 0); }
		public TerminalNode RENAME() { return getToken(MySqlClassifierParser.RENAME, 0); }
		public List<TerminalNode> TO() { return getTokens(MySqlClassifierParser.TO); }
		public TerminalNode TO(int i) {
			return getToken(MySqlClassifierParser.TO, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySqlClassifierParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySqlClassifierParser.COMMA, i);
		}
		public SchemaNameContext schemaName() {
			return getRuleContext(SchemaNameContext.class,0);
		}
		public TerminalNode DATABASE() { return getToken(MySqlClassifierParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(MySqlClassifierParser.SCHEMA, 0); }
		public DdlStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddlStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterDdlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitDdlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitDdlStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DdlStatementContext ddlStatement() throws RecognitionException {
		DdlStatementContext _localctx = new DdlStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_ddlStatement);
		int _la;
		try {
			int _alt;
			setState(144);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(50);
				match(CREATE);
				setState(53);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(51);
					match(OR);
					setState(52);
					match(REPLACE);
					}
				}

				setState(56);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(55);
					match(TEMPORARY);
					}
				}

				setState(58);
				match(TABLE);
				setState(60);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(59);
					ifNotExists();
					}
					break;
				}
				setState(62);
				tableName();
				setState(63);
				remainingSql();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(65);
				match(ALTER);
				setState(66);
				match(TABLE);
				setState(67);
				tableName();
				setState(68);
				remainingSql();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(70);
				match(DROP);
				setState(71);
				match(TABLE);
				setState(73);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(72);
					ifExists();
					}
					break;
				}
				setState(75);
				tableName();
				setState(77);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
				case 1:
					{
					setState(76);
					remainingSql();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(79);
				match(CREATE);
				setState(81);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 234881024L) != 0)) {
					{
					setState(80);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 234881024L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(83);
				match(INDEX);
				setState(84);
				indexName();
				setState(85);
				match(ON);
				setState(86);
				tableName();
				setState(87);
				remainingSql();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(89);
				match(DROP);
				setState(90);
				match(INDEX);
				setState(92);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(91);
					ifExists();
					}
					break;
				}
				setState(94);
				indexName();
				setState(95);
				match(ON);
				setState(96);
				tableName();
				setState(98);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(97);
					remainingSql();
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(100);
				match(TRUNCATE);
				setState(102);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(101);
					match(TABLE);
					}
					break;
				}
				setState(104);
				tableName();
				setState(106);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
				case 1:
					{
					setState(105);
					remainingSql();
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(108);
				match(RENAME);
				setState(109);
				match(TABLE);
				setState(110);
				tableName();
				setState(111);
				match(TO);
				setState(112);
				tableName();
				setState(120);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(113);
						match(COMMA);
						setState(114);
						tableName();
						setState(115);
						match(TO);
						setState(116);
						tableName();
						}
						} 
					}
					setState(122);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				setState(124);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
				case 1:
					{
					setState(123);
					remainingSql();
					}
					break;
				}
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(126);
				match(CREATE);
				setState(127);
				_la = _input.LA(1);
				if ( !(_la==DATABASE || _la==SCHEMA) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(129);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
				case 1:
					{
					setState(128);
					ifNotExists();
					}
					break;
				}
				setState(131);
				schemaName();
				setState(133);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
				case 1:
					{
					setState(132);
					remainingSql();
					}
					break;
				}
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(135);
				match(DROP);
				setState(136);
				_la = _input.LA(1);
				if ( !(_la==DATABASE || _la==SCHEMA) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(138);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(137);
					ifExists();
					}
					break;
				}
				setState(140);
				schemaName();
				setState(142);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
				case 1:
					{
					setState(141);
					remainingSql();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DmlStatementContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(MySqlClassifierParser.INSERT, 0); }
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public TerminalNode UPDATE() { return getToken(MySqlClassifierParser.UPDATE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode DELETE() { return getToken(MySqlClassifierParser.DELETE, 0); }
		public TerminalNode REPLACE() { return getToken(MySqlClassifierParser.REPLACE, 0); }
		public DmlStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dmlStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterDmlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitDmlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitDmlStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DmlStatementContext dmlStatement() throws RecognitionException {
		DmlStatementContext _localctx = new DmlStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_dmlStatement);
		try {
			setState(158);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INSERT:
				enterOuterAlt(_localctx, 1);
				{
				setState(146);
				match(INSERT);
				setState(147);
				remainingSql();
				}
				break;
			case UPDATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(148);
				match(UPDATE);
				setState(149);
				tableName();
				setState(150);
				remainingSql();
				}
				break;
			case DELETE:
				enterOuterAlt(_localctx, 3);
				{
				setState(152);
				match(DELETE);
				setState(153);
				remainingSql();
				}
				break;
			case REPLACE:
				enterOuterAlt(_localctx, 4);
				{
				setState(154);
				match(REPLACE);
				setState(155);
				tableName();
				setState(156);
				remainingSql();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TransactionStatementContext extends ParserRuleContext {
		public TerminalNode BEGIN() { return getToken(MySqlClassifierParser.BEGIN, 0); }
		public TerminalNode WORK() { return getToken(MySqlClassifierParser.WORK, 0); }
		public TerminalNode COMMIT() { return getToken(MySqlClassifierParser.COMMIT, 0); }
		public TerminalNode ROLLBACK() { return getToken(MySqlClassifierParser.ROLLBACK, 0); }
		public TerminalNode START() { return getToken(MySqlClassifierParser.START, 0); }
		public TerminalNode TRANSACTION() { return getToken(MySqlClassifierParser.TRANSACTION, 0); }
		public TerminalNode SAVEPOINT() { return getToken(MySqlClassifierParser.SAVEPOINT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TransactionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transactionStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterTransactionStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitTransactionStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitTransactionStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransactionStatementContext transactionStatement() throws RecognitionException {
		TransactionStatementContext _localctx = new TransactionStatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_transactionStatement);
		int _la;
		try {
			setState(176);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BEGIN:
				enterOuterAlt(_localctx, 1);
				{
				setState(160);
				match(BEGIN);
				setState(162);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WORK) {
					{
					setState(161);
					match(WORK);
					}
				}

				}
				break;
			case COMMIT:
				enterOuterAlt(_localctx, 2);
				{
				setState(164);
				match(COMMIT);
				setState(166);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WORK) {
					{
					setState(165);
					match(WORK);
					}
				}

				}
				break;
			case ROLLBACK:
				enterOuterAlt(_localctx, 3);
				{
				setState(168);
				match(ROLLBACK);
				setState(170);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WORK) {
					{
					setState(169);
					match(WORK);
					}
				}

				}
				break;
			case START:
				enterOuterAlt(_localctx, 4);
				{
				setState(172);
				match(START);
				setState(173);
				match(TRANSACTION);
				}
				break;
			case SAVEPOINT:
				enterOuterAlt(_localctx, 5);
				{
				setState(174);
				match(SAVEPOINT);
				setState(175);
				identifier();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UseStatementContext extends ParserRuleContext {
		public TerminalNode USE() { return getToken(MySqlClassifierParser.USE, 0); }
		public SchemaNameContext schemaName() {
			return getRuleContext(SchemaNameContext.class,0);
		}
		public UseStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_useStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterUseStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitUseStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitUseStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UseStatementContext useStatement() throws RecognitionException {
		UseStatementContext _localctx = new UseStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_useStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			match(USE);
			setState(179);
			schemaName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SetStatementContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(MySqlClassifierParser.SET, 0); }
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public SetStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterSetStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitSetStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitSetStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetStatementContext setStatement() throws RecognitionException {
		SetStatementContext _localctx = new SetStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_setStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			match(SET);
			setState(182);
			remainingSql();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OtherStatementContext extends ParserRuleContext {
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public OtherStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_otherStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterOtherStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitOtherStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitOtherStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OtherStatementContext otherStatement() throws RecognitionException {
		OtherStatementContext _localctx = new OtherStatementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_otherStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			remainingSql();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfNotExistsContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(MySqlClassifierParser.IF, 0); }
		public TerminalNode NOT() { return getToken(MySqlClassifierParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(MySqlClassifierParser.EXISTS, 0); }
		public IfNotExistsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifNotExists; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterIfNotExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitIfNotExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitIfNotExists(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfNotExistsContext ifNotExists() throws RecognitionException {
		IfNotExistsContext _localctx = new IfNotExistsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_ifNotExists);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			match(IF);
			setState(187);
			match(NOT);
			setState(188);
			match(EXISTS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfExistsContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(MySqlClassifierParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(MySqlClassifierParser.EXISTS, 0); }
		public IfExistsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifExists; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterIfExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitIfExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitIfExists(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfExistsContext ifExists() throws RecognitionException {
		IfExistsContext _localctx = new IfExistsContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_ifExists);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			match(IF);
			setState(191);
			match(EXISTS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SchemaNameContext schemaName() {
			return getRuleContext(SchemaNameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(MySqlClassifierParser.DOT, 0); }
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(193);
				schemaName();
				setState(194);
				match(DOT);
				}
				break;
			}
			setState(198);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SchemaNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SchemaNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterSchemaName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitSchemaName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitSchemaName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaNameContext schemaName() throws RecognitionException {
		SchemaNameContext _localctx = new SchemaNameContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IndexNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public IndexNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterIndexName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitIndexName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitIndexName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexNameContext indexName() throws RecognitionException {
		IndexNameContext _localctx = new IndexNameContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(MySqlClassifierParser.IDENTIFIER, 0); }
		public TerminalNode BACKTICK_QUOTED() { return getToken(MySqlClassifierParser.BACKTICK_QUOTED, 0); }
		public KeywordAsIdContext keywordAsId() {
			return getRuleContext(KeywordAsIdContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_identifier);
		try {
			setState(207);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(204);
				match(IDENTIFIER);
				}
				break;
			case BACKTICK_QUOTED:
				enterOuterAlt(_localctx, 2);
				{
				setState(205);
				match(BACKTICK_QUOTED);
				}
				break;
			case CREATE:
			case OR:
			case REPLACE:
			case TEMPORARY:
			case TABLE:
			case IF:
			case NOT:
			case EXISTS:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case RENAME:
			case TO:
			case UNIQUE:
			case FULLTEXT:
			case SPATIAL:
			case INDEX:
			case KEY:
			case ON:
			case INSERT:
			case UPDATE:
			case DELETE:
			case SELECT:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case START:
			case TRANSACTION:
			case SAVEPOINT:
			case SET:
			case WORK:
			case AND:
			case NO:
			case CHAIN:
			case RELEASE:
			case USE:
			case DATABASE:
			case SCHEMA:
			case WITH:
			case CONSISTENT:
			case SNAPSHOT:
			case READ:
			case WRITE:
			case ONLY:
			case IN:
			case FROM:
			case INTO:
			case VALUES:
			case WHERE:
			case ADD:
			case COLUMN:
			case PRIMARY:
			case FOREIGN:
			case REFERENCES:
			case CONSTRAINT:
			case DEFAULT:
			case AUTO_INCREMENT:
			case NULL:
			case NOT_NULL:
			case INT:
			case VARCHAR:
			case CHAR:
			case TEXT:
			case BLOB:
			case DECIMAL:
			case FLOAT:
			case DOUBLE:
			case DATE:
			case DATETIME:
			case TIMESTAMP:
			case TIME:
			case YEAR:
			case BOOLEAN:
			case BIGINT:
			case SMALLINT:
			case TINYINT:
			case MEDIUMINT:
			case INTEGER:
			case ENUM:
			case SET_KW:
			case JSON:
			case BIT:
			case BINARY:
			case VARBINARY:
				enterOuterAlt(_localctx, 3);
				{
				setState(206);
				keywordAsId();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class KeywordAsIdContext extends ParserRuleContext {
		public TerminalNode TABLE() { return getToken(MySqlClassifierParser.TABLE, 0); }
		public TerminalNode DATABASE() { return getToken(MySqlClassifierParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(MySqlClassifierParser.SCHEMA, 0); }
		public TerminalNode INDEX() { return getToken(MySqlClassifierParser.INDEX, 0); }
		public TerminalNode KEY() { return getToken(MySqlClassifierParser.KEY, 0); }
		public TerminalNode TEMPORARY() { return getToken(MySqlClassifierParser.TEMPORARY, 0); }
		public TerminalNode OR() { return getToken(MySqlClassifierParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(MySqlClassifierParser.REPLACE, 0); }
		public TerminalNode IF() { return getToken(MySqlClassifierParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(MySqlClassifierParser.EXISTS, 0); }
		public TerminalNode NOT() { return getToken(MySqlClassifierParser.NOT, 0); }
		public TerminalNode TO() { return getToken(MySqlClassifierParser.TO, 0); }
		public TerminalNode AND() { return getToken(MySqlClassifierParser.AND, 0); }
		public TerminalNode NO() { return getToken(MySqlClassifierParser.NO, 0); }
		public TerminalNode CHAIN() { return getToken(MySqlClassifierParser.CHAIN, 0); }
		public TerminalNode RELEASE() { return getToken(MySqlClassifierParser.RELEASE, 0); }
		public TerminalNode WORK() { return getToken(MySqlClassifierParser.WORK, 0); }
		public TerminalNode BEGIN() { return getToken(MySqlClassifierParser.BEGIN, 0); }
		public TerminalNode COMMIT() { return getToken(MySqlClassifierParser.COMMIT, 0); }
		public TerminalNode ROLLBACK() { return getToken(MySqlClassifierParser.ROLLBACK, 0); }
		public TerminalNode TRANSACTION() { return getToken(MySqlClassifierParser.TRANSACTION, 0); }
		public TerminalNode SAVEPOINT() { return getToken(MySqlClassifierParser.SAVEPOINT, 0); }
		public TerminalNode SET() { return getToken(MySqlClassifierParser.SET, 0); }
		public TerminalNode INSERT() { return getToken(MySqlClassifierParser.INSERT, 0); }
		public TerminalNode UPDATE() { return getToken(MySqlClassifierParser.UPDATE, 0); }
		public TerminalNode DELETE() { return getToken(MySqlClassifierParser.DELETE, 0); }
		public TerminalNode SELECT() { return getToken(MySqlClassifierParser.SELECT, 0); }
		public TerminalNode CREATE() { return getToken(MySqlClassifierParser.CREATE, 0); }
		public TerminalNode ALTER() { return getToken(MySqlClassifierParser.ALTER, 0); }
		public TerminalNode DROP() { return getToken(MySqlClassifierParser.DROP, 0); }
		public TerminalNode TRUNCATE() { return getToken(MySqlClassifierParser.TRUNCATE, 0); }
		public TerminalNode RENAME() { return getToken(MySqlClassifierParser.RENAME, 0); }
		public TerminalNode USE() { return getToken(MySqlClassifierParser.USE, 0); }
		public TerminalNode START() { return getToken(MySqlClassifierParser.START, 0); }
		public TerminalNode WITH() { return getToken(MySqlClassifierParser.WITH, 0); }
		public TerminalNode CONSISTENT() { return getToken(MySqlClassifierParser.CONSISTENT, 0); }
		public TerminalNode SNAPSHOT() { return getToken(MySqlClassifierParser.SNAPSHOT, 0); }
		public TerminalNode READ() { return getToken(MySqlClassifierParser.READ, 0); }
		public TerminalNode WRITE() { return getToken(MySqlClassifierParser.WRITE, 0); }
		public TerminalNode ONLY() { return getToken(MySqlClassifierParser.ONLY, 0); }
		public TerminalNode UNIQUE() { return getToken(MySqlClassifierParser.UNIQUE, 0); }
		public TerminalNode FULLTEXT() { return getToken(MySqlClassifierParser.FULLTEXT, 0); }
		public TerminalNode SPATIAL() { return getToken(MySqlClassifierParser.SPATIAL, 0); }
		public TerminalNode ON() { return getToken(MySqlClassifierParser.ON, 0); }
		public TerminalNode IN() { return getToken(MySqlClassifierParser.IN, 0); }
		public TerminalNode FROM() { return getToken(MySqlClassifierParser.FROM, 0); }
		public TerminalNode INTO() { return getToken(MySqlClassifierParser.INTO, 0); }
		public TerminalNode VALUES() { return getToken(MySqlClassifierParser.VALUES, 0); }
		public TerminalNode WHERE() { return getToken(MySqlClassifierParser.WHERE, 0); }
		public TerminalNode ADD() { return getToken(MySqlClassifierParser.ADD, 0); }
		public TerminalNode COLUMN() { return getToken(MySqlClassifierParser.COLUMN, 0); }
		public TerminalNode PRIMARY() { return getToken(MySqlClassifierParser.PRIMARY, 0); }
		public TerminalNode FOREIGN() { return getToken(MySqlClassifierParser.FOREIGN, 0); }
		public TerminalNode REFERENCES() { return getToken(MySqlClassifierParser.REFERENCES, 0); }
		public TerminalNode CONSTRAINT() { return getToken(MySqlClassifierParser.CONSTRAINT, 0); }
		public TerminalNode DEFAULT() { return getToken(MySqlClassifierParser.DEFAULT, 0); }
		public TerminalNode AUTO_INCREMENT() { return getToken(MySqlClassifierParser.AUTO_INCREMENT, 0); }
		public TerminalNode NULL() { return getToken(MySqlClassifierParser.NULL, 0); }
		public TerminalNode NOT_NULL() { return getToken(MySqlClassifierParser.NOT_NULL, 0); }
		public TerminalNode INT() { return getToken(MySqlClassifierParser.INT, 0); }
		public TerminalNode VARCHAR() { return getToken(MySqlClassifierParser.VARCHAR, 0); }
		public TerminalNode CHAR() { return getToken(MySqlClassifierParser.CHAR, 0); }
		public TerminalNode TEXT() { return getToken(MySqlClassifierParser.TEXT, 0); }
		public TerminalNode BLOB() { return getToken(MySqlClassifierParser.BLOB, 0); }
		public TerminalNode DECIMAL() { return getToken(MySqlClassifierParser.DECIMAL, 0); }
		public TerminalNode FLOAT() { return getToken(MySqlClassifierParser.FLOAT, 0); }
		public TerminalNode DOUBLE() { return getToken(MySqlClassifierParser.DOUBLE, 0); }
		public TerminalNode DATE() { return getToken(MySqlClassifierParser.DATE, 0); }
		public TerminalNode DATETIME() { return getToken(MySqlClassifierParser.DATETIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(MySqlClassifierParser.TIMESTAMP, 0); }
		public TerminalNode TIME() { return getToken(MySqlClassifierParser.TIME, 0); }
		public TerminalNode YEAR() { return getToken(MySqlClassifierParser.YEAR, 0); }
		public TerminalNode BOOLEAN() { return getToken(MySqlClassifierParser.BOOLEAN, 0); }
		public TerminalNode BIGINT() { return getToken(MySqlClassifierParser.BIGINT, 0); }
		public TerminalNode SMALLINT() { return getToken(MySqlClassifierParser.SMALLINT, 0); }
		public TerminalNode TINYINT() { return getToken(MySqlClassifierParser.TINYINT, 0); }
		public TerminalNode MEDIUMINT() { return getToken(MySqlClassifierParser.MEDIUMINT, 0); }
		public TerminalNode INTEGER() { return getToken(MySqlClassifierParser.INTEGER, 0); }
		public TerminalNode ENUM() { return getToken(MySqlClassifierParser.ENUM, 0); }
		public TerminalNode SET_KW() { return getToken(MySqlClassifierParser.SET_KW, 0); }
		public TerminalNode JSON() { return getToken(MySqlClassifierParser.JSON, 0); }
		public TerminalNode BIT() { return getToken(MySqlClassifierParser.BIT, 0); }
		public TerminalNode BINARY() { return getToken(MySqlClassifierParser.BINARY, 0); }
		public TerminalNode VARBINARY() { return getToken(MySqlClassifierParser.VARBINARY, 0); }
		public KeywordAsIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keywordAsId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterKeywordAsId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitKeywordAsId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitKeywordAsId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeywordAsIdContext keywordAsId() throws RecognitionException {
		KeywordAsIdContext _localctx = new KeywordAsIdContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_keywordAsId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & -4096L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 4294967295L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RemainingSqlContext extends ParserRuleContext {
		public List<TerminalNode> EOF() { return getTokens(MySqlClassifierParser.EOF); }
		public TerminalNode EOF(int i) {
			return getToken(MySqlClassifierParser.EOF, i);
		}
		public RemainingSqlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_remainingSql; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).enterRemainingSql(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MySqlClassifierListener ) ((MySqlClassifierListener)listener).exitRemainingSql(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MySqlClassifierVisitor ) return ((MySqlClassifierVisitor<? extends T>)visitor).visitRemainingSql(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RemainingSqlContext remainingSql() throws RecognitionException {
		RemainingSqlContext _localctx = new RemainingSqlContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_remainingSql);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -2L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 17179869183L) != 0)) {
				{
				{
				setState(211);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==EOF) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(216);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001a\u00da\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u00001\b\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u00016\b\u0001\u0001\u0001"+
		"\u0003\u00019\b\u0001\u0001\u0001\u0001\u0001\u0003\u0001=\b\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001J\b"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001N\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001R\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001]\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001c\b\u0001\u0001\u0001\u0001\u0001\u0003\u0001g\b\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001k\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0005\u0001w\b\u0001\n\u0001\f\u0001z\t\u0001\u0001\u0001"+
		"\u0003\u0001}\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"\u0082\b\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u0086\b\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u008b\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001\u008f\b\u0001\u0003\u0001\u0091\b\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002"+
		"\u009f\b\u0002\u0001\u0003\u0001\u0003\u0003\u0003\u00a3\b\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003\u00a7\b\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u00ab\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u00b1\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0003"+
		"\t\u00c5\b\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u00d0\b\f\u0001\r\u0001\r\u0001\u000e\u0005"+
		"\u000e\u00d5\b\u000e\n\u000e\f\u000e\u00d8\t\u000e\u0001\u000e\u0000\u0000"+
		"\u000f\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u0000\u0004\u0001\u0000\u0019\u001b\u0001\u000001\u0001\u0000"+
		"\f_\u0000\u0001\u00f5\u00000\u0001\u0000\u0000\u0000\u0002\u0090\u0001"+
		"\u0000\u0000\u0000\u0004\u009e\u0001\u0000\u0000\u0000\u0006\u00b0\u0001"+
		"\u0000\u0000\u0000\b\u00b2\u0001\u0000\u0000\u0000\n\u00b5\u0001\u0000"+
		"\u0000\u0000\f\u00b8\u0001\u0000\u0000\u0000\u000e\u00ba\u0001\u0000\u0000"+
		"\u0000\u0010\u00be\u0001\u0000\u0000\u0000\u0012\u00c4\u0001\u0000\u0000"+
		"\u0000\u0014\u00c8\u0001\u0000\u0000\u0000\u0016\u00ca\u0001\u0000\u0000"+
		"\u0000\u0018\u00cf\u0001\u0000\u0000\u0000\u001a\u00d1\u0001\u0000\u0000"+
		"\u0000\u001c\u00d6\u0001\u0000\u0000\u0000\u001e\u001f\u0003\u0002\u0001"+
		"\u0000\u001f \u0005\u0000\u0000\u0001 1\u0001\u0000\u0000\u0000!\"\u0003"+
		"\u0004\u0002\u0000\"#\u0005\u0000\u0000\u0001#1\u0001\u0000\u0000\u0000"+
		"$%\u0003\u0006\u0003\u0000%&\u0005\u0000\u0000\u0001&1\u0001\u0000\u0000"+
		"\u0000\'(\u0003\b\u0004\u0000()\u0005\u0000\u0000\u0001)1\u0001\u0000"+
		"\u0000\u0000*+\u0003\n\u0005\u0000+,\u0005\u0000\u0000\u0001,1\u0001\u0000"+
		"\u0000\u0000-.\u0003\f\u0006\u0000./\u0005\u0000\u0000\u0001/1\u0001\u0000"+
		"\u0000\u00000\u001e\u0001\u0000\u0000\u00000!\u0001\u0000\u0000\u0000"+
		"0$\u0001\u0000\u0000\u00000\'\u0001\u0000\u0000\u00000*\u0001\u0000\u0000"+
		"\u00000-\u0001\u0000\u0000\u00001\u0001\u0001\u0000\u0000\u000025\u0005"+
		"\f\u0000\u000034\u0005\r\u0000\u000046\u0005\u000e\u0000\u000053\u0001"+
		"\u0000\u0000\u000056\u0001\u0000\u0000\u000068\u0001\u0000\u0000\u0000"+
		"79\u0005\u000f\u0000\u000087\u0001\u0000\u0000\u000089\u0001\u0000\u0000"+
		"\u00009:\u0001\u0000\u0000\u0000:<\u0005\u0010\u0000\u0000;=\u0003\u000e"+
		"\u0007\u0000<;\u0001\u0000\u0000\u0000<=\u0001\u0000\u0000\u0000=>\u0001"+
		"\u0000\u0000\u0000>?\u0003\u0012\t\u0000?@\u0003\u001c\u000e\u0000@\u0091"+
		"\u0001\u0000\u0000\u0000AB\u0005\u0014\u0000\u0000BC\u0005\u0010\u0000"+
		"\u0000CD\u0003\u0012\t\u0000DE\u0003\u001c\u000e\u0000E\u0091\u0001\u0000"+
		"\u0000\u0000FG\u0005\u0015\u0000\u0000GI\u0005\u0010\u0000\u0000HJ\u0003"+
		"\u0010\b\u0000IH\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JK\u0001"+
		"\u0000\u0000\u0000KM\u0003\u0012\t\u0000LN\u0003\u001c\u000e\u0000ML\u0001"+
		"\u0000\u0000\u0000MN\u0001\u0000\u0000\u0000N\u0091\u0001\u0000\u0000"+
		"\u0000OQ\u0005\f\u0000\u0000PR\u0007\u0000\u0000\u0000QP\u0001\u0000\u0000"+
		"\u0000QR\u0001\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000ST\u0005\u001c"+
		"\u0000\u0000TU\u0003\u0016\u000b\u0000UV\u0005\u001e\u0000\u0000VW\u0003"+
		"\u0012\t\u0000WX\u0003\u001c\u000e\u0000X\u0091\u0001\u0000\u0000\u0000"+
		"YZ\u0005\u0015\u0000\u0000Z\\\u0005\u001c\u0000\u0000[]\u0003\u0010\b"+
		"\u0000\\[\u0001\u0000\u0000\u0000\\]\u0001\u0000\u0000\u0000]^\u0001\u0000"+
		"\u0000\u0000^_\u0003\u0016\u000b\u0000_`\u0005\u001e\u0000\u0000`b\u0003"+
		"\u0012\t\u0000ac\u0003\u001c\u000e\u0000ba\u0001\u0000\u0000\u0000bc\u0001"+
		"\u0000\u0000\u0000c\u0091\u0001\u0000\u0000\u0000df\u0005\u0016\u0000"+
		"\u0000eg\u0005\u0010\u0000\u0000fe\u0001\u0000\u0000\u0000fg\u0001\u0000"+
		"\u0000\u0000gh\u0001\u0000\u0000\u0000hj\u0003\u0012\t\u0000ik\u0003\u001c"+
		"\u000e\u0000ji\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000\u0000k\u0091"+
		"\u0001\u0000\u0000\u0000lm\u0005\u0017\u0000\u0000mn\u0005\u0010\u0000"+
		"\u0000no\u0003\u0012\t\u0000op\u0005\u0018\u0000\u0000px\u0003\u0012\t"+
		"\u0000qr\u0005\u0002\u0000\u0000rs\u0003\u0012\t\u0000st\u0005\u0018\u0000"+
		"\u0000tu\u0003\u0012\t\u0000uw\u0001\u0000\u0000\u0000vq\u0001\u0000\u0000"+
		"\u0000wz\u0001\u0000\u0000\u0000xv\u0001\u0000\u0000\u0000xy\u0001\u0000"+
		"\u0000\u0000y|\u0001\u0000\u0000\u0000zx\u0001\u0000\u0000\u0000{}\u0003"+
		"\u001c\u000e\u0000|{\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000"+
		"}\u0091\u0001\u0000\u0000\u0000~\u007f\u0005\f\u0000\u0000\u007f\u0081"+
		"\u0007\u0001\u0000\u0000\u0080\u0082\u0003\u000e\u0007\u0000\u0081\u0080"+
		"\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000\u0000\u0082\u0083"+
		"\u0001\u0000\u0000\u0000\u0083\u0085\u0003\u0014\n\u0000\u0084\u0086\u0003"+
		"\u001c\u000e\u0000\u0085\u0084\u0001\u0000\u0000\u0000\u0085\u0086\u0001"+
		"\u0000\u0000\u0000\u0086\u0091\u0001\u0000\u0000\u0000\u0087\u0088\u0005"+
		"\u0015\u0000\u0000\u0088\u008a\u0007\u0001\u0000\u0000\u0089\u008b\u0003"+
		"\u0010\b\u0000\u008a\u0089\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000"+
		"\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008e\u0003\u0014"+
		"\n\u0000\u008d\u008f\u0003\u001c\u000e\u0000\u008e\u008d\u0001\u0000\u0000"+
		"\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\u0091\u0001\u0000\u0000"+
		"\u0000\u00902\u0001\u0000\u0000\u0000\u0090A\u0001\u0000\u0000\u0000\u0090"+
		"F\u0001\u0000\u0000\u0000\u0090O\u0001\u0000\u0000\u0000\u0090Y\u0001"+
		"\u0000\u0000\u0000\u0090d\u0001\u0000\u0000\u0000\u0090l\u0001\u0000\u0000"+
		"\u0000\u0090~\u0001\u0000\u0000\u0000\u0090\u0087\u0001\u0000\u0000\u0000"+
		"\u0091\u0003\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u001f\u0000\u0000"+
		"\u0093\u009f\u0003\u001c\u000e\u0000\u0094\u0095\u0005 \u0000\u0000\u0095"+
		"\u0096\u0003\u0012\t\u0000\u0096\u0097\u0003\u001c\u000e\u0000\u0097\u009f"+
		"\u0001\u0000\u0000\u0000\u0098\u0099\u0005!\u0000\u0000\u0099\u009f\u0003"+
		"\u001c\u000e\u0000\u009a\u009b\u0005\u000e\u0000\u0000\u009b\u009c\u0003"+
		"\u0012\t\u0000\u009c\u009d\u0003\u001c\u000e\u0000\u009d\u009f\u0001\u0000"+
		"\u0000\u0000\u009e\u0092\u0001\u0000\u0000\u0000\u009e\u0094\u0001\u0000"+
		"\u0000\u0000\u009e\u0098\u0001\u0000\u0000\u0000\u009e\u009a\u0001\u0000"+
		"\u0000\u0000\u009f\u0005\u0001\u0000\u0000\u0000\u00a0\u00a2\u0005#\u0000"+
		"\u0000\u00a1\u00a3\u0005*\u0000\u0000\u00a2\u00a1\u0001\u0000\u0000\u0000"+
		"\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u00b1\u0001\u0000\u0000\u0000"+
		"\u00a4\u00a6\u0005$\u0000\u0000\u00a5\u00a7\u0005*\u0000\u0000\u00a6\u00a5"+
		"\u0001\u0000\u0000\u0000\u00a6\u00a7\u0001\u0000\u0000\u0000\u00a7\u00b1"+
		"\u0001\u0000\u0000\u0000\u00a8\u00aa\u0005%\u0000\u0000\u00a9\u00ab\u0005"+
		"*\u0000\u0000\u00aa\u00a9\u0001\u0000\u0000\u0000\u00aa\u00ab\u0001\u0000"+
		"\u0000\u0000\u00ab\u00b1\u0001\u0000\u0000\u0000\u00ac\u00ad\u0005&\u0000"+
		"\u0000\u00ad\u00b1\u0005\'\u0000\u0000\u00ae\u00af\u0005(\u0000\u0000"+
		"\u00af\u00b1\u0003\u0018\f\u0000\u00b0\u00a0\u0001\u0000\u0000\u0000\u00b0"+
		"\u00a4\u0001\u0000\u0000\u0000\u00b0\u00a8\u0001\u0000\u0000\u0000\u00b0"+
		"\u00ac\u0001\u0000\u0000\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000\u00b1"+
		"\u0007\u0001\u0000\u0000\u0000\u00b2\u00b3\u0005/\u0000\u0000\u00b3\u00b4"+
		"\u0003\u0014\n\u0000\u00b4\t\u0001\u0000\u0000\u0000\u00b5\u00b6\u0005"+
		")\u0000\u0000\u00b6\u00b7\u0003\u001c\u000e\u0000\u00b7\u000b\u0001\u0000"+
		"\u0000\u0000\u00b8\u00b9\u0003\u001c\u000e\u0000\u00b9\r\u0001\u0000\u0000"+
		"\u0000\u00ba\u00bb\u0005\u0011\u0000\u0000\u00bb\u00bc\u0005\u0012\u0000"+
		"\u0000\u00bc\u00bd\u0005\u0013\u0000\u0000\u00bd\u000f\u0001\u0000\u0000"+
		"\u0000\u00be\u00bf\u0005\u0011\u0000\u0000\u00bf\u00c0\u0005\u0013\u0000"+
		"\u0000\u00c0\u0011\u0001\u0000\u0000\u0000\u00c1\u00c2\u0003\u0014\n\u0000"+
		"\u00c2\u00c3\u0005\u0003\u0000\u0000\u00c3\u00c5\u0001\u0000\u0000\u0000"+
		"\u00c4\u00c1\u0001\u0000\u0000\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000"+
		"\u00c5\u00c6\u0001\u0000\u0000\u0000\u00c6\u00c7\u0003\u0018\f\u0000\u00c7"+
		"\u0013\u0001\u0000\u0000\u0000\u00c8\u00c9\u0003\u0018\f\u0000\u00c9\u0015"+
		"\u0001\u0000\u0000\u0000\u00ca\u00cb\u0003\u0018\f\u0000\u00cb\u0017\u0001"+
		"\u0000\u0000\u0000\u00cc\u00d0\u0005`\u0000\u0000\u00cd\u00d0\u0005\n"+
		"\u0000\u0000\u00ce\u00d0\u0003\u001a\r\u0000\u00cf\u00cc\u0001\u0000\u0000"+
		"\u0000\u00cf\u00cd\u0001\u0000\u0000\u0000\u00cf\u00ce\u0001\u0000\u0000"+
		"\u0000\u00d0\u0019\u0001\u0000\u0000\u0000\u00d1\u00d2\u0007\u0002\u0000"+
		"\u0000\u00d2\u001b\u0001\u0000\u0000\u0000\u00d3\u00d5\b\u0003\u0000\u0000"+
		"\u00d4\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d8\u0001\u0000\u0000\u0000"+
		"\u00d6\u00d4\u0001\u0000\u0000\u0000\u00d6\u00d7\u0001\u0000\u0000\u0000"+
		"\u00d7\u001d\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001\u0000\u0000\u0000"+
		"\u001a058<IMQ\\bfjx|\u0081\u0085\u008a\u008e\u0090\u009e\u00a2\u00a6\u00aa"+
		"\u00b0\u00c4\u00cf\u00d6";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}