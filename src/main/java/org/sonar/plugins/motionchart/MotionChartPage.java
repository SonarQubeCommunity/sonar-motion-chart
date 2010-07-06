/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
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

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.NavigationSection;
import org.sonar.api.web.RubyRailsPage;
import org.sonar.api.web.UserRole;

@NavigationSection({NavigationSection.RESOURCE, NavigationSection.HOME})
@UserRole(UserRole.VIEWER)
public class MotionChartPage extends AbstractRubyTemplate implements RubyRailsPage {

  public static final String HEIGHT_KEY = "sonar.motionchart.height";
  public static final String WIDTH_KEY = "sonar.motionchart.width";
  public static final String DEFAULT_HEIGHT = "600";
  public static final String DEFAULT_WIDTH = "800";

  public static final String DEFAULT_METRICS_KEY = "sonar.motionchart.defaultmetrics";
  /**
   * Comma-separated list of the 4 axis metrics loaded by default : X, Y, color, size.
   */
  public static final String DEFAULT_METRICS_VALUE = "violations_density,coverage,function_complexity,complexity";

  public static final String ADDITIONAL_METRICS_KEY = "sonar.motionchart.additionalmetrics";

  public static final String ADDITIONAL_METRICS_DEFAULT_VALUE = "duplicated_lines_density,public_documented_api_density,uncovered_lines," +
      "ncloc,test_execution_time,function_complexity";
  
  public String getTitle() {
    return "Motion chart";
  }

  @Override
  public String getTemplatePath() {
    return "/org/sonar/plugins/motionchart/motionchart.html.erb";
  }

  public String getId() {
    return getClass().getName();
  }

  
  /* USEFUL METHODS FOR THE JRUBY SIDE */


  public String getHeightKey() {
    return HEIGHT_KEY;
  }

  public String getWidthKey() {
    return WIDTH_KEY;
  }

  public String getDefaultHeight() {
    return DEFAULT_HEIGHT;
  }

  public String getDefaultWidth() {
    return DEFAULT_WIDTH;
  }

  public String getDefaultMetricsKey() {
    return DEFAULT_METRICS_KEY;
  }

  public String getDefaultMetricsValue() {
    return DEFAULT_METRICS_VALUE;
  }

  public String getAdditionalMetricsKey() {
    return ADDITIONAL_METRICS_KEY;
  }

  public String getAdditionalMetricsDefaultValue() {
    return ADDITIONAL_METRICS_DEFAULT_VALUE;
  }
}