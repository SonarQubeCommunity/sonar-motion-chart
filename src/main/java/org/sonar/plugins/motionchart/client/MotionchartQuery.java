/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.motionchart.client;

import java.util.List;

import org.sonar.api.web.gwt.client.Utils;
import org.sonar.api.web.gwt.client.webservices.AbstractResourceQuery;
import org.sonar.api.web.gwt.client.webservices.QueryCallBack;
import org.sonar.api.web.gwt.client.webservices.WSMetrics.Metric;

import com.google.gwt.visualization.client.Query;
import com.google.gwt.visualization.client.QueryResponse;
import com.google.gwt.visualization.client.Query.Callback;

public final class MotionchartQuery extends AbstractResourceQuery<DataTable> {

  private String metrics;

  public static MotionchartQuery get(String resourceKey) {
    return new MotionchartQuery(resourceKey);
  }

  private MotionchartQuery(String resourceKey) {
    super(resourceKey);
  }

  public MotionchartQuery setMetrics(List<Metric> metrics) {
    this.metrics = getMetricsWSRequest(metrics);
    return this;
  }

  private final String getMetricsWSRequest(List<Metric> metrics) {
    StringBuilder metricsDelimByComma = new StringBuilder(64);
    for (Metric metric : metrics) {
      metricsDelimByComma.append(metric.getKey()).append(",");
    }
    return metricsDelimByComma.substring(0, metricsDelimByComma.length() - 1);
  }

  @Override
  public String toString() {
    String url = Utils.getServerApiUrl() + "/plugins/RubyMotionchartWebService";
    url = getResourceKey() == null ? url + "?out=json" : url + "?resource=" + getResourceKey() + "&out=json";
    if (metrics != null) {
      url += "&metrics=" + metrics;
    }
    return url;
  }

  @Override
  public void execute(final QueryCallBack<DataTable> callback) {
    Callback queryCallBack = new Callback() {
      public void onResponse(QueryResponse response) {
        if (response.isError()) {
          // not great but unfortunatly QueryResponse does not provide a better way to detect timeout 
          if (response.getMessage().toLowerCase().contains("timed out")) {
            callback.onTimeout();
          } else {
            callback.onError(500, response.getDetailedMessage());
          }
        } else {
          callback.onResponse(new DataTable(response.getDataTable()), null);
        }
      }
    };
    Query query = Query.create(toString());
    query.send(queryCallBack);
  }
  
}
