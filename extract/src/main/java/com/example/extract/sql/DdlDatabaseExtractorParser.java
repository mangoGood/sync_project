// Generated from com/example/extract/sql/DdlDatabaseExtractor.g4 by ANTLR 4.13.1
package com.example.extract.sql;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class DdlDatabaseExtractorParser extends Parser {
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
		ENUM=90, SET_KW=91, JSON=92, BIT=93, BINARY=94, VARBINARY=95, PARTITION=96, 
		PARTITIONS=97, LESS=98, THAN=99, MAXVALUE=100, VALUE=101, VALUES_KW=102, 
		PROCEDURE=103, FUNCTION=104, TRIGGER=105, EVENT=106, VIEW=107, CURSOR=108, 
		DEFINER=109, SQL=110, SECURITY=111, INVOKER=112, DETERMINISTIC=113, CONTAINS=114, 
		COMMENT=115, ENGINE=116, CHARSET=117, COLLATE=118, CHARACTER=119, IDENTIFIER=120, 
		WS=121;
	public static final int
		RULE_statement = 0, RULE_ddlStatement = 1, RULE_createTableStatement = 2, 
		RULE_alterTableStatement = 3, RULE_dropTableStatement = 4, RULE_truncateStatement = 5, 
		RULE_renameTableStatement = 6, RULE_createIndexStatement = 7, RULE_dropIndexStatement = 8, 
		RULE_createDatabaseStatement = 9, RULE_dropDatabaseStatement = 10, RULE_otherStatement = 11, 
		RULE_ifNotExists = 12, RULE_ifExists = 13, RULE_tableName = 14, RULE_schemaName = 15, 
		RULE_indexName = 16, RULE_identifier = 17, RULE_keywordAsId = 18, RULE_remainingSql = 19;
	private static String[] makeRuleNames() {
		return new String[] {
			"statement", "ddlStatement", "createTableStatement", "alterTableStatement", 
			"dropTableStatement", "truncateStatement", "renameTableStatement", "createIndexStatement", 
			"dropIndexStatement", "createDatabaseStatement", "dropDatabaseStatement", 
			"otherStatement", "ifNotExists", "ifExists", "tableName", "schemaName", 
			"indexName", "identifier", "keywordAsId", "remainingSql"
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
			"'FROM'", "'INTO'", null, "'WHERE'", "'ADD'", "'COLUMN'", "'PRIMARY'", 
			"'FOREIGN'", "'REFERENCES'", "'CONSTRAINT'", "'DEFAULT'", "'AUTO_INCREMENT'", 
			"'NULL'", "'NOT NULL'", "'INT'", "'VARCHAR'", "'CHAR'", "'TEXT'", "'BLOB'", 
			"'DECIMAL'", "'FLOAT'", "'DOUBLE'", "'DATE'", "'DATETIME'", "'TIMESTAMP'", 
			"'TIME'", "'YEAR'", "'BOOLEAN'", "'BIGINT'", "'SMALLINT'", "'TINYINT'", 
			"'MEDIUMINT'", "'INTEGER'", "'ENUM'", null, "'JSON'", "'BIT'", "'BINARY'", 
			"'VARBINARY'", "'PARTITION'", "'PARTITIONS'", "'LESS'", "'THAN'", "'MAXVALUE'", 
			"'VALUE'", null, "'PROCEDURE'", "'FUNCTION'", "'TRIGGER'", "'EVENT'", 
			"'VIEW'", "'CURSOR'", "'DEFINER'", "'SQL'", "'SECURITY'", "'INVOKER'", 
			"'DETERMINISTIC'", "'CONTAINS'", "'COMMENT'", "'ENGINE'", "'CHARSET'", 
			"'COLLATE'", "'CHARACTER'"
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
			"VARBINARY", "PARTITION", "PARTITIONS", "LESS", "THAN", "MAXVALUE", "VALUE", 
			"VALUES_KW", "PROCEDURE", "FUNCTION", "TRIGGER", "EVENT", "VIEW", "CURSOR", 
			"DEFINER", "SQL", "SECURITY", "INVOKER", "DETERMINISTIC", "CONTAINS", 
			"COMMENT", "ENGINE", "CHARSET", "COLLATE", "CHARACTER", "IDENTIFIER", 
			"WS"
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
	public String getGrammarFileName() { return "DdlDatabaseExtractor.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DdlDatabaseExtractorParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public DdlStatementContext ddlStatement() {
			return getRuleContext(DdlStatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(DdlDatabaseExtractorParser.EOF, 0); }
		public OtherStatementContext otherStatement() {
			return getRuleContext(OtherStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statement);
		try {
			setState(46);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(40);
				ddlStatement();
				setState(41);
				match(EOF);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(43);
				otherStatement();
				setState(44);
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
		public CreateTableStatementContext createTableStatement() {
			return getRuleContext(CreateTableStatementContext.class,0);
		}
		public AlterTableStatementContext alterTableStatement() {
			return getRuleContext(AlterTableStatementContext.class,0);
		}
		public DropTableStatementContext dropTableStatement() {
			return getRuleContext(DropTableStatementContext.class,0);
		}
		public TruncateStatementContext truncateStatement() {
			return getRuleContext(TruncateStatementContext.class,0);
		}
		public RenameTableStatementContext renameTableStatement() {
			return getRuleContext(RenameTableStatementContext.class,0);
		}
		public CreateIndexStatementContext createIndexStatement() {
			return getRuleContext(CreateIndexStatementContext.class,0);
		}
		public DropIndexStatementContext dropIndexStatement() {
			return getRuleContext(DropIndexStatementContext.class,0);
		}
		public CreateDatabaseStatementContext createDatabaseStatement() {
			return getRuleContext(CreateDatabaseStatementContext.class,0);
		}
		public DropDatabaseStatementContext dropDatabaseStatement() {
			return getRuleContext(DropDatabaseStatementContext.class,0);
		}
		public DdlStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddlStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterDdlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitDdlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitDdlStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DdlStatementContext ddlStatement() throws RecognitionException {
		DdlStatementContext _localctx = new DdlStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_ddlStatement);
		try {
			setState(57);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(48);
				createTableStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(49);
				alterTableStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(50);
				dropTableStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(51);
				truncateStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(52);
				renameTableStatement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(53);
				createIndexStatement();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(54);
				dropIndexStatement();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(55);
				createDatabaseStatement();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(56);
				dropDatabaseStatement();
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
	public static class CreateTableStatementContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DdlDatabaseExtractorParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(DdlDatabaseExtractorParser.TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public TerminalNode OR() { return getToken(DdlDatabaseExtractorParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(DdlDatabaseExtractorParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(DdlDatabaseExtractorParser.TEMPORARY, 0); }
		public IfNotExistsContext ifNotExists() {
			return getRuleContext(IfNotExistsContext.class,0);
		}
		public CreateTableStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterCreateTableStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitCreateTableStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitCreateTableStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableStatementContext createTableStatement() throws RecognitionException {
		CreateTableStatementContext _localctx = new CreateTableStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_createTableStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			match(CREATE);
			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OR) {
				{
				setState(60);
				match(OR);
				setState(61);
				match(REPLACE);
				}
			}

			setState(65);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(64);
				match(TEMPORARY);
				}
			}

			setState(67);
			match(TABLE);
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(68);
				ifNotExists();
				}
				break;
			}
			setState(71);
			tableName();
			setState(72);
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
	public static class AlterTableStatementContext extends ParserRuleContext {
		public TerminalNode ALTER() { return getToken(DdlDatabaseExtractorParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(DdlDatabaseExtractorParser.TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public AlterTableStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterTableStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterAlterTableStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitAlterTableStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitAlterTableStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterTableStatementContext alterTableStatement() throws RecognitionException {
		AlterTableStatementContext _localctx = new AlterTableStatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_alterTableStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			match(ALTER);
			setState(75);
			match(TABLE);
			setState(76);
			tableName();
			setState(77);
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
	public static class DropTableStatementContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DdlDatabaseExtractorParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(DdlDatabaseExtractorParser.TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public IfExistsContext ifExists() {
			return getRuleContext(IfExistsContext.class,0);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public DropTableStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropTableStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterDropTableStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitDropTableStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitDropTableStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DropTableStatementContext dropTableStatement() throws RecognitionException {
		DropTableStatementContext _localctx = new DropTableStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_dropTableStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(DROP);
			setState(80);
			match(TABLE);
			setState(82);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(81);
				ifExists();
				}
				break;
			}
			setState(84);
			tableName();
			setState(86);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(85);
				remainingSql();
				}
				break;
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
	public static class TruncateStatementContext extends ParserRuleContext {
		public TerminalNode TRUNCATE() { return getToken(DdlDatabaseExtractorParser.TRUNCATE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(DdlDatabaseExtractorParser.TABLE, 0); }
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public TruncateStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_truncateStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterTruncateStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitTruncateStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitTruncateStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TruncateStatementContext truncateStatement() throws RecognitionException {
		TruncateStatementContext _localctx = new TruncateStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_truncateStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(TRUNCATE);
			setState(90);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(89);
				match(TABLE);
				}
				break;
			}
			setState(92);
			tableName();
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(93);
				remainingSql();
				}
				break;
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
	public static class RenameTableStatementContext extends ParserRuleContext {
		public TerminalNode RENAME() { return getToken(DdlDatabaseExtractorParser.RENAME, 0); }
		public TerminalNode TABLE() { return getToken(DdlDatabaseExtractorParser.TABLE, 0); }
		public List<TableNameContext> tableName() {
			return getRuleContexts(TableNameContext.class);
		}
		public TableNameContext tableName(int i) {
			return getRuleContext(TableNameContext.class,i);
		}
		public List<TerminalNode> TO() { return getTokens(DdlDatabaseExtractorParser.TO); }
		public TerminalNode TO(int i) {
			return getToken(DdlDatabaseExtractorParser.TO, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DdlDatabaseExtractorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DdlDatabaseExtractorParser.COMMA, i);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public RenameTableStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_renameTableStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterRenameTableStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitRenameTableStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitRenameTableStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RenameTableStatementContext renameTableStatement() throws RecognitionException {
		RenameTableStatementContext _localctx = new RenameTableStatementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_renameTableStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			match(RENAME);
			setState(97);
			match(TABLE);
			setState(98);
			tableName();
			setState(99);
			match(TO);
			setState(100);
			tableName();
			setState(108);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(101);
					match(COMMA);
					setState(102);
					tableName();
					setState(103);
					match(TO);
					setState(104);
					tableName();
					}
					} 
				}
				setState(110);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			}
			setState(112);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(111);
				remainingSql();
				}
				break;
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
	public static class CreateIndexStatementContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DdlDatabaseExtractorParser.CREATE, 0); }
		public TerminalNode INDEX() { return getToken(DdlDatabaseExtractorParser.INDEX, 0); }
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public TerminalNode ON() { return getToken(DdlDatabaseExtractorParser.ON, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public TerminalNode UNIQUE() { return getToken(DdlDatabaseExtractorParser.UNIQUE, 0); }
		public TerminalNode FULLTEXT() { return getToken(DdlDatabaseExtractorParser.FULLTEXT, 0); }
		public TerminalNode SPATIAL() { return getToken(DdlDatabaseExtractorParser.SPATIAL, 0); }
		public CreateIndexStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createIndexStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterCreateIndexStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitCreateIndexStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitCreateIndexStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateIndexStatementContext createIndexStatement() throws RecognitionException {
		CreateIndexStatementContext _localctx = new CreateIndexStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_createIndexStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			match(CREATE);
			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 234881024L) != 0)) {
				{
				setState(115);
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

			setState(118);
			match(INDEX);
			setState(119);
			indexName();
			setState(120);
			match(ON);
			setState(121);
			tableName();
			setState(122);
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
	public static class DropIndexStatementContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DdlDatabaseExtractorParser.DROP, 0); }
		public TerminalNode INDEX() { return getToken(DdlDatabaseExtractorParser.INDEX, 0); }
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public TerminalNode ON() { return getToken(DdlDatabaseExtractorParser.ON, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public IfExistsContext ifExists() {
			return getRuleContext(IfExistsContext.class,0);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public DropIndexStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropIndexStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterDropIndexStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitDropIndexStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitDropIndexStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DropIndexStatementContext dropIndexStatement() throws RecognitionException {
		DropIndexStatementContext _localctx = new DropIndexStatementContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_dropIndexStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			match(DROP);
			setState(125);
			match(INDEX);
			setState(127);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(126);
				ifExists();
				}
				break;
			}
			setState(129);
			indexName();
			setState(130);
			match(ON);
			setState(131);
			tableName();
			setState(133);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(132);
				remainingSql();
				}
				break;
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
	public static class CreateDatabaseStatementContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DdlDatabaseExtractorParser.CREATE, 0); }
		public SchemaNameContext schemaName() {
			return getRuleContext(SchemaNameContext.class,0);
		}
		public TerminalNode DATABASE() { return getToken(DdlDatabaseExtractorParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(DdlDatabaseExtractorParser.SCHEMA, 0); }
		public IfNotExistsContext ifNotExists() {
			return getRuleContext(IfNotExistsContext.class,0);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public CreateDatabaseStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createDatabaseStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterCreateDatabaseStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitCreateDatabaseStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitCreateDatabaseStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateDatabaseStatementContext createDatabaseStatement() throws RecognitionException {
		CreateDatabaseStatementContext _localctx = new CreateDatabaseStatementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_createDatabaseStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			match(CREATE);
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
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(137);
				ifNotExists();
				}
				break;
			}
			setState(140);
			schemaName();
			setState(142);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(141);
				remainingSql();
				}
				break;
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
	public static class DropDatabaseStatementContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DdlDatabaseExtractorParser.DROP, 0); }
		public SchemaNameContext schemaName() {
			return getRuleContext(SchemaNameContext.class,0);
		}
		public TerminalNode DATABASE() { return getToken(DdlDatabaseExtractorParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(DdlDatabaseExtractorParser.SCHEMA, 0); }
		public IfExistsContext ifExists() {
			return getRuleContext(IfExistsContext.class,0);
		}
		public RemainingSqlContext remainingSql() {
			return getRuleContext(RemainingSqlContext.class,0);
		}
		public DropDatabaseStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropDatabaseStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterDropDatabaseStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitDropDatabaseStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitDropDatabaseStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DropDatabaseStatementContext dropDatabaseStatement() throws RecognitionException {
		DropDatabaseStatementContext _localctx = new DropDatabaseStatementContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_dropDatabaseStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			match(DROP);
			setState(145);
			_la = _input.LA(1);
			if ( !(_la==DATABASE || _la==SCHEMA) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(147);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(146);
				ifExists();
				}
				break;
			}
			setState(149);
			schemaName();
			setState(151);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(150);
				remainingSql();
				}
				break;
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
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterOtherStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitOtherStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitOtherStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OtherStatementContext otherStatement() throws RecognitionException {
		OtherStatementContext _localctx = new OtherStatementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_otherStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
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
		public TerminalNode IF() { return getToken(DdlDatabaseExtractorParser.IF, 0); }
		public TerminalNode NOT() { return getToken(DdlDatabaseExtractorParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(DdlDatabaseExtractorParser.EXISTS, 0); }
		public IfNotExistsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifNotExists; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterIfNotExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitIfNotExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitIfNotExists(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfNotExistsContext ifNotExists() throws RecognitionException {
		IfNotExistsContext _localctx = new IfNotExistsContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_ifNotExists);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(IF);
			setState(156);
			match(NOT);
			setState(157);
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
		public TerminalNode IF() { return getToken(DdlDatabaseExtractorParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(DdlDatabaseExtractorParser.EXISTS, 0); }
		public IfExistsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifExists; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterIfExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitIfExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitIfExists(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfExistsContext ifExists() throws RecognitionException {
		IfExistsContext _localctx = new IfExistsContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_ifExists);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			match(IF);
			setState(160);
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
		public TerminalNode DOT() { return getToken(DdlDatabaseExtractorParser.DOT, 0); }
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(162);
				schemaName();
				setState(163);
				match(DOT);
				}
				break;
			}
			setState(167);
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
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterSchemaName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitSchemaName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitSchemaName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaNameContext schemaName() throws RecognitionException {
		SchemaNameContext _localctx = new SchemaNameContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
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
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterIndexName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitIndexName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitIndexName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexNameContext indexName() throws RecognitionException {
		IndexNameContext _localctx = new IndexNameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
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
		public TerminalNode IDENTIFIER() { return getToken(DdlDatabaseExtractorParser.IDENTIFIER, 0); }
		public TerminalNode BACKTICK_QUOTED() { return getToken(DdlDatabaseExtractorParser.BACKTICK_QUOTED, 0); }
		public KeywordAsIdContext keywordAsId() {
			return getRuleContext(KeywordAsIdContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_identifier);
		try {
			setState(176);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(173);
				match(IDENTIFIER);
				}
				break;
			case BACKTICK_QUOTED:
				enterOuterAlt(_localctx, 2);
				{
				setState(174);
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
			case PARTITION:
			case PARTITIONS:
			case LESS:
			case THAN:
			case MAXVALUE:
			case VALUE:
			case VALUES_KW:
			case PROCEDURE:
			case FUNCTION:
			case TRIGGER:
			case EVENT:
			case VIEW:
			case CURSOR:
			case DEFINER:
			case SQL:
			case SECURITY:
			case INVOKER:
			case DETERMINISTIC:
			case CONTAINS:
			case COMMENT:
			case ENGINE:
			case CHARSET:
			case COLLATE:
			case CHARACTER:
				enterOuterAlt(_localctx, 3);
				{
				setState(175);
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
		public TerminalNode TABLE() { return getToken(DdlDatabaseExtractorParser.TABLE, 0); }
		public TerminalNode DATABASE() { return getToken(DdlDatabaseExtractorParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(DdlDatabaseExtractorParser.SCHEMA, 0); }
		public TerminalNode INDEX() { return getToken(DdlDatabaseExtractorParser.INDEX, 0); }
		public TerminalNode KEY() { return getToken(DdlDatabaseExtractorParser.KEY, 0); }
		public TerminalNode TEMPORARY() { return getToken(DdlDatabaseExtractorParser.TEMPORARY, 0); }
		public TerminalNode OR() { return getToken(DdlDatabaseExtractorParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(DdlDatabaseExtractorParser.REPLACE, 0); }
		public TerminalNode IF() { return getToken(DdlDatabaseExtractorParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(DdlDatabaseExtractorParser.EXISTS, 0); }
		public TerminalNode NOT() { return getToken(DdlDatabaseExtractorParser.NOT, 0); }
		public TerminalNode TO() { return getToken(DdlDatabaseExtractorParser.TO, 0); }
		public TerminalNode AND() { return getToken(DdlDatabaseExtractorParser.AND, 0); }
		public TerminalNode NO() { return getToken(DdlDatabaseExtractorParser.NO, 0); }
		public TerminalNode CHAIN() { return getToken(DdlDatabaseExtractorParser.CHAIN, 0); }
		public TerminalNode RELEASE() { return getToken(DdlDatabaseExtractorParser.RELEASE, 0); }
		public TerminalNode WORK() { return getToken(DdlDatabaseExtractorParser.WORK, 0); }
		public TerminalNode BEGIN() { return getToken(DdlDatabaseExtractorParser.BEGIN, 0); }
		public TerminalNode COMMIT() { return getToken(DdlDatabaseExtractorParser.COMMIT, 0); }
		public TerminalNode ROLLBACK() { return getToken(DdlDatabaseExtractorParser.ROLLBACK, 0); }
		public TerminalNode TRANSACTION() { return getToken(DdlDatabaseExtractorParser.TRANSACTION, 0); }
		public TerminalNode SAVEPOINT() { return getToken(DdlDatabaseExtractorParser.SAVEPOINT, 0); }
		public TerminalNode SET() { return getToken(DdlDatabaseExtractorParser.SET, 0); }
		public TerminalNode INSERT() { return getToken(DdlDatabaseExtractorParser.INSERT, 0); }
		public TerminalNode UPDATE() { return getToken(DdlDatabaseExtractorParser.UPDATE, 0); }
		public TerminalNode DELETE() { return getToken(DdlDatabaseExtractorParser.DELETE, 0); }
		public TerminalNode SELECT() { return getToken(DdlDatabaseExtractorParser.SELECT, 0); }
		public TerminalNode CREATE() { return getToken(DdlDatabaseExtractorParser.CREATE, 0); }
		public TerminalNode ALTER() { return getToken(DdlDatabaseExtractorParser.ALTER, 0); }
		public TerminalNode DROP() { return getToken(DdlDatabaseExtractorParser.DROP, 0); }
		public TerminalNode TRUNCATE() { return getToken(DdlDatabaseExtractorParser.TRUNCATE, 0); }
		public TerminalNode RENAME() { return getToken(DdlDatabaseExtractorParser.RENAME, 0); }
		public TerminalNode USE() { return getToken(DdlDatabaseExtractorParser.USE, 0); }
		public TerminalNode START() { return getToken(DdlDatabaseExtractorParser.START, 0); }
		public TerminalNode WITH() { return getToken(DdlDatabaseExtractorParser.WITH, 0); }
		public TerminalNode CONSISTENT() { return getToken(DdlDatabaseExtractorParser.CONSISTENT, 0); }
		public TerminalNode SNAPSHOT() { return getToken(DdlDatabaseExtractorParser.SNAPSHOT, 0); }
		public TerminalNode READ() { return getToken(DdlDatabaseExtractorParser.READ, 0); }
		public TerminalNode WRITE() { return getToken(DdlDatabaseExtractorParser.WRITE, 0); }
		public TerminalNode ONLY() { return getToken(DdlDatabaseExtractorParser.ONLY, 0); }
		public TerminalNode UNIQUE() { return getToken(DdlDatabaseExtractorParser.UNIQUE, 0); }
		public TerminalNode FULLTEXT() { return getToken(DdlDatabaseExtractorParser.FULLTEXT, 0); }
		public TerminalNode SPATIAL() { return getToken(DdlDatabaseExtractorParser.SPATIAL, 0); }
		public TerminalNode ON() { return getToken(DdlDatabaseExtractorParser.ON, 0); }
		public TerminalNode IN() { return getToken(DdlDatabaseExtractorParser.IN, 0); }
		public TerminalNode FROM() { return getToken(DdlDatabaseExtractorParser.FROM, 0); }
		public TerminalNode INTO() { return getToken(DdlDatabaseExtractorParser.INTO, 0); }
		public TerminalNode VALUES() { return getToken(DdlDatabaseExtractorParser.VALUES, 0); }
		public TerminalNode WHERE() { return getToken(DdlDatabaseExtractorParser.WHERE, 0); }
		public TerminalNode ADD() { return getToken(DdlDatabaseExtractorParser.ADD, 0); }
		public TerminalNode COLUMN() { return getToken(DdlDatabaseExtractorParser.COLUMN, 0); }
		public TerminalNode PRIMARY() { return getToken(DdlDatabaseExtractorParser.PRIMARY, 0); }
		public TerminalNode FOREIGN() { return getToken(DdlDatabaseExtractorParser.FOREIGN, 0); }
		public TerminalNode REFERENCES() { return getToken(DdlDatabaseExtractorParser.REFERENCES, 0); }
		public TerminalNode CONSTRAINT() { return getToken(DdlDatabaseExtractorParser.CONSTRAINT, 0); }
		public TerminalNode DEFAULT() { return getToken(DdlDatabaseExtractorParser.DEFAULT, 0); }
		public TerminalNode AUTO_INCREMENT() { return getToken(DdlDatabaseExtractorParser.AUTO_INCREMENT, 0); }
		public TerminalNode NULL() { return getToken(DdlDatabaseExtractorParser.NULL, 0); }
		public TerminalNode NOT_NULL() { return getToken(DdlDatabaseExtractorParser.NOT_NULL, 0); }
		public TerminalNode INT() { return getToken(DdlDatabaseExtractorParser.INT, 0); }
		public TerminalNode VARCHAR() { return getToken(DdlDatabaseExtractorParser.VARCHAR, 0); }
		public TerminalNode CHAR() { return getToken(DdlDatabaseExtractorParser.CHAR, 0); }
		public TerminalNode TEXT() { return getToken(DdlDatabaseExtractorParser.TEXT, 0); }
		public TerminalNode BLOB() { return getToken(DdlDatabaseExtractorParser.BLOB, 0); }
		public TerminalNode DECIMAL() { return getToken(DdlDatabaseExtractorParser.DECIMAL, 0); }
		public TerminalNode FLOAT() { return getToken(DdlDatabaseExtractorParser.FLOAT, 0); }
		public TerminalNode DOUBLE() { return getToken(DdlDatabaseExtractorParser.DOUBLE, 0); }
		public TerminalNode DATE() { return getToken(DdlDatabaseExtractorParser.DATE, 0); }
		public TerminalNode DATETIME() { return getToken(DdlDatabaseExtractorParser.DATETIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(DdlDatabaseExtractorParser.TIMESTAMP, 0); }
		public TerminalNode TIME() { return getToken(DdlDatabaseExtractorParser.TIME, 0); }
		public TerminalNode YEAR() { return getToken(DdlDatabaseExtractorParser.YEAR, 0); }
		public TerminalNode BOOLEAN() { return getToken(DdlDatabaseExtractorParser.BOOLEAN, 0); }
		public TerminalNode BIGINT() { return getToken(DdlDatabaseExtractorParser.BIGINT, 0); }
		public TerminalNode SMALLINT() { return getToken(DdlDatabaseExtractorParser.SMALLINT, 0); }
		public TerminalNode TINYINT() { return getToken(DdlDatabaseExtractorParser.TINYINT, 0); }
		public TerminalNode MEDIUMINT() { return getToken(DdlDatabaseExtractorParser.MEDIUMINT, 0); }
		public TerminalNode INTEGER() { return getToken(DdlDatabaseExtractorParser.INTEGER, 0); }
		public TerminalNode ENUM() { return getToken(DdlDatabaseExtractorParser.ENUM, 0); }
		public TerminalNode SET_KW() { return getToken(DdlDatabaseExtractorParser.SET_KW, 0); }
		public TerminalNode JSON() { return getToken(DdlDatabaseExtractorParser.JSON, 0); }
		public TerminalNode BIT() { return getToken(DdlDatabaseExtractorParser.BIT, 0); }
		public TerminalNode BINARY() { return getToken(DdlDatabaseExtractorParser.BINARY, 0); }
		public TerminalNode VARBINARY() { return getToken(DdlDatabaseExtractorParser.VARBINARY, 0); }
		public TerminalNode PARTITION() { return getToken(DdlDatabaseExtractorParser.PARTITION, 0); }
		public TerminalNode PARTITIONS() { return getToken(DdlDatabaseExtractorParser.PARTITIONS, 0); }
		public TerminalNode LESS() { return getToken(DdlDatabaseExtractorParser.LESS, 0); }
		public TerminalNode THAN() { return getToken(DdlDatabaseExtractorParser.THAN, 0); }
		public TerminalNode MAXVALUE() { return getToken(DdlDatabaseExtractorParser.MAXVALUE, 0); }
		public TerminalNode VALUE() { return getToken(DdlDatabaseExtractorParser.VALUE, 0); }
		public TerminalNode VALUES_KW() { return getToken(DdlDatabaseExtractorParser.VALUES_KW, 0); }
		public TerminalNode PROCEDURE() { return getToken(DdlDatabaseExtractorParser.PROCEDURE, 0); }
		public TerminalNode FUNCTION() { return getToken(DdlDatabaseExtractorParser.FUNCTION, 0); }
		public TerminalNode TRIGGER() { return getToken(DdlDatabaseExtractorParser.TRIGGER, 0); }
		public TerminalNode EVENT() { return getToken(DdlDatabaseExtractorParser.EVENT, 0); }
		public TerminalNode VIEW() { return getToken(DdlDatabaseExtractorParser.VIEW, 0); }
		public TerminalNode CURSOR() { return getToken(DdlDatabaseExtractorParser.CURSOR, 0); }
		public TerminalNode DEFINER() { return getToken(DdlDatabaseExtractorParser.DEFINER, 0); }
		public TerminalNode SQL() { return getToken(DdlDatabaseExtractorParser.SQL, 0); }
		public TerminalNode SECURITY() { return getToken(DdlDatabaseExtractorParser.SECURITY, 0); }
		public TerminalNode INVOKER() { return getToken(DdlDatabaseExtractorParser.INVOKER, 0); }
		public TerminalNode DETERMINISTIC() { return getToken(DdlDatabaseExtractorParser.DETERMINISTIC, 0); }
		public TerminalNode CONTAINS() { return getToken(DdlDatabaseExtractorParser.CONTAINS, 0); }
		public TerminalNode COMMENT() { return getToken(DdlDatabaseExtractorParser.COMMENT, 0); }
		public TerminalNode ENGINE() { return getToken(DdlDatabaseExtractorParser.ENGINE, 0); }
		public TerminalNode CHARSET() { return getToken(DdlDatabaseExtractorParser.CHARSET, 0); }
		public TerminalNode COLLATE() { return getToken(DdlDatabaseExtractorParser.COLLATE, 0); }
		public TerminalNode CHARACTER() { return getToken(DdlDatabaseExtractorParser.CHARACTER, 0); }
		public KeywordAsIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keywordAsId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterKeywordAsId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitKeywordAsId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitKeywordAsId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeywordAsIdContext keywordAsId() throws RecognitionException {
		KeywordAsIdContext _localctx = new KeywordAsIdContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_keywordAsId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & -4096L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 72057594037927935L) != 0)) ) {
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
		public List<TerminalNode> EOF() { return getTokens(DdlDatabaseExtractorParser.EOF); }
		public TerminalNode EOF(int i) {
			return getToken(DdlDatabaseExtractorParser.EOF, i);
		}
		public RemainingSqlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_remainingSql; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).enterRemainingSql(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DdlDatabaseExtractorListener ) ((DdlDatabaseExtractorListener)listener).exitRemainingSql(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DdlDatabaseExtractorVisitor ) return ((DdlDatabaseExtractorVisitor<? extends T>)visitor).visitRemainingSql(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RemainingSqlContext remainingSql() throws RecognitionException {
		RemainingSqlContext _localctx = new RemainingSqlContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_remainingSql);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -2L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 288230376151711743L) != 0)) {
				{
				{
				setState(180);
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
				setState(185);
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
		"\u0004\u0001y\u00bb\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0003\u0000/\b\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001:\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0003\u0002?\b\u0002\u0001\u0002\u0003\u0002B\b\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002F\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004S\b\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004W\b\u0004\u0001\u0005\u0001\u0005\u0003\u0005[\b\u0005\u0001\u0005"+
		"\u0001\u0005\u0003\u0005_\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0005\u0006k\b\u0006\n\u0006\f\u0006n\t\u0006\u0001\u0006"+
		"\u0003\u0006q\b\u0006\u0001\u0007\u0001\u0007\u0003\u0007u\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\b\u0001\b\u0001\b\u0003\b\u0080\b\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003"+
		"\b\u0086\b\b\u0001\t\u0001\t\u0001\t\u0003\t\u008b\b\t\u0001\t\u0001\t"+
		"\u0003\t\u008f\b\t\u0001\n\u0001\n\u0001\n\u0003\n\u0094\b\n\u0001\n\u0001"+
		"\n\u0003\n\u0098\b\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e"+
		"\u00a6\b\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u00b1\b\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0013\u0005\u0013\u00b6\b\u0013\n\u0013"+
		"\f\u0013\u00b9\t\u0013\u0001\u0013\u0000\u0000\u0014\u0000\u0002\u0004"+
		"\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \""+
		"$&\u0000\u0004\u0001\u0000\u0019\u001b\u0001\u000001\u0001\u0000\fw\u0000"+
		"\u0001\u00c3\u0000.\u0001\u0000\u0000\u0000\u00029\u0001\u0000\u0000\u0000"+
		"\u0004;\u0001\u0000\u0000\u0000\u0006J\u0001\u0000\u0000\u0000\bO\u0001"+
		"\u0000\u0000\u0000\nX\u0001\u0000\u0000\u0000\f`\u0001\u0000\u0000\u0000"+
		"\u000er\u0001\u0000\u0000\u0000\u0010|\u0001\u0000\u0000\u0000\u0012\u0087"+
		"\u0001\u0000\u0000\u0000\u0014\u0090\u0001\u0000\u0000\u0000\u0016\u0099"+
		"\u0001\u0000\u0000\u0000\u0018\u009b\u0001\u0000\u0000\u0000\u001a\u009f"+
		"\u0001\u0000\u0000\u0000\u001c\u00a5\u0001\u0000\u0000\u0000\u001e\u00a9"+
		"\u0001\u0000\u0000\u0000 \u00ab\u0001\u0000\u0000\u0000\"\u00b0\u0001"+
		"\u0000\u0000\u0000$\u00b2\u0001\u0000\u0000\u0000&\u00b7\u0001\u0000\u0000"+
		"\u0000()\u0003\u0002\u0001\u0000)*\u0005\u0000\u0000\u0001*/\u0001\u0000"+
		"\u0000\u0000+,\u0003\u0016\u000b\u0000,-\u0005\u0000\u0000\u0001-/\u0001"+
		"\u0000\u0000\u0000.(\u0001\u0000\u0000\u0000.+\u0001\u0000\u0000\u0000"+
		"/\u0001\u0001\u0000\u0000\u00000:\u0003\u0004\u0002\u00001:\u0003\u0006"+
		"\u0003\u00002:\u0003\b\u0004\u00003:\u0003\n\u0005\u00004:\u0003\f\u0006"+
		"\u00005:\u0003\u000e\u0007\u00006:\u0003\u0010\b\u00007:\u0003\u0012\t"+
		"\u00008:\u0003\u0014\n\u000090\u0001\u0000\u0000\u000091\u0001\u0000\u0000"+
		"\u000092\u0001\u0000\u0000\u000093\u0001\u0000\u0000\u000094\u0001\u0000"+
		"\u0000\u000095\u0001\u0000\u0000\u000096\u0001\u0000\u0000\u000097\u0001"+
		"\u0000\u0000\u000098\u0001\u0000\u0000\u0000:\u0003\u0001\u0000\u0000"+
		"\u0000;>\u0005\f\u0000\u0000<=\u0005\r\u0000\u0000=?\u0005\u000e\u0000"+
		"\u0000><\u0001\u0000\u0000\u0000>?\u0001\u0000\u0000\u0000?A\u0001\u0000"+
		"\u0000\u0000@B\u0005\u000f\u0000\u0000A@\u0001\u0000\u0000\u0000AB\u0001"+
		"\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000CE\u0005\u0010\u0000\u0000"+
		"DF\u0003\u0018\f\u0000ED\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000"+
		"FG\u0001\u0000\u0000\u0000GH\u0003\u001c\u000e\u0000HI\u0003&\u0013\u0000"+
		"I\u0005\u0001\u0000\u0000\u0000JK\u0005\u0014\u0000\u0000KL\u0005\u0010"+
		"\u0000\u0000LM\u0003\u001c\u000e\u0000MN\u0003&\u0013\u0000N\u0007\u0001"+
		"\u0000\u0000\u0000OP\u0005\u0015\u0000\u0000PR\u0005\u0010\u0000\u0000"+
		"QS\u0003\u001a\r\u0000RQ\u0001\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000"+
		"ST\u0001\u0000\u0000\u0000TV\u0003\u001c\u000e\u0000UW\u0003&\u0013\u0000"+
		"VU\u0001\u0000\u0000\u0000VW\u0001\u0000\u0000\u0000W\t\u0001\u0000\u0000"+
		"\u0000XZ\u0005\u0016\u0000\u0000Y[\u0005\u0010\u0000\u0000ZY\u0001\u0000"+
		"\u0000\u0000Z[\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\^\u0003"+
		"\u001c\u000e\u0000]_\u0003&\u0013\u0000^]\u0001\u0000\u0000\u0000^_\u0001"+
		"\u0000\u0000\u0000_\u000b\u0001\u0000\u0000\u0000`a\u0005\u0017\u0000"+
		"\u0000ab\u0005\u0010\u0000\u0000bc\u0003\u001c\u000e\u0000cd\u0005\u0018"+
		"\u0000\u0000dl\u0003\u001c\u000e\u0000ef\u0005\u0002\u0000\u0000fg\u0003"+
		"\u001c\u000e\u0000gh\u0005\u0018\u0000\u0000hi\u0003\u001c\u000e\u0000"+
		"ik\u0001\u0000\u0000\u0000je\u0001\u0000\u0000\u0000kn\u0001\u0000\u0000"+
		"\u0000lj\u0001\u0000\u0000\u0000lm\u0001\u0000\u0000\u0000mp\u0001\u0000"+
		"\u0000\u0000nl\u0001\u0000\u0000\u0000oq\u0003&\u0013\u0000po\u0001\u0000"+
		"\u0000\u0000pq\u0001\u0000\u0000\u0000q\r\u0001\u0000\u0000\u0000rt\u0005"+
		"\f\u0000\u0000su\u0007\u0000\u0000\u0000ts\u0001\u0000\u0000\u0000tu\u0001"+
		"\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vw\u0005\u001c\u0000\u0000"+
		"wx\u0003 \u0010\u0000xy\u0005\u001e\u0000\u0000yz\u0003\u001c\u000e\u0000"+
		"z{\u0003&\u0013\u0000{\u000f\u0001\u0000\u0000\u0000|}\u0005\u0015\u0000"+
		"\u0000}\u007f\u0005\u001c\u0000\u0000~\u0080\u0003\u001a\r\u0000\u007f"+
		"~\u0001\u0000\u0000\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0081"+
		"\u0001\u0000\u0000\u0000\u0081\u0082\u0003 \u0010\u0000\u0082\u0083\u0005"+
		"\u001e\u0000\u0000\u0083\u0085\u0003\u001c\u000e\u0000\u0084\u0086\u0003"+
		"&\u0013\u0000\u0085\u0084\u0001\u0000\u0000\u0000\u0085\u0086\u0001\u0000"+
		"\u0000\u0000\u0086\u0011\u0001\u0000\u0000\u0000\u0087\u0088\u0005\f\u0000"+
		"\u0000\u0088\u008a\u0007\u0001\u0000\u0000\u0089\u008b\u0003\u0018\f\u0000"+
		"\u008a\u0089\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000"+
		"\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008e\u0003\u001e\u000f\u0000"+
		"\u008d\u008f\u0003&\u0013\u0000\u008e\u008d\u0001\u0000\u0000\u0000\u008e"+
		"\u008f\u0001\u0000\u0000\u0000\u008f\u0013\u0001\u0000\u0000\u0000\u0090"+
		"\u0091\u0005\u0015\u0000\u0000\u0091\u0093\u0007\u0001\u0000\u0000\u0092"+
		"\u0094\u0003\u001a\r\u0000\u0093\u0092\u0001\u0000\u0000\u0000\u0093\u0094"+
		"\u0001\u0000\u0000\u0000\u0094\u0095\u0001\u0000\u0000\u0000\u0095\u0097"+
		"\u0003\u001e\u000f\u0000\u0096\u0098\u0003&\u0013\u0000\u0097\u0096\u0001"+
		"\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000\u0000\u0098\u0015\u0001"+
		"\u0000\u0000\u0000\u0099\u009a\u0003&\u0013\u0000\u009a\u0017\u0001\u0000"+
		"\u0000\u0000\u009b\u009c\u0005\u0011\u0000\u0000\u009c\u009d\u0005\u0012"+
		"\u0000\u0000\u009d\u009e\u0005\u0013\u0000\u0000\u009e\u0019\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u0005\u0011\u0000\u0000\u00a0\u00a1\u0005\u0013"+
		"\u0000\u0000\u00a1\u001b\u0001\u0000\u0000\u0000\u00a2\u00a3\u0003\u001e"+
		"\u000f\u0000\u00a3\u00a4\u0005\u0003\u0000\u0000\u00a4\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a5\u00a2\u0001\u0000\u0000\u0000\u00a5\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a6\u00a7\u0001\u0000\u0000\u0000\u00a7\u00a8\u0003\"\u0011"+
		"\u0000\u00a8\u001d\u0001\u0000\u0000\u0000\u00a9\u00aa\u0003\"\u0011\u0000"+
		"\u00aa\u001f\u0001\u0000\u0000\u0000\u00ab\u00ac\u0003\"\u0011\u0000\u00ac"+
		"!\u0001\u0000\u0000\u0000\u00ad\u00b1\u0005x\u0000\u0000\u00ae\u00b1\u0005"+
		"\n\u0000\u0000\u00af\u00b1\u0003$\u0012\u0000\u00b0\u00ad\u0001\u0000"+
		"\u0000\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000\u00b0\u00af\u0001\u0000"+
		"\u0000\u0000\u00b1#\u0001\u0000\u0000\u0000\u00b2\u00b3\u0007\u0002\u0000"+
		"\u0000\u00b3%\u0001\u0000\u0000\u0000\u00b4\u00b6\b\u0003\u0000\u0000"+
		"\u00b5\u00b4\u0001\u0000\u0000\u0000\u00b6\u00b9\u0001\u0000\u0000\u0000"+
		"\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000"+
		"\u00b8\'\u0001\u0000\u0000\u0000\u00b9\u00b7\u0001\u0000\u0000\u0000\u0015"+
		".9>AERVZ^lpt\u007f\u0085\u008a\u008e\u0093\u0097\u00a5\u00b0\u00b7";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}