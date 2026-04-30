// Generated from com/example/increment/sql/MySqlClassifier.g4 by ANTLR 4.13.1
package com.example.increment.sql;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MySqlClassifierParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MySqlClassifierVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MySqlClassifierParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#ddlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdlStatement(MySqlClassifierParser.DdlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#dmlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDmlStatement(MySqlClassifierParser.DmlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#transactionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionStatement(MySqlClassifierParser.TransactionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#useStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUseStatement(MySqlClassifierParser.UseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#setStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStatement(MySqlClassifierParser.SetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#otherStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherStatement(MySqlClassifierParser.OtherStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#ifNotExists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfNotExists(MySqlClassifierParser.IfNotExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#ifExists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfExists(MySqlClassifierParser.IfExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(MySqlClassifierParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(MySqlClassifierParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(MySqlClassifierParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(MySqlClassifierParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#keywordAsId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordAsId(MySqlClassifierParser.KeywordAsIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link MySqlClassifierParser#remainingSql}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemainingSql(MySqlClassifierParser.RemainingSqlContext ctx);
}