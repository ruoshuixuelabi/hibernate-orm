/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.ast.tree.expression;

import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.query.sqm.sql.internal.DomainResultProducer;
import org.hibernate.sql.ast.SqlAstWalker;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.basic.BasicResult;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A numeric literal coming from an HQL query, which needs special handling
 *
 * @see org.hibernate.query.sqm.tree.expression.SqmHqlNumericLiteral
 *
 * @author Steve Ebersole
 */
public class UnparsedNumericLiteral<N extends Number> implements Expression, DomainResultProducer<N> {
	private final String literalValue;
	private final JdbcMapping jdbcMapping;

	public UnparsedNumericLiteral(String literalValue, JdbcMapping jdbcMapping) {
		this.literalValue = literalValue;
		this.jdbcMapping = jdbcMapping;
	}

	public String getLiteralValue() {
		return literalValue;
	}

	public JdbcMapping getJdbcMapping() {
		return jdbcMapping;
	}

	@Override
	public JdbcMappingContainer getExpressionType() {
		return getJdbcMapping();
	}

	@Override
	public DomainResult<N> createDomainResult(String resultVariable, DomainResultCreationState creationState) {
		final SqlExpressionResolver sqlExpressionResolver = creationState.getSqlAstCreationState().getSqlExpressionResolver();
		final TypeConfiguration typeConfiguration = creationState.getSqlAstCreationState().getCreationContext().getSessionFactory().getTypeConfiguration();

		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				this,
				getJdbcMapping().getJdbcJavaType(),
				null,
				typeConfiguration
		);

		return new BasicResult<>(
				sqlSelection.getValuesArrayPosition(),
				resultVariable,
				jdbcMapping
		);
	}

	@Override
	public void applySqlSelections(DomainResultCreationState creationState) {
		creationState.getSqlAstCreationState().getSqlExpressionResolver().resolveSqlSelection(
				this,
				jdbcMapping.getJdbcJavaType(),
				null,
				creationState.getSqlAstCreationState().getCreationContext().getMappingMetamodel().getTypeConfiguration()
		);
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitUnparsedNumericLiteral( this );
	}
}
