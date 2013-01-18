package com.onsmsc.db.shovler;

import java.util.Arrays;

import javax.jms.Message;

public class StatementHolder {
	private StatementTypes statementTypes;
	private String statement;
	private Object[] arguments;

	public StatementHolder() {

	}

	public StatementTypes getStatementTypes() {
		return statementTypes;
	}

	public void setStatementTypes(StatementTypes statementTypes) {
		this.statementTypes = statementTypes;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arguments);
		result = prime * result
				+ ((statement == null) ? 0 : statement.hashCode());
		result = prime * result
				+ ((statementTypes == null) ? 0 : statementTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatementHolder other = (StatementHolder) obj;
		if (!Arrays.equals(arguments, other.arguments))
			return false;
		if (statement == null) {
			if (other.statement != null)
				return false;
		} else if (!statement.equals(other.statement))
			return false;
		if (statementTypes != other.statementTypes)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StatementHolder [statementTypes=" + statementTypes
				+ ", statement=" + statement + ", arguments="
				+ Arrays.toString(arguments) + "]";
	}

	public static StatementTypes detectType(Message message) {
		// TODO Auto-generated method stub
		return null;
	}

	public static StatementHolder newFromMessage(Message message) {
		// TODO Auto-generated method stub
		return null;
	}
}
