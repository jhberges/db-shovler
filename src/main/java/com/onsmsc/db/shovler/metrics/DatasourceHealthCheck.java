package com.onsmsc.db.shovler.metrics;

import java.sql.Connection;

import javax.sql.DataSource;

import com.codahale.metrics.health.HealthCheck;

public class DatasourceHealthCheck extends HealthCheck {

	private final DataSource dataSource;

	public DatasourceHealthCheck(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected Result check() throws Exception {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			return connection.isClosed() ? Result.unhealthy("Connection is closed") : Result.healthy();
		} catch (Exception e) {
			connection.close();
			throw e;
		}

	}

}
