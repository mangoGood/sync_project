// Generated from com/example/extract/sql/DdlDatabaseExtractor.g4 by ANTLR 4.13.1
package com.example.extract.sql;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DdlDatabaseExtractorParser}.
 */
public interface DdlDatabaseExtractorListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(DdlDatabaseExtractorParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(DdlDatabaseExtractorParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDdlStatement(DdlDatabaseExtractorParser.DdlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDdlStatement(DdlDatabaseExtractorParser.DdlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#createTableStatement}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableStatement(DdlDatabaseExtractorParser.CreateTableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#createTableStatement}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableStatement(DdlDatabaseExtractorParser.CreateTableStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#alterTableStatement}.
	 * @param ctx the parse tree
	 */
	void enterAlterTableStatement(DdlDatabaseExtractorParser.AlterTableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#alterTableStatement}.
	 * @param ctx the parse tree
	 */
	void exitAlterTableStatement(DdlDatabaseExtractorParser.AlterTableStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#dropTableStatement}.
	 * @param ctx the parse tree
	 */
	void enterDropTableStatement(DdlDatabaseExtractorParser.DropTableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#dropTableStatement}.
	 * @param ctx the parse tree
	 */
	void exitDropTableStatement(DdlDatabaseExtractorParser.DropTableStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#truncateStatement}.
	 * @param ctx the parse tree
	 */
	void enterTruncateStatement(DdlDatabaseExtractorParser.TruncateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#truncateStatement}.
	 * @param ctx the parse tree
	 */
	void exitTruncateStatement(DdlDatabaseExtractorParser.TruncateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#renameTableStatement}.
	 * @param ctx the parse tree
	 */
	void enterRenameTableStatement(DdlDatabaseExtractorParser.RenameTableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#renameTableStatement}.
	 * @param ctx the parse tree
	 */
	void exitRenameTableStatement(DdlDatabaseExtractorParser.RenameTableStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#createIndexStatement}.
	 * @param ctx the parse tree
	 */
	void enterCreateIndexStatement(DdlDatabaseExtractorParser.CreateIndexStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#createIndexStatement}.
	 * @param ctx the parse tree
	 */
	void exitCreateIndexStatement(DdlDatabaseExtractorParser.CreateIndexStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#dropIndexStatement}.
	 * @param ctx the parse tree
	 */
	void enterDropIndexStatement(DdlDatabaseExtractorParser.DropIndexStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#dropIndexStatement}.
	 * @param ctx the parse tree
	 */
	void exitDropIndexStatement(DdlDatabaseExtractorParser.DropIndexStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#createDatabaseStatement}.
	 * @param ctx the parse tree
	 */
	void enterCreateDatabaseStatement(DdlDatabaseExtractorParser.CreateDatabaseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#createDatabaseStatement}.
	 * @param ctx the parse tree
	 */
	void exitCreateDatabaseStatement(DdlDatabaseExtractorParser.CreateDatabaseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#dropDatabaseStatement}.
	 * @param ctx the parse tree
	 */
	void enterDropDatabaseStatement(DdlDatabaseExtractorParser.DropDatabaseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#dropDatabaseStatement}.
	 * @param ctx the parse tree
	 */
	void exitDropDatabaseStatement(DdlDatabaseExtractorParser.DropDatabaseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#otherStatement}.
	 * @param ctx the parse tree
	 */
	void enterOtherStatement(DdlDatabaseExtractorParser.OtherStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#otherStatement}.
	 * @param ctx the parse tree
	 */
	void exitOtherStatement(DdlDatabaseExtractorParser.OtherStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#ifNotExists}.
	 * @param ctx the parse tree
	 */
	void enterIfNotExists(DdlDatabaseExtractorParser.IfNotExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#ifNotExists}.
	 * @param ctx the parse tree
	 */
	void exitIfNotExists(DdlDatabaseExtractorParser.IfNotExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void enterIfExists(DdlDatabaseExtractorParser.IfExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void exitIfExists(DdlDatabaseExtractorParser.IfExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(DdlDatabaseExtractorParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(DdlDatabaseExtractorParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(DdlDatabaseExtractorParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(DdlDatabaseExtractorParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(DdlDatabaseExtractorParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(DdlDatabaseExtractorParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(DdlDatabaseExtractorParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(DdlDatabaseExtractorParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#keywordAsId}.
	 * @param ctx the parse tree
	 */
	void enterKeywordAsId(DdlDatabaseExtractorParser.KeywordAsIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#keywordAsId}.
	 * @param ctx the parse tree
	 */
	void exitKeywordAsId(DdlDatabaseExtractorParser.KeywordAsIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DdlDatabaseExtractorParser#remainingSql}.
	 * @param ctx the parse tree
	 */
	void enterRemainingSql(DdlDatabaseExtractorParser.RemainingSqlContext ctx);
	/**
	 * Exit a parse tree produced by {@link DdlDatabaseExtractorParser#remainingSql}.
	 * @param ctx the parse tree
	 */
	void exitRemainingSql(DdlDatabaseExtractorParser.RemainingSqlContext ctx);
}