// Generated from com/example/extract/sql/DdlDatabaseExtractor.g4 by ANTLR 4.13.1
package com.example.extract.sql;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DdlDatabaseExtractorParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DdlDatabaseExtractorVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(DdlDatabaseExtractorParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#ddlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdlStatement(DdlDatabaseExtractorParser.DdlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#createTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableStatement(DdlDatabaseExtractorParser.CreateTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#alterTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableStatement(DdlDatabaseExtractorParser.AlterTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#dropTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTableStatement(DdlDatabaseExtractorParser.DropTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#truncateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncateStatement(DdlDatabaseExtractorParser.TruncateStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#renameTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameTableStatement(DdlDatabaseExtractorParser.RenameTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#createIndexStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateIndexStatement(DdlDatabaseExtractorParser.CreateIndexStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#dropIndexStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropIndexStatement(DdlDatabaseExtractorParser.DropIndexStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#createDatabaseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDatabaseStatement(DdlDatabaseExtractorParser.CreateDatabaseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#dropDatabaseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropDatabaseStatement(DdlDatabaseExtractorParser.DropDatabaseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#otherStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherStatement(DdlDatabaseExtractorParser.OtherStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#ifNotExists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfNotExists(DdlDatabaseExtractorParser.IfNotExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#ifExists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfExists(DdlDatabaseExtractorParser.IfExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(DdlDatabaseExtractorParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(DdlDatabaseExtractorParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(DdlDatabaseExtractorParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DdlDatabaseExtractorParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#keywordAsId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordAsId(DdlDatabaseExtractorParser.KeywordAsIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DdlDatabaseExtractorParser#remainingSql}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemainingSql(DdlDatabaseExtractorParser.RemainingSqlContext ctx);
}