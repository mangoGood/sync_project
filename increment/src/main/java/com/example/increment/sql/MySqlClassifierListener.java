// Generated from com/example/increment/sql/MySqlClassifier.g4 by ANTLR 4.13.1
package com.example.increment.sql;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MySqlClassifierParser}.
 */
public interface MySqlClassifierListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MySqlClassifierParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MySqlClassifierParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDdlStatement(MySqlClassifierParser.DdlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDdlStatement(MySqlClassifierParser.DdlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDmlStatement(MySqlClassifierParser.DmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDmlStatement(MySqlClassifierParser.DmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#transactionStatement}.
	 * @param ctx the parse tree
	 */
	void enterTransactionStatement(MySqlClassifierParser.TransactionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#transactionStatement}.
	 * @param ctx the parse tree
	 */
	void exitTransactionStatement(MySqlClassifierParser.TransactionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#useStatement}.
	 * @param ctx the parse tree
	 */
	void enterUseStatement(MySqlClassifierParser.UseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#useStatement}.
	 * @param ctx the parse tree
	 */
	void exitUseStatement(MySqlClassifierParser.UseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetStatement(MySqlClassifierParser.SetStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetStatement(MySqlClassifierParser.SetStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#otherStatement}.
	 * @param ctx the parse tree
	 */
	void enterOtherStatement(MySqlClassifierParser.OtherStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#otherStatement}.
	 * @param ctx the parse tree
	 */
	void exitOtherStatement(MySqlClassifierParser.OtherStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#ifNotExists}.
	 * @param ctx the parse tree
	 */
	void enterIfNotExists(MySqlClassifierParser.IfNotExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#ifNotExists}.
	 * @param ctx the parse tree
	 */
	void exitIfNotExists(MySqlClassifierParser.IfNotExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void enterIfExists(MySqlClassifierParser.IfExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void exitIfExists(MySqlClassifierParser.IfExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(MySqlClassifierParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(MySqlClassifierParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(MySqlClassifierParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(MySqlClassifierParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(MySqlClassifierParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(MySqlClassifierParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(MySqlClassifierParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(MySqlClassifierParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#keywordAsId}.
	 * @param ctx the parse tree
	 */
	void enterKeywordAsId(MySqlClassifierParser.KeywordAsIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#keywordAsId}.
	 * @param ctx the parse tree
	 */
	void exitKeywordAsId(MySqlClassifierParser.KeywordAsIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link MySqlClassifierParser#remainingSql}.
	 * @param ctx the parse tree
	 */
	void enterRemainingSql(MySqlClassifierParser.RemainingSqlContext ctx);
	/**
	 * Exit a parse tree produced by {@link MySqlClassifierParser#remainingSql}.
	 * @param ctx the parse tree
	 */
	void exitRemainingSql(MySqlClassifierParser.RemainingSqlContext ctx);
}