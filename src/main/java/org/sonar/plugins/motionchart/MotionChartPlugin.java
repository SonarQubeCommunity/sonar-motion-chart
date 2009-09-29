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
package org.sonar.plugins.motionchart;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

@Properties({
  @Property(
      key = MotionChartPlugin.WIDTH_PROP,
      name = "Chart width",
      description = "The motion chart width in pixels",
      defaultValue = MotionChartPlugin.DEFAULT_WIDTH),
  @Property(
      key = MotionChartPlugin.HEIGHT_PROP,
      name = "Chart height",
      description = "The motion chart height in pixels",
      defaultValue = MotionChartPlugin.DEFAULT_HEIGHT),
  @Property(
      key = MotionChartPlugin.AXIS_METRICS_PROP,
      name = "Axis metrics",
      description = "The list of 4 metrics for the chart axis (X,Y,Color,Size)",
      defaultValue = MotionChartPlugin.DEFAULT_AXIS_METRICS),
  @Property(
      key = MotionChartPlugin.METRICS_LIST_PROP,
      name = "Chart metrics list",
      description = "The list of available metrics for the chart",
      defaultValue = MotionChartPlugin.DEFAULT_METRICS)
})
public class MotionChartPlugin implements Plugin {
  
  public static final String HEIGHT_PROP = "sonar.motionchart.height";
  public static final String WIDTH_PROP = "sonar.motionchart.width";
  public static final String DEFAULT_HEIGHT = "600";
  public static final String DEFAULT_WIDTH = "800";
  
  public static final String AXIS_METRICS_PROP = "sonar.motionchart.metrics.axis";
  // Axis Order : X, Y, Color, Size
  public static final String DEFAULT_AXIS_METRICS = "violations_density,coverage,function_complexity,complexity";
  
  public static final String METRICS_LIST_PROP = "sonar.motionchart.metrics.list";
  
  public static final String DEFAULT_METRICS = "duplicated_lines_density,test_success_density,public_documented_api_density,uncovered_lines," +
                                               "comment_lines_density,ncloc,test_execution_time,weighted_violations,function_complexity"; 

  public String getKey() {
    return "gwt-motionchart";
  }

  public String getName() {
    return "Motion chart";
  }

  public String getDescription() {
    return "GWT metrics history motion chart plugin";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    extensions.add(RubyMotionChartPage.class);
    extensions.add(RubyMotionChartWebService.class);
    return extensions;
  }

  @Override
  public String toString() {
    return getKey();
  }
}
