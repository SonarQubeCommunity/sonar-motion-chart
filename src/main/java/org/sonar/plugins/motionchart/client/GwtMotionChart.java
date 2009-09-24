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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.web.gwt.client.AbstractPage;
import org.sonar.api.web.gwt.client.ResourceDictionary;
import org.sonar.api.web.gwt.client.webservices.BaseQueryCallback;
import org.sonar.api.web.gwt.client.webservices.Properties;
import org.sonar.api.web.gwt.client.webservices.PropertiesQuery;
import org.sonar.api.web.gwt.client.webservices.SequentialQueries;
import org.sonar.api.web.gwt.client.webservices.VoidResponse;
import org.sonar.api.web.gwt.client.webservices.WSMetrics;
import org.sonar.api.web.gwt.client.webservices.WSMetrics.Metric;
import org.sonar.api.web.gwt.client.widgets.LoadingLabel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.gwt.visualization.client.visualizations.MotionChart.Options;

public class GwtMotionChart extends AbstractPage {
  public static final String GWT_ID = "org.sonar.plugins.motionchart.GwtMotionChart";
  public static final String DEFAULT_WIDTH = "800";
  public static final String DEFAULT_HEIGHT = "600";
  public static final String HEIGHT_PROP = "sonar.motionchart.height";
  public static final String WIDTH_PROP = "sonar.motionchart.width";
  
  private Properties properties = null;

  public void onModuleLoad() {
    final String projectKey = ResourceDictionary.getResourceKey();
    getRootPanel().add(new LoadingLabel());
    
    PropertiesQuery propsQ = new PropertiesQuery();
    BaseQueryCallback<Properties> propsCb = new BaseQueryCallback<Properties>() {
      public void onResponse(Properties response, JavaScriptObject jsonRawResponse) {
        properties = response;
      }
    };

    final Runnable onLoadCallback = new Runnable() {
      public void run() {
        MotionchartQuery.get(projectKey)
            .setMetrics(getDefaultMetrics())
            .execute(new BaseQueryCallback<DataTable>() {
              public void onResponse(DataTable response, JavaScriptObject jsonRawResponse) {
                Widget toDisplay = response.getTable().getNumberOfRows() > 0 ? 
                    new MotionChart(response.getTable(), createOptions()) : getNoProjects();
                displayView(toDisplay);
              }
            });
      }
    };
    BaseQueryCallback<VoidResponse> queriesCb = new BaseQueryCallback<VoidResponse>() {
      public void onResponse(VoidResponse response, JavaScriptObject jsonRawResponse) {
        VisualizationUtils.loadVisualizationApi(onLoadCallback, MotionChart.PACKAGE);
      }
    };
    SequentialQueries queries = SequentialQueries.get().add(propsQ, propsCb);
    queries.execute(queriesCb);
  }
  
  private Widget getNoProjects() {
    String msg = "<h3>No projects have been analysed.</h3>" +
                 "<p>If Maven and Sonar are installed with default parameters on the same box, just launch the command" +
                 "<code>mvn sonar:sonar</code> to analyse your first project. In any other case, please refer to the " +
                 "<a href='http://sonar.codehaus.org/documentation'>documentation</a>.</p>";
    return new HTML(msg);
  }

  private List<Metric> getDefaultMetrics() {
    List<Metric> defaults = new ArrayList<Metric>();
    defaults.add(WSMetrics.VIOLATIONS_DENSITY); // X
    defaults.add(WSMetrics.DUPLICATED_LINES_DENSITY); // Y
    defaults.add(WSMetrics.COVERAGE); // COLOR
    defaults.add(WSMetrics.COMPLEXITY); // SIZE
    
    defaults.add(WSMetrics.TEST_SUCCESS_DENSITY);
    defaults.add(WSMetrics.PUBLIC_DOCUMENTED_API_DENSITY);
    defaults.add(WSMetrics.UNCOVERED_COMPLEXITY_BY_TESTS);
    defaults.add(WSMetrics.COMMENT_LINES_DENSITY);
    defaults.add(WSMetrics.NCLOC);
    defaults.add(WSMetrics.TESTS_EXECUTION_TIME);
    defaults.add(WSMetrics.WEIGHTED_VIOLATIONS);
    defaults.add(WSMetrics.FUNCTION_COMPLEXITY);
    return defaults;
  }
  
  private Options createOptions() {
    Options options = Options.create();
    options.setWidth(Integer.parseInt(properties.get(GwtMotionChart.WIDTH_PROP, GwtMotionChart.DEFAULT_WIDTH)));
    options.setHeight(Integer.parseInt(properties.get(GwtMotionChart.HEIGHT_PROP, GwtMotionChart.DEFAULT_HEIGHT)));
    // state options does unfortunalty not work see http://code.google.com/apis/visualization/documentation/gallery/motionchart.html#Motion_Chart_initial_state
    //options.setOption("state", "{...}");
    return options;
  }
}
